package org.example.ssoserver.mapper;

import org.apache.ibatis.annotations.*;
import org.example.ssoserver.entity.SysPermission;

import java.util.List;

/**
 * 权限数据访问层
 */
@Mapper
public interface SysPermissionMapper {

    /**
     * 根据用户ID获取权限列表
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1")
    List<SysPermission> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID获取权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1")
    List<SysPermission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询所有权限
     */
    @Select("SELECT * FROM sys_permission WHERE status = 1 ORDER BY sort_order")
    List<SysPermission> selectAll();

    /**
     * 根据ID查询权限
     */
    @Select("SELECT * FROM sys_permission WHERE id = #{id}")
    SysPermission selectById(@Param("id") Long id);

    /**
     * 根据权限码查询权限
     */
    @Select("SELECT * FROM sys_permission WHERE permission_code = #{permissionCode}")
    SysPermission selectByCode(@Param("permissionCode") String permissionCode);

    /**
     * 插入权限
     */
    @Insert("INSERT INTO sys_permission(permission_name, permission_code, permission_type, " +
            "parent_id, path, component, icon, sort_order, status, description, " +
            "create_time, update_time) VALUES(#{permissionName}, #{permissionCode}, " +
            "#{permissionType}, #{parentId}, #{path}, #{component}, #{icon}, " +
            "#{sortOrder}, #{status}, #{description}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysPermission permission);

    /**
     * 更新权限
     */
    @Update("UPDATE sys_permission SET permission_name = #{permissionName}, " +
            "permission_code = #{permissionCode}, permission_type = #{permissionType}, " +
            "parent_id = #{parentId}, path = #{path}, component = #{component}, " +
            "icon = #{icon}, sort_order = #{sortOrder}, status = #{status}, " +
            "description = #{description}, update_time = #{updateTime} WHERE id = #{id}")
    int update(SysPermission permission);

    /**
     * 删除权限
     */
    @Delete("DELETE FROM sys_permission WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 根据父级ID查询子权限
     */
    @Select("SELECT * FROM sys_permission WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort_order")
    List<SysPermission> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 检查权限码是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_permission WHERE permission_code = #{permissionCode} AND id != #{id}")
    int checkPermissionCodeExists(@Param("permissionCode") String permissionCode, @Param("id") Long id);

    /**
     * 根据用户ID获取权限码列表
     */
    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID获取菜单权限列表
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1 AND p.permission_type IN (1, 2)")
    List<SysPermission> selectMenuPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 根据权限码查询权限
     */
    @Select("SELECT * FROM sys_permission WHERE permission_code = #{permissionCode}")
    SysPermission selectByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 统计权限码数量
     */
    @Select("SELECT COUNT(1) FROM sys_permission WHERE permission_code = #{permissionCode}")
    int countByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 统计子权限数量
     */
    @Select("SELECT COUNT(1) FROM sys_permission WHERE parent_id = #{parentId}")
    int countByParentId(@Param("parentId") Long parentId);

    /**
     * 删除角色所有权限
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteRoleAllPermissions(@Param("roleId") Long roleId);

    /**
     * 插入角色权限关联
     */
    @Insert("INSERT INTO sys_role_permission(role_id, permission_id, create_time, create_by) " +
            "VALUES(#{roleId}, #{permissionId}, #{createTime}, #{createBy})")
    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId,
                           @Param("createTime") java.time.LocalDateTime createTime, @Param("createBy") Long createBy);

    /**
     * 删除角色权限关联
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int deleteRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}
