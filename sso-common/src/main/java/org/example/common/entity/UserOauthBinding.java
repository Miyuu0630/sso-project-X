package org.example.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 第三方账号绑定实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserOauthBinding {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 第三方平台：wechat-微信，alipay-支付宝，qq-QQ，github-GitHub
     */
    @NotBlank(message = "第三方平台不能为空")
    @Size(max = 20, message = "第三方平台长度不能超过20个字符")
    private String provider;
    
    /**
     * 第三方平台用户唯一标识
     */
    @NotBlank(message = "第三方平台用户标识不能为空")
    @Size(max = 100, message = "第三方平台用户标识长度不能超过100个字符")
    private String openId;
    
    /**
     * 第三方平台统一标识
     */
    @Size(max = 100, message = "第三方平台统一标识长度不能超过100个字符")
    private String unionId;
    
    /**
     * 第三方平台昵称
     */
    @Size(max = 100, message = "第三方平台昵称长度不能超过100个字符")
    private String nickname;
    
    /**
     * 第三方平台头像
     */
    @Size(max = 255, message = "第三方平台头像长度不能超过255个字符")
    private String avatar;
    
    /**
     * 访问令牌
     */
    @Size(max = 500, message = "访问令牌长度不能超过500个字符")
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    @Size(max = 500, message = "刷新令牌长度不能超过500个字符")
    private String refreshToken;
    
    /**
     * 令牌过期时间（秒）
     */
    private Integer expiresIn;
    
    /**
     * 绑定时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bindTime;
    
    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
    
    /**
     * 是否激活：0-否，1-是
     */
    private Integer isActive;
    
    /**
     * 获取第三方平台描述
     */
    public String getProviderDesc() {
        if (provider == null) {
            return "未知";
        }
        switch (provider) {
            case "wechat":
                return "微信";
            case "alipay":
                return "支付宝";
            case "qq":
                return "QQ";
            case "github":
                return "GitHub";
            default:
                return "其他";
        }
    }
    
    /**
     * 判断是否激活
     */
    public boolean isActive() {
        return isActive != null && isActive == 1;
    }
    
    /**
     * 获取激活状态描述
     */
    public String getActiveDesc() {
        return isActive() ? "激活" : "未激活";
    }
    
    /**
     * 判断令牌是否过期
     */
    public boolean isTokenExpired() {
        if (expiresIn == null || bindTime == null) {
            return true;
        }
        LocalDateTime expireTime = bindTime.plusSeconds(expiresIn);
        return LocalDateTime.now().isAfter(expireTime);
    }
}
