package org.example.ssoserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.common.dto.LoginRequest;
import org.example.common.dto.LoginResponse;
import org.example.common.model.UserDTO;
import org.example.common.model.PageResult;
import org.example.ssoserver.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口
 * 提供用户管理的核心业务逻辑
 */
public interface SysUserService {

    // ========================================
    // 用户认证相关
    // ========================================

    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean logout(Long userId);

    /**
     * 验证用户密码
     * @param account 登录账号
     * @param password 密码
     * @return 用户信息，验证失败返回null
     */
    SysUser validateUser(String account, String password);

    /**
     * 验证用户验证码登录
     * @param account 登录账号（手机号或邮箱）
     * @param verificationCode 验证码
     * @param loginType 登录类型
     * @return 用户信息，验证失败返回null
     */
    SysUser validateUserByCode(String account, String verificationCode, String loginType);

    // ========================================
    // 用户查询相关
    // ========================================

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    SysUser getUserById(Long id);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getUserByUsername(String username);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    SysUser getUserByPhone(String phone);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    SysUser getUserByEmail(String email);

    /**
     * 根据登录账号查询用户（支持用户名、手机号、邮箱）
     * @param account 登录账号
     * @return 用户信息
     */
    SysUser getUserByAccount(String account);

    /**
     * 转换为UserDTO
     * @param user 用户实体
     * @return 用户DTO
     */
    UserDTO convertToDTO(SysUser user);

    /**
     * 批量转换为UserDTO
     * @param users 用户实体列表
     * @return 用户DTO列表
     */
    List<UserDTO> convertToDTO(List<SysUser> users);

    // ========================================
    // 用户管理相关
    // ========================================

    /**
     * 创建用户
     * @param user 用户信息
     * @return 是否成功
     */
    boolean createUser(SysUser user);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUser(SysUser user);

    /**
     * 删除用户（逻辑删除）
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);

    /**
     * 启用/禁用用户
     * @param userId 用户ID
     * @param status 状态：1-启用，0-禁用
     * @return 是否成功
     */
    boolean updateUserStatus(Long userId, String status);

    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    // ========================================
    // 用户安全相关
    // ========================================

    /**
     * 更新用户登录信息
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @return 是否成功
     */
    boolean updateLoginInfo(Long userId, String loginIp);

    /**
     * 记录登录失败
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean recordLoginFailure(Long userId);

    /**
     * 锁定用户账号
     * @param userId 用户ID
     * @param reason 锁定原因
     * @return 是否成功
     */
    boolean lockUser(Long userId, String reason);

    /**
     * 解锁用户账号
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean unlockUser(Long userId);

    /**
     * 检查用户是否可以登录
     * @param user 用户信息
     * @return 是否可以登录
     */
    boolean canUserLogin(SysUser user);

    // ========================================
    // 用户验证相关
    // ========================================

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    boolean isUsernameExists(String username, Long excludeId);

    /**
     * 检查手机号是否存在
     * @param phone 手机号
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    boolean isPhoneExists(String phone, Long excludeId);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    boolean isEmailExists(String email, Long excludeId);

    // ========================================
    // 分页查询相关
    // ========================================

    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 页大小
     * @param userType 用户类型
     * @param status 状态
     * @param keyword 关键词
     * @return 分页结果
     */
    PageResult<UserDTO> getUserPage(Integer pageNum, Integer pageSize, String userType, String status, String keyword);

    /**
     * 根据角色查询用户列表
     * @param roleId 角色ID
     * @return 用户列表
     */
    List<UserDTO> getUsersByRole(Long roleId);

    // ========================================
    // 统计相关
    // ========================================

    /**
     * 统计用户数量
     * @param userType 用户类型
     * @param status 状态
     * @return 用户数量
     */
    long countUsers(String userType, String status);

    /**
     * 获取需要解锁的用户列表
     * @param lockMinutes 锁定分钟数
     * @return 用户列表
     */
    List<SysUser> getUsersToUnlock(int lockMinutes);

    /**
     * 获取密码即将过期的用户列表
     * @param days 天数
     * @return 用户列表
     */
    List<SysUser> getUsersWithExpiredPassword(int days);
}
