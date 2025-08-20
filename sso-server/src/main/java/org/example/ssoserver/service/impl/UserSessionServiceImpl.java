package org.example.ssoserver.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.entity.SysLoginLog;
import org.example.common.mapper.SysLoginLogMapper;
import org.example.common.result.Result;
import org.example.common.util.DeviceUtil;
import org.example.ssoserver.service.UserSessionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户会话管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {
    
    private final SysLoginLogMapper loginLogMapper;
    
    @Override
    public void recordLogin(Long userId, String username, String loginType, String loginIp, 
                           String userAgent, String deviceId, boolean success, String failureReason) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setLoginType(loginType);
            loginLog.setLoginIp(loginIp);
            loginLog.setDeviceFingerprint(deviceId);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setStatus(success ? "1" : "0");
            loginLog.setMsg(failureReason);
            
            if (StrUtil.isNotBlank(userAgent)) {
                Map<String, String> deviceInfo = DeviceUtil.parseUserAgent(userAgent);
                loginLog.setBrowser(deviceInfo.get("browser"));
                loginLog.setOs(deviceInfo.get("os"));
                loginLog.setDeviceType(deviceInfo.get("deviceType"));
            }
            
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }
    
    @Override
    public void recordLogout(Long userId, String sessionId) {
        try {
            if (StrUtil.isNotBlank(sessionId)) {
                loginLogMapper.updateLogoutTimeByDevice(sessionId, LocalDateTime.now());
            }
        } catch (Exception e) {
            log.error("记录登出日志失败", e);
        }
    }
    
    @Override
    public Result<Map<String, Object>> getUserLoginLogs(Long userId, Integer page, Integer size) {
        try {
            int offset = (page - 1) * size;

            List<SysLoginLog> logs = loginLogMapper.selectPage(userId, null, null, null,
                                                              null, null, offset, size);
            int total = loginLogMapper.selectCount(userId, null, null, null, null, null);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", logs);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户登录记录失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<List<Map<String, Object>>> getUserOnlineDevices(Long userId) {
        try {
            List<SysLoginLog> onlineDevices = loginLogMapper.selectOnlineDevicesByUserId(userId);
            
            List<Map<String, Object>> devices = onlineDevices.stream().map(device -> {
                Map<String, Object> deviceMap = new HashMap<>();
                deviceMap.put("deviceId", device.getDeviceFingerprint());
                deviceMap.put("deviceType", device.getDeviceType());
                deviceMap.put("deviceName", DeviceUtil.getDeviceName(device.getBrowser() + " " + device.getOs()));
                deviceMap.put("loginIp", device.getLoginIp());
                deviceMap.put("loginLocation", device.getLoginLocation());
                deviceMap.put("loginTime", device.getLoginTime());
                deviceMap.put("browser", device.getBrowser());
                deviceMap.put("os", device.getOs());
                return deviceMap;
            }).toList();
            
            return Result.success(devices);
        } catch (Exception e) {
            log.error("获取用户在线设备失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Void> forceLogoutDevice(Long userId, String sessionId) {
        try {
            // 记录登出时间
            recordLogout(userId, sessionId);
            
            // TODO: 实现强制下线逻辑，需要结合Sa-Token的会话管理
            
            return Result.<Void>success();
        } catch (Exception e) {
            log.error("强制下线设备失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Void> forceLogoutAllDevices(Long userId) {
        try {
            // TODO: 实现强制下线所有设备的逻辑
            
            return Result.<Void>success();
        } catch (Exception e) {
            log.error("强制下线所有设备失败", e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> checkMultiDeviceLogin(Long userId, String currentSessionId) {
        try {
            List<SysLoginLog> onlineDevices = loginLogMapper.selectOnlineDevicesByUserId(userId);
            
            boolean hasMultiDevice = onlineDevices.size() > 1;
            
            Map<String, Object> result = new HashMap<>();
            result.put("hasMultiDevice", hasMultiDevice);
            result.put("deviceCount", onlineDevices.size());
            result.put("currentSessionId", currentSessionId);
            
            if (hasMultiDevice) {
                result.put("message", "检测到您的账号在多个设备上登录，请注意账号安全");
                result.put("devices", onlineDevices.stream().map(device -> {
                    Map<String, Object> deviceMap = new HashMap<>();
                    deviceMap.put("deviceType", device.getDeviceType());
                    deviceMap.put("loginIp", device.getLoginIp());
                    deviceMap.put("loginTime", device.getLoginTime());
                    return deviceMap;
                }).toList());
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查多设备登录失败", e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }
    
    @Override
    public void updateSessionLastAccessTime(String sessionId) {
        try {
            // TODO: 实现更新会话最后访问时间的逻辑
        } catch (Exception e) {
            log.error("更新会话访问时间失败", e);
        }
    }
    
    @Override
    public void cleanExpiredSessions() {
        try {
            // 清理30天前的登录记录
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(30);
            int deletedCount = loginLogMapper.deleteBeforeTime(beforeTime);
            log.info("清理过期登录记录: {} 条", deletedCount);
        } catch (Exception e) {
            log.error("清理过期会话失败", e);
        }
    }
    
    @Override
    public int getUserActiveSessionCount(Long userId) {
        try {
            List<SysLoginLog> onlineDevices = loginLogMapper.selectOnlineDevicesByUserId(userId);
            return onlineDevices.size();
        } catch (Exception e) {
            log.error("获取用户活跃会话数失败", e);
            return 0;
        }
    }
    
    @Override
    public boolean isExceedMaxConcurrentSessions(Long userId) {
        try {
            int activeSessionCount = getUserActiveSessionCount(userId);
            int maxConcurrentSessions = 3; // 可以从配置中读取
            return activeSessionCount > maxConcurrentSessions;
        } catch (Exception e) {
            log.error("检查并发会话数失败", e);
            return false;
        }
    }
}
