// src/utils/encryptUtil.tsx
import { JSEncrypt } from 'jsencrypt';

/**
 * RSA 공개키로 문자열을 암호화
 * @param {string} plainText - 암호화할 평문
 * @param {string} publicKey - RSA 공개키
 * @returns {string|null} 암호화된 문자열 or null
 */
export const encryptPassword = (plainText: string, publicKey: string): string | null => {
    if (!plainText || !publicKey) return null;

    const encryptor = new JSEncrypt();
    encryptor.setPublicKey(publicKey);

    const encrypted = encryptor.encrypt(plainText);
    return encrypted === false ? null : encrypted;
};