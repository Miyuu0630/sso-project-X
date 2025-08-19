package org.example.ssoserver.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ssoserver.common.Result;
import org.example.ssoserver.service.VerificationCodeService;
import org.example.ssoserver.util.EncryptUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {
    
    private final StringRedisTemplate redisTemplate;
    
    // 验证码缓存key前缀
    private static final String SMS_CODE_PREFIX = "sms_code:";
    private static final String EMAIL_CODE_PREFIX = "email_code:";
    private static final String IMAGE_CAPTCHA_PREFIX = "image_captcha:";
    
    // 验证码有效期（秒）
    private static final int CODE_EXPIRE_TIME = 300; // 5分钟
    private static final int CAPTCHA_EXPIRE_TIME = 120; // 2分钟
    
    @Override
    public Result<Void> sendSmsCode(String phone, String purpose) {
        try {
            if (StrUtil.isBlank(phone)) {
                return Result.error(Result.ResultCode.PARAM_ERROR.getCode(), "手机号不能为空");
            }
            
            // 检查发送频率限制
            String rateLimitKey = SMS_CODE_PREFIX + "rate:" + phone;
            if (redisTemplate.hasKey(rateLimitKey)) {
                return Result.error("发送过于频繁，请稍后再试");
            }
            
            // 生成验证码
            String code = RandomUtil.randomNumbers(6);
            
            // 存储验证码到Redis
            String cacheKey = SMS_CODE_PREFIX + phone + ":" + purpose;
            redisTemplate.opsForValue().set(cacheKey, code, CODE_EXPIRE_TIME, TimeUnit.SECONDS);
            
            // 设置发送频率限制（60秒内不能重复发送）
            redisTemplate.opsForValue().set(rateLimitKey, "1", 60, TimeUnit.SECONDS);
            
            // TODO: 调用短信服务发送验证码
            log.info("发送短信验证码: phone={}, code={}, purpose={}", phone, code, purpose);
            
            return Result.<Void>success();
        } catch (Exception e) {
            log.error("发送短信验证码失败", e);
            return Result.error(Result.ResultCode.SMS_SEND_ERROR);
        }
    }
    
    @Override
    public Result<Void> sendEmailCode(String email, String purpose) {
        try {
            if (StrUtil.isBlank(email)) {
                return Result.error(Result.ResultCode.PARAM_ERROR.getCode(), "邮箱不能为空");
            }
            
            // 检查发送频率限制
            String rateLimitKey = EMAIL_CODE_PREFIX + "rate:" + email;
            if (redisTemplate.hasKey(rateLimitKey)) {
                return Result.error("发送过于频繁，请稍后再试");
            }
            
            // 生成验证码
            String code = RandomUtil.randomNumbers(6);
            
            // 存储验证码到Redis
            String cacheKey = EMAIL_CODE_PREFIX + email + ":" + purpose;
            redisTemplate.opsForValue().set(cacheKey, code, CODE_EXPIRE_TIME, TimeUnit.SECONDS);
            
            // 设置发送频率限制（60秒内不能重复发送）
            redisTemplate.opsForValue().set(rateLimitKey, "1", 60, TimeUnit.SECONDS);
            
            // TODO: 调用邮件服务发送验证码
            log.info("发送邮箱验证码: email={}, code={}, purpose={}", email, code, purpose);
            
            return Result.<Void>success();
        } catch (Exception e) {
            log.error("发送邮箱验证码失败", e);
            return Result.error(Result.ResultCode.EMAIL_SEND_ERROR);
        }
    }
    
    @Override
    public Result<Boolean> verifySmsCode(String phone, String code, String purpose) {
        try {
            if (StrUtil.isBlank(phone) || StrUtil.isBlank(code)) {
                return Result.error(Result.ResultCode.PARAM_ERROR.getCode(), "手机号和验证码不能为空");
            }
            
            String cacheKey = SMS_CODE_PREFIX + phone + ":" + purpose;
            String cachedCode = redisTemplate.opsForValue().get(cacheKey);
            
            if (StrUtil.isBlank(cachedCode)) {
                return Result.success(false); // 验证码不存在或已过期
            }
            
            boolean isValid = code.equals(cachedCode);
            if (isValid) {
                // 验证成功后删除验证码
                redisTemplate.delete(cacheKey);
            }
            
            return Result.success(isValid);
        } catch (Exception e) {
            log.error("验证短信验证码失败", e);
            return Result.error("验证失败");
        }
    }
    
    @Override
    public Result<Boolean> verifyEmailCode(String email, String code, String purpose) {
        try {
            if (StrUtil.isBlank(email) || StrUtil.isBlank(code)) {
                return Result.error(Result.ResultCode.PARAM_ERROR.getCode(), "邮箱和验证码不能为空");
            }
            
            String cacheKey = EMAIL_CODE_PREFIX + email + ":" + purpose;
            String cachedCode = redisTemplate.opsForValue().get(cacheKey);
            
            if (StrUtil.isBlank(cachedCode)) {
                return Result.success(false); // 验证码不存在或已过期
            }
            
            boolean isValid = code.equals(cachedCode);
            if (isValid) {
                // 验证成功后删除验证码
                redisTemplate.delete(cacheKey);
            }
            
            return Result.success(isValid);
        } catch (Exception e) {
            log.error("验证邮箱验证码失败", e);
            return Result.error("验证失败");
        }
    }
    
    @Override
    public Result<Map<String, Object>> generateImageCaptcha() {
        try {
            // 生成验证码key和code
            String key = EncryptUtil.generateRandomString(32);
            String code = RandomUtil.randomString(4).toUpperCase();
            
            // 存储验证码到Redis
            String cacheKey = IMAGE_CAPTCHA_PREFIX + key;
            redisTemplate.opsForValue().set(cacheKey, code, CAPTCHA_EXPIRE_TIME, TimeUnit.SECONDS);
            
            // TODO: 生成验证码图片
            String imageBase64 = generateCaptchaImage(code);
            
            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("image", imageBase64);
            result.put("expiresIn", CAPTCHA_EXPIRE_TIME);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("生成图片验证码失败", e);
            return Result.error("生成失败");
        }
    }
    
    @Override
    public Result<Boolean> verifyImageCaptcha(String key, String code) {
        try {
            if (StrUtil.isBlank(key) || StrUtil.isBlank(code)) {
                return Result.error(Result.ResultCode.PARAM_ERROR.getCode(), "验证码key和code不能为空");
            }
            
            String cacheKey = IMAGE_CAPTCHA_PREFIX + key;
            String cachedCode = redisTemplate.opsForValue().get(cacheKey);
            
            if (StrUtil.isBlank(cachedCode)) {
                return Result.success(false); // 验证码不存在或已过期
            }
            
            boolean isValid = code.toUpperCase().equals(cachedCode.toUpperCase());
            if (isValid) {
                // 验证成功后删除验证码
                redisTemplate.delete(cacheKey);
            }
            
            return Result.success(isValid);
        } catch (Exception e) {
            log.error("验证图片验证码失败", e);
            return Result.error("验证失败");
        }
    }
    
    /**
     * 生成验证码图片（简单实现）
     */
    private String generateCaptchaImage(String code) {
        // TODO: 实现验证码图片生成逻辑
        // 这里返回一个简单的base64字符串作为示例
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
    }
}
