package org.example.ssoserver.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.LoginRequest;
import org.example.common.dto.LoginResponse;
import org.example.common.model.ApiResponse;
import org.example.common.model.UserDTO;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResultCode;
import org.example.common.util.DeviceUtil;
import org.example.ssoserver.service.AuthService;
import org.example.ssoserver.service.SysUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 提供用户认证相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;
    private final SysUserService userService;
    
    // ========================================
    // 用户登录相关接口
    // ========================================

    /**
     * 统一登录接口
     * 支持密码登录、短信登录、邮箱登录、第三方登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持多种登录方式：密码、短信验证码、邮箱验证码、第三方OAuth")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                          HttpServletRequest httpRequest,
                                          HttpServletResponse httpResponse) {
        try {
            // 设置客户端信息
            enrichLoginRequest(request, httpRequest);

            // 执行登录
            LoginResponse response = authService.ssoLogin(request);

            // 设置Cookie（如果需要）
            if (response.getAccessToken() != null) {
                setCookieToken(httpResponse, response.getAccessToken());
            }

            log.info("用户登录成功: account={}, loginType={}, deviceType={}",
                    request.getAccount(), request.getLoginType(), request.getDeviceInfo());

            return ApiResponse.success("登录成功", response);

        } catch (BusinessException e) {
            log.warn("用户登录失败: account={}, reason={}", request.getAccount(), e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("用户登录异常: account={}", request.getAccount(), e);
            return ApiResponse.error("登录失败，请稍后重试");
        }
    }

    /**
     * 密码登录
     */
    @PostMapping("/login/password")
    @Operation(summary = "密码登录", description = "使用用户名/手机号/邮箱 + 密码登录")
    public ApiResponse<LoginResponse> passwordLogin(@Valid @RequestBody LoginRequest request,
                                                   HttpServletRequest httpRequest) {
        request.setLoginType("password");
        enrichLoginRequest(request, httpRequest);

        LoginResponse response = authService.passwordLogin(
            request.getAccount(),
            request.getPassword(),
            request
        );

        return ApiResponse.success("登录成功", response);
    }

    /**
     * 短信验证码登录
     */
    @PostMapping("/login/sms")
    @Operation(summary = "短信登录", description = "使用手机号 + 短信验证码登录")
    public ApiResponse<LoginResponse> smsLogin(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        request.setLoginType("sms");
        enrichLoginRequest(request, httpRequest);

        LoginResponse response = authService.smsLogin(
            request.getAccount(),
            request.getVerificationCode(),
            request
        );

        return ApiResponse.success("登录成功", response);
    }

    /**
     * 邮箱验证码登录
     */
    @PostMapping("/login/email")
    @Operation(summary = "邮箱登录", description = "使用邮箱 + 邮箱验证码登录")
    public ApiResponse<LoginResponse> emailLogin(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest) {
        request.setLoginType("email");
        enrichLoginRequest(request, httpRequest);

        LoginResponse response = authService.emailLogin(
            request.getAccount(),
            request.getVerificationCode(),
            request
        );

        return ApiResponse.success("登录成功", response);
    }

    // ========================================
    // 用户登出相关接口
    // ========================================

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出当前登录状态")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId();
            if (userId != null) {
                boolean success = authService.ssoLogout(userId);
                if (success) {
                    log.info("用户登出成功: userId={}", userId);
                    return ApiResponse.success("登出成功");
                }
            }
            return ApiResponse.error("登出失败");
        } catch (Exception e) {
            log.error("用户登出异常", e);
            return ApiResponse.error("登出失败");
        }
    }

    /**
     * 强制登出（管理员功能）
     */
    @PostMapping("/logout/{userId}")
    @Operation(summary = "强制登出", description = "管理员强制用户登出")
    public ApiResponse<Void> forceLogout(@PathVariable Long userId) {
        try {
            boolean success = authService.ssoLogout(userId);
            if (success) {
                log.info("强制登出成功: userId={}", userId);
                return ApiResponse.success("强制登出成功");
            }
            return ApiResponse.error("强制登出失败");
        } catch (Exception e) {
            log.error("强制登出异常: userId={}", userId, e);
            return ApiResponse.error("强制登出失败");
        }
    }

    // ========================================
    // Token管理相关接口
    // ========================================

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    public ApiResponse<Map<String, Object>> refreshToken(@RequestParam @NotBlank String refreshToken) {
        try {
            String newAccessToken = authService.refreshAccessToken(refreshToken);
            if (newAccessToken != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("accessToken", newAccessToken);
                result.put("tokenType", "Bearer");
                result.put("expiresIn", 7200); // 2小时

                return ApiResponse.success("Token刷新成功", result);
            }
            return ApiResponse.<Map<String, Object>>error(ResultCode.TOKEN_INVALID.getCode(), "刷新令牌无效");
        } catch (Exception e) {
            log.error("Token刷新异常", e);
            return ApiResponse.error("Token刷新失败");
        }
    }

    /**
     * 验证Token
     */
    @GetMapping("/validate")
    @Operation(summary = "验证Token", description = "验证访问令牌的有效性")
    public ApiResponse<UserDTO> validateToken(@RequestParam @NotBlank String token) {
        try {
            Long userId = authService.validateAccessToken(token);
            if (userId != null) {
                UserDTO user = userService.convertToDTO(userService.getUserById(userId));
                return ApiResponse.success("Token有效", user);
            }
            return ApiResponse.<UserDTO>error(ResultCode.TOKEN_INVALID.getCode(), "Token无效");
        } catch (Exception e) {
            log.error("Token验证异常", e);
            return ApiResponse.error("Token验证失败");
        }
    }

    // ========================================
    // 用户信息相关接口
    // ========================================

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public ApiResponse<UserDTO> getUserInfo() {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return ApiResponse.unauthorized("未登录");
            }

            UserDTO user = userService.convertToDTO(userService.getUserById(userId));
            if (user != null) {
                // 获取用户权限信息
                user.setRoles(authService.getUserRoles(userId));
                user.setPermissions(authService.getUserPermissions(userId));

                return ApiResponse.success(user);
            }
            return ApiResponse.error("用户不存在");
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return ApiResponse.error("获取用户信息失败");
        }
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/check")
    @Operation(summary = "检查登录状态", description = "检查当前用户是否已登录")
    public ApiResponse<Map<String, Object>> checkLoginStatus() {
        try {
            Long userId = getCurrentUserId();
            boolean isLoggedIn = userId != null && authService.isUserLoggedIn(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("isLoggedIn", isLoggedIn);
            result.put("userId", userId);

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("检查登录状态异常", e);
            return ApiResponse.error("检查登录状态失败");
        }
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    /**
     * 丰富登录请求信息
     */
    private void enrichLoginRequest(LoginRequest request, HttpServletRequest httpRequest) {
        // 设置客户端IP
        request.setClientIp(getClientIp(httpRequest));

        // 设置User-Agent
        request.setUserAgent(httpRequest.getHeader("User-Agent"));

        // 设置设备信息
        if (StrUtil.isBlank(request.getDeviceInfo())) {
            request.setDeviceInfo(DeviceUtil.getDeviceInfo(request.getUserAgent()));
        }

        // 生成设备指纹
        if (StrUtil.isBlank(request.getDeviceFingerprint())) {
            request.setDeviceFingerprint(DeviceUtil.generateSimpleDeviceFingerprint(
                request.getUserAgent(), request.getClientIp()));
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 设置Cookie中的Token
     */
    private void setCookieToken(HttpServletResponse response, String token) {
        // 这里可以根据需要设置Cookie
        // response.addCookie(new Cookie("satoken", token));
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {
            log.debug("获取当前用户ID失败", e);
        }
        return null;
    }
}
