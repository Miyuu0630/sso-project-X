package org.example.ssoserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.model.RoleDTO;
import org.example.common.model.MenuDTO;
import org.example.common.model.UserDTO;
import org.example.ssoserver.entity.SysRole;
import org.example.ssoserver.entity.SysMenu;
import org.example.ssoserver.entity.SysUserRole;
import org.example.ssoserver.entity.SysRoleMenu;
import org.example.ssoserver.mapper.SysRoleMapper;
import org.example.ssoserver.mapper.SysMenuMapper;
import org.example.ssoserver.mapper.SysUserRoleMapper;
import org.example.ssoserver.mapper.SysRoleMenuMapper;
import org.example.ssoserver.service.PermissionService;
import org.example.ssoserver.service.SysUserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 缓存前缀
    private static final String USER_ROLES_CACHE_PREFIX = "user:roles:";
    private static final String USER_PERMISSIONS_CACHE_PREFIX = "user:permissions:";
    private static final String ROLE_PERMISSIONS_CACHE_PREFIX = "role:permissions:";
    
    // 缓存过期时间（小时）
    private static final int CACHE_EXPIRE_HOURS = 2;
    
    // ========================================
    // 用户权限查询
    // ========================================
    
    @Override
    public List<String> getUserRoles(Long userId) {
        try {
            // 先从缓存获取
            String cacheKey = USER_ROLES_CACHE_PREFIX + userId;
            @SuppressWarnings("unchecked")
            List<String> cachedRoles = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRoles != null) {
                return cachedRoles;
            }
            
            // 从数据库查询
            List<String> roles = roleMapper.selectRoleKeysByUserId(userId);
            
            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, roles, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            return roles;
        } catch (Exception e) {
            log.error("获取用户角色失败: userId={}", userId, e);
            return List.of();
        }
    }
    
    @Override
    public List<String> getUserPermissions(Long userId) {
        try {
            // 先从缓存获取
            String cacheKey = USER_PERMISSIONS_CACHE_PREFIX + userId;
            @SuppressWarnings("unchecked")
            List<String> cachedPermissions = (List<String>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedPermissions != null) {
                return cachedPermissions;
            }
            
            // 从数据库查询
            List<String> permissions = menuMapper.selectPermissionsByUserId(userId);
            
            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            return permissions;
        } catch (Exception e) {
            log.error("获取用户权限失败: userId={}", userId, e);
            return List.of();
        }
    }
    
    @Override
    public List<MenuDTO> getUserMenus(Long userId) {
        try {
            List<SysMenu> menus = menuMapper.selectByUserId(userId);
            return buildMenuTree(convertToMenuDTO(menus), 0L);
        } catch (Exception e) {
            log.error("获取用户菜单失败: userId={}", userId, e);
            return List.of();
        }
    }
    
    @Override
    public List<String> getUserButtonPermissions(Long userId) {
        try {
            List<SysMenu> menus = menuMapper.selectByUserId(userId);
            return menus.stream()
                    .filter(menu -> "F".equals(menu.getMenuType()) && menu.getPerms() != null)
                    .map(SysMenu::getPerms)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户按钮权限失败: userId={}", userId, e);
            return List.of();
        }
    }
    
    @Override
    public boolean hasRole(Long userId, String roleKey) {
        List<String> roles = getUserRoles(userId);
        return roles.contains(roleKey);
    }
    
    @Override
    public boolean hasPermission(Long userId, String permission) {
        List<String> permissions = getUserPermissions(userId);
        return permissions.contains(permission);
    }
    
    @Override
    public boolean hasAnyRole(Long userId, String... roleKeys) {
        List<String> userRoles = getUserRoles(userId);
        for (String roleKey : roleKeys) {
            if (userRoles.contains(roleKey)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean hasAnyPermission(Long userId, String... permissions) {
        List<String> userPermissions = getUserPermissions(userId);
        for (String permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }
    
    // ========================================
    // 角色管理
    // ========================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(SysRole role) {
        try {
            int result = roleMapper.insert(role);
            return result > 0;
        } catch (Exception e) {
            log.error("创建角色失败: roleKey={}", role.getRoleKey(), e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole role) {
        try {
            int result = roleMapper.updateById(role);
            if (result > 0) {
                // 清除相关缓存
                refreshRolePermissionCache(role.getId());
            }
            return result > 0;
        } catch (Exception e) {
            log.error("更新角色失败: id={}", role.getId(), e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        try {
            // 删除角色菜单关联
            roleMenuMapper.deleteByRoleId(roleId);
            // 删除用户角色关联
            userRoleMapper.deleteByRoleId(roleId);
            // 删除角色
            int result = roleMapper.deleteById(roleId);
            
            if (result > 0) {
                // 清除相关缓存
                refreshRolePermissionCache(roleId);
            }
            return result > 0;
        } catch (Exception e) {
            log.error("删除角色失败: roleId={}", roleId, e);
            return false;
        }
    }
    
    @Override
    public SysRole getRoleById(Long roleId) {
        try {
            return roleMapper.selectById(roleId);
        } catch (Exception e) {
            log.error("根据ID获取角色失败: roleId={}", roleId, e);
            return null;
        }
    }
    
    @Override
    public SysRole getRoleByKey(String roleKey) {
        try {
            return roleMapper.selectByRoleKey(roleKey);
        } catch (Exception e) {
            log.error("根据角色标识获取角色失败: roleKey={}", roleKey, e);
            return null;
        }
    }
    
    @Override
    public List<RoleDTO> getAllRoles() {
        try {
            List<SysRole> roles = roleMapper.selectAllEnabled();
            return convertToRoleDTO(roles);
        } catch (Exception e) {
            log.error("获取所有角色失败", e);
            return List.of();
        }
    }
    
    @Override
    public boolean isRoleKeyExists(String roleKey, Long excludeId) {
        try {
            Long id = excludeId != null ? excludeId : 0L;
            int count = roleMapper.checkRoleKeyExists(roleKey, id);
            return count > 0;
        } catch (Exception e) {
            log.error("检查角色标识是否存在失败: roleKey={}", roleKey, e);
            return false;
        }
    }

    // ========================================
    // 菜单权限管理
    // ========================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMenu(SysMenu menu) {
        try {
            int result = menuMapper.insert(menu);
            return result > 0;
        } catch (Exception e) {
            log.error("创建菜单失败: menuName={}", menu.getMenuName(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenu(SysMenu menu) {
        try {
            int result = menuMapper.updateById(menu);
            if (result > 0) {
                // 清除相关缓存
                clearAllPermissionCache();
            }
            return result > 0;
        } catch (Exception e) {
            log.error("更新菜单失败: id={}", menu.getId(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenu(Long menuId) {
        try {
            // 删除角色菜单关联
            roleMenuMapper.deleteByMenuId(menuId);
            // 删除菜单
            int result = menuMapper.deleteById(menuId);

            if (result > 0) {
                // 清除相关缓存
                clearAllPermissionCache();
            }
            return result > 0;
        } catch (Exception e) {
            log.error("删除菜单失败: menuId={}", menuId, e);
            return false;
        }
    }

    @Override
    public SysMenu getMenuById(Long menuId) {
        try {
            return menuMapper.selectById(menuId);
        } catch (Exception e) {
            log.error("根据ID获取菜单失败: menuId={}", menuId, e);
            return null;
        }
    }

    @Override
    public List<MenuDTO> getAllMenusTree() {
        try {
            List<SysMenu> menus = menuMapper.selectAllEnabled();
            return buildMenuTree(convertToMenuDTO(menus), 0L);
        } catch (Exception e) {
            log.error("获取所有菜单树失败", e);
            return List.of();
        }
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        try {
            return roleMenuMapper.selectMenuIdsByRoleId(roleId);
        } catch (Exception e) {
            log.error("获取角色菜单权限失败: roleId={}", roleId, e);
            return List.of();
        }
    }

    // ========================================
    // 用户角色关联管理
    // ========================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(Long userId, List<Long> roleIds) {
        try {
            // 先删除原有关联
            userRoleMapper.deleteByUserId(userId);

            // 插入新关联
            if (roleIds != null && !roleIds.isEmpty()) {
                List<SysUserRole> userRoles = roleIds.stream()
                        .map(roleId -> SysUserRole.builder()
                                .userId(userId)
                                .roleId(roleId)
                                .build())
                        .collect(Collectors.toList());

                userRoleMapper.batchInsert(userRoles);
            }

            // 清除用户权限缓存
            refreshUserPermissionCache(userId);
            return true;
        } catch (Exception e) {
            log.error("分配用户角色失败: userId={}, roleIds={}", userId, roleIds, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeRolesFromUser(Long userId, List<Long> roleIds) {
        try {
            int result = userRoleMapper.batchDeleteByUserIdAndRoleIds(userId, roleIds);
            if (result > 0) {
                // 清除用户权限缓存
                refreshUserPermissionCache(userId);
            }
            return result > 0;
        } catch (Exception e) {
            log.error("移除用户角色失败: userId={}, roleIds={}", userId, roleIds, e);
            return false;
        }
    }

    @Override
    public List<RoleDTO> getUserRoleDetails(Long userId) {
        try {
            List<SysRole> roles = roleMapper.selectByUserId(userId);
            return convertToRoleDTO(roles);
        } catch (Exception e) {
            log.error("获取用户角色详情失败: userId={}", userId, e);
            return List.of();
        }
    }

    @Override
    public List<UserDTO> getRoleUsers(Long roleId) {
        try {
            return userService.getUsersByRole(roleId);
        } catch (Exception e) {
            log.error("获取角色用户列表失败: roleId={}", roleId, e);
            return List.of();
        }
    }

    // ========================================
    // 角色菜单关联管理
    // ========================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenusToRole(Long roleId, List<Long> menuIds) {
        try {
            // 先删除原有关联
            roleMenuMapper.deleteByRoleId(roleId);

            // 插入新关联
            if (menuIds != null && !menuIds.isEmpty()) {
                List<SysRoleMenu> roleMenus = menuIds.stream()
                        .map(menuId -> SysRoleMenu.builder()
                                .roleId(roleId)
                                .menuId(menuId)
                                .build())
                        .collect(Collectors.toList());

                roleMenuMapper.batchInsert(roleMenus);
            }

            // 清除角色权限缓存
            refreshRolePermissionCache(roleId);
            return true;
        } catch (Exception e) {
            log.error("分配角色菜单失败: roleId={}, menuIds={}", roleId, menuIds, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMenusFromRole(Long roleId, List<Long> menuIds) {
        try {
            int result = roleMenuMapper.batchDeleteByRoleIdAndMenuIds(roleId, menuIds);
            if (result > 0) {
                // 清除角色权限缓存
                refreshRolePermissionCache(roleId);
            }
            return result > 0;
        } catch (Exception e) {
            log.error("移除角色菜单失败: roleId={}, menuIds={}", roleId, menuIds, e);
            return false;
        }
    }

    // ========================================
    // 权限缓存管理
    // ========================================

    @Override
    public void refreshUserPermissionCache(Long userId) {
        try {
            // 删除用户相关缓存
            redisTemplate.delete(USER_ROLES_CACHE_PREFIX + userId);
            redisTemplate.delete(USER_PERMISSIONS_CACHE_PREFIX + userId);
        } catch (Exception e) {
            log.error("刷新用户权限缓存失败: userId={}", userId, e);
        }
    }

    @Override
    public void refreshRolePermissionCache(Long roleId) {
        try {
            // 删除角色相关缓存
            redisTemplate.delete(ROLE_PERMISSIONS_CACHE_PREFIX + roleId);

            // 删除所有用户的权限缓存（因为角色权限变更会影响用户权限）
            Set<String> userKeys = redisTemplate.keys(USER_ROLES_CACHE_PREFIX + "*");
            if (userKeys != null && !userKeys.isEmpty()) {
                redisTemplate.delete(userKeys);
            }

            Set<String> permissionKeys = redisTemplate.keys(USER_PERMISSIONS_CACHE_PREFIX + "*");
            if (permissionKeys != null && !permissionKeys.isEmpty()) {
                redisTemplate.delete(permissionKeys);
            }
        } catch (Exception e) {
            log.error("刷新角色权限缓存失败: roleId={}", roleId, e);
        }
    }

    @Override
    public void clearAllPermissionCache() {
        try {
            // 删除所有权限相关缓存
            Set<String> userRoleKeys = redisTemplate.keys(USER_ROLES_CACHE_PREFIX + "*");
            Set<String> userPermissionKeys = redisTemplate.keys(USER_PERMISSIONS_CACHE_PREFIX + "*");
            Set<String> rolePermissionKeys = redisTemplate.keys(ROLE_PERMISSIONS_CACHE_PREFIX + "*");

            if (userRoleKeys != null && !userRoleKeys.isEmpty()) {
                redisTemplate.delete(userRoleKeys);
            }
            if (userPermissionKeys != null && !userPermissionKeys.isEmpty()) {
                redisTemplate.delete(userPermissionKeys);
            }
            if (rolePermissionKeys != null && !rolePermissionKeys.isEmpty()) {
                redisTemplate.delete(rolePermissionKeys);
            }
        } catch (Exception e) {
            log.error("清除所有权限缓存失败", e);
        }
    }

    // ========================================
    // 权限验证辅助方法
    // ========================================

    @Override
    public List<MenuDTO> buildMenuTree(List<MenuDTO> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .map(menu -> {
                    menu.setChildren(buildMenuTree(menus, menu.getId()));
                    return menu;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getChildMenuIds(Long menuId) {
        try {
            List<SysMenu> childMenus = menuMapper.selectByParentId(menuId);
            return childMenus.stream().map(SysMenu::getId).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取子菜单ID失败: menuId={}", menuId, e);
            return List.of();
        }
    }

    @Override
    public boolean hasChildMenus(Long menuId) {
        try {
            int count = menuMapper.countChildMenus(menuId);
            return count > 0;
        } catch (Exception e) {
            log.error("检查是否有子菜单失败: menuId={}", menuId, e);
            return false;
        }
    }

    @Override
    public List<String> getUserRoutes(Long userId) {
        try {
            return menuMapper.selectRoutesByUserId(userId);
        } catch (Exception e) {
            log.error("获取用户路由失败: userId={}", userId, e);
            return List.of();
        }
    }

    @Override
    public boolean canAccessRoute(Long userId, String route) {
        List<String> userRoutes = getUserRoutes(userId);
        return userRoutes.contains(route);
    }

    @Override
    public String getUserDataScope(Long userId) {
        try {
            List<SysRole> roles = roleMapper.selectByUserId(userId);
            // 取最高权限的数据范围
            return roles.stream()
                    .map(SysRole::getDataScope)
                    .min(String::compareTo)
                    .orElse("5"); // 默认仅本人数据权限
        } catch (Exception e) {
            log.error("获取用户数据权限范围失败: userId={}", userId, e);
            return "5";
        }
    }

    @Override
    public boolean canAccessData(Long userId, Long dataOwnerId, Long deptId) {
        String dataScope = getUserDataScope(userId);

        switch (dataScope) {
            case "1": // 全部数据权限
                return true;
            case "2": // 自定数据权限
                // TODO: 实现自定义数据权限逻辑
                return true;
            case "3": // 本部门数据权限
                // TODO: 实现部门数据权限逻辑
                return true;
            case "4": // 本部门及以下数据权限
                // TODO: 实现部门及子部门数据权限逻辑
                return true;
            case "5": // 仅本人数据权限
            default:
                return userId.equals(dataOwnerId);
        }
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    /**
     * 转换为RoleDTO列表
     */
    private List<RoleDTO> convertToRoleDTO(List<SysRole> roles) {
        return roles.stream()
                .map(this::convertToRoleDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为RoleDTO
     */
    private RoleDTO convertToRoleDTO(SysRole role) {
        return RoleDTO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleKey(role.getRoleKey())
                .roleSort(role.getRoleSort())
                .dataScope(role.getDataScope())
                .status(role.getStatus())
                .createTime(role.getCreateTime())
                .updateTime(role.getUpdateTime())
                .remark(role.getRemark())
                .build();
    }

    /**
     * 转换为MenuDTO列表
     */
    private List<MenuDTO> convertToMenuDTO(List<SysMenu> menus) {
        return menus.stream()
                .map(this::convertToMenuDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为MenuDTO
     */
    private MenuDTO convertToMenuDTO(SysMenu menu) {
        return MenuDTO.builder()
                .id(menu.getId())
                .menuName(menu.getMenuName())
                .parentId(menu.getParentId())
                .orderNum(menu.getOrderNum())
                .path(menu.getPath())
                .component(menu.getComponent())
                .queryParam(menu.getQueryParam())
                .isFrame(menu.getIsFrame() != null && menu.getIsFrame() == 0)
                .isCache(menu.getIsCache() != null && menu.getIsCache() == 0)
                .menuType(menu.getMenuType())
                .visible(menu.getVisible())
                .status(menu.getStatus())
                .perms(menu.getPerms())
                .icon(menu.getIcon())
                .createTime(menu.getCreateTime())
                .updateTime(menu.getUpdateTime())
                .remark(menu.getRemark())
                .build();
    }
}
