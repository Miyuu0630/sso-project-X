package org.example.ssoserver.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

/**
 * MD5 + 盐值加密工具类
 * 
 * @author SSO Team
 * @since 2.0.0
 */
@Slf4j
public class Md5SaltUtil {
    
    /**
     * 盐值长度
     */
    private static final int SALT_LENGTH = 32;
    
    /**
     * 安全随机数生成器
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /**
     * 生成32位随机盐值
     * 
     * @return 32位随机字符串
     */
    public static String generateSalt() {
        try {
            // 方式1：使用 SecureRandom 生成更安全的随机盐值
            byte[] saltBytes = new byte[16];
            SECURE_RANDOM.nextBytes(saltBytes);
            StringBuilder salt = new StringBuilder();
            for (byte b : saltBytes) {
                salt.append(String.format("%02x", b & 0xff));
            }
            return salt.toString();
            
        } catch (Exception e) {
            log.warn("使用 SecureRandom 生成盐值失败，降级使用 RandomUtil: {}", e.getMessage());
            // 方式2：降级使用 HuTool 的 RandomUtil
            return RandomUtil.randomString(SALT_LENGTH);
        }
    }
    
    /**
     * 使用 MD5 + 盐值加密密码
     * 
     * @param rawPassword 原始密码
     * @param salt 盐值
     * @return 32位 MD5 哈希值
     */
    public static String encrypt(String rawPassword, String salt) {
        if (rawPassword == null || salt == null) {
            throw new IllegalArgumentException("密码和盐值不能为空");
        }
        
        try {
            // MD5(password + salt) - 注意拼接顺序
            String passwordWithSalt = rawPassword + salt;
            String hashedPassword = DigestUtil.md5Hex(passwordWithSalt);
            
            log.debug("密码加密成功: 原始密码长度={}, 盐值长度={}, 哈希值长度={}", 
                     rawPassword.length(), salt.length(), hashedPassword.length());
            
            return hashedPassword;
            
        } catch (Exception e) {
            log.error("密码加密失败: rawPassword={}, salt={}", 
                     rawPassword != null ? "***" : "null", 
                     salt != null ? salt.substring(0, 8) + "***" : "null", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 验证密码是否匹配
     * 
     * @param rawPassword 原始密码
     * @param hashedPassword 数据库中存储的哈希密码
     * @param salt 盐值
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String hashedPassword, String salt) {
        if (rawPassword == null || hashedPassword == null || salt == null) {
            log.warn("密码验证参数不完整: rawPassword={}, hashedPassword={}, salt={}", 
                    rawPassword != null ? "***" : "null",
                    hashedPassword != null ? "***" : "null",
                    salt != null ? "***" : "null");
            return false;
        }
        
        try {
            String inputHash = encrypt(rawPassword, salt);
            boolean matches = inputHash.equals(hashedPassword);
            
            log.debug("密码验证结果: {}", matches ? "匹配" : "不匹配");
            
            return matches;
            
        } catch (Exception e) {
            log.error("密码验证异常", e);
            return false;
        }
    }
    
    /**
     * 验证盐值格式是否正确
     * 
     * @param salt 盐值
     * @return 是否有效
     */
    public static boolean isValidSalt(String salt) {
        return salt != null && salt.length() == SALT_LENGTH && salt.matches("[a-f0-9]+");
    }
    
    /**
     * 验证哈希密码格式是否正确
     * 
     * @param hashedPassword 哈希密码
     * @return 是否有效
     */
    public static boolean isValidHash(String hashedPassword) {
        return hashedPassword != null && hashedPassword.length() == SALT_LENGTH && hashedPassword.matches("[a-f0-9]+");
    }
    
    /**
     * 生成测试用的密码和盐值（仅用于开发测试）
     * 
     * @param rawPassword 原始密码
     * @return 包含盐值和哈希值的数组 [salt, hash]
     */
    public static String[] generateTestPassword(String rawPassword) {
        String salt = generateSalt();
        String hash = encrypt(rawPassword, salt);
        return new String[]{salt, hash};
    }
    
    /**
     * 打印密码加密信息（仅用于开发测试）
     * 
     * @param rawPassword 原始密码
     */
    public static void printPasswordInfo(String rawPassword) {
        String[] result = generateTestPassword(rawPassword);
        System.out.println("=== 密码加密信息 ===");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("盐值: " + result[0]);
        System.out.println("哈希值: " + result[1]);
        System.out.println("验证结果: " + matches(rawPassword, result[1], result[0]));
        System.out.println("==================");
    }
    
    // 测试方法
    public static void main(String[] args) {
        // 测试密码加密
        printPasswordInfo("admin123456");
        printPasswordInfo("system123456");
        printPasswordInfo("test123456");
        
        // 验证已知的密码
        String knownSalt = "a1b2c3d4e5f6789012345678901234ab";
        String knownHash = "c091d2cf4a0c12813f546fa11739ea40";
        String knownPassword = "admin123456";
        
        System.out.println("=== 验证已知密码 ===");
        System.out.println("密码: " + knownPassword);
        System.out.println("盐值: " + knownSalt);
        System.out.println("期望哈希: " + knownHash);
        System.out.println("计算哈希: " + encrypt(knownPassword, knownSalt));
        System.out.println("验证结果: " + matches(knownPassword, knownHash, knownSalt));
    }
}
