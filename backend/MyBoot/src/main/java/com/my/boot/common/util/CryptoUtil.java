package com.my.boot.common.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

@Component
public class CryptoUtil {
    private static final String AES_KEY = "Ul79596jag7im1ecFacils84keyuk710"; // AES 암호화 키
    private static final String URL_KEY = "kElecFacilitieknchecs54272359"; // URL 신뢰성 향상을 위한 키
    private static final byte[] IV_BYTES = new byte[16];

    public String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(new Date());
    }

    public String getUrlKey() {
        return URL_KEY + new SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(new Date());
    }

    public String encryptAES(String data) {
        return processAES(data, AES_KEY, Cipher.ENCRYPT_MODE);
    }

    public String decryptAES(String data) {
        return processAES(data, AES_KEY, Cipher.DECRYPT_MODE);
    }

    private String processAES(String data, String key, int mode) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(mode, secretKey, new IvParameterSpec(IV_BYTES));
            byte[] processedData = (mode == Cipher.ENCRYPT_MODE)
                    ? cipher.doFinal(data.getBytes(StandardCharsets.UTF_8))
                    : cipher.doFinal(Base64.getUrlDecoder().decode(data)); // ✅ AES는 URL-safe 사용 중

            return mode == Cipher.ENCRYPT_MODE
                    ? Base64.getUrlEncoder().withoutPadding().encodeToString(processedData) // ✅ AES는 URL-safe 사용 중
                    : new String(processedData, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("AES processing failed", e);
        }
    }

    /**
     * Base64 인코딩된 공개키 문자열을 PublicKey 객체로 변환 (X.509)
     */
    public PublicKey getPublicKeyFromBase64(String base64PublicKey) {
        try {
            // 키 복원은 일반 Base64 디코더 사용 (키 생성 시 일반 Base64를 사용했기 때문)
            byte[] decodedKey = Base64.getDecoder().decode(base64PublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve public key from Base64", e);
        }
    }

    /**
     * Base64 인코딩된 개인키 문자열을 PrivateKey 객체로 변환 (PKCS8)
     */
    public PrivateKey getPrivateKeyFromBase64(String base64PrivateKey) {
        try {
            // 키 복원은 일반 Base64 디코더 사용
            byte[] decodedKey = Base64.getDecoder().decode(base64PrivateKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve private key from Base64", e);
        }
    }

    public String encryptRSA(String plainText, String base64PublicKey) {
        try {
            PublicKey publicKey = getPublicKeyFromBase64(base64PublicKey);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 🌟 수정: RSA 암호화 후 URL-Safe Base64 인코더 사용
            return Base64.getUrlEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("RSA encryption failed", e);
        }
    }

    /**
     * Private Key로 RSA 복호화 수행 (RSA/ECB/PKCS1Padding 사용)
     */
    public String decryptRSA(String encryptedText, String base64PrivateKey) {
        try {
            PrivateKey privateKey = getPrivateKeyFromBase64(base64PrivateKey);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            // 🌟 수정: RSA 복호화 전 URL-Safe Base64 디코더 사용 (BadPaddingException 방지)
            byte[] decryptedBytes = cipher.doFinal(Base64.getUrlDecoder().decode(encryptedText));

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("RSA decryption failed", e);
        }
    }


    /**
     * 2048비트 RSA 키 쌍 생성
     */
    public KeyPair generateRSAKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }
}