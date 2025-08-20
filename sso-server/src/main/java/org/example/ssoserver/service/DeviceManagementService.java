package org.example.ssoserver.service;


import jakarta.servlet.http.HttpServletRequest;
import org.example.common.entity.UserDevice;
import org.example.common.result.Result;

import java.util.List;

/**
 * 设备管理服务接口
 */
public interface DeviceManagementService {
    
    /**
     * 记录用户登录设备
     * @param userId 用户ID
     * @param request HTTP请求
     * @return 设备信息
     */
    Result<UserDevice> recordLoginDevice(Long userId, HttpServletRequest request);
    
    /**
     * 检查是否为新设备
     * @param userId 用户ID
     * @param deviceFingerprint 设备指纹
     * @return 是否为新设备
     */
    boolean isNewDevice(Long userId, String deviceFingerprint);
    
    /**
     * 生成设备指纹
     * @param request HTTP请求
     * @return 设备指纹
     */
    String generateDeviceFingerprint(HttpServletRequest request);
    
    /**
     * 发送新设备登录提醒
     * @param userId 用户ID
     * @param device 设备信息
     * @return 发送结果
     */
    Result<Void> sendNewDeviceAlert(Long userId, UserDevice device);
    
    /**
     * 获取用户所有设备
     * @param userId 用户ID
     * @return 设备列表
     */
    Result<List<UserDevice>> getUserDevices(Long userId);
    
    /**
     * 获取用户活跃设备
     * @param userId 用户ID
     * @return 活跃设备列表
     */
    Result<List<UserDevice>> getUserActiveDevices(Long userId);
    
    /**
     * 信任设备
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 操作结果
     */
    Result<Void> trustDevice(Long userId, Long deviceId);
    
    /**
     * 移除设备信任
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 操作结果
     */
    Result<Void> untrustDevice(Long userId, Long deviceId);
    
    /**
     * 踢出设备（强制下线）
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 操作结果
     */
    Result<Void> kickoutDevice(Long userId, Long deviceId);
    
    /**
     * 清理过期设备记录
     * @param days 保留天数
     * @return 清理数量
     */
    Result<Integer> cleanExpiredDevices(int days);
    
    /**
     * 检测异常登录
     * @param userId 用户ID
     * @param device 设备信息
     * @return 是否异常
     */
    boolean detectAbnormalLogin(Long userId, UserDevice device);
    
    /**
     * 更新设备最后登录时间
     * @param deviceId 设备ID
     * @return 更新结果
     */
    Result<Void> updateLastLoginTime(String deviceId);
    
    /**
     * 根据IP获取地理位置
     * @param ip IP地址
     * @return 地理位置
     */
    String getLocationByIp(String ip);
}
