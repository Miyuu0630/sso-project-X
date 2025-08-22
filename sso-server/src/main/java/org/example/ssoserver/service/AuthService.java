package org.example.ssoserver.service;

import org.example.common.dto.LoginRequest;
import org.example.common.dto.LoginResponse;
import org.example.common.model.UserDTO;
import org.example.ssoserver.entity.SysUser;

import java.util.List;

/**
 * 认证服务接口
 * 提供SSO认证的核心业务逻辑
 */
public interface AuthService {
    
    // ========================================
    // SSO认证相关
    // ========================================
    
    /**
     * SSO登录认证
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse ssoLogin(LoginRequest request);
    
    /**
     * 生成SSO票据
     * @param userId 用户ID
     * @param clientId 客户端ID
     * @param redirectUri 重定向地址
     * @return SSO票据
     */
    String generateSsoTicket(Long userId, String clientId, String redirectUri);
    
    /**
     * 验证SSO票据
     * @param ticket SSO票据
     * @param clientId 客户端ID
     * @return 用户信息，验证失败返回null
     */
    UserDTO validateSsoTicket(String ticket, String clientId);
    
    /**
     * 销毁SSO票据
     * @param ticket SSO票据
     * @return 是否成功
     */
    boolean destroySsoTicket(String ticket);
    
    /**
     * SSO单点登出
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean ssoLogout(Long userId);
    
    /**
     * 检查用户是否已登录
     * @param userId 用户ID
     * @return 是否已登录
     */
    boolean isUserLoggedIn(Long userId);
    
    // ========================================
    // 多种登录方式支持
    // ========================================
    
    /**
     * 密码登录
     * @param account 登录账号
     * @param password 密码
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse passwordLogin(String account, String password, LoginRequest request);
    
    /**
     * 短信验证码登录
     * @param phone 手机号
     * @param verificationCode 验证码
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse smsLogin(String phone, String verificationCode, LoginRequest request);
    
    /**
     * 邮箱验证码登录
     * @param email 邮箱
     * @param verificationCode 验证码
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse emailLogin(String email, String verificationCode, LoginRequest request);
    
    /**
     * 第三方OAuth登录
     * @param provider 第三方平台
     * @param code 授权码
     * @param state 状态码
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse oauthLogin(String provider, String code, String state, LoginRequest request);
    
    // ========================================
    // Token管理相关
    // ========================================
    
    /**
     * 生成访问令牌
     * @param user 用户信息
     * @param rememberMe 是否记住我
     * @return 访问令牌
     */
    String generateAccessToken(SysUser user, boolean rememberMe);
    
    /**
     * 生成刷新令牌
     * @param userId 用户ID
     * @return 刷新令牌
     */
    String generateRefreshToken(Long userId);
    
    /**
     * 验证访问令牌
     * @param token 访问令牌
     * @return 用户ID，验证失败返回null
     */
    Long validateAccessToken(String token);
    
    /**
     * 刷新访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    String refreshAccessToken(String refreshToken);
    
    /**
     * 销毁令牌
     * @param token 令牌
     * @return 是否成功
     */
    boolean destroyToken(String token);
    
    /**
     * 销毁用户所有令牌
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean destroyAllTokens(Long userId);
    
    // ========================================
    // 权限验证相关
    // ========================================
    
    /**
     * 获取用户角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<String> getUserRoles(Long userId);
    
    /**
     * 获取用户权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> getUserPermissions(Long userId);
    
    /**
     * 检查用户是否有指定角色
     * @param userId 用户ID
     * @param role 角色
     * @return 是否有权限
     */
    boolean hasRole(Long userId, String role);
    
    /**
     * 检查用户是否有指定权限
     * @param userId 用户ID
     * @param permission 权限
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permission);
    
    // ========================================
    // 安全相关
    // ========================================
    
    /**
     * 记录登录日志
     * @param user 用户信息
     * @param request 登录请求
     * @param success 是否成功
     * @param message 消息
     */
    void recordLoginLog(SysUser user, LoginRequest request, boolean success, String message);
    
    /**
     * 检查登录安全性
     * @param user 用户信息
     * @param request 登录请求
     * @return 安全检查结果
     */
    SecurityCheckResult checkLoginSecurity(SysUser user, LoginRequest request);
    
    /**
     * 发送安全通知
     * @param user 用户信息
     * @param eventType 事件类型
     * @param details 详细信息
     */
    void sendSecurityNotification(SysUser user, String eventType, String details);
    
    // ========================================
    // 内部类：安全检查结果
    // ========================================
    
    /**
     * 安全检查结果
     */
    class SecurityCheckResult {
        private boolean passed;
        private String reason;
        private List<String> warnings;
        
        public SecurityCheckResult(boolean passed, String reason) {
            this.passed = passed;
            this.reason = reason;
        }
        
        public SecurityCheckResult(boolean passed, String reason, List<String> warnings) {
            this.passed = passed;
            this.reason = reason;
            this.warnings = warnings;
        }
        
        // Getters and Setters
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }
}
