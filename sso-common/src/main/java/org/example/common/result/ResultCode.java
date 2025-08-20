package org.example.common.result;

import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
public enum ResultCode {
    
    // 通用响应码
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "参数错误"),
    
    // 用户相关 (1000-1999)
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    USERNAME_EXISTS(1003, "用户名已存在"),
    PHONE_EXISTS(1004, "手机号已存在"),
    EMAIL_EXISTS(1005, "邮箱已存在"),
    PASSWORD_ERROR(1006, "密码错误"),
    OLD_PASSWORD_ERROR(1007, "原密码错误"),
    PASSWORD_NOT_MATCH(1008, "两次密码不一致"),
    USER_LOCKED(1009, "用户已被锁定"),
    USER_EXPIRED(1010, "用户已过期"),
    
    // 验证码相关 (2000-2999)
    CAPTCHA_ERROR(2001, "验证码错误"),
    CAPTCHA_EXPIRED(2002, "验证码已过期"),
    SMS_SEND_ERROR(2003, "短信发送失败"),
    EMAIL_SEND_ERROR(2004, "邮件发送失败"),
    CAPTCHA_REQUIRED(2005, "请输入验证码"),
    
    // 登录相关 (3000-3999)
    LOGIN_ERROR(3001, "登录失败"),
    ACCOUNT_LOCKED(3002, "账号已被锁定"),
    LOGIN_EXPIRED(3003, "登录已过期"),
    MULTI_DEVICE_LOGIN(3004, "检测到多设备登录"),
    TOKEN_INVALID(3005, "Token无效"),
    TOKEN_EXPIRED(3006, "Token已过期"),
    
    // 权限相关 (4000-4999)
    PERMISSION_DENIED(4001, "权限不足"),
    ROLE_NOT_FOUND(4002, "角色不存在"),
    PERMISSION_NOT_FOUND(4003, "权限不存在"),
    
    // SSO相关 (5000-5999)
    SSO_TICKET_INVALID(5001, "SSO票据无效"),
    SSO_TICKET_EXPIRED(5002, "SSO票据已过期"),
    SSO_CLIENT_NOT_FOUND(5003, "SSO客户端不存在"),
    SSO_CLIENT_UNAUTHORIZED(5004, "SSO客户端未授权"),
    SSO_REDIRECT_URL_INVALID(5005, "重定向地址无效"),
    
    // 系统相关 (6000-6999)
    SYSTEM_ERROR(6001, "系统错误"),
    DATABASE_ERROR(6002, "数据库错误"),
    NETWORK_ERROR(6003, "网络错误"),
    FILE_UPLOAD_ERROR(6004, "文件上传失败"),
    FILE_NOT_FOUND(6005, "文件不存在"),
    
    // 业务相关 (7000-7999)
    BUSINESS_ERROR(7001, "业务处理失败"),
    DATA_NOT_FOUND(7002, "数据不存在"),
    DATA_EXISTS(7003, "数据已存在"),
    OPERATION_NOT_ALLOWED(7004, "操作不被允许"),
    
    // 第三方服务相关 (8000-8999)
    THIRD_PARTY_ERROR(8001, "第三方服务错误"),
    WECHAT_ERROR(8002, "微信服务错误"),
    ALIPAY_ERROR(8003, "支付宝服务错误"),
    SMS_SERVICE_ERROR(8004, "短信服务错误"),
    EMAIL_SERVICE_ERROR(8005, "邮件服务错误");
    
    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
