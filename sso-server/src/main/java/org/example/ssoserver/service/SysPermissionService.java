package org.example.ssoserver.service;

import org.example.ssoserver.common.Result;
import org.example.ssoserver.entity.SysPermission;

import java.util.List;
import java.util.Map;

/**
 * 权限管理服务接口
 */
public interface SysPermissionService {
    
    /**
     * 获取用户权限列表
     */
    List<String> getUserPermissions(Long userId);
    
    /**
     * 获取用户菜单权限树
     */
    Result<List<SysPermission>> getUserMenuTree(Long userId);
    
    /**
     * 获取所有权限树
     */
    Result<List<SysPermission>> getAllPermissionTree();
    
    /**
     * 根据ID查询权限
     */
    SysPermission getPermissionById(Long id);
    
    /**
     * 根据权限编码查询权限
     */
    SysPermission getPermissionByCode(String permissionCode);
    
    /**
     * 创建权限
     */
    Result<SysPermission> createPermission(SysPermission permission);
    
    /**
     * 更新权限
     */
    Result<SysPermission> updatePermission(SysPermission permission);
    
    /**
     * 删除权限
     */
    Result<Void> deletePermission(Long id);
    
    /**
     * 检查用户是否有指定权限
     */
    boolean hasPermission(Long userId, String permissionCode);
    
    /**
     * 检查用户是否有任意一个权限
     */
    boolean hasAnyPermission(Long userId, String... permissionCodes);
    
    /**
     * 检查用户是否有所有权限
     */
    boolean hasAllPermissions(Long userId, String... permissionCodes);
    
    /**
     * 获取角色权限列表
     */
    List<SysPermission> getRolePermissions(Long roleId);
    
    /**
     * 为角色分配权限
     */
    Result<Void> assignPermissionsToRole(Long roleId, List<Long> permissionIds);
    
    /**
     * 移除角色权限
     */
    Result<Void> removePermissionsFromRole(Long roleId, List<Long> permissionIds);
}
