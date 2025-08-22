package org.example.ssoserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.example.ssoserver.entity.SysRoleMenu;

import java.util.List;

/**
 * 角色菜单关联Mapper接口
 */
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
    
    /**
     * 根据角色ID删除角色菜单关联
     * @param roleId 角色ID
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据菜单ID删除角色菜单关联
     * @param menuId 菜单ID
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_role_menu WHERE menu_id = #{menuId}")
    int deleteByMenuId(@Param("menuId") Long menuId);
    
    /**
     * 根据角色ID查询菜单ID列表
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据菜单ID查询角色ID列表
     * @param menuId 菜单ID
     * @return 角色ID列表
     */
    @Select("SELECT role_id FROM sys_role_menu WHERE menu_id = #{menuId}")
    List<Long> selectRoleIdsByMenuId(@Param("menuId") Long menuId);
    
    /**
     * 检查角色是否有指定菜单权限
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM sys_role_menu WHERE role_id = #{roleId} AND menu_id = #{menuId}")
    int checkRoleHasMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
    
    /**
     * 批量插入角色菜单关联
     * @param roleMenus 角色菜单关联列表
     * @return 插入行数
     */
    int batchInsert(@Param("roleMenus") List<SysRoleMenu> roleMenus);
    
    /**
     * 批量删除角色菜单关联
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 删除行数
     */
    int batchDeleteByRoleIdAndMenuIds(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}
