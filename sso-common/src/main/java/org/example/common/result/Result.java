package org.example.common.result;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一响应结果类
 * 简化版的API响应封装
 * 
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    
    // ========================================
    // 静态工厂方法
    // ========================================
    
    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 失败响应（默认错误）
     */
    public static <T> Result<T> error() {
        return Result.<T>builder()
                .code(ResultCode.ERROR.getCode())
                .message(ResultCode.ERROR.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 失败响应（自定义消息）
     */
    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(ResultCode.ERROR.getCode())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 失败响应（自定义错误码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 失败响应（使用ResultCode枚举）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 失败响应（使用ResultCode枚举，带数据）
     */
    public static <T> Result<T> error(ResultCode resultCode, T data) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
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
     * 设置数据并返回当前对象（链式调用）
     */
    public Result<T> data(T data) {
        this.data = data;
        return this;
    }
    
    /**
     * 设置消息并返回当前对象（链式调用）
     */
    public Result<T> message(String message) {
        this.message = message;
        return this;
    }
    
    /**
     * 设置响应码并返回当前对象（链式调用）
     */
    public Result<T> code(Integer code) {
        this.code = code;
        return this;
    }
}
