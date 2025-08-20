package org.example.ssoserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 登录页面控制器
 */
@Controller
public class LoginPageController {

    /**
     * 显示登录页面
     */
    @GetMapping("/login")
    public ModelAndView loginPage(@RequestParam(value = "redirect", required = false) String redirect,
                                  @RequestParam(value = "return_url", required = false) String returnUrl) {
        ModelAndView mv = new ModelAndView("login");
        mv.addObject("redirect", redirect);
        mv.addObject("returnUrl", returnUrl);
        return mv;
    }
}
