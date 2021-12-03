package Security;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SecurityLayer {

    public static final String ASYMMETRIC = "RSA";
    public static final String SYMMETRIC = "AES";
    public static final String SIGNATURE = "SHA256withRSA";
    private static int size = 256;
    private DataInputStream in;
    private DataOutputStream out;
    private byte[] key;

    //hasher

    public void init(InputStream in, OutputStream out){
        this.in = new DataInputStream(new BufferedInputStream(in));
        this.out = new DataOutputStream(new BufferedOutputStream(out));
    }

    public void sendBytes(byte[] data) throws IOException {
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    public byte[] receiveBytes() throws IOException {
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        return data;
    }

    public byte[] receive() throws IOException {
        byte[] encrypted = receiveBytes();
        try{
            byte[] decrypted = decrypt(encrypted);
            return decrypted;
            //add digest verification
        }catch(NoSuchPaddingException |
                NoSuchAlgorithmException |
                InvalidKeyException |
                BadPaddingException |
                IllegalBlockSizeException e){
            throw new IOException("Decryption failed.", e);
        }
    }

    public void send(byte[] data) throws IOException {
        try{
            byte[] encrypted = encrypt(data);
            sendBytes(encrypted);
        }catch (NoSuchPaddingException |
                NoSuchAlgorithmException |
                InvalidKeyException |
                BadPaddingException |
                IllegalBlockSizeException e){
            e.printStackTrace();
        }

    }

    private byte[] encrypt(byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec secretKey = new SecretKeySpec(key, SYMMETRIC);
        Cipher cipher = Cipher.getInstance(SYMMETRIC);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    private byte[] decrypt( byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec secretKey = new SecretKeySpec(key, SYMMETRIC);
        Cipher cipher = Cipher.getInstance(SYMMETRIC);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    public void clientHandshake() throws IOException, GeneralSecurityException {
        KeyPair clientKeys = loadKeys("./keys/client", "./keys/client_pub");
        PublicKey server_pub = loadKey("./keys/server_pub");

        try{
            sendBytes(clientKeys.getPublic().getEncoded());
            System.out.println("Client pub sent.");//--------------------------
            //gen aes key
            SecretKey aes = genAES();
            byte[] aes_encrypted = rsaEncrypt(server_pub, aes.getEncoded());
            sendBytes(aes_encrypted);
            System.out.println("AES sent");//------------------------
            //send

            //encrypt hash(aes) with client_priv  SIGNATURE
            byte[] sig = signData(clientKeys.getPrivate(), aes.getEncoded());
            sendBytes(sig);
            System.out.println("Sig sent");//------------------------
            //send sig

            key = aes.getEncoded();
        }catch (NoSuchPaddingException | InvalidKeyException | SignatureException
                | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();///----------------------
            throw new GeneralSecurityException("Secure connection failed.", e);
        }

    }

    public void serverHandshake() throws IOException, GeneralSecurityException {
        KeyPair serverKeys = loadKeys("./keys/server", "./keys/server_pub");

        try{
            byte[] client_pub_bytes = receiveBytes();
            System.out.println("received client_pub_bytes: "+client_pub_bytes.length);//-------------------
            PublicKey client_pub = rsaDecodePublicKey(client_pub_bytes);
            System.out.println("decoded public key");//-------------------
            //receive aes key
            //decrypt aes with server_priv
            byte[] aes_encrypted = receiveBytes();
            System.out.println("received aes_encrypted: "+aes_encrypted.length);//-------------------
            byte[] aes_bytes = rsaDecrypt(serverKeys.getPrivate(), aes_encrypted);
            System.out.println("decrypted aes key bytes");//-------------------

            //receive sig
            byte[] signature = receiveBytes();
            boolean verifies = verifySignature(client_pub, signature, aes_bytes);
            if(!verifies){
                throw new GeneralSecurityException("Verification failed.");
            }
            key = aes_bytes;

        }catch (NoSuchPaddingException | InvalidKeyException | SignatureException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();//---------------------------------
            throw new GeneralSecurityException("Secure connection failed.", e);
        }
    }


    public byte[] rsaDecrypt(PrivateKey key, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ASYMMETRIC);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public byte[] rsaEncrypt(PublicKey key, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ASYMMETRIC);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public boolean verifySignature(PublicKey key, byte[] signature, byte[] data) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Signature sign = Signature.getInstance(SIGNATURE);
        sign.initVerify(key);
        sign.update(data);
        return sign.verify(signature);
    }

    public byte[] signData(PrivateKey key, byte[] data) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Signature sign = Signature.getInstance(SIGNATURE);
        sign.initSign(key);
        sign.update(data);
        return sign.sign();
    }

    public KeyPair loadKeys(String keyFile, String keyFilePub) throws IOException{
        try (ObjectInputStream kf = new ObjectInputStream(Files.newInputStream(Paths.get(keyFile)));
             ObjectInputStream kfp = new ObjectInputStream(Files.newInputStream(Paths.get(keyFilePub)))){

            KeyFactory factory = KeyFactory.getInstance(ASYMMETRIC);

            BigInteger kfModulus = (BigInteger) kf.readObject();
            BigInteger kfExponent =  (BigInteger) kf.readObject();
            BigInteger kfpModulus = (BigInteger) kfp.readObject();
            BigInteger kfpExponent =  (BigInteger) kfp.readObject();

            RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(kfModulus, kfExponent);
            RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(kfpModulus, kfpExponent);
            PrivateKey privateKey = factory.generatePrivate(rsaPrivateKeySpec);
            PublicKey publicKey = factory.generatePublic(rsaPublicKeySpec);

            return new KeyPair(publicKey, privateKey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | ClassNotFoundException e){
            throw new IOException("Key loading exception.", e);
        }
    }

    public PublicKey loadKey(String KeyFilePub) throws IOException{
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(Paths.get(KeyFilePub)))) {
            BigInteger modulus = (BigInteger) in.readObject();
            BigInteger exponent = (BigInteger) in.readObject();

            RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance(ASYMMETRIC);
            return factory.generatePublic(rsaPublicKeySpec);

        } catch (InvalidKeySpecException | NoSuchAlgorithmException | ClassNotFoundException e) {
            throw new IOException("Key loading exception.", e);
        }
    }

    private SecretKey genAES() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(SYMMETRIC);
        keyGen.init(size); // 256
        return keyGen.generateKey();
    }

    private PublicKey rsaDecodePublicKey(byte[] encoded) throws InvalidKeySpecException, NoSuchAlgorithmException {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC);
        return keyFactory.generatePublic(pubKeySpec);
    }
}










