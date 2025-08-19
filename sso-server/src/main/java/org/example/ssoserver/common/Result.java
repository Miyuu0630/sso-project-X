package org.example.ssoserver.common;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一响应结果类
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }
    
    public Result(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }
    
    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage());
    }
    
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message);
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }
    
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage());
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
    
    /**
     * 响应码枚举
     */
    public enum ResultCode {
        SUCCESS(200, "操作成功"),
        ERROR(500, "操作失败"),
        UNAUTHORIZED(401, "未授权"),
        FORBIDDEN(403, "禁止访问"),
        NOT_FOUND(404, "资源不存在"),
        PARAM_ERROR(400, "参数错误"),
        
        // 用户相关
        USER_NOT_FOUND(1001, "用户不存在"),
        USER_DISABLED(1002, "用户已被禁用"),
        USERNAME_EXISTS(1003, "用户名已存在"),
        PHONE_EXISTS(1004, "手机号已存在"),
        EMAIL_EXISTS(1005, "邮箱已存在"),
        PASSWORD_ERROR(1006, "密码错误"),
        OLD_PASSWORD_ERROR(1007, "原密码错误"),
        PASSWORD_NOT_MATCH(1008, "两次密码不一致"),
        
        // 验证码相关
        CAPTCHA_ERROR(2001, "验证码错误"),
        CAPTCHA_EXPIRED(2002, "验证码已过期"),
        SMS_SEND_ERROR(2003, "短信发送失败"),
        EMAIL_SEND_ERROR(2004, "邮件发送失败"),
        
        // 登录相关
        LOGIN_ERROR(3001, "登录失败"),
        ACCOUNT_LOCKED(3002, "账号已被锁定"),
        LOGIN_EXPIRED(3003, "登录已过期"),
        MULTI_DEVICE_LOGIN(3004, "检测到多设备登录"),
        
        // 权限相关
        NO_PERMISSION(4001, "无权限访问"),
        ROLE_NOT_FOUND(4002, "角色不存在"),
        PERMISSION_DENIED(4003, "权限不足");
        
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
}
