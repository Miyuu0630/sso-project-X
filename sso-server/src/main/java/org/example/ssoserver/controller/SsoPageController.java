package org.example.ssoserver.controller;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * SSO 页面控制器
 * 负责渲染登录、注册等页面模板
 * 
 * @author SSO Team
 * @since 2.0.0
 */
@Controller
@RequestMapping("/sso")
@RequiredArgsConstructor
@Slf4j
public class SsoPageController {

    /**
     * 登录页面
     * 支持重定向URL参数和客户端ID参数
     * 
     * @param model Spring MVC Model
     * @param request HTTP请求
     * @param redirectUrl 登录成功后的重定向URL
     * @param clientId 客户端ID
     * @param state OAuth状态参数
     * @param message 提示消息
     * @return 登录页面模板
     */
    @GetMapping("/login")
    public String loginPage(Model model, 
                           HttpServletRequest request,
                           @RequestParam(value = "redirect_url", required = false) String redirectUrl,
                           @RequestParam(value = "client_id", required = false) String clientId,
                           @RequestParam(value = "state", required = false) String state,
                           @RequestParam(value = "message", required = false) String message) {
        
        log.debug("访问登录页面: redirectUrl={}, clientId={}, state={}", redirectUrl, clientId, state);
        
        // 设置重定向URL
        if (StrUtil.isNotBlank(redirectUrl)) {
            model.addAttribute("redirect", redirectUrl);
        }
        
        // 设置客户端ID
        if (StrUtil.isNotBlank(clientId)) {
            model.addAttribute("clientId", clientId);
        }
        
        // 设置OAuth状态参数
        if (StrUtil.isNotBlank(state)) {
            model.addAttribute("state", state);
        }
        
        // 设置提示消息
        if (StrUtil.isNotBlank(message)) {
            model.addAttribute("message", message);
        }
        
        // 设置页面标题
        model.addAttribute("pageTitle", "SSO 统一认证中心 - 登录");
        
        // 设置系统信息
        model.addAttribute("systemName", "SSO 统一认证中心");
        model.addAttribute("systemVersion", "2.0.0");
        
        // 设置API基础路径
        model.addAttribute("apiBaseUrl", "/api/auth");
        
        return "login";  // 返回 templates/login.html
    }

    /**
     * 注册页面
     * 
     * @param model Spring MVC Model
     * @param userType 用户类型（normal-普通用户，enterprise-企业用户，airline-航司用户）
     * @param message 提示消息
     * @return 注册页面模板
     */
    @GetMapping("/register")
    public String registerPage(Model model,
                              @RequestParam(value = "user_type", required = false, defaultValue = "normal") String userType,
                              @RequestParam(value = "message", required = false) String message) {
        
        log.debug("访问注册页面: userType={}", userType);
        
        // 设置用户类型
        model.addAttribute("userType", userType);
        
        // 设置提示消息
        if (StrUtil.isNotBlank(message)) {
            model.addAttribute("message", message);
        }
        
        // 设置页面标题
        model.addAttribute("pageTitle", "SSO 统一认证中心 - 注册");
        
        // 设置系统信息
        model.addAttribute("systemName", "SSO 统一认证中心");
        model.addAttribute("systemVersion", "2.0.0");
        
        // 设置用户类型选项
        model.addAttribute("userTypes", getUserTypeOptions());
        
        // 设置API基础路径
        model.addAttribute("apiBaseUrl", "/api/auth");
        
        return "register";  // 返回 templates/register.html
    }

    /**
     * 首页重定向到登录页
     * 
     * @return 重定向到登录页
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/sso/login";
    }

    /**
     * 根路径重定向到登录页
     * 
     * @return 重定向到登录页
     */
    @GetMapping("")
    public String root() {
        return "redirect:/sso/login";
    }

    /**
     * 错误页面
     * 
     * @param model Spring MVC Model
     * @param error 错误信息
     * @param code 错误代码
     * @return 错误页面模板
     */
    @GetMapping("/error")
    public String errorPage(Model model,
                           @RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "code", required = false) String code) {
        
        log.debug("访问错误页面: error={}, code={}", error, code);
        
        // 设置错误信息
        if (StrUtil.isNotBlank(error)) {
            model.addAttribute("error", error);
        } else {
            model.addAttribute("error", "未知错误");
        }
        
        // 设置错误代码
        if (StrUtil.isNotBlank(code)) {
            model.addAttribute("code", code);
        } else {
            model.addAttribute("code", "500");
        }
        
        // 设置页面标题
        model.addAttribute("pageTitle", "SSO 统一认证中心 - 错误");
        
        return "error";  // 返回 templates/error.html
    }

    /**
     * 成功页面
     * 
     * @param model Spring MVC Model
     * @param message 成功消息
     * @param redirectUrl 重定向URL
     * @param delay 延迟时间（秒）
     * @return 成功页面模板
     */
    @GetMapping("/success")
    public String successPage(Model model,
                             @RequestParam(value = "message", required = false) String message,
                             @RequestParam(value = "redirect_url", required = false) String redirectUrl,
                             @RequestParam(value = "delay", required = false, defaultValue = "3") Integer delay) {
        
        log.debug("访问成功页面: message={}, redirectUrl={}, delay={}", message, redirectUrl, delay);
        
        // 设置成功消息
        if (StrUtil.isNotBlank(message)) {
            model.addAttribute("message", message);
        } else {
            model.addAttribute("message", "操作成功");
        }
        
        // 设置重定向URL
        if (StrUtil.isNotBlank(redirectUrl)) {
            model.addAttribute("redirectUrl", redirectUrl);
            model.addAttribute("delay", delay);
        }
        
        // 设置页面标题
        model.addAttribute("pageTitle", "SSO 统一认证中心 - 成功");
        
        return "success";  // 返回 templates/success.html
    }

    /**
     * 获取用户类型选项
     * 
     * @return 用户类型选项列表
     */
    private java.util.List<java.util.Map<String, String>> getUserTypeOptions() {
        java.util.List<java.util.Map<String, String>> options = new java.util.ArrayList<>();
        
        java.util.Map<String, String> normal = new java.util.HashMap<>();
        normal.put("value", "normal");
        normal.put("label", "普通用户");
        normal.put("description", "个人用户，拥有基本功能权限");
        options.add(normal);
        
        java.util.Map<String, String> enterprise = new java.util.HashMap<>();
        enterprise.put("value", "enterprise");
        enterprise.put("label", "企业用户");
        enterprise.put("description", "企业用户，拥有企业级功能权限");
        options.add(enterprise);
        
        java.util.Map<String, String> airline = new java.util.HashMap<>();
        airline.put("value", "airline");
        airline.put("label", "航司用户");
        airline.put("description", "航空公司用户，拥有航司专用功能权限");
        options.add(airline);
        
        return options;
    }
}

// 注意：根路径映射已在 SsoPageController 的 index() 方法中处理
// 删除了重复的 RootController 类以避免路径冲突
