package com.esin.base.encrypt;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class ClientRSAUtil {
    private static final String pubKeyEnc = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDvs7Nxlx0/hQw8UvkLGYrNCfwbHXVuTLIK9eemwNhuKr9QOAGzovn318ZpPGxslospbsyjZfM5LE8gOqjmSoswkNK/tVBUvll7Js9R6Qd7/wIAmaf3q47X9bNzwh9XjeOGRHEn7RClWo+OQJa2+j/ivuFBBCWQwziJe9ut7MdM9wIDAQAB";
    private static final String priKeyEnc = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAO+zs3GXHT+FDDxS+QsZis0J/BsddW5Msgr156bA2G4qv1A4AbOi+ffXxmk8bGyWiyluzKNl8zksTyA6qOZKizCQ0r+1UFS+WXsmz1HpB3v/AgCZp/erjtf1s3PCH1eN44ZEcSftEKVaj45Alrb6P+K+4UEEJZDDOIl7263sx0z3AgMBAAECgYEAik7/MIDIUJl9iOU7bCstyseDH0YtNxqr9OUU6EQH3fFueGQIn1VftKFdi+VgjnuDCsIy8+lkoU2uzmLqiA7lJl+4ghg1Nlg81fHKRuQ0bBbu2VhUDpcH8jbbMZQ9SIXmD7OaTNwcv9wUzxdaPlzXyI1Bhrf5saV8H9fgjiUmm+ECQQD/dwLLvajUd2rvQBOaDOwHjEk+9nscJhIVLHpN/yTucBcH0Rljf4KY/dBUNOJDGXSSCUuLpNQkiSY5ZFGMm5unAkEA8DQ8xomCHSLQtI4+Xb3Xj8ffZX0I4k8BZEdBDgt9k6ydbe+6OTZJBSsCTdTG/FYkKSIaFc2cRcWofNN1AZWuMQJBAJfx7VPJZtWYgZ3z+rSx4uFKa3ZrnCXN7wtw/P3PN+Qp/0jC8drgSIk+zd6H5dwLE+6YYLqaOyPP/1A7ftWm6BkCQQCgTpyI+9iETWnwNkZVFY+5e3ESMGIvdv68x/kYwH5sgfUHG8iyyhHtiwicnPa4DV1QvlueVXyH7CRIOm+KQTThAkARLoHvnmOf1Z6DqeTAobH2erVILCejArIsqN7EQDlVu17JIf1yGTIzGa01f3YpNfrAnk0MLci5U9QmHPjV2U/O";

    public static String getPubKeyEnc() {
        return pubKeyEnc;
    }

    public static String decrypt(String text) {
        return decrypt(text, priKeyEnc);
    }

    private static final String UTF_8 = "UTF-8";
    private static final String RSA_ALGORITHM_NOPADDING = "RSA";

    public static String decrypt(String text, String key) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM_NOPADDING);
            byte[] privateKeyArray = key.getBytes();
            byte[] dataArray = text.getBytes();
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyArray));
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM_NOPADDING);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(Base64.decodeBase64(dataArray)), UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}