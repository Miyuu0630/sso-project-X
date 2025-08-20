package org.example.ssoserver;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 生成正确的密码哈希值
 */
public class GeneratePasswordHash {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成admin用户的密码哈希
        String adminPassword = "admin123456";
        String adminHash = encoder.encode(adminPassword);
        System.out.println("admin用户密码哈希:");
        System.out.println("明文: " + adminPassword);
        System.out.println("哈希: " + adminHash);
        System.out.println("验证: " + encoder.matches(adminPassword, adminHash));
        System.out.println();
        
        // 生成其他用户的密码哈希
        String userPassword = "123456";
        String userHash = encoder.encode(userPassword);
        System.out.println("普通用户密码哈希:");
        System.out.println("明文: " + userPassword);
        System.out.println("哈希: " + userHash);
        System.out.println("验证: " + encoder.matches(userPassword, userHash));
        System.out.println();
        
        // 生成SQL更新语句
        System.out.println("=== 数据库更新SQL ===");
        System.out.println("-- 更新admin用户密码");
        System.out.println("UPDATE sys_user SET password = '" + adminHash + "' WHERE username = 'admin';");
        System.out.println();
        System.out.println("-- 更新其他用户密码");
        System.out.println("UPDATE sys_user SET password = '" + userHash + "' WHERE username IN ('personal_user', 'enterprise_user', 'airline_user');");
    }
}
