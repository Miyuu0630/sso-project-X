package org.example.common.mapper;

import org.apache.ibatis.annotations.*;
import org.example.common.entity.SysRoleMenu;
import java.util.List;

/**
 * 角色菜单关联数据访问层
 */
@Mapper
public interface SysRoleMenuMapper {
    
    /**
     * 插入角色菜单关联
     */
    @Insert("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (#{roleId}, #{menuId})")
    int insert(SysRoleMenu roleMenu);
    
    /**
     * 批量插入角色菜单关联
     */
    @Insert("<script>" +
            "INSERT INTO sys_role_menu (role_id, menu_id) VALUES " +
            "<foreach collection='roleMenus' item='item' separator=','>" +
            "(#{item.roleId}, #{item.menuId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("roleMenus") List<SysRoleMenu> roleMenus);
    
    /**
     * 根据角色ID查询菜单关联
     */
    @Select("SELECT * FROM sys_role_menu WHERE role_id = #{roleId}")
    List<SysRoleMenu> selectByRoleId(Long roleId);
    
    /**
     * 根据菜单ID查询角色关联
     */
    @Select("SELECT * FROM sys_role_menu WHERE menu_id = #{menuId}")
    List<SysRoleMenu> selectByMenuId(Long menuId);
    
    /**
     * 根据角色ID和菜单ID查询关联
     */
    @Select("SELECT * FROM sys_role_menu WHERE role_id = #{roleId} AND menu_id = #{menuId}")
    SysRoleMenu selectByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
    
    /**
     * 删除角色菜单关联
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId} AND menu_id = #{menuId}")
    int delete(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
    
    /**
     * 根据角色ID删除所有菜单关联
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(Long roleId);
    
    /**
     * 根据菜单ID删除所有角色关联
     */
    @Delete("DELETE FROM sys_role_menu WHERE menu_id = #{menuId}")
    int deleteByMenuId(Long menuId);
    
    /**
     * 检查角色是否拥有指定菜单权限
     */
    @Select("SELECT COUNT(1) FROM sys_role_menu WHERE role_id = #{roleId} AND menu_id = #{menuId}")
    int checkRoleHasMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
    
    /**
     * 查询角色的菜单ID列表
     */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(Long roleId);
    
    /**
     * 查询菜单的角色ID列表
     */
    @Select("SELECT role_id FROM sys_role_menu WHERE menu_id = #{menuId}")
    List<Long> selectRoleIdsByMenuId(Long menuId);
    
    /**
     * 根据用户ID查询菜单权限
     */
    @Select("SELECT DISTINCT rm.menu_id FROM sys_role_menu rm " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Long> selectMenuIdsByUserId(Long userId);
}
