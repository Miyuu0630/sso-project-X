package org.example.ssoclient.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息DTO（客户端专用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoDTO {
    
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
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
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
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 用户角色列表
     */
    private List<String> roles;
    
    /**
     * 用户权限列表
     */
    private List<String> permissions;
    
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
        return "enterprise".equals(userType) || (roles != null && roles.contains("ENTERPRISE_USER"));
    }
    
    /**
     * 是否为航司用户
     */
    public boolean isAirlineUser() {
        return "airline".equals(userType) || (roles != null && roles.contains("AIRLINE_USER"));
    }
    
    /**
     * 是否为个人用户
     */
    public boolean isPersonalUser() {
        return "normal".equals(userType) || (roles != null && roles.contains("PERSONAL_USER"));
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
        if (realName != null && !realName.trim().isEmpty()) {
            return realName;
        }
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        return username;
    }
    
    /**
     * 获取角色列表
     */
    public List<String> getRoles() {
        return roles;
    }
    
    /**
     * 获取权限列表
     */
    public List<String> getPermissions() {
        return permissions;
    }
}
