package org.example.ssoserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SSO票据信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SsoTicketInfo {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 重定向URI
     */
    private String redirectUri;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
