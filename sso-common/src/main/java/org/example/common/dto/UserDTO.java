package org.example.common.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 * 用户信息DTO
 * 用于前后端数据传输和模块间数据交换
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 手机号（脱敏处理）
     */
    private String phone;
    
    /**
     * 邮箱（脱敏处理）
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;
    
    /**
     * 性别描述
     */
    private String genderDesc;
    
    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    
    /**
     * 状态：0-禁用，1-正常
     */
    private String status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 用户类型：normal-普通用户，enterprise-企业用户，airline-航司用户
     */
    private String userType;
    
    /**
     * 用户类型描述
     */
    private String userTypeDesc;
    
    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    
    /**
     * 登录次数
     */
    private Integer loginCount;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 用户角色列表
     */
    private List<String> roles;
    
    /**
     * 用户权限列表
     */
    private List<String> permissions;
    
    /**
     * 是否为新设备登录
     */
    private Boolean isNewDevice;
    
    /**
     * 活跃设备数量
     */
    private Integer activeDeviceCount;
    
    /**
     * 第三方绑定平台列表
     */
    private List<String> boundProviders;
    
    /**
     * 备注
     */
    private String remark;
    
    // ========================================
    // 业务方法
    // ========================================
    
    /**
     * 是否为管理员
     */
    public boolean isAdmin() {
        return roles != null && roles.contains("ADMIN");
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
     * 是否账号正常
     */
    public boolean isEnabled() {
        return "1".equals(status);
    }
    
    /**
     * 获取显示名称（优先昵称，其次真实姓名，最后用户名）
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
     * 检查是否有指定权限
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * 检查是否有指定角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
