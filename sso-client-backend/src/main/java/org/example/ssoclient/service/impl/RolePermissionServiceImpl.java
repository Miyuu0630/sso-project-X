package org.example.ssoclient.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.ssoclient.dto.MenuDTO;
import org.example.ssoclient.dto.RoleInfoDTO;
import org.example.ssoclient.dto.UserInfoDTO;
import org.example.ssoclient.service.RolePermissionService;
import org.example.ssoclient.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色权限服务实现
 */
@Service
@Slf4j
public class RolePermissionServiceImpl implements RolePermissionService {

    @Autowired
    private UserInfoService userInfoService;

    // 角色层级定义（数字越小权限越高，与数据库role_sort字段对应）
    private static final Map<String, Integer> ROLE_HIERARCHY = Map.of(
        "ADMIN", 1,           // 管理员 - 最高权限
        "PERSONAL_USER", 2,   // 个人用户
        "ENTERPRISE_USER", 3, // 企业用户  
        "AIRLINE_USER", 4     // 航司用户
    );

    // 角色对应的仪表板路径
    private static final Map<String, String> ROLE_DASHBOARD_MAP = Map.of(
        "ADMIN", "/dashboard/admin",
        "AIRLINE_USER", "/dashboard/airline",
        "ENTERPRISE_USER", "/dashboard/enterprise",
        "PERSONAL_USER", "/dashboard/personal"
    );

    // 角色对应的权限
    private static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
        "ADMIN", Arrays.asList(
            "system:user:list", "system:user:add", "system:user:edit", "system:user:delete",
            "system:role:list", "system:role:add", "system:role:edit", "system:role:delete",
            "system:menu:list", "system:menu:add", "system:menu:edit", "system:menu:delete",
            "monitor:online", "monitor:loginlog", "monitor:server", "monitor:performance"
        ),
        "AIRLINE_USER", Arrays.asList(
            "airline:info:view", "airline:info:edit",
            "airline:flight:list", "airline:flight:add", "airline:flight:edit", "airline:flight:delete",
            "airline:passenger:list", "airline:passenger:view",
            "airline:booking:list", "airline:booking:view",
            "airline:analytics:view"
        ),
        "ENTERPRISE_USER", Arrays.asList(
            "enterprise:info:view", "enterprise:info:edit",
            "enterprise:member:list", "enterprise:member:invite", "enterprise:member:remove",
            "enterprise:project:list", "enterprise:project:add", "enterprise:project:edit",
            "enterprise:analytics:view", "enterprise:auth:apply"
        ),
        "PERSONAL_USER", Arrays.asList(
            "user:profile:view", "user:profile:edit",
            "user:security:view", "user:security:edit",
            "user:oauth:view", "user:oauth:bind",
            "user:device:view", "user:loginlog:view"
        )
    );

    // 角色对应的可访问路由
    private static final Map<String, List<String>> ROLE_ROUTES = Map.of(
        "ADMIN", Arrays.asList(
            "/dashboard/admin", "/system/*", "/monitor/*", "/user/*"
        ),
        "AIRLINE_USER", Arrays.asList(
            "/dashboard/airline", "/airline/*", "/user/*"
        ),
        "ENTERPRISE_USER", Arrays.asList(
            "/dashboard/enterprise", "/enterprise/*", "/user/*"
        ),
        "PERSONAL_USER", Arrays.asList(
            "/dashboard/personal", "/user/*"
        )
    );

    @Override
    public RoleInfoDTO getCurrentUserRoleInfo(Long userId) {
        try {
            UserInfoDTO userInfo = userInfoService.getUserInfo(userId);
            if (userInfo == null) {
                return null;
            }

            List<String> userRoles = userInfo.getRoles();
            String primaryRole = getUserPrimaryRole(userId);

            RoleInfoDTO roleInfo = new RoleInfoDTO();
            roleInfo.setUserId(userId);
            roleInfo.setUsername(userInfo.getUsername());
            roleInfo.setRoles(userRoles);
            roleInfo.setPrimaryRole(primaryRole);
            roleInfo.setRoleLevel(getRoleLevel(primaryRole));
            roleInfo.setDashboardPath(ROLE_DASHBOARD_MAP.get(primaryRole));
            roleInfo.setPermissions(getUserPermissions(userId));

            return roleInfo;
        } catch (Exception e) {
            log.error("获取用户角色信息失败, userId: {}", userId, e);
            return null;
        }
    }

    @Override
    public List<MenuDTO> getUserMenus(Long userId) {
        String primaryRole = getUserPrimaryRole(userId);
        return buildMenusByRole(primaryRole);
    }

    @Override
    public boolean hasPermission(Long userId, String permission) {
        List<String> userPermissions = getUserPermissions(userId);
        return userPermissions.contains(permission);
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        try {
            UserInfoDTO userInfo = userInfoService.getUserInfo(userId);
            if (userInfo == null || userInfo.getRoles() == null) {
                return false;
            }
            return userInfo.getRoles().contains(roleCode);
        } catch (Exception e) {
            log.error("检查用户角色失败, userId: {}, roleCode: {}", userId, roleCode, e);
            return false;
        }
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        try {
            UserInfoDTO userInfo = userInfoService.getUserInfo(userId);
            if (userInfo == null || userInfo.getRoles() == null) {
                return Collections.emptyList();
            }

            Set<String> allPermissions = new HashSet<>();
            for (String role : userInfo.getRoles()) {
                List<String> rolePermissions = ROLE_PERMISSIONS.get(role);
                if (rolePermissions != null) {
                    allPermissions.addAll(rolePermissions);
                }
            }

            return new ArrayList<>(allPermissions);
        } catch (Exception e) {
            log.error("获取用户权限失败, userId: {}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public String getUserDashboardPath(Long userId) {
        String primaryRole = getUserPrimaryRole(userId);
        return ROLE_DASHBOARD_MAP.getOrDefault(primaryRole, "/dashboard/personal");
    }

    @Override
    public Map<String, Object> getRoleFeatures(Long userId) {
        String primaryRole = getUserPrimaryRole(userId);
        Map<String, Object> features = new HashMap<>();

        switch (primaryRole) {
            case "ADMIN":
                features.put("canManageUsers", true);
                features.put("canViewSystemMonitor", true);
                features.put("canManageRoles", true);
                features.put("canExportData", true);
                break;
            case "AIRLINE_USER":
                features.put("canManageFlights", true);
                features.put("canViewPassengers", true);
                features.put("canViewAnalytics", true);
                features.put("canManageBookings", true);
                break;
            case "ENTERPRISE_USER":
                features.put("canManageMembers", true);
                features.put("canManageProjects", true);
                features.put("canViewAnalytics", true);
                features.put("canApplyAuth", true);
                break;
            case "PERSONAL_USER":
                features.put("canEditProfile", true);
                features.put("canManageSecurity", true);
                features.put("canBindOAuth", true);
                features.put("canViewLoginLog", true);
                break;
            default:
                features.put("canEditProfile", true);
                break;
        }

        return features;
    }

    @Override
    public Map<String, Boolean> batchCheckPermissions(Long userId, List<String> permissions) {
        List<String> userPermissions = getUserPermissions(userId);
        return permissions.stream()
            .collect(Collectors.toMap(
                permission -> permission,
                userPermissions::contains
            ));
    }

    @Override
    public List<String> getAccessibleRoutes(Long userId) {
        try {
            UserInfoDTO userInfo = userInfoService.getUserInfo(userId);
            if (userInfo == null || userInfo.getRoles() == null) {
                return Collections.emptyList();
            }

            Set<String> allRoutes = new HashSet<>();
            for (String role : userInfo.getRoles()) {
                List<String> roleRoutes = ROLE_ROUTES.get(role);
                if (roleRoutes != null) {
                    allRoutes.addAll(roleRoutes);
                }
            }

            return new ArrayList<>(allRoutes);
        } catch (Exception e) {
            log.error("获取用户可访问路由失败, userId: {}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public String getUserPrimaryRole(Long userId) {
        try {
            UserInfoDTO userInfo = userInfoService.getUserInfo(userId);
            if (userInfo == null || userInfo.getRoles() == null || userInfo.getRoles().isEmpty()) {
                return "PERSONAL_USER";
            }

            // 根据角色层级返回最高权限的角色
            return userInfo.getRoles().stream()
                .filter(ROLE_HIERARCHY::containsKey)
                .min(Comparator.comparing(ROLE_HIERARCHY::get))
                .orElse("PERSONAL_USER");
        } catch (Exception e) {
            log.error("获取用户主要角色失败, userId: {}", userId, e);
            return "PERSONAL_USER";
        }
    }

    @Override
    public int getRoleLevel(String roleCode) {
        return ROLE_HIERARCHY.getOrDefault(roleCode, 999);
    }

    /**
     * 根据角色构建菜单
     */
    private List<MenuDTO> buildMenusByRole(String role) {
        List<MenuDTO> menus = new ArrayList<>();

        switch (role) {
            case "ADMIN":
                menus.addAll(buildAdminMenus());
                break;
            case "AIRLINE_USER":
                menus.addAll(buildAirlineMenus());
                break;
            case "ENTERPRISE_USER":
                menus.addAll(buildEnterpriseMenus());
                break;
            case "PERSONAL_USER":
                menus.addAll(buildPersonalMenus());
                break;
        }

        // 所有角色都有的通用菜单
        menus.addAll(buildCommonMenus());

        return menus;
    }

    private List<MenuDTO> buildAdminMenus() {
        return Arrays.asList(
            MenuDTO.builder().id(1L).name("系统管理").path("/system").icon("Setting").build(),
            MenuDTO.builder().id(2L).name("用户管理").path("/system/user").icon("User").parentId(1L).build(),
            MenuDTO.builder().id(3L).name("角色管理").path("/system/role").icon("UserFilled").parentId(1L).build(),
            MenuDTO.builder().id(4L).name("系统监控").path("/monitor").icon("Monitor").build(),
            MenuDTO.builder().id(5L).name("在线用户").path("/monitor/online").icon("UserFilled").parentId(4L).build()
        );
    }

    private List<MenuDTO> buildAirlineMenus() {
        return Arrays.asList(
            MenuDTO.builder().id(10L).name("航司管理").path("/airline").icon("Airplane").build(),
            MenuDTO.builder().id(11L).name("航班管理").path("/airline/flight").icon("Airplane").parentId(10L).build(),
            MenuDTO.builder().id(12L).name("乘客管理").path("/airline/passenger").icon("UserFilled").parentId(10L).build()
        );
    }

    private List<MenuDTO> buildEnterpriseMenus() {
        return Arrays.asList(
            MenuDTO.builder().id(20L).name("企业管理").path("/enterprise").icon("OfficeBuilding").build(),
            MenuDTO.builder().id(21L).name("成员管理").path("/enterprise/member").icon("UserFilled").parentId(20L).build(),
            MenuDTO.builder().id(22L).name("项目管理").path("/enterprise/project").icon("Folder").parentId(20L).build()
        );
    }

    private List<MenuDTO> buildPersonalMenus() {
        return Arrays.asList(
            MenuDTO.builder().id(30L).name("个人中心").path("/user").icon("User").build()
        );
    }

    private List<MenuDTO> buildCommonMenus() {
        return Arrays.asList(
            MenuDTO.builder().id(100L).name("个人资料").path("/user/profile").icon("User").build(),
            MenuDTO.builder().id(101L).name("安全设置").path("/user/security").icon("Lock").build()
        );
    }
}
