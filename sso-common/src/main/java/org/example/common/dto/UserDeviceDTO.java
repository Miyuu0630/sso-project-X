package org.example.common.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 用户设备DTO
 * 用于前后端数据传输和模块间数据交换
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDeviceDTO {
    
    /**
     * 设备ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 设备唯一标识（设备指纹）
     */
    private String deviceId;
    
    /**
     * 设备名称
     */
    private String deviceName;
    
    /**
     * 设备类型：mobile-手机，desktop-桌面，tablet-平板
     */
    private String deviceType;
    
    /**
     * 设备类型描述
     */
    private String deviceTypeDesc;
    
    /**
     * 浏览器信息
     */
    private String browser;
    
    /**
     * 操作系统
     */
    private String os;
    
    /**
     * 登录IP地址
     */
    private String loginIp;
    
    /**
     * 登录地点
     */
    private String loginLocation;
    
    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
    
    /**
     * 是否活跃：0-否，1-是
     */
    private Boolean isActive;
    
    /**
     * 活跃状态描述
     */
    private String activeDesc;
    
    /**
     * 是否信任设备：0-否，1-是
     */
    private Boolean isTrusted;
    
    /**
     * 信任状态描述
     */
    private String trustedDesc;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 用户名（关联查询）
     */
    private String username;
    
    /**
     * 登录次数
     */
    private Integer loginCount;
    
    /**
     * 最近登录天数
     */
    private Integer recentLoginDays;
    
    /**
     * 是否为当前设备
     */
    private Boolean isCurrent;
    
    // ========================================
    // 业务方法
    // ========================================
    
    /**
     * 是否为移动设备
     */
    public boolean isMobile() {
        return "mobile".equals(deviceType);
    }
    
    /**
     * 是否为桌面设备
     */
    public boolean isDesktop() {
        return "desktop".equals(deviceType);
    }
    
    /**
     * 是否为平板设备
     */
    public boolean isTablet() {
        return "tablet".equals(deviceType);
    }
    
    /**
     * 是否活跃设备
     */
    public boolean isActiveDevice() {
        return isActive != null && isActive;
    }
    
    /**
     * 是否信任设备
     */
    public boolean isTrustedDevice() {
        return isTrusted != null && isTrusted;
    }
    
    /**
     * 获取设备显示名称
     */
    public String getDisplayName() {
        if (deviceName != null && !deviceName.trim().isEmpty()) {
            return deviceName;
        }
        
        StringBuilder sb = new StringBuilder();
        if (os != null && !os.trim().isEmpty()) {
            sb.append(os);
        }
        if (browser != null && !browser.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(browser);
        }
        if (sb.length() == 0) {
            sb.append("未知设备");
        }
        
        return sb.toString();
    }
    
    /**
     * 获取设备类型图标
     */
    public String getDeviceIcon() {
        if (deviceType == null) {
            return "device";
        }
        switch (deviceType) {
            case "mobile":
                return "mobile";
            case "desktop":
                return "desktop";
            case "tablet":
                return "tablet";
            default:
                return "device";
        }
    }
    
    /**
     * 是否为新设备（7天内首次登录）
     */
    public boolean isNewDevice() {
        if (createTime == null) {
            return false;
        }
        return createTime.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    /**
     * 是否长时间未登录（30天以上）
     */
    public boolean isLongTimeNoLogin() {
        if (lastLoginTime == null) {
            return true;
        }
        return lastLoginTime.isBefore(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * 获取安全等级
     */
    public String getSecurityLevel() {
        if (isTrustedDevice()) {
            return "高";
        } else if (isNewDevice()) {
            return "低";
        } else {
            return "中";
        }
    }
}
