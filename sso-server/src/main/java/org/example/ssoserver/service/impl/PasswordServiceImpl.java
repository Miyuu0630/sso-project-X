package org.example.ssoserver.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.ssoserver.service.PasswordService;
import org.example.ssoserver.util.Md5SaltUtil;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 密码服务实现
 * 使用MD5+随机盐值进行密码加密和验证
 */
@Slf4j
@Service
public class PasswordServiceImpl implements PasswordService {

    // 密码强度正则表达式
    private static final Pattern WEAK_PATTERN = Pattern.compile("^[a-z]+$|^[A-Z]+$|^\\d+$");
    private static final Pattern MEDIUM_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$");
    private static final Pattern STRONG_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    // 常见弱密码列表
    private static final List<String> WEAK_PASSWORDS = Arrays.asList(
        "123456", "password", "123456789", "12345678", "12345", "1234567", "1234567890",
        "qwerty", "abc123", "111111", "123123", "admin", "letmein", "welcome", "monkey",
        "password123", "admin123", "root", "user", "test", "guest", "demo", "sample"
    );

    /**
     * 生成随机盐值
     * @return 32位随机盐值
     */
    public String generateSalt() {
        return Md5SaltUtil.generateSalt();
    }

    /**
     * 使用MD5+盐值加密密码
     * @param rawPassword 原始密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public String encodePasswordWithSalt(String rawPassword, String salt) {
        return Md5SaltUtil.encrypt(rawPassword, salt);
    }

    @Override
    public String encodePassword(String rawPassword) {
        // 为了兼容旧接口，生成随机盐值并加密
        String salt = generateSalt();
        return encodePasswordWithSalt(rawPassword, salt);
    }

    /**
     * 验证密码（使用盐值）
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @param salt 盐值
     * @return 是否匹配
     */
    public boolean matchesWithSalt(String rawPassword, String encodedPassword, String salt) {
        return Md5SaltUtil.matches(rawPassword, encodedPassword, salt);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        // 为了兼容旧接口，这里无法验证（因为缺少盐值）
        // 建议使用 matchesWithSalt 方法
        log.warn("使用了不推荐的密码验证方法，建议使用 matchesWithSalt");
        return false;
    }

    @Override
    public int checkPasswordStrength(String password) {
        if (StrUtil.isBlank(password)) {
            return 0;
        }

        // 检查是否为弱密码
        if (isWeakPassword(password)) {
            return 0;
        }

        // 强密码检查
        if (STRONG_PATTERN.matcher(password).matches()) {
            return 2;
        }

        // 中等密码检查
        if (MEDIUM_PATTERN.matcher(password).matches()) {
            return 1;
        }

        // 弱密码
        return 0;
    }

    @Override
    public boolean isValidPassword(String password) {
        if (StrUtil.isBlank(password)) {
            return false;
        }

        // 长度检查
        if (password.length() < 6 || password.length() > 50) {
            return false;
        }

        // 基本要求：至少包含字母和数字
        return password.matches("^(?=.*[a-zA-Z])(?=.*\\d).+$");
    }

    @Override
    public String generateRandomPassword(int length) {
        if (length < 6) {
            length = 6;
        }
        if (length > 50) {
            length = 50;
        }

        // 确保包含各种字符类型
        StringBuilder password = new StringBuilder();
        
        // 至少包含一个小写字母
        password.append(RandomUtil.randomChar("abcdefghijklmnopqrstuvwxyz"));
        
        // 至少包含一个大写字母
        password.append(RandomUtil.randomChar("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        
        // 至少包含一个数字
        password.append(RandomUtil.randomChar("0123456789"));
        
        // 至少包含一个特殊字符
        password.append(RandomUtil.randomChar("@$!%*?&"));
        
        // 填充剩余长度
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@$!%*?&";
        for (int i = 4; i < length; i++) {
            password.append(RandomUtil.randomChar(chars));
        }
        
        // 打乱字符顺序
        return RandomUtil.randomString(password.toString(), length);
    }

    @Override
    public boolean isWeakPassword(String password) {
        if (StrUtil.isBlank(password)) {
            return true;
        }

        // 检查是否在弱密码列表中
        String lowerPassword = password.toLowerCase();
        for (String weakPassword : WEAK_PASSWORDS) {
            if (lowerPassword.equals(weakPassword) || 
                lowerPassword.contains(weakPassword) ||
                weakPassword.contains(lowerPassword)) {
                return true;
            }
        }

        // 检查是否为纯数字、纯字母等简单模式
        if (WEAK_PATTERN.matcher(password).matches()) {
            return true;
        }

        // 检查是否为重复字符
        if (isRepeatingPattern(password)) {
            return true;
        }

        // 检查是否为连续字符
        if (isSequentialPattern(password)) {
            return true;
        }

        return false;
    }

    /**
     * 检查是否为重复字符模式
     */
    private boolean isRepeatingPattern(String password) {
        if (password.length() < 3) {
            return false;
        }

        // 检查是否为同一字符重复
        char firstChar = password.charAt(0);
        boolean allSame = true;
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) != firstChar) {
                allSame = false;
                break;
            }
        }
        if (allSame) {
            return true;
        }

        // 检查是否为简单重复模式（如：123123、abcabc）
        for (int len = 1; len <= password.length() / 2; len++) {
            String pattern = password.substring(0, len);
            boolean isRepeating = true;
            for (int i = len; i < password.length(); i += len) {
                int endIndex = Math.min(i + len, password.length());
                String segment = password.substring(i, endIndex);
                if (!pattern.startsWith(segment)) {
                    isRepeating = false;
                    break;
                }
            }
            if (isRepeating && len < password.length()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查是否为连续字符模式
     */
    private boolean isSequentialPattern(String password) {
        if (password.length() < 3) {
            return false;
        }

        // 检查连续递增或递减
        boolean ascending = true;
        boolean descending = true;

        for (int i = 1; i < password.length(); i++) {
            char prev = password.charAt(i - 1);
            char curr = password.charAt(i);

            if (curr != prev + 1) {
                ascending = false;
            }
            if (curr != prev - 1) {
                descending = false;
            }

            if (!ascending && !descending) {
                break;
            }
        }

        return ascending || descending;
    }
}
