package Security;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SecurityLayer {

    public static final String ALGORITHM = "RSA";
    public static final String SYMMETRIC = "AES";
    public static final String SIGNATURE = "SHA256withRSA";
    private static int size = 256;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private SecretKey key;
    //aes

    //hasher

    public void init(InputStream in, OutputStream out){
        this.in = new BufferedInputStream(in);
        this.out = new BufferedOutputStream(out);
    }

    public void sendBytes(byte[] data) throws IOException {
        out.write(data);
        out.flush();
    }

    public byte[] receiveBytes() throws IOException {
        byte[] data = new byte[in.available()];
        in.read(data);
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
        Cipher cipher = Cipher.getInstance(SYMMETRIC);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private byte[] decrypt( byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(SYMMETRIC);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public void clientHandshake() throws IOException {
        KeyPair clientKeys = loadKeys("file", "file_pub");
        PublicKey server_pub = loadKey("file1");

        try{
            byte[] client_encrypted = rsaEncrypt(server_pub, clientKeys.getPublic().getEncoded());
            //send client_encrypted
            sendBytes(client_encrypted);

            //gen aes key
            SecretKey aes = genAES();
            byte[] aes_encrypted = rsaEncrypt(server_pub, aes.getEncoded());
            sendBytes(aes_encrypted);
            //send

            //encrypt hash(aes) with client_priv  SIGNATURE
            byte[] sig = signData(clientKeys.getPrivate(), aes.getEncoded());
            sendBytes(sig);
            //send sig

            key = aes;
        }catch (NoSuchPaddingException | InvalidKeyException | SignatureException
                | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            //deny connection
        }

    }

    public void serverHandshake() throws IOException {
        //load keys         KEYPAIR
        KeyPair serverKeys = loadKeys("file", "file_pub");
        //receive client_pub

        try{
            //decrypt client_pub with server_priv
            byte[] client_encrypted = receiveBytes();
            byte[] client_pub_bytes = rsaDecrypt(serverKeys.getPrivate(), client_encrypted);
            PublicKey client_pub = rsaDecodePublicKey(client_pub_bytes);

            //receive aes key
            //decrypt aes with server_priv
            byte[] aes_encrypted = receiveBytes();
            byte[] aes_bytes = rsaDecrypt(serverKeys.getPrivate(), aes_encrypted);
            SecretKey aes = aesDecodeKey(aes_bytes);

            //receive sig
            byte[] signature = receiveBytes();
            boolean verifies = verifySignature(client_pub, signature, aes.getEncoded());
            if(!verifies){
                throw new IOException("Verification failed.");
            }
            key = aes;

        }catch (NoSuchPaddingException | InvalidKeyException | SignatureException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            //deny connection
        }


        //initSession
    }


    public byte[] rsaDecrypt(PrivateKey key, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public byte[] rsaEncrypt(PublicKey key, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
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

            KeyFactory factory = KeyFactory.getInstance(ALGORITHM);

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
            KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
            return factory.generatePublic(rsaPublicKeySpec);

        } catch (InvalidKeySpecException | NoSuchAlgorithmException | ClassNotFoundException e) {
            throw new IOException("Key loading exception.", e);
        }
    }

    private SecretKey genAES() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(size); // 256
        return keyGen.generateKey();
    }

    private PublicKey rsaDecodePublicKey(byte[] encoded) throws InvalidKeySpecException, NoSuchAlgorithmException {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePublic(pubKeySpec);
    }

    private SecretKey aesDecodeKey(byte[] encoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeySpec secretKey = new SecretKeySpec(encoded, "AES");
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("AES");
        return keyFactory.generateSecret(secretKey);
    }
}










