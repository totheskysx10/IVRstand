package com.good.ivrstand.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Сервис шифрования
 */
@Component
public class EncodeService {
    private static final String ALGORITHM = "AES";

    private final String secretKey;

    public EncodeService(@Value("${auth.password-encrypt-key}") String secretKey) {
        this.secretKey = secretKey;
    }


    /**
     * Шифрует текст алгоритмом AES по заданному ключу
     *
     * @param plainText текст
     * @return зашифрованный текст
     * @throws RuntimeException ошибка при шифровании
     */
    public String encrypt(String plainText) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(this.secretKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при шифровании текста: " + plainText, e);
        }
    }

    /**
     * Расшифровывает текст алгоритмом AES по заданному ключу
     *
     * @param encryptedText шифрованный текст
     * @return расшифрованный текст
     * @throws RuntimeException ошибка при дешифровании
     */
    public String decrypt(String encryptedText) {
        try {
            encryptedText = encryptedText.replaceAll("\\s", "+");
            SecretKeySpec secretKey = new SecretKeySpec(this.secretKey.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при дешифровании текста: " + encryptedText, e);
        }
    }

    /**
     * Генерирует хэш описания
     *
     * @param description описание
     * @return хэш
     */
    public String generateHashForAudio(String description) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(description.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: algo not found", e);
        }

    }
}
