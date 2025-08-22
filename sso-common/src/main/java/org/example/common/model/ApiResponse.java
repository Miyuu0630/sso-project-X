package org.example.common.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.common.result.ResultCode;

/**
 * 统一API响应封装类
 * 替代原有的Result类，提供更丰富的响应信息
 * 
 * @param <T> 响应数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
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
    
    /**
     * 请求ID（用于链路追踪）
     */
    private String requestId;
    
    /**
     * 服务器信息
     */
    private String server;
    
    /**
     * API版本
     */
    private String version;
    
    /**
     * 额外信息
     */
    private Object extra;
    
    // ========================================
    // 构造方法
    // ========================================
    
    public ApiResponse(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    public ApiResponse(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }
    
    public ApiResponse(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }
    
    public ApiResponse(ResultCode resultCode, T data) {
        this(resultCode.getCode(), resultCode.getMessage(), data);
    }
    
    // ========================================
    // 成功响应静态方法
    // ========================================
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ResultCode.SUCCESS);
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS, data);
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), message);
    }
    
    /**
     * 成功响应（自定义消息和数据）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), message, data);
    }
    
    // ========================================
    // 失败响应静态方法
    // ========================================
    
    /**
     * 失败响应（默认错误）
     */
    public static <T> ApiResponse<T> error() {
        return new ApiResponse<>(ResultCode.ERROR);
    }
    
    /**
     * 失败响应（自定义消息）
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(ResultCode.ERROR.getCode(), message);
    }
    
    /**
     * 失败响应（错误码枚举）
     */
    public static <T> ApiResponse<T> error(ResultCode resultCode) {
        return new ApiResponse<>(resultCode);
    }
    
    /**
     * 失败响应（错误码枚举和数据）
     */
    public static <T> ApiResponse<T> error(ResultCode resultCode, T data) {
        return new ApiResponse<>(resultCode, data);
    }
    
    /**
     * 失败响应（自定义错误码和消息）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message);
    }
    
    /**
     * 失败响应（自定义错误码、消息和数据）
     */
    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
    
    // ========================================
    // 特殊响应静态方法
    // ========================================
    
    /**
     * 未授权响应
     */
    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>(ResultCode.UNAUTHORIZED);
    }
    
    /**
     * 未授权响应（自定义消息）
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(ResultCode.UNAUTHORIZED.getCode(), message);
    }
    
    /**
     * 禁止访问响应
     */
    public static <T> ApiResponse<T> forbidden() {
        return new ApiResponse<>(ResultCode.FORBIDDEN);
    }
    
    /**
     * 禁止访问响应（自定义消息）
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(ResultCode.FORBIDDEN.getCode(), message);
    }
    
    /**
     * 资源不存在响应
     */
    public static <T> ApiResponse<T> notFound() {
        return new ApiResponse<>(ResultCode.NOT_FOUND);
    }
    
    /**
     * 资源不存在响应（自定义消息）
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(ResultCode.NOT_FOUND.getCode(), message);
    }
    
    /**
     * 参数错误响应
     */
    public static <T> ApiResponse<T> badRequest() {
        return new ApiResponse<>(ResultCode.PARAM_ERROR);
    }
    
    /**
     * 参数错误响应（自定义消息）
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(ResultCode.PARAM_ERROR.getCode(), message);
    }
    
    // ========================================
    // 业务方法
    // ========================================
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
    
    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
    
    /**
     * 设置请求ID
     */
    public ApiResponse<T> requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }
    
    /**
     * 设置服务器信息
     */
    public ApiResponse<T> server(String server) {
        this.server = server;
        return this;
    }
    
    /**
     * 设置版本信息
     */
    public ApiResponse<T> version(String version) {
        this.version = version;
        return this;
    }
    
    /**
     * 设置额外信息
     */
    public ApiResponse<T> extra(Object extra) {
        this.extra = extra;
        return this;
    }
    
    /**
     * 获取错误信息（如果是错误响应）
     */
    public String getErrorMessage() {
        return isError() ? message : null;
    }
    
    /**
     * 获取成功数据（如果是成功响应）
     */
    public T getSuccessData() {
        return isSuccess() ? data : null;
    }
}
