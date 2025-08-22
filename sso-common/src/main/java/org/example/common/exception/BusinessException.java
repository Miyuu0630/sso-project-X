package org.example.common.exception;

import lombok.Getter;
import org.example.common.result.ResultCode;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误消息
     */
    private final String message;
    
    /**
     * 额外数据
     */
    private final Object data;
    
    /**
     * 构造函数 - 使用错误码枚举
     * @param resultCode 错误码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = null;
    }
    
    /**
     * 构造函数 - 使用错误码枚举和自定义消息
     * @param resultCode 错误码枚举
     * @param message 自定义错误消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
        this.data = null;
    }
    
    /**
     * 构造函数 - 使用错误码枚举和额外数据
     * @param resultCode 错误码枚举
     * @param data 额外数据
     */
    public BusinessException(ResultCode resultCode, Object data) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }
    
    /**
     * 构造函数 - 使用错误码枚举、自定义消息和额外数据
     * @param resultCode 错误码枚举
     * @param message 自定义错误消息
     * @param data 额外数据
     */
    public BusinessException(ResultCode resultCode, String message, Object data) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
        this.data = data;
    }
    
    /**
     * 构造函数 - 使用自定义错误码和消息
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = null;
    }
    
    /**
     * 构造函数 - 使用自定义错误码、消息和额外数据
     * @param code 错误码
     * @param message 错误消息
     * @param data 额外数据
     */
    public BusinessException(Integer code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    /**
     * 构造函数 - 使用自定义错误码、消息和原因
     * @param code 错误码
     * @param message 错误消息
     * @param cause 原因
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.data = null;
    }
    
    // ========================================
    // 静态工厂方法
    // ========================================
    
    /**
     * 创建用户不存在异常
     */
    public static BusinessException userNotFound() {
        return new BusinessException(ResultCode.USER_NOT_FOUND);
    }
    
    /**
     * 创建用户不存在异常（自定义消息）
     */
    public static BusinessException userNotFound(String message) {
        return new BusinessException(ResultCode.USER_NOT_FOUND, message);
    }
    
    /**
     * 创建密码错误异常
     */
    public static BusinessException passwordError() {
        return new BusinessException(ResultCode.PASSWORD_ERROR);
    }
    
    /**
     * 创建账号被禁用异常
     */
    public static BusinessException accountDisabled() {
        return new BusinessException(ResultCode.ACCOUNT_DISABLED);
    }
    
    /**
     * 创建账号被锁定异常
     */
    public static BusinessException accountLocked() {
        return new BusinessException(ResultCode.ACCOUNT_LOCKED);
    }
    
    /**
     * 创建验证码错误异常
     */
    public static BusinessException captchaError() {
        return new BusinessException(ResultCode.CAPTCHA_ERROR);
    }
    
    /**
     * 创建验证码过期异常
     */
    public static BusinessException captchaExpired() {
        return new BusinessException(ResultCode.CAPTCHA_EXPIRED);
    }
    
    /**
     * 创建Token无效异常
     */
    public static BusinessException tokenInvalid() {
        return new BusinessException(ResultCode.TOKEN_INVALID);
    }
    
    /**
     * 创建Token过期异常
     */
    public static BusinessException tokenExpired() {
        return new BusinessException(ResultCode.TOKEN_EXPIRED);
    }
    
    /**
     * 创建未授权异常
     */
    public static BusinessException unauthorized() {
        return new BusinessException(ResultCode.UNAUTHORIZED);
    }
    
    /**
     * 创建禁止访问异常
     */
    public static BusinessException forbidden() {
        return new BusinessException(ResultCode.FORBIDDEN);
    }
    
    /**
     * 创建参数错误异常
     */
    public static BusinessException paramError(String message) {
        return new BusinessException(ResultCode.PARAM_ERROR, message);
    }
    
    /**
     * 创建用户名已存在异常
     */
    public static BusinessException usernameExists() {
        return new BusinessException(ResultCode.USERNAME_EXISTS);
    }
    
    /**
     * 创建手机号已存在异常
     */
    public static BusinessException phoneExists() {
        return new BusinessException(ResultCode.PHONE_EXISTS);
    }
    
    /**
     * 创建邮箱已存在异常
     */
    public static BusinessException emailExists() {
        return new BusinessException(ResultCode.EMAIL_EXISTS);
    }
    
    /**
     * 创建登录失败次数过多异常
     */
    public static BusinessException loginFailTooMany() {
        return new BusinessException(ResultCode.LOGIN_FAIL_TOO_MANY);
    }
    
    /**
     * 创建SSO票据无效异常
     */
    public static BusinessException ssoTicketInvalid() {
        return new BusinessException(ResultCode.SSO_TICKET_INVALID);
    }
    
    /**
     * 创建第三方登录失败异常
     */
    public static BusinessException oauthLoginFailed(String message) {
        return new BusinessException(ResultCode.OAUTH_LOGIN_FAILED, message);
    }
    
    @Override
    public String toString() {
        return String.format("BusinessException{code=%d, message='%s', data=%s}", 
                           code, message, data);
    }
}
