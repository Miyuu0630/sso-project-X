package org.example.common.entity;

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
     * 登录类型：password-密码，sms-短信，oauth-第三方
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
     * 设备类型：mobile-手机，desktop-桌面，tablet-平板
     */
    private String deviceType;

    /**
     * 设备指纹
     */
    private String deviceFingerprint;

    /**
     * 是否新设备：0-否，1-是
     */
    private Integer isNewDevice;

    /**
     * 是否异常登录：0-否，1-是
     */
    private Integer isAbnormal;
    
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
     * 登录时长（秒）
     */
    private Integer loginDuration;

    /**
     * 登录状态：0-失败，1-成功
     */
    private String status;

    /**
     * 提示消息
     */
    private String msg;
    
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
            case "sms":
                return "短信登录";
            case "oauth":
                return "第三方登录";
            default:
                return "其他";
        }
    }
    
    /**
     * 获取登录状态描述
     */
    public String getLoginStatusDesc() {
        if (status == null) {
            return "未知";
        }
        return "1".equals(status) ? "成功" : "失败";
    }
    
    /**
     * 获取设备类型描述
     */
    public String getDeviceTypeDesc() {
        if (deviceType == null) {
            return "未知";
        }
        switch (deviceType) {
            case "desktop":
                return "桌面";
            case "mobile":
                return "手机";
            case "tablet":
                return "平板";
            default:
                return "其他";
        }
    }
}
