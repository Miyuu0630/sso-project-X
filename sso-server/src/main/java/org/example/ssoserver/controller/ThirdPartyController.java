package org.example.ssoserver.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ssoserver.common.Result;
import org.example.ssoserver.service.ThirdPartyLoginService;
import org.example.ssoserver.service.VerificationCodeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 第三方登录控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ThirdPartyController {
    
    private final ThirdPartyLoginService thirdPartyLoginService;
    private final VerificationCodeService verificationCodeService;
    
    /**
     * 获取微信登录授权URL
     */
    @GetMapping("/wechat/auth-url")
    public Result<String> getWechatAuthUrl(@RequestParam(required = false) String state) {
        return thirdPartyLoginService.getWechatAuthUrl(state);
    }
    
    /**
     * 微信登录回调
     */
    @GetMapping("/wechat/callback")
    public Result<Map<String, Object>> wechatCallback(@RequestParam String code, 
                                                     @RequestParam(required = false) String state) {
        return thirdPartyLoginService.wechatCallback(code, state);
    }
    
    /**
     * 获取支付宝登录授权URL
     */
    @GetMapping("/alipay/auth-url")
    public Result<String> getAlipayAuthUrl(@RequestParam(required = false) String state) {
        return thirdPartyLoginService.getAlipayAuthUrl(state);
    }
    
    /**
     * 支付宝登录回调
     */
    @GetMapping("/alipay/callback")
    public Result<Map<String, Object>> alipayCallback(@RequestParam String code, 
                                                     @RequestParam(required = false) String state) {
        return thirdPartyLoginService.alipayCallback(code, state);
    }
    
    /**
     * 绑定第三方账号
     */
    @PostMapping("/bind-third-party")
    public Result<Void> bindThirdPartyAccount(@RequestBody Map<String, String> request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            String platform = request.get("platform");
            String platformUserId = request.get("platformUserId");
            String platformUsername = request.get("platformUsername");
            String accessToken = request.get("accessToken");
            
            return thirdPartyLoginService.bindThirdPartyAccount(userId, platform, platformUserId, 
                                                              platformUsername, accessToken);
        } catch (Exception e) {
            log.error("绑定第三方账号失败", e);
            return Result.error("绑定失败");
        }
    }
    
    /**
     * 解绑第三方账号
     */
    @PostMapping("/unbind-third-party")
    public Result<Void> unbindThirdPartyAccount(@RequestBody Map<String, String> request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            String platform = request.get("platform");
            
            return thirdPartyLoginService.unbindThirdPartyAccount(userId, platform);
        } catch (Exception e) {
            log.error("解绑第三方账号失败", e);
            return Result.error("解绑失败");
        }
    }
    
    /**
     * 获取用户绑定的第三方账号列表
     */
    @GetMapping("/third-party-accounts")
    public Result<Map<String, Object>> getUserThirdPartyAccounts() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            return thirdPartyLoginService.getUserThirdPartyAccounts(userId);
        } catch (Exception e) {
            log.error("获取第三方账号列表失败", e);
            return Result.error("获取失败");
        }
    }
    
    /**
     * 发送短信验证码
     */
    @PostMapping("/send-sms-code")
    public Result<Void> sendSmsCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String purpose = request.get("purpose"); // register, login, reset_password, bind_phone
        
        return verificationCodeService.sendSmsCode(phone, purpose);
    }
    
    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-email-code")
    public Result<Void> sendEmailCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String purpose = request.get("purpose"); // register, login, reset_password, bind_email
        
        return verificationCodeService.sendEmailCode(email, purpose);
    }
    
    /**
     * 生成图片验证码
     */
    @GetMapping("/captcha")
    public Result<Map<String, Object>> generateCaptcha() {
        return verificationCodeService.generateImageCaptcha();
    }
    
    /**
     * 验证图片验证码
     */
    @PostMapping("/verify-captcha")
    public Result<Boolean> verifyCaptcha(@RequestBody Map<String, String> request) {
        String key = request.get("key");
        String code = request.get("code");
        
        return verificationCodeService.verifyImageCaptcha(key, code);
    }
}
