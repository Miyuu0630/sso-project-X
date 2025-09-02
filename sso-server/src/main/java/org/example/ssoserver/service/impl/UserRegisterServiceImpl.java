package org.example.ssoserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResultCode;
import org.example.ssoserver.dto.RegisterRequest;
import org.example.ssoserver.dto.RegisterResponse;
import org.example.ssoserver.entity.SysUser;
import org.example.ssoserver.entity.SysUserRole;
import org.example.ssoserver.mapper.SysUserMapper;
import org.example.ssoserver.mapper.SysUserRoleMapper;
import org.example.ssoserver.service.PasswordService;
import org.example.ssoserver.service.UserRegisterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户注册服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegisterServiceImpl implements UserRegisterService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordService passwordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest registerRequest) {
        log.info("开始用户注册: username={}, phone={}, email={}", 
                registerRequest.getUsername(), registerRequest.getPhone(), registerRequest.getEmail());

        try {
            // 1. 验证注册数据
            validateRegisterData(registerRequest);

            // 2. 创建用户实体
            SysUser user = buildUserFromRequest(registerRequest);

            // 3. 保存用户
            int result = sysUserMapper.insert(user);
            if (result <= 0) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户注册失败");
            }

            // 4. 分配默认角色
            assignDefaultRole(user.getId(), registerRequest.getUserType());

            // 5. 构建响应
            RegisterResponse response = buildRegisterResponse(user);

            log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
            return response;

        } catch (BusinessException e) {
            log.warn("用户注册失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("用户注册异常: username={}", registerRequest.getUsername(), e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "注册失败，请稍后重试");
        }
    }

    @Override
    public boolean isUsernameExists(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return sysUserMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean isPhoneExists(String phone) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, phone);
        return sysUserMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean isEmailExists(String email) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, email);
        return sysUserMapper.selectCount(wrapper) > 0;
    }

    @Override
    public void validateRegisterData(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (isUsernameExists(registerRequest.getUsername())) {
            throw BusinessException.usernameExists();
        }

        // 检查手机号是否已存在
        if (isPhoneExists(registerRequest.getPhone())) {
            throw BusinessException.phoneExists();
        }

        // 检查邮箱是否已存在
        if (isEmailExists(registerRequest.getEmail())) {
            throw BusinessException.emailExists();
        }

        // 验证密码强度
        validatePasswordStrength(registerRequest.getPassword());
    }

    /**
     * 验证密码强度
     */
    private void validatePasswordStrength(String password) {
        // 使用密码服务进行验证
        if (!passwordService.isValidPassword(password)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "密码格式不符合要求：至少6位，包含字母和数字");
        }

        // 检查是否为弱密码
        if (passwordService.isWeakPassword(password)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "密码过于简单，请使用更复杂的密码");
        }

        // 检查密码强度
        int strength = passwordService.checkPasswordStrength(password);
        if (strength == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "密码强度过低，请使用包含大小写字母、数字和特殊字符的密码");
        }
    }

    /**
     * 从注册请求构建用户实体
     */
    private SysUser buildUserFromRequest(RegisterRequest registerRequest) {
        // 生成随机盐值
        String salt = passwordService.generateSalt();

        // 使用MD5+盐值加密密码
        String encryptedPassword = passwordService.encodePasswordWithSalt(registerRequest.getPassword(), salt);

        return SysUser.builder()
                .username(registerRequest.getUsername())
                .password(encryptedPassword)
                .salt(salt)  // 存储盐值
                .realName(registerRequest.getRealName())
                .nickname(registerRequest.getNickname())
                .phone(registerRequest.getPhone())
                .email(registerRequest.getEmail())
                .gender(registerRequest.getGender())
                .userType(registerRequest.getUserType())
                .status("1") // 默认启用
                .loginCount(0)
                .failedLoginCount(0)
                .isLocked(0)
                .passwordUpdateTime(LocalDateTime.now())
                .remark(registerRequest.getRemark())
                .build();
    }

    /**
     * 分配默认角色
     */
    private void assignDefaultRole(Long userId, String userType) {
        try {
            // 根据用户类型分配不同的默认角色
            Long roleId = getDefaultRoleId(userType);
            if (roleId != null) {
                SysUserRole userRole = SysUserRole.builder()
                        .userId(userId)
                        .roleId(roleId)
                        .build();
                sysUserRoleMapper.insert(userRole);
                log.info("为用户分配默认角色: userId={}, roleId={}", userId, roleId);
            }
        } catch (Exception e) {
            log.warn("分配默认角色失败: userId={}, userType={}", userId, userType, e);
            // 不抛出异常，角色分配失败不影响注册
        }
    }

    /**
     * 获取默认角色ID
     */
    private Long getDefaultRoleId(String userType) {
        // 根据用户类型分配对应的角色ID
        switch (userType) {
            case "normal":
                return 2L; // 个人用户角色 (ID=2, PERSONAL_USER)
            case "enterprise":
                return 3L; // 企业用户角色 (ID=3, ENTERPRISE_USER)
            case "airline":
                return 4L; // 航司用户角色 (ID=4, AIRLINE_USER)
            case "admin":
                return 1L; // 管理员角色 (ID=1, ADMIN)
            default:
                return 2L; // 默认个人用户角色
        }
    }

    /**
     * 构建注册响应
     */
    private RegisterResponse buildRegisterResponse(SysUser user) {
        return RegisterResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(RegisterResponse.maskPhone(user.getPhone()))
                .email(RegisterResponse.maskEmail(user.getEmail()))
                .userType(user.getUserType())
                .createTime(user.getCreateTime())
                .needActivation(false) // 暂时不需要激活
                .message("注册成功，请使用用户名和密码登录")
                .build();
    }
}
