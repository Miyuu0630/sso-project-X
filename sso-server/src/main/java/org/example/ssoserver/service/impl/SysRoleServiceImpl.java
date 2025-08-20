package org.example.ssoserver.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.result.Result;
import org.example.common.result.ResultCode;
import org.example.common.entity.SysRole;
import org.example.common.entity.SysUserRole;
import org.example.common.mapper.SysRoleMapper;
import org.example.common.mapper.SysUserRoleMapper;
import org.example.ssoserver.service.SysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {
    
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    
    @Override
    public List<SysRole> getUserRoles(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        return roleIds.stream()
                .map(roleMapper::selectById)
                .filter(role -> role != null)
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Override
    public SysRole getRoleById(Long id) {
        if (id == null) {
            return null;
        }
        return roleMapper.selectById(id);
    }
    
    @Override
    public SysRole getRoleByCode(String roleCode) {
        if (StrUtil.isBlank(roleCode)) {
            return null;
        }
        return roleMapper.selectByRoleKey(roleCode);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<SysRole> createRole(SysRole role) {
        try {
            // 检查必填字段
            if (StrUtil.isBlank(role.getRoleKey()) || StrUtil.isBlank(role.getRoleName())) {
                return Result.error(ResultCode.PARAM_ERROR.getCode(), "角色权限字符串和角色名称不能为空");
            }

            // 检查角色编码是否存在
            if (getRoleByCode(role.getRoleKey()) != null) {
                return Result.error("角色权限字符串已存在");
            }

            role.setCreateTime(LocalDateTime.now());
            role.setUpdateTime(LocalDateTime.now());
            if (role.getStatus() == null) {
                role.setStatus("1"); // 默认启用
            }
            
            int result = roleMapper.insert(role);
            if (result > 0) {
                return Result.success("创建成功", role);
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建角色失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<SysRole> updateRole(SysRole role) {
        try {
            if (role.getId() == null) {
                return Result.error(ResultCode.PARAM_ERROR.getCode(), "角色ID不能为空");
            }
            
            role.setUpdateTime(LocalDateTime.now());
            int result = roleMapper.update(role);
            if (result > 0) {
                return Result.success("更新成功", getRoleById(role.getId()));
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteRole(Long id) {
        try {
            // 检查是否有用户使用该角色
            List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(id);
            if (!userIds.isEmpty()) {
                return Result.error("该角色正在使用中，无法删除");
            }
            
            int result = roleMapper.deleteById(id);
            if (result > 0) {
                return Result.<Void>success();
            } else {
                return Result.<Void>error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> getRoleList(String roleName, String roleCode,
                                                  String status, Integer page, Integer size) {
        try {
            // 计算偏移量
            int offset = (page - 1) * size;
            
            // 查询角色列表
            List<SysRole> roles = roleMapper.selectPage(roleName, roleCode, status, offset, size);
            
            // 查询总数
            Long totalLong = roleMapper.selectCount(roleName, roleCode, status);
            int total = totalLong != null ? totalLong.intValue() : 0;
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", roles);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询角色列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<SysRole> getAllEnabledRoles() {
        return roleMapper.selectAllEnabled();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> assignRolesToUser(Long userId, List<Long> roleIds) {
        try {
            // 先删除用户所有角色
            userRoleMapper.deleteByUserId(userId);

            // 分配新角色
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole(userId, roleId);
                userRoleMapper.insert(userRole);
            }

            return Result.<Void>success();
        } catch (Exception e) {
            log.error("分配角色失败", e);
            return Result.<Void>error("分配失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> removeRolesFromUser(Long userId, List<Long> roleIds) {
        try {
            for (Long roleId : roleIds) {
                userRoleMapper.delete(userId, roleId);
            }
            return Result.<Void>success();
        } catch (Exception e) {
            log.error("移除角色失败", e);
            return Result.<Void>error("移除失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean hasRole(Long userId, String roleCode) {
        if (userId == null || StrUtil.isBlank(roleCode)) {
            return false;
        }
        
        List<SysRole> roles = getUserRoles(userId);
        return roles.stream().anyMatch(role -> roleCode.equals(role.getRoleKey()));
    }
    
    @Override
    public boolean hasAnyRole(Long userId, String... roleCodes) {
        if (userId == null || roleCodes == null || roleCodes.length == 0) {
            return false;
        }
        
        List<SysRole> roles = getUserRoles(userId);
        for (String roleCode : roleCodes) {
            if (roles.stream().anyMatch(role -> roleCode.equals(role.getRoleKey()))) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean hasAllRoles(Long userId, String... roleCodes) {
        if (userId == null || roleCodes == null || roleCodes.length == 0) {
            return false;
        }
        
        List<SysRole> roles = getUserRoles(userId);
        for (String roleCode : roleCodes) {
            if (roles.stream().noneMatch(role -> roleCode.equals(role.getRoleKey()))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public SysRole getDefaultRoleByUserType(String userType) {
        if (StrUtil.isBlank(userType)) {
            userType = "user"; // 默认普通用户
        }
        // 根据用户类型返回默认角色，这里可以根据业务需求实现
        // 暂时返回第一个启用的角色作为默认角色
        List<SysRole> enabledRoles = roleMapper.selectAllEnabled();
        return enabledRoles.isEmpty() ? null : enabledRoles.get(0);
    }
}
