package org.example.ssoserver.mapper;

import org.apache.ibatis.annotations.*;
import org.example.ssoserver.entity.SysRole;

import java.util.List;

/**
 * 角色数据访问层
 */
@Mapper
public interface SysRoleMapper {

    /**
     * 根据用户ID获取角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<SysRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询所有角色
     */
    @Select("SELECT * FROM sys_role WHERE status = 1 ORDER BY sort_order")
    List<SysRole> selectAll();

    /**
     * 根据ID查询角色
     */
    @Select("SELECT * FROM sys_role WHERE id = #{id}")
    SysRole selectById(@Param("id") Long id);

    /**
     * 根据角色码查询角色
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    SysRole selectByCode(@Param("roleCode") String roleCode);

    /**
     * 插入角色
     */
    @Insert("INSERT INTO sys_role(role_name, role_code, description, sort_order, " +
            "status, create_time, update_time) VALUES(#{roleName}, #{roleCode}, " +
            "#{description}, #{sortOrder}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysRole role);

    /**
     * 更新角色
     */
    @Update("UPDATE sys_role SET role_name = #{roleName}, role_code = #{roleCode}, " +
            "description = #{description}, sort_order = #{sortOrder}, status = #{status}, " +
            "update_time = #{updateTime} WHERE id = #{id}")
    int update(SysRole role);

    /**
     * 删除角色
     */
    @Delete("DELETE FROM sys_role WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 检查角色码是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_role WHERE role_code = #{roleCode} AND id != #{id}")
    int checkRoleCodeExists(@Param("roleCode") String roleCode, @Param("id") Long id);

    /**
     * 为用户分配角色
     */
    @Insert("INSERT INTO sys_user_role(user_id, role_id, create_time, create_by) VALUES(#{userId}, #{roleId}, #{createTime}, #{createBy})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId,
                      @Param("createTime") java.time.LocalDateTime createTime, @Param("createBy") Long createBy);

    /**
     * 删除用户角色关联
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRolesByUserId(@Param("userId") Long userId);

    /**
     * 删除用户特定角色关联
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 为角色分配权限
     */
    @Insert("INSERT INTO sys_role_permission(role_id, permission_id) VALUES(#{roleId}, #{permissionId})")
    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 删除角色权限关联
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteRolePermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色特定权限关联
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int deleteRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 根据角色码查询角色
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 分页查询角色
     */
    @Select("<script>" +
            "SELECT * FROM sys_role WHERE 1=1 " +
            "<if test='roleName != null and roleName != \"\"'> AND role_name LIKE CONCAT('%', #{roleName}, '%') </if>" +
            "<if test='roleCode != null and roleCode != \"\"'> AND role_code LIKE CONCAT('%', #{roleCode}, '%') </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "ORDER BY sort_order LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<SysRole> selectPage(@Param("roleName") String roleName, @Param("roleCode") String roleCode,
                           @Param("status") Integer status, @Param("offset") int offset, @Param("pageSize") Integer pageSize);

    /**
     * 统计角色数量
     */
    @Select("<script>" +
            "SELECT COUNT(1) FROM sys_role WHERE 1=1 " +
            "<if test='roleName != null and roleName != \"\"'> AND role_name LIKE CONCAT('%', #{roleName}, '%') </if>" +
            "<if test='roleCode != null and roleCode != \"\"'> AND role_code LIKE CONCAT('%', #{roleCode}, '%') </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "</script>")
    Long selectCount(@Param("roleName") String roleName, @Param("roleCode") String roleCode, @Param("status") Integer status);

    /**
     * 查询所有启用的角色
     */
    @Select("SELECT * FROM sys_role WHERE status = 1 ORDER BY sort_order")
    List<SysRole> selectAllEnabled();

    /**
     * 删除用户所有角色
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserAllRoles(@Param("userId") Long userId);

    /**
     * 根据用户类型获取默认角色
     */
    @Select("SELECT * FROM sys_role WHERE is_default = 1 AND user_type = #{userType} AND status = 1")
    SysRole selectDefaultRoleByUserType(@Param("userType") Integer userType);
}
