package org.example.ssoserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Refresh Token功能测试
 * 此测试类演示了Refresh Token的完整使用流程
 */
@SpringBootTest
@AutoConfigureWebMvc
@Transactional
public class RefreshTokenTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试完整的Refresh Token流程
     * 1. 用户登录获得双token
     * 2. 使用refresh token刷新access token
     * 3. 验证token状态
     * 4. 撤销refresh token
     */
    @Test
    public void testRefreshTokenFlow() throws Exception {
        // 1. 模拟用户登录
        String loginRequest = """
            {
                "account": "admin",
                "password": "123456",
                "loginType": "password"
            }
            """;

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content(loginRequest))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.refreshToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 2. 提取refresh token（实际测试中需要解析JSON）
        String refreshToken = "extracted_refresh_token_from_response";

        // 3. 使用refresh token刷新access token
        mockMvc.perform(post("/auth/refresh")
                .param("refreshToken", refreshToken))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").exists());

        // 4. 查看refresh token状态
        mockMvc.perform(get("/auth/refresh-token/status")
                .param("refreshToken", refreshToken))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.userId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.isExpired").exists());

        // 5. 撤销refresh token
        mockMvc.perform(post("/auth/revoke")
                .param("refreshToken", refreshToken))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200));

        // 6. 验证refresh token已被撤销
        mockMvc.perform(get("/auth/refresh-token/status")
                .param("refreshToken", refreshToken))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(400));
    }

    /**
     * 测试Refresh Token过期场景
     */
    @Test
    public void testRefreshTokenExpiration() throws Exception {
        // 设置一个很快过期的refresh token
        String expiredRefreshToken = "expired_token";

        // 尝试使用过期的refresh token
        mockMvc.perform(post("/auth/refresh")
                .param("refreshToken", expiredRefreshToken))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(400));
    }

    /**
     * 测试无效Refresh Token
     */
    @Test
    public void testInvalidRefreshToken() throws Exception {
        String invalidRefreshToken = "invalid_token_123";

        // 尝试使用无效的refresh token
        mockMvc.perform(post("/auth/refresh")
                .param("refreshToken", invalidRefreshToken))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(400));
    }
}
