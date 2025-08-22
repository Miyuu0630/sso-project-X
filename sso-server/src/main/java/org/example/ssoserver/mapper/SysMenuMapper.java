package org.example.ssoserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.ssoserver.entity.SysMenu;

import java.util.List;

/**
 * 菜单权限Mapper接口
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    
    /**
     * 根据用户ID查询菜单权限
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = '1' " +
            "ORDER BY m.parent_id ASC, m.order_num ASC")
    List<SysMenu> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询菜单权限
     * @param roleId 角色ID
     * @return 菜单列表
     */
    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id = #{roleId} AND m.status = '1' " +
            "ORDER BY m.parent_id ASC, m.order_num ASC")
    List<SysMenu> selectByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色ID查询菜单ID列表
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据用户ID查询权限标识列表
     * @param userId 用户ID
     * @return 权限标识列表
     */
    @Select("SELECT DISTINCT m.perms FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = '1' AND m.perms IS NOT NULL AND m.perms != ''")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有菜单
     * @return 菜单列表
     */
    @Select("SELECT * FROM sys_menu WHERE status = '1' ORDER BY parent_id ASC, order_num ASC")
    List<SysMenu> selectAllEnabled();
    
    /**
     * 根据父菜单ID查询子菜单
     * @param parentId 父菜单ID
     * @return 子菜单列表
     */
    @Select("SELECT * FROM sys_menu WHERE parent_id = #{parentId} AND status = '1' ORDER BY order_num ASC")
    List<SysMenu> selectByParentId(@Param("parentId") Long parentId);
    
    /**
     * 检查是否有子菜单
     * @param menuId 菜单ID
     * @return 子菜单数量
     */
    @Select("SELECT COUNT(*) FROM sys_menu WHERE parent_id = #{menuId}")
    int countChildMenus(@Param("menuId") Long menuId);
    
    /**
     * 根据菜单类型查询菜单
     * @param menuType 菜单类型
     * @return 菜单列表
     */
    @Select("SELECT * FROM sys_menu WHERE menu_type = #{menuType} AND status = '1' ORDER BY order_num ASC")
    List<SysMenu> selectByMenuType(@Param("menuType") String menuType);
    
    /**
     * 查询用户可访问的路由
     * @param userId 用户ID
     * @return 路由列表
     */
    @Select("SELECT DISTINCT m.path FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = '1' AND m.menu_type IN ('C', 'M') " +
            "AND m.path IS NOT NULL AND m.path != ''")
    List<String> selectRoutesByUserId(@Param("userId") Long userId);
}
