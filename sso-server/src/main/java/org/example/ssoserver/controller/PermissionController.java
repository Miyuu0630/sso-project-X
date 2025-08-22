package org.example.ssoserver.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.model.ApiResponse;
import org.example.common.model.RoleDTO;
import org.example.common.model.MenuDTO;
import org.example.common.model.UserDTO;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResultCode;
import org.example.ssoserver.entity.SysRole;
import org.example.ssoserver.entity.SysMenu;
import org.example.ssoserver.service.PermissionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限管理控制器
 * 提供角色、菜单、权限管理相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
@Validated
@Tag(name = "权限管理", description = "角色、菜单、权限管理相关接口")
public class PermissionController {
    
    private final PermissionService permissionService;
    
    // ========================================
    // 用户权限查询接口
    // ========================================
    
    /**
     * 获取当前用户权限信息
     */
    @GetMapping("/user/current")
    @Operation(summary = "获取当前用户权限", description = "获取当前登录用户的角色和权限信息")
    public ApiResponse<Map<String, Object>> getCurrentUserPermissions() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            Map<String, Object> permissions = new HashMap<>();
            permissions.put("roles", permissionService.getUserRoles(userId));
            permissions.put("permissions", permissionService.getUserPermissions(userId));
            permissions.put("menus", permissionService.getUserMenus(userId));
            permissions.put("buttons", permissionService.getUserButtonPermissions(userId));
            permissions.put("routes", permissionService.getUserRoutes(userId));
            
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            log.error("获取当前用户权限异常", e);
            return ApiResponse.error("获取权限信息失败");
        }
    }
    
    /**
     * 获取指定用户权限信息
     */
    @GetMapping("/user/{userId}")
    @SaCheckPermission("system:user:query")
    @Operation(summary = "获取用户权限", description = "获取指定用户的权限信息")
    public ApiResponse<Map<String, Object>> getUserPermissions(@PathVariable @NotNull Long userId) {
        try {
            Map<String, Object> permissions = new HashMap<>();
            permissions.put("userId", userId);
            permissions.put("roles", permissionService.getUserRoleDetails(userId));
            permissions.put("permissions", permissionService.getUserPermissions(userId));
            permissions.put("menus", permissionService.getUserMenus(userId));
            
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            log.error("获取用户权限异常: userId={}", userId, e);
            return ApiResponse.error("获取权限信息失败");
        }
    }
    
    /**
     * 检查用户权限
     */
    @GetMapping("/check")
    @Operation(summary = "检查权限", description = "检查当前用户是否有指定权限")
    public ApiResponse<Map<String, Boolean>> checkPermissions(@RequestParam String permissions) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            String[] permArray = permissions.split(",");
            
            Map<String, Boolean> result = new HashMap<>();
            for (String perm : permArray) {
                result.put(perm.trim(), permissionService.hasPermission(userId, perm.trim()));
            }
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("检查权限异常: permissions={}", permissions, e);
            return ApiResponse.error("权限检查失败");
        }
    }
    
    // ========================================
    // 角色管理接口
    // ========================================
    
    /**
     * 获取所有角色
     */
    @GetMapping("/roles")
    @SaCheckPermission("system:role:list")
    @Operation(summary = "获取角色列表", description = "获取所有角色信息")
    public ApiResponse<List<RoleDTO>> getAllRoles() {
        try {
            List<RoleDTO> roles = permissionService.getAllRoles();
            return ApiResponse.success(roles);
        } catch (Exception e) {
            log.error("获取角色列表异常", e);
            return ApiResponse.error("获取角色列表失败");
        }
    }
    
    /**
     * 根据ID获取角色详情
     */
    @GetMapping("/roles/{id}")
    @SaCheckPermission("system:role:query")
    @Operation(summary = "获取角色详情", description = "根据角色ID获取角色详细信息")
    public ApiResponse<RoleDTO> getRoleById(@PathVariable @NotNull Long id) {
        try {
            SysRole role = permissionService.getRoleById(id);
            if (role != null) {
                // 转换为DTO并获取菜单权限
                RoleDTO roleDTO = convertToRoleDTO(role);
                roleDTO.setMenuIds(permissionService.getRoleMenuIds(id));
                
                return ApiResponse.success(roleDTO);
            } else {
                return ApiResponse.error("角色不存在");
            }
        } catch (Exception e) {
            log.error("获取角色详情异常: id={}", id, e);
            return ApiResponse.error("获取角色详情失败");
        }
    }
    
    /**
     * 创建角色
     */
    @PostMapping("/roles")
    @SaCheckPermission("system:role:add")
    @Operation(summary = "创建角色", description = "创建新角色")
    public ApiResponse<Void> createRole(@Valid @RequestBody SysRole role) {
        try {
            // 检查角色标识是否存在
            if (permissionService.isRoleKeyExists(role.getRoleKey(), null)) {
                return ApiResponse.error("角色标识已存在");
            }
            
            // 设置创建人
            role.setCreateBy(StpUtil.getLoginIdAsLong());
            
            boolean success = permissionService.createRole(role);
            if (success) {
                log.info("创建角色成功: roleKey={}, createBy={}", role.getRoleKey(), role.getCreateBy());
                return ApiResponse.success("角色创建成功");
            } else {
                return ApiResponse.error("角色创建失败");
            }
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建角色异常: roleKey={}", role.getRoleKey(), e);
            return ApiResponse.error("角色创建失败");
        }
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/roles/{id}")
    @SaCheckPermission("system:role:edit")
    @Operation(summary = "更新角色", description = "更新角色信息")
    public ApiResponse<Void> updateRole(@PathVariable @NotNull Long id, @Valid @RequestBody SysRole role) {
        try {
            // 检查角色是否存在
            SysRole existingRole = permissionService.getRoleById(id);
            if (existingRole == null) {
                return ApiResponse.error("角色不存在");
            }
            
            // 检查角色标识是否重复
            if (!existingRole.getRoleKey().equals(role.getRoleKey()) && 
                permissionService.isRoleKeyExists(role.getRoleKey(), id)) {
                return ApiResponse.error("角色标识已存在");
            }
            
            // 检查是否为内置角色
            if (existingRole.isBuiltIn()) {
                return ApiResponse.error("不能修改系统内置角色");
            }
            
            // 设置更新信息
            role.setId(id);
            role.setUpdateBy(StpUtil.getLoginIdAsLong());
            
            boolean success = permissionService.updateRole(role);
            if (success) {
                log.info("更新角色成功: id={}, updateBy={}", id, role.getUpdateBy());
                return ApiResponse.success("角色更新成功");
            } else {
                return ApiResponse.error("角色更新失败");
            }
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新角色异常: id={}", id, e);
            return ApiResponse.error("角色更新失败");
        }
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/roles/{id}")
    @SaCheckPermission("system:role:remove")
    @Operation(summary = "删除角色", description = "删除角色")
    public ApiResponse<Void> deleteRole(@PathVariable @NotNull Long id) {
        try {
            // 检查角色是否存在
            SysRole role = permissionService.getRoleById(id);
            if (role == null) {
                return ApiResponse.error("角色不存在");
            }
            
            // 检查是否为内置角色
            if (role.isBuiltIn()) {
                return ApiResponse.error("不能删除系统内置角色");
            }
            
            // 检查是否有用户使用该角色
            List<UserDTO> roleUsers = permissionService.getRoleUsers(id);
            if (!roleUsers.isEmpty()) {
                return ApiResponse.error("该角色下还有用户，不能删除");
            }
            
            boolean success = permissionService.deleteRole(id);
            if (success) {
                Long currentUserId = StpUtil.getLoginIdAsLong();
                log.info("删除角色成功: id={}, deleteBy={}", id, currentUserId);
                return ApiResponse.success("角色删除成功");
            } else {
                return ApiResponse.error("角色删除失败");
            }
        } catch (Exception e) {
            log.error("删除角色异常: id={}", id, e);
            return ApiResponse.error("角色删除失败");
        }
    }

    // ========================================
    // 菜单管理接口
    // ========================================

    /**
     * 获取菜单树
     */
    @GetMapping("/menus/tree")
    @SaCheckPermission("system:menu:list")
    @Operation(summary = "获取菜单树", description = "获取所有菜单的树形结构")
    public ApiResponse<List<MenuDTO>> getMenuTree() {
        try {
            List<MenuDTO> menuTree = permissionService.getAllMenusTree();
            return ApiResponse.success(menuTree);
        } catch (Exception e) {
            log.error("获取菜单树异常", e);
            return ApiResponse.error("获取菜单树失败");
        }
    }

    /**
     * 根据ID获取菜单详情
     */
    @GetMapping("/menus/{id}")
    @SaCheckPermission("system:menu:query")
    @Operation(summary = "获取菜单详情", description = "根据菜单ID获取菜单详细信息")
    public ApiResponse<MenuDTO> getMenuById(@PathVariable @NotNull Long id) {
        try {
            SysMenu menu = permissionService.getMenuById(id);
            if (menu != null) {
                MenuDTO menuDTO = convertToMenuDTO(menu);
                return ApiResponse.success(menuDTO);
            } else {
                return ApiResponse.error("菜单不存在");
            }
        } catch (Exception e) {
            log.error("获取菜单详情异常: id={}", id, e);
            return ApiResponse.error("获取菜单详情失败");
        }
    }

    /**
     * 创建菜单
     */
    @PostMapping("/menus")
    @SaCheckPermission("system:menu:add")
    @Operation(summary = "创建菜单", description = "创建新菜单")
    public ApiResponse<Void> createMenu(@Valid @RequestBody SysMenu menu) {
        try {
            // 设置创建人
            menu.setCreateBy(StpUtil.getLoginIdAsLong());

            boolean success = permissionService.createMenu(menu);
            if (success) {
                log.info("创建菜单成功: menuName={}, createBy={}", menu.getMenuName(), menu.getCreateBy());
                return ApiResponse.success("菜单创建成功");
            } else {
                return ApiResponse.error("菜单创建失败");
            }
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建菜单异常: menuName={}", menu.getMenuName(), e);
            return ApiResponse.error("菜单创建失败");
        }
    }

    /**
     * 更新菜单
     */
    @PutMapping("/menus/{id}")
    @SaCheckPermission("system:menu:edit")
    @Operation(summary = "更新菜单", description = "更新菜单信息")
    public ApiResponse<Void> updateMenu(@PathVariable @NotNull Long id, @Valid @RequestBody SysMenu menu) {
        try {
            // 检查菜单是否存在
            SysMenu existingMenu = permissionService.getMenuById(id);
            if (existingMenu == null) {
                return ApiResponse.error("菜单不存在");
            }

            // 设置更新信息
            menu.setId(id);
            menu.setUpdateBy(StpUtil.getLoginIdAsLong());

            boolean success = permissionService.updateMenu(menu);
            if (success) {
                log.info("更新菜单成功: id={}, updateBy={}", id, menu.getUpdateBy());
                return ApiResponse.success("菜单更新成功");
            } else {
                return ApiResponse.error("菜单更新失败");
            }
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新菜单异常: id={}", id, e);
            return ApiResponse.error("菜单更新失败");
        }
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/menus/{id}")
    @SaCheckPermission("system:menu:remove")
    @Operation(summary = "删除菜单", description = "删除菜单")
    public ApiResponse<Void> deleteMenu(@PathVariable @NotNull Long id) {
        try {
            // 检查是否有子菜单
            if (permissionService.hasChildMenus(id)) {
                return ApiResponse.error("存在子菜单，不能删除");
            }

            boolean success = permissionService.deleteMenu(id);
            if (success) {
                Long currentUserId = StpUtil.getLoginIdAsLong();
                log.info("删除菜单成功: id={}, deleteBy={}", id, currentUserId);
                return ApiResponse.success("菜单删除成功");
            } else {
                return ApiResponse.error("菜单删除失败");
            }
        } catch (Exception e) {
            log.error("删除菜单异常: id={}", id, e);
            return ApiResponse.error("菜单删除失败");
        }
    }

    // ========================================
    // 权限分配接口
    // ========================================

    /**
     * 为用户分配角色
     */
    @PostMapping("/assign/user-roles")
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "分配用户角色", description = "为用户分配角色")
    public ApiResponse<Void> assignRolesToUser(@RequestParam @NotNull Long userId,
                                              @RequestParam @NotEmpty List<Long> roleIds) {
        try {
            boolean success = permissionService.assignRolesToUser(userId, roleIds);
            if (success) {
                // 刷新用户权限缓存
                permissionService.refreshUserPermissionCache(userId);

                Long currentUserId = StpUtil.getLoginIdAsLong();
                log.info("分配用户角色成功: userId={}, roleIds={}, operateBy={}", userId, roleIds, currentUserId);
                return ApiResponse.success("角色分配成功");
            } else {
                return ApiResponse.error("角色分配失败");
            }
        } catch (Exception e) {
            log.error("分配用户角色异常: userId={}, roleIds={}", userId, roleIds, e);
            return ApiResponse.error("角色分配失败");
        }
    }

    /**
     * 为角色分配菜单权限
     */
    @PostMapping("/assign/role-menus")
    @SaCheckPermission("system:role:edit")
    @Operation(summary = "分配角色菜单", description = "为角色分配菜单权限")
    public ApiResponse<Void> assignMenusToRole(@RequestParam @NotNull Long roleId,
                                              @RequestParam @NotEmpty List<Long> menuIds) {
        try {
            boolean success = permissionService.assignMenusToRole(roleId, menuIds);
            if (success) {
                // 刷新角色权限缓存
                permissionService.refreshRolePermissionCache(roleId);

                Long currentUserId = StpUtil.getLoginIdAsLong();
                log.info("分配角色菜单成功: roleId={}, menuIds={}, operateBy={}", roleId, menuIds, currentUserId);
                return ApiResponse.success("菜单权限分配成功");
            } else {
                return ApiResponse.error("菜单权限分配失败");
            }
        } catch (Exception e) {
            log.error("分配角色菜单异常: roleId={}, menuIds={}", roleId, menuIds, e);
            return ApiResponse.error("菜单权限分配失败");
        }
    }

    /**
     * 清除权限缓存
     */
    @PostMapping("/cache/clear")
    @SaCheckRole("ADMIN")
    @Operation(summary = "清除权限缓存", description = "清除所有权限缓存")
    public ApiResponse<Void> clearPermissionCache() {
        try {
            permissionService.clearAllPermissionCache();
            log.info("清除权限缓存成功");
            return ApiResponse.success("权限缓存清除成功");
        } catch (Exception e) {
            log.error("清除权限缓存异常", e);
            return ApiResponse.error("权限缓存清除失败");
        }
    }

    // ========================================
    // 私有辅助方法
    // ========================================

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
