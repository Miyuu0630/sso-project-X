package org.example.ssoserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(
    scanBasePackages = {"org.example.ssoserver", "org.example.common"},
    exclude = {SecurityAutoConfiguration.class}  // 排除Spring Security自动配置
)
@MapperScan(basePackages = "org.example.ssoserver.mapper")
public class SsoServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SsoServerApplication.class, args);
	}
}
