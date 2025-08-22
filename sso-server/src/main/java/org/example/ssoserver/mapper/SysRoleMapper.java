package org.example.ssoserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.ssoserver.entity.SysRole;

import java.util.List;

/**
 * 角色信息Mapper接口
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {
    
    /**
     * 根据角色标识查询角色
     * @param roleKey 角色标识
     * @return 角色信息
     */
    @Select("SELECT * FROM sys_role WHERE role_key = #{roleKey} AND status = '1'")
    SysRole selectByRoleKey(@Param("roleKey") String roleKey);
    
    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = '1'")
    List<SysRole> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询角色标识列表
     * @param userId 用户ID
     * @return 角色标识列表
     */
    @Select("SELECT r.role_key FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = '1'")
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);
    
    /**
     * 检查角色标识是否存在
     * @param roleKey 角色标识
     * @param excludeId 排除的角色ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_role WHERE role_key = #{roleKey} AND id != #{excludeId}")
    int checkRoleKeyExists(@Param("roleKey") String roleKey, @Param("excludeId") Long excludeId);
    
    /**
     * 查询所有启用的角色
     * @return 角色列表
     */
    @Select("SELECT * FROM sys_role WHERE status = '1' ORDER BY role_sort ASC, create_time ASC")
    List<SysRole> selectAllEnabled();
    
    /**
     * 根据状态统计角色数量
     * @param status 状态
     * @return 角色数量
     */
    @Select("SELECT COUNT(*) FROM sys_role WHERE status = #{status}")
    long countByStatus(@Param("status") String status);
}
