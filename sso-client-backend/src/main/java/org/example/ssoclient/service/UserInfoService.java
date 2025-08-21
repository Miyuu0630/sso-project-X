package org.example.ssoclient.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
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

            // 构造基本用户信息（在实际项目中，这里应该从数据库获取）
            Map<String, Object> userInfo = Map.of(
                "id", userId,
                "username", "user" + userId,
                "nickname", "用户" + userId,
                "email", "user" + userId + "@example.com",
                "phone", "138****" + String.format("%04d", userId % 10000),
                "avatar", "",
                "status", 1,
                "createTime", System.currentTimeMillis()
            );

            // 缓存用户信息（5分钟）
            redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(userInfo), 5, TimeUnit.MINUTES);

            return userInfo;
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

            // 构造基本权限信息（在实际项目中，这里应该从数据库获取）
            List<String> roles = Arrays.asList("user");
            List<String> permissions = Arrays.asList("system:user:view", "system:user:edit");

            // 如果是管理员用户，添加更多权限
            if (userId.equals(1L)) {
                roles = Arrays.asList("admin", "user");
                permissions = Arrays.asList(
                    "system:user:view", "system:user:edit", "system:user:add", "system:user:delete",
                    "system:role:view", "system:role:edit", "system:menu:view"
                );
            }

            Map<String, Object> permissionInfo = Map.of(
                "userId", userId,
                "roles", roles,
                "permissions", permissions
            );

            // 缓存权限信息（10分钟）
            redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(permissionInfo), 10, TimeUnit.MINUTES);

            return permissionInfo;
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
