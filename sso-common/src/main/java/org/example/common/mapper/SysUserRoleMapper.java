package org.example.common.mapper;

import org.apache.ibatis.annotations.*;
import org.example.common.entity.SysUserRole;
import java.util.List;

/**
 * 用户角色关联数据访问层
 */
@Mapper
public interface SysUserRoleMapper {
    
    /**
     * 插入用户角色关联
     */
    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insert(SysUserRole userRole);
    
    /**
     * 批量插入用户角色关联
     */
    @Insert("<script>" +
            "INSERT INTO sys_user_role (user_id, role_id) VALUES " +
            "<foreach collection='userRoles' item='item' separator=','>" +
            "(#{item.userId}, #{item.roleId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("userRoles") List<SysUserRole> userRoles);
    
    /**
     * 根据用户ID查询角色关联
     */
    @Select("SELECT * FROM sys_user_role WHERE user_id = #{userId}")
    List<SysUserRole> selectByUserId(Long userId);
    
    /**
     * 根据角色ID查询用户关联
     */
    @Select("SELECT * FROM sys_user_role WHERE role_id = #{roleId}")
    List<SysUserRole> selectByRoleId(Long roleId);
    
    /**
     * 根据用户ID和角色ID查询关联
     */
    @Select("SELECT * FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    SysUserRole selectByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 删除用户角色关联
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int delete(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 根据用户ID删除所有角色关联
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
    
    /**
     * 根据角色ID删除所有用户关联
     */
    @Delete("DELETE FROM sys_user_role WHERE role_id = #{roleId}")
    int deleteByRoleId(Long roleId);
    
    /**
     * 检查用户是否拥有指定角色
     */
    @Select("SELECT COUNT(1) FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int checkUserHasRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 查询用户的角色ID列表
     */
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(Long userId);
    
    /**
     * 查询角色的用户ID列表
     */
    @Select("SELECT user_id FROM sys_user_role WHERE role_id = #{roleId}")
    List<Long> selectUserIdsByRoleId(Long roleId);
}
