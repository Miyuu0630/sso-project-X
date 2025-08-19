package org.example.ssoserver.service;

import org.example.ssoserver.common.Result;
import org.example.ssoserver.entity.SysLoginLog;

import java.util.List;
import java.util.Map;

/**
 * 用户会话管理服务接口
 */
public interface UserSessionService {
    
    /**
     * 记录用户登录
     */
    void recordLogin(Long userId, String username, String loginType, String loginIp, 
                    String userAgent, String deviceId, boolean success, String failureReason);
    
    /**
     * 记录用户登出
     */
    void recordLogout(Long userId, String sessionId);
    
    /**
     * 获取用户登录记录
     */
    Result<Map<String, Object>> getUserLoginLogs(Long userId, Integer page, Integer size);
    
    /**
     * 获取用户在线设备列表
     */
    Result<List<Map<String, Object>>> getUserOnlineDevices(Long userId);
    
    /**
     * 强制下线指定设备
     */
    Result<Void> forceLogoutDevice(Long userId, String sessionId);
    
    /**
     * 强制下线用户所有设备
     */
    Result<Void> forceLogoutAllDevices(Long userId);
    
    /**
     * 检查多设备登录
     */
    Result<Map<String, Object>> checkMultiDeviceLogin(Long userId, String currentSessionId);
    
    /**
     * 更新会话最后访问时间
     */
    void updateSessionLastAccessTime(String sessionId);
    
    /**
     * 清理过期会话
     */
    void cleanExpiredSessions();
    
    /**
     * 获取用户当前活跃会话数
     */
    int getUserActiveSessionCount(Long userId);
    
    /**
     * 检查是否超过最大并发会话数
     */
    boolean isExceedMaxConcurrentSessions(Long userId);
}
