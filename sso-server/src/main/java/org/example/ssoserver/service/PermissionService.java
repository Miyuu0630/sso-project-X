package org.example.ssoserver.service;

import org.example.common.model.RoleDTO;
import org.example.common.model.MenuDTO;
import org.example.common.model.UserDTO;
import org.example.ssoserver.entity.SysRole;
import org.example.ssoserver.entity.SysMenu;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 * 提供用户权限管理的核心业务逻辑
 */
public interface PermissionService {
    
    // ========================================
    // 用户权限查询
    // ========================================
    
    /**
     * 获取用户的所有角色
     * @param userId 用户ID
     * @return 角色列表
     */
    List<String> getUserRoles(Long userId);
    
    /**
     * 获取用户的所有权限
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> getUserPermissions(Long userId);
    
    /**
     * 获取用户的菜单权限
     * @param userId 用户ID
     * @return 菜单列表（树形结构）
     */
    List<MenuDTO> getUserMenus(Long userId);
    
    /**
     * 获取用户的按钮权限
     * @param userId 用户ID
     * @return 按钮权限列表
     */
    List<String> getUserButtonPermissions(Long userId);
    
    /**
     * 检查用户是否有指定角色
     * @param userId 用户ID
     * @param roleKey 角色标识
     * @return 是否有角色
     */
    boolean hasRole(Long userId, String roleKey);
    
    /**
     * 检查用户是否有指定权限
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permission);
    
    /**
     * 检查用户是否有任意一个角色
     * @param userId 用户ID
     * @param roleKeys 角色标识列表
     * @return 是否有任意角色
     */
    boolean hasAnyRole(Long userId, String... roleKeys);
    
    /**
     * 检查用户是否有任意一个权限
     * @param userId 用户ID
     * @param permissions 权限标识列表
     * @return 是否有任意权限
     */
    boolean hasAnyPermission(Long userId, String... permissions);
    
    // ========================================
    // 角色管理
    // ========================================
    
    /**
     * 创建角色
     * @param role 角色信息
     * @return 是否成功
     */
    boolean createRole(SysRole role);
    
    /**
     * 更新角色
     * @param role 角色信息
     * @return 是否成功
     */
    boolean updateRole(SysRole role);
    
    /**
     * 删除角色
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long roleId);
    
    /**
     * 根据ID获取角色
     * @param roleId 角色ID
     * @return 角色信息
     */
    SysRole getRoleById(Long roleId);
    
    /**
     * 根据角色标识获取角色
     * @param roleKey 角色标识
     * @return 角色信息
     */
    SysRole getRoleByKey(String roleKey);
    
    /**
     * 获取所有角色
     * @return 角色列表
     */
    List<RoleDTO> getAllRoles();
    
    /**
     * 检查角色标识是否存在
     * @param roleKey 角色标识
     * @param excludeId 排除的角色ID
     * @return 是否存在
     */
    boolean isRoleKeyExists(String roleKey, Long excludeId);
    
    // ========================================
    // 菜单权限管理
    // ========================================
    
    /**
     * 创建菜单
     * @param menu 菜单信息
     * @return 是否成功
     */
    boolean createMenu(SysMenu menu);
    
    /**
     * 更新菜单
     * @param menu 菜单信息
     * @return 是否成功
     */
    boolean updateMenu(SysMenu menu);
    
    /**
     * 删除菜单
     * @param menuId 菜单ID
     * @return 是否成功
     */
    boolean deleteMenu(Long menuId);
    
    /**
     * 根据ID获取菜单
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    SysMenu getMenuById(Long menuId);
    
    /**
     * 获取所有菜单（树形结构）
     * @return 菜单树
     */
    List<MenuDTO> getAllMenusTree();
    
    /**
     * 获取角色的菜单权限
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);
    
    // ========================================
    // 用户角色关联管理
    // ========================================
    
    /**
     * 为用户分配角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean assignRolesToUser(Long userId, List<Long> roleIds);
    
    /**
     * 移除用户的角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean removeRolesFromUser(Long userId, List<Long> roleIds);
    
    /**
     * 获取用户的角色信息
     * @param userId 用户ID
     * @return 角色列表
     */
    List<RoleDTO> getUserRoleDetails(Long userId);
    
    /**
     * 获取角色下的用户列表
     * @param roleId 角色ID
     * @return 用户列表
     */
    List<UserDTO> getRoleUsers(Long roleId);
    
    // ========================================
    // 角色菜单关联管理
    // ========================================
    
    /**
     * 为角色分配菜单权限
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean assignMenusToRole(Long roleId, List<Long> menuIds);
    
    /**
     * 移除角色的菜单权限
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean removeMenusFromRole(Long roleId, List<Long> menuIds);
    
    // ========================================
    // 权限缓存管理
    // ========================================
    
    /**
     * 刷新用户权限缓存
     * @param userId 用户ID
     */
    void refreshUserPermissionCache(Long userId);
    
    /**
     * 刷新角色权限缓存
     * @param roleId 角色ID
     */
    void refreshRolePermissionCache(Long roleId);
    
    /**
     * 清除所有权限缓存
     */
    void clearAllPermissionCache();
    
    // ========================================
    // 权限验证辅助方法
    // ========================================
    
    /**
     * 构建菜单树
     * @param menus 菜单列表
     * @param parentId 父菜单ID
     * @return 菜单树
     */
    List<MenuDTO> buildMenuTree(List<MenuDTO> menus, Long parentId);
    
    /**
     * 获取菜单的所有子菜单ID
     * @param menuId 菜单ID
     * @return 子菜单ID列表
     */
    List<Long> getChildMenuIds(Long menuId);
    
    /**
     * 检查菜单是否有子菜单
     * @param menuId 菜单ID
     * @return 是否有子菜单
     */
    boolean hasChildMenus(Long menuId);
    
    /**
     * 获取用户可访问的路由
     * @param userId 用户ID
     * @return 路由列表
     */
    List<String> getUserRoutes(Long userId);
    
    /**
     * 检查用户是否可以访问指定路由
     * @param userId 用户ID
     * @param route 路由路径
     * @return 是否可以访问
     */
    boolean canAccessRoute(Long userId, String route);
    
    // ========================================
    // 数据权限相关
    // ========================================
    
    /**
     * 获取用户的数据权限范围
     * @param userId 用户ID
     * @return 数据权限范围
     */
    String getUserDataScope(Long userId);
    
    /**
     * 检查用户是否可以访问指定数据
     * @param userId 用户ID
     * @param dataOwnerId 数据所有者ID
     * @param deptId 部门ID
     * @return 是否可以访问
     */
    boolean canAccessData(Long userId, Long dataOwnerId, Long deptId);
}
