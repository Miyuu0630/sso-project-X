package org.example.ssoserver.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 静态资源控制器
 * 用于提供静态HTML页面
 */
@RestController
public class StaticResourceController {

    /**
     * 提供登录页面
     */
    @GetMapping("/login.html")
    public ResponseEntity<String> getLoginPage() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/login.html");
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("登录页面加载失败: " + e.getMessage());
        }
    }
}
