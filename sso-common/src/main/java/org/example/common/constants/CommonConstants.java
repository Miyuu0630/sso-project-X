package org.example.common.constants;

/**
 * 公共常量类
 * 定义系统中使用的各种常量
 */
public class CommonConstants {
    
    // ========================================
    // Token相关常量
    // ========================================
    
    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * Token请求头名称
     */
    public static final String TOKEN_HEADER = "Authorization";
    
    /**
     * Sa-Token的token名称
     */
    public static final String SA_TOKEN_NAME = "satoken";
    
    /**
     * 默认Token过期时间（秒）- 2小时
     */
    public static final long DEFAULT_TOKEN_EXPIRE = 7200L;
    
    /**
     * 记住我Token过期时间（秒）- 7天
     */
    public static final long REMEMBER_ME_TOKEN_EXPIRE = 604800L;
    
    // ========================================
    // 用户状态常量
    // ========================================
    
    /**
     * 用户状态：正常
     */
    public static final String USER_STATUS_NORMAL = "1";
    
    /**
     * 用户状态：禁用
     */
    public static final String USER_STATUS_DISABLED = "0";
    
    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";
    
    /**
     * 超级管理员用户ID
     */
    public static final Long SUPER_ADMIN_ID = 1L;
    
    // ========================================
    // 验证码相关常量
    // ========================================
    
    /**
     * 验证码长度
     */
    public static final int VERIFICATION_CODE_LENGTH = 6;
    
    /**
     * 验证码有效期（分钟）
     */
    public static final int VERIFICATION_CODE_EXPIRE_MINUTES = 5;
    
    /**
     * 验证码发送间隔（秒）
     */
    public static final int VERIFICATION_CODE_SEND_INTERVAL = 60;
    
    /**
     * 验证码每日发送限制
     */
    public static final int VERIFICATION_CODE_DAILY_LIMIT = 10;
    
    // ========================================
    // 缓存相关常量
    // ========================================
    
    /**
     * 用户信息缓存前缀
     */
    public static final String CACHE_USER_INFO = "user:info:";
    
    /**
     * 用户权限缓存前缀
     */
    public static final String CACHE_USER_PERMISSIONS = "user:permissions:";
    
    /**
     * 验证码缓存前缀
     */
    public static final String CACHE_VERIFICATION_CODE = "verification:code:";
    
    /**
     * 登录失败次数缓存前缀
     */
    public static final String CACHE_LOGIN_FAIL_COUNT = "login:fail:count:";
    
    /**
     * SSO票据缓存前缀
     */
    public static final String CACHE_SSO_TICKET = "sso:ticket:";
    
    // ========================================
    // 设备和安全相关常量
    // ========================================
    
    /**
     * 设备指纹长度
     */
    public static final int DEVICE_FINGERPRINT_LENGTH = 32;
    
    /**
     * 最大登录失败次数
     */
    public static final int MAX_LOGIN_FAIL_COUNT = 5;
    
    /**
     * 账号锁定时间（分钟）
     */
    public static final int ACCOUNT_LOCK_MINUTES = 30;
    
    /**
     * 密码最小长度
     */
    public static final int PASSWORD_MIN_LENGTH = 6;
    
    /**
     * 密码最大长度
     */
    public static final int PASSWORD_MAX_LENGTH = 20;
    
    // ========================================
    // 第三方登录相关常量
    // ========================================
    
    /**
     * OAuth状态码有效期（分钟）
     */
    public static final int OAUTH_STATE_EXPIRE_MINUTES = 10;
    
    /**
     * OAuth访问令牌默认有效期（秒）
     */
    public static final int OAUTH_ACCESS_TOKEN_EXPIRE = 7200;
    
    /**
     * OAuth刷新令牌默认有效期（秒）
     */
    public static final int OAUTH_REFRESH_TOKEN_EXPIRE = 2592000; // 30天
    
    // ========================================
    // 分页相关常量
    // ========================================
    
    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;
    
    /**
     * 默认页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;
    
    /**
     * 最大页大小
     */
    public static final int MAX_PAGE_SIZE = 100;
    
    // ========================================
    // 文件上传相关常量
    // ========================================
    
    /**
     * 头像文件最大大小（字节）- 2MB
     */
    public static final long AVATAR_MAX_SIZE = 2 * 1024 * 1024;
    
    /**
     * 允许的头像文件类型
     */
    public static final String[] AVATAR_ALLOWED_TYPES = {"jpg", "jpeg", "png", "gif"};
    
    // ========================================
    // 正则表达式常量
    // ========================================
    
    /**
     * 手机号正则表达式
     */
    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    
    /**
     * 邮箱正则表达式
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    
    /**
     * 用户名正则表达式（字母、数字、下划线，4-20位）
     */
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{4,20}$";
    
    /**
     * 密码正则表达式（至少包含字母和数字，6-20位）
     */
    public static final String PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,20}$";
    
    // ========================================
    // 系统配置常量
    // ========================================
    
    /**
     * 系统名称
     */
    public static final String SYSTEM_NAME = "SSO统一认证系统";
    
    /**
     * 系统版本
     */
    public static final String SYSTEM_VERSION = "2.0.0";
    
    /**
     * 系统作者
     */
    public static final String SYSTEM_AUTHOR = "SSO Team";
    
    /**
     * 默认时区
     */
    public static final String DEFAULT_TIMEZONE = "Asia/Shanghai";
    
    /**
     * 默认语言
     */
    public static final String DEFAULT_LANGUAGE = "zh_CN";
    
    // ========================================
    // 私有构造函数，防止实例化
    // ========================================
    
    private CommonConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
