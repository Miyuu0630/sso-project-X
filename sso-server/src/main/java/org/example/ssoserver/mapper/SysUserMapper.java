package org.example.ssoserver.mapper;

import org.apache.ibatis.annotations.*;
import org.example.ssoserver.entity.SysUser;
import java.util.List;

/**
 * 用户数据访问层
 */
@Mapper
public interface SysUserMapper {
    
    /**
     * 根据ID查询用户
     */
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    SysUser selectById(Long id);
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser selectByUsername(String username);
    
    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM sys_user WHERE phone = #{phone}")
    SysUser selectByPhone(String phone);
    
    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email}")
    SysUser selectByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_user WHERE username = #{username}")
    int countByUsername(String username);
    
    /**
     * 检查手机号是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_user WHERE phone = #{phone}")
    int countByPhone(String phone);
    
    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_user WHERE email = #{email}")
    int countByEmail(String email);
    
    /**
     * 插入用户
     */
    @Insert("INSERT INTO sys_user (username, password, phone, email, real_name, user_type, status, " +
            "create_time, update_time, create_by) VALUES " +
            "(#{username}, #{password}, #{phone}, #{email}, #{realName}, #{userType}, #{status}, " +
            "#{createTime}, #{updateTime}, #{createBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);
    
    /**
     * 更新用户
     */
    @Update("UPDATE sys_user SET username = #{username}, phone = #{phone}, email = #{email}, " +
            "real_name = #{realName}, avatar = #{avatar}, gender = #{gender}, birthday = #{birthday}, " +
            "status = #{status}, user_type = #{userType}, update_time = #{updateTime}, " +
            "update_by = #{updateBy}, remark = #{remark} WHERE id = #{id}")
    int update(SysUser user);
    
    /**
     * 更新密码
     */
    @Update("UPDATE sys_user SET password = #{password}, update_time = #{updateTime}, " +
            "update_by = #{updateBy} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password, 
                      @Param("updateTime") java.time.LocalDateTime updateTime, 
                      @Param("updateBy") Long updateBy);
    
    /**
     * 更新用户状态
     */
    @Update("UPDATE sys_user SET status = #{status}, update_time = #{updateTime}, " +
            "update_by = #{updateBy} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, 
                    @Param("updateTime") java.time.LocalDateTime updateTime, 
                    @Param("updateBy") Long updateBy);
    
    /**
     * 更新最后登录信息
     */
    @Update("UPDATE sys_user SET last_login_time = #{lastLoginTime}, " +
            "last_login_ip = #{lastLoginIp} WHERE id = #{id}")
    int updateLastLogin(@Param("id") Long id, 
                       @Param("lastLoginTime") java.time.LocalDateTime lastLoginTime,
                       @Param("lastLoginIp") String lastLoginIp);
    
    /**
     * 删除用户
     */
    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * 分页查询用户列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_user WHERE 1=1 " +
            "<if test='username != null and username != \"\"'>" +
            "AND username LIKE CONCAT('%', #{username}, '%') " +
            "</if>" +
            "<if test='phone != null and phone != \"\"'>" +
            "AND phone LIKE CONCAT('%', #{phone}, '%') " +
            "</if>" +
            "<if test='email != null and email != \"\"'>" +
            "AND email LIKE CONCAT('%', #{email}, '%') " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "<if test='userType != null'>" +
            "AND user_type = #{userType} " +
            "</if>" +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<SysUser> selectPage(@Param("username") String username,
                            @Param("phone") String phone,
                            @Param("email") String email,
                            @Param("status") Integer status,
                            @Param("userType") Integer userType,
                            @Param("offset") Integer offset,
                            @Param("limit") Integer limit);
    
    /**
     * 查询用户总数
     */
    @Select("<script>" +
            "SELECT COUNT(1) FROM sys_user WHERE 1=1 " +
            "<if test='username != null and username != \"\"'>" +
            "AND username LIKE CONCAT('%', #{username}, '%') " +
            "</if>" +
            "<if test='phone != null and phone != \"\"'>" +
            "AND phone LIKE CONCAT('%', #{phone}, '%') " +
            "</if>" +
            "<if test='email != null and email != \"\"'>" +
            "AND email LIKE CONCAT('%', #{email}, '%') " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "<if test='userType != null'>" +
            "AND user_type = #{userType} " +
            "</if>" +
            "</script>")
    int selectCount(@Param("username") String username,
                   @Param("phone") String phone,
                   @Param("email") String email,
                   @Param("status") Integer status,
                   @Param("userType") Integer userType);
}
