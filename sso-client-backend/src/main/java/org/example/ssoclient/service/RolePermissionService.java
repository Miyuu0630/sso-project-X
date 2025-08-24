package org.example.ssoclient.service;

import org.example.ssoclient.dto.MenuDTO;
import org.example.ssoclient.dto.RoleInfoDTO;

import java.util.List;
import java.util.Map;

/**
 * 角色权限服务接口
 */
public interface RolePermissionService {

    /**
     * 获取用户角色信息
     */
    RoleInfoDTO getCurrentUserRoleInfo(Long userId);

    /**
     * 获取用户菜单列表
     */
    List<MenuDTO> getUserMenus(Long userId);

    /**
     * 检查用户是否有指定权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 检查用户是否有指定角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 获取用户权限列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户仪表板路径
     */
    String getUserDashboardPath(Long userId);

    /**
     * 获取角色功能配置
     */
    Map<String, Object> getRoleFeatures(Long userId);

    /**
     * 批量检查权限
     */
    Map<String, Boolean> batchCheckPermissions(Long userId, List<String> permissions);

    /**
     * 获取用户可访问的路由列表
     */
    List<String> getAccessibleRoutes(Long userId);

    /**
     * 获取用户主要角色
     */
    String getUserPrimaryRole(Long userId);

    /**
     * 获取角色层级
     */
    int getRoleLevel(String roleCode);
}
