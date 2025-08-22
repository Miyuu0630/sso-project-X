package org.example.common.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.digest.BCrypt;
import lombok.extern.slf4j.Slf4j;
import org.example.common.constants.CommonConstants;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * 数据加密工具类
 * 提供密码加密、数据加密、数据脱敏等功能
 */
@Slf4j
public class EncryptUtil {

    /**
     * AES密钥(实际使用时应该从配置文件或环境变量中获取)
     */
    private static final String AES_KEY = "MySecretKey12345"; // 16位密钥

    private static final AES aes = new AES(AES_KEY.getBytes(StandardCharsets.UTF_8));

    /**
     * 密码强度正则表达式
     */
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    // ========================================
    // 密码相关方法
    // ========================================

    /**
     * BCrypt密码加密
     * @param password 明文密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        if (StrUtil.isBlank(password)) {
            return password;
        }
        try {
            return BCrypt.hashpw(password, BCrypt.gensalt());
        } catch (Exception e) {
            log.error("密码加密失败", e);
            // 如果BCrypt失败，降级使用MD5（不推荐生产环境）
            return encryptMD5(password);
        }
    }

    /**
     * MD5加密（仅用于兼容旧数据，不推荐新系统使用）
     * @param password 明文密码
     * @return MD5加密后的密码
     */
    public static String encryptMD5(String password) {
        if (StrUtil.isBlank(password)) {
            return password;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5加密失败", e);
            return password;
        }
    }

    /**
     * 验证密码
     * @param password 明文密码
     * @param hashedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (StrUtil.isBlank(password) || StrUtil.isBlank(hashedPassword)) {
            return false;
        }
        try {
            // 如果是BCrypt格式
            if (hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$")) {
                return BCrypt.checkpw(password, hashedPassword);
            }
            // 兼容MD5格式（旧数据）
            else if (hashedPassword.length() == 32) {
                return hashedPassword.equals(encryptMD5(password));
            }
            // 明文比较（开发测试环境）
            else {
                return password.equals(hashedPassword);
            }
        } catch (Exception e) {
            log.error("密码验证失败", e);
            return false;
        }
    }

    /**
     * 检查密码强度
     * @param password 密码
     * @return 强度等级：0-弱，1-中，2-强
     */
    public static int checkPasswordStrength(String password) {
        if (StrUtil.isBlank(password)) {
            return 0;
        }

        int score = 0;

        // 长度检查
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;

        // 字符类型检查
        if (password.matches(".*[a-z].*")) score++; // 小写字母
        if (password.matches(".*[A-Z].*")) score++; // 大写字母
        if (password.matches(".*\\d.*")) score++;   // 数字
        if (password.matches(".*[@$!%*?&].*")) score++; // 特殊字符

        // 返回强度等级
        if (score <= 2) return 0; // 弱
        if (score <= 4) return 1; // 中
        return 2; // 强
    }

    /**
     * 验证密码格式
     * @param password 密码
     * @return 是否符合要求
     */
    public static boolean isValidPassword(String password) {
        if (StrUtil.isBlank(password)) {
            return false;
        }

        // 长度检查
        if (password.length() < CommonConstants.PASSWORD_MIN_LENGTH ||
            password.length() > CommonConstants.PASSWORD_MAX_LENGTH) {
            return false;
        }

        // 基本要求：至少包含字母和数字
        return password.matches(CommonConstants.PASSWORD_REGEX);
    }
    
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
