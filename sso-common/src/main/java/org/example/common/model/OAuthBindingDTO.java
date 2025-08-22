package org.example.common.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 第三方账号绑定DTO
 * 用于前后端数据传输和模块间数据交换
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OAuthBindingDTO {
    
    /**
     * 绑定ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 第三方平台：wechat-微信，alipay-支付宝，qq-QQ，github-GitHub
     */
    private String provider;
    
    /**
     * 第三方平台名称
     */
    private String providerName;
    
    /**
     * 第三方平台用户唯一标识
     */
    private String openId;
    
    /**
     * 第三方平台统一标识
     */
    private String unionId;
    
    /**
     * 第三方平台昵称
     */
    private String nickname;
    
    /**
     * 第三方平台头像
     */
    private String avatar;
    
    /**
     * 授权范围
     */
    private String scope;
    
    /**
     * Token类型
     */
    private String tokenType;
    
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
     * 最后刷新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastRefreshTime;
    
    /**
     * 是否激活：0-否，1-是
     */
    private Boolean isActive;
    
    /**
     * 激活状态描述
     */
    private String activeDesc;
    
    /**
     * 绑定IP
     */
    private String bindIp;
    
    /**
     * 用户名（关联查询）
     */
    private String username;
    
    /**
     * 用户真实姓名（关联查询）
     */
    private String realName;
    
    /**
     * 登录次数
     */
    private Integer loginCount;
    
    /**
     * 绑定天数
     */
    private Integer bindDays;
    
    /**
     * 是否可以解绑
     */
    private Boolean canUnbind;
    
    // ========================================
    // 业务方法
    // ========================================
    
    /**
     * 是否为微信平台
     */
    public boolean isWechat() {
        return "wechat".equals(provider);
    }
    
    /**
     * 是否为支付宝平台
     */
    public boolean isAlipay() {
        return "alipay".equals(provider);
    }
    
    /**
     * 是否为QQ平台
     */
    public boolean isQQ() {
        return "qq".equals(provider);
    }
    
    /**
     * 是否为GitHub平台
     */
    public boolean isGitHub() {
        return "github".equals(provider);
    }
    
    /**
     * 是否激活
     */
    public boolean isActiveBinding() {
        return isActive != null && isActive;
    }
    
    /**
     * 获取平台图标
     */
    public String getProviderIcon() {
        if (provider == null) {
            return "oauth";
        }
        switch (provider) {
            case "wechat":
                return "wechat";
            case "alipay":
                return "alipay";
            case "qq":
                return "qq";
            case "github":
                return "github";
            case "dingtalk":
                return "dingtalk";
            default:
                return "oauth";
        }
    }
    
    /**
     * 获取平台颜色
     */
    public String getProviderColor() {
        if (provider == null) {
            return "#666666";
        }
        switch (provider) {
            case "wechat":
                return "#07C160";
            case "alipay":
                return "#1677FF";
            case "qq":
                return "#12B7F5";
            case "github":
                return "#24292E";
            case "dingtalk":
                return "#0089FF";
            default:
                return "#666666";
        }
    }
    
    /**
     * 获取显示昵称
     */
    public String getDisplayNickname() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        return "第三方用户";
    }
    
    /**
     * 是否最近登录（7天内）
     */
    public boolean isRecentLogin() {
        if (lastLoginTime == null) {
            return false;
        }
        return lastLoginTime.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    /**
     * 是否长时间未使用（30天以上）
     */
    public boolean isLongTimeNoUse() {
        if (lastLoginTime == null) {
            return bindTime != null && bindTime.isBefore(LocalDateTime.now().minusDays(30));
        }
        return lastLoginTime.isBefore(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * 获取绑定状态描述
     */
    public String getBindingStatus() {
        if (!isActiveBinding()) {
            return "已停用";
        } else if (isRecentLogin()) {
            return "活跃";
        } else if (isLongTimeNoUse()) {
            return "长期未用";
        } else {
            return "正常";
        }
    }
    
    /**
     * 是否可以解绑
     */
    public boolean canUnbind() {
        // 如果明确设置了不能解绑，则返回false
        if (canUnbind != null && !canUnbind) {
            return false;
        }
        // 默认可以解绑
        return true;
    }
}
