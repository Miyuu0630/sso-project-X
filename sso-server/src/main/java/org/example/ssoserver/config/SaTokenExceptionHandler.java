package org.example.ssoserver.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.example.common.model.ApiResponse;
import org.example.common.result.ResultCode;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token异常处理器
 * 专门处理Sa-Token相关的异常
 */
@Slf4j
@RestControllerAdvice
@Order(1) // 优先级高于全局异常处理器
public class SaTokenExceptionHandler {

    /**
     * 处理 Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<Void> handleNotLoginException(NotLoginException e) {
        log.warn("用户未登录或token无效: {}", e.getMessage());
        
        // 根据不同的未登录类型返回不同的错误信息
        String message = switch (e.getType()) {
            case NotLoginException.NOT_TOKEN -> "未提供token";
            case NotLoginException.INVALID_TOKEN -> "token无效";
            case NotLoginException.TOKEN_TIMEOUT -> "token已过期";
            case NotLoginException.BE_REPLACED -> "token已被顶下线";
            case NotLoginException.KICK_OUT -> "token已被踢下线";
            default -> "当前会话未登录";
        };
        
        return ApiResponse.<Void>error(ResultCode.UNAUTHORIZED.getCode(), message);
    }

    /**
     * 处理 Sa-Token 权限不足异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public ApiResponse<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足: {}", e.getMessage());
        return ApiResponse.<Void>error(ResultCode.FORBIDDEN.getCode(), "权限不足：" + e.getPermission());
    }

    /**
     * 处理 Sa-Token 角色不足异常
     */
    @ExceptionHandler(NotRoleException.class)
    public ApiResponse<Void> handleNotRoleException(NotRoleException e) {
        log.warn("角色不足: {}", e.getMessage());
        return ApiResponse.<Void>error(ResultCode.FORBIDDEN.getCode(), "角色不足：" + e.getRole());
    }
}
