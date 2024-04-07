package com.esin.base.encrypt;

import com.esin.base.utility.FileUtil;
import com.esin.base.utility.Logger;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtil {
    private static Logger logger = Logger.getLogger(RSAUtil.class);

    private static final String KEY_ALGORITHM = "RSA";

    private static final String pubKeyEnc = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEApxmAAPx905MRfec0UQa7UBNo9CAnBqeYHAt86cfPy0i41y0EnYelxIiNXYwS3b36qi7J-prLr0v6A2cQ0yu38QIDAQABAkBDVmlY_GcQXdHMtOFGTboqOMgwqMxRSDRqCM8bvqOkPU-w_PQ1IHLmQkJmNbP7yS3mm9RBBVwvuo77NNO-outpAiEA7kfZ3zmbKBpJciJFPaM8d7CEVApknk2nXtslmi1ZBe8CIQCzhpM-9eENctQRDI3TI8YG4jrm4FttNf8dfuB7hNwAHwIhAKY2IokussdHhuIBe9EiE6Td0YCZ-1PdkRl-vn0MkuOrAiBq5KBcBq9AALVAiYcQxuYsRxvnSINnnZUPjPJA2bTc5QIgPdo2TAgy05DhJ8je6SSQEgcYKkDR5mlGuXqVW2XJj_c";
    private static final String priKeyEnc = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKcZgAD8fdOTEX3nNFEGu1ATaPQgJwanmBwLfOnHz8tIuNctBJ2HpcSIjV2MEt29-qouyfqay69L-gNnENMrt_ECAwEAAQ";

    public static String encrypt(String text) {
        return encrypt(text, pubKeyEnc);
    }

    public static String decrypt(String text) {
        return decrypt(text, priKeyEnc);
    }

    public static String encrypt(String text, String key) {
        try {
            byte[] keys = Base64.getUrlDecoder().decode(key);
            byte[] values = encrypt(text.getBytes(FileUtil.UTF8), keys);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(values);
        } catch (Exception e) {
            logger.error("encrypt: " + text + ", " + key, e);
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String text, String key) {
        try {
            byte[] keys = Base64.getUrlDecoder().decode(key);
            byte[] values = Base64.getUrlDecoder().decode(text);
            values = decrypt(values, keys);
            return new String(values, FileUtil.UTF8);
        } catch (Exception e) {
            logger.error("decrypt: " + text + ", " + key, e);
            throw new RuntimeException(e);
        }
    }

    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    private static void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(512);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            System.out.println("Private Key : ");
            System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(keyPair.getPrivate().getEncoded()));
            System.out.println("Public Key : ");
            System.out.println(Base64.getUrlEncoder().withoutPadding().encodeToString(keyPair.getPublic().getEncoded()));
        } catch (Exception e) {
            logger.error("generateKey", e);
        }
    }

    public static void main(String[] args) {
//        generateKeyPair();
        System.out.println(encrypt("123456"));
        System.out.println(decrypt(encrypt("123456")));
    }

}
