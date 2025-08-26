package org.example.ssoserver.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.model.ApiResponse;
import org.example.common.model.UserDTO;
import org.example.common.exception.BusinessException;
import org.example.common.result.ResultCode;
import org.example.common.dto.LoginRequest;
import org.example.common.dto.LoginResponse;
import org.example.common.enums.LoginType;
import org.example.ssoserver.dto.RegisterRequest;
import org.example.ssoserver.dto.RegisterResponse;
import org.example.ssoserver.service.AuthService;
import org.example.ssoserver.service.PasswordService;
import org.example.ssoserver.service.SysUserService;
import org.example.ssoserver.service.UserRegisterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * SSO单点登录控制器
 * 提供SSO核心功能：票据生成、验证、单点登出等
 */
@Slf4j
@RestController
@RequestMapping("/sso")
@RequiredArgsConstructor
@Tag(name = "SSO单点登录", description = "SSO单点登录核心接口")
public class SsoController {

    private final AuthService authService;
    private final SysUserService userService;
    private final UserRegisterService userRegisterService;
    private final PasswordService passwordService;
    
    // ========================================
    // SSO认证核心接口
    // ========================================

    /**
     * SSO统一认证入口
     * 当用户在客户端点击登录时，会跳转到这个接口
     */
    @RequestMapping("/auth")
    @Operation(summary = "SSO认证入口", description = "SSO统一认证入口，检查登录状态并生成票据")
    public void ssoAuth(HttpServletRequest request,
                       HttpServletResponse response,
                       @RequestParam(required = false) String redirect,
                       @RequestParam(required = false) String clientId) throws IOException {

        log.info("SSO认证请求: redirect={}, clientId={}", redirect, clientId);

        try {
            // 检查是否已经登录
            if (StpUtil.isLogin()) {
                // 已登录，生成SSO票据并重定向
                Long userId = StpUtil.getLoginIdAsLong();
                String ticket = authService.generateSsoTicket(userId, clientId, redirect);

                String redirectUrl = buildRedirectUrl(redirect, ticket);

                log.info("用户已登录，生成票据并重定向: userId={}, ticket={}, redirectUrl={}",
                        userId, ticket, redirectUrl);

                response.sendRedirect(redirectUrl);
            } else {
                // 未登录，显示登录页面
                log.info("用户未登录，显示登录页面: redirect={}, clientId={}", redirect, clientId);

                try {
                    response.setContentType("text/html;charset=UTF-8");
                    // 使用动态生成的HTML，但修复JavaScript问题
                    String loginHtml = getLoginPageHtml(redirect, clientId);
                    response.getWriter().write(loginHtml);
                    response.getWriter().flush();
                } catch (Exception ex) {
                    log.error("显示登录页面失败", ex);
                    throw ex;
                }
            }
        } catch (Exception e) {
            log.error("SSO认证异常: redirect={}, clientId={}", redirect, clientId, e);
            // 发生异常时显示错误页面
            try {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(getErrorPageHtml("SSO认证失败，请重试", redirect));
            } catch (Exception ex) {
                log.error("显示错误页面失败", ex);
            }
        }
    }

    /**
     * 处理favicon.ico请求
     */
    @GetMapping("/sso/favicon.ico")
    public void favicon(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * SSO登录处理接口
     */
    @PostMapping("/doLogin")
    @Operation(summary = "SSO登录处理", description = "处理用户登录请求")
    public void doLogin(@RequestParam String username,
                       @RequestParam String password,
                       @RequestParam(required = false) String redirect,
                       @RequestParam(required = false) String clientId,
                       @RequestParam(required = false) String expectedRole,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        try {
            log.info("SSO登录请求: username={}, redirect={}, clientId={}, expectedRole={}", username, redirect, clientId, expectedRole);

            // 构建登录请求
            LoginRequest loginRequest = LoginRequest.builder()
                .account(username)
                .password(password)
                .loginType(LoginType.PASSWORD.getCode())
                .expectedRole(expectedRole)
                .rememberMe(false)  // 明确设置rememberMe字段
                .userAgent(request.getHeader("User-Agent"))
                .clientIp(getClientIp(request))
                .redirectUri(redirect)
                .clientId(clientId)
                .build();

            // 执行登录
            LoginResponse loginResponse = authService.ssoLogin(loginRequest);

            if (loginResponse != null && loginResponse.getAccessToken() != null) {
                // 登录成功，生成SSO票据
                Long userId = StpUtil.getLoginIdAsLong();
                String ticket = authService.generateSsoTicket(userId, clientId, redirect);

                log.info("SSO登录成功: username={}, userId={}, ticket={}", username, userId, ticket);

                // 构建重定向URL
                String redirectUrl = buildRedirectUrl(redirect, ticket);

                // 返回JSON响应给前端
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":200,\"message\":\"登录成功\",\"data\":{\"redirectUrl\":\"" + redirectUrl + "\"}}");
            } else {
                // 登录失败
                log.warn("SSO登录失败: username={}", username);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":400,\"message\":\"用户名或密码错误\"}");
            }
        } catch (Exception e) {
            log.error("SSO登录异常: username={}", username, e);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"登录失败：" + e.getMessage() + "\"}");
        }
    }

    /**
     * 验证SSO票据
     * 客户端使用票据换取用户信息
     */
    @PostMapping("/validate")
    @Operation(summary = "验证SSO票据", description = "客户端使用票据换取用户信息")
    public ApiResponse<UserDTO> validateTicket(@RequestParam @NotBlank String ticket,
                                              @RequestParam(required = false) String clientId) {
        try {
            UserDTO user = authService.validateSsoTicket(ticket, clientId);
            if (user != null) {
                log.info("SSO票据验证成功: ticket={}, userId={}, clientId={}",
                        ticket, user.getId(), clientId);
                return ApiResponse.success("票据验证成功", user);
            } else {
                log.warn("SSO票据验证失败: ticket={}, clientId={}", ticket, clientId);
                return ApiResponse.<UserDTO>error(ResultCode.SSO_TICKET_INVALID.getCode(), "票据无效或已过期");
            }
        } catch (Exception e) {
            log.error("SSO票据验证异常: ticket={}, clientId={}", ticket, clientId, e);
            return ApiResponse.<UserDTO>error(ResultCode.SSO_TICKET_INVALID.getCode(), "票据验证失败");
        }
    }

    /**
     * 验证SSO票据（GET方式，兼容前端调用）
     */
    @GetMapping("/validate")
    @Operation(summary = "验证SSO票据", description = "GET方式验证票据，兼容前端调用")
    public ApiResponse<UserDTO> validateTicketGet(@RequestParam @NotBlank String ticket,
                                                 @RequestParam(required = false) String clientId) {
        return validateTicket(ticket, clientId);
    }

    /**
     * 兼容接口：检查票据有效性（GET方式）
     * 为了兼容旧版客户端
     */
    @GetMapping("/check-ticket")
    @Operation(summary = "检查票据有效性", description = "兼容接口，检查票据是否有效")
    public ApiResponse<Map<String, Object>> checkTicket(@RequestParam @NotBlank String ticket,
                                                       @RequestParam(required = false) String clientId) {
        try {
            UserDTO user = authService.validateSsoTicket(ticket, clientId);
            if (user != null) {
                log.info("SSO票据检查成功: ticket={}, userId={}, clientId={}",
                        ticket, user.getId(), clientId);

                Map<String, Object> result = Map.of(
                    "valid", true,
                    "userId", user.getId().toString(),
                    "username", user.getUsername()
                );
                return ApiResponse.success("票据有效", result);
            } else {
                log.warn("SSO票据检查失败: ticket={}, clientId={}", ticket, clientId);
                Map<String, Object> result = Map.of("valid", false);
                return ApiResponse.success("票据无效", result);
            }
        } catch (BusinessException e) {
            log.warn("SSO票据验证失败: ticket={}, reason={}", ticket, e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("SSO票据验证异常: ticket={}", ticket, e);
            return ApiResponse.error("票据验证失败");
        }
    }

    /**
     * SSO单点登出
     * 登出所有已登录的客户端
     */
    @PostMapping("/logout")
    @Operation(summary = "SSO单点登出", description = "单点登出，清除所有客户端的登录状态")
    public ApiResponse<Map<String, Object>> ssoLogout(@RequestParam(required = false) String redirect) {
        try {
            if (StpUtil.isLogin()) {
                Long userId = StpUtil.getLoginIdAsLong();
                boolean success = authService.ssoLogout(userId);

                if (success) {
                    log.info("SSO单点登出成功: userId={}", userId);

                    Map<String, Object> result = new HashMap<>();
                    if (StrUtil.isNotBlank(redirect)) {
                        result.put("redirectUrl", redirect);
                    }

                    return ApiResponse.<Map<String, Object>>success("登出成功", result);
                } else {
                    return ApiResponse.error("登出失败");
                }
            } else {
                return ApiResponse.success("用户未登录");
            }
        } catch (Exception e) {
            log.error("SSO单点登出异常", e);
            return ApiResponse.error("登出失败");
        }
    }

    // ========================================
    // SSO管理接口
    // ========================================

    /**
     * 获取SSO登录状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取SSO状态", description = "获取当前用户的SSO登录状态")
    public ApiResponse<Map<String, Object>> getSsoStatus() {
        try {
            Map<String, Object> status = new HashMap<>();

            if (StpUtil.isLogin()) {
                Long userId = StpUtil.getLoginIdAsLong();
                UserDTO user = userService.convertToDTO(userService.getUserById(userId));

                status.put("isLoggedIn", true);
                status.put("userId", userId);
                status.put("username", user.getUsername());
                status.put("displayName", user.getDisplayName());
                status.put("loginTime", System.currentTimeMillis());
                status.put("tokenTimeout", StpUtil.getTokenTimeout());
            } else {
                status.put("isLoggedIn", false);
            }

            return ApiResponse.success(status);
        } catch (Exception e) {
            log.error("获取SSO状态异常", e);
            return ApiResponse.error("获取SSO状态失败");
        }
    }

    /**
     * 销毁指定票据
     */
    @PostMapping("/destroy-ticket")
    @Operation(summary = "销毁票据", description = "销毁指定的SSO票据")
    public ApiResponse<Void> destroyTicket(@RequestParam @NotBlank String ticket) {
        try {
            boolean success = authService.destroySsoTicket(ticket);
            if (success) {
                log.info("SSO票据销毁成功: ticket={}", ticket);
                return ApiResponse.success("票据销毁成功");
            } else {
                return ApiResponse.error("票据销毁失败");
            }
        } catch (Exception e) {
            log.error("SSO票据销毁异常: ticket={}", ticket, e);
            return ApiResponse.error("票据销毁失败");
        }
    }

    /**
     * 检查用户登录状态
     */
    @GetMapping("/check-login")
    @Operation(summary = "检查登录状态", description = "检查指定用户的登录状态")
    public ApiResponse<Map<String, Object>> checkUserLogin(@RequestParam Long userId) {
        try {
            boolean isLoggedIn = authService.isUserLoggedIn(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("isLoggedIn", isLoggedIn);

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("检查用户登录状态异常: userId={}", userId, e);
            return ApiResponse.error("检查登录状态失败");
        }
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    /**
     * 构建重定向URL
     */
    private String buildRedirectUrl(String redirect, String ticket) {
        if (StrUtil.isBlank(redirect)) {
            // 默认跳转到前端客户端的回调页面
            redirect = "http://localhost:5137/callback";
        }

        if (redirect.contains("?")) {
            return redirect + "&ticket=" + ticket;
        } else {
            return redirect + "?ticket=" + ticket;
        }
    }

    /**
     * 生成登录页面HTML
     */
    private String getLoginPageHtml(String redirect, String clientId) {
        String redirectValue = redirect != null ? redirect : "";
        String clientIdValue = clientId != null ? clientId : "";

        return "<!DOCTYPE html>" +
            "<html lang=\"zh-CN\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
            "<title>SSO统一登录 - 企业级单点登录系统</title>" +
            "<style>" +
            "* { margin: 0; padding: 0; box-sizing: border-box; }" +
            "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; }" +
            ".login-container { background: white; padding: 40px; border-radius: 12px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); width: 100%; max-width: 420px; }" +
            ".login-header { text-align: center; margin-bottom: 30px; }" +
            ".login-header h1 { color: #333; font-size: 28px; margin-bottom: 8px; }" +
            ".login-header p { color: #666; font-size: 14px; }" +
            ".role-selector { margin-bottom: 25px; }" +
            ".role-selector label { display: block; margin-bottom: 8px; color: #555; font-weight: 500; }" +
            ".role-tabs { display: flex; border: 1px solid #e0e0e0; border-radius: 6px; overflow: hidden; }" +
            ".role-tab { flex: 1; padding: 10px 8px; text-align: center; background: #f8f9fa; border: none; cursor: pointer; font-size: 12px; color: #666; transition: all 0.3s; }" +
            ".role-tab.active { background: #007bff; color: white; }" +
            ".role-tab:hover:not(.active) { background: #e9ecef; }" +
            ".form-group { margin-bottom: 20px; }" +
            ".form-group label { display: block; margin-bottom: 8px; color: #555; font-weight: 500; }" +
            ".form-group input { width: 100%; padding: 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; transition: border-color 0.3s; }" +
            ".form-group input:focus { outline: none; border-color: #007bff; box-shadow: 0 0 0 2px rgba(0,123,255,0.25); }" +
            ".login-btn { width: 100%; padding: 14px; background: linear-gradient(135deg, #007bff, #0056b3); color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 16px; font-weight: 500; transition: all 0.3s; }" +
            ".login-btn:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,123,255,0.3); }" +
            ".login-btn:disabled { background: #ccc; cursor: not-allowed; transform: none; box-shadow: none; }" +
            ".message { margin-top: 15px; padding: 10px; border-radius: 6px; font-size: 14px; display: none; }" +
            ".error-msg { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }" +
            ".success-msg { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }" +
            ".demo-accounts { margin-top: 25px; padding: 15px; background: #f8f9fa; border-radius: 6px; font-size: 12px; }" +
            ".demo-accounts h4 { color: #666; margin-bottom: 8px; }" +
            ".demo-accounts .account-item { margin: 5px 0; padding: 5px 8px; background: white; border-radius: 4px; cursor: pointer; transition: background 0.2s; }" +
            ".demo-accounts .account-item:hover { background: #e9ecef; }" +
            ".demo-accounts .role-badge { display: inline-block; padding: 2px 6px; background: #007bff; color: white; border-radius: 3px; font-size: 10px; margin-left: 5px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class=\"login-container\">" +
            "<div class=\"login-header\">" +
            "<h1>SSO统一登录</h1>" +
            "<p>企业级单点登录系统</p>" +
            "</div>" +

            "<div class=\"role-selector\">" +
            "<label>选择用户类型：</label>" +
            "<div class=\"role-tabs\">" +
            "<button type=\"button\" class=\"role-tab active\" onclick=\"document.getElementById('selectedRole').value='PERSONAL_USER'; document.querySelectorAll('.role-tab').forEach(tab => { tab.classList.remove('active'); if(tab.dataset.role==='PERSONAL_USER') tab.classList.add('active'); }); console.log('选择角色: PERSONAL_USER');\" data-role=\"PERSONAL_USER\">个人用户</button>" +
            "<button type=\"button\" class=\"role-tab\" onclick=\"document.getElementById('selectedRole').value='ENTERPRISE_USER'; document.querySelectorAll('.role-tab').forEach(tab => { tab.classList.remove('active'); if(tab.dataset.role==='ENTERPRISE_USER') tab.classList.add('active'); }); console.log('选择角色: ENTERPRISE_USER');\" data-role=\"ENTERPRISE_USER\">企业用户</button>" +
            "<button type=\"button\" class=\"role-tab\" onclick=\"document.getElementById('selectedRole').value='AIRLINE_USER'; document.querySelectorAll('.role-tab').forEach(tab => { tab.classList.remove('active'); if(tab.dataset.role==='AIRLINE_USER') tab.classList.add('active'); }); console.log('选择角色: AIRLINE_USER');\" data-role=\"AIRLINE_USER\">航司用户</button>" +
            "<button type=\"button\" class=\"role-tab\" onclick=\"document.getElementById('selectedRole').value='ADMIN'; document.querySelectorAll('.role-tab').forEach(tab => { tab.classList.remove('active'); if(tab.dataset.role==='ADMIN') tab.classList.add('active'); }); console.log('选择角色: ADMIN');\" data-role=\"ADMIN\">管理员</button>" +
            "</div>" +
            "</div>" +

            "<form id=\"loginForm\">" +
            "<input type=\"hidden\" name=\"redirect\" value=\"" + redirectValue + "\">" +
            "<input type=\"hidden\" name=\"clientId\" value=\"" + clientIdValue + "\">" +
            "<input type=\"hidden\" id=\"selectedRole\" name=\"expectedRole\" value=\"PERSONAL_USER\">" +

            "<div class=\"form-group\">" +
            "<label for=\"username\">用户名/手机号/邮箱</label>" +
            "<input type=\"text\" id=\"username\" name=\"username\" required placeholder=\"请输入用户名\">" +
            "</div>" +

            "<div class=\"form-group\">" +
            "<label for=\"password\">密码</label>" +
            "<input type=\"password\" id=\"password\" name=\"password\" required placeholder=\"请输入密码\">" +
            "</div>" +

            "<button type=\"button\" class=\"login-btn\" id=\"loginBtn\" onclick=\"(function(){const username=document.getElementById('username').value;const password=document.getElementById('password').value;const expectedRole=document.getElementById('selectedRole').value;if(!username||!password){alert('请输入用户名和密码');return;}const formData=new FormData();formData.append('username',username);formData.append('password',password);formData.append('expectedRole',expectedRole);const loginBtn=document.getElementById('loginBtn');const errorMsg=document.getElementById('errorMsg');const successMsg=document.getElementById('successMsg');errorMsg.style.display='none';successMsg.style.display='none';loginBtn.disabled=true;loginBtn.textContent='登录中...';fetch('/sso/doLogin',{method:'POST',body:formData}).then(response=>response.json()).then(data=>{console.log('登录响应:',data);if(data.code===200){successMsg.textContent='登录成功，正在跳转...';successMsg.style.display='block';setTimeout(()=>{if(data.data&&data.data.redirectUrl){console.log('跳转到:',data.data.redirectUrl);window.location.href=data.data.redirectUrl;}else{console.log('跳转到默认地址');window.location.href='/';}},1000);}else{errorMsg.textContent=data.message||'登录失败，请检查用户名和密码';errorMsg.style.display='block';}}).catch(error=>{console.error('登录错误:',error);errorMsg.textContent='网络错误，请稍后重试';errorMsg.style.display='block';}).finally(()=>{loginBtn.disabled=false;loginBtn.textContent='登录';});})()\">登录</button>" +

            "<div class=\"message error-msg\" id=\"errorMsg\"></div>" +
            "<div class=\"message success-msg\" id=\"successMsg\"></div>" +
            "</form>" +

            "<div class=\"demo-accounts\">" +
            "<h4>测试账号：</h4>" +
            "<div class=\"account-item\" onclick=\"fillAccount('admin', 'admin123456')\">管理员: admin / admin123456 <span class=\"role-badge\">ADMIN</span></div>" +
            "<div class=\"account-item\" onclick=\"fillAccount('airline_user', '123456')\">航司用户: airline_user / 123456 <span class=\"role-badge\">AIRLINE</span></div>" +
            "<div class=\"account-item\" onclick=\"fillAccount('enterprise_user', '123456')\">企业用户: enterprise_user / 123456 <span class=\"role-badge\">ENTERPRISE</span></div>" +
            "<div class=\"account-item\" onclick=\"fillAccount('personal_user', '123456')\">个人用户: personal_user / 123456 <span class=\"role-badge\">PERSONAL</span></div>" +
            "</div>" +
            "</div>" +
            "<script>" +
            "// 全局变量" +
            "let currentRole = 'PERSONAL_USER';" +
            "" +
            "// 角色选择功能 - 使用内联onclick" +
            "function selectRole(role) {" +
            "  console.log('选择角色:', role);" +
            "  currentRole = role;" +
            "  " +
            "  // 更新隐藏字段" +
            "  document.getElementById('selectedRole').value = role;" +
            "  " +
            "  // 更新UI显示" +
            "  document.querySelectorAll('.role-tab').forEach(tab => {" +
            "    tab.classList.remove('active');" +
            "    if (tab.dataset.role === role) {" +
            "      tab.classList.add('active');" +
            "    }" +
            "  });" +
            "  " +
            "  console.log('当前选择角色:', currentRole);" +
            "}" +
            "" +
            "// 快速填充账号" +
            "function fillAccount(username, password) {" +
            "  console.log('填充账号:', username, password);" +
            "  " +
            "  document.getElementById('username').value = username;" +
            "  document.getElementById('password').value = password;" +
            "  " +
            "  // 根据用户名自动选择对应角色" +
            "  let expectedRole = 'PERSONAL_USER';" +
            "  if (username === 'admin') {" +
            "    expectedRole = 'ADMIN';" +
            "  } else if (username === 'airline_user') {" +
            "    expectedRole = 'AIRLINE_USER';" +
            "  } else if (username === 'enterprise_user') {" +
            "    expectedRole = 'ENTERPRISE_USER';" +
            "  } else if (username === 'personal_user') {" +
            "    expectedRole = 'PERSONAL_USER';" +
            "  }" +
            "  " +
            "  // 调用角色选择函数" +
            "  selectRole(expectedRole);" +
            "}" +
            "" +
            "// 登录表单提交" +
            "function submitLogin() {" +
            "  console.log('提交登录，当前角色:', currentRole);" +
            "  " +
            "  const username = document.getElementById('username').value;" +
            "  const password = document.getElementById('password').value;" +
            "  " +
            "  if (!username || !password) {" +
            "    alert('请输入用户名和密码');" +
            "    return;" +
            "  }" +
            "  " +
            "  // 构建FormData" +
            "  const formData = new FormData();" +
            "  formData.append('username', username);" +
            "  formData.append('password', password);" +
            "  formData.append('expectedRole', currentRole);" +
            "  " +
            "  const loginBtn = document.getElementById('loginBtn');" +
            "  const errorMsg = document.getElementById('errorMsg');" +
            "  const successMsg = document.getElementById('successMsg');" +
            "" +
            "  // 隐藏消息" +
            "  errorMsg.style.display = 'none';" +
            "  successMsg.style.display = 'none';" +
            "" +
            "  // 禁用按钮" +
            "  loginBtn.disabled = true;" +
            "  loginBtn.textContent = '登录中...';" +
            "" +
            "  fetch('/sso/doLogin', {" +
            "    method: 'POST'," +
            "    body: formData" +
            "  })" +
            "  .then(response => response.json())" +
            "  .then(data => {" +
            "    console.log('登录响应:', data);" +
            "    " +
            "    if (data.code === 200) {" +
            "      successMsg.textContent = '登录成功，正在跳转...';" +
            "      successMsg.style.display = 'block';" +
            "      " +
            "      setTimeout(() => {" +
            "        if (data.data && data.data.redirectUrl) {" +
            "          console.log('跳转到:', data.data.redirectUrl);" +
            "          window.location.href = data.data.redirectUrl;" +
            "        } else {" +
            "          console.log('跳转到默认地址');" +
            "          window.location.href = '/';" +
            "        }" +
            "      }, 1000);" +
            "    } else {" +
            "      errorMsg.textContent = data.message || '登录失败，请检查用户名和密码';" +
            "      errorMsg.style.display = 'block';" +
            "    }" +
            "  })" +
            "  .catch(error => {" +
            "    console.error('登录错误:', error);" +
            "    errorMsg.textContent = '网络错误，请稍后重试';" +
            "    errorMsg.style.display = 'block';" +
            "  })" +
            "  .finally(() => {" +
            "    loginBtn.disabled = false;" +
            "    loginBtn.textContent = '登录';" +
            "  });" +
            "}" +
            "" +
            "// 页面加载完成后初始化" +
            "window.onload = function() {" +
            "  console.log('页面加载完成，初始化完成');" +
            "  console.log('当前选择角色:', currentRole);" +
            "};" +
            "</script>" +
            "</body>" +
            "</html>";
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况，取第一个
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 生成错误页面HTML
     */
    private String getErrorPageHtml(String errorMessage, String redirect) {
        String message = errorMessage != null ? errorMessage : "未知错误";
        String redirectValue = redirect != null ? redirect : "";

        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<title>SSO错误</title>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 50px; }" +
            ".error-container { max-width: 400px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }" +
            ".error-title { color: #dc3545; margin-bottom: 20px; }" +
            ".error-message { color: #666; margin-bottom: 30px; }" +
            ".retry-btn { padding: 10px 20px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; display: inline-block; }" +
            ".retry-btn:hover { background: #0056b3; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class=\"error-container\">" +
            "<h2 class=\"error-title\">登录失败</h2>" +
            "<p class=\"error-message\">" + message + "</p>" +
            "<a href=\"/sso/auth?redirect=" + redirectValue + "\" class=\"retry-btn\">重新登录</a>" +
            "</div>" +
            "</body>" +
            "</html>";
    }

    // ========================================
    // 用户注册相关接口
    // ========================================

    // 注意：注册页面的显示已在 SsoPageController 中处理
    // 删除了重复的 /sso/register 映射以避免冲突

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "处理用户注册请求")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest,
                                                 HttpServletRequest request) {
        String clientIp = getClientIp(request);
        log.info("用户注册请求: username={}, phone={}, email={}, clientIp={}",
                registerRequest.getUsername(), registerRequest.getPhone(),
                registerRequest.getEmail(), clientIp);

        try {
            // 执行注册
            RegisterResponse response = userRegisterService.register(registerRequest);

            log.info("用户注册成功: userId={}, username={}", response.getUserId(), response.getUsername());
            return ApiResponse.success(response);

        } catch (BusinessException e) {
            log.warn("用户注册失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户注册异常: username={}", registerRequest.getUsername(), e);
            return ApiResponse.error("注册失败，请稍后重试");
        }
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否已存在")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        try {
            boolean exists = userRegisterService.isUsernameExists(username);
            return ApiResponse.success(!exists); // 返回是否可用（不存在即可用）
        } catch (Exception e) {
            log.error("检查用户名失败: username={}", username, e);
            return ApiResponse.error("检查失败");
        }
    }

    /**
     * 检查手机号是否可用
     */
    @GetMapping("/check-phone")
    @Operation(summary = "检查手机号", description = "检查手机号是否已注册")
    public ApiResponse<Boolean> checkPhone(@RequestParam String phone) {
        try {
            boolean exists = userRegisterService.isPhoneExists(phone);
            return ApiResponse.success(!exists); // 返回是否可用（不存在即可用）
        } catch (Exception e) {
            log.error("检查手机号失败: phone={}", phone, e);
            return ApiResponse.error("检查失败");
        }
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已注册")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        try {
            boolean exists = userRegisterService.isEmailExists(email);
            return ApiResponse.success(!exists); // 返回是否可用（不存在即可用）
        } catch (Exception e) {
            log.error("检查邮箱失败: email={}", email, e);
            return ApiResponse.error("检查失败");
        }
    }

    /**
     * 检查密码强度
     */
    @PostMapping("/check-password-strength")
    @Operation(summary = "检查密码强度", description = "检查密码强度和安全性")
    public ApiResponse<Map<String, Object>> checkPasswordStrength(@RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            if (password == null || password.trim().isEmpty()) {
                return ApiResponse.error("密码不能为空");
            }

            // 使用密码服务检查强度
            int strength = passwordService.checkPasswordStrength(password);
            boolean isValid = passwordService.isValidPassword(password);
            boolean isWeak = passwordService.isWeakPassword(password);

            Map<String, Object> result = new HashMap<>();
            result.put("strength", strength); // 0-弱，1-中，2-强
            result.put("isValid", isValid);
            result.put("isWeak", isWeak);

            // 提供强度描述
            String strengthText;
            switch (strength) {
                case 0:
                    strengthText = "弱";
                    break;
                case 1:
                    strengthText = "中";
                    break;
                case 2:
                    strengthText = "强";
                    break;
                default:
                    strengthText = "未知";
            }
            result.put("strengthText", strengthText);

            // 提供改进建议
            if (!isValid || isWeak) {
                result.put("suggestions", getPasswordSuggestions(password));
            }

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("检查密码强度失败", e);
            return ApiResponse.error("检查失败");
        }
    }

    // ========================================
    // 注册页面相关私有方法
    // ========================================



    /**
     * 获取简单的注册页面HTML（备用）
     */
    private String getSimpleRegisterPageHtml() {
        return "<!DOCTYPE html>" +
                "<html lang='zh-CN'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<title>用户注册</title>" +
                "<style>body{font-family:Arial,sans-serif;max-width:400px;margin:50px auto;padding:20px;}</style>" +
                "</head>" +
                "<body>" +
                "<h2>用户注册</h2>" +
                "<p>注册页面加载失败，请刷新页面重试。</p>" +
                "<a href='/sso/auth'>返回登录</a>" +
                "</body>" +
                "</html>";
    }

    /**
     * 获取密码改进建议
     */
    private java.util.List<String> getPasswordSuggestions(String password) {
        java.util.List<String> suggestions = new java.util.ArrayList<>();

        if (password.length() < 8) {
            suggestions.add("密码长度至少8位");
        }

        if (!password.matches(".*[a-z].*")) {
            suggestions.add("包含小写字母");
        }

        if (!password.matches(".*[A-Z].*")) {
            suggestions.add("包含大写字母");
        }

        if (!password.matches(".*\\d.*")) {
            suggestions.add("包含数字");
        }

        if (!password.matches(".*[@$!%*?&].*")) {
            suggestions.add("包含特殊字符(@$!%*?&)");
        }

        if (passwordService.isWeakPassword(password)) {
            suggestions.add("避免使用常见密码或简单模式");
        }

        return suggestions;
    }
}
