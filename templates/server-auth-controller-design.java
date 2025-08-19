/**
 * SSO Server 层认证控制器设计
 * 企业级单点登录认证中心核心接口
 */

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Validated
public class SsoAuthController {

    @Autowired
    private SsoAuthService ssoAuthService;
    
    @Autowired
    private SysUserService userService;

    /**
     * 1. 统一登录接口
     * 支持用户名/手机号/邮箱登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        try {
            log.info("用户登录请求: loginType={}, credential={}", 
                    request.getLoginType(), request.getCredential());
            
            // 1. 参数验证
            validateLoginRequest(request);
            
            // 2. 用户认证
            SysUser user = authenticateUser(request);
            
            // 3. 检查账号状态
            checkAccountStatus(user);
            
            // 4. 生成 Token
            String accessToken = ssoAuthService.generateAccessToken(user);
            String refreshToken = ssoAuthService.generateRefreshToken(user);
            
            // 5. 记录登录日志
            recordLoginLog(user, request, true, null);
            
            // 6. 构建响应
            LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(7200) // 2小时
                .refreshToken(refreshToken)
                .redirectUri(buildRedirectUri(request.getRedirectUri(), accessToken))
                .userInfo(buildUserInfo(user))
                .build();
            
            return Result.success(response);
            
        } catch (AuthenticationException e) {
            log.warn("登录失败: {}", e.getMessage());
            recordLoginLog(null, request, false, e.getMessage());
            return Result.error(401, e.getMessage());
        } catch (Exception e) {
            log.error("登录异常", e);
            return Result.error(500, "登录服务异常");
        }
    }

    /**
     * 2. Token 验证接口
     */
    @GetMapping("/verify")
    public Result<TokenVerifyResponse> verifyToken(
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = extractToken(authorization);
            
            // 验证 Token
            TokenInfo tokenInfo = ssoAuthService.verifyToken(token);
            
            if (tokenInfo.isValid()) {
                SysUser user = userService.getUserById(tokenInfo.getUserId());
                
                TokenVerifyResponse response = TokenVerifyResponse.builder()
                    .valid(true)
                    .userId(user.getId())
                    .expiresIn(tokenInfo.getExpiresIn())
                    .userInfo(buildSimpleUserInfo(user))
                    .build();
                
                return Result.success(response);
            } else {
                return Result.error(401, "Token无效或已过期");
            }
            
        } catch (Exception e) {
            log.error("Token验证异常", e);
            return Result.error(401, "Token验证失败");
        }
    }

    /**
     * 3. 获取用户信息接口
     */
    @GetMapping("/userinfo")
    public Result<UserInfoResponse> getUserInfo(
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = extractToken(authorization);
            Long userId = ssoAuthService.getUserIdFromToken(token);
            
            SysUser user = userService.getUserById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            UserInfoResponse response = buildDetailedUserInfo(user);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return Result.error(500, "获取用户信息失败");
        }
    }

    /**
     * 4. 登出接口
     */
    @PostMapping("/logout")
    public Result<LogoutResponse> logout(
            @RequestHeader("Authorization") String authorization,
            @RequestBody LogoutRequest request) {
        try {
            String token = extractToken(authorization);
            Long userId = ssoAuthService.getUserIdFromToken(token);
            
            // 使 Token 失效
            ssoAuthService.revokeToken(token);
            
            // 记录登出日志
            recordLogoutLog(userId);
            
            LogoutResponse response = LogoutResponse.builder()
                .success(true)
                .redirectUri(request.getRedirectUri())
                .message("登出成功")
                .build();
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("登出异常", e);
            return Result.error(500, "登出失败");
        }
    }

    /**
     * 5. Token 刷新接口
     */
    @PostMapping("/refresh")
    public Result<RefreshTokenResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        try {
            String newAccessToken = ssoAuthService.refreshAccessToken(request.getRefreshToken());
            
            RefreshTokenResponse response = RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(7200)
                .build();
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("Token刷新异常", e);
            return Result.error(401, "Token刷新失败");
        }
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    /**
     * 验证登录请求参数
     */
    private void validateLoginRequest(LoginRequest request) {
        if (StringUtils.isBlank(request.getCredential())) {
            throw new IllegalArgumentException("登录凭证不能为空");
        }
        if (StringUtils.isBlank(request.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 根据登录类型验证格式
        switch (request.getLoginType()) {
            case "phone":
                if (!PhoneUtil.isValidPhone(request.getCredential())) {
                    throw new IllegalArgumentException("手机号格式不正确");
                }
                break;
            case "email":
                if (!EmailUtil.isValidEmail(request.getCredential())) {
                    throw new IllegalArgumentException("邮箱格式不正确");
                }
                break;
        }
    }

    /**
     * 用户认证
     */
    private SysUser authenticateUser(LoginRequest request) {
        SysUser user = null;
        
        // 根据登录类型查找用户
        switch (request.getLoginType()) {
            case "username":
                user = userService.getUserByUsername(request.getCredential());
                break;
            case "phone":
                user = userService.getUserByPhone(request.getCredential());
                break;
            case "email":
                user = userService.getUserByEmail(request.getCredential());
                break;
            default:
                throw new IllegalArgumentException("不支持的登录类型");
        }
        
        if (user == null) {
            throw new AuthenticationException("用户不存在");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("密码错误");
        }
        
        return user;
    }

    /**
     * 检查账号状态
     */
    private void checkAccountStatus(SysUser user) {
        if (user.getStatus() == 0) {
            throw new AuthenticationException("账号已被禁用");
        }
        if (user.getStatus() == 2) {
            throw new AuthenticationException("账号已被锁定");
        }
    }

    /**
     * 构建重定向 URI
     */
    private String buildRedirectUri(String redirectUri, String token) {
        if (StringUtils.isBlank(redirectUri)) {
            return null;
        }
        
        String separator = redirectUri.contains("?") ? "&" : "?";
        return redirectUri + separator + "token=" + token;
    }

    /**
     * 构建用户信息响应
     */
    private UserInfo buildUserInfo(SysUser user) {
        List<String> roles = roleService.getUserRoles(user.getId())
            .stream()
            .map(SysRole::getRoleCode)
            .collect(Collectors.toList());
        
        return UserInfo.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .userType(user.getUserType())
            .roles(roles)
            .avatar(user.getAvatar())
            .build();
    }

    /**
     * 提取 Token
     */
    private String extractToken(String authorization) {
        if (StringUtils.isBlank(authorization) || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("无效的Authorization头");
        }
        return authorization.substring(7);
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(SysUser user, LoginRequest request, boolean success, String failureReason) {
        // 实现登录日志记录逻辑
    }
}
