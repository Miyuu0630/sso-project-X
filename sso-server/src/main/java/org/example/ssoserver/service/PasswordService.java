package org.example.ssoserver.service;

/**
 * 密码服务接口
 * 提供密码加密、验证、强度检查等功能
 * 使用MD5+随机盐值进行密码加密
 */
public interface PasswordService {

    /**
     * 生成随机盐值
     * @return 32位随机盐值
     */
    String generateSalt();

    /**
     * 使用MD5+盐值加密密码
     * @param rawPassword 原始密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    String encodePasswordWithSalt(String rawPassword, String salt);

    /**
     * 验证密码（使用盐值）
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @param salt 盐值
     * @return 是否匹配
     */
    boolean matchesWithSalt(String rawPassword, String encodedPassword, String salt);

    /**
     * 加密密码（兼容旧接口）
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    String encodePassword(String rawPassword);

    /**
     * 验证密码（兼容旧接口，不推荐使用）
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean matches(String rawPassword, String encodedPassword);

    /**
     * 检查密码强度
     * @param password 密码
     * @return 强度等级：0-弱，1-中，2-强
     */
    int checkPasswordStrength(String password);

    /**
     * 验证密码格式
     * @param password 密码
     * @return 是否符合要求
     */
    boolean isValidPassword(String password);

    /**
     * 生成随机密码
     * @param length 密码长度
     * @return 随机密码
     */
    String generateRandomPassword(int length);

    /**
     * 检查密码是否为常见弱密码
     * @param password 密码
     * @return 是否为弱密码
     */
    boolean isWeakPassword(String password);
}
