package org.example.ssoserver.config;

import lombok.RequiredArgsConstructor;
import org.example.ssoserver.security.CustomAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.web.SecurityFilterChain;
import java.util.Arrays;

/**
 * Spring Security配置类
 * 配置自定义认证提供者，使用 MD5 + 盐值进行密码验证
 * 同时保持与 Sa-Token 的兼容性
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationProvider customAuthenticationProvider;
    

    /**
     * CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的源地址
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5137",
            "http://localhost:8080",
            "http://localhost:8082",
            "http://127.0.0.1:*",
            "http://localhost:*"
        ));
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 允许发送凭据
        configuration.setAllowCredentials(true);
        // 预检请求缓存时间（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 禁用CSRF保护
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用表单登录
            .formLogin(AbstractHttpConfigurer::disable)
            // 禁用HTTP Basic认证
            .httpBasic(AbstractHttpConfigurer::disable)
            // 禁用默认登出
            .logout(AbstractHttpConfigurer::disable)
            // 禁用会话管理
            .sessionManagement(AbstractHttpConfigurer::disable)
            // 禁用默认的安全头
            .headers(AbstractHttpConfigurer::disable)
            // 允许所有请求（完全由Sa-Token处理认证）
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    /**
     * 暴露 AuthenticationManager Bean
     * 用于在 Controller 中手动进行认证
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 密码编码器
     * 由于我们使用自定义的 MD5 + 盐值加密，这里使用 NoOpPasswordEncoder
     * 实际的密码验证在 CustomAuthenticationProvider 中进行
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 注意：这里使用 NoOpPasswordEncoder 是因为我们在 CustomAuthenticationProvider 中
        // 自己处理密码验证，Spring Security 的 PasswordEncoder 不会被使用
        return NoOpPasswordEncoder.getInstance();
    }
}
