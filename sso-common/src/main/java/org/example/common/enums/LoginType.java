package org.example.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录类型枚举
 * 对应数据库 sys_login_log.login_type 字段
 */
@Getter
@AllArgsConstructor
public enum LoginType {
    
    /**
     * 密码登录
     */
    PASSWORD("password", "密码登录", "用户名/手机号/邮箱 + 密码"),
    
    /**
     * 短信验证码登录
     */
    SMS("sms", "短信登录", "手机号 + 短信验证码"),
    
    /**
     * 邮箱验证码登录
     */
    EMAIL("email", "邮箱登录", "邮箱 + 邮箱验证码"),
    
    /**
     * 第三方OAuth登录
     */
    OAUTH("oauth", "第三方登录", "微信、支付宝等第三方平台登录");
    
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
     * @return LoginType枚举，未找到返回null
     */
    public static LoginType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (LoginType loginType : values()) {
            if (loginType.getCode().equals(code)) {
                return loginType;
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
        LoginType loginType = fromCode(code);
        return loginType != null ? loginType.getName() : code;
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
