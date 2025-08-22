package org.example.ssoserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录日志实体类
 * 对应数据库表：sys_login_log
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_login_log")
public class SysLoginLog {
    
    /**
     * 访问ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 用户账号
     */
    @TableField("username")
    private String username;
    
    /**
     * 登录类型
     */
    @TableField("login_type")
    private String loginType;
    
    /**
     * 登录IP地址
     */
    @TableField("login_ip")
    private String loginIp;
    
    /**
     * 登录地点
     */
    @TableField("login_location")
    private String loginLocation;
    
    /**
     * 浏览器类型
     */
    @TableField("browser")
    private String browser;
    
    /**
     * 操作系统
     */
    @TableField("os")
    private String os;
    
    /**
     * 设备类型
     */
    @TableField("device_type")
    private String deviceType;
    
    /**
     * 设备指纹
     */
    @TableField("device_fingerprint")
    private String deviceFingerprint;
    
    /**
     * 登录时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;
    
    /**
     * 登录状态：0-失败，1-成功
     */
    @TableField("status")
    private String status;
    
    /**
     * 提示消息
     */
    @TableField("msg")
    private String msg;
    
    // ========================================
    // 业务方法
    // ========================================
    
    /**
     * 是否登录成功
     */
    public boolean isSuccess() {
        return "1".equals(status);
    }
    
    /**
     * 是否登录失败
     */
    public boolean isFailed() {
        return "0".equals(status);
    }
}
