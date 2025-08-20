-- ========================================
-- SSO系统完整数据库设计
-- 支持手机号、邮箱、第三方账号注册登录
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS sso_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sso_db;

-- ========================================
-- 1. 用户基础信息表
-- ========================================
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_user_type (user_type),
    INDEX idx_create_time (create_time)
) COMMENT '用户信息表';

-- ========================================
-- 2. 角色表
-- ========================================
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

-- ========================================
-- 3. 权限表
-- ========================================
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

-- ========================================
-- 4. 用户角色关联表
-- ========================================
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) COMMENT '用户和角色关联表';

-- ========================================
-- 5. 角色菜单关联表
-- ========================================
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) COMMENT '角色和菜单关联表';

-- ========================================
-- 6. 登录历史表
-- ========================================
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

-- ========================================
-- 7. 第三方账号绑定表
-- ========================================
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
    bind_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    is_active TINYINT DEFAULT 1 COMMENT '是否激活：0-否，1-是',
    UNIQUE KEY uk_provider_openid (provider, open_id),
    INDEX idx_user_id (user_id),
    INDEX idx_provider (provider),
    INDEX idx_union_id (union_id)
) COMMENT '第三方账号绑定表';

-- ========================================
-- 8. 用户设备表
-- ========================================
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

-- ========================================
-- 9. 密码历史表
-- ========================================
CREATE TABLE user_password_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '用户密码历史表';

-- ========================================
-- 10. 验证码记录表
-- ========================================
CREATE TABLE verification_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    code_type VARCHAR(20) NOT NULL COMMENT '验证码类型：sms-短信，email-邮箱',
    target VARCHAR(100) NOT NULL COMMENT '目标（手机号或邮箱）',
    code VARCHAR(10) NOT NULL COMMENT '验证码',
    purpose VARCHAR(20) NOT NULL COMMENT '用途：login-登录，register-注册，reset-重置密码',
    is_used TINYINT DEFAULT 0 COMMENT '是否已使用：0-否，1-是',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_target_type (target, code_type),
    INDEX idx_expire_time (expire_time)
) COMMENT '验证码记录表';
