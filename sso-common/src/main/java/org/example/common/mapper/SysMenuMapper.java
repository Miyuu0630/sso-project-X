package org.example.common.mapper;

import org.apache.ibatis.annotations.*;
import org.example.common.entity.SysMenu;

import java.util.List;

/**
 * 菜单权限数据访问层
 */
@Mapper
public interface SysMenuMapper {

    /**
     * 插入菜单
     */
    @Insert("INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query_param, " +
            "is_frame, is_cache, menu_type, visible, status, perms, icon, create_time, update_time, " +
            "create_by, update_by, remark) " +
            "VALUES (#{menuName}, #{parentId}, #{orderNum}, #{path}, #{component}, #{queryParam}, " +
            "#{isFrame}, #{isCache}, #{menuType}, #{visible}, #{status}, #{perms}, #{icon}, " +
            "#{createTime}, #{updateTime}, #{createBy}, #{updateBy}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysMenu menu);

    /**
     * 更新菜单
     */
    @Update("UPDATE sys_menu SET menu_name = #{menuName}, parent_id = #{parentId}, " +
            "order_num = #{orderNum}, path = #{path}, component = #{component}, query_param = #{queryParam}, " +
            "is_frame = #{isFrame}, is_cache = #{isCache}, menu_type = #{menuType}, visible = #{visible}, " +
            "status = #{status}, perms = #{perms}, icon = #{icon}, update_time = #{updateTime}, " +
            "update_by = #{updateBy}, remark = #{remark} WHERE id = #{id}")
    int update(SysMenu menu);

    /**
     * 删除菜单
     */
    @Delete("DELETE FROM sys_menu WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据ID查询菜单
     */
    @Select("SELECT * FROM sys_menu WHERE id = #{id}")
    SysMenu selectById(Long id);

    /**
     * 根据用户ID获取菜单权限列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = '1'")
    List<SysMenu> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID获取菜单权限列表
     */
    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id = #{roleId} AND m.status = '1'")
    List<SysMenu> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询所有菜单
     */
    @Select("SELECT * FROM sys_menu WHERE status = '1' ORDER BY order_num")
    List<SysMenu> selectAll();

    /**
     * 根据权限标识查询菜单
     */
    @Select("SELECT * FROM sys_menu WHERE perms = #{perms}")
    SysMenu selectByPerms(@Param("perms") String perms);

    /**
     * 根据父级ID查询子菜单
     */
    @Select("SELECT * FROM sys_menu WHERE parent_id = #{parentId} AND status = '1' ORDER BY order_num")
    List<SysMenu> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 检查权限标识是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_menu WHERE perms = #{perms} AND id != #{id}")
    int checkPermsExists(@Param("perms") String perms, @Param("id") Long id);

    /**
     * 根据用户ID获取权限标识列表
     */
    @Select("SELECT DISTINCT m.perms FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = '1' AND m.perms IS NOT NULL")
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID获取菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = '1' AND m.menu_type IN ('M', 'C')")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据权限标识查询菜单
     */
    @Select("SELECT * FROM sys_menu WHERE perms = #{perms}")
    SysMenu selectByPermissionCode(@Param("perms") String perms);

    /**
     * 统计权限标识数量
     */
    @Select("SELECT COUNT(1) FROM sys_menu WHERE perms = #{perms}")
    int countByPerms(@Param("perms") String perms);

    /**
     * 统计子菜单数量
     */
    @Select("SELECT COUNT(1) FROM sys_menu WHERE parent_id = #{parentId}")
    int countByParentId(@Param("parentId") Long parentId);

    /**
     * 删除角色所有菜单权限
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteRoleAllMenus(@Param("roleId") Long roleId);

    /**
     * 插入角色菜单关联
     */
    @Insert("INSERT INTO sys_role_menu(role_id, menu_id) VALUES(#{roleId}, #{menuId})")
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    /**
     * 删除角色菜单关联
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId} AND menu_id = #{menuId}")
    int deleteRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
}
