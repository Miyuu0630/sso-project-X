package org.example.ssoserver;

import org.example.ssoserver.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {SsoServerApplication.class, TestConfig.class})
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true"
})
class SsoServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
