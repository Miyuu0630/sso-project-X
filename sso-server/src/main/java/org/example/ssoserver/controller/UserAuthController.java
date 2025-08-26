package org.example.ssoserver.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.LoginRequest;
import org.example.common.dto.LoginResponse;
import org.example.common.model.ApiResponse;
import org.example.common.result.ResultCode;
import org.example.ssoserver.dto.RegisterRequest;
import org.example.ssoserver.dto.RegisterResponse;
import org.example.ssoserver.entity.SysUser;
import org.example.ssoserver.security.CustomAuthenticationProvider;
import org.example.ssoserver.service.UserRegisterService;
import org.example.ssoserver.util.Md5SaltUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户认证控制器
 * 处理用户注册、登录等认证相关操作
 * 
 * @author SSO Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户认证", description = "用户注册、登录、登出等认证相关接口")
@Validated
public class UserAuthController {
    
    private final UserRegisterService userRegisterService;
    private final AuthenticationManager authenticationManager;
    private final CustomAuthenticationProvider customAuthenticationProvider;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口，支持用户名、手机号、邮箱注册")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletRequest httpRequest) {
        try {
            log.info("用户注册请求: username={}, phone={}, email={}, userType={}", 
                    request.getUsername(), request.getPhone(), request.getEmail(), request.getUserType());
            
            // 设置客户端信息
            enrichRegisterRequest(request, httpRequest);
            
            // 执行注册
            RegisterResponse response = userRegisterService.register(request);
            
            log.info("用户注册成功: userId={}, username={}", response.getUserId(), request.getUsername());
            
            return ApiResponse.success("注册成功", response);
            
        } catch (Exception e) {
            log.error("用户注册失败: username={}, error={}", request.getUsername(), e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户密码登录接口，支持用户名、手机号、邮箱登录")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                          HttpServletRequest httpRequest) {
        try {
            log.info("用户登录请求: account={}, loginType={}", request.getAccount(), request.getLoginType());
            
            // 设置客户端信息
            enrichLoginRequest(request, httpRequest);
            
            // 使用 Spring Security 进行认证
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    request.getAccount(), request.getPassword());
            
            Authentication result = authenticationManager.authenticate(authentication);
            
            if (result.isAuthenticated()) {
                // 认证成功，获取用户信息
                SysUser user = (SysUser) result.getPrincipal();

                // 使用 Sa-Token 生成会话
                StpUtil.login(user.getId());

                // 构建登录响应
                LoginResponse response = buildLoginResponse(user);

                log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

                return ApiResponse.success("登录成功", response);
            } else {
                return ApiResponse.error(ResultCode.LOGIN_ERROR.getCode(), "登录失败");
            }

        } catch (AuthenticationException e) {
            log.warn("用户登录失败: account={}, reason={}", request.getAccount(), e.getMessage());
            return ApiResponse.error(ResultCode.LOGIN_ERROR.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("用户登录异常: account={}", request.getAccount(), e);
            return ApiResponse.error(ResultCode.SYSTEM_ERROR.getCode(), "登录失败，请稍后重试");
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出接口")
    public ApiResponse<Void> logout() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            StpUtil.logout();
            
            log.info("用户登出成功: userId={}", userId);
            
            return ApiResponse.success("登出成功");
            
        } catch (Exception e) {
            log.error("用户登出异常", e);
            return ApiResponse.error("登出失败");
        }
    }
    
    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否已存在")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        try {
            boolean exists = userRegisterService.isUsernameExists(username);
            return ApiResponse.success("检查完成", !exists); // 返回是否可用
        } catch (Exception e) {
            log.error("检查用户名异常: username={}", username, e);
            return ApiResponse.error("检查失败");
        }
    }
    
    /**
     * 检查手机号是否存在
     */
    @GetMapping("/check-phone")
    @Operation(summary = "检查手机号", description = "检查手机号是否已存在")
    public ApiResponse<Boolean> checkPhone(@RequestParam String phone) {
        try {
            boolean exists = userRegisterService.isPhoneExists(phone);
            return ApiResponse.success("检查完成", !exists); // 返回是否可用
        } catch (Exception e) {
            log.error("检查手机号异常: phone={}", phone, e);
            return ApiResponse.error("检查失败");
        }
    }
    
    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已存在")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        try {
            boolean exists = userRegisterService.isEmailExists(email);
            return ApiResponse.success("检查完成", !exists); // 返回是否可用
        } catch (Exception e) {
            log.error("检查邮箱异常: email={}", email, e);
            return ApiResponse.error("检查失败");
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/user-info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息")
    public ApiResponse<SysUser> getUserInfo() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            // 这里应该调用 UserService 获取用户信息，简化处理
            return ApiResponse.success("获取成功", null);
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return ApiResponse.error("获取失败");
        }
    }
    
    /**
     * 密码加密测试接口（仅用于开发测试）
     */
    @PostMapping("/test-password")
    @Operation(summary = "密码加密测试", description = "测试密码加密功能（仅开发环境）")
    public ApiResponse<Object> testPassword(@RequestParam String password) {
        try {
            String[] result = Md5SaltUtil.generateTestPassword(password);
            
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("rawPassword", password);
            data.put("salt", result[0]);
            data.put("hashedPassword", result[1]);
            data.put("verification", Md5SaltUtil.matches(password, result[1], result[0]));
            
            return ApiResponse.success("测试完成", data);
        } catch (Exception e) {
            log.error("密码加密测试异常", e);
            return ApiResponse.error("测试失败");
        }
    }
    
    // ========================================
    // 私有辅助方法
    // ========================================
    
    /**
     * 丰富注册请求信息
     */
    private void enrichRegisterRequest(RegisterRequest request, HttpServletRequest httpRequest) {
        // 设置客户端IP
        String clientIp = getClientIp(httpRequest);
        // 可以在 RegisterRequest 中添加 clientIp 字段
        
        // 设置用户代理
        String userAgent = httpRequest.getHeader("User-Agent");
        // 可以在 RegisterRequest 中添加 userAgent 字段
        
        log.debug("注册请求客户端信息: ip={}, userAgent={}", clientIp, userAgent);
    }
    
    /**
     * 丰富登录请求信息
     */
    private void enrichLoginRequest(LoginRequest request, HttpServletRequest httpRequest) {
        // 设置客户端IP
        String clientIp = getClientIp(httpRequest);
        request.setClientIp(clientIp);
        
        // 设置用户代理
        String userAgent = httpRequest.getHeader("User-Agent");
        request.setUserAgent(userAgent);
        
        log.debug("登录请求客户端信息: ip={}, userAgent={}", clientIp, userAgent);
    }
    
    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(SysUser user) {
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRealName(user.getRealName());
        response.setUserType(user.getUserType());
        response.setAccessToken(StpUtil.getTokenValue());
        response.setTokenType("Bearer");
        response.setExpiresIn(StpUtil.getTokenTimeout());
        response.setLoginTime(LocalDateTime.now());
        
        return response;
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
}
