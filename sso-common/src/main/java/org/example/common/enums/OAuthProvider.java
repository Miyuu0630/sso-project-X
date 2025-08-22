package org.example.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 第三方OAuth平台枚举
 * 对应数据库 user_oauth_binding.provider 字段
 */
@Getter
@AllArgsConstructor
public enum OAuthProvider {
    
    /**
     * 微信
     */
    WECHAT("wechat", "微信", "微信开放平台", "https://open.weixin.qq.com"),
    
    /**
     * 支付宝
     */
    ALIPAY("alipay", "支付宝", "支付宝开放平台", "https://open.alipay.com"),
    
    /**
     * QQ
     */
    QQ("qq", "QQ", "QQ互联", "https://connect.qq.com"),
    
    /**
     * GitHub
     */
    GITHUB("github", "GitHub", "GitHub OAuth", "https://github.com"),
    
    /**
     * 钉钉
     */
    DINGTALK("dingtalk", "钉钉", "钉钉开放平台", "https://open.dingtalk.com");
    
    /**
     * 数据库存储值
     */
    private final String code;
    
    /**
     * 显示名称
     */
    private final String name;
    
    /**
     * 平台描述
     */
    private final String description;
    
    /**
     * 官方网站
     */
    private final String website;
    
    /**
     * 根据code获取枚举
     * @param code 数据库存储值
     * @return OAuthProvider枚举，未找到返回null
     */
    public static OAuthProvider fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (OAuthProvider provider : values()) {
            if (provider.getCode().equals(code)) {
                return provider;
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
        OAuthProvider provider = fromCode(code);
        return provider != null ? provider.getName() : code;
    }
    
    /**
     * 验证code是否有效
     * @param code 数据库存储值
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
    
    /**
     * 获取所有支持的平台代码
     * @return 平台代码数组
     */
    public static String[] getAllCodes() {
        OAuthProvider[] providers = values();
        String[] codes = new String[providers.length];
        for (int i = 0; i < providers.length; i++) {
            codes[i] = providers[i].getCode();
        }
        return codes;
    }
}
