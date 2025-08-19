package org.example.ssoserver.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
    
    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 真实姓名
     */
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;
    
    /**
     * 用户类型:1-个人用户,2-企业用户,3-航司用户
     */
    @NotNull(message = "用户类型不能为空")
    @Min(value = 1, message = "用户类型值不正确")
    @Max(value = 3, message = "用户类型值不正确")
    private Integer userType;
    
    /**
     * 手机验证码
     */
    private String phoneCode;
    
    /**
     * 邮箱验证码
     */
    private String emailCode;
    
    /**
     * 注册方式: username, phone, email
     */
    @NotBlank(message = "注册方式不能为空")
    private String registerType;
    
    /**
     * 验证密码是否一致
     */
    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }
    
    /**
     * 获取主要联系方式
     */
    public String getPrimaryContact() {
        switch (registerType) {
            case "phone":
                return phone;
            case "email":
                return email;
            default:
                return username;
        }
    }
}
