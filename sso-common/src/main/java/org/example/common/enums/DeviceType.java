package org.example.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备类型枚举
 * 对应数据库 sys_login_log.device_type 和 user_device.device_type 字段
 */
@Getter
@AllArgsConstructor
public enum DeviceType {
    
    /**
     * 手机设备
     */
    MOBILE("mobile", "手机", "移动设备，包括手机、小程序等"),
    
    /**
     * 桌面设备
     */
    DESKTOP("desktop", "桌面", "桌面设备，包括PC、笔记本等"),
    
    /**
     * 平板设备
     */
    TABLET("tablet", "平板", "平板设备，包括iPad、Android平板等"),
    
    /**
     * 未知设备
     */
    UNKNOWN("unknown", "未知", "无法识别的设备类型");
    
    /**
     * 数据库存储值
     */
    private final String code;
    
    /**
     * 显示名称
     */
    private final String name;
    
    /**
     * 描述信息
     */
    private final String description;
    
    /**
     * 根据code获取枚举
     * @param code 数据库存储值
     * @return DeviceType枚举，未找到返回UNKNOWN
     */
    public static DeviceType fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (DeviceType deviceType : values()) {
            if (deviceType.getCode().equals(code)) {
                return deviceType;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * 根据code获取显示名称
     * @param code 数据库存储值
     * @return 显示名称
     */
    public static String getNameByCode(String code) {
        return fromCode(code).getName();
    }
    
    /**
     * 根据User-Agent判断设备类型
     * @param userAgent 浏览器User-Agent字符串
     * @return 设备类型
     */
    public static DeviceType fromUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return UNKNOWN;
        }
        
        String ua = userAgent.toLowerCase();
        
        // 移动设备检测
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone") || 
            ua.contains("ipod") || ua.contains("blackberry") || ua.contains("windows phone")) {
            return MOBILE;
        }
        
        // 平板设备检测
        if (ua.contains("ipad") || ua.contains("tablet")) {
            return TABLET;
        }
        
        // 桌面设备检测
        if (ua.contains("windows") || ua.contains("macintosh") || ua.contains("linux")) {
            return DESKTOP;
        }
        
        return UNKNOWN;
    }
}
