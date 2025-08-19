package org.example.ssoserver.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.example.ssoserver.service.SysPermissionService;
import org.example.ssoserver.service.SysRoleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token配置类
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {
    
    private final SysRoleService roleService;
    private final SysPermissionService permissionService;
    
    /**
     * 注册Sa-Token拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册Sa-Token拦截器，校验规则为StpUtil.checkLogin()登录校验
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter
                // 登录校验 -- 拦截所有路由，并排除/auth/login、/auth/register等用于开放访问的路由
                .match("/**")
                .notMatch("/auth/login", "/auth/register", "/auth/check-username", 
                         "/auth/check-phone", "/auth/check-email", "/auth/send-sms-code", 
                         "/auth/send-email-code", "/auth/captcha", "/auth/verify-captcha",
                         "/auth/wechat/**", "/auth/alipay/**", "/error", "/favicon.ico")
                .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
    
    /**
     * 自定义权限验证接口扩展
     */
    @Bean
    public StpInterface stpInterface() {
        return new StpInterface() {
            
            /**
             * 返回一个账号所拥有的权限码集合
             */
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                Long userId = Long.valueOf(loginId.toString());
                return permissionService.getUserPermissions(userId);
            }
            
            /**
             * 返回一个账号所拥有的角色标识集合
             */
            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                Long userId = Long.valueOf(loginId.toString());
                return roleService.getUserRoles(userId)
                    .stream()
                    .map(role -> role.getRoleCode())
                    .collect(Collectors.toList());
            }
        };
    }
}
