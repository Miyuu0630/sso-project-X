package org.example.ssoserver.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.model.ApiResponse;
import org.example.common.model.UserDTO;
import org.example.common.model.PageResult;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResultCode;
import org.example.ssoserver.entity.SysUser;
import org.example.ssoserver.service.SysUserService;
import org.example.ssoserver.service.AuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 提供用户管理相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {
    
    private final SysUserService userService;
    private final AuthService authService;
    
    // ========================================
    // 用户查询接口
    // ========================================
    
    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    @SaCheckPermission("system:user:list")
    @Operation(summary = "分页查询用户", description = "分页查询用户列表，支持条件筛选")
    public ApiResponse<PageResult<UserDTO>> getUserPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        try {
            PageResult<UserDTO> result = userService.getUserPage(pageNum, pageSize, userType, status, keyword);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("分页查询用户异常", e);
            return ApiResponse.error("查询用户列表失败");
        }
    }
    
    /**
     * 根据ID查询用户详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("system:user:query")
    @Operation(summary = "查询用户详情", description = "根据用户ID查询用户详细信息")
    public ApiResponse<UserDTO> getUserById(@PathVariable @NotNull Long id) {
        try {
            SysUser user = userService.getUserById(id);
            if (user != null) {
                UserDTO userDTO = userService.convertToDTO(user);
                // 获取用户权限信息
                userDTO.setRoles(authService.getUserRoles(id));
                userDTO.setPermissions(authService.getUserPermissions(id));
                
                return ApiResponse.success(userDTO);
            } else {
                return ApiResponse.<UserDTO>error(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }
        } catch (Exception e) {
            log.error("查询用户详情异常: id={}", id, e);
            return ApiResponse.error("查询用户详情失败");
        }
    }
    
    /**
     * 根据用户名查询用户
     */
    @GetMapping("/username/{username}")
    @SaCheckPermission("system:user:query")
    @Operation(summary = "根据用户名查询", description = "根据用户名查询用户信息")
    public ApiResponse<UserDTO> getUserByUsername(@PathVariable @NotBlank String username) {
        try {
            SysUser user = userService.getUserByUsername(username);
            if (user != null) {
                UserDTO userDTO = userService.convertToDTO(user);
                return ApiResponse.success(userDTO);
            } else {
                return ApiResponse.<UserDTO>error(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }
        } catch (Exception e) {
            log.error("根据用户名查询用户异常: username={}", username, e);
            return ApiResponse.error("查询用户失败");
        }
    }
    
    // ========================================
    // 用户管理接口
    // ========================================
    
    /**
     * 创建用户
     */
    @PostMapping
    @SaCheckPermission("system:user:add")
    @Operation(summary = "创建用户", description = "创建新用户")
    public ApiResponse<Void> createUser(@Valid @RequestBody SysUser user) {
        try {
            // 检查用户名是否存在
            if (userService.isUsernameExists(user.getUsername(), null)) {
                return ApiResponse.error(ResultCode.USERNAME_EXISTS);
            }
            
            // 检查手机号是否存在
            if (user.getPhone() != null && userService.isPhoneExists(user.getPhone(), null)) {
                return ApiResponse.error(ResultCode.PHONE_EXISTS);
            }
            
            // 检查邮箱是否存在
            if (user.getEmail() != null && userService.isEmailExists(user.getEmail(), null)) {
                return ApiResponse.error(ResultCode.EMAIL_EXISTS);
            }
            
            // 设置创建人
            user.setCreateBy(StpUtil.getLoginIdAsLong());
            
            boolean success = userService.createUser(user);
            if (success) {
                log.info("创建用户成功: username={}, createBy={}", user.getUsername(), user.getCreateBy());
                return ApiResponse.success("用户创建成功");
            } else {
                return ApiResponse.error("用户创建失败");
            }
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建用户异常: username={}", user.getUsername(), e);
            return ApiResponse.error("用户创建失败");
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public ApiResponse<Void> updateUser(@PathVariable @NotNull Long id, @Valid @RequestBody SysUser user) {
        try {
            // 检查用户是否存在
            SysUser existingUser = userService.getUserById(id);
            if (existingUser == null) {
                return ApiResponse.<Void>error(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }
            
            // 检查用户名是否重复
            if (!existingUser.getUsername().equals(user.getUsername()) && 
                userService.isUsernameExists(user.getUsername(), id)) {
                return ApiResponse.error(ResultCode.USERNAME_EXISTS);
            }
            
            // 检查手机号是否重复
            if (user.getPhone() != null && !user.getPhone().equals(existingUser.getPhone()) && 
                userService.isPhoneExists(user.getPhone(), id)) {
                return ApiResponse.error(ResultCode.PHONE_EXISTS);
            }
            
            // 检查邮箱是否重复
            if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail()) && 
                userService.isEmailExists(user.getEmail(), id)) {
                return ApiResponse.error(ResultCode.EMAIL_EXISTS);
            }
            
            // 设置更新信息
            user.setId(id);
            user.setUpdateBy(StpUtil.getLoginIdAsLong());
            
            boolean success = userService.updateUser(user);
            if (success) {
                log.info("更新用户成功: id={}, updateBy={}", id, user.getUpdateBy());
                return ApiResponse.success("用户更新成功");
            } else {
                return ApiResponse.error("用户更新失败");
            }
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新用户异常: id={}", id, e);
            return ApiResponse.error("用户更新失败");
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:remove")
    @Operation(summary = "删除用户", description = "删除用户（逻辑删除）")
    public ApiResponse<Void> deleteUser(@PathVariable @NotNull Long id) {
        try {
            // 检查是否为当前登录用户
            Long currentUserId = StpUtil.getLoginIdAsLong();
            if (id.equals(currentUserId)) {
                return ApiResponse.error("不能删除当前登录用户");
            }
            
            // 检查是否为超级管理员
            if (id.equals(1L)) {
                return ApiResponse.error("不能删除超级管理员");
            }
            
            boolean success = userService.deleteUser(id);
            if (success) {
                log.info("删除用户成功: id={}, deleteBy={}", id, currentUserId);
                return ApiResponse.success("用户删除成功");
            } else {
                return ApiResponse.error("用户删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户异常: id={}", id, e);
            return ApiResponse.error("用户删除失败");
        }
    }

    // ========================================
    // 用户状态管理接口
    // ========================================

    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/status")
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    public ApiResponse<Void> updateUserStatus(@PathVariable @NotNull Long id,
                                             @RequestParam @NotBlank String status) {
        try {
            // 检查状态值
            if (!"0".equals(status) && !"1".equals(status)) {
                return ApiResponse.error("状态值无效");
            }

            // 检查是否为当前登录用户
            Long currentUserId = StpUtil.getLoginIdAsLong();
            if (id.equals(currentUserId) && "0".equals(status)) {
                return ApiResponse.error("不能禁用当前登录用户");
            }

            boolean success = userService.updateUserStatus(id, status);
            if (success) {
                String action = "1".equals(status) ? "启用" : "禁用";
                log.info("{}用户成功: id={}, operateBy={}", action, id, currentUserId);
                return ApiResponse.success("用户" + action + "成功");
            } else {
                return ApiResponse.error("用户状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户状态异常: id={}, status={}", id, status, e);
            return ApiResponse.error("用户状态更新失败");
        }
    }

    /**
     * 锁定用户
     */
    @PutMapping("/{id}/lock")
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "锁定用户", description = "锁定用户账号")
    public ApiResponse<Void> lockUser(@PathVariable @NotNull Long id,
                                     @RequestParam(required = false) String reason) {
        try {
            // 检查是否为当前登录用户
            Long currentUserId = StpUtil.getLoginIdAsLong();
            if (id.equals(currentUserId)) {
                return ApiResponse.error("不能锁定当前登录用户");
            }

            boolean success = userService.lockUser(id, reason != null ? reason : "管理员锁定");
            if (success) {
                log.info("锁定用户成功: id={}, reason={}, operateBy={}", id, reason, currentUserId);
                return ApiResponse.success("用户锁定成功");
            } else {
                return ApiResponse.error("用户锁定失败");
            }
        } catch (Exception e) {
            log.error("锁定用户异常: id={}", id, e);
            return ApiResponse.error("用户锁定失败");
        }
    }

    /**
     * 解锁用户
     */
    @PutMapping("/{id}/unlock")
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "解锁用户", description = "解锁用户账号")
    public ApiResponse<Void> unlockUser(@PathVariable @NotNull Long id) {
        try {
            boolean success = userService.unlockUser(id);
            if (success) {
                Long currentUserId = StpUtil.getLoginIdAsLong();
                log.info("解锁用户成功: id={}, operateBy={}", id, currentUserId);
                return ApiResponse.success("用户解锁成功");
            } else {
                return ApiResponse.error("用户解锁失败");
            }
        } catch (Exception e) {
            log.error("解锁用户异常: id={}", id, e);
            return ApiResponse.error("用户解锁失败");
        }
    }

    // ========================================
    // 密码管理接口
    // ========================================

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/reset-password")
    @SaCheckPermission("system:user:resetPwd")
    @Operation(summary = "重置密码", description = "管理员重置用户密码")
    public ApiResponse<Void> resetPassword(@PathVariable @NotNull Long id,
                                          @RequestParam(required = false) String newPassword) {
        try {
            // 如果没有提供新密码，使用默认密码
            if (newPassword == null || newPassword.trim().isEmpty()) {
                newPassword = "123456"; // 默认密码
            }

            boolean success = userService.resetPassword(id, newPassword);
            if (success) {
                Long currentUserId = StpUtil.getLoginIdAsLong();
                log.info("重置用户密码成功: id={}, operateBy={}", id, currentUserId);
                return ApiResponse.success("密码重置成功");
            } else {
                return ApiResponse.error("密码重置失败");
            }
        } catch (Exception e) {
            log.error("重置用户密码异常: id={}", id, e);
            return ApiResponse.error("密码重置失败");
        }
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/change-password")
    @Operation(summary = "修改密码", description = "用户修改自己的密码")
    public ApiResponse<Void> changePassword(@RequestParam @NotBlank String oldPassword,
                                           @RequestParam @NotBlank String newPassword) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean success = userService.changePassword(userId, oldPassword, newPassword);
            if (success) {
                log.info("用户修改密码成功: userId={}", userId);
                return ApiResponse.success("密码修改成功");
            } else {
                return ApiResponse.error("原密码错误");
            }
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("修改密码异常", e);
            return ApiResponse.error("密码修改失败");
        }
    }

    // ========================================
    // 统计查询接口
    // ========================================

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    @SaCheckPermission("system:user:list")
    @Operation(summary = "用户统计", description = "获取用户统计信息")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总用户数
            statistics.put("totalUsers", userService.countUsers(null, null));

            // 正常用户数
            statistics.put("activeUsers", userService.countUsers(null, "1"));

            // 禁用用户数
            statistics.put("disabledUsers", userService.countUsers(null, "0"));

            // 各类型用户数
            statistics.put("normalUsers", userService.countUsers("normal", "1"));
            statistics.put("enterpriseUsers", userService.countUsers("enterprise", "1"));
            statistics.put("airlineUsers", userService.countUsers("airline", "1"));

            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("获取用户统计信息异常", e);
            return ApiResponse.error("获取统计信息失败");
        }
    }
}
