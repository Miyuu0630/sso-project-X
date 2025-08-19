package org.example.ssoserver.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ssoserver.common.Result;
import org.example.ssoserver.dto.LoginRequest;
import org.example.ssoserver.dto.RegisterRequest;
import org.example.ssoserver.entity.SysUser;
import org.example.ssoserver.service.SysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    
    private final SysUserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<SysUser> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求: {}", request.getUsername());
        return userService.register(request);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request, 
                                            HttpServletRequest httpRequest) {
        // 设置客户端信息
        request.setClientIp(getClientIp(httpRequest));
        request.setUserAgent(httpRequest.getHeader("User-Agent"));
        
        log.info("用户登录请求: {}", request.getAccount());
        return userService.login(request);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        String token = StpUtil.getTokenValue();
        log.info("用户登出请求");
        return userService.logout(token);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    public Result<SysUser> getUserInfo() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            SysUser user = userService.getUserById(userId);
            if (user != null) {
                return Result.success(user);
            } else {
                return Result.error(Result.ResultCode.USER_NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("获取用户信息失败");
        }
    }
    
    /**
     * 检查登录状态
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> checkLogin() {
        try {
            boolean isLogin = StpUtil.isLogin();
            Map<String, Object> result = Map.of(
                "isLogin", isLogin,
                "token", isLogin ? StpUtil.getTokenValue() : "",
                "userId", isLogin ? StpUtil.getLoginId() : ""
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.success(Map.of("isLogin", false));
        }
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            
            if (StrUtil.isBlank(oldPassword) || StrUtil.isBlank(newPassword)) {
                return Result.error(Result.ResultCode.PARAM_ERROR.getCode(), "原密码和新密码不能为空");
            }
            
            return userService.changePassword(userId, oldPassword, newPassword);
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("修改密码失败");
        }
    }
    
    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.isUsernameExists(username);
        return Result.success(exists);
    }
    
    /**
     * 检查手机号是否存在
     */
    @GetMapping("/check-phone")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        boolean exists = userService.isPhoneExists(phone);
        return Result.success(exists);
    }
    
    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email);
        return Result.success(exists);
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个IP
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
