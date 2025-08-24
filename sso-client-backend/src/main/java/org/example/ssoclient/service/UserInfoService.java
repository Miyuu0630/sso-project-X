package org.example.ssoclient.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.example.ssoclient.dto.UserInfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

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
     * 获取用户信息（返回DTO）
     */
    public UserInfoDTO getUserInfo(Long userId) {
        try {
            if (userId == null) {
                return null;
            }

            String cacheKey = "user_info_dto:" + userId;

            // 先从缓存获取
            String cachedUserInfo = redisTemplate.opsForValue().get(cacheKey);
            if (cachedUserInfo != null) {
                return JSONUtil.toBean(cachedUserInfo, UserInfoDTO.class);
            }

            // 构造用户信息（在实际项目中，这里应该从数据库获取）
            UserInfoDTO userInfo = buildUserInfo(userId);

            // 缓存用户信息（5分钟）
            redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(userInfo), 5, TimeUnit.MINUTES);

            return userInfo;
        } catch (Exception e) {
            log.error("获取用户信息异常, userId: {}", userId, e);
            return null;
        }
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
            String username, nickname, email, phone;
            
            // 根据用户ID设置不同的用户信息
            if (userId.equals(1L)) {
                username = "admin";
                nickname = "系统管理员";
                email = "admin@example.com";
                phone = "13800000001";
            } else if (userId.equals(2L)) {
                username = "personal_user";
                nickname = "张三";
                email = "personal@example.com";
                phone = "13800000002";
            } else if (userId.equals(3L)) {
                username = "enterprise_user";
                nickname = "企业用户";
                email = "enterprise@example.com";
                phone = "13800000003";
            } else if (userId.equals(4L)) {
                username = "airline_user";
                nickname = "航司用户";
                email = "airline@example.com";
                phone = "13800000004";
            } else {
                username = "user" + userId;
                nickname = "用户" + userId;
                email = "user" + userId + "@example.com";
                phone = "138****" + String.format("%04d", userId % 10000);
            }
            
            Map<String, Object> userInfo = Map.of(
                "id", userId,
                "username", username,
                "nickname", nickname,
                "email", email,
                "phone", phone,
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

            // 根据用户ID构造角色和权限信息
            List<String> roles = new ArrayList<>();
            List<String> permissions = new ArrayList<>();

            // 根据用户ID分配角色（在实际项目中，这里应该从数据库获取）
            if (userId.equals(1L)) {
                // 管理员用户
                roles = Arrays.asList("ADMIN");
                permissions = Arrays.asList(
                    "system:user:view", "system:user:edit", "system:user:add", "system:user:delete",
                    "system:role:view", "system:role:edit", "system:menu:view",
                    "monitor:online:view", "monitor:server:view", "monitor:loginlog:view"
                );
            } else if (userId.equals(2L)) {
                // 个人用户
                roles = Arrays.asList("PERSONAL_USER");
                permissions = Arrays.asList(
                    "user:profile:view", "user:profile:edit",
                    "user:security:view", "user:security:edit",
                    "user:oauth:view", "user:device:view"
                );
            } else if (userId.equals(3L)) {
                // 企业用户
                roles = Arrays.asList("ENTERPRISE_USER");
                permissions = Arrays.asList(
                    "enterprise:info:view", "enterprise:info:edit",
                    "enterprise:member:view", "enterprise:member:edit",
                    "enterprise:project:view", "enterprise:project:edit"
                );
            } else if (userId.equals(4L)) {
                // 航司用户
                roles = Arrays.asList("AIRLINE_USER");
                permissions = Arrays.asList(
                    "airline:info:view", "airline:info:edit",
                    "airline:flight:view", "airline:flight:edit",
                    "airline:passenger:view", "airline:passenger:edit"
                );
            } else {
                // 默认个人用户
                roles = Arrays.asList("PERSONAL_USER");
                permissions = Arrays.asList(
                    "user:profile:view", "user:profile:edit"
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

    /**
     * 构建用户信息（模拟数据，实际应从数据库获取）
     */
    private UserInfoDTO buildUserInfo(Long userId) {
        UserInfoDTO.UserInfoDTOBuilder builder = UserInfoDTO.builder()
            .id(userId)
            .username("user" + userId)
            .nickname("用户" + userId)
            .email("user" + userId + "@example.com")
            .phone("138****" + String.format("%04d", userId % 10000))
            .avatar("")
            .status("1")
            .statusDesc("正常")
            .createTime(LocalDateTime.now().minusDays(30));

        // 根据用户ID分配不同的角色和权限
        if (userId.equals(1L)) {
            // 管理员
            builder.realName("系统管理员")
                .userType("admin")
                .userTypeDesc("管理员")
                .roles(Arrays.asList("ADMIN"))
                .permissions(Arrays.asList(
                    "system:user:list", "system:user:add", "system:user:edit", "system:user:delete",
                    "system:role:list", "system:role:add", "system:role:edit", "system:role:delete",
                    "system:menu:list", "system:menu:add", "system:menu:edit", "system:menu:delete",
                    "monitor:online", "monitor:loginlog", "monitor:server", "monitor:performance"
                ));
        } else if (userId.equals(2L)) {
            // 航司用户
            builder.realName("航司管理员")
                .userType("airline")
                .userTypeDesc("航司用户")
                .roles(Arrays.asList("AIRLINE_USER"))
                .permissions(Arrays.asList(
                    "airline:info:view", "airline:info:edit",
                    "airline:flight:list", "airline:flight:add", "airline:flight:edit", "airline:flight:delete",
                    "airline:passenger:list", "airline:passenger:view",
                    "airline:booking:list", "airline:booking:view",
                    "airline:analytics:view"
                ));
        } else if (userId.equals(3L)) {
            // 企业用户
            builder.realName("企业管理员")
                .userType("enterprise")
                .userTypeDesc("企业用户")
                .roles(Arrays.asList("ENTERPRISE_USER"))
                .permissions(Arrays.asList(
                    "enterprise:info:view", "enterprise:info:edit",
                    "enterprise:member:list", "enterprise:member:invite", "enterprise:member:remove",
                    "enterprise:project:list", "enterprise:project:add", "enterprise:project:edit",
                    "enterprise:analytics:view", "enterprise:auth:apply"
                ));
        } else {
            // 个人用户
            builder.realName("个人用户")
                .userType("normal")
                .userTypeDesc("个人用户")
                .roles(Arrays.asList("PERSONAL_USER"))
                .permissions(Arrays.asList(
                    "user:profile:view", "user:profile:edit",
                    "user:security:view", "user:security:edit",
                    "user:oauth:view", "user:oauth:bind",
                    "user:device:view", "user:loginlog:view"
                ));
        }

        return builder.build();
    }
}
