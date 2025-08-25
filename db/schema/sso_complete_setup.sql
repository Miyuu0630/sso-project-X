-- ========================================
-- SSO系统完整数据库安装脚本
-- 版本: 2.0.0
-- 创建时间: 2025-08-22
-- 说明: 一键安装SSO系统所有数据库结构和初始数据
-- 包含: 基础表结构 + 安全增强 + 第三方登录 + 初始数据
-- ========================================

-- 删除已存在的数据库（谨慎操作）
-- DROP DATABASE IF EXISTS sso_db;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS sso_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sso_db;

-- ========================================
-- 第一部分：核心业务表
-- ========================================

-- 1. 用户基础信息表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（支持明文或加密）',
    nickname VARCHAR(100) COMMENT '昵称',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    birthday DATE COMMENT '生日',
    status CHAR(1) DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
    user_type VARCHAR(20) DEFAULT 'normal' COMMENT '用户类型：normal-普通用户，enterprise-企业用户，airline-航司用户',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    login_count INT DEFAULT 0 COMMENT '登录次数',
    -- 安全增强字段
    password_update_time DATETIME COMMENT '密码更新时间',
    failed_login_count INT DEFAULT 0 COMMENT '连续登录失败次数',
    last_failed_login_time DATETIME COMMENT '最后失败登录时间',
    is_locked TINYINT DEFAULT 0 COMMENT '是否锁定：0-否，1-是',
    lock_time DATETIME COMMENT '锁定时间',
    -- 审计字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    remark VARCHAR(500) COMMENT '备注',
    -- 索引
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_user_type (user_type),
    INDEX idx_create_time (create_time)
) COMMENT '用户信息表';

-- 2. 角色表
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL UNIQUE COMMENT '角色权限字符串',
    role_sort INT DEFAULT 0 COMMENT '显示顺序',
    data_scope CHAR(1) DEFAULT '1' COMMENT '数据范围：1-全部，2-自定义，3-本部门，4-本部门及以下，5-仅本人',
    menu_check_strictly TINYINT DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
    dept_check_strictly TINYINT DEFAULT 1 COMMENT '部门树选择项是否关联显示',
    status CHAR(1) DEFAULT '1' COMMENT '状态：0-停用，1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_role_key (role_key),
    INDEX idx_status (status)
) COMMENT '角色信息表';

-- 3. 权限菜单表
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    path VARCHAR(200) COMMENT '路由地址',
    component VARCHAR(255) COMMENT '组件路径',
    query_param VARCHAR(255) COMMENT '路由参数',
    is_frame TINYINT DEFAULT 1 COMMENT '是否为外链：0-是，1-否',
    is_cache TINYINT DEFAULT 0 COMMENT '是否缓存：0-缓存，1-不缓存',
    menu_type CHAR(1) COMMENT '菜单类型：M-目录，C-菜单，F-按钮',
    visible CHAR(1) DEFAULT '1' COMMENT '显示状态：0-隐藏，1-显示',
    status CHAR(1) DEFAULT '1' COMMENT '菜单状态：0-停用，1-正常',
    perms VARCHAR(100) COMMENT '权限标识',
    icon VARCHAR(100) COMMENT '菜单图标',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_parent_id (parent_id),
    INDEX idx_menu_type (menu_type),
    INDEX idx_status (status)
) COMMENT '菜单权限表';

-- 4. 用户角色关联表
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) COMMENT '用户和角色关联表';

-- 5. 角色菜单关联表
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) COMMENT '角色和菜单关联表';

-- ========================================
-- 第二部分：日志和安全表
-- ========================================

-- 6. 登录历史表
CREATE TABLE sys_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '访问ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户账号',
    login_type VARCHAR(20) DEFAULT 'password' COMMENT '登录类型：password-密码，sms-短信，oauth-第三方',
    login_ip VARCHAR(50) COMMENT '登录IP地址',
    login_location VARCHAR(255) COMMENT '登录地点',
    browser VARCHAR(50) COMMENT '浏览器类型',
    os VARCHAR(50) COMMENT '操作系统',
    device_type VARCHAR(20) COMMENT '设备类型：mobile-手机，desktop-桌面，tablet-平板',
    device_fingerprint VARCHAR(100) COMMENT '设备指纹',
    is_new_device TINYINT DEFAULT 0 COMMENT '是否新设备：0-否，1-是',
    is_abnormal TINYINT DEFAULT 0 COMMENT '是否异常登录：0-否，1-是',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    logout_time DATETIME COMMENT '登出时间',
    login_duration INT COMMENT '登录时长（秒）',
    status CHAR(1) DEFAULT '1' COMMENT '登录状态：0-失败，1-成功',
    msg VARCHAR(255) COMMENT '提示消息',
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_login_type (login_type),
    INDEX idx_login_time (login_time),
    INDEX idx_status (status)
) COMMENT '系统访问记录';

-- 7. 用户设备表
CREATE TABLE user_device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id VARCHAR(100) NOT NULL COMMENT '设备唯一标识（设备指纹）',
    device_name VARCHAR(100) COMMENT '设备名称',
    device_type VARCHAR(20) COMMENT '设备类型：mobile-手机，desktop-桌面，tablet-平板',
    browser VARCHAR(50) COMMENT '浏览器信息',
    os VARCHAR(50) COMMENT '操作系统',
    login_ip VARCHAR(50) COMMENT '登录IP地址',
    login_location VARCHAR(100) COMMENT '登录地点',
    last_login_time DATETIME COMMENT '最后登录时间',
    is_active TINYINT DEFAULT 1 COMMENT '是否活跃：0-否，1-是',
    is_trusted TINYINT DEFAULT 0 COMMENT '是否信任设备：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_device (user_id, device_id),
    INDEX idx_user_id (user_id),
    INDEX idx_device_id (device_id),
    INDEX idx_last_login_time (last_login_time)
) COMMENT '用户设备表';

-- 8. 密码历史表
CREATE TABLE user_password_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '用户密码历史表';

-- 9. 验证码记录表
CREATE TABLE verification_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    code_type VARCHAR(20) NOT NULL COMMENT '验证码类型：sms-短信，email-邮箱',
    target VARCHAR(100) NOT NULL COMMENT '目标（手机号或邮箱）',
    code VARCHAR(10) NOT NULL COMMENT '验证码',
    purpose VARCHAR(30) NOT NULL COMMENT '用途：login-登录，register-注册，reset-重置密码，unlock-解锁账号，bind-绑定手机/邮箱',
    is_used TINYINT DEFAULT 0 COMMENT '是否已使用：0-否，1-是',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_target_type (target, code_type),
    INDEX idx_expire_time (expire_time)
) COMMENT '验证码记录表';

-- ========================================
-- 第三部分：第三方登录相关表
-- ========================================

-- 10. 第三方账号绑定表
CREATE TABLE user_oauth_binding (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    provider VARCHAR(20) NOT NULL COMMENT '第三方平台：wechat-微信，alipay-支付宝，qq-QQ，github-GitHub',
    open_id VARCHAR(100) NOT NULL COMMENT '第三方平台用户唯一标识',
    union_id VARCHAR(100) COMMENT '第三方平台统一标识',
    nickname VARCHAR(100) COMMENT '第三方平台昵称',
    avatar VARCHAR(255) COMMENT '第三方平台头像',
    access_token VARCHAR(500) COMMENT '访问令牌',
    refresh_token VARCHAR(500) COMMENT '刷新令牌',
    expires_in INT COMMENT '令牌过期时间（秒）',
    -- 增强字段
    scope VARCHAR(200) COMMENT '授权范围',
    token_type VARCHAR(20) DEFAULT 'Bearer' COMMENT 'Token类型',
    refresh_expires_in INT COMMENT '刷新令牌过期时间（秒）',
    raw_user_info JSON COMMENT '原始用户信息',
    bind_ip VARCHAR(50) COMMENT '绑定IP',
    last_refresh_time DATETIME COMMENT '最后刷新时间',
    -- 时间字段
    bind_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    is_active TINYINT DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    -- 索引
    UNIQUE KEY uk_provider_openid (provider, open_id),
    INDEX idx_user_id (user_id),
    INDEX idx_provider (provider),
    INDEX idx_union_id (union_id),
    INDEX idx_last_login_time (last_login_time),
    INDEX idx_bind_time (bind_time),
    INDEX idx_provider_active (provider, is_active)
) COMMENT '第三方账号绑定表';

-- 11. 第三方应用配置表
CREATE TABLE oauth_app_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    provider VARCHAR(20) NOT NULL COMMENT '第三方平台：wechat,alipay,qq,github,dingtalk',
    app_id VARCHAR(100) NOT NULL COMMENT '应用ID',
    app_secret VARCHAR(200) NOT NULL COMMENT '应用密钥',
    app_name VARCHAR(100) COMMENT '应用名称',
    redirect_uri VARCHAR(500) COMMENT '回调地址',
    scope VARCHAR(200) COMMENT '授权范围',
    auth_url VARCHAR(500) COMMENT '授权地址',
    token_url VARCHAR(500) COMMENT '获取token地址',
    user_info_url VARCHAR(500) COMMENT '获取用户信息地址',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_provider (provider),
    INDEX idx_is_enabled (is_enabled)
) COMMENT '第三方应用配置表';

-- 12. 第三方登录状态表
CREATE TABLE oauth_login_state (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    state_code VARCHAR(100) NOT NULL COMMENT '状态码',
    provider VARCHAR(20) NOT NULL COMMENT '第三方平台',
    redirect_uri VARCHAR(500) COMMENT '回调地址',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    is_used TINYINT DEFAULT 0 COMMENT '是否已使用',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    use_time DATETIME COMMENT '使用时间',
    UNIQUE KEY uk_state_code (state_code),
    INDEX idx_provider (provider),
    INDEX idx_expire_time (expire_time)
) COMMENT '第三方登录状态表';

-- 13. 第三方登录日志表
CREATE TABLE oauth_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT COMMENT '用户ID',
    provider VARCHAR(20) NOT NULL COMMENT '第三方平台',
    open_id VARCHAR(100) COMMENT '第三方用户ID',
    action VARCHAR(20) NOT NULL COMMENT '操作：login-登录，bind-绑定，unbind-解绑',
    status VARCHAR(20) NOT NULL COMMENT '状态：success-成功，failed-失败',
    error_code VARCHAR(50) COMMENT '错误码',
    error_message VARCHAR(500) COMMENT '错误信息',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_provider (provider),
    INDEX idx_action (action),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) COMMENT '第三方登录日志表';

-- ========================================
-- 第四部分：安全增强表
-- ========================================

-- 14. 密码策略配置表
CREATE TABLE password_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    min_length INT DEFAULT 6 COMMENT '最小长度',
    max_length INT DEFAULT 20 COMMENT '最大长度',
    require_uppercase TINYINT DEFAULT 0 COMMENT '是否需要大写字母',
    require_lowercase TINYINT DEFAULT 0 COMMENT '是否需要小写字母',
    require_number TINYINT DEFAULT 0 COMMENT '是否需要数字',
    require_special TINYINT DEFAULT 0 COMMENT '是否需要特殊字符',
    max_history_count INT DEFAULT 5 COMMENT '密码历史记录数量',
    expire_days INT DEFAULT 90 COMMENT '密码过期天数',
    max_retry_count INT DEFAULT 5 COMMENT '最大重试次数',
    lock_duration INT DEFAULT 30 COMMENT '锁定时长（分钟）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '密码策略配置表';

-- 15. 账号锁定记录表
CREATE TABLE account_lock_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    lock_reason VARCHAR(100) COMMENT '锁定原因',
    lock_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '锁定时间',
    unlock_time DATETIME COMMENT '解锁时间',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    is_active TINYINT DEFAULT 1 COMMENT '是否生效',
    INDEX idx_user_id (user_id),
    INDEX idx_lock_time (lock_time)
) COMMENT '账号锁定记录表';

-- 16. 密码重置请求表
CREATE TABLE password_reset_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    reset_token VARCHAR(100) NOT NULL COMMENT '重置令牌',
    request_type VARCHAR(20) NOT NULL COMMENT '请求类型：sms-短信，email-邮箱',
    target VARCHAR(100) NOT NULL COMMENT '目标（手机号或邮箱）',
    verification_code VARCHAR(10) COMMENT '验证码',
    is_used TINYINT DEFAULT 0 COMMENT '是否已使用',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    use_time DATETIME COMMENT '使用时间',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    INDEX idx_user_id (user_id),
    INDEX idx_reset_token (reset_token),
    INDEX idx_expire_time (expire_time)
) COMMENT '密码重置请求表';

-- 17. 安全事件日志表
CREATE TABLE security_event_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT COMMENT '用户ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型：password_change,account_lock,suspicious_login,etc',
    event_description VARCHAR(500) COMMENT '事件描述',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    risk_level VARCHAR(20) DEFAULT 'LOW' COMMENT '风险级别：LOW,MEDIUM,HIGH',
    is_handled TINYINT DEFAULT 0 COMMENT '是否已处理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_event_type (event_type),
    INDEX idx_risk_level (risk_level),
    INDEX idx_create_time (create_time)
) COMMENT '安全事件日志表';

-- ========================================
-- 第五部分：视图创建
-- ========================================

-- 用户安全状态视图
CREATE OR REPLACE VIEW user_security_status AS
SELECT
    u.id,
    u.username,
    u.status,
    u.is_locked,
    u.failed_login_count,
    u.last_failed_login_time,
    u.password_update_time,
    CASE
        WHEN u.password_update_time IS NULL THEN '未设置'
        WHEN DATEDIFF(NOW(), u.password_update_time) > 90 THEN '已过期'
        WHEN DATEDIFF(NOW(), u.password_update_time) > 60 THEN '即将过期'
        ELSE '正常'
    END as password_status,
    (SELECT COUNT(*) FROM sys_login_log WHERE user_id = u.id AND login_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)) as login_count_30d,
    (SELECT COUNT(*) FROM user_device WHERE user_id = u.id AND is_active = 1) as active_device_count
FROM sys_user u;

-- 第三方登录统计视图
CREATE OR REPLACE VIEW oauth_login_statistics AS
SELECT
    provider as '第三方平台',
    COUNT(DISTINCT user_id) as '绑定用户数',
    COUNT(*) as '总绑定次数',
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as '活跃绑定数',
    MAX(bind_time) as '最近绑定时间',
    MAX(last_login_time) as '最近登录时间'
FROM user_oauth_binding
GROUP BY provider
ORDER BY COUNT(DISTINCT user_id) DESC;

-- 用户第三方绑定状态视图
CREATE OR REPLACE VIEW user_oauth_status AS
SELECT
    u.id as user_id,
    u.username,
    u.real_name,
    GROUP_CONCAT(DISTINCT o.provider) as bound_providers,
    COUNT(o.id) as binding_count,
    MAX(o.last_login_time) as last_oauth_login
FROM sys_user u
LEFT JOIN user_oauth_binding o ON u.id = o.user_id AND o.is_active = 1
GROUP BY u.id, u.username, u.real_name;
