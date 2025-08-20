package org.example.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * 需要校验的权限码
     */
    String[] value() default {};
    
    /**
     * 验证模式：AND-必须拥有所有权限，OR-只需拥有其中一个权限
     */
    Mode mode() default Mode.AND;
    
    enum Mode {
        AND, OR
    }
}
