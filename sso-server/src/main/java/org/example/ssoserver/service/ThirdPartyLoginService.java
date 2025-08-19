package org.example.ssoserver.service;

import org.example.ssoserver.common.Result;
import java.util.Map;

/**
 * 第三方登录服务接口
 */
public interface ThirdPartyLoginService {
    
    /**
     * 获取微信登录授权URL
     */
    Result<String> getWechatAuthUrl(String state);
    
    /**
     * 微信登录回调处理
     */
    Result<Map<String, Object>> wechatCallback(String code, String state);
    
    /**
     * 获取支付宝登录授权URL
     */
    Result<String> getAlipayAuthUrl(String state);
    
    /**
     * 支付宝登录回调处理
     */
    Result<Map<String, Object>> alipayCallback(String code, String state);
    
    /**
     * 绑定第三方账号
     */
    Result<Void> bindThirdPartyAccount(Long userId, String platform, String platformUserId, 
                                      String platformUsername, String accessToken);
    
    /**
     * 解绑第三方账号
     */
    Result<Void> unbindThirdPartyAccount(Long userId, String platform);
    
    /**
     * 获取用户绑定的第三方账号列表
     */
    Result<Map<String, Object>> getUserThirdPartyAccounts(Long userId);
}
