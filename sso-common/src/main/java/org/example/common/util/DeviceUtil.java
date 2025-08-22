package org.example.common.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.common.enums.DeviceType;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 设备信息工具类
 * 提供设备类型识别、设备指纹生成、浏览器识别等功能
 */
@Slf4j
public class DeviceUtil {
    
    /**
     * 解析User-Agent获取设备信息
     */
    public static Map<String, String> parseUserAgent(String userAgent) {
        Map<String, String> deviceInfo = new HashMap<>();
        
        if (StrUtil.isBlank(userAgent)) {
            deviceInfo.put("browser", "Unknown");
            deviceInfo.put("os", "Unknown");
            deviceInfo.put("deviceType", "Unknown");
            return deviceInfo;
        }
        
        // 解析浏览器
        String browser = parseBrowser(userAgent);
        deviceInfo.put("browser", browser);
        
        // 解析操作系统
        String os = parseOperatingSystem(userAgent);
        deviceInfo.put("os", os);
        
        // 解析设备类型
        String deviceType = parseDeviceType(userAgent);
        deviceInfo.put("deviceType", deviceType);
        
        return deviceInfo;
    }
    
    /**
     * 解析浏览器
     */
    private static String parseBrowser(String userAgent) {
        if (userAgent.contains("Edg/")) {
            return "Microsoft Edge";
        } else if (userAgent.contains("Chrome/")) {
            return "Google Chrome";
        } else if (userAgent.contains("Firefox/")) {
            return "Mozilla Firefox";
        } else if (userAgent.contains("Safari/") && !userAgent.contains("Chrome/")) {
            return "Safari";
        } else if (userAgent.contains("Opera/") || userAgent.contains("OPR/")) {
            return "Opera";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident/")) {
            return "Internet Explorer";
        } else {
            return "Unknown Browser";
        }
    }
    
    /**
     * 解析操作系统
     */
    private static String parseOperatingSystem(String userAgent) {
        if (userAgent.contains("Windows NT 10.0")) {
            return "Windows 10";
        } else if (userAgent.contains("Windows NT 6.3")) {
            return "Windows 8.1";
        } else if (userAgent.contains("Windows NT 6.2")) {
            return "Windows 8";
        } else if (userAgent.contains("Windows NT 6.1")) {
            return "Windows 7";
        } else if (userAgent.contains("Windows NT 6.0")) {
            return "Windows Vista";
        } else if (userAgent.contains("Windows NT 5.1")) {
            return "Windows XP";
        } else if (userAgent.contains("Windows")) {
            return "Windows";
        } else if (userAgent.contains("Mac OS X")) {
            Pattern pattern = Pattern.compile("Mac OS X ([\\d_]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                return "macOS " + matcher.group(1).replace("_", ".");
            }
            return "macOS";
        } else if (userAgent.contains("Linux")) {
            if (userAgent.contains("Android")) {
                Pattern pattern = Pattern.compile("Android ([\\d.]+)");
                Matcher matcher = pattern.matcher(userAgent);
                if (matcher.find()) {
                    return "Android " + matcher.group(1);
                }
                return "Android";
            }
            return "Linux";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            Pattern pattern = Pattern.compile("OS ([\\d_]+)");
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                return "iOS " + matcher.group(1).replace("_", ".");
            }
            return "iOS";
        } else {
            return "Unknown OS";
        }
    }
    
    /**
     * 解析设备类型
     */
    private static String parseDeviceType(String userAgent) {
        if (userAgent.contains("Mobile") || userAgent.contains("Android") ||
            userAgent.contains("iPhone") || userAgent.contains("BlackBerry") ||
            userAgent.contains("Windows Phone")) {
            return "mobile";
        } else if (userAgent.contains("iPad") || userAgent.contains("Tablet")) {
            return "tablet";
        } else {
            return "desktop";
        }
    }
    
    /**
     * 生成设备指纹
     */
    public static String generateDeviceFingerprint(String userAgent, String ip) {
        StringBuilder fingerprint = new StringBuilder();
        
        if (StrUtil.isNotBlank(userAgent)) {
            fingerprint.append(userAgent.hashCode());
        }
        
        if (StrUtil.isNotBlank(ip)) {
            fingerprint.append("-").append(ip.hashCode());
        }
        
        return EncryptUtil.encryptAES(fingerprint.toString());
    }
    
    /**
     * 获取设备名称
     */
    public static String getDeviceName(String userAgent) {
        Map<String, String> deviceInfo = parseUserAgent(userAgent);
        String browser = deviceInfo.get("browser");
        String os = deviceInfo.get("os");
        String deviceType = deviceInfo.get("deviceType");
        
        StringBuilder deviceName = new StringBuilder();
        
        switch (deviceType) {
            case "mobile":
                deviceName.append("手机");
                break;
            case "tablet":
                deviceName.append("平板");
                break;
            case "desktop":
                deviceName.append("电脑");
                break;
            default:
                deviceName.append("未知设备");
                break;
        }
        
        if (!"Unknown OS".equals(os)) {
            deviceName.append(" (").append(os).append(")");
        }
        
        return deviceName.toString();
    }
    
    /**
     * 判断是否为移动设备
     */
    public static boolean isMobileDevice(String userAgent) {
        if (StrUtil.isBlank(userAgent)) {
            return false;
        }

        String deviceType = parseDeviceType(userAgent);
        return "mobile".equals(deviceType) || "tablet".equals(deviceType);
    }

    /**
     * 获取设备信息字符串
     */
    public static String getDeviceInfo(String userAgent) {
        Map<String, String> deviceInfo = parseUserAgent(userAgent);
        return String.format("%s on %s", deviceInfo.get("browser"), deviceInfo.get("os"));
    }

    /**
     * 生成简单设备指纹
     */
    public static String generateSimpleDeviceFingerprint(String userAgent, String clientIp) {
        return generateDeviceFingerprint(userAgent, clientIp);
    }

    /**
     * 获取浏览器名称
     */
    public static String getBrowserName(String userAgent) {
        Map<String, String> deviceInfo = parseUserAgent(userAgent);
        return deviceInfo.get("browser");
    }

    /**
     * 获取操作系统
     */
    public static String getOperatingSystem(String userAgent) {
        Map<String, String> deviceInfo = parseUserAgent(userAgent);
        return deviceInfo.get("os");
    }

    /**
     * 获取设备风险等级
     */
    public static String getDeviceRiskLevel(String userAgent, boolean isNewDevice) {
        if (isNewDevice) {
            return "MEDIUM";
        }

        // 检查是否为可疑User-Agent
        if (StrUtil.isBlank(userAgent) || userAgent.length() < 10) {
            return "HIGH";
        }

        // 检查是否为爬虫或自动化工具
        String lowerUserAgent = userAgent.toLowerCase();
        if (lowerUserAgent.contains("bot") ||
            lowerUserAgent.contains("crawler") ||
            lowerUserAgent.contains("spider") ||
            lowerUserAgent.contains("curl") ||
            lowerUserAgent.contains("wget")) {
            return "HIGH";
        }

        return "LOW";
    }

    /**
     * 获取设备类型
     */
    public static DeviceType getDeviceType(String userAgent) {
        String deviceTypeStr = parseDeviceType(userAgent);
        switch (deviceTypeStr) {
            case "mobile":
                return DeviceType.MOBILE;
            case "tablet":
                return DeviceType.TABLET;
            case "desktop":
                return DeviceType.DESKTOP;
            default:
                return DeviceType.UNKNOWN;
        }
    }
}
