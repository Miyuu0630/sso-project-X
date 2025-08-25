package org.example.ssoclient.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import cn.dev33.satoken.filter.SaServletFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SSO客户端配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SsoClientConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 全局过滤器
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")    // 拦截所有请求
                .addExclude("/favicon.ico", "/static/**", "/error") // 排除静态资源和错误页面
                .setAuth(obj -> {
                    // 权限校验逻辑，可以根据需要写
                    // 对于SSO客户端，我们主要使用拦截器来处理认证
                    log.debug("请求进入 Sa-Token 过滤器: {}", obj);
                })
                .setError(e -> {
                    log.warn("认证失败: {}", e.getMessage());
                    return "{\"code\":401, \"msg\":\"not login\"}";
                });
    }

    /**
     * 配置RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 使用StringRedisSerializer来序列化和反序列化redis的value值
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        log.info("RedisTemplate 配置完成");
        return template;
    }

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5137,http://localhost:8080,http://localhost:8081}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;
    
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
