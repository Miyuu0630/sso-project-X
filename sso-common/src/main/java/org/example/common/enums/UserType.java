package org.example.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 * 对应数据库 sys_user.user_type 字段
 */
@Getter
@AllArgsConstructor
public enum UserType {
    
    /**
     * 普通用户
     */
    NORMAL("normal", "普通用户", "个人用户，拥有基本功能权限"),
    
    /**
     * 企业用户
     */
    ENTERPRISE("enterprise", "企业用户", "企业用户，拥有企业级功能权限"),
    
    /**
     * 航司用户
     */
    AIRLINE("airline", "航司用户", "航空公司用户，拥有航司专用功能权限");
    
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
     * @return UserType枚举，未找到返回null
     */
    public static UserType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (UserType userType : values()) {
            if (userType.getCode().equals(code)) {
                return userType;
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
        UserType userType = fromCode(code);
        return userType != null ? userType.getName() : code;
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
