package org.example.ssoclient.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SSO客户端配置类
 */
@Slf4j
@Configuration
public class SsoClientConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5137,http://localhost:8080,http://localhost:8081}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    /**
     * 注册Sa-Token拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册Sa-Token拦截器，校验规则为StpUtil.checkLogin()登录校验
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter
                // 拦截需要登录的路由
                .match("/api/**")
                .notMatch("/api/public/**", "/sso-auth", "/sso/logout-call", 
                         "/", "/index.html", "/static/**", "/favicon.ico")
                .check(r -> {
                    // 检查是否登录，如果未登录则跳转到SSO认证中心
                    if (!StpUtil.isLogin()) {
                        log.info("用户未登录，跳转到SSO认证中心");
                        // 构建登录地址
                        String loginUrl = "http://localhost:8081/sso/auth?redirect=" +
                                         "http://localhost:8082/sso-auth";
                        throw new RuntimeException("REDIRECT:" + loginUrl);
                    }
                });
        })).addPathPatterns("/**");
    }
    
    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(3600);
    }
}
