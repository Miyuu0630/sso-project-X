package org.example.ssoserver.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码迁移工具类
 * 用于生成BCrypt加密密码，帮助迁移明文密码
 */
@Slf4j
public class PasswordMigrationUtil {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static void main(String[] args) {
        // 生成常用密码的BCrypt哈希值
        String[] passwords = {
            "admin123456",  // 管理员密码
            "testpass",     // 测试用户密码
            "123456",       // 通用测试密码
            "personal123",  // 个人用户密码
            "enterprise123", // 企业用户密码
            "airline123"    // 航司用户密码
        };

        System.out.println("=== 密码BCrypt哈希值生成 ===");
        System.out.println();

        for (String password : passwords) {
            String hash = passwordEncoder.encode(password);
            System.out.println("明文密码: " + password);
            System.out.println("BCrypt哈希: " + hash);
            System.out.println("验证结果: " + passwordEncoder.matches(password, hash));
            System.out.println("---");
        }

        System.out.println();
        System.out.println("=== SQL更新语句 ===");
        System.out.println();

        // 生成SQL更新语句
        generateUpdateSQL();
    }

    private static void generateUpdateSQL() {
        String adminHash = passwordEncoder.encode("admin123456");
        String testHash = passwordEncoder.encode("testpass");
        String commonHash = passwordEncoder.encode("123456");

        System.out.println("-- 更新管理员密码");
        System.out.println("UPDATE sys_user SET password = '" + adminHash + "', password_update_time = NOW() WHERE username = 'admin';");
        System.out.println();

        System.out.println("-- 更新测试用户密码");
        System.out.println("UPDATE sys_user SET password = '" + testHash + "', password_update_time = NOW() WHERE username = 'testuser';");
        System.out.println();

        System.out.println("-- 更新其他用户密码 (123456)");
        System.out.println("UPDATE sys_user SET password = '" + commonHash + "', password_update_time = NOW() WHERE username IN ('personal_user', 'enterprise_user', 'airline_user');");
        System.out.println();

        System.out.println("-- 验证更新结果");
        System.out.println("SELECT username, CASE WHEN password LIKE '$2a$%' THEN 'BCrypt加密' ELSE '明文密码' END as password_type FROM sys_user;");
    }

    /**
     * 生成指定密码的BCrypt哈希值
     */
    public static String generateBCryptHash(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 验证密码是否匹配
     */
    public static boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 检查密码是否为BCrypt格式
     */
    public static boolean isBCryptFormat(String password) {
        return password != null && password.startsWith("$2a$");
    }
}
