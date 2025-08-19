package org.example.ssoserver.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 数据加密工具类
 */
@Slf4j
public class EncryptUtil {
    
    /**
     * AES密钥(实际使用时应该从配置文件或环境变量中获取)
     */
    private static final String AES_KEY = "MySecretKey12345"; // 16位密钥
    
    private static final AES aes = new AES(AES_KEY.getBytes(StandardCharsets.UTF_8));
    
    /**
     * AES加密
     */
    public static String encryptAES(String plainText) {
        if (StrUtil.isBlank(plainText)) {
            return plainText;
        }
        try {
            return aes.encryptHex(plainText);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            return plainText;
        }
    }
    
    /**
     * AES解密
     */
    public static String decryptAES(String encryptedText) {
        if (StrUtil.isBlank(encryptedText)) {
            return encryptedText;
        }
        try {
            return aes.decryptStr(encryptedText);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            return encryptedText;
        }
    }
    
    /**
     * 手机号脱敏
     */
    public static String maskPhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    
    /**
     * 邮箱脱敏
     */
    public static String maskEmail(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return email;
        }
        
        String maskedUsername = username.substring(0, 1) + 
                               "*".repeat(username.length() - 2) + 
                               username.substring(username.length() - 1);
        
        return maskedUsername + "@" + domain;
    }
    
    /**
     * 身份证号脱敏
     */
    public static String maskIdCard(String idCard) {
        if (StrUtil.isBlank(idCard) || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }
    
    /**
     * 姓名脱敏
     */
    public static String maskName(String name) {
        if (StrUtil.isBlank(name)) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.substring(0, 1) + "*";
        }
        return name.substring(0, 1) + "*".repeat(name.length() - 2) + name.substring(name.length() - 1);
    }
    
    /**
     * 生成随机密钥
     */
    public static String generateRandomKey(int length) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(length * 8); // 转换为位数
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            log.error("生成随机密钥失败", e);
            return null;
        }
    }
    
    /**
     * 生成随机字符串
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
    
    /**
     * 生成数字验证码
     */
    public static String generateNumericCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }
}
