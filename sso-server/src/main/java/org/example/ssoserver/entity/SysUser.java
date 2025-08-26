package org.example.ssoserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 用户信息实体类
 * 对应数据库表：sys_user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class SysUser {
    
    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名
     */
    @TableField("username")
    private String username;
    
    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 密码盐值
     */
    @TableField("salt")
    private String salt;
    
    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;
    
    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;
    
    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;
    
    /**
     * 邮箱
     */
    @TableField("email")
    private String email;
    
    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;
    
    /**
     * 性别：0-未知，1-男，2-女
     */
    @TableField("gender")
    private Integer gender;
    
    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDate birthday;
    
    /**
     * 状态：0-禁用，1-正常
     */
    @TableField("status")
    private String status;
    
    /**
     * 用户类型：normal-普通用户，enterprise-企业用户，airline-航司用户
     */
    @TableField("user_type")
    private String userType;
    
    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;
    
    /**
     * 登录次数
     */
    @TableField("login_count")
    private Integer loginCount;
    
    /**
     * 密码更新时间
     */
    @TableField("password_update_time")
    private LocalDateTime passwordUpdateTime;
    
    /**
     * 连续登录失败次数
     */
    @TableField("failed_login_count")
    private Integer failedLoginCount;
    
    /**
     * 最后失败登录时间
     */
    @TableField("last_failed_login_time")
    private LocalDateTime lastFailedLoginTime;
    
    /**
     * 是否锁定：0-否，1-是
     */
    @TableField("is_locked")
    private Integer isLocked;
    
    /**
     * 锁定时间
     */
    @TableField("lock_time")
    private LocalDateTime lockTime;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;
    
    /**
     * 更新人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    
    // ========================================
    // 业务方法
    // ========================================
    
    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return "1".equals(status);
    }
    
    /**
     * 是否锁定
     */
    public boolean isLocked() {
        return isLocked != null && isLocked == 1;
    }
    
    /**
     * 是否为企业用户
     */
    public boolean isEnterpriseUser() {
        return "enterprise".equals(userType);
    }
    
    /**
     * 是否为航司用户
     */
    public boolean isAirlineUser() {
        return "airline".equals(userType);
    }
    
    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        if (realName != null && !realName.trim().isEmpty()) {
            return realName;
        }
        return username;
    }
    
    /**
     * 是否需要重置密码
     */
    public boolean needResetPassword() {
        if (passwordUpdateTime == null) {
            return true;
        }
        // 密码超过90天未更新
        return passwordUpdateTime.isBefore(LocalDateTime.now().minusDays(90));
    }
    
    /**
     * 是否可以登录
     */
    public boolean canLogin() {
        return isEnabled() && !isLocked();
    }
}
