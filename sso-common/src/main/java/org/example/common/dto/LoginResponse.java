package org.example.common.dto;

import lombok.Data;
import java.util.List;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    
    /**
     * 访问令牌
     */
    private String token;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 用户角色列表
     */
    private List<String> roles;
    
    /**
     * 用户权限列表
     */
    private List<String> permissions;
    
    /**
     * Token过期时间（时间戳）
     */
    private Long expireTime;
    
    /**
     * 是否为新设备登录
     */
    private Boolean isNewDevice;
}
