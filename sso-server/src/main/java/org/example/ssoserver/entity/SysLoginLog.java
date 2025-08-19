package org.example.ssoserver.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysLoginLog {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 登录类型:password,phone,email,wechat,alipay
     */
    private String loginType;
    
    /**
     * 登录IP
     */
    private String loginIp;
    
    /**
     * 登录地点
     */
    private String loginLocation;
    
    /**
     * 浏览器
     */
    private String browser;
    
    /**
     * 操作系统
     */
    private String os;
    
    /**
     * 设备类型:pc,mobile,tablet
     */
    private String deviceType;
    
    /**
     * 设备唯一标识
     */
    private String deviceId;
    
    /**
     * 登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
    
    /**
     * 登出时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logoutTime;
    
    /**
     * 登录状态:0-失败,1-成功
     */
    private Integer loginStatus;
    
    /**
     * 失败原因
     */
    private String failureReason;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 获取登录类型描述
     */
    public String getLoginTypeDesc() {
        if (loginType == null) {
            return "未知";
        }
        switch (loginType) {
            case "password":
                return "密码登录";
            case "phone":
                return "手机号登录";
            case "email":
                return "邮箱登录";
            case "wechat":
                return "微信登录";
            case "alipay":
                return "支付宝登录";
            default:
                return "其他";
        }
    }
    
    /**
     * 获取登录状态描述
     */
    public String getLoginStatusDesc() {
        if (loginStatus == null) {
            return "未知";
        }
        return loginStatus == 1 ? "成功" : "失败";
    }
    
    /**
     * 获取设备类型描述
     */
    public String getDeviceTypeDesc() {
        if (deviceType == null) {
            return "未知";
        }
        switch (deviceType) {
            case "pc":
                return "电脑";
            case "mobile":
                return "手机";
            case "tablet":
                return "平板";
            default:
                return "其他";
        }
    }
}
