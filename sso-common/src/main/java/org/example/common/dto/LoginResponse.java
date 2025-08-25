package org.example.common.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应DTO
 * 统一的登录成功响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * Token过期时间（秒）
     */
    private Long expiresIn;

    /**
     * Refresh Token过期时间（秒）
     */
    private Long refreshTokenExpiresIn;

    /**
     * Token过期时间戳
     */
    private Long expireTime;

    /**
     * 用户基本信息
     */
    private Long userId;
    private String username;
    private String nickname;
    private String realName;
    private String avatar;
    private String userType;
    private String userTypeDesc;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 用户权限列表
     */
    private List<String> permissions;

    /**
     * 登录相关信息
     */
    private Boolean isNewDevice;
    private String deviceType;
    private String loginLocation;

    /**
     * 登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    /**
     * SSO相关字段
     */
    private String redirectUri; // 重定向地址
    private String state; // 状态码
    private String ticket; // SSO票据

    /**
     * 安全提醒
     */
    private List<String> securityWarnings;

    /**
     * 第三方绑定信息
     */
    private List<String> boundProviders;

    /**
     * 是否需要完善信息
     */
    private Boolean needCompleteInfo;

    /**
     * 需要完善的信息类型
     */
    private List<String> incompleteFields;

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
     * 是否有安全警告
     */
    public boolean hasSecurityWarnings() {
        return securityWarnings != null && !securityWarnings.isEmpty();
    }

    /**
     * 是否需要完善信息
     */
    public boolean needsCompleteInfo() {
        return needCompleteInfo != null && needCompleteInfo;
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
