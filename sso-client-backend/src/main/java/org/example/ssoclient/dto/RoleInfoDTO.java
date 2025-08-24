package org.example.ssoclient.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色信息DTO
 */
@Data
public class RoleInfoDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户所有角色
     */
    private List<String> roles;
    
    /**
     * 主要角色（权限最高的角色）
     */
    private String primaryRole;
    
    /**
     * 角色层级（数字越小权限越高）
     */
    private Integer roleLevel;
    
    /**
     * 对应的仪表板路径
     */
    private String dashboardPath;
    
    /**
     * 用户权限列表
     */
    private List<String> permissions;
    
    /**
     * 角色描述
     */
    private String roleDescription;
    
    /**
     * 是否为管理员
     */
    public boolean isAdmin() {
        return "ADMIN".equals(primaryRole);
    }
    
    /**
     * 是否为航司用户
     */
    public boolean isAirlineUser() {
        return "AIRLINE_USER".equals(primaryRole);
    }
    
    /**
     * 是否为企业用户
     */
    public boolean isEnterpriseUser() {
        return "ENTERPRISE_USER".equals(primaryRole);
    }
    
    /**
     * 是否为个人用户
     */
    public boolean isPersonalUser() {
        return "PERSONAL_USER".equals(primaryRole);
    }
}
