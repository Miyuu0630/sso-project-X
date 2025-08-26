package org.example.ssoserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户注册响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号（脱敏）
     */
    private String phone;

    /**
     * 邮箱（脱敏）
     */
    private String email;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 注册时间
     */
    private LocalDateTime createTime;

    /**
     * 是否需要激活
     */
    private Boolean needActivation;

    /**
     * 激活链接（如果需要激活）
     */
    private String activationUrl;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 脱敏手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 脱敏邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return username + "***@" + domain;
        } else {
            return username.substring(0, 2) + "***@" + domain;
        }
    }
}
