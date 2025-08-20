package org.example.ssoserver.service;

import org.example.common.entity.SysMenu;
import org.example.common.result.Result;

import java.util.List;

/**
 * 菜单权限管理服务接口
 */
public interface SysMenuService {
    
    /**
     * 获取用户权限列表
     */
    List<String> getUserPermissions(Long userId);
    
    /**
     * 获取用户菜单权限树
     */
    Result<List<SysMenu>> getUserMenuTree(Long userId);

    /**
     * 获取所有菜单权限树
     */
    Result<List<SysMenu>> getAllMenuTree();

    /**
     * 根据ID查询菜单
     */
    SysMenu getMenuById(Long id);

    /**
     * 根据权限标识查询菜单
     */
    SysMenu getMenuByPerms(String perms);

    /**
     * 创建菜单
     */
    Result<SysMenu> createMenu(SysMenu menu);

    /**
     * 更新菜单
     */
    Result<SysMenu> updateMenu(SysMenu menu);
    
    /**
     * 删除菜单
     */
    Result<Void> deleteMenu(Long id);

    /**
     * 检查用户是否有指定权限
     */
    boolean hasPermission(Long userId, String perms);

    /**
     * 检查用户是否有任意一个权限
     */
    boolean hasAnyPermission(Long userId, String... perms);

    /**
     * 检查用户是否有所有权限
     */
    boolean hasAllPermissions(Long userId, String... perms);

    /**
     * 获取角色菜单列表
     */
    List<SysMenu> getRoleMenus(Long roleId);

    /**
     * 为角色分配菜单权限
     */
    Result<Void> assignMenusToRole(Long roleId, List<Long> menuIds);

    /**
     * 移除角色菜单权限
     */
    Result<Void> removeMenusFromRole(Long roleId, List<Long> menuIds);
}
