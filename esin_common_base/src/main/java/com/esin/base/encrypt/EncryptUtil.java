package com.esin.base.encrypt;

import com.esin.base.executor.IExecutorAR;
import com.esin.base.utility.FileUtil;
import com.esin.base.utility.Utility;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class EncryptUtil {

    private static String handle(String text, IExecutorAR<String, String> executor, boolean isWrapOrNot) {
        if (Utility.isEmpty(text)) {
            return text;
        }
        if (isWrapOrNot) {
            if (!text.startsWith("ENC(") || !text.endsWith(")")) {
                text = "ENC(" + executor.doExecute(text) + ")";
            }
        } else {
            if (text.startsWith("ENC(") && text.endsWith(")")) {
                text = text.substring("ENC(".length(), text.length() - 1);
                text = executor.doExecute(text);
            }
        }
        return text;
    }

    public static String encryptRSA(String text) {
        return handle(text, RSAUtil::encrypt, true);
    }

    public static String decryptRSA(String text) {
        return handle(text, RSAUtil::decrypt, false);
    }

    public static String encodeBase64(String text) {
        return handle(text, value -> Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes()), true);
    }

    public static String decodeBase64(String text) {
        return handle(text, value -> new String(Base64.getUrlDecoder().decode(value)), false);
    }

    public static String encryptMD5(String text) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(text.getBytes(FileUtil.UTF8));
            return Hex.encodeHexString(digest);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return text;
        }
    }

    public static String generatePassword(int length) {
        String[] values = {
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                "abcdefghijklmnopqrstuvwxyz",
                "0123456789",
                "~!@#$%^&*()_+`-={}|[]:;'<>?,./",
        };
        int index = new Random().nextInt(values.length);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String text = values[(i + index) % values.length];
            sb.append(text.charAt(new Random().nextInt(text.length())));
        }
        return sb.toString();
    }

}
