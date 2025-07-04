package com.pdsu.charge_palteform.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5签名工具类
 */
public class MD5Util {
    private static final Logger log = LoggerFactory.getLogger(MD5Util.class);
    
    /**
     * 计算MD5签名
     * 
     * @param content 内容
     * @return MD5签名（大写）
     */
    public static String md5(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(content.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5签名异常: ", e);
            throw new RuntimeException("MD5签名失败", e);
        }
    }
    
    /**
     * 计算签名
     * 
     * @param content 需要签名的内容
     * @param key 签名密钥
     * @return 签名结果（大写）
     */
    public static String sign(String content, String key) {
        return md5(content + key);
    }
} 