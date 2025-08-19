package org.example.ssoserver.config;

import org.example.ssoserver.mapper.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * 测试配置类 - 模拟 Mapper 接口
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public SysUserMapper sysUserMapper() {
        return mock(SysUserMapper.class);
    }

    @Bean
    @Primary
    public SysRoleMapper sysRoleMapper() {
        return mock(SysRoleMapper.class);
    }

    @Bean
    @Primary
    public SysPermissionMapper sysPermissionMapper() {
        return mock(SysPermissionMapper.class);
    }

    @Bean
    @Primary
    public SysLoginLogMapper sysLoginLogMapper() {
        return mock(SysLoginLogMapper.class);
    }
}
