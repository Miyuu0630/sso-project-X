package org.example.ssoclient.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ssoclient.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * SSO客户端控制器
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class SsoClientController {

    @Value("${sa-token.sso.server-url:http://localhost:8081}")
    private String ssoServerUrl;

    private final UserInfoService userInfoService;
    private final RedisTemplate<String, Object> redisTemplate;

    // 防循环重定向配置
    private static final int MAX_REDIRECT_COUNT = 3;
    private static final String REDIRECT_COUNT_PREFIX = "sso_redirect_count:";
    private static final Duration REDIRECT_COUNT_EXPIRE = Duration.ofMinutes(5);
    
    /**
     * SSO登录回调接口 - 增强版本，包含防循环重定向和安全性检查
     */
    @RequestMapping("/sso-auth")
    public Object ssoAuth(HttpServletRequest request,
                         @RequestParam(required = false) String ticket,
                         @RequestParam(required = false) String state) {
        String clientIp = getClientIp(request);
        log.info("SSO登录回调: {}, ticket: {}, clientIp: {}", request.getRequestURL(),
                ticket != null ? "存在" : "不存在", clientIp);

        try {
            // 1. 防循环重定向检查
            if (!checkRedirectLoop(clientIp)) {
                log.warn("检测到循环重定向，客户端IP: {}", clientIp);
                return Map.of(
                    "code", 429,
                    "message", "重定向次数过多，请稍后重试",
                    "data", Map.of("errorType", "REDIRECT_LOOP")
                );
            }

            if (ticket != null && !ticket.isEmpty()) {
                // 2. Ticket安全性验证
                if (!isValidTicketFormat(ticket)) {
                    log.warn("无效的ticket格式: {}, clientIp: {}", ticket, clientIp);
                    return Map.of(
                        "code", 400,
                        "message", "无效的认证票据",
                        "data", Map.of("errorType", "INVALID_TICKET")
                    );
                }

                // 3. 验证ticket - 使用增强的验证接口
                Map<String, Object> validationResult = validateTicketWithSecurity(ticket, clientIp);

                if ((Integer) validationResult.get("code") == 200) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) validationResult.get("data");

                    if (userData != null) {
                        // 4. 建立安全的本地会话
                        String userId = userData.get("id").toString();
                        establishSecureSession(userId, userData, clientIp);

                        // 5. 清除重定向计数
                        clearRedirectCount(clientIp);

                        log.info("SSO登录成功，用户ID: {}, clientIp: {}", userId, clientIp);

                        // 6. 解析原始URL
                        String originalUrl = decodeState(state);

                        return Map.of(
                            "code", 200,
                            "message", "登录成功",
                            "data", Map.of(
                                "userId", userId,
                                "token", StpUtil.getTokenValue(),
                                "redirectUrl", originalUrl != null ? originalUrl : "/",
                                "userInfo", userData
                            )
                        );
                    }
                } else {
                    log.warn("Ticket验证失败: {}, clientIp: {}", validationResult.get("message"), clientIp);
                }
            }

            // 7. 增加重定向计数
            incrementRedirectCount(clientIp);

            // 8. 构建安全的登录URL
            String currentUrl = request.getRequestURL().toString();
            String encodedState = encodeState(currentUrl);
            String loginUrl = ssoServerUrl + "/sso/auth?redirect=" +
                             java.net.URLEncoder.encode(currentUrl, "UTF-8") +
                             "&state=" + encodedState +
                             "&client_ip=" + clientIp +
                             "&timestamp=" + System.currentTimeMillis();

            return Map.of(
                "code", 302,
                "message", "需要登录",
                "data", Map.of("loginUrl", loginUrl)
            );

        } catch (Exception e) {
            log.error("SSO回调处理失败, clientIp: {}", clientIp, e);
            return Map.of(
                "code", 500,
                "message", "登录失败",
                "data", Map.of("errorType", "INTERNAL_ERROR")
            );
        }
    }

    /**
     * 处理POST请求的SSO回调（供前端调用）
     */
    @PostMapping("/sso-auth")
    public Object ssoAuthPost(@RequestBody Map<String, String> request) {
        String ticket = request.get("ticket");
        log.info("SSO POST回调, ticket: {}", ticket);

        try {
            if (ticket != null && !ticket.isEmpty()) {
                // 验证ticket - 使用正确的接口
                String url = ssoServerUrl + "/sso/validate";
                Map<String, Object> params = Map.of("ticket", ticket);
                String response = HttpUtil.post(url, params);
                JSONObject result = JSONUtil.parseObj(response);

                if (result.getInt("code") == 200) {
                    JSONObject data = result.getJSONObject("data");
                    if (data != null) {
                        // ticket有效，执行本地登录
                        String userId = data.getStr("id");
                        StpUtil.login(userId);

                        log.info("SSO POST登录成功，用户ID: {}", userId);
                        return Map.of(
                            "code", 200,
                            "message", "登录成功",
                            "data", Map.of(
                                "userId", userId,
                                "token", StpUtil.getTokenValue()
                            )
                        );
                    }
                }
            }

            return Map.of(
                "code", 400,
                "message", "ticket无效或已过期"
            );

        } catch (Exception e) {
            log.error("SSO POST回调处理失败", e);
            return Map.of(
                "code", 500,
                "message", "登录失败"
            );
        }
    }
    
    /**
     * 获取登录地址
     */
    @GetMapping("/sso/login-url")
    public Map<String, Object> getLoginUrl(@RequestParam(required = false) String back) {
        String loginUrl = ssoServerUrl + "/sso/auth?redirect=" + 
                         (back != null ? back : "http://localhost:8082/");
        
        return Map.of(
            "loginUrl", loginUrl,
            "serverUrl", ssoServerUrl
        );
    }
    
    /**
     * 检查登录状态
     */
    @GetMapping("/sso/check-login")
    public Map<String, Object> checkLogin() {
        boolean isLogin = StpUtil.isLogin();
        
        Map<String, Object> result = Map.of(
            "isLogin", isLogin,
            "userId", isLogin ? StpUtil.getLoginId() : "",
            "token", isLogin ? StpUtil.getTokenValue() : ""
        );
        
        return result;
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/sso/userinfo")
    public Object getUserInfo() {
        try {
            if (!StpUtil.isLogin()) {
                return Map.of("code", 401, "message", "未登录");
            }

            Map<String, Object> userInfo = userInfoService.getCurrentUserInfo();
            if (userInfo != null) {
                return Map.of("code", 200, "message", "获取成功", "data", userInfo);
            } else {
                return Map.of("code", 500, "message", "获取用户信息失败");
            }
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Map.of("code", 500, "message", "获取用户信息失败");
        }
    }

    /**
     * 获取当前用户权限
     */
    @GetMapping("/sso/user-permissions")
    public Object getUserPermissions() {
        try {
            if (!StpUtil.isLogin()) {
                return Map.of("code", 401, "message", "未登录");
            }

            Map<String, Object> permissions = userInfoService.getCurrentUserPermissions();
            if (permissions != null) {
                return Map.of("code", 200, "message", "获取成功", "data", permissions);
            } else {
                return Map.of("code", 500, "message", "获取权限信息失败");
            }
        } catch (Exception e) {
            log.error("获取用户权限失败", e);
            return Map.of("code", 500, "message", "获取权限信息失败");
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/sso/refresh-token")
    public Object refreshToken() {
        try {
            if (!StpUtil.isLogin()) {
                return Map.of("code", 401, "message", "未登录");
            }

            boolean success = userInfoService.refreshToken();
            if (success) {
                return Map.of("code", 200, "message", "Token刷新成功");
            } else {
                return Map.of("code", 500, "message", "Token刷新失败");
            }
        } catch (Exception e) {
            log.error("Token刷新失败", e);
            return Map.of("code", 500, "message", "Token刷新失败");
        }
    }
    
    /**
     * 获取用户权限（从服务端）
     */
    @GetMapping("/sso/permissions")
    public Object getUserPermissionsFromServer() {
        try {
            if (!StpUtil.isLogin()) {
                return Map.of("code", 401, "message", "未登录");
            }

            String token = StpUtil.getTokenValue();
            String url = ssoServerUrl + "/sso/permissions?ticket=" + token;

            String response = HttpUtil.get(url);
            JSONObject result = JSONUtil.parseObj(response);

            return result;
        } catch (Exception e) {
            log.error("获取用户权限失败", e);
            return Map.of("code", 500, "message", "获取权限失败");
        }
    }
    
    /**
     * 建立本地会话（前端SSO登录成功后调用）
     */
    @PostMapping("/sso/establish-session")
    public Object establishSession(@RequestBody Map<String, Object> sessionData) {
        try {
            String ticket = (String) sessionData.get("ticket");
            String clientToken = (String) sessionData.get("token");
            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = (Map<String, Object>) sessionData.get("userInfo");

            if (ticket == null || userInfo == null || userInfo.get("id") == null) {
                return Map.of("code", 400, "message", "参数不完整");
            }

            // 验证ticket有效性
            String verifyUrl = ssoServerUrl + "/sso/validate";
            Map<String, Object> verifyParams = Map.of("ticket", ticket);
            String verifyResponse = HttpUtil.post(verifyUrl, verifyParams);
            JSONObject verifyResult = JSONUtil.parseObj(verifyResponse);

            if (verifyResult.getInt("code") != 200) {
                return Map.of("code", 401, "message", "Ticket验证失败");
            }

            // 建立本地会话
            Long userId = Long.valueOf(userInfo.get("id").toString());
            StpUtil.login(userId);

            // 存储用户信息到缓存
            String cacheKey = "user_info:" + userId;
            redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(userInfo), Duration.ofMinutes(30));

            log.info("本地会话建立成功, userId: {}, ticket: {}", userId, ticket);
            return Map.of("code", 200, "message", "会话建立成功");

        } catch (Exception e) {
            log.error("建立本地会话失败", e);
            return Map.of("code", 500, "message", "会话建立失败");
        }
    }

    /**
     * 登出
     */
    @PostMapping("/sso/logout")
    public Object logout() {
        try {
            if (StpUtil.isLogin()) {
                Long userId = StpUtil.getLoginIdAsLong();

                // 获取用户当前的ticket（如果有）
                String ticketCacheKey = "user_ticket:" + userId;
                String ticket = (String) redisTemplate.opsForValue().get(ticketCacheKey);

                if (ticket != null) {
                    // 调用认证中心登出接口
                    String url = ssoServerUrl + "/sso/logout";
                    Map<String, Object> params = Map.of("ticket", ticket);
                    HttpUtil.post(url, params);
                }

                // 清除本地缓存
                redisTemplate.delete("user_info:" + userId);
                redisTemplate.delete("user_permissions:" + userId);
                redisTemplate.delete(ticketCacheKey);

                // 本地登出
                StpUtil.logout();
            }

            return Map.of("code", 200, "message", "登出成功");
        } catch (Exception e) {
            log.error("登出失败", e);
            return Map.of("code", 500, "message", "登出失败");
        }
    }
    
    /**
     * 业务接口示例 - 需要登录才能访问
     */
    @GetMapping("/api/user/profile")
    public Object getUserProfile() {
        // 检查登录状态
        StpUtil.checkLogin();
        
        return Map.of(
            "code", 200,
            "message", "获取成功",
            "data", Map.of(
                "userId", StpUtil.getLoginId(),
                "loginTime", System.currentTimeMillis(),
                "clientName", "业务系统1"
            )
        );
    }
    
    /**
     * 业务接口示例 - 需要特定权限才能访问
     */
    @GetMapping("/api/admin/users")
    public Object getUsers() {
        // 检查登录状态
        StpUtil.checkLogin();
        
        // 检查权限
        StpUtil.checkPermission("system:user:list");
        
        return Map.of(
            "code", 200,
            "message", "获取成功",
            "data", "用户列表数据"
        );
    }
    
    /**
     * Token验证接口（供前端调用）
     */
    @GetMapping("/sso/verify")
    public Object verifyToken() {
        try {
            boolean isLogin = StpUtil.isLogin();
            Map<String, Object> result = Map.of(
                "valid", isLogin,
                "userInfo", isLogin ? Map.of("userId", StpUtil.getLoginId()) : null
            );
            return Map.of(
                "code", 200,
                "data", result
            );
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return Map.of(
                "code", 500,
                "message", "验证失败"
            );
        }
    }



    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "SSO Client - 业务系统1<br/>" +
               "登录状态: " + (StpUtil.isLogin() ? "已登录 (用户ID: " + StpUtil.getLoginId() + ")" : "未登录") + "<br/>" +
               "<a href='/sso/login-url'>获取登录地址</a><br/>" +
               "<a href='/sso/userinfo'>获取用户信息</a><br/>" +
               "<a href='/api/user/profile'>获取用户资料</a><br/>" +
               "<a href='/sso/logout'>登出</a>";
    }

    // ========================================
    // 安全性辅助方法
    // ========================================

    /**
     * 检查是否存在循环重定向
     */
    private boolean checkRedirectLoop(String clientIp) {
        try {
            String countKey = REDIRECT_COUNT_PREFIX + clientIp;
            String countStr = (String) redisTemplate.opsForValue().get(countKey);
            int count = countStr != null ? Integer.parseInt(countStr) : 0;
            return count < MAX_REDIRECT_COUNT;
        } catch (Exception e) {
            log.error("检查重定向循环失败", e);
            return true; // 出错时允许重定向
        }
    }

    /**
     * 增加重定向计数
     */
    private void incrementRedirectCount(String clientIp) {
        try {
            String countKey = REDIRECT_COUNT_PREFIX + clientIp;
            String countStr = (String) redisTemplate.opsForValue().get(countKey);
            int count = countStr != null ? Integer.parseInt(countStr) : 0;
            redisTemplate.opsForValue().set(countKey, String.valueOf(count + 1), REDIRECT_COUNT_EXPIRE);
        } catch (Exception e) {
            log.error("增加重定向计数失败", e);
        }
    }

    /**
     * 清除重定向计数
     */
    private void clearRedirectCount(String clientIp) {
        try {
            String countKey = REDIRECT_COUNT_PREFIX + clientIp;
            redisTemplate.delete(countKey);
        } catch (Exception e) {
            log.error("清除重定向计数失败", e);
        }
    }

    /**
     * 验证ticket格式
     */
    private boolean isValidTicketFormat(String ticket) {
        if (ticket == null || ticket.trim().isEmpty()) {
            return false;
        }

        // 基本格式检查：长度、字符集等
        if (ticket.length() < 10 || ticket.length() > 200) {
            return false;
        }

        // 检查是否包含危险字符
        String dangerousChars = "<>\"'&;(){}[]|\\^~`";
        for (char c : dangerousChars.toCharArray()) {
            if (ticket.indexOf(c) != -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * 增强的ticket验证，包含安全性检查
     */
    private Map<String, Object> validateTicketWithSecurity(String ticket, String clientIp) {
        try {
            // 使用GET方法调用票据验证接口
            String url = ssoServerUrl + "/sso/check-ticket?ticket=" + ticket + "&clientId=sso-client1";

            String response = HttpUtil.get(url, 5000); // 5秒超时
            JSONObject result = JSONUtil.parseObj(response);

            return Map.of(
                "code", result.getInt("code"),
                "message", result.getStr("message"),
                "data", result.get("data")
            );

        } catch (Exception e) {
            log.error("Ticket验证失败: ticket={}, clientIp={}", ticket, clientIp, e);
            return Map.of(
                "code", 500,
                "message", "验证服务异常",
                "data", null
            );
        }
    }

    /**
     * 建立安全的本地会话
     */
    private void establishSecureSession(String userId, Map<String, Object> userData, String clientIp) {
        try {
            // 1. Sa-Token登录
            StpUtil.login(userId);

            // 2. 存储用户信息到缓存（增强安全性）
            String userCacheKey = "user_info:" + userId;
            Map<String, Object> secureUserData = new HashMap<>(userData);
            secureUserData.put("loginTime", System.currentTimeMillis());
            secureUserData.put("loginIp", clientIp);
            secureUserData.put("sessionId", StpUtil.getTokenValue());

            redisTemplate.opsForValue().set(userCacheKey, JSONUtil.toJsonStr(secureUserData), Duration.ofHours(2));

            // 3. 记录登录日志
            log.info("用户登录成功: userId={}, clientIp={}, sessionId={}",
                    userId, clientIp, StpUtil.getTokenValue());

        } catch (Exception e) {
            log.error("建立安全会话失败: userId={}, clientIp={}", userId, clientIp, e);
            throw new RuntimeException("会话建立失败", e);
        }
    }

    /**
     * 编码状态参数
     */
    private String encodeState(String originalUrl) {
        try {
            if (originalUrl == null) return null;
            return java.util.Base64.getEncoder().encodeToString(originalUrl.getBytes("UTF-8"));
        } catch (Exception e) {
            log.error("编码状态参数失败", e);
            return null;
        }
    }

    /**
     * 解码状态参数
     */
    private String decodeState(String state) {
        try {
            if (state == null || state.trim().isEmpty()) return null;
            return new String(java.util.Base64.getDecoder().decode(state), "UTF-8");
        } catch (Exception e) {
            log.error("解码状态参数失败", e);
            return null;
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
