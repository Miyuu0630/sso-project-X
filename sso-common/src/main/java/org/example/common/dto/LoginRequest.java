package org.example.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    
    /**
     * 登录账号(用户名/手机号/邮箱)
     */
    @NotBlank(message = "登录账号不能为空")
    private String account;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 登录类型: username, phone, email
     */
    private String loginType = "username";
    
    /**
     * 验证码
     */
    private String captcha;
    
    /**
     * 验证码key
     */
    private String captchaKey;
    
    /**
     * 记住我
     */
    private Boolean rememberMe = false;
    
    /**
     * 设备信息
     */
    private String deviceInfo;
    
    /**
     * 客户端IP
     */
    private String clientIp;
    
    /**
     * User-Agent
     */
    private String userAgent;
}
