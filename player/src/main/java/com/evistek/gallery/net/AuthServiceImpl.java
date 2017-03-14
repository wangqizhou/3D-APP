package com.evistek.gallery.net;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class AuthServiceImpl implements IAuthService {

    private static final String ALGORITHM = "DES";
    private static final String CHARSET = "UTF-8";
    private static final int KEY_LENGHT = 8;

    @Override
    public SecretKey generateKey(String key) {
        try {
            if (key.length() < KEY_LENGHT) {
                for (int i = 0; i < (KEY_LENGHT - key.length()); i++) {
                    key += "0";
                }
            }
            DESKeySpec keySpec = new DESKeySpec(key.getBytes(CHARSET));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            return keyFactory.generateSecret(keySpec);
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public SecretKey generateKeyFromFile(String filePath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String generateRandomString(int length) {
        StringBuffer buffer = new StringBuffer("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        StringBuffer stringBuffer = new StringBuffer();

        SecureRandom secureRandom = new SecureRandom();
        int range = buffer.length();
        for (int i = 0; i < length; i++) {
            stringBuffer.append(buffer.charAt(secureRandom.nextInt(range)));
        }

        return stringBuffer.toString();
    }

    @Override
    public byte[] encrypt(String content, SecretKey key) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(content.getBytes(CHARSET));
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String encryptToBase64String(String content, SecretKey key) {
        return Base64.encodeToString(encrypt(content, key), Base64.NO_WRAP);
    }

    @Override
    public byte[] decrypt(byte[] content, SecretKey key) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] decryptFromBase64String(String content, SecretKey key) {
        return decrypt(Base64.decode(content, Base64.NO_WRAP), key);
    }
}
