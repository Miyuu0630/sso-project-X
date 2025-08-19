package org.example.ssoserver.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ssoserver.common.Result;
import org.example.ssoserver.entity.SysPermission;
import org.example.ssoserver.mapper.*;
import org.example.ssoserver.service.SysPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {
    
    private final SysPermissionMapper permissionMapper;
    
    @Override
    public List<String> getUserPermissions(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return permissionMapper.selectPermissionCodesByUserId(userId);
    }
    
    @Override
    public Result<List<SysPermission>> getUserMenuTree(Long userId) {
        try {
            List<SysPermission> menuPermissions = permissionMapper.selectMenuPermissionsByUserId(userId);
            List<SysPermission> tree = buildPermissionTree(menuPermissions);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取用户菜单权限树失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<List<SysPermission>> getAllPermissionTree() {
        try {
            List<SysPermission> allPermissions = permissionMapper.selectAll();
            List<SysPermission> tree = buildPermissionTree(allPermissions);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取权限树失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    @Override
    public SysPermission getPermissionById(Long id) {
        if (id == null) {
            return null;
        }
        return permissionMapper.selectById(id);
    }
    
    @Override
    public SysPermission getPermissionByCode(String permissionCode) {
        if (StrUtil.isBlank(permissionCode)) {
            return null;
        }
        return permissionMapper.selectByPermissionCode(permissionCode);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<SysPermission> createPermission(SysPermission permission) {
        try {
            // 检查必填字段
            if (StrUtil.isBlank(permission.getPermissionCode()) || StrUtil.isBlank(permission.getPermissionName())) {
                return Result.error(Result.ResultCode.PARAM_ERROR.getCode(), "权限编码和权限名称不能为空");
            }
            
            // 检查权限编码是否存在
            if (permissionMapper.countByPermissionCode(permission.getPermissionCode()) > 0) {
                return Result.error("权限编码已存在");
            }
            
            permission.setCreateTime(LocalDateTime.now());
            permission.setUpdateTime(LocalDateTime.now());
            if (permission.getStatus() == null) {
                permission.setStatus(1); // 默认启用
            }
            if (permission.getParentId() == null) {
                permission.setParentId(0L); // 默认为根权限
            }
            
            int result = permissionMapper.insert(permission);
            if (result > 0) {
                return Result.success("创建成功", permission);
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建权限失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<SysPermission> updatePermission(SysPermission permission) {
        try {
            if (permission.getId() == null) {
                return Result.error(Result.ResultCode.PARAM_ERROR.getCode(), "权限ID不能为空");
            }
            
            permission.setUpdateTime(LocalDateTime.now());
            int result = permissionMapper.update(permission);
            if (result > 0) {
                return Result.success("更新成功", getPermissionById(permission.getId()));
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新权限失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deletePermission(Long id) {
        try {
            // 检查是否有子权限
            if (permissionMapper.countByParentId(id) > 0) {
                return Result.error("该权限下有子权限，无法删除");
            }
            
            int result = permissionMapper.deleteById(id);
            if (result > 0) {
                return Result.<Void>success();
            } else {
                return Result.<Void>error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除权限失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (userId == null || StrUtil.isBlank(permissionCode)) {
            return false;
        }
        
        List<String> permissions = getUserPermissions(userId);
        return permissions.contains(permissionCode);
    }
    
    @Override
    public boolean hasAnyPermission(Long userId, String... permissionCodes) {
        if (userId == null || permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }
        
        List<String> permissions = getUserPermissions(userId);
        for (String permissionCode : permissionCodes) {
            if (permissions.contains(permissionCode)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean hasAllPermissions(Long userId, String... permissionCodes) {
        if (userId == null || permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }
        
        List<String> permissions = getUserPermissions(userId);
        for (String permissionCode : permissionCodes) {
            if (!permissions.contains(permissionCode)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public List<SysPermission> getRolePermissions(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        return permissionMapper.selectByRoleId(roleId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        try {
            // 先删除角色所有权限
            permissionMapper.deleteRoleAllPermissions(roleId);
            
            // 分配新权限
            LocalDateTime now = LocalDateTime.now();
            for (Long permissionId : permissionIds) {
                permissionMapper.insertRolePermission(roleId, permissionId, now, roleId);
            }
            
            return Result.<Void>success();
        } catch (Exception e) {
            log.error("分配权限失败", e);
            return Result.<Void>error("分配失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        try {
            for (Long permissionId : permissionIds) {
                permissionMapper.deleteRolePermission(roleId, permissionId);
            }
            return Result.<Void>success();
        } catch (Exception e) {
            log.error("移除权限失败", e);
            return Result.<Void>error("移除失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建权限树
     */
    private List<SysPermission> buildPermissionTree(List<SysPermission> permissions) {
        // 按父ID分组
        Map<Long, List<SysPermission>> permissionMap = permissions.stream()
            .collect(Collectors.groupingBy(p -> p.getParentId() == null ? 0L : p.getParentId()));
        
        // 构建树结构
        List<SysPermission> rootPermissions = permissionMap.getOrDefault(0L, new ArrayList<>());
        buildChildren(rootPermissions, permissionMap);
        
        return rootPermissions;
    }
    
    /**
     * 递归构建子权限
     */
    private void buildChildren(List<SysPermission> permissions, Map<Long, List<SysPermission>> permissionMap) {
        for (SysPermission permission : permissions) {
            List<SysPermission> children = permissionMap.getOrDefault(permission.getId(), new ArrayList<>());
            permission.setChildren(children);
            if (!children.isEmpty()) {
                buildChildren(children, permissionMap);
            }
        }
    }
}
