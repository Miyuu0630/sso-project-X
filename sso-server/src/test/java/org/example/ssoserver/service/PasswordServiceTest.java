package org.example.ssoserver.service;

import org.example.ssoserver.service.impl.PasswordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码服务测试类
 * 测试MD5+盐值密码加密和验证功能
 */
@SpringBootTest
@DisplayName("密码服务测试")
public class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordServiceImpl();
    }

    @Test
    @DisplayName("测试生成盐值")
    void testGenerateSalt() {
        String salt1 = passwordService.generateSalt();
        String salt2 = passwordService.generateSalt();
        
        // 盐值不应为空
        assertNotNull(salt1);
        assertNotNull(salt2);
        
        // 盐值长度应为32位
        assertEquals(32, salt1.length());
        assertEquals(32, salt2.length());
        
        // 两次生成的盐值应该不同
        assertNotEquals(salt1, salt2);
        
        System.out.println("生成的盐值1: " + salt1);
        System.out.println("生成的盐值2: " + salt2);
    }

    @Test
    @DisplayName("测试MD5+盐值密码加密")
    void testEncodePasswordWithSalt() {
        String password = "test123456";
        String salt = "abcd1234567890abcd1234567890abcd";
        
        String encrypted1 = passwordService.encodePasswordWithSalt(password, salt);
        String encrypted2 = passwordService.encodePasswordWithSalt(password, salt);
        
        // 加密结果不应为空
        assertNotNull(encrypted1);
        assertNotNull(encrypted2);
        
        // 相同密码和盐值应产生相同的加密结果
        assertEquals(encrypted1, encrypted2);
        
        // 加密结果应为32位MD5哈希
        assertEquals(32, encrypted1.length());
        
        System.out.println("原始密码: " + password);
        System.out.println("盐值: " + salt);
        System.out.println("加密结果: " + encrypted1);
    }

    @Test
    @DisplayName("测试不同盐值产生不同加密结果")
    void testDifferentSaltProducesDifferentResult() {
        String password = "test123456";
        String salt1 = "salt1234567890abcd1234567890abcd";
        String salt2 = "salt2234567890abcd1234567890abcd";
        
        String encrypted1 = passwordService.encodePasswordWithSalt(password, salt1);
        String encrypted2 = passwordService.encodePasswordWithSalt(password, salt2);
        
        // 不同盐值应产生不同的加密结果
        assertNotEquals(encrypted1, encrypted2);
        
        System.out.println("相同密码，不同盐值:");
        System.out.println("盐值1: " + salt1 + " -> " + encrypted1);
        System.out.println("盐值2: " + salt2 + " -> " + encrypted2);
    }

    @Test
    @DisplayName("测试密码验证")
    void testMatchesWithSalt() {
        String password = "myPassword123";
        String salt = passwordService.generateSalt();
        String encrypted = passwordService.encodePasswordWithSalt(password, salt);
        
        // 正确密码应验证成功
        assertTrue(passwordService.matchesWithSalt(password, encrypted, salt));
        
        // 错误密码应验证失败
        assertFalse(passwordService.matchesWithSalt("wrongPassword", encrypted, salt));
        
        // 错误盐值应验证失败
        String wrongSalt = passwordService.generateSalt();
        assertFalse(passwordService.matchesWithSalt(password, encrypted, wrongSalt));
        
        System.out.println("密码验证测试:");
        System.out.println("原始密码: " + password);
        System.out.println("盐值: " + salt);
        System.out.println("加密结果: " + encrypted);
        System.out.println("验证结果: " + passwordService.matchesWithSalt(password, encrypted, salt));
    }

    @Test
    @DisplayName("测试密码强度检查")
    void testPasswordStrength() {
        // 弱密码
        assertEquals(0, passwordService.checkPasswordStrength("123456"));
        assertEquals(0, passwordService.checkPasswordStrength("password"));
        assertEquals(0, passwordService.checkPasswordStrength("abc"));
        
        // 中等密码
        assertEquals(1, passwordService.checkPasswordStrength("abc123"));
        assertEquals(1, passwordService.checkPasswordStrength("password123"));
        
        // 强密码
        assertEquals(2, passwordService.checkPasswordStrength("MyPassword123!"));
        assertEquals(2, passwordService.checkPasswordStrength("Secure@Pass123"));
        
        System.out.println("密码强度测试完成");
    }

    @Test
    @DisplayName("测试弱密码检查")
    void testWeakPasswordCheck() {
        // 常见弱密码
        assertTrue(passwordService.isWeakPassword("123456"));
        assertTrue(passwordService.isWeakPassword("password"));
        assertTrue(passwordService.isWeakPassword("admin"));
        
        // 纯数字、纯字母
        assertTrue(passwordService.isWeakPassword("123456789"));
        assertTrue(passwordService.isWeakPassword("abcdefgh"));
        assertTrue(passwordService.isWeakPassword("ABCDEFGH"));
        
        // 强密码
        assertFalse(passwordService.isWeakPassword("MySecure123!"));
        assertFalse(passwordService.isWeakPassword("Complex@Pass456"));
        
        System.out.println("弱密码检查测试完成");
    }

    @Test
    @DisplayName("测试密码格式验证")
    void testPasswordValidation() {
        // 有效密码
        assertTrue(passwordService.isValidPassword("abc123"));
        assertTrue(passwordService.isValidPassword("MyPassword123"));
        
        // 无效密码
        assertFalse(passwordService.isValidPassword(""));
        assertFalse(passwordService.isValidPassword("abc"));  // 太短
        assertFalse(passwordService.isValidPassword("123"));  // 太短
        assertFalse(passwordService.isValidPassword("abcdef")); // 只有字母
        assertFalse(passwordService.isValidPassword("123456")); // 只有数字
        
        System.out.println("密码格式验证测试完成");
    }

    @Test
    @DisplayName("测试空值和异常情况")
    void testNullAndExceptionCases() {
        String salt = passwordService.generateSalt();
        
        // 测试空密码
        assertThrows(IllegalArgumentException.class, () -> {
            passwordService.encodePasswordWithSalt("", salt);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            passwordService.encodePasswordWithSalt(null, salt);
        });
        
        // 测试空盐值
        assertThrows(IllegalArgumentException.class, () -> {
            passwordService.encodePasswordWithSalt("password", "");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            passwordService.encodePasswordWithSalt("password", null);
        });
        
        // 测试验证时的空值
        assertFalse(passwordService.matchesWithSalt("", "hash", salt));
        assertFalse(passwordService.matchesWithSalt("password", "", salt));
        assertFalse(passwordService.matchesWithSalt("password", "hash", ""));
        
        System.out.println("空值和异常情况测试完成");
    }

    @Test
    @DisplayName("测试完整的注册登录流程")
    void testCompleteRegistrationLoginFlow() {
        // 模拟注册流程
        String username = "testuser";
        String password = "MyPassword123!";
        
        // 1. 生成盐值
        String salt = passwordService.generateSalt();
        assertNotNull(salt);
        assertEquals(32, salt.length());
        
        // 2. 加密密码
        String encryptedPassword = passwordService.encodePasswordWithSalt(password, salt);
        assertNotNull(encryptedPassword);
        assertEquals(32, encryptedPassword.length());
        
        // 3. 模拟存储到数据库（这里只是验证）
        System.out.println("=== 注册流程 ===");
        System.out.println("用户名: " + username);
        System.out.println("原始密码: " + password);
        System.out.println("生成盐值: " + salt);
        System.out.println("加密密码: " + encryptedPassword);
        
        // 4. 模拟登录验证
        System.out.println("\n=== 登录验证 ===");
        String loginPassword = "MyPassword123!";  // 用户输入的密码
        boolean loginSuccess = passwordService.matchesWithSalt(loginPassword, encryptedPassword, salt);
        assertTrue(loginSuccess);
        System.out.println("登录密码: " + loginPassword);
        System.out.println("验证结果: " + (loginSuccess ? "成功" : "失败"));
        
        // 5. 测试错误密码
        String wrongPassword = "WrongPassword123!";
        boolean wrongLoginResult = passwordService.matchesWithSalt(wrongPassword, encryptedPassword, salt);
        assertFalse(wrongLoginResult);
        System.out.println("错误密码: " + wrongPassword);
        System.out.println("验证结果: " + (wrongLoginResult ? "成功" : "失败"));
        
        System.out.println("\n完整流程测试通过！");
    }
}
