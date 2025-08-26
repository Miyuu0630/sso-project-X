package org.example.ssoserver.service;

import org.example.ssoserver.dto.RegisterRequest;
import org.example.ssoserver.dto.RegisterResponse;

/**
 * 用户注册服务接口
 */
public interface UserRegisterService {

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    RegisterResponse register(RegisterRequest registerRequest);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExists(String username);

    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return 是否存在
     */
    boolean isPhoneExists(String phone);

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean isEmailExists(String email);

    /**
     * 验证注册数据
     * 
     * @param registerRequest 注册请求
     * @throws IllegalArgumentException 验证失败时抛出异常
     */
    void validateRegisterData(RegisterRequest registerRequest);
}
