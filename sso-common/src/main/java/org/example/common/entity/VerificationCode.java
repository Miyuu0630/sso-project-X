package org.example.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 验证码记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VerificationCode {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 验证码类型：sms-短信，email-邮箱
     */
    @NotBlank(message = "验证码类型不能为空")
    @Size(max = 20, message = "验证码类型长度不能超过20个字符")
    private String codeType;
    
    /**
     * 目标（手机号或邮箱）
     */
    @NotBlank(message = "目标不能为空")
    @Size(max = 100, message = "目标长度不能超过100个字符")
    private String target;
    
    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Size(max = 10, message = "验证码长度不能超过10个字符")
    private String code;
    
    /**
     * 用途：login-登录，register-注册，reset-重置密码
     */
    @NotBlank(message = "用途不能为空")
    @Size(max = 20, message = "用途长度不能超过20个字符")
    private String purpose;
    
    /**
     * 是否已使用：0-否，1-是
     */
    private Integer isUsed;
    
    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 获取验证码类型描述
     */
    public String getCodeTypeDesc() {
        if (codeType == null) {
            return "未知";
        }
        switch (codeType) {
            case "sms":
                return "短信验证码";
            case "email":
                return "邮箱验证码";
            default:
                return "其他";
        }
    }
    
    /**
     * 获取用途描述
     */
    public String getPurposeDesc() {
        if (purpose == null) {
            return "未知";
        }
        switch (purpose) {
            case "login":
                return "登录验证";
            case "register":
                return "注册验证";
            case "reset":
                return "重置密码";
            default:
                return "其他";
        }
    }
    
    /**
     * 判断是否已使用
     */
    public boolean isUsed() {
        return isUsed != null && isUsed == 1;
    }
    
    /**
     * 判断是否已过期
     */
    public boolean isExpired() {
        if (expireTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(expireTime);
    }
    
    /**
     * 判断验证码是否有效（未使用且未过期）
     */
    public boolean isValid() {
        return !isUsed() && !isExpired();
    }
    
    /**
     * 获取使用状态描述
     */
    public String getUsedDesc() {
        return isUsed() ? "已使用" : "未使用";
    }
    
    /**
     * 获取过期状态描述
     */
    public String getExpiredDesc() {
        return isExpired() ? "已过期" : "有效";
    }
    
    /**
     * 获取剩余有效时间（分钟）
     */
    public long getRemainingMinutes() {
        if (expireTime == null || isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expireTime).toMinutes();
    }
}
