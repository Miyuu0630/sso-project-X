package org.example.ssoserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 静态资源控制器
 * 处理favicon.ico等静态资源请求
 */
@Slf4j
@RestController
public class StaticResourceController {

    /**
     * 处理根路径的favicon.ico请求
     */
    @GetMapping("/favicon.ico")
    public void favicon(HttpServletResponse response) throws IOException {
        log.debug("处理favicon.ico请求");
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        response.setContentLength(0);
    }

    /**
     * 处理robots.txt请求
     */
    @GetMapping("/robots.txt")
    public ResponseEntity<String> robots() {
        log.debug("处理robots.txt请求");
        String robotsTxt = "User-agent: *\nDisallow: /";
        return ResponseEntity.ok()
                .header("Content-Type", "text/plain")
                .body(robotsTxt);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
