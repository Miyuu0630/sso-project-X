package org.example.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 安全事件类型枚举
 * 对应数据库 security_event_log.event_type 字段
 */
@Getter
@AllArgsConstructor
public enum SecurityEventType {
    
    /**
     * 密码修改
     */
    PASSWORD_CHANGE("password_change", "密码修改", "用户主动修改密码"),
    
    /**
     * 密码重置
     */
    PASSWORD_RESET("password_reset", "密码重置", "通过验证码重置密码"),
    
    /**
     * 账号锁定
     */
    ACCOUNT_LOCK("account_lock", "账号锁定", "账号被系统锁定"),
    
    /**
     * 账号解锁
     */
    ACCOUNT_UNLOCK("account_unlock", "账号解锁", "账号被解锁"),
    
    /**
     * 可疑登录
     */
    SUSPICIOUS_LOGIN("suspicious_login", "可疑登录", "检测到异常登录行为"),
    
    /**
     * 新设备登录
     */
    NEW_DEVICE_LOGIN("new_device_login", "新设备登录", "在新设备上登录"),
    
    /**
     * 异地登录
     */
    REMOTE_LOGIN("remote_login", "异地登录", "在异常地点登录"),
    
    /**
     * 登录失败
     */
    LOGIN_FAILED("login_failed", "登录失败", "登录验证失败"),
    
    /**
     * 第三方绑定
     */
    OAUTH_BIND("oauth_bind", "第三方绑定", "绑定第三方账号"),
    
    /**
     * 第三方解绑
     */
    OAUTH_UNBIND("oauth_unbind", "第三方解绑", "解绑第三方账号"),
    
    /**
     * 权限变更
     */
    PERMISSION_CHANGE("permission_change", "权限变更", "用户权限发生变更"),
    
    /**
     * 敏感操作
     */
    SENSITIVE_OPERATION("sensitive_operation", "敏感操作", "执行敏感操作");
    
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
     * @return SecurityEventType枚举，未找到返回null
     */
    public static SecurityEventType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (SecurityEventType eventType : values()) {
            if (eventType.getCode().equals(code)) {
                return eventType;
            }
        }
        return null;
    }
    
    /**
     * 根据code获取显示名称
     * @param code 数据库存储值
     * @return 显示名称，未找到返回原code
     */
    public static String getNameByCode(String code) {
        SecurityEventType eventType = fromCode(code);
        return eventType != null ? eventType.getName() : code;
    }
    
    /**
     * 验证code是否有效
     * @param code 数据库存储值
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}
