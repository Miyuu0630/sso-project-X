package org.example.ssoserver.service;


import org.example.common.result.Result;

import java.util.Map;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {
    
    /**
     * 发送短信验证码
     */
    Result<Void> sendSmsCode(String phone, String purpose);
    
    /**
     * 发送邮箱验证码
     */
    Result<Void> sendEmailCode(String email, String purpose);
    
    /**
     * 验证短信验证码
     */
    Result<Boolean> verifySmsCode(String phone, String code, String purpose);
    
    /**
     * 验证邮箱验证码
     */
    Result<Boolean> verifyEmailCode(String email, String code, String purpose);
    
    /**
     * 生成图片验证码
     */
    Result<Map<String, Object>> generateImageCaptcha();
    
    /**
     * 验证图片验证码
     */
    Result<Boolean> verifyImageCaptcha(String key, String code);
}
