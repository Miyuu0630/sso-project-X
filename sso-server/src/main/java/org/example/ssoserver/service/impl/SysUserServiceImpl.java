package org.example.ssoserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.dto.LoginRequest;
import org.example.common.dto.LoginResponse;
import org.example.common.model.UserDTO;
import org.example.common.model.PageResult;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResultCode;
import org.example.common.util.EncryptUtil;
import org.example.common.util.DesensitizeUtil;
import org.example.common.enums.UserType;
import org.example.common.enums.Gender;
import org.example.ssoserver.entity.SysUser;
import org.example.ssoserver.mapper.SysUserMapper;
import org.example.ssoserver.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {
    
    private final SysUserMapper userMapper;
    
    // ========================================
    // 用户认证相关
    // ========================================
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 这个方法应该由AuthService处理
        throw new UnsupportedOperationException("登录功能由AuthService处理");
    }
    
    @Override
    public boolean logout(Long userId) {
        // 这个方法应该由AuthService处理
        throw new UnsupportedOperationException("登出功能由AuthService处理");
    }
    
    @Override
    public SysUser validateUser(String account, String password) {
        try {
            // 根据账号查询用户
            SysUser user = getUserByAccount(account);
            if (user == null) {
                return null;
            }
            
            // 验证密码
            if (EncryptUtil.verifyPassword(password, user.getPassword())) {
                return user;
            }
            
            // 记录登录失败
            recordLoginFailure(user.getId());
            return null;
        } catch (Exception e) {
            log.error("验证用户失败: account={}", account, e);
            return null;
        }
    }
    
    @Override
    public SysUser validateUserByCode(String account, String verificationCode, String loginType) {
        try {
            // TODO: 实现验证码验证逻辑
            SysUser user = getUserByAccount(account);
            if (user == null) {
                return null;
            }
            
            // 这里应该验证验证码，暂时返回用户
            return user;
        } catch (Exception e) {
            log.error("验证码验证失败: account={}, loginType={}", account, loginType, e);
            return null;
        }
    }
    
    // ========================================
    // 用户查询相关
    // ========================================
    
    @Override
    public SysUser getUserById(Long id) {
        try {
            return userMapper.selectById(id);
        } catch (Exception e) {
            log.error("根据ID查询用户失败: id={}", id, e);
            return null;
        }
    }
    
    @Override
    public SysUser getUserByUsername(String username) {
        try {
            return userMapper.selectByUsername(username);
        } catch (Exception e) {
            log.error("根据用户名查询用户失败: username={}", username, e);
            return null;
        }
    }
    
    @Override
    public SysUser getUserByPhone(String phone) {
        try {
            return userMapper.selectByPhone(phone);
        } catch (Exception e) {
            log.error("根据手机号查询用户失败: phone={}", phone, e);
            return null;
        }
    }
    
    @Override
    public SysUser getUserByEmail(String email) {
        try {
            return userMapper.selectByEmail(email);
        } catch (Exception e) {
            log.error("根据邮箱查询用户失败: email={}", email, e);
            return null;
        }
    }
    
    @Override
    public SysUser getUserByAccount(String account) {
        try {
            return userMapper.selectByAccount(account);
        } catch (Exception e) {
            log.error("根据账号查询用户失败: account={}", account, e);
            return null;
        }
    }
    
    @Override
    public UserDTO convertToDTO(SysUser user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .realName(user.getRealName())
                .phone(DesensitizeUtil.desensitizePhone(user.getPhone()))
                .email(DesensitizeUtil.desensitizeEmail(user.getEmail()))
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .genderDesc(getGenderDesc(user.getGender()))
                .birthday(user.getBirthday())
                .status(user.getStatus())
                .statusDesc(user.isEnabled() ? "正常" : "禁用")
                .userType(user.getUserType())
                .userTypeDesc(getUserTypeDesc(user.getUserType()))
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .loginCount(user.getLoginCount())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .remark(user.getRemark())
                .build();
    }
    
    @Override
    public List<UserDTO> convertToDTO(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // ========================================
    // 用户管理相关
    // ========================================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(SysUser user) {
        try {
            // 加密密码
            if (user.getPassword() != null) {
                user.setPassword(EncryptUtil.encryptPassword(user.getPassword()));
            }
            
            // 设置默认值
            if (user.getStatus() == null) {
                user.setStatus("1");
            }
            if (user.getUserType() == null) {
                user.setUserType("normal");
            }
            
            int result = userMapper.insert(user);
            return result > 0;
        } catch (Exception e) {
            log.error("创建用户失败: username={}", user.getUsername(), e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        try {
            // 不更新密码字段
            user.setPassword(null);
            
            int result = userMapper.updateById(user);
            return result > 0;
        } catch (Exception e) {
            log.error("更新用户失败: id={}", user.getId(), e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        try {
            // 逻辑删除：设置状态为禁用
            SysUser user = new SysUser();
            user.setId(userId);
            user.setStatus("0");
            user.setUpdateTime(LocalDateTime.now());
            
            int result = userMapper.updateById(user);
            return result > 0;
        } catch (Exception e) {
            log.error("删除用户失败: userId={}", userId, e);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long userId, String status) {
        try {
            SysUser user = new SysUser();
            user.setId(userId);
            user.setStatus(status);
            user.setUpdateTime(LocalDateTime.now());
            
            int result = userMapper.updateById(user);
            return result > 0;
        } catch (Exception e) {
            log.error("更新用户状态失败: userId={}, status={}", userId, status, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        try {
            String encryptedPassword = EncryptUtil.encryptPassword(newPassword);
            int result = userMapper.updatePassword(userId, encryptedPassword, LocalDateTime.now());
            return result > 0;
        } catch (Exception e) {
            log.error("重置密码失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            // 验证旧密码
            SysUser user = getUserById(userId);
            if (user == null || !EncryptUtil.verifyPassword(oldPassword, user.getPassword())) {
                throw BusinessException.passwordError();
            }

            // 更新新密码
            String encryptedPassword = EncryptUtil.encryptPassword(newPassword);
            int result = userMapper.updatePassword(userId, encryptedPassword, LocalDateTime.now());
            return result > 0;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改密码失败: userId={}", userId, e);
            return false;
        }
    }

    // ========================================
    // 用户安全相关
    // ========================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLoginInfo(Long userId, String loginIp) {
        try {
            int result = userMapper.updateLoginInfo(userId, loginIp, LocalDateTime.now());
            return result > 0;
        } catch (Exception e) {
            log.error("更新登录信息失败: userId={}, loginIp={}", userId, loginIp, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordLoginFailure(Long userId) {
        try {
            int result = userMapper.updateLoginFailInfo(userId, LocalDateTime.now());
            return result > 0;
        } catch (Exception e) {
            log.error("记录登录失败失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockUser(Long userId, String reason) {
        try {
            int result = userMapper.lockUser(userId, LocalDateTime.now());
            return result > 0;
        } catch (Exception e) {
            log.error("锁定用户失败: userId={}, reason={}", userId, reason, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlockUser(Long userId) {
        try {
            int result = userMapper.unlockUser(userId);
            return result > 0;
        } catch (Exception e) {
            log.error("解锁用户失败: userId={}", userId, e);
            return false;
        }
    }

    @Override
    public boolean canUserLogin(SysUser user) {
        return user != null && user.canLogin();
    }

    // ========================================
    // 用户验证相关
    // ========================================

    @Override
    public boolean isUsernameExists(String username, Long excludeId) {
        try {
            Long id = excludeId != null ? excludeId : 0L;
            int count = userMapper.checkUsernameExists(username, id);
            return count > 0;
        } catch (Exception e) {
            log.error("检查用户名是否存在失败: username={}", username, e);
            return false;
        }
    }

    @Override
    public boolean isPhoneExists(String phone, Long excludeId) {
        try {
            Long id = excludeId != null ? excludeId : 0L;
            int count = userMapper.checkPhoneExists(phone, id);
            return count > 0;
        } catch (Exception e) {
            log.error("检查手机号是否存在失败: phone={}", phone, e);
            return false;
        }
    }

    @Override
    public boolean isEmailExists(String email, Long excludeId) {
        try {
            Long id = excludeId != null ? excludeId : 0L;
            int count = userMapper.checkEmailExists(email, id);
            return count > 0;
        } catch (Exception e) {
            log.error("检查邮箱是否存在失败: email={}", email, e);
            return false;
        }
    }

    // ========================================
    // 分页查询相关
    // ========================================

    @Override
    public PageResult<UserDTO> getUserPage(Integer pageNum, Integer pageSize, String userType, String status, String keyword) {
        try {
            Page<SysUser> page = new Page<>(pageNum, pageSize);
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();

            // 添加查询条件
            if (userType != null && !userType.trim().isEmpty()) {
                queryWrapper.eq("user_type", userType);
            }
            if (status != null && !status.trim().isEmpty()) {
                queryWrapper.eq("status", status);
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                        .like("username", keyword)
                        .or().like("nickname", keyword)
                        .or().like("real_name", keyword)
                        .or().like("phone", keyword)
                        .or().like("email", keyword));
            }

            queryWrapper.orderByDesc("create_time");

            Page<SysUser> result = userMapper.selectPage(page, queryWrapper);
            List<UserDTO> userDTOs = convertToDTO(result.getRecords());

            return PageResult.of((int) result.getCurrent(), (int) result.getSize(), result.getTotal(), userDTOs);
        } catch (Exception e) {
            log.error("分页查询用户失败", e);
            return PageResult.empty(pageNum, pageSize);
        }
    }

    @Override
    public List<UserDTO> getUsersByRole(Long roleId) {
        try {
            List<SysUser> users = userMapper.selectByRoleId(roleId);
            return convertToDTO(users);
        } catch (Exception e) {
            log.error("根据角色查询用户失败: roleId={}", roleId, e);
            return List.of();
        }
    }

    // ========================================
    // 统计相关
    // ========================================

    @Override
    public long countUsers(String userType, String status) {
        try {
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
            if (userType != null && !userType.trim().isEmpty()) {
                queryWrapper.eq("user_type", userType);
            }
            if (status != null && !status.trim().isEmpty()) {
                queryWrapper.eq("status", status);
            }

            return userMapper.selectCount(queryWrapper);
        } catch (Exception e) {
            log.error("统计用户数量失败: userType={}, status={}", userType, status, e);
            return 0;
        }
    }

    @Override
    public List<SysUser> getUsersToUnlock(int lockMinutes) {
        try {
            return userMapper.selectUsersToUnlock(lockMinutes);
        } catch (Exception e) {
            log.error("获取需要解锁的用户失败: lockMinutes={}", lockMinutes, e);
            return List.of();
        }
    }

    @Override
    public List<SysUser> getUsersWithExpiredPassword(int days) {
        try {
            return userMapper.selectUsersWithExpiredPassword(days);
        } catch (Exception e) {
            log.error("获取密码过期用户失败: days={}", days, e);
            return List.of();
        }
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    /**
     * 获取性别描述
     */
    private String getGenderDesc(Integer gender) {
        if (gender == null) {
            return Gender.UNKNOWN.getName();
        }

        for (Gender g : Gender.values()) {
            if (g.getCode().equals(gender)) {
                return g.getName();
            }
        }
        return Gender.UNKNOWN.getName();
    }

    /**
     * 获取用户类型描述
     */
    private String getUserTypeDesc(String userType) {
        if (userType == null) {
            return UserType.NORMAL.getName();
        }

        UserType type = UserType.fromCode(userType);
        return type != null ? type.getName() : UserType.NORMAL.getName();
    }
}
