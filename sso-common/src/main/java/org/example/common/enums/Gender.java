package org.example.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 * 对应数据库 sys_user.gender 字段
 */
@Getter
@AllArgsConstructor
public enum Gender {
    
    /**
     * 未知
     */
    UNKNOWN(0, "未知"),
    
    /**
     * 男性
     */
    MALE(1, "男"),
    
    /**
     * 女性
     */
    FEMALE(2, "女");
    
    /**
     * 数据库存储值
     */
    private final Integer code;
    
    /**
     * 显示名称
     */
    private final String name;
    
    /**
     * 根据code获取枚举
     * @param code 数据库存储值
     * @return Gender枚举，未找到返回UNKNOWN
     */
    public static Gender fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (Gender gender : values()) {
            if (gender.getCode().equals(code)) {
                return gender;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * 根据code获取显示名称
     * @param code 数据库存储值
     * @return 显示名称
     */
    public static String getNameByCode(Integer code) {
        return fromCode(code).getName();
    }
}
