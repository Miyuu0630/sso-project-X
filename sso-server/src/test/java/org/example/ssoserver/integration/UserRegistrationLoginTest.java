package org.example.ssoserver.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.ssoserver.dto.RegisterRequest;
import org.example.ssoserver.entity.SysUser;
import org.example.ssoserver.mapper.SysUserMapper;
import org.example.ssoserver.service.PasswordService;
import org.example.ssoserver.service.UserRegisterService;
import org.example.ssoserver.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户注册和登录集成测试
 * 测试完整的注册登录流程
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("用户注册登录集成测试")
public class UserRegistrationLoginTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRegisterService userRegisterService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("测试用户注册流程")
    void testUserRegistration() {
        // 准备注册数据
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser001");
        registerRequest.setPassword("TestPassword123!");
        registerRequest.setRealName("测试用户");
        registerRequest.setPhone("13800138001");
        registerRequest.setEmail("testuser001@example.com");
        registerRequest.setUserType("normal");

        try {
            // 执行注册
            var response = userRegisterService.register(registerRequest);
            assertNotNull(response);
            assertNotNull(response.getUserId());

            // 验证用户是否正确保存到数据库
            SysUser savedUser = sysUserMapper.selectById(response.getUserId());
            assertNotNull(savedUser);
            assertEquals("testuser001", savedUser.getUsername());
            assertEquals("测试用户", savedUser.getRealName());
            assertEquals("13800138001", savedUser.getPhone());
            assertEquals("testuser001@example.com", savedUser.getEmail());
            assertEquals("normal", savedUser.getUserType());

            // 验证密码和盐值
            assertNotNull(savedUser.getPassword());
            assertNotNull(savedUser.getSalt());
            assertEquals(32, savedUser.getSalt().length()); // 盐值长度应为32位
            assertEquals(32, savedUser.getPassword().length()); // MD5哈希长度应为32位

            // 验证密码加密是否正确
            boolean passwordMatches = passwordService.matchesWithSalt(
                "TestPassword123!", 
                savedUser.getPassword(), 
                savedUser.getSalt()
            );
            assertTrue(passwordMatches, "密码验证应该成功");

            System.out.println("=== 注册测试结果 ===");
            System.out.println("用户ID: " + savedUser.getId());
            System.out.println("用户名: " + savedUser.getUsername());
            System.out.println("盐值: " + savedUser.getSalt());
            System.out.println("加密密码: " + savedUser.getPassword());
            System.out.println("密码验证: " + (passwordMatches ? "成功" : "失败"));

        } catch (Exception e) {
            fail("注册过程中发生异常: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试用户登录验证")
    void testUserLoginValidation() {
        // 先注册一个用户
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("logintest001");
        registerRequest.setPassword("LoginTest123!");
        registerRequest.setRealName("登录测试用户");
        registerRequest.setPhone("13800138002");
        registerRequest.setEmail("logintest001@example.com");
        registerRequest.setUserType("normal");

        var registerResponse = userRegisterService.register(registerRequest);
        assertNotNull(registerResponse);

        // 测试正确密码登录
        SysUser validUser = sysUserService.validateUser("logintest001", "LoginTest123!");
        assertNotNull(validUser, "正确密码应该登录成功");
        assertEquals("logintest001", validUser.getUsername());

        // 测试错误密码登录
        SysUser invalidUser = sysUserService.validateUser("logintest001", "WrongPassword123!");
        assertNull(invalidUser, "错误密码应该登录失败");

        // 测试不存在的用户
        SysUser nonExistentUser = sysUserService.validateUser("nonexistent", "AnyPassword123!");
        assertNull(nonExistentUser, "不存在的用户应该登录失败");

        System.out.println("=== 登录验证测试结果 ===");
        System.out.println("正确密码登录: " + (validUser != null ? "成功" : "失败"));
        System.out.println("错误密码登录: " + (invalidUser == null ? "正确拒绝" : "错误通过"));
        System.out.println("不存在用户登录: " + (nonExistentUser == null ? "正确拒绝" : "错误通过"));
    }

    @Test
    @DisplayName("测试多种登录方式")
    void testMultipleLoginMethods() {
        // 注册用户
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("multilogin001");
        registerRequest.setPassword("MultiLogin123!");
        registerRequest.setRealName("多登录方式测试");
        registerRequest.setPhone("13800138003");
        registerRequest.setEmail("multilogin001@example.com");
        registerRequest.setUserType("normal");

        userRegisterService.register(registerRequest);

        // 测试用户名登录
        SysUser userByUsername = sysUserService.validateUser("multilogin001", "MultiLogin123!");
        assertNotNull(userByUsername, "用户名登录应该成功");

        // 测试手机号登录
        SysUser userByPhone = sysUserService.validateUser("13800138003", "MultiLogin123!");
        assertNotNull(userByPhone, "手机号登录应该成功");

        // 测试邮箱登录
        SysUser userByEmail = sysUserService.validateUser("multilogin001@example.com", "MultiLogin123!");
        assertNotNull(userByEmail, "邮箱登录应该成功");

        // 验证是同一个用户
        assertEquals(userByUsername.getId(), userByPhone.getId());
        assertEquals(userByUsername.getId(), userByEmail.getId());

        System.out.println("=== 多种登录方式测试结果 ===");
        System.out.println("用户名登录: " + (userByUsername != null ? "成功" : "失败"));
        System.out.println("手机号登录: " + (userByPhone != null ? "成功" : "失败"));
        System.out.println("邮箱登录: " + (userByEmail != null ? "成功" : "失败"));
    }

    @Test
    @DisplayName("测试密码修改功能")
    void testPasswordChange() {
        // 注册用户
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("pwdchange001");
        registerRequest.setPassword("OldPassword123!");
        registerRequest.setRealName("密码修改测试");
        registerRequest.setPhone("13800138004");
        registerRequest.setEmail("pwdchange001@example.com");
        registerRequest.setUserType("normal");

        var registerResponse = userRegisterService.register(registerRequest);
        Long userId = registerResponse.getUserId();

        // 验证原密码可以登录
        SysUser userBeforeChange = sysUserService.validateUser("pwdchange001", "OldPassword123!");
        assertNotNull(userBeforeChange, "原密码应该可以登录");

        // 修改密码
        boolean changeResult = sysUserService.changePassword(userId, "OldPassword123!", "NewPassword123!");
        assertTrue(changeResult, "密码修改应该成功");

        // 验证新密码可以登录
        SysUser userAfterChange = sysUserService.validateUser("pwdchange001", "NewPassword123!");
        assertNotNull(userAfterChange, "新密码应该可以登录");

        // 验证旧密码不能登录
        SysUser userWithOldPassword = sysUserService.validateUser("pwdchange001", "OldPassword123!");
        assertNull(userWithOldPassword, "旧密码应该不能登录");

        // 验证用户的盐值已更新
        SysUser updatedUser = sysUserMapper.selectById(userId);
        assertNotEquals(userBeforeChange.getSalt(), updatedUser.getSalt(), "盐值应该已更新");

        System.out.println("=== 密码修改测试结果 ===");
        System.out.println("原密码登录: " + (userBeforeChange != null ? "成功" : "失败"));
        System.out.println("密码修改: " + (changeResult ? "成功" : "失败"));
        System.out.println("新密码登录: " + (userAfterChange != null ? "成功" : "失败"));
        System.out.println("旧密码登录: " + (userWithOldPassword == null ? "正确拒绝" : "错误通过"));
        System.out.println("原盐值: " + userBeforeChange.getSalt());
        System.out.println("新盐值: " + updatedUser.getSalt());
    }

    @Test
    @DisplayName("测试重复注册检查")
    void testDuplicateRegistrationCheck() {
        // 第一次注册
        RegisterRequest registerRequest1 = new RegisterRequest();
        registerRequest1.setUsername("duplicate001");
        registerRequest1.setPassword("Password123!");
        registerRequest1.setRealName("重复注册测试1");
        registerRequest1.setPhone("13800138005");
        registerRequest1.setEmail("duplicate001@example.com");
        registerRequest1.setUserType("normal");

        var response1 = userRegisterService.register(registerRequest1);
        assertNotNull(response1, "第一次注册应该成功");

        // 尝试用相同用户名注册
        RegisterRequest registerRequest2 = new RegisterRequest();
        registerRequest2.setUsername("duplicate001"); // 相同用户名
        registerRequest2.setPassword("Password456!");
        registerRequest2.setRealName("重复注册测试2");
        registerRequest2.setPhone("13800138006");
        registerRequest2.setEmail("duplicate002@example.com");
        registerRequest2.setUserType("normal");

        assertThrows(Exception.class, () -> {
            userRegisterService.register(registerRequest2);
        }, "重复用户名注册应该抛出异常");

        System.out.println("=== 重复注册检查测试结果 ===");
        System.out.println("第一次注册: 成功");
        System.out.println("重复用户名注册: 正确拒绝");
    }

    @Test
    @DisplayName("测试安全性：盐值唯一性")
    void testSaltUniqueness() {
        // 注册多个用户，验证盐值的唯一性
        String[] usernames = {"salt001", "salt002", "salt003", "salt004", "salt005"};
        String[] salts = new String[usernames.length];

        for (int i = 0; i < usernames.length; i++) {
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername(usernames[i]);
            registerRequest.setPassword("SamePassword123!");  // 使用相同密码
            registerRequest.setRealName("盐值测试用户" + (i + 1));
            registerRequest.setPhone("1380013800" + (i + 1));
            registerRequest.setEmail(usernames[i] + "@example.com");
            registerRequest.setUserType("normal");

            var response = userRegisterService.register(registerRequest);
            SysUser user = sysUserMapper.selectById(response.getUserId());
            salts[i] = user.getSalt();

            System.out.println("用户 " + usernames[i] + " 的盐值: " + salts[i]);
        }

        // 验证所有盐值都不相同
        for (int i = 0; i < salts.length; i++) {
            for (int j = i + 1; j < salts.length; j++) {
                assertNotEquals(salts[i], salts[j], 
                    "用户 " + usernames[i] + " 和 " + usernames[j] + " 的盐值不应相同");
            }
        }

        System.out.println("=== 盐值唯一性测试结果 ===");
        System.out.println("所有用户的盐值都是唯一的: 通过");
    }
}
