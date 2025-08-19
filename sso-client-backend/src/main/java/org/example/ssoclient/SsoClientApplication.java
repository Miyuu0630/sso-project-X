package org.example.ssoclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SsoClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SsoClientApplication.class, args);
	}

}
