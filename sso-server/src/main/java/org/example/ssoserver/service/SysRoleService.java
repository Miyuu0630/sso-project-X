package org.example.ssoserver.service;


import org.example.common.entity.SysRole;
import org.example.common.result.Result;

import java.util.List;
import java.util.Map;

/**
 * 角色管理服务接口
 */
public interface SysRoleService {
    
    /**
     * 获取用户角色列表
     */
    List<SysRole> getUserRoles(Long userId);
    
    /**
     * 根据ID查询角色
     */
    SysRole getRoleById(Long id);
    
    /**
     * 根据角色编码查询角色
     */
    SysRole getRoleByCode(String roleCode);
    
    /**
     * 创建角色
     */
    Result<SysRole> createRole(SysRole role);
    
    /**
     * 更新角色
     */
    Result<SysRole> updateRole(SysRole role);
    
    /**
     * 删除角色
     */
    Result<Void> deleteRole(Long id);
    
    /**
     * 分页查询角色列表
     */
    Result<Map<String, Object>> getRoleList(String roleName, String roleCode,
                                           String status, Integer page, Integer size);
    
    /**
     * 获取所有启用的角色
     */
    List<SysRole> getAllEnabledRoles();
    
    /**
     * 为用户分配角色
     */
    Result<Void> assignRolesToUser(Long userId, List<Long> roleIds);
    
    /**
     * 移除用户角色
     */
    Result<Void> removeRolesFromUser(Long userId, List<Long> roleIds);
    
    /**
     * 检查用户是否有指定角色
     */
    boolean hasRole(Long userId, String roleCode);
    
    /**
     * 检查用户是否有任意一个角色
     */
    boolean hasAnyRole(Long userId, String... roleCodes);
    
    /**
     * 检查用户是否有所有角色
     */
    boolean hasAllRoles(Long userId, String... roleCodes);
    
    /**
     * 根据用户类型获取默认角色
     */
    SysRole getDefaultRoleByUserType(String userType);
}
