package org.example.ssoserver.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ssoserver.common.Result;
import org.example.ssoserver.dto.LoginRequest;
import org.example.ssoserver.dto.RegisterRequest;
import org.example.ssoserver.entity.SysUser;
import org.example.ssoserver.mapper.SysUserMapper;
import org.example.ssoserver.service.SysUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {
    
    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<SysUser> register(RegisterRequest request) {
        try {
            // 验证密码一致性
            if (!request.isPasswordMatch()) {
                return Result.error(Result.ResultCode.PASSWORD_NOT_MATCH);
            }
            
            // 检查用户名是否存在
            if (isUsernameExists(request.getUsername())) {
                return Result.error(Result.ResultCode.USERNAME_EXISTS);
            }
            
            // 根据注册方式检查手机号或邮箱是否存在
            if ("phone".equals(request.getRegisterType()) && isPhoneExists(request.getPhone())) {
                return Result.error(Result.ResultCode.PHONE_EXISTS);
            }
            if ("email".equals(request.getRegisterType()) && isEmailExists(request.getEmail())) {
                return Result.error(Result.ResultCode.EMAIL_EXISTS);
            }
            
            // 创建用户对象
            SysUser user = new SysUser();
            user.setUsername(request.getUsername());
            user.setPassword(encodePassword(request.getPassword()));
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());
            user.setRealName(request.getRealName());
            user.setUserType(request.getUserType());
            user.setStatus(1); // 默认启用
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            
            // 保存用户
            int result = userMapper.insert(user);
            if (result > 0) {
                // 清除密码字段
                user.setPassword(null);
                log.info("用户注册成功: {}", user.getUsername());
                return Result.success("注册成功", user);
            } else {
                return Result.error("注册失败");
            }
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error("注册失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> login(LoginRequest request) {
        try {
            // 根据登录类型查找用户
            SysUser user = null;
            switch (request.getLoginType()) {
                case "phone":
                    user = getUserByPhone(request.getAccount());
                    break;
                case "email":
                    user = getUserByEmail(request.getAccount());
                    break;
                default:
                    user = getUserByUsername(request.getAccount());
                    break;
            }
            
            if (user == null) {
                return Result.error(Result.ResultCode.USER_NOT_FOUND);
            }
            
            // 检查用户状态
            if (!user.isEnabled()) {
                return Result.error(Result.ResultCode.USER_DISABLED);
            }
            
            // 验证密码
            if (!verifyPassword(request.getPassword(), user.getPassword())) {
                return Result.error(Result.ResultCode.PASSWORD_ERROR);
            }
            
            // 执行登录
            StpUtil.login(user.getId());
            
            // 更新最后登录信息
            updateLastLoginInfo(user.getId(), request.getClientIp());
            
            // 构建返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("token", StpUtil.getTokenValue());
            result.put("user", user);
            result.put("permissions", getUserPermissions(user.getId()));
            
            log.info("用户登录成功: {}", user.getUsername());
            return Result.success("登录成功", result);
            
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error("登录失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Void> logout(String token) {
        try {
            StpUtil.logout();
            log.info("用户登出成功");
            return Result.<Void>success();
        } catch (Exception e) {
            log.error("用户登出失败", e);
            return Result.error("登出失败: " + e.getMessage());
        }
    }
    
    @Override
    public SysUser getUserById(Long id) {
        if (id == null) {
            return null;
        }
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null); // 清除密码字段
        }
        return user;
    }
    
    @Override
    public SysUser getUserByUsername(String username) {
        if (StrUtil.isBlank(username)) {
            return null;
        }
        return userMapper.selectByUsername(username);
    }
    
    @Override
    public SysUser getUserByPhone(String phone) {
        if (StrUtil.isBlank(phone)) {
            return null;
        }
        return userMapper.selectByPhone(phone);
    }
    
    @Override
    public SysUser getUserByEmail(String email) {
        if (StrUtil.isBlank(email)) {
            return null;
        }
        return userMapper.selectByEmail(email);
    }
    
    @Override
    public boolean isUsernameExists(String username) {
        if (StrUtil.isBlank(username)) {
            return false;
        }
        return userMapper.countByUsername(username) > 0;
    }
    
    @Override
    public boolean isPhoneExists(String phone) {
        if (StrUtil.isBlank(phone)) {
            return false;
        }
        return userMapper.countByPhone(phone) > 0;
    }
    
    @Override
    public boolean isEmailExists(String email) {
        if (StrUtil.isBlank(email)) {
            return false;
        }
        return userMapper.countByEmail(email) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<SysUser> createUser(SysUser user) {
        try {
            // 检查必填字段
            if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
                return Result.error(Result.ResultCode.PARAM_ERROR.getCode(), "用户名和密码不能为空");
            }
            
            // 检查用户名是否存在
            if (isUsernameExists(user.getUsername())) {
                return Result.error(Result.ResultCode.USERNAME_EXISTS);
            }
            
            // 加密密码
            user.setPassword(encodePassword(user.getPassword()));
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            
            int result = userMapper.insert(user);
            if (result > 0) {
                user.setPassword(null);
                return Result.success("创建成功", user);
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<SysUser> updateUser(SysUser user) {
        try {
            if (user.getId() == null) {
                return Result.error(Result.ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
            }
            
            user.setUpdateTime(LocalDateTime.now());
            int result = userMapper.update(user);
            if (result > 0) {
                return Result.success("更新成功", getUserById(user.getId()));
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            SysUser user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error(Result.ResultCode.USER_NOT_FOUND);
            }
            
            // 验证原密码
            if (!verifyPassword(oldPassword, user.getPassword())) {
                return Result.error(Result.ResultCode.OLD_PASSWORD_ERROR);
            }
            
            // 更新密码
            String encodedPassword = encodePassword(newPassword);
            int result = userMapper.updatePassword(userId, encodedPassword, LocalDateTime.now(), userId);
            if (result > 0) {
                return Result.<Void>success();
            } else {
                return Result.<Void>error("密码修改失败");
            }
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error("密码修改失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Void> resetPassword(String account, String newPassword, String verificationCode) {
        // TODO: 实现密码重置逻辑，需要验证验证码
        return Result.error("功能暂未实现");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateUserStatus(Long userId, Integer status) {
        try {
            int result = userMapper.updateStatus(userId, status, LocalDateTime.now(), userId);
            if (result > 0) {
                return Result.<Void>success();
            } else {
                return Result.<Void>error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteUser(Long userId) {
        try {
            int result = userMapper.deleteById(userId);
            if (result > 0) {
                return Result.<Void>success();
            } else {
                return Result.<Void>error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> getUserList(String username, String phone, String email,
                                                  Integer status, Integer userType,
                                                  Integer page, Integer size) {
        try {
            // 计算偏移量
            int offset = (page - 1) * size;
            
            // 查询用户列表
            List<SysUser> users = userMapper.selectPage(username, phone, email, status, userType, offset, size);
            
            // 查询总数
            int total = userMapper.selectCount(username, phone, email, status, userType);
            
            // 清除密码字段
            users.forEach(user -> user.setPassword(null));
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", users);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    @Override
    public void updateLastLoginInfo(Long userId, String loginIp) {
        try {
            userMapper.updateLastLogin(userId, LocalDateTime.now(), loginIp);
        } catch (Exception e) {
            log.error("更新最后登录信息失败", e);
        }
    }
    
    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * 获取用户权限列表
     */
    private List<String> getUserPermissions(Long userId) {
        // TODO: 实现获取用户权限逻辑
        return List.of();
    }
}
