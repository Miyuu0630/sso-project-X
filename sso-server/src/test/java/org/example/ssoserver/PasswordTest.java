package org.example.ssoserver;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码验证测试
 */
public class PasswordTest {
    
    @Test
    public void testPasswordVerification() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 数据库中admin用户的密码哈希
        String hashedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXIgBINrS.QSLbm8Qs8Ue6.4NeO";
        
        // 测试密码
        String rawPassword = "admin123456";
        
        // 验证密码
        boolean matches = encoder.matches(rawPassword, hashedPassword);
        
        System.out.println("密码验证结果: " + matches);
        System.out.println("原始密码: " + rawPassword);
        System.out.println("哈希密码: " + hashedPassword);
        
        // 生成新的哈希值进行对比
        String newHash = encoder.encode(rawPassword);
        System.out.println("新生成的哈希: " + newHash);
        System.out.println("新哈希验证: " + encoder.matches(rawPassword, newHash));
    }
    
    @Test
    public void testOtherPasswords() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 测试其他用户的密码（123456）
        String hashedPassword123456 = "$2a$10$7JB720yubVSOfvVMe6/YqO4wkhWGEn67XVb1TqHGjpQYqHrDfbpBG";

        // 测试多个可能的密码
        String[] testPasswords = {"123456", "admin123456", "password", "admin", "123", "000000"};

        for (String pwd : testPasswords) {
            boolean matches = encoder.matches(pwd, hashedPassword123456);
            System.out.println("密码 '" + pwd + "' 验证结果: " + matches);
            if (matches) {
                System.out.println("*** 找到正确密码: " + pwd + " ***");
            }
        }

        // 生成新的正确哈希值
        System.out.println("\n=== 生成新的哈希值 ===");
        String newAdminHash = encoder.encode("admin123456");
        String new123456Hash = encoder.encode("123456");

        System.out.println("admin123456 新哈希: " + newAdminHash);
        System.out.println("123456 新哈希: " + new123456Hash);

        // 验证新哈希
        System.out.println("admin123456 新哈希验证: " + encoder.matches("admin123456", newAdminHash));
        System.out.println("123456 新哈希验证: " + encoder.matches("123456", new123456Hash));
    }
}
