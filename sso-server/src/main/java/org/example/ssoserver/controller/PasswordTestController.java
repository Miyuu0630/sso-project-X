package org.example.ssoserver.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ssoserver.service.PasswordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 密码测试控制器
 * 用于调试密码验证问题
 */
@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@SaIgnore
public class PasswordTestController {

    private final PasswordService passwordService;

    /**
     * 测试密码匹配
     */
    @GetMapping("/password")
    public Map<String, Object> testPassword(
            @RequestParam String rawPassword,
            @RequestParam String encodedPassword) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试密码匹配
            boolean matches = passwordService.matches(rawPassword, encodedPassword);
            
            result.put("success", true);
            result.put("rawPassword", rawPassword);
            result.put("encodedPassword", encodedPassword);
            result.put("matches", matches);
            result.put("encodedLength", encodedPassword.length());
            result.put("isBCryptFormat", encodedPassword.startsWith("$2a$"));
            
            log.info("密码测试: raw={}, encoded={}, matches={}", rawPassword, encodedPassword, matches);
            
        } catch (Exception e) {
            log.error("密码测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 测试admin密码
     */
    @GetMapping("/admin-password")
    public Map<String, Object> testAdminPassword() {
        String rawPassword = "admin123456";
        String encodedPassword = "$2a$10$N.zmdr9k7uOCQb97.AnUPuTEDTP.4VQHpYm2XIjuewOzXALqJlqm2";

        return testPassword(rawPassword, encodedPassword);
    }

    /**
     * 生成新的BCrypt密码
     */
    @GetMapping("/generate-password")
    public Map<String, Object> generatePassword(@RequestParam String password) {
        Map<String, Object> result = new HashMap<>();

        try {
            String encoded = passwordService.encodePassword(password);
            boolean matches = passwordService.matches(password, encoded);

            result.put("success", true);
            result.put("rawPassword", password);
            result.put("encodedPassword", encoded);
            result.put("matches", matches);
            result.put("sqlUpdate", "UPDATE sys_user SET password = '" + encoded + "' WHERE username = 'admin';");

            log.info("生成密码: raw={}, encoded={}, matches={}", password, encoded, matches);

        } catch (Exception e) {
            log.error("生成密码失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
