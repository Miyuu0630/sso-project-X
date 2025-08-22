package org.example.common.util;

import cn.hutool.core.util.StrUtil;

/**
 * 数据脱敏工具类
 * 提供手机号、邮箱、身份证号等敏感信息的脱敏处理
 */
public class DesensitizeUtil {
    
    /**
     * 默认脱敏字符
     */
    private static final String DEFAULT_MASK_CHAR = "*";
    
    /**
     * 手机号脱敏
     * 保留前3位和后4位，中间用*号替换
     * 例：138****1234
     * 
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String desensitizePhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        
        if (phone.length() == 11) {
            // 标准11位手机号
            return phone.substring(0, 3) + "****" + phone.substring(7);
        } else {
            // 其他长度的电话号码
            int length = phone.length();
            int keepStart = Math.min(3, length / 3);
            int keepEnd = Math.min(4, length / 3);
            int maskLength = length - keepStart - keepEnd;
            
            if (maskLength <= 0) {
                return phone;
            }
            
            return phone.substring(0, keepStart) + 
                   DEFAULT_MASK_CHAR.repeat(maskLength) + 
                   phone.substring(length - keepEnd);
        }
    }
    
    /**
     * 邮箱脱敏
     * 保留邮箱前缀的前2位和@后的域名，中间用*号替换
     * 例：te****@example.com
     * 
     * @param email 邮箱地址
     * @return 脱敏后的邮箱
     */
    public static String desensitizeEmail(String email) {
        if (StrUtil.isBlank(email) || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return email;
        }
        
        String prefix = parts[0];
        String domain = parts[1];
        
        if (prefix.length() <= 2) {
            return email;
        }
        
        String maskedPrefix;
        if (prefix.length() <= 4) {
            maskedPrefix = prefix.substring(0, 1) + "***" + prefix.substring(prefix.length() - 1);
        } else {
            maskedPrefix = prefix.substring(0, 2) + "****" + prefix.substring(prefix.length() - 2);
        }
        
        return maskedPrefix + "@" + domain;
    }
    
    /**
     * 身份证号脱敏
     * 保留前4位和后4位，中间用*号替换
     * 例：1234**********5678
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String desensitizeIdCard(String idCard) {
        if (StrUtil.isBlank(idCard) || idCard.length() < 8) {
            return idCard;
        }
        
        int length = idCard.length();
        return idCard.substring(0, 4) + 
               DEFAULT_MASK_CHAR.repeat(length - 8) + 
               idCard.substring(length - 4);
    }
    
    /**
     * 姓名脱敏
     * 保留姓氏，其他字符用*号替换
     * 例：张*、李**、欧阳**
     * 
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String desensitizeName(String name) {
        if (StrUtil.isBlank(name) || name.length() <= 1) {
            return name;
        }
        
        if (name.length() == 2) {
            return name.substring(0, 1) + "*";
        } else {
            return name.substring(0, 1) + DEFAULT_MASK_CHAR.repeat(name.length() - 1);
        }
    }
    
    /**
     * 银行卡号脱敏
     * 保留前4位和后4位，中间用*号替换
     * 例：1234 **** **** 5678
     * 
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String desensitizeBankCard(String bankCard) {
        if (StrUtil.isBlank(bankCard) || bankCard.length() < 8) {
            return bankCard;
        }
        
        // 移除空格和其他分隔符
        String cleanCard = bankCard.replaceAll("\\s+", "");
        
        if (cleanCard.length() < 8) {
            return bankCard;
        }
        
        String masked = cleanCard.substring(0, 4) + 
                       " **** **** " + 
                       cleanCard.substring(cleanCard.length() - 4);
        
        return masked;
    }
    
    /**
     * 地址脱敏
     * 保留省市信息，详细地址用*号替换
     * 例：北京市朝阳区****
     * 
     * @param address 地址
     * @return 脱敏后的地址
     */
    public static String desensitizeAddress(String address) {
        if (StrUtil.isBlank(address) || address.length() <= 6) {
            return address;
        }
        
        // 简单的省市识别
        String[] cityKeywords = {"省", "市", "区", "县", "自治区", "特别行政区"};
        
        int lastCityIndex = -1;
        for (String keyword : cityKeywords) {
            int index = address.lastIndexOf(keyword);
            if (index > lastCityIndex) {
                lastCityIndex = index + keyword.length();
            }
        }
        
        if (lastCityIndex > 0 && lastCityIndex < address.length()) {
            return address.substring(0, lastCityIndex) + "****";
        } else {
            // 如果没有找到省市关键词，保留前6位
            return address.substring(0, Math.min(6, address.length())) + "****";
        }
    }
    
    /**
     * 通用脱敏方法
     * 保留前后指定位数，中间用*号替换
     * 
     * @param str 原字符串
     * @param keepStart 保留开头位数
     * @param keepEnd 保留结尾位数
     * @return 脱敏后的字符串
     */
    public static String desensitize(String str, int keepStart, int keepEnd) {
        if (StrUtil.isBlank(str) || str.length() <= keepStart + keepEnd) {
            return str;
        }
        
        int maskLength = str.length() - keepStart - keepEnd;
        return str.substring(0, keepStart) + 
               DEFAULT_MASK_CHAR.repeat(maskLength) + 
               str.substring(str.length() - keepEnd);
    }
    
    /**
     * 通用脱敏方法（自定义脱敏字符）
     * 
     * @param str 原字符串
     * @param keepStart 保留开头位数
     * @param keepEnd 保留结尾位数
     * @param maskChar 脱敏字符
     * @return 脱敏后的字符串
     */
    public static String desensitize(String str, int keepStart, int keepEnd, String maskChar) {
        if (StrUtil.isBlank(str) || str.length() <= keepStart + keepEnd) {
            return str;
        }
        
        int maskLength = str.length() - keepStart - keepEnd;
        return str.substring(0, keepStart) + 
               maskChar.repeat(maskLength) + 
               str.substring(str.length() - keepEnd);
    }
    
    /**
     * IP地址脱敏
     * 保留前两段，后两段用*替换
     * 例：192.168.*.*
     * 
     * @param ip IP地址
     * @return 脱敏后的IP地址
     */
    public static String desensitizeIp(String ip) {
        if (StrUtil.isBlank(ip) || !ip.contains(".")) {
            return ip;
        }
        
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return ip;
        }
        
        return parts[0] + "." + parts[1] + ".*.*";
    }
    
    /**
     * 根据数据类型自动选择脱敏方法
     * 
     * @param data 原数据
     * @param dataType 数据类型：phone, email, idcard, name, bankcard, address, ip
     * @return 脱敏后的数据
     */
    public static String autoDesensitize(String data, String dataType) {
        if (StrUtil.isBlank(data) || StrUtil.isBlank(dataType)) {
            return data;
        }
        
        switch (dataType.toLowerCase()) {
            case "phone":
                return desensitizePhone(data);
            case "email":
                return desensitizeEmail(data);
            case "idcard":
                return desensitizeIdCard(data);
            case "name":
                return desensitizeName(data);
            case "bankcard":
                return desensitizeBankCard(data);
            case "address":
                return desensitizeAddress(data);
            case "ip":
                return desensitizeIp(data);
            default:
                return desensitize(data, 2, 2);
        }
    }
}
