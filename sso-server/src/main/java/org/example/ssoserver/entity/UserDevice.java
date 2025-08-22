package org.example.ssoserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户设备信息实体类
 * 对应数据库表：user_device
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_device")
public class UserDevice {
    
    /**
     * 设备ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;
    
    /**
     * 设备类型：mobile-手机，tablet-平板，desktop-桌面，unknown-未知
     */
    @TableField("device_type")
    private String deviceType;
    
    /**
     * 设备指纹
     */
    @TableField("device_fingerprint")
    private String deviceFingerprint;
    
    /**
     * 操作系统
     */
    @TableField("os")
    private String os;
    
    /**
     * 浏览器
     */
    @TableField("browser")
    private String browser;
    
    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;
    
    /**
     * 地理位置
     */
    @TableField("location")
    private String location;
    
    /**
     * 是否信任设备：0-否，1-是
     */
    @TableField("is_trusted")
    private Integer isTrusted;
    
    /**
     * 设备状态：0-禁用，1-正常
     */
    @TableField("status")
    private String status;
    
    /**
     * 最后活跃时间
     */
    @TableField("last_active_time")
    private LocalDateTime lastActiveTime;
    
    /**
     * 首次登录时间
     */
    @TableField("first_login_time")
    private LocalDateTime firstLoginTime;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    
    // ========================================
    // 业务方法
    // ========================================
    
    /**
     * 是否为信任设备
     */
    public boolean isTrusted() {
        return isTrusted != null && isTrusted == 1;
    }
    
    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return "1".equals(status);
    }
    
    /**
     * 是否为移动设备
     */
    public boolean isMobileDevice() {
        return "mobile".equals(deviceType) || "tablet".equals(deviceType);
    }
    
    /**
     * 是否为桌面设备
     */
    public boolean isDesktopDevice() {
        return "desktop".equals(deviceType);
    }
}
