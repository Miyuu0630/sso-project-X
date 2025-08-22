package org.example.ssoserver.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.model.ApiResponse;
import org.example.common.model.UserDTO;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResultCode;
import org.example.common.dto.LoginRequest;
import org.example.common.dto.LoginResponse;
import org.example.common.enums.LoginType;
import org.example.ssoserver.service.AuthService;
import org.example.ssoserver.service.SysUserService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * SSO单点登录控制器
 * 提供SSO核心功能：票据生成、验证、单点登出等
 */
@Slf4j
@RestController
@RequestMapping("/sso")
@RequiredArgsConstructor
@Tag(name = "SSO单点登录", description = "SSO单点登录核心接口")
public class SsoController {

    private final AuthService authService;
    private final SysUserService userService;
    
    // ========================================
    // SSO认证核心接口
    // ========================================

    /**
     * SSO统一认证入口
     * 当用户在客户端点击登录时，会跳转到这个接口
     */
    @RequestMapping("/auth")
    @Operation(summary = "SSO认证入口", description = "SSO统一认证入口，检查登录状态并生成票据")
    public void ssoAuth(HttpServletRequest request,
                       HttpServletResponse response,
                       @RequestParam(required = false) String redirect,
                       @RequestParam(required = false) String clientId) throws IOException {

        log.info("SSO认证请求: redirect={}, clientId={}", redirect, clientId);

        try {
            // 检查是否已经登录
            if (StpUtil.isLogin()) {
                // 已登录，生成SSO票据并重定向
                Long userId = StpUtil.getLoginIdAsLong();
                String ticket = authService.generateSsoTicket(userId, clientId, redirect);

                String redirectUrl = buildRedirectUrl(redirect, ticket);

                log.info("用户已登录，生成票据并重定向: userId={}, ticket={}, redirectUrl={}",
                        userId, ticket, redirectUrl);

                response.sendRedirect(redirectUrl);
            } else {
                // 未登录，显示登录页面
                log.info("用户未登录，显示登录页面: redirect={}, clientId={}", redirect, clientId);

                try {
                    response.setContentType("text/html;charset=UTF-8");
                    String loginHtml = getLoginPageHtml(redirect, clientId);
                    response.getWriter().write(loginHtml);
                    response.getWriter().flush();
                } catch (Exception ex) {
                    log.error("显示登录页面失败", ex);
                    throw ex;
                }
            }
        } catch (Exception e) {
            log.error("SSO认证异常: redirect={}, clientId={}", redirect, clientId, e);
            // 发生异常时显示错误页面
            try {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(getErrorPageHtml("SSO认证失败，请重试", redirect));
            } catch (Exception ex) {
                log.error("显示错误页面失败", ex);
            }
        }
    }

    /**
     * 处理favicon.ico请求
     */
    @GetMapping("/sso/favicon.ico")
    public void favicon(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * SSO登录处理接口
     */
    @PostMapping("/doLogin")
    @Operation(summary = "SSO登录处理", description = "处理用户登录请求")
    public void doLogin(@RequestParam String username,
                       @RequestParam String password,
                       @RequestParam(required = false) String redirect,
                       @RequestParam(required = false) String clientId,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        try {
            log.info("SSO登录请求: username={}, redirect={}, clientId={}", username, redirect, clientId);

            // 构建登录请求
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setAccount(username);
            loginRequest.setPassword(password);
            loginRequest.setLoginType(LoginType.PASSWORD.getCode());

            // 设置设备和请求信息
            loginRequest.setUserAgent(request.getHeader("User-Agent"));
            loginRequest.setClientIp(getClientIp(request));
            loginRequest.setRedirectUri(redirect);
            loginRequest.setClientId(clientId);

            // 执行登录
            LoginResponse loginResponse = authService.ssoLogin(loginRequest);

            if (loginResponse != null && loginResponse.getAccessToken() != null) {
                // 登录成功，生成SSO票据
                Long userId = StpUtil.getLoginIdAsLong();
                String ticket = authService.generateSsoTicket(userId, clientId, redirect);

                log.info("SSO登录成功: username={}, userId={}, ticket={}", username, userId, ticket);

                // 构建重定向URL
                String redirectUrl = buildRedirectUrl(redirect, ticket);

                // 返回JSON响应给前端
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":200,\"message\":\"登录成功\",\"data\":{\"redirectUrl\":\"" + redirectUrl + "\"}}");
            } else {
                // 登录失败
                log.warn("SSO登录失败: username={}", username);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":400,\"message\":\"用户名或密码错误\"}");
            }
        } catch (Exception e) {
            log.error("SSO登录异常: username={}", username, e);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"登录失败：" + e.getMessage() + "\"}");
        }
    }

    /**
     * 验证SSO票据
     * 客户端使用票据换取用户信息
     */
    @PostMapping("/validate")
    @Operation(summary = "验证SSO票据", description = "客户端使用票据换取用户信息")
    public ApiResponse<UserDTO> validateTicket(@RequestParam @NotBlank String ticket,
                                              @RequestParam(required = false) String clientId) {
        try {
            UserDTO user = authService.validateSsoTicket(ticket, clientId);
            if (user != null) {
                log.info("SSO票据验证成功: ticket={}, userId={}, clientId={}",
                        ticket, user.getId(), clientId);
                return ApiResponse.success("票据验证成功", user);
            } else {
                log.warn("SSO票据验证失败: ticket={}, clientId={}", ticket, clientId);
                return ApiResponse.<UserDTO>error(ResultCode.SSO_TICKET_INVALID.getCode(), "票据无效或已过期");
            }
        } catch (Exception e) {
            log.error("SSO票据验证异常: ticket={}, clientId={}", ticket, clientId, e);
            return ApiResponse.<UserDTO>error(ResultCode.SSO_TICKET_INVALID.getCode(), "票据验证失败");
        }
    }

    /**
     * 兼容接口：检查票据有效性（GET方式）
     * 为了兼容旧版客户端
     */
    @GetMapping("/check-ticket")
    @Operation(summary = "检查票据有效性", description = "兼容接口，检查票据是否有效")
    public ApiResponse<Map<String, Object>> checkTicket(@RequestParam @NotBlank String ticket,
                                                       @RequestParam(required = false) String clientId) {
        try {
            UserDTO user = authService.validateSsoTicket(ticket, clientId);
            if (user != null) {
                log.info("SSO票据检查成功: ticket={}, userId={}, clientId={}",
                        ticket, user.getId(), clientId);

                Map<String, Object> result = Map.of(
                    "valid", true,
                    "userId", user.getId().toString(),
                    "username", user.getUsername()
                );
                return ApiResponse.success("票据有效", result);
            } else {
                log.warn("SSO票据检查失败: ticket={}, clientId={}", ticket, clientId);
                Map<String, Object> result = Map.of("valid", false);
                return ApiResponse.success("票据无效", result);
            }
        } catch (BusinessException e) {
            log.warn("SSO票据验证失败: ticket={}, reason={}", ticket, e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("SSO票据验证异常: ticket={}", ticket, e);
            return ApiResponse.error("票据验证失败");
        }
    }

    /**
     * SSO单点登出
     * 登出所有已登录的客户端
     */
    @PostMapping("/logout")
    @Operation(summary = "SSO单点登出", description = "单点登出，清除所有客户端的登录状态")
    public ApiResponse<Map<String, Object>> ssoLogout(@RequestParam(required = false) String redirect) {
        try {
            if (StpUtil.isLogin()) {
                Long userId = StpUtil.getLoginIdAsLong();
                boolean success = authService.ssoLogout(userId);

                if (success) {
                    log.info("SSO单点登出成功: userId={}", userId);

                    Map<String, Object> result = new HashMap<>();
                    if (StrUtil.isNotBlank(redirect)) {
                        result.put("redirectUrl", redirect);
                    }

                    return ApiResponse.<Map<String, Object>>success("登出成功", result);
                } else {
                    return ApiResponse.error("登出失败");
                }
            } else {
                return ApiResponse.success("用户未登录");
            }
        } catch (Exception e) {
            log.error("SSO单点登出异常", e);
            return ApiResponse.error("登出失败");
        }
    }

    // ========================================
    // SSO管理接口
    // ========================================

    /**
     * 获取SSO登录状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取SSO状态", description = "获取当前用户的SSO登录状态")
    public ApiResponse<Map<String, Object>> getSsoStatus() {
        try {
            Map<String, Object> status = new HashMap<>();

            if (StpUtil.isLogin()) {
                Long userId = StpUtil.getLoginIdAsLong();
                UserDTO user = userService.convertToDTO(userService.getUserById(userId));

                status.put("isLoggedIn", true);
                status.put("userId", userId);
                status.put("username", user.getUsername());
                status.put("displayName", user.getDisplayName());
                status.put("loginTime", System.currentTimeMillis());
                status.put("tokenTimeout", StpUtil.getTokenTimeout());
            } else {
                status.put("isLoggedIn", false);
            }

            return ApiResponse.success(status);
        } catch (Exception e) {
            log.error("获取SSO状态异常", e);
            return ApiResponse.error("获取SSO状态失败");
        }
    }

    /**
     * 销毁指定票据
     */
    @PostMapping("/destroy-ticket")
    @Operation(summary = "销毁票据", description = "销毁指定的SSO票据")
    public ApiResponse<Void> destroyTicket(@RequestParam @NotBlank String ticket) {
        try {
            boolean success = authService.destroySsoTicket(ticket);
            if (success) {
                log.info("SSO票据销毁成功: ticket={}", ticket);
                return ApiResponse.success("票据销毁成功");
            } else {
                return ApiResponse.error("票据销毁失败");
            }
        } catch (Exception e) {
            log.error("SSO票据销毁异常: ticket={}", ticket, e);
            return ApiResponse.error("票据销毁失败");
        }
    }

    /**
     * 检查用户登录状态
     */
    @GetMapping("/check-login")
    @Operation(summary = "检查登录状态", description = "检查指定用户的登录状态")
    public ApiResponse<Map<String, Object>> checkUserLogin(@RequestParam Long userId) {
        try {
            boolean isLoggedIn = authService.isUserLoggedIn(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("isLoggedIn", isLoggedIn);

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("检查用户登录状态异常: userId={}", userId, e);
            return ApiResponse.error("检查登录状态失败");
        }
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    /**
     * 构建重定向URL
     */
    private String buildRedirectUrl(String redirect, String ticket) {
        if (StrUtil.isBlank(redirect)) {
            redirect = "http://localhost:8082/"; // 默认客户端地址
        }

        if (redirect.contains("?")) {
            return redirect + "&ticket=" + ticket;
        } else {
            return redirect + "?ticket=" + ticket;
        }
    }

    /**
     * 生成登录页面HTML
     */
    private String getLoginPageHtml(String redirect, String clientId) {
        String redirectValue = redirect != null ? redirect : "";
        String clientIdValue = clientId != null ? clientId : "";

        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<title>SSO统一登录</title>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 50px; }" +
            ".login-container { max-width: 400px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
            ".login-title { text-align: center; color: #333; margin-bottom: 30px; }" +
            ".form-group { margin-bottom: 20px; }" +
            ".form-group label { display: block; margin-bottom: 5px; color: #555; }" +
            ".form-group input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }" +
            ".login-btn { width: 100%; padding: 12px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }" +
            ".login-btn:hover { background: #0056b3; }" +
            ".error-msg { color: #dc3545; margin-top: 10px; display: none; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class=\"login-container\">" +
            "<h2 class=\"login-title\">SSO统一登录</h2>" +
            "<form id=\"loginForm\" action=\"/sso/doLogin\" method=\"post\">" +
            "<input type=\"hidden\" name=\"redirect\" value=\"" + redirectValue + "\">" +
            "<input type=\"hidden\" name=\"clientId\" value=\"" + clientIdValue + "\">" +
            "<div class=\"form-group\">" +
            "<label for=\"username\">用户名/手机号/邮箱:</label>" +
            "<input type=\"text\" id=\"username\" name=\"username\" required>" +
            "</div>" +
            "<div class=\"form-group\">" +
            "<label for=\"password\">密码:</label>" +
            "<input type=\"password\" id=\"password\" name=\"password\" required>" +
            "</div>" +
            "<button type=\"submit\" class=\"login-btn\">登录</button>" +
            "<div class=\"error-msg\" id=\"errorMsg\"></div>" +
            "</form>" +
            "</div>" +
            "<script>" +
            "document.getElementById('loginForm').addEventListener('submit', function(e) {" +
            "e.preventDefault();" +
            "const formData = new FormData(this);" +
            "fetch('/sso/doLogin', {" +
            "method: 'POST'," +
            "body: formData" +
            "})" +
            ".then(response => response.json())" +
            ".then(data => {" +
            "if (data.code === 200) {" +
            "if (data.data && data.data.redirectUrl) {" +
            "window.location.href = data.data.redirectUrl;" +
            "} else {" +
            "window.location.href = '/sso/auth?redirect=" + redirectValue + "&clientId=" + clientIdValue + "';" +
            "}" +
            "} else {" +
            "document.getElementById('errorMsg').textContent = data.message || '登录失败';" +
            "document.getElementById('errorMsg').style.display = 'block';" +
            "}" +
            "})" +
            ".catch(error => {" +
            "document.getElementById('errorMsg').textContent = '网络错误，请重试';" +
            "document.getElementById('errorMsg').style.display = 'block';" +
            "});" +
            "});" +
            "</script>" +
            "</body>" +
            "</html>";
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况，取第一个
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 生成错误页面HTML
     */
    private String getErrorPageHtml(String errorMessage, String redirect) {
        String message = errorMessage != null ? errorMessage : "未知错误";
        String redirectValue = redirect != null ? redirect : "";

        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<title>SSO错误</title>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 50px; }" +
            ".error-container { max-width: 400px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }" +
            ".error-title { color: #dc3545; margin-bottom: 20px; }" +
            ".error-message { color: #666; margin-bottom: 30px; }" +
            ".retry-btn { padding: 10px 20px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; display: inline-block; }" +
            ".retry-btn:hover { background: #0056b3; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class=\"error-container\">" +
            "<h2 class=\"error-title\">登录失败</h2>" +
            "<p class=\"error-message\">" + message + "</p>" +
            "<a href=\"/sso/auth?redirect=" + redirectValue + "\" class=\"retry-btn\">重新登录</a>" +
            "</div>" +
            "</body>" +
            "</html>";
    }
}
