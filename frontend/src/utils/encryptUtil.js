// src/utils/encryptUtil.tsx
import { JSEncrypt } from 'jsencrypt';
/**
 * RSA 공개키로 문자열을 암호화
 * @param {string} plainText - 암호화할 평문
 * @param {string} publicKey - RSA 공개키
 * @returns {string|null} 암호화된 문자열 or null
 */
export const encryptPassword = (plainText, publicKey) => {
    if (!plainText || !publicKey)
        return null;
    // console.log("publicKey",publicKey);
    const encryptor = new JSEncrypt();
    encryptor.setPublicKey(publicKey);
    const encrypted = encryptor.encrypt(plainText);
    console.log("encrypted", encrypted);
    return encrypted === false ? null : encrypted;
};
