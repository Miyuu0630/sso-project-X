package org.example.ssoserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.ssoserver.entity.SysUser;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息Mapper接口
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND status = '1'")
    SysUser selectByUsername(@Param("username") String username);
    
    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND status = '1'")
    SysUser selectByPhone(@Param("phone") String phone);
    
    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND status = '1'")
    SysUser selectByEmail(@Param("email") String email);
    
    /**
     * 根据登录账号查询用户（支持用户名、手机号、邮箱）
     * @param account 登录账号
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE (username = #{account} OR phone = #{account} OR email = #{account}) AND status = '1'")
    SysUser selectByAccount(@Param("account") String account);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username} AND id != #{excludeId}")
    int checkUsernameExists(@Param("username") String username, @Param("excludeId") Long excludeId);
    
    /**
     * 检查手机号是否存在
     * @param phone 手机号
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE phone = #{phone} AND id != #{excludeId}")
    int checkPhoneExists(@Param("phone") String phone, @Param("excludeId") Long excludeId);
    
    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE email = #{email} AND id != #{excludeId}")
    int checkEmailExists(@Param("email") String email, @Param("excludeId") Long excludeId);
    
    /**
     * 更新用户登录信息
     * @param userId 用户ID
     * @param loginIp 登录IP
     * @param loginTime 登录时间
     * @return 更新行数
     */
    @Update("UPDATE sys_user SET last_login_ip = #{loginIp}, last_login_time = #{loginTime}, " +
            "login_count = login_count + 1, failed_login_count = 0 WHERE id = #{userId}")
    int updateLoginInfo(@Param("userId") Long userId, 
                       @Param("loginIp") String loginIp, 
                       @Param("loginTime") LocalDateTime loginTime);
    
    /**
     * 更新用户登录失败信息
     * @param userId 用户ID
     * @param failTime 失败时间
     * @return 更新行数
     */
    @Update("UPDATE sys_user SET failed_login_count = failed_login_count + 1, " +
            "last_failed_login_time = #{failTime} WHERE id = #{userId}")
    int updateLoginFailInfo(@Param("userId") Long userId, @Param("failTime") LocalDateTime failTime);
    
    /**
     * 锁定用户账号
     * @param userId 用户ID
     * @param lockTime 锁定时间
     * @return 更新行数
     */
    @Update("UPDATE sys_user SET is_locked = 1, lock_time = #{lockTime} WHERE id = #{userId}")
    int lockUser(@Param("userId") Long userId, @Param("lockTime") LocalDateTime lockTime);
    
    /**
     * 解锁用户账号
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE sys_user SET is_locked = 0, lock_time = NULL, failed_login_count = 0 WHERE id = #{userId}")
    int unlockUser(@Param("userId") Long userId);
    
    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param password 新密码
     * @param updateTime 更新时间
     * @return 更新行数
     */
    @Update("UPDATE sys_user SET password = #{password}, password_update_time = #{updateTime} WHERE id = #{userId}")
    int updatePassword(@Param("userId") Long userId, 
                      @Param("password") String password, 
                      @Param("updateTime") LocalDateTime updateTime);
    
    /**
     * 根据角色ID查询用户列表
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} AND u.status = '1'")
    List<SysUser> selectByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据用户类型查询用户列表
     * @param userType 用户类型
     * @param page 分页参数
     * @return 用户分页列表
     */
    @Select("SELECT * FROM sys_user WHERE user_type = #{userType} AND status = '1' ORDER BY create_time DESC")
    Page<SysUser> selectByUserType(@Param("userType") String userType, Page<SysUser> page);
    
    /**
     * 查询需要解锁的用户（锁定时间超过指定分钟数）
     * @param minutes 锁定分钟数
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE is_locked = 1 AND lock_time < DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE)")
    List<SysUser> selectUsersToUnlock(@Param("minutes") int minutes);
    
    /**
     * 查询密码即将过期的用户（密码更新时间超过指定天数）
     * @param days 天数
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE status = '1' AND " +
            "(password_update_time IS NULL OR password_update_time < DATE_SUB(NOW(), INTERVAL #{days} DAY))")
    List<SysUser> selectUsersWithExpiredPassword(@Param("days") int days);
    
    /**
     * 统计用户数量（按状态）
     * @param status 状态
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE status = #{status}")
    long countByStatus(@Param("status") String status);
    
    /**
     * 统计用户数量（按用户类型）
     * @param userType 用户类型
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE user_type = #{userType} AND status = '1'")
    long countByUserType(@Param("userType") String userType);
}
