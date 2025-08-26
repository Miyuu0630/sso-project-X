package org.example.ssoserver.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 页面控制器
 * 负责渲染登录、注册等页面
 */
@Slf4j
@Controller
public class LoginPageController {

    /**
     * 显示登录页面
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "redirect", required = false) String redirect,
                           @RequestParam(value = "clientId", required = false) String clientId,
                           @RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "message", required = false) String message,
                           Model model) {

        // 如果已经登录，直接重定向
        if (StpUtil.isLogin()) {
            if (redirect != null && !redirect.isEmpty()) {
                return "redirect:" + redirect;
            }
            return "redirect:/";
        }

        // 传递参数到模板
        model.addAttribute("redirect", redirect);
        model.addAttribute("clientId", clientId);
        model.addAttribute("error", error);
        model.addAttribute("message", message);

        log.info("显示登录页面: redirect={}, clientId={}", redirect, clientId);
        return "login";
    }

    // 注意：注册页面的显示已在 SsoPageController 中处理
    // 删除了重复的 /register 映射以避免冲突

    // 注意：根路径 "/" 的映射已在 SsoPageController 中处理
    // 删除了重复的根路径映射以避免冲突
}
