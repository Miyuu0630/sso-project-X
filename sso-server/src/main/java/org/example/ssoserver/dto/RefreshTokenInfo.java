package org.example.ssoserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh Token信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenInfo {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 设备指纹
     */
    private String deviceFingerprint;

    /**
     * IP地址
     */
    private String clientIp;
}
