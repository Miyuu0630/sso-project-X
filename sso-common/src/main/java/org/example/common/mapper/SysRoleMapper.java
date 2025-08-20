package org.example.common.mapper;

import org.apache.ibatis.annotations.*;
import org.example.common.entity.SysRole;

import java.util.List;

/**
 * 角色数据访问层
 */
@Mapper
public interface SysRoleMapper {

    /**
     * 插入角色
     */
    @Insert("INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, " +
            "dept_check_strictly, status, create_time, update_time, create_by, update_by, remark) " +
            "VALUES (#{roleName}, #{roleKey}, #{roleSort}, #{dataScope}, #{menuCheckStrictly}, " +
            "#{deptCheckStrictly}, #{status}, #{createTime}, #{updateTime}, #{createBy}, #{updateBy}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysRole role);

    /**
     * 更新角色
     */
    @Update("UPDATE sys_role SET role_name = #{roleName}, role_key = #{roleKey}, " +
            "role_sort = #{roleSort}, data_scope = #{dataScope}, menu_check_strictly = #{menuCheckStrictly}, " +
            "dept_check_strictly = #{deptCheckStrictly}, status = #{status}, update_time = #{updateTime}, " +
            "update_by = #{updateBy}, remark = #{remark} WHERE id = #{id}")
    int update(SysRole role);

    /**
     * 删除角色
     */
    @Delete("DELETE FROM sys_role WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据ID查询角色
     */
    @Select("SELECT * FROM sys_role WHERE id = #{id}")
    SysRole selectById(Long id);

    /**
     * 根据用户ID获取角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = '1'")
    List<SysRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询所有角色
     */
    @Select("SELECT * FROM sys_role WHERE status = '1' ORDER BY role_sort")
    List<SysRole> selectAll();

    /**
     * 根据角色键查询角色
     */
    @Select("SELECT * FROM sys_role WHERE role_key = #{roleKey}")
    SysRole selectByRoleKey(@Param("roleKey") String roleKey);

    /**
     * 检查角色键是否存在
     */
    @Select("SELECT COUNT(1) FROM sys_role WHERE role_key = #{roleKey} AND id != #{id}")
    int checkRoleKeyExists(@Param("roleKey") String roleKey, @Param("id") Long id);

    /**
     * 分页查询角色
     */
    @Select("<script>" +
            "SELECT * FROM sys_role WHERE 1=1 " +
            "<if test='roleName != null and roleName != \"\"'> AND role_name LIKE CONCAT('%', #{roleName}, '%') </if>" +
            "<if test='roleKey != null and roleKey != \"\"'> AND role_key LIKE CONCAT('%', #{roleKey}, '%') </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "ORDER BY role_sort LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<SysRole> selectPage(@Param("roleName") String roleName, @Param("roleKey") String roleKey,
                           @Param("status") String status, @Param("offset") int offset, @Param("pageSize") Integer pageSize);

    /**
     * 统计角色数量
     */
    @Select("<script>" +
            "SELECT COUNT(1) FROM sys_role WHERE 1=1 " +
            "<if test='roleName != null and roleName != \"\"'> AND role_name LIKE CONCAT('%', #{roleName}, '%') </if>" +
            "<if test='roleKey != null and roleKey != \"\"'> AND role_key LIKE CONCAT('%', #{roleKey}, '%') </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "</script>")
    Long selectCount(@Param("roleName") String roleName, @Param("roleKey") String roleKey, @Param("status") String status);

    /**
     * 查询所有启用的角色
     */
    @Select("SELECT * FROM sys_role WHERE status = '1' ORDER BY role_sort")
    List<SysRole> selectAllEnabled();
}
