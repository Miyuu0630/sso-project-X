package org.example.ssoclient.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户信息服务
 */
@Slf4j
@Service
public class UserInfoService {
    
    @Value("${sa-token.sso.server-url}")
    private String ssoServerUrl;
    
    private final StringRedisTemplate redisTemplate;
    
    public UserInfoService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 获取当前用户信息
     */
    public Map<String, Object> getCurrentUserInfo() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            String cacheKey = "user_info:" + userId;
            
            // 先从缓存获取
            String cachedUserInfo = redisTemplate.opsForValue().get(cacheKey);
            if (cachedUserInfo != null) {
                return JSONUtil.parseObj(cachedUserInfo);
            }
            
            // 从SSO服务器获取用户信息
            String token = StpUtil.getTokenValue();
            String url = ssoServerUrl + "/sso/userinfo?ticket=" + token;
            
            String response = HttpUtil.get(url);
            JSONObject result = JSONUtil.parseObj(response);
            
            if (result.getInt("code") == 200) {
                Map<String, Object> userInfo = result.getJSONObject("data");
                
                // 缓存用户信息（5分钟）
                redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(userInfo), 5, TimeUnit.MINUTES);
                
                return userInfo;
            } else {
                log.error("获取用户信息失败: {}", result.getStr("message"));
                return null;
            }
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return null;
        }
    }
    
    /**
     * 获取用户权限信息
     */
    public Map<String, Object> getCurrentUserPermissions() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            
            Long userId = StpUtil.getLoginIdAsLong();
            String cacheKey = "user_permissions:" + userId;
            
            // 先从缓存获取
            String cachedPermissions = redisTemplate.opsForValue().get(cacheKey);
            if (cachedPermissions != null) {
                return JSONUtil.parseObj(cachedPermissions);
            }
            
            // 从SSO服务器获取权限信息
            String token = StpUtil.getTokenValue();
            String url = ssoServerUrl + "/sso/permissions?ticket=" + token;
            
            String response = HttpUtil.get(url);
            JSONObject result = JSONUtil.parseObj(response);
            
            if (result.getInt("code") == 200) {
                Map<String, Object> permissions = result.getJSONObject("data");
                
                // 缓存权限信息（10分钟）
                redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(permissions), 10, TimeUnit.MINUTES);
                
                return permissions;
            } else {
                log.error("获取用户权限失败: {}", result.getStr("message"));
                return null;
            }
        } catch (Exception e) {
            log.error("获取用户权限异常", e);
            return null;
        }
    }
    
    /**
     * 刷新Token
     */
    public boolean refreshToken() {
        try {
            if (!StpUtil.isLogin()) {
                return false;
            }
            
            String token = StpUtil.getTokenValue();
            String url = ssoServerUrl + "/sso/refresh-ticket?ticket=" + token;
            
            String response = HttpUtil.post(url, "");
            JSONObject result = JSONUtil.parseObj(response);
            
            if (result.getInt("code") == 200) {
                // Token刷新成功，清除用户信息缓存
                Long userId = StpUtil.getLoginIdAsLong();
                redisTemplate.delete("user_info:" + userId);
                redisTemplate.delete("user_permissions:" + userId);
                
                log.info("Token刷新成功");
                return true;
            } else {
                log.error("Token刷新失败: {}", result.getStr("message"));
                return false;
            }
        } catch (Exception e) {
            log.error("Token刷新异常", e);
            return false;
        }
    }
    
    /**
     * 清除用户缓存
     */
    public void clearUserCache(Long userId) {
        try {
            redisTemplate.delete("user_info:" + userId);
            redisTemplate.delete("user_permissions:" + userId);
        } catch (Exception e) {
            log.error("清除用户缓存失败", e);
        }
    }
    
    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(String permission) {
        try {
            Map<String, Object> permissions = getCurrentUserPermissions();
            if (permissions == null) {
                return false;
            }
            
            Object permissionList = permissions.get("permissions");
            if (permissionList instanceof java.util.List) {
                return ((java.util.List<?>) permissionList).contains(permission);
            }
            
            return false;
        } catch (Exception e) {
            log.error("检查权限异常", e);
            return false;
        }
    }
    
    /**
     * 检查用户是否有指定角色
     */
    public boolean hasRole(String role) {
        try {
            Map<String, Object> permissions = getCurrentUserPermissions();
            if (permissions == null) {
                return false;
            }
            
            Object roleList = permissions.get("roles");
            if (roleList instanceof java.util.List) {
                return ((java.util.List<?>) roleList).contains(role);
            }
            
            return false;
        } catch (Exception e) {
            log.error("检查角色异常", e);
            return false;
        }
    }
}
