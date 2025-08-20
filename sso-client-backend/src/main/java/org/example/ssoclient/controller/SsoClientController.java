package org.example.ssoclient.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ssoclient.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    
    /**
     * SSO登录回调接口
     */
    @RequestMapping("/sso-auth")
    public Object ssoAuth(HttpServletRequest request, @RequestParam(required = false) String ticket) {
        log.info("SSO登录回调: {}, ticket: {}", request.getRequestURL(), ticket);

        try {
            if (ticket != null && !ticket.isEmpty()) {
                // 验证ticket
                String url = ssoServerUrl + "/sso/check-ticket?ticket=" + ticket;
                String response = HttpUtil.get(url);
                JSONObject result = JSONUtil.parseObj(response);

                if (result.getInt("code") == 200) {
                    JSONObject data = result.getJSONObject("data");
                    if (data.getBool("valid")) {
                        // ticket有效，执行本地登录
                        String userId = data.getStr("userId");
                        StpUtil.login(userId);

                        log.info("SSO登录成功，用户ID: {}", userId);
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

            // ticket无效或不存在，重定向到认证中心
            String loginUrl = ssoServerUrl + "/sso/auth?redirect=" +
                             request.getRequestURL().toString();

            return Map.of(
                "code", 302,
                "message", "需要登录",
                "data", Map.of("loginUrl", loginUrl)
            );

        } catch (Exception e) {
            log.error("SSO回调处理失败", e);
            return Map.of(
                "code", 500,
                "message", "登录失败"
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
                // 验证ticket
                String url = ssoServerUrl + "/sso/check-ticket?ticket=" + ticket;
                String response = HttpUtil.get(url);
                JSONObject result = JSONUtil.parseObj(response);

                if (result.getInt("code") == 200) {
                    JSONObject data = result.getJSONObject("data");
                    if (data.getBool("valid")) {
                        // ticket有效，执行本地登录
                        String userId = data.getStr("userId");
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
     * 登出
     */
    @PostMapping("/sso/logout")
    public Object logout() {
        try {
            if (StpUtil.isLogin()) {
                String token = StpUtil.getTokenValue();
                
                // 调用认证中心登出接口
                String url = ssoServerUrl + "/sso/single-logout?ticket=" + token;
                HttpUtil.post(url, "");
                
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
}
