package com.evistek.gallery.net;

import javax.crypto.SecretKey;

public interface IAuthService {

    public SecretKey generateKey(String key);

    public SecretKey generateKeyFromFile(String filePath);

    public String generateRandomString(int length);

    public byte[] encrypt(String content, SecretKey key);

    public String encryptToBase64String(String content, SecretKey key);

    public byte[] decrypt(byte[] content, SecretKey key);

    public byte[] decryptFromBase64String(String content, SecretKey key);
}
