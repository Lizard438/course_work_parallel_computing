package Security;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class GenRSA {

    public static void saveKeys(String fileName, BigInteger mod, BigInteger exp) {
        System.out.println("Generating [" + fileName + "] key...");
        try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)))){
            objectOutputStream.writeObject(mod);
            objectOutputStream.writeObject(exp);
            System.out.println("["+fileName + "] key generated successfully\n");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        if(args.length != 2){
            System.err.println("Usage: java GenRSA <publicKeyFile> <privateKeyFile>");
            System.exit(1);
        }
        String publicKeyFile = args[0];
        String privateKeyFile = args[1];
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048, new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec rsaPublicKeySpec = keyFactory.getKeySpec(keyPair.getPublic(), RSAPublicKeySpec.class);
            RSAPrivateKeySpec rsaPrivateKeySpec = keyFactory.getKeySpec(keyPair.getPrivate(), RSAPrivateKeySpec.class);
            saveKeys(publicKeyFile, rsaPublicKeySpec.getModulus(), rsaPublicKeySpec.getPublicExponent());
            saveKeys(privateKeyFile, rsaPrivateKeySpec.getModulus(), rsaPrivateKeySpec.getPrivateExponent());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
}
