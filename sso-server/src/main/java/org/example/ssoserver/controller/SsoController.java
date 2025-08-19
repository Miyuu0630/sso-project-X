package org.example.ssoserver.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ssoserver.common.Result;
import org.example.ssoserver.entity.SysUser;
import org.example.ssoserver.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * SSO认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/sso")
@RequiredArgsConstructor
public class SsoController {
    
    private final SysUserService userService;
    
    /**
     * SSO统一认证入口
     * 当用户在 Client 端点击登录时，会跳转到这个接口
     */
    @RequestMapping("/auth")
    public Object ssoAuth(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam(required = false) String redirect) {
        log.info("SSO认证请求: {}, redirect: {}", request.getRequestURL(), redirect);

        try {
            // 检查是否已经登录
            if (StpUtil.isLogin()) {
                // 已登录，生成ticket并重定向
                String ticket = StpUtil.getTokenValue();
                String redirectUrl = redirect != null ? redirect : "http://localhost:8082/";

                if (redirectUrl.contains("?")) {
                    redirectUrl += "&ticket=" + ticket;
                } else {
                    redirectUrl += "?ticket=" + ticket;
                }

                log.info("用户已登录，重定向到: {}", redirectUrl);
                response.setStatus(302);
                response.setHeader("Location", redirectUrl);
                return null;
            } else {
                // 未登录，重定向到登录页面
                String loginUrl = "http://localhost:8081/login.html";
                if (redirect != null) {
                    loginUrl += "?redirect=" + redirect;
                }

                log.info("用户未登录，重定向到登录页面: {}", loginUrl);
                response.setStatus(302);
                response.setHeader("Location", loginUrl);
                return null;
            }
        } catch (Exception e) {
            log.error("SSO认证处理失败", e);
            return Result.error("认证失败");
        }
    }
    
    /**
     * 配置SSO相关参数
     */
    @RequestMapping("/config")
    public Result<Map<String, Object>> ssoConfig() {
        Map<String, Object> config = Map.of(
            "serverUrl", "http://localhost:8081",
            "loginUrl", "http://localhost:8081/sso/auth",
            "logoutUrl", "http://localhost:8081/sso/logout"
        );
        return Result.success(config);
    }
    
    /**
     * SSO登出
     */
    @RequestMapping("/logout")
    public Object ssoLogout(@RequestParam(required = false) String redirect) {
        log.info("SSO登出请求, redirect: {}", redirect);

        try {
            // 执行登出
            if (StpUtil.isLogin()) {
                StpUtil.logout();
                log.info("用户登出成功");
            }

            // 重定向到指定页面或默认页面
            String redirectUrl = redirect != null ? redirect : "http://localhost:8082/";
            return Result.success("登出成功", Map.of("redirectUrl", redirectUrl));
        } catch (Exception e) {
            log.error("SSO登出失败", e);
            return Result.error("登出失败");
        }
    }
    
    /**
     * 获取当前登录用户信息（供Client端调用）
     */
    @GetMapping("/userinfo")
    public Result<SysUser> getSsoUserInfo(@RequestParam String ticket) {
        try {
            // 验证ticket并获取用户ID
            Object loginId = StpUtil.getLoginIdByToken(ticket);
            if (loginId == null) {
                return Result.error(Result.ResultCode.LOGIN_EXPIRED);
            }
            
            Long userId = Long.valueOf(loginId.toString());
            SysUser user = userService.getUserById(userId);
            
            if (user != null) {
                return Result.success(user);
            } else {
                return Result.error(Result.ResultCode.USER_NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("获取SSO用户信息失败", e);
            return Result.error("获取用户信息失败");
        }
    }
    
    /**
     * 验证ticket有效性（供Client端调用）
     */
    @GetMapping("/check-ticket")
    public Result<Map<String, Object>> checkTicket(@RequestParam String ticket) {
        try {
            // 验证ticket
            Object loginId = StpUtil.getLoginIdByToken(ticket);
            boolean isValid = loginId != null;
            
            Map<String, Object> result = Map.of(
                "valid", isValid,
                "userId", isValid ? loginId : "",
                "timestamp", System.currentTimeMillis()
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("验证ticket失败", e);
            return Result.error("ticket验证失败");
        }
    }
    
    /**
     * 获取用户权限信息（供Client端调用）
     */
    @GetMapping("/permissions")
    public Result<Map<String, Object>> getUserPermissions(@RequestParam String ticket) {
        try {
            // 验证ticket并获取用户ID
            Object loginId = StpUtil.getLoginIdByToken(ticket);
            if (loginId == null) {
                return Result.error(Result.ResultCode.LOGIN_EXPIRED);
            }
            
            Long userId = Long.valueOf(loginId.toString());
            
            // 获取用户权限和角色
            Map<String, Object> permissions = Map.of(
                "userId", userId,
                "roles", StpUtil.getRoleList(loginId),
                "permissions", StpUtil.getPermissionList(loginId)
            );
            
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("获取用户权限失败", e);
            return Result.error("获取权限失败");
        }
    }
    
    /**
     * 刷新ticket（供Client端调用）
     */
    @PostMapping("/refresh-ticket")
    public Result<Map<String, Object>> refreshTicket(@RequestParam String ticket) {
        try {
            // 验证当前ticket
            Object loginId = StpUtil.getLoginIdByToken(ticket);
            if (loginId == null) {
                return Result.error(Result.ResultCode.LOGIN_EXPIRED);
            }
            
            // 续签token
            StpUtil.renewTimeout(Long.parseLong(loginId.toString()));
            
            Map<String, Object> result = Map.of(
                "ticket", ticket,
                "expiresIn", StpUtil.getTokenTimeout(ticket),
                "timestamp", System.currentTimeMillis()
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("刷新ticket失败", e);
            return Result.error("刷新失败");
        }
    }
    
    /**
     * 单点登出（供Client端调用）
     */
    @PostMapping("/single-logout")
    public Result<Void> singleLogout(@RequestParam String ticket) {
        try {
            // 根据ticket登出
            StpUtil.logoutByTokenValue(ticket);
            
            log.info("单点登出成功: ticket={}", ticket);
            return Result.success();
        } catch (Exception e) {
            log.error("单点登出失败", e);
            return Result.error("登出失败");
        }
    }
}
