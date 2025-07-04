package com.pdsu.charge_palteform.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES加密解密工具类
 */
public class AesUtil {
    private static final Logger log = LoggerFactory.getLogger(AesUtil.class);
    
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    
    /**
     * AES加密
     * 
     * @param content 要加密的内容
     * @param key 加密密钥
     * @param iv 初始化向量
     * @return 加密后的Base64字符串
     */
    public static String encrypt(String content, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("AES加密异常: ", e);
            throw new RuntimeException("AES加密失败", e);
        }
    }
    
    /**
     * AES解密
     * 
     * @param encryptedContent 已加密的Base64字符串
     * @param key 解密密钥
     * @param iv 初始化向量
     * @return 解密后的字符串
     */
    public static String decrypt(String encryptedContent, String key, String iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedContent));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密异常: ", e);
            throw new RuntimeException("AES解密失败", e);
        }
    }
} 