package org.example.ssoserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.example.ssoserver.entity.SysUserRole;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    
    /**
     * 根据用户ID删除用户角色关联
     * @param userId 用户ID
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID删除用户角色关联
     * @param roleId 角色ID
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_user_role WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据用户ID查询角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询用户ID列表
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM sys_user_role WHERE role_id = #{roleId}")
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 检查用户是否有指定角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int checkUserHasRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 批量插入用户角色关联
     * @param userRoles 用户角色关联列表
     * @return 插入行数
     */
    int batchInsert(@Param("userRoles") List<SysUserRole> userRoles);
    
    /**
     * 批量删除用户角色关联
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 删除行数
     */
    int batchDeleteByUserIdAndRoleIds(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
