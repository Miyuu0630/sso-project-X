package org.example.ssoserver.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ssoserver.entity.SysUser;
import org.example.ssoserver.mapper.SysUserMapper;
import org.example.ssoserver.util.Md5SaltUtil;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义认证提供者
 * 使用 MD5 + 盐值进行密码验证
 * 
 * @author SSO Team
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {
    
    private final SysUserMapper userMapper;
    
    /**
     * 最大登录失败次数
     */
    private static final int MAX_FAILED_ATTEMPTS = 5;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        
        log.debug("开始认证用户: {}", username);
        
        try {
            // 1. 查询用户信息
            SysUser user = userMapper.selectByAccount(username);
            if (user == null) {
                log.warn("用户不存在: {}", username);
                throw new BadCredentialsException("用户名或密码错误");
            }
            
            // 2. 检查用户状态
            checkUserStatus(user);
            
            // 3. 验证密码
            boolean passwordMatches = Md5SaltUtil.matches(password, user.getPassword(), user.getSalt());
            
            if (passwordMatches) {
                // 密码正确 - 重置失败次数并更新登录信息
                handleSuccessfulLogin(user);
                
                // 创建认证成功的 Authentication 对象
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                
                // 根据用户类型添加角色
                switch (user.getUserType()) {
                    case "admin":
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        break;
                    case "enterprise":
                        authorities.add(new SimpleGrantedAuthority("ROLE_ENTERPRISE"));
                        break;
                    case "airline":
                        authorities.add(new SimpleGrantedAuthority("ROLE_AIRLINE"));
                        break;
                    default:
                        authorities.add(new SimpleGrantedAuthority("ROLE_NORMAL"));
                        break;
                }
                
                log.info("用户认证成功: userId={}, username={}", user.getId(), username);
                
                return new UsernamePasswordAuthenticationToken(user, password, authorities);
                
            } else {
                // 密码错误 - 记录失败次数
                handleFailedLogin(user);
                
                log.warn("用户密码错误: username={}, failedCount={}", username, user.getFailedLoginCount() + 1);
                throw new BadCredentialsException("用户名或密码错误");
            }
            
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("认证过程发生异常: username={}", username, e);
            throw new BadCredentialsException("认证失败，请稍后重试");
        }
    }
    
    /**
     * 检查用户状态
     */
    private void checkUserStatus(SysUser user) throws AuthenticationException {
        // 检查用户是否被禁用
        if (!"1".equals(user.getStatus())) {
            log.warn("用户已被禁用: userId={}, username={}", user.getId(), user.getUsername());
            throw new DisabledException("账号已被禁用");
        }
        
        // 检查用户是否被锁定
        if (user.getIsLocked() != null && user.getIsLocked() == 1) {
            log.warn("用户账号已被锁定: userId={}, username={}, lockTime={}", 
                    user.getId(), user.getUsername(), user.getLockTime());
            throw new LockedException("账号已被锁定，请联系管理员");
        }
        
        // 检查登录失败次数
        if (user.getFailedLoginCount() != null && user.getFailedLoginCount() >= MAX_FAILED_ATTEMPTS) {
            // 自动锁定账号
            lockUser(user);
            log.warn("用户因登录失败次数过多被自动锁定: userId={}, username={}, failedCount={}", 
                    user.getId(), user.getUsername(), user.getFailedLoginCount());
            throw new LockedException("登录失败次数过多，账号已被锁定");
        }
    }
    
    /**
     * 处理登录成功
     */
    private void handleSuccessfulLogin(SysUser user) {
        try {
            SysUser updateUser = new SysUser();
            updateUser.setId(user.getId());
            updateUser.setFailedLoginCount(0);  // 重置失败次数
            updateUser.setLastLoginTime(LocalDateTime.now());
            updateUser.setLoginCount((user.getLoginCount() != null ? user.getLoginCount() : 0) + 1);
            
            userMapper.updateById(updateUser);
            
            log.debug("更新用户登录信息成功: userId={}", user.getId());
            
        } catch (Exception e) {
            log.error("更新用户登录信息失败: userId={}", user.getId(), e);
        }
    }
    
    /**
     * 处理登录失败
     */
    private void handleFailedLogin(SysUser user) {
        try {
            int newFailedCount = (user.getFailedLoginCount() != null ? user.getFailedLoginCount() : 0) + 1;
            
            SysUser updateUser = new SysUser();
            updateUser.setId(user.getId());
            updateUser.setFailedLoginCount(newFailedCount);
            updateUser.setLastFailedLoginTime(LocalDateTime.now());
            
            // 如果达到最大失败次数，锁定账号
            if (newFailedCount >= MAX_FAILED_ATTEMPTS) {
                updateUser.setIsLocked(1);
                updateUser.setLockTime(LocalDateTime.now());
            }
            
            userMapper.updateById(updateUser);
            
            log.debug("更新用户登录失败信息: userId={}, failedCount={}", user.getId(), newFailedCount);
            
        } catch (Exception e) {
            log.error("更新用户登录失败信息失败: userId={}", user.getId(), e);
        }
    }
    
    /**
     * 锁定用户账号
     */
    private void lockUser(SysUser user) {
        try {
            SysUser updateUser = new SysUser();
            updateUser.setId(user.getId());
            updateUser.setIsLocked(1);
            updateUser.setLockTime(LocalDateTime.now());
            
            userMapper.updateById(updateUser);
            
            log.info("用户账号已被锁定: userId={}, username={}", user.getId(), user.getUsername());
            
        } catch (Exception e) {
            log.error("锁定用户账号失败: userId={}", user.getId(), e);
        }
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
