package com.my.boot.common.util;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

// 개발/테스트용 SafeDB 대체 클래스
public class MockSafeDB { // 싱글톤 패턴
//AES/CTR/NoPadding 알고리즘 사용
    private static final String CIPHER_ALGORITHM = "AES/CTR/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_SIZE = 16;
    private static final int KEY_SIZE = 32;

    private final byte[] secretKey;
    private static MockSafeDB instance = null;

    private MockSafeDB(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public static MockSafeDB getInstance(String key) {
        if (instance == null) {
            byte[] keyBytes;
            // key 길이가 64자(16진수 32바이트)라면 hex로 간주
            // 길이가 **64자(hex)**이면 → hexStringToByteArray()로 변환
            //그 외에는 → padKey()로 32바이트 맞춤
            if (key.length() == KEY_SIZE * 2) {
                keyBytes = hexStringToByteArray(key);
            } else {
                keyBytes = padKey(key);
            }
            instance = new MockSafeDB(keyBytes);
        }
        return instance;
    }

    public byte[] encrypt(String userName, String tableName, String columnName, byte[] plainData) throws Exception {
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(secretKey, KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal(plainData);
        // 해당 평문을 암호화

        byte[] output = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, output, 0, iv.length);
        System.arraycopy(cipherText, 0, output, iv.length, cipherText.length);

        //랜덤 IV 생성 후 AES CTR 암호화 수행
        //IV + 암호문 형태로 리턴
        return output;
    }

    /*
    * 복호화 메소드
    * */

    public byte[] decrypt(String userName, String tableName, String columnName, byte[] input) throws Exception {
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(input, 0, iv, 0, IV_SIZE);
        byte[] cipherText = new byte[input.length - IV_SIZE];
        System.arraycopy(input, IV_SIZE, cipherText, 0, cipherText.length);

//        input에서 앞의 16바이트는 IV, 나머지는 암호문



        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(secretKey, KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
//        같은 키 + IV로 복호화 수행 → 원문 데이터 복원
        return cipher.doFinal(cipherText);
    }

    // hex → byte[]
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // String → 32byte 패딩
    private static byte[] padKey(String key) {
        byte[] keyBytes = key.getBytes();
        byte[] padded = new byte[KEY_SIZE];
        System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, KEY_SIZE));
        return padded;
    }
}
