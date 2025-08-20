package org.example.ssoserver.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.entity.SysMenu;
import org.example.common.mapper.SysMenuMapper;

import org.example.common.result.Result;
import org.example.common.result.ResultCode;
import org.example.ssoserver.service.SysMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper menuMapper;
    
    @Override
    public List<String> getUserPermissions(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return menuMapper.selectPermsByUserId(userId);
    }

    @Override
    public Result<List<SysMenu>> getUserMenuTree(Long userId) {
        try {
            List<SysMenu> userMenus = menuMapper.selectMenusByUserId(userId);
            List<SysMenu> tree = buildMenuTree(userMenus);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取用户菜单权限树失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @Override
    public Result<List<SysMenu>> getAllMenuTree() {
        try {
            List<SysMenu> allMenus = menuMapper.selectAll();
            List<SysMenu> tree = buildMenuTree(allMenus);
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取菜单树失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    @Override
    public SysMenu getMenuById(Long id) {
        if (id == null) {
            return null;
        }
        return menuMapper.selectById(id);
    }

    @Override
    public SysMenu getMenuByPerms(String perms) {
        if (StrUtil.isBlank(perms)) {
            return null;
        }
        return menuMapper.selectByPerms(perms);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<SysMenu> createMenu(SysMenu menu) {
        try {
            // 检查必填字段
            if (StrUtil.isBlank(menu.getMenuName())) {
                return Result.error(ResultCode.PARAM_ERROR.getCode(), "菜单名称不能为空");
            }

            // 检查权限标识是否存在（如果有的话）
            if (StrUtil.isNotBlank(menu.getPerms()) && menuMapper.countByPerms(menu.getPerms()) > 0) {
                return Result.error("权限标识已存在");
            }

            menu.setCreateTime(LocalDateTime.now());
            menu.setUpdateTime(LocalDateTime.now());
            if (StrUtil.isBlank(menu.getStatus())) {
                menu.setStatus("1"); // 默认启用
            }
            if (menu.getParentId() == null) {
                menu.setParentId(0L); // 默认为根菜单
            }
            if (menu.getOrderNum() == null) {
                menu.setOrderNum(0); // 默认排序
            }

            int result = menuMapper.insert(menu);
            if (result > 0) {
                return Result.success("创建成功", menu);
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建菜单失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<SysMenu> updateMenu(SysMenu menu) {
        try {
            if (menu.getId() == null) {
                return Result.error(ResultCode.PARAM_ERROR.getCode(), "菜单ID不能为空");
            }

            menu.setUpdateTime(LocalDateTime.now());
            int result = menuMapper.update(menu);
            if (result > 0) {
                return Result.success("更新成功", getMenuById(menu.getId()));
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新菜单失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteMenu(Long id) {
        try {
            // 检查是否有子菜单
            if (menuMapper.countByParentId(id) > 0) {
                return Result.error("该菜单下有子菜单，无法删除");
            }

            int result = menuMapper.deleteById(id);
            if (result > 0) {
                return Result.<Void>success();
            } else {
                return Result.<Void>error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除菜单失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean hasPermission(Long userId, String perms) {
        if (userId == null || StrUtil.isBlank(perms)) {
            return false;
        }

        List<String> permissions = getUserPermissions(userId);
        return permissions.contains(perms);
    }

    @Override
    public boolean hasAnyPermission(Long userId, String... perms) {
        if (userId == null || perms == null || perms.length == 0) {
            return false;
        }

        List<String> permissions = getUserPermissions(userId);
        for (String perm : perms) {
            if (permissions.contains(perm)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAllPermissions(Long userId, String... perms) {
        if (userId == null || perms == null || perms.length == 0) {
            return false;
        }

        List<String> permissions = getUserPermissions(userId);
        for (String perm : perms) {
            if (!permissions.contains(perm)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public List<SysMenu> getRoleMenus(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        return menuMapper.selectByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> assignMenusToRole(Long roleId, List<Long> menuIds) {
        try {
            // 先删除角色所有菜单权限
            menuMapper.deleteRoleAllMenus(roleId);

            // 分配新菜单权限
            for (Long menuId : menuIds) {
                menuMapper.insertRoleMenu(roleId, menuId);
            }

            return Result.<Void>success();
        } catch (Exception e) {
            log.error("分配菜单权限失败", e);
            return Result.<Void>error("分配失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> removeMenusFromRole(Long roleId, List<Long> menuIds) {
        try {
            for (Long menuId : menuIds) {
                menuMapper.deleteRoleMenu(roleId, menuId);
            }
            return Result.<Void>success();
        } catch (Exception e) {
            log.error("移除菜单权限失败", e);
            return Result.<Void>error("移除失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        // 按父ID分组
        Map<Long, List<SysMenu>> menuMap = menus.stream()
            .collect(Collectors.groupingBy(m -> m.getParentId() == null ? 0L : m.getParentId()));

        // 构建树结构
        List<SysMenu> rootMenus = menuMap.getOrDefault(0L, new ArrayList<>());
        buildChildren(rootMenus, menuMap);

        return rootMenus;
    }

    /**
     * 递归构建子菜单
     */
    private void buildChildren(List<SysMenu> menus, Map<Long, List<SysMenu>> menuMap) {
        for (SysMenu menu : menus) {
            List<SysMenu> children = menuMap.getOrDefault(menu.getId(), new ArrayList<>());
            menu.setChildren(children);
            if (!children.isEmpty()) {
                buildChildren(children, menuMap);
            }
        }
    }
}
