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
import org.example.ssoserver.service.PermissionService;
import org.example.ssoserver.mapper.SysLoginLogMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
            
            // 销毁一次性票据
            redisTemplate.delete(key);
            
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
        // Sa-Token的refresh token功能
        // 这里可以根据需要实现自定义的refresh token逻辑
        return null;
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
        // TODO: 实现refresh token逻辑
        throw new BusinessException(ResultCode.ERROR, "刷新令牌功能暂未实现");
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
            String accessToken = generateAccessToken(user, request.getRememberMe());

            // 更新用户登录信息
            userService.updateLoginInfo(user.getId(), request.getClientIp());

            // 记录登录日志
            recordLoginLog(user, request, true, "登录成功");

            // 构建登录响应
            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(StpUtil.getTokenTimeout())
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


}
