package org.example.ssoserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码编码配置类
 * 仅使用Spring Security Crypto进行密码编码，不使用Spring Security的认证和授权功能
 * 认证和授权完全由Sa-Token处理
 */
@Configuration
public class PasswordConfig {

    /**
     * 密码编码器
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
