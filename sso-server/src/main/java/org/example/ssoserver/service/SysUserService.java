package org.example.ssoserver.service;

import org.example.common.dto.LoginRequest;
import org.example.common.dto.RegisterRequest;
import org.example.common.entity.SysUser;
import org.example.common.result.Result;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface SysUserService {
    
    /**
     * 用户注册
     */
    Result<SysUser> register(RegisterRequest request);
    
    /**
     * 用户登录
     */
    Result<Map<String, Object>> login(LoginRequest request);
    
    /**
     * 用户登出
     */
    Result<Void> logout(String token);
    
    /**
     * 根据ID查询用户
     */
    SysUser getUserById(Long id);
    
    /**
     * 根据用户名查询用户
     */
    SysUser getUserByUsername(String username);
    
    /**
     * 根据手机号查询用户
     */
    SysUser getUserByPhone(String phone);
    
    /**
     * 根据邮箱查询用户
     */
    SysUser getUserByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 检查手机号是否存在
     */
    boolean isPhoneExists(String phone);
    
    /**
     * 检查邮箱是否存在
     */
    boolean isEmailExists(String email);
    
    /**
     * 创建用户
     */
    Result<SysUser> createUser(SysUser user);
    
    /**
     * 更新用户信息
     */
    Result<SysUser> updateUser(SysUser user);
    
    /**
     * 修改密码
     */
    Result<Void> changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 重置密码
     */
    Result<Void> resetPassword(String account, String newPassword, String verificationCode);
    
    /**
     * 启用/禁用用户
     */
    Result<Void> updateUserStatus(Long userId, String status);
    
    /**
     * 删除用户
     */
    Result<Void> deleteUser(Long userId);
    
    /**
     * 分页查询用户列表
     */
    Result<Map<String, Object>> getUserList(String username, String phone, String email,
                                           String status, String userType,
                                           Integer page, Integer size);
    
    /**
     * 更新最后登录信息
     */
    void updateLastLoginInfo(Long userId, String loginIp);
    
    /**
     * 验证密码
     */
    boolean verifyPassword(String rawPassword, String encodedPassword);

    /**
     * 加密密码
     */
    String encodePassword(String rawPassword);

    /**
     * 简单登录验证（用于SSO）
     * @param username 用户名
     * @param password 密码
     * @return 验证成功返回用户信息，失败返回null
     */
    SysUser simpleLogin(String username, String password);
}
