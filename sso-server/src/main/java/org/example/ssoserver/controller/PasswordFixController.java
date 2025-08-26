package org.example.ssoserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.model.ApiResponse;
import org.example.ssoserver.util.PasswordHashFixUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 密码修复控制器
 * 仅用于开发环境修复密码哈希问题
 */
@RestController
@RequestMapping("/api/fix")
@RequiredArgsConstructor
@Slf4j
public class PasswordFixController {

    private final PasswordHashFixUtil passwordHashFixUtil;

    /**
     * 修复密码哈希
     */
    @PostMapping("/passwords")
    public ApiResponse<String> fixPasswords() {
        try {
            log.info("手动触发密码哈希修复...");
            passwordHashFixUtil.fixPasswordHashes();
            return ApiResponse.success("密码哈希修复完成");
        } catch (Exception e) {
            log.error("密码哈希修复失败", e);
            return ApiResponse.error("密码哈希修复失败: " + e.getMessage());
        }
    }

    /**
     * 验证密码哈希
     */
    @PostMapping("/verify-passwords")
    public ApiResponse<String> verifyPasswords() {
        try {
            log.info("手动触发密码哈希验证...");
            passwordHashFixUtil.verifyPasswordHashes();
            return ApiResponse.success("密码哈希验证完成，请查看日志");
        } catch (Exception e) {
            log.error("密码哈希验证失败", e);
            return ApiResponse.error("密码哈希验证失败: " + e.getMessage());
        }
    }
}
