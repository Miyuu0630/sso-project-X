package org.example.ssoserver.util;

import cn.hutool.crypto.digest.DigestUtil;
import org.junit.jupiter.api.Test;

/**
 * 密码验证测试类
 * 用于验证数据库中的密码哈希是否正确
 */
public class PasswordVerificationTest {

    @Test
    public void testPersonalUserPassword() {
        // 数据库中的数据
        String password = "123456";
        String salt = "c2d3e4f5g6h789012345678901234cd";
        String expectedHash = "5f4dcc3b5aa765d61d8327deb882cf99";
        
        // 计算实际哈希值
        String actualHash = DigestUtil.md5Hex(password + salt);
        
        System.out.println("=== personal_user 密码验证 ===");
        System.out.println("密码: " + password);
        System.out.println("盐值: " + salt);
        System.out.println("期望哈希: " + expectedHash);
        System.out.println("实际哈希: " + actualHash);
        System.out.println("是否匹配: " + expectedHash.equals(actualHash));
        
        // 使用工具类验证
        boolean matches = Md5SaltUtil.matches(password, expectedHash, salt);
        System.out.println("工具类验证: " + matches);
    }
    
    @Test
    public void testAllUsersPasswords() {
        System.out.println("=== 验证所有用户密码 ===");
        
        // admin 用户
        testUser("admin", "admin123456", "a1b2c3d4e5f6789012345678901234ab", "c091d2cf4a0c12813f546fa11739ea40");
        
        // testuser
        testUser("testuser", "testpass", "b1c2d3e4f5g6789012345678901234bc", "8d969eef6ecad3c29a3a629280e686cf");
        
        // personal_user
        testUser("personal_user", "123456", "c2d3e4f5g6h789012345678901234cd", "5f4dcc3b5aa765d61d8327deb882cf99");
        
        // enterprise_user
        testUser("enterprise_user", "123456", "d3e4f5g6h7i89012345678901234de", "25d55ad283aa400af464c76d713c07ad");
        
        // airline_user
        testUser("airline_user", "123456", "e4f5g6h7i8j9012345678901234ef", "25f9e794323b453885f5181f1b624d0b");
    }
    
    private void testUser(String username, String password, String salt, String expectedHash) {
        String actualHash = DigestUtil.md5Hex(password + salt);
        boolean matches = expectedHash.equals(actualHash);
        
        System.out.println("\n--- " + username + " ---");
        System.out.println("密码: " + password);
        System.out.println("盐值: " + salt);
        System.out.println("期望哈希: " + expectedHash);
        System.out.println("实际哈希: " + actualHash);
        System.out.println("是否匹配: " + matches);
        
        if (!matches) {
            System.out.println("❌ 密码哈希不匹配！");
            // 生成正确的哈希值
            System.out.println("正确的SQL更新语句:");
            System.out.println("UPDATE sys_user SET password = '" + actualHash + "' WHERE username = '" + username + "';");
        } else {
            System.out.println("✅ 密码哈希正确");
        }
    }
    
    @Test
    public void generateCorrectHashes() {
        System.out.println("=== 生成正确的密码哈希 ===");
        
        // 为所有用户生成正确的密码哈希
        System.out.println("-- 更新用户密码的SQL语句");
        
        generateHashForUser("admin", "admin123456", "a1b2c3d4e5f6789012345678901234ab");
        generateHashForUser("testuser", "testpass", "b1c2d3e4f5g6789012345678901234bc");
        generateHashForUser("personal_user", "123456", "c2d3e4f5g6h789012345678901234cd");
        generateHashForUser("enterprise_user", "123456", "d3e4f5g6h7i89012345678901234de");
        generateHashForUser("airline_user", "123456", "e4f5g6h7i8j9012345678901234ef");
    }
    
    private void generateHashForUser(String username, String password, String salt) {
        String hash = DigestUtil.md5Hex(password + salt);
        System.out.println("UPDATE sys_user SET password = '" + hash + "' WHERE username = '" + username + "';");
    }
}
