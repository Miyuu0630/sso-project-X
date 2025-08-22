package org.example.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.common.model.ApiResponse;
import org.example.common.result.ResultCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常，返回标准的API响应格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Object> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", e.getCode(), e.getMessage());
        logRequestInfo(request, e);
        
        return ApiResponse.error(e.getCode(), e.getMessage(), e.getData())
                         .requestId(getRequestId(request));
    }
    
    /**
     * 处理参数校验异常 - @Valid注解
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数校验异常: {}", e.getMessage());
        logRequestInfo(request, e);
        
        List<String> errors = e.getBindingResult()
                               .getFieldErrors()
                               .stream()
                               .map(FieldError::getDefaultMessage)
                               .collect(Collectors.toList());
        
        String message = String.join(", ", errors);
        return ApiResponse.badRequest(message)
                         .requestId(getRequestId(request));
    }
    
    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("参数绑定异常: {}", e.getMessage());
        logRequestInfo(request, e);
        
        List<String> errors = e.getFieldErrors()
                               .stream()
                               .map(FieldError::getDefaultMessage)
                               .collect(Collectors.toList());
        
        String message = String.join(", ", errors);
        return ApiResponse.badRequest(message)
                         .requestId(getRequestId(request));
    }
    
    /**
     * 处理约束违反异常 - @Validated注解
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("约束违反异常: {}", e.getMessage());
        logRequestInfo(request, e);
        
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        List<String> errors = violations.stream()
                                       .map(ConstraintViolation::getMessage)
                                       .collect(Collectors.toList());
        
        String message = String.join(", ", errors);
        return ApiResponse.badRequest(message)
                         .requestId(getRequestId(request));
    }
    
    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleMissingParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数: {}", e.getMessage());
        logRequestInfo(request, e);
        
        String message = String.format("缺少必需的请求参数: %s", e.getParameterName());
        return ApiResponse.badRequest(message)
                         .requestId(getRequestId(request));
    }
    
    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型不匹配: {}", e.getMessage());
        logRequestInfo(request, e);
        
        String message = String.format("参数 %s 类型不正确", e.getName());
        return ApiResponse.badRequest(message)
                         .requestId(getRequestId(request));
    }
    
    /**
     * 处理HTTP消息不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("HTTP消息不可读: {}", e.getMessage());
        logRequestInfo(request, e);
        
        return ApiResponse.badRequest("请求体格式错误")
                         .requestId(getRequestId(request));
    }
    
    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Object> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持: {}", e.getMessage());
        logRequestInfo(request, e);
        
        return ApiResponse.error(ResultCode.METHOD_NOT_ALLOWED)
                         .requestId(getRequestId(request));
    }
    
    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Object> handleNotFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("请求路径不存在: {}", e.getRequestURL());
        logRequestInfo(request, e);

        return ApiResponse.notFound("请求的资源不存在")
                         .requestId(getRequestId(request));
    }

    /**
     * 处理静态资源不存在异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Object> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        String resourcePath = e.getResourcePath();

        // 对于favicon.ico等常见静态资源，只记录debug级别日志
        if (resourcePath != null && (resourcePath.contains("favicon.ico") ||
                                   resourcePath.contains("robots.txt") ||
                                   resourcePath.contains(".ico") ||
                                   resourcePath.contains(".png") ||
                                   resourcePath.contains(".jpg"))) {
            log.debug("静态资源不存在: {}", resourcePath);
            // 对于这些资源，返回204 No Content而不是404
            return ApiResponse.success("No Content");
        } else {
            log.warn("静态资源不存在: {}", resourcePath);
            logRequestInfo(request, e);
            return ApiResponse.notFound("请求的资源不存在")
                             .requestId(getRequestId(request));
        }
    }
    
    /**
     * 处理安全异常
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Object> handleSecurityException(SecurityException e, HttpServletRequest request) {
        log.warn("安全异常: {}", e.getMessage());
        logRequestInfo(request, e);
        
        return ApiResponse.forbidden("访问被拒绝")
                         .requestId(getRequestId(request));
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常: {}", e.getMessage());
        logRequestInfo(request, e);
        
        return ApiResponse.badRequest(e.getMessage())
                         .requestId(getRequestId(request));
    }
    
    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常", e);
        logRequestInfo(request, e);
        
        return ApiResponse.error("系统内部错误")
                         .requestId(getRequestId(request));
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常", e);
        logRequestInfo(request, e);
        
        return ApiResponse.error("系统内部错误")
                         .requestId(getRequestId(request));
    }
    
    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常", e);
        logRequestInfo(request, e);
        
        return ApiResponse.error("系统内部错误")
                         .requestId(getRequestId(request));
    }
    
    // ========================================
    // 私有辅助方法
    // ========================================
    
    /**
     * 记录请求信息
     */
    private void logRequestInfo(HttpServletRequest request, Exception e) {
        if (request != null) {
            log.info("异常请求信息 - URI: {}, Method: {}, IP: {}, User-Agent: {}", 
                    request.getRequestURI(), 
                    request.getMethod(),
                    getClientIp(request),
                    request.getHeader("User-Agent"));
        }
    }
    
    /**
     * 获取请求ID
     */
    private String getRequestId(HttpServletRequest request) {
        if (request != null) {
            String requestId = request.getHeader("X-Request-ID");
            if (requestId == null) {
                requestId = request.getHeader("X-Trace-ID");
            }
            return requestId;
        }
        return null;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
