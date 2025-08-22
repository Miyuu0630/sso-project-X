package org.example.common.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 登录请求DTO
 * 支持多种登录方式：用户名、手机号、邮箱、第三方登录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequest {

    /**
     * 登录账号(用户名/手机号/邮箱)
     */
    @NotBlank(message = "登录账号不能为空")
    private String account;

    /**
     * 密码（密码登录时必填）
     */
    private String password;

    /**
     * 登录类型: password-密码登录, sms-短信登录, email-邮箱登录, oauth-第三方登录
     */
    @Pattern(regexp = "^(password|sms|email|oauth)$", message = "登录类型不正确")
    private String loginType = "password";

    /**
     * 验证码（短信/邮箱登录时必填）
     */
    private String verificationCode;

    /**
     * 验证码key
     */
    private String verificationKey;

    /**
     * 图形验证码
     */
    private String captcha;

    /**
     * 图形验证码key
     */
    private String captchaKey;

    /**
     * 记住我
     */
    private Boolean rememberMe = false;

    /**
     * 第三方登录相关字段
     */
    private String provider; // 第三方平台：wechat, alipay, qq, github
    private String code; // 第三方授权码
    private String state; // 第三方状态码

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 设备指纹
     */
    private String deviceFingerprint;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 重定向地址（SSO登录时使用）
     */
    private String redirectUri;

    /**
     * 客户端ID（SSO登录时使用）
     */
    private String clientId;

    // ========================================
    // 业务方法
    // ========================================

    /**
     * 是否为密码登录
     */
    public boolean isPasswordLogin() {
        return "password".equals(loginType);
    }

    /**
     * 是否为短信登录
     */
    public boolean isSmsLogin() {
        return "sms".equals(loginType);
    }

    /**
     * 是否为邮箱登录
     */
    public boolean isEmailLogin() {
        return "email".equals(loginType);
    }

    /**
     * 是否为第三方登录
     */
    public boolean isOAuthLogin() {
        return "oauth".equals(loginType);
    }

    /**
     * 是否需要验证码
     */
    public boolean needsVerificationCode() {
        return isSmsLogin() || isEmailLogin();
    }

    /**
     * 是否需要密码
     */
    public boolean needsPassword() {
        return isPasswordLogin();
    }

    /**
     * 获取登录凭证（账号或验证码）
     */
    public String getCredential() {
        if (needsVerificationCode()) {
            return verificationCode;
        } else {
            return password;
        }
    }
}
