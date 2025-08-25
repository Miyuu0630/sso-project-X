package org.example.ssoserver.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.LoginRequest;
import org.example.common.dto.LoginResponse;
import org.example.common.model.UserDTO;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResultCode;
import org.example.common.util.EncryptUtil;
import org.example.common.util.DeviceUtil;
import org.example.ssoserver.entity.SysUser;
import org.example.ssoserver.entity.SysLoginLog;
import org.example.ssoserver.service.AuthService;
import org.example.ssoserver.service.SysUserService;
import org.example.ssoserver.dto.SsoTicketInfo;
import org.example.ssoserver.dto.RefreshTokenInfo;
import org.example.ssoserver.service.PermissionService;
import org.example.ssoserver.mapper.SysLoginLogMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final SysUserService userService;
    private final PermissionService permissionService;
    private final SysLoginLogMapper loginLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // SSO票据前缀
    private static final String SSO_TICKET_PREFIX = "sso:ticket:";
    // SSO票据有效期（分钟）
    private static final int SSO_TICKET_EXPIRE_MINUTES = 5;

    // Refresh Token前缀
    private static final String REFRESH_TOKEN_PREFIX = "sso:refresh:";

    // Refresh Token锁前缀（用于并发控制）
    private static final String REFRESH_TOKEN_LOCK_PREFIX = "sso:refresh:lock:";

    // 锁过期时间（秒）
    private static final int LOCK_EXPIRE_SECONDS = 30;

    // Refresh Token配置（从配置文件读取）
    @Value("${refresh-token.expire-days:7}")
    private int refreshTokenExpireDays;

    @Value("${refresh-token.auto-renewal:true}")
    private boolean refreshTokenAutoRenewal;

    @Value("${refresh-token.renewal-threshold-days:1}")
    private int refreshTokenRenewalThresholdDays;

    @Value("${refresh-token.device-fingerprint-check:true}")
    private boolean deviceFingerprintCheck;
    
    @Override
    public LoginResponse ssoLogin(LoginRequest request) {
        try {
            // 根据登录类型执行不同的登录逻辑
            switch (request.getLoginType()) {
                case "password":
                    return passwordLogin(request.getAccount(), request.getPassword(), request);
                case "sms":
                    return smsLogin(request.getAccount(), request.getVerificationCode(), request);
                case "email":
                    return emailLogin(request.getAccount(), request.getVerificationCode(), request);
                case "oauth":
                    return oauthLogin(request.getProvider(), request.getCode(), request.getState(), request);
                default:
                    throw BusinessException.paramError("不支持的登录类型");
            }
        } catch (BusinessException e) {
            // 记录登录失败日志
            recordLoginLog(null, request, false, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("SSO登录异常", e);
            recordLoginLog(null, request, false, "系统异常");
            throw new BusinessException(ResultCode.ERROR, "登录失败");
        }
    }
    
    @Override
    public LoginResponse passwordLogin(String account, String password, LoginRequest request) {
        // 验证用户
        SysUser user = userService.validateUser(account, password);
        if (user == null) {
            throw BusinessException.passwordError();
        }
        
        // 检查登录安全性
        SecurityCheckResult securityCheck = checkLoginSecurity(user, request);
        if (!securityCheck.isPassed()) {
            throw new BusinessException(ResultCode.LOGIN_FAIL_TOO_MANY, securityCheck.getReason());
        }
        
        // 验证用户角色（如果指定了期望角色）
        if (request.getExpectedRole() != null && !request.getExpectedRole().isEmpty()) {
            List<String> userRoles = getUserRoles(user.getId());
            if (!userRoles.contains(request.getExpectedRole())) {
                throw new BusinessException(ResultCode.LOGIN_FAIL_TOO_MANY, 
                    "用户角色不匹配，期望角色：" + request.getExpectedRole() + 
                    "，实际角色：" + String.join(",", userRoles));
            }
        }
        
        // 执行登录
        return performLogin(user, request, securityCheck.getWarnings());
    }
    
    @Override
    public LoginResponse smsLogin(String phone, String verificationCode, LoginRequest request) {
        // 验证短信验证码
        SysUser user = userService.validateUserByCode(phone, verificationCode, "sms");
        if (user == null) {
            throw BusinessException.captchaError();
        }
        
        // 执行登录
        return performLogin(user, request, null);
    }
    
    @Override
    public LoginResponse emailLogin(String email, String verificationCode, LoginRequest request) {
        // 验证邮箱验证码
        SysUser user = userService.validateUserByCode(email, verificationCode, "email");
        if (user == null) {
            throw BusinessException.captchaError();
        }
        
        // 执行登录
        return performLogin(user, request, null);
    }
    
    @Override
    public LoginResponse oauthLogin(String provider, String code, String state, LoginRequest request) {
        // TODO: 实现第三方OAuth登录逻辑
        throw new BusinessException(ResultCode.ERROR, "第三方登录功能暂未实现");
    }
    
    @Override
    public String generateSsoTicket(Long userId, String clientId, String redirectUri) {
        try {
            // 生成唯一票据
            String ticket = IdUtil.fastSimpleUUID();
            
            // 构建票据信息
            SsoTicketInfo ticketInfo = SsoTicketInfo.builder()
                    .userId(userId)
                    .clientId(clientId)
                    .redirectUri(redirectUri)
                    .createTime(LocalDateTime.now())
                    .build();
            
            // 存储到Redis，设置过期时间
            String key = SSO_TICKET_PREFIX + ticket;
            redisTemplate.opsForValue().set(key, ticketInfo, SSO_TICKET_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            log.info("生成SSO票据成功: ticket={}, userId={}, clientId={}", ticket, userId, clientId);
            return ticket;
        } catch (Exception e) {
            log.error("生成SSO票据失败: userId={}, clientId={}", userId, clientId, e);
            throw new BusinessException(ResultCode.ERROR, "生成SSO票据失败");
        }
    }
    
    @Override
    public UserDTO validateSsoTicket(String ticket, String clientId) {
        try {
            String key = SSO_TICKET_PREFIX + ticket;
            SsoTicketInfo ticketInfo = (SsoTicketInfo) redisTemplate.opsForValue().get(key);
            
            if (ticketInfo == null) {
                throw BusinessException.ssoTicketInvalid();
            }
            
            // 验证客户端ID（如果提供）
            if (StrUtil.isNotBlank(clientId) && !clientId.equals(ticketInfo.getClientId())) {
                throw new BusinessException(ResultCode.SSO_TICKET_INVALID, "客户端ID不匹配");
            }
            
            // 获取用户信息
            SysUser user = userService.getUserById(ticketInfo.getUserId());
            if (user == null || !user.canLogin()) {
                throw BusinessException.userNotFound();
            }
            
            // 标记票据已使用，但保留一段时间以支持重复验证
            String usedKey = key + ":used";
            Boolean isUsed = redisTemplate.hasKey(usedKey);

            if (Boolean.TRUE.equals(isUsed)) {
                // 票据已被使用过，但在有效期内可以重复验证
                log.debug("票据已被使用，但在有效期内: ticket={}", ticket);
            } else {
                // 首次使用，标记为已使用
                redisTemplate.opsForValue().set(usedKey, "true", Duration.ofMinutes(5));
                log.debug("首次使用票据: ticket={}", ticket);
            }
            
            // 转换为DTO并返回
            UserDTO userDTO = userService.convertToDTO(user);
            userDTO.setRoles(getUserRoles(user.getId()));
            userDTO.setPermissions(getUserPermissions(user.getId()));
            
            log.info("SSO票据验证成功: ticket={}, userId={}", ticket, user.getId());
            return userDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("SSO票据验证异常: ticket={}", ticket, e);
            throw new BusinessException(ResultCode.SSO_TICKET_INVALID, "票据验证失败");
        }
    }
    
    @Override
    public boolean destroySsoTicket(String ticket) {
        try {
            String key = SSO_TICKET_PREFIX + ticket;
            Boolean deleted = redisTemplate.delete(key);
            return Boolean.TRUE.equals(deleted);
        } catch (Exception e) {
            log.error("销毁SSO票据失败: ticket={}", ticket, e);
            return false;
        }
    }
    
    @Override
    public boolean ssoLogout(Long userId) {
        try {
            // Sa-Token登出
            StpUtil.logout(userId);
            
            // 清理相关缓存
            permissionService.refreshUserPermissionCache(userId);
            
            log.info("SSO单点登出成功: userId={}", userId);
            return true;
        } catch (Exception e) {
            log.error("SSO单点登出失败: userId={}", userId, e);
            return false;
        }
    }
    
    @Override
    public boolean isUserLoggedIn(Long userId) {
        try {
            return StpUtil.isLogin(userId);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String generateAccessToken(SysUser user, boolean rememberMe) {
        try {
            // 使用Sa-Token生成token
            StpUtil.login(user.getId(), rememberMe);
            return StpUtil.getTokenValue();
        } catch (Exception e) {
            log.error("生成访问令牌失败: userId={}", user.getId(), e);
            throw new BusinessException(ResultCode.ERROR, "生成访问令牌失败");
        }
    }
    
    @Override
    public String generateRefreshToken(Long userId) {
        try {
            // 生成唯一refresh token
            String refreshToken = IdUtil.fastSimpleUUID();

            // 构建refresh token信息
            RefreshTokenInfo refreshTokenInfo = RefreshTokenInfo.builder()
                    .userId(userId)
                    .accessToken(null) // 在登录时会更新为对应的access token
                    .createTime(LocalDateTime.now())
                    .expireTime(LocalDateTime.now().plusDays(refreshTokenExpireDays))
                    .deviceFingerprint(null) // 在登录时会设置
                    .clientIp(null) // 在登录时会设置
                    .build();

            // 存储到Redis，设置过期时间
            String key = REFRESH_TOKEN_PREFIX + refreshToken;
            redisTemplate.opsForValue().set(key, refreshTokenInfo,
                    refreshTokenExpireDays, TimeUnit.DAYS);

            log.info("生成Refresh Token成功: userId={}, refreshToken={}", userId, refreshToken);
            return refreshToken;
        } catch (Exception e) {
            log.error("生成Refresh Token失败: userId={}", userId, e);
            throw new BusinessException(ResultCode.ERROR, "生成Refresh Token失败");
        }
    }
    
    @Override
    public Long validateAccessToken(String token) {
        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            return loginId != null ? Long.valueOf(loginId.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String refreshAccessToken(String refreshToken) {
        // 生成锁的key
        String lockKey = REFRESH_TOKEN_LOCK_PREFIX + refreshToken;
        String lockValue = IdUtil.fastSimpleUUID();

        try {
            // 尝试获取分布式锁
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue,
                    LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);

            if (Boolean.FALSE.equals(lockAcquired)) {
                log.warn("Refresh Token正在被其他请求处理: refreshToken={}", refreshToken);
                throw new BusinessException(ResultCode.ERROR, "Refresh Token正在处理中，请稍后重试");
            }

            String key = REFRESH_TOKEN_PREFIX + refreshToken;
            RefreshTokenInfo refreshTokenInfo = (RefreshTokenInfo) redisTemplate.opsForValue().get(key);

            if (refreshTokenInfo == null) {
                throw new BusinessException(ResultCode.TOKEN_INVALID, "Refresh Token不存在或已过期");
            }

            // 检查refresh token是否过期
            if (refreshTokenInfo.getExpireTime().isBefore(LocalDateTime.now())) {
                // 删除过期的refresh token
                redisTemplate.delete(key);
                log.warn("Refresh Token已过期: userId={}, refreshToken={}", refreshTokenInfo.getUserId(), refreshToken);
                throw new BusinessException(ResultCode.TOKEN_INVALID, "Refresh Token已过期");
            }

            // 检查是否需要自动续期
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime renewalThreshold = now.plusDays(refreshTokenRenewalThresholdDays);
            boolean needsRenewal = refreshTokenAutoRenewal &&
                                  refreshTokenInfo.getExpireTime().isBefore(renewalThreshold);

            // 如果启用了设备指纹检查，验证设备一致性
            if (deviceFingerprintCheck && refreshTokenInfo.getDeviceFingerprint() != null) {
                // 验证设备指纹是否匹配（这里需要从请求中获取当前设备指纹）
                // 由于refresh token接口可能没有完整的请求信息，我们暂时记录日志
                log.debug("设备指纹检查已启用，存储的指纹: {}, IP: {}",
                         refreshTokenInfo.getDeviceFingerprint(), refreshTokenInfo.getClientIp());
            }

            // 获取用户ID
            Long userId = refreshTokenInfo.getUserId();

            // 检查用户是否仍然有效
            SysUser user = userService.getUserById(userId);
            if (user == null || !user.canLogin()) {
                // 删除无效的refresh token
                redisTemplate.delete(key);
                throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在或已被禁用");
            }

            // 生成新的access token
            StpUtil.login(userId);
            String newAccessToken = StpUtil.getTokenValue();

            // 更新refresh token信息中的access token
            refreshTokenInfo.setAccessToken(newAccessToken);

            // 如果需要自动续期，延长refresh token的过期时间
            if (needsRenewal) {
                LocalDateTime newExpireTime = now.plusDays(refreshTokenExpireDays);
                refreshTokenInfo.setExpireTime(newExpireTime);
                refreshTokenInfo.setCreateTime(now); // 更新创建时间

                log.info("Refresh Token自动续期: userId={}, oldExpireTime={}, newExpireTime={}",
                        userId, refreshTokenInfo.getExpireTime(), newExpireTime);
            }

            // 重新存储到Redis
            redisTemplate.opsForValue().set(key, refreshTokenInfo,
                    refreshTokenExpireDays, TimeUnit.DAYS);

            log.info("Refresh Token刷新成功: userId={}, refreshToken={}, newAccessToken={}, renewed={}",
                    userId, refreshToken, newAccessToken, needsRenewal);

            return newAccessToken;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Refresh Token刷新异常: refreshToken={}", refreshToken, e);
            throw new BusinessException(ResultCode.TOKEN_INVALID, "Refresh Token刷新失败");
        } finally {
            // 释放分布式锁
            try {
                Object lockValueObj = redisTemplate.opsForValue().get(lockKey);
                if (lockValueObj instanceof String) {
                    String currentLockValue = (String) lockValueObj;
                    if (lockValue.equals(currentLockValue)) {
                        redisTemplate.delete(lockKey);
                        log.debug("Refresh Token锁已释放: refreshToken={}", refreshToken);
                    }
                }
            } catch (Exception e) {
                log.warn("释放Refresh Token锁失败: refreshToken={}, error={}", refreshToken, e.getMessage());
            }
        }
    }
    
    @Override
    public boolean destroyToken(String token) {
        try {
            StpUtil.logoutByTokenValue(token);
            return true;
        } catch (Exception e) {
            log.error("销毁令牌失败: token={}", token, e);
            return false;
        }
    }
    
    @Override
    public boolean destroyAllTokens(Long userId) {
        try {
            StpUtil.logout(userId);
            return true;
        } catch (Exception e) {
            log.error("销毁用户所有令牌失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        return permissionService.getUserRoles(userId);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        return permissionService.getUserPermissions(userId);
    }

    @Override
    public boolean hasRole(Long userId, String role) {
        return permissionService.hasRole(userId, role);
    }

    @Override
    public boolean hasPermission(Long userId, String permission) {
        return permissionService.hasPermission(userId, permission);
    }

    @Override
    public void recordLoginLog(SysUser user, LoginRequest request, boolean success, String message) {
        try {
            SysLoginLog loginLog = SysLoginLog.builder()
                    .userId(user != null ? user.getId() : null)
                    .username(user != null ? user.getUsername() : request.getAccount())
                    .loginType(request.getLoginType())
                    .loginIp(request.getClientIp())
                    .loginLocation(getLocationByIp(request.getClientIp()))
                    .browser(DeviceUtil.getBrowserName(request.getUserAgent()))
                    .os(DeviceUtil.getOperatingSystem(request.getUserAgent()))
                    .deviceType(DeviceUtil.getDeviceType(request.getUserAgent()).getCode())
                    .deviceFingerprint(request.getDeviceFingerprint())
                    .loginTime(LocalDateTime.now())
                    .status(success ? "1" : "0")
                    .msg(message)
                    .build();

            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }

    @Override
    public SecurityCheckResult checkLoginSecurity(SysUser user, LoginRequest request) {
        try {
            // 检查用户状态
            if (!user.canLogin()) {
                return new SecurityCheckResult(false, "账号已被禁用或锁定");
            }

            // 检查登录失败次数
            if (user.getFailedLoginCount() != null && user.getFailedLoginCount() >= 5) {
                return new SecurityCheckResult(false, "登录失败次数过多，账号已被锁定");
            }

            // 检查设备安全性（暂时放宽检测，只记录日志）
            String riskLevel = DeviceUtil.getDeviceRiskLevel(request.getUserAgent(), isNewDevice(user.getId(), request));
            if ("HIGH".equals(riskLevel)) {
                log.warn("检测到高风险设备登录: userId={}, userAgent={}, ip={}",
                        user.getId(), request.getUserAgent(), request.getClientIp());
                // 暂时不阻止登录，只记录警告
                // return new SecurityCheckResult(false, "检测到可疑设备，登录被拒绝");
            }

            // 生成安全警告
            List<String> warnings = generateSecurityWarnings(user, request);

            return new SecurityCheckResult(true, "安全检查通过", warnings);
        } catch (Exception e) {
            log.error("安全检查异常", e);
            return new SecurityCheckResult(false, "安全检查失败");
        }
    }

    @Override
    public void sendSecurityNotification(SysUser user, String eventType, String details) {
        // TODO: 实现安全通知发送逻辑（邮件、短信等）
        log.info("安全通知: userId={}, eventType={}, details={}", user.getId(), eventType, details);
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    /**
     * 执行登录
     */
    private LoginResponse performLogin(SysUser user, LoginRequest request, List<String> warnings) {
        try {
            // 生成访问令牌
            String accessToken = generateAccessToken(user, request.getRememberMe() != null ? request.getRememberMe() : false);

            // 生成refresh token
            String refreshToken = generateRefreshToken(user.getId());

            // 更新refresh token信息，关联access token和设备信息
            String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;
            RefreshTokenInfo refreshTokenInfo = (RefreshTokenInfo) redisTemplate.opsForValue().get(refreshKey);
            if (refreshTokenInfo != null) {
                refreshTokenInfo.setAccessToken(accessToken);

                // 生成或使用设备指纹
                String deviceFingerprint = generateDeviceFingerprint(request);
                refreshTokenInfo.setDeviceFingerprint(deviceFingerprint);
                refreshTokenInfo.setClientIp(request.getClientIp());

                redisTemplate.opsForValue().set(refreshKey, refreshTokenInfo,
                        refreshTokenExpireDays, TimeUnit.DAYS);

                log.debug("Refresh Token设备信息更新: userId={}, deviceFingerprint={}, ip={}",
                         user.getId(), deviceFingerprint, request.getClientIp());
            }

            // 更新用户登录信息
            userService.updateLoginInfo(user.getId(), request.getClientIp());

            // 记录登录日志
            recordLoginLog(user, request, true, "登录成功");

            // 构建登录响应
            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(StpUtil.getTokenTimeout())
                    .refreshTokenExpiresIn(refreshTokenExpireDays * 24 * 60 * 60L) // 转换为秒
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .realName(user.getRealName())
                    .avatar(user.getAvatar())
                    .userType(user.getUserType())
                    .roles(getUserRoles(user.getId()))
                    .permissions(getUserPermissions(user.getId()))
                    .isNewDevice(isNewDevice(user.getId(), request))
                    .deviceType(DeviceUtil.getDeviceType(request.getUserAgent()).getCode())
                    .loginLocation(getLocationByIp(request.getClientIp()))
                    .loginTime(LocalDateTime.now())
                    .securityWarnings(warnings)
                    .build();

            // 如果是SSO登录，生成重定向信息
            if (StrUtil.isNotBlank(request.getRedirectUri())) {
                String ticket = generateSsoTicket(user.getId(), request.getClientId(), request.getRedirectUri());
                response.setTicket(ticket);
                response.setRedirectUri(request.getRedirectUri());
                response.setState(request.getState());
            }

            return response;
        } catch (Exception e) {
            log.error("执行登录失败: userId={}", user.getId(), e);
            throw new BusinessException(ResultCode.ERROR, "登录失败");
        }
    }

    /**
     * 检查是否为新设备
     */
    private boolean isNewDevice(Long userId, LoginRequest request) {
        // TODO: 实现设备识别逻辑
        return false;
    }

    /**
     * 根据IP获取地理位置
     */
    private String getLocationByIp(String ip) {
        // TODO: 实现IP地理位置查询
        return "未知";
    }

    /**
     * 生成安全警告
     */
    private List<String> generateSecurityWarnings(SysUser user, LoginRequest request) {
        List<String> warnings = new java.util.ArrayList<>();

        // 检查是否为新设备
        if (isNewDevice(user.getId(), request)) {
            warnings.add("检测到新设备登录");
        }

        // 检查是否为异地登录
        // TODO: 实现异地登录检测

        return warnings;
    }

    /**
     * 生成设备指纹
     */
    private String generateDeviceFingerprint(LoginRequest request) {
        try {
            // 基于User-Agent、IP地址、设备类型等生成设备指纹
            StringBuilder fingerprint = new StringBuilder();
            fingerprint.append(request.getUserAgent() != null ? request.getUserAgent().hashCode() : "unknown");
            fingerprint.append("_");
            fingerprint.append(request.getClientIp() != null ? request.getClientIp() : "unknown");
            fingerprint.append("_");
            fingerprint.append(request.getDeviceInfo() != null ? request.getDeviceInfo().hashCode() : "unknown");

            String fingerprintStr = String.valueOf(fingerprint.toString().hashCode());
            log.debug("生成设备指纹: userAgent={}, ip={}, fingerprint={}",
                     request.getUserAgent(), request.getClientIp(), fingerprintStr);
            return fingerprintStr;
        } catch (Exception e) {
            log.warn("生成设备指纹失败", e);
            return "unknown";
        }
    }

    /**
     * 验证设备指纹
     */
    private boolean validateDeviceFingerprint(String storedFingerprint, String currentFingerprint) {
        if (!deviceFingerprintCheck) {
            return true; // 如果未启用设备指纹检查，直接通过
        }

        if (storedFingerprint == null || currentFingerprint == null) {
            log.warn("设备指纹验证失败: 存储指纹或当前指纹为空");
            return false;
        }

        boolean isValid = storedFingerprint.equals(currentFingerprint);
        log.debug("设备指纹验证: stored={}, current={}, valid={}",
                 storedFingerprint, currentFingerprint, isValid);
        return isValid;
    }

    /**
     * 增强的Refresh Token刷新（带设备验证）
     */
    public String refreshAccessTokenWithValidation(String refreshToken, String currentDeviceFingerprint, String currentIp) {
        try {
            String key = REFRESH_TOKEN_PREFIX + refreshToken;
            RefreshTokenInfo refreshTokenInfo = (RefreshTokenInfo) redisTemplate.opsForValue().get(key);

            if (refreshTokenInfo == null) {
                throw new BusinessException(ResultCode.TOKEN_INVALID, "Refresh Token不存在或已过期");
            }

            // 检查refresh token是否过期
            LocalDateTime now = LocalDateTime.now();
            if (refreshTokenInfo.getExpireTime().isBefore(now)) {
                redisTemplate.delete(key);
                log.warn("Refresh Token已过期: userId={}, refreshToken={}", refreshTokenInfo.getUserId(), refreshToken);
                throw new BusinessException(ResultCode.TOKEN_INVALID, "Refresh Token已过期");
            }

            // 验证设备指纹
            if (!validateDeviceFingerprint(refreshTokenInfo.getDeviceFingerprint(), currentDeviceFingerprint)) {
                log.warn("设备指纹验证失败: userId={}, refreshToken={}", refreshTokenInfo.getUserId(), refreshToken);
                throw new BusinessException(ResultCode.TOKEN_INVALID, "设备指纹验证失败，可能存在安全风险");
            }

            // 检查是否需要自动续期
            LocalDateTime renewalThreshold = now.plusDays(refreshTokenRenewalThresholdDays);
            boolean needsRenewal = refreshTokenAutoRenewal &&
                                  refreshTokenInfo.getExpireTime().isBefore(renewalThreshold);

            // 获取用户ID并验证用户
            Long userId = refreshTokenInfo.getUserId();
            SysUser user = userService.getUserById(userId);
            if (user == null || !user.canLogin()) {
                redisTemplate.delete(key);
                throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在或已被禁用");
            }

            // 生成新的access token
            StpUtil.login(userId);
            String newAccessToken = StpUtil.getTokenValue();

            // 更新refresh token信息
            refreshTokenInfo.setAccessToken(newAccessToken);

            if (needsRenewal) {
                LocalDateTime newExpireTime = now.plusDays(refreshTokenExpireDays);
                refreshTokenInfo.setExpireTime(newExpireTime);
                refreshTokenInfo.setCreateTime(now);
                log.info("Refresh Token自动续期: userId={}, newExpireTime={}", userId, newExpireTime);
            }

            redisTemplate.opsForValue().set(key, refreshTokenInfo, refreshTokenExpireDays, TimeUnit.DAYS);

            log.info("Refresh Token刷新成功: userId={}, refreshToken={}, newAccessToken={}, renewed={}, deviceValid={}",
                    userId, refreshToken, newAccessToken, needsRenewal, true);

            return newAccessToken;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Refresh Token刷新异常: refreshToken={}", refreshToken, e);
            throw new BusinessException(ResultCode.TOKEN_INVALID, "Refresh Token刷新失败");
        }
    }


}
