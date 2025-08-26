package org.example.ssoserver.config;

import org.springframework.context.annotation.Configuration;

/**
 * 密码编码配置类
 *
 * 注意：此配置类已废弃，密码编码功能已迁移到 SecurityConfig 中
 * 现在使用 MD5 + 盐值方案，不再使用 BCrypt
 *
 * @deprecated 使用 SecurityConfig 中的 passwordEncoder 配置
 */
@Configuration
public class PasswordConfig {

    // 此类已废弃，所有密码编码配置已迁移到 SecurityConfig
    // 保留此类仅为了避免删除时可能的依赖问题
    // 实际的 passwordEncoder Bean 在 SecurityConfig 中定义
}
