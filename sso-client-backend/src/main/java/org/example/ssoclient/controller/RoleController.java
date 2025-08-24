package org.example.ssoclient.controller;

import java.util.List;
import java.util.Map;

import org.example.ssoclient.dto.MenuDTO;
import org.example.ssoclient.dto.RoleInfoDTO;
import org.example.ssoclient.service.RolePermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 角色权限控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class RoleController {

    private final RolePermissionService rolePermissionService;

    /**
     * 获取当前用户角色信息
     */
    @GetMapping("/current")
    public Map<String, Object> getCurrentRole() {
        try {
            if (!StpUtil.isLogin()) {
                return Map.of("code", 401, "message", "未登录");
            }

            Long userId = StpUtil.getLoginIdAsLong();
            RoleInfoDTO roleInfo = rolePermissionService.getCurrentUserRoleInfo(userId);
            
            if (roleInfo != null) {
                return Map.of("code", 200, "message", "获取成功", "data", roleInfo);
            } else {
                return Map.of("code", 500, "message", "获取角色信息失败");
            }
        } catch (Exception e) {
            log.error("获取角色信息失败", e);
            return Map.of("code", 500, "message", "获取角色信息失败");
        }
    }

    /**
     * 获取当前用户权限列表
     */
    @GetMapping("/permissions")
    public Map<String, Object> getCurrentPermissions() {
        try {
            if (!StpUtil.isLogin()) {
                return Map.of("code", 401, "message", "未登录");
            }

            Long userId = StpUtil.getLoginIdAsLong();
            List<String> permissions = rolePermissionService.getUserPermissions(userId);
            
            if (permissions != null) {
                return Map.of("code", 200, "message", "获取成功", "data", permissions);
            } else {
                return Map.of("code", 500, "message", "获取权限信息失败");
            }
        } catch (Exception e) {
            log.error("获取权限信息失败", e);
            return Map.of("code", 500, "message", "获取权限信息失败");
        }
    }

    /**
     * 获取当前用户菜单列表
     */
    @GetMapping("/menus")
    public Map<String, Object> getCurrentMenus() {
        try {
            if (!StpUtil.isLogin()) {
                return Map.of("code", 401, "message", "未登录");
            }

            Long userId = StpUtil.getLoginIdAsLong();
            List<MenuDTO> menus = rolePermissionService.getUserMenus(userId);
            
            if (menus != null) {
                return Map.of("code", 200, "message", "获取成功", "data", menus);
            } else {
                return Map.of("code", 500, "message", "获取菜单信息失败");
            }
        } catch (Exception e) {
            log.error("获取菜单信息失败", e);
            return Map.of("code", 500, "message", "获取菜单信息失败");
        }
    }

    /**
     * 获取当前用户功能配置
     */
    @GetMapping("/features")
    public Map<String, Object> getCurrentFeatures() {
        try {
            if (!StpUtil.isLogin()) {
                return Map.of("code", 401, "message", "未登录");
            }

            Long userId = StpUtil.getLoginIdAsLong();
            Map<String, Object> features = rolePermissionService.getRoleFeatures(userId);
            
            if (features != null) {
                return Map.of("code", 200, "message", "获取成功", "data", features);
            } else {
                return Map.of("code", 500, "message", "获取功能配置失败");
            }
        } catch (Exception e) {
            log.error("获取功能配置失败", e);
            return Map.of("code", 500, "message", "获取功能配置失败");
        }
    }

    /**
     * 检查用户是否有指定权限
     */
    @PostMapping("/check-permission")
    public Map<String, Object> checkPermission(@RequestBody Map<String, String> request) {
        try {
            if (!StpUtil.isLogin()) {
                return Map.of("code", 401, "message", "未登录");
            }

            String permission = request.get("permission");
            if (permission == null || permission.trim().isEmpty()) {
                return Map.of("code", 400, "message", "权限参数不能为空");
            }

            Long userId = StpUtil.getLoginIdAsLong();
            boolean hasPermission = rolePermissionService.hasPermission(userId, permission);
            
            return Map.of(
                "code", 200, 
                "message", "检查完成", 
                "data", Map.of(
                    "hasPermission", hasPermission,
                    "permission", permission,
                    "userId", userId
                )
            );
        } catch (Exception e) {
            log.error("检查权限失败", e);
            return Map.of("code", 500, "message", "检查权限失败");
        }
    }

    /**
     * 获取用户仪表板路径
     */
    @GetMapping("/dashboard-path")
    public Map<String, Object> getDashboardPath() {
        try {
            if (!StpUtil.isLogin()) {
                return Map.of("code", 401, "message", "未登录");
            }

            Long userId = StpUtil.getLoginIdAsLong();
            String dashboardPath = rolePermissionService.getUserDashboardPath(userId);
            
            return Map.of(
                "code", 200, 
                "message", "获取成功", 
                "data", Map.of(
                    "dashboardPath", dashboardPath,
                    "userId", userId
                )
            );
        } catch (Exception e) {
            log.error("获取仪表板路径失败", e);
            return Map.of("code", 500, "message", "获取仪表板路径失败");
        }
    }
}
