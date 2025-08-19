-- ========================================
-- SSO Server 层建表脚本
-- 版本: 1.0.0
-- 创建时间: 2025-01-19
-- 说明: SSO 认证和授权核心功能数据库表结构
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS sso_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sso_db;

-- ========================================
-- 核心业务表（按依赖顺序创建）
-- ========================================

-- 1. 用户账号表 (user_account_entity) - 核心用户信息
CREATE TABLE user_account_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar VARCHAR(255) COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别:0-未知,1-男,2-女',
    birthday DATE COMMENT '生日',
    status TINYINT DEFAULT 1 COMMENT '账号状态:0-禁用,1-启用,2-锁定',
    user_type TINYINT DEFAULT 1 COMMENT '用户类型:1-个人用户,2-企业用户,3-航司用户',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    password_update_time DATETIME COMMENT '密码最后修改时间',
    login_failure_count INT DEFAULT 0 COMMENT '连续登录失败次数',
    lock_time DATETIME COMMENT '账号锁定时间',
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
    INDEX idx_last_login_time (last_login_time)
) COMMENT '用户账号表-支持手机号/邮箱/第三方账号注册登录';

-- 2. 角色表 (role_entity) - 角色定义
CREATE TABLE role_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    INDEX idx_role_code (role_code),
    INDEX idx_status (status)
) COMMENT '角色表-定义管理员/个人用户/企业用户/航司用户角色';

-- 3. 权限表 (permission_entity) - 权限定义
CREATE TABLE permission_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_type TINYINT DEFAULT 1 COMMENT '权限类型:1-菜单,2-按钮,3-接口',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    path VARCHAR(200) COMMENT '路径',
    component VARCHAR(200) COMMENT '组件',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_permission_code (permission_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) COMMENT '权限表-定义系统功能权限';

-- ========================================
-- 关联表（依赖核心表）
-- ========================================

-- 4. 用户角色关联表 (user_account_entity_roles) - 用户角色多对多关系
CREATE TABLE user_account_entity_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    FOREIGN KEY (user_id) REFERENCES user_account_entity(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role_entity(id) ON DELETE CASCADE
) COMMENT '用户角色关联表-实现用户角色分配';

-- 5. 角色权限关联表 (role_permission_entity) - 角色权限多对多关系
CREATE TABLE role_permission_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id),
    FOREIGN KEY (role_id) REFERENCES role_entity(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permission_entity(id) ON DELETE CASCADE
) COMMENT '角色权限关联表-实现角色权限分配';

-- ========================================
-- SSO 功能支持表
-- ========================================

-- 6. 登录历史表 (login_history_entity) - 登录记录和多设备管理
CREATE TABLE login_history_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    login_type VARCHAR(20) DEFAULT 'password' COMMENT '登录类型:password,phone,email,wechat,alipay',
    login_ip VARCHAR(50) COMMENT '登录IP',
    login_location VARCHAR(100) COMMENT '登录地点',
    browser VARCHAR(50) COMMENT '浏览器',
    os VARCHAR(50) COMMENT '操作系统',
    device_type VARCHAR(20) COMMENT '设备类型:pc,mobile,tablet',
    device_id VARCHAR(100) COMMENT '设备唯一标识',
    device_name VARCHAR(100) COMMENT '设备名称',
    user_agent TEXT COMMENT '完整User-Agent',
    session_id VARCHAR(100) COMMENT '会话ID',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    logout_time DATETIME COMMENT '登出时间',
    login_status TINYINT DEFAULT 1 COMMENT '登录状态:0-失败,1-成功',
    failure_reason VARCHAR(200) COMMENT '失败原因',
    is_new_device TINYINT DEFAULT 0 COMMENT '是否新设备:0-否,1-是',
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_login_time (login_time),
    INDEX idx_login_ip (login_ip),
    INDEX idx_device_id (device_id),
    INDEX idx_login_status (login_status),
    INDEX idx_session_id (session_id),
    FOREIGN KEY (user_id) REFERENCES user_account_entity(id) ON DELETE SET NULL
) COMMENT '登录历史表-记录登录历史和支持多设备登录提醒';

-- 7. 第三方账号表 (third_party_account_entity) - 第三方账号绑定
CREATE TABLE third_party_account_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    platform VARCHAR(20) NOT NULL COMMENT '第三方平台:wechat,alipay,qq,weibo',
    platform_user_id VARCHAR(100) NOT NULL COMMENT '第三方平台用户ID',
    platform_username VARCHAR(100) COMMENT '第三方平台用户名',
    platform_avatar VARCHAR(255) COMMENT '第三方平台头像',
    access_token TEXT COMMENT '访问令牌(加密存储)',
    refresh_token TEXT COMMENT '刷新令牌(加密存储)',
    scope VARCHAR(200) COMMENT '授权范围',
    expires_time DATETIME COMMENT '令牌过期时间',
    last_sync_time DATETIME COMMENT '最后同步时间',
    bind_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    status TINYINT DEFAULT 1 COMMENT '状态:0-解绑,1-已绑定,2-已过期',
    UNIQUE KEY uk_platform_user (platform, platform_user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_platform (platform),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES user_account_entity(id) ON DELETE CASCADE
) COMMENT '第三方账号表-支持微信/支付宝等第三方账号绑定登录';

-- ========================================
-- 安全和会话管理表
-- ========================================

-- 8. 用户会话表 - 多设备登录管理
CREATE TABLE user_session_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    session_id VARCHAR(100) NOT NULL UNIQUE COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token_value VARCHAR(255) COMMENT 'Token值',
    device_type VARCHAR(20) COMMENT '设备类型',
    device_id VARCHAR(100) COMMENT '设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    login_ip VARCHAR(50) COMMENT '登录IP',
    login_location VARCHAR(100) COMMENT '登录地点',
    user_agent TEXT COMMENT 'User-Agent',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    last_access_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后访问时间',
    expires_time DATETIME COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态:0-已下线,1-在线,2-异常',
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_token_value (token_value),
    INDEX idx_expires_time (expires_time),
    INDEX idx_device_id (device_id),
    FOREIGN KEY (user_id) REFERENCES user_account_entity(id) ON DELETE CASCADE
) COMMENT '用户会话表-支持多设备登录管理和会话控制';

-- 9. 验证码表 - 手机验证码密码修改支持
CREATE TABLE verification_code_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT COMMENT '用户ID',
    code_type VARCHAR(20) NOT NULL COMMENT '验证码类型:login,register,reset_password,bind_phone,change_password',
    code_value VARCHAR(10) NOT NULL COMMENT '验证码值',
    target VARCHAR(100) NOT NULL COMMENT '目标(手机号/邮箱)',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retry INT DEFAULT 5 COMMENT '最大重试次数',
    ip_address VARCHAR(50) COMMENT '请求IP',
    expires_time DATETIME NOT NULL COMMENT '过期时间',
    used_time DATETIME COMMENT '使用时间',
    status TINYINT DEFAULT 0 COMMENT '状态:0-未使用,1-已使用,2-已过期,3-已失效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_target (target),
    INDEX idx_code_type (code_type),
    INDEX idx_expires_time (expires_time),
    INDEX idx_ip_address (ip_address),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES user_account_entity(id) ON DELETE SET NULL
) COMMENT '验证码表-支持手机验证码修改密码等功能';

-- ========================================
-- Sa-Token 持久化支持表
-- ========================================

-- 10. Sa-Token持久化存储表
CREATE TABLE sa_token_persistent (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    token_key VARCHAR(255) NOT NULL COMMENT 'Token键',
    token_value TEXT NOT NULL COMMENT 'Token值',
    token_type VARCHAR(50) DEFAULT 'login' COMMENT 'Token类型:login,temp,refresh',
    login_id VARCHAR(100) COMMENT '登录ID',
    device VARCHAR(50) COMMENT '设备标识',
    timeout_time BIGINT COMMENT '过期时间戳',
    session_timeout BIGINT COMMENT '会话超时时间',
    token_timeout BIGINT COMMENT 'Token超时时间',
    last_activity_time BIGINT COMMENT '最后活跃时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_token_key (token_key),
    INDEX idx_login_id (login_id),
    INDEX idx_timeout_time (timeout_time),
    INDEX idx_token_type (token_type)
) COMMENT 'Sa-Token持久化存储表-支持分布式Token管理';

-- 11. Token黑名单表
CREATE TABLE sa_token_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    token_value VARCHAR(255) NOT NULL COMMENT 'Token值',
    login_id VARCHAR(100) COMMENT '登录ID',
    blacklist_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入黑名单时间',
    expire_time DATETIME COMMENT '过期时间',
    reason VARCHAR(200) COMMENT '加入原因',
    operator_id BIGINT COMMENT '操作人ID',
    UNIQUE KEY uk_token_value (token_value),
    INDEX idx_login_id (login_id),
    INDEX idx_expire_time (expire_time)
) COMMENT 'Token黑名单表-支持Token强制失效';

-- ========================================
-- 系统配置和扩展表
-- ========================================

-- 12. 系统配置表
CREATE TABLE system_config_entity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(20) DEFAULT 'string' COMMENT '配置类型:string,number,boolean,json',
    description VARCHAR(200) COMMENT '配置描述',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统配置:0-否,1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key)
) COMMENT '系统配置表-存储系统参数配置';

-- ========================================
-- 建表完成
-- ========================================

COMMIT;
