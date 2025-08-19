-- ========================================
-- SSO Server 层数据初始化脚本
-- 版本: 1.0.0
-- 创建时间: 2025-01-19
-- 说明: 初始化角色、权限、管理员账号等基础数据
-- ========================================

USE sso_db;

-- ========================================
-- 系统配置初始化
-- ========================================

-- 1. 插入系统基础配置
INSERT INTO system_config_entity (config_key, config_value, config_type, description, is_system) VALUES
('system.name', 'SSO认证中心', 'string', '系统名称', 1),
('system.version', '1.0.0', 'string', '系统版本', 1),
('login.max_failure_count', '5', 'number', '最大登录失败次数', 1),
('login.lock_duration', '30', 'number', '账户锁定时长(分钟)', 1),
('password.min_length', '6', 'number', '密码最小长度', 1),
('password.complexity', 'true', 'boolean', '是否启用密码复杂度检查', 1),
('sms.enabled', 'true', 'boolean', '是否启用短信验证', 1),
('email.enabled', 'true', 'boolean', '是否启用邮件验证', 1),
('third_party.wechat.enabled', 'true', 'boolean', '是否启用微信登录', 1),
('third_party.alipay.enabled', 'true', 'boolean', '是否启用支付宝登录', 1),
('session.timeout', '1800', 'number', '会话超时时间(秒)', 1),
('token.expire_time', '86400', 'number', 'Token过期时间(秒)', 1),
('multi_device.enabled', 'true', 'boolean', '是否启用多设备登录', 1),
('new_device.notify', 'true', 'boolean', '新设备登录是否通知', 1)
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value);

-- ========================================
-- 角色初始化（四类用户角色）
-- ========================================

-- 2. 插入四类用户角色
INSERT INTO role_entity (role_code, role_name, description, status, sort_order) VALUES
('ADMIN', '管理员', '系统管理员，拥有所有权限，负责系统管理和用户管理', 1, 1),
('PERSONAL_USER', '个人用户', '个人用户，拥有基本功能权限，可以使用系统基础服务', 1, 2),
('ENTERPRISE_USER', '企业用户', '企业用户，拥有企业级功能权限，可以管理企业相关业务', 1, 3),
('AIRLINE_USER', '航司用户', '航空公司用户，拥有航司专用功能权限，可以管理航司业务', 1, 4)
ON DUPLICATE KEY UPDATE role_code=role_code;

-- ========================================
-- 权限初始化（分层级权限体系）
-- ========================================

-- 3. 插入一级菜单权限
INSERT INTO permission_entity (permission_code, permission_name, permission_type, parent_id, path, component, icon, sort_order, status) VALUES
-- 系统管理模块（管理员专用）
('system', '系统管理', 1, 0, '/system', 'Layout', 'system', 1, 1),
-- 用户中心模块（所有用户）
('user', '用户中心', 1, 0, '/user', 'Layout', 'user', 2, 1),
-- 企业管理模块（企业用户专用）
('enterprise', '企业管理', 1, 0, '/enterprise', 'Layout', 'enterprise', 3, 1),
-- 航司管理模块（航司用户专用）
('airline', '航司管理', 1, 0, '/airline', 'Layout', 'airline', 4, 1),
-- 系统监控模块（管理员专用）
('monitor', '系统监控', 1, 0, '/monitor', 'Layout', 'monitor', 5, 1)
ON DUPLICATE KEY UPDATE permission_code=permission_code;

-- 4. 插入二级菜单权限
INSERT INTO permission_entity (permission_code, permission_name, permission_type, parent_id, path, component, icon, sort_order, status) VALUES
-- 系统管理子菜单
('system:user', '用户管理', 1, 1, '/system/user', 'system/user/index', 'user', 1, 1),
('system:role', '角色管理', 1, 1, '/system/role', 'system/role/index', 'role', 2, 1),
('system:permission', '权限管理', 1, 1, '/system/permission', 'system/permission/index', 'permission', 3, 1),
('system:config', '系统配置', 1, 1, '/system/config', 'system/config/index', 'config', 4, 1),

-- 用户中心子菜单
('user:profile', '个人资料', 1, 2, '/user/profile', 'user/profile/index', 'profile', 1, 1),
('user:security', '安全设置', 1, 2, '/user/security', 'user/security/index', 'security', 2, 1),
('user:third_party', '第三方绑定', 1, 2, '/user/third_party', 'user/third_party/index', 'third_party', 3, 1),
('user:device', '设备管理', 1, 2, '/user/device', 'user/device/index', 'device', 4, 1),

-- 企业管理子菜单
('enterprise:info', '企业信息', 1, 3, '/enterprise/info', 'enterprise/info/index', 'enterprise_info', 1, 1),
('enterprise:member', '成员管理', 1, 3, '/enterprise/member', 'enterprise/member/index', 'member', 2, 1),
('enterprise:auth', '企业认证', 1, 3, '/enterprise/auth', 'enterprise/auth/index', 'auth', 3, 1),

-- 航司管理子菜单
('airline:info', '航司信息', 1, 4, '/airline/info', 'airline/info/index', 'airline_info', 1, 1),
('airline:flight', '航班管理', 1, 4, '/airline/flight', 'airline/flight/index', 'flight', 2, 1),
('airline:passenger', '乘客管理', 1, 4, '/airline/passenger', 'airline/passenger/index', 'passenger', 3, 1),

-- 系统监控子菜单
('monitor:session', '在线用户', 1, 5, '/monitor/session', 'monitor/session/index', 'session', 1, 1),
('monitor:login', '登录日志', 1, 5, '/monitor/login', 'monitor/login/index', 'login_log', 2, 1),
('monitor:server', '服务监控', 1, 5, '/monitor/server', 'monitor/server/index', 'server', 3, 1)
ON DUPLICATE KEY UPDATE permission_code=permission_code;

-- 5. 插入按钮权限
INSERT INTO permission_entity (permission_code, permission_name, permission_type, parent_id, sort_order, status) VALUES
-- 用户管理按钮权限
('system:user:list', '用户查询', 2, 6, 1, 1),
('system:user:add', '用户新增', 2, 6, 2, 1),
('system:user:edit', '用户修改', 2, 6, 3, 1),
('system:user:delete', '用户删除', 2, 6, 4, 1),
('system:user:reset', '重置密码', 2, 6, 5, 1),
('system:user:lock', '锁定/解锁', 2, 6, 6, 1),

-- 角色管理按钮权限
('system:role:list', '角色查询', 2, 7, 1, 1),
('system:role:add', '角色新增', 2, 7, 2, 1),
('system:role:edit', '角色修改', 2, 7, 3, 1),
('system:role:delete', '角色删除', 2, 7, 4, 1),
('system:role:assign', '分配权限', 2, 7, 5, 1),

-- 权限管理按钮权限
('system:permission:list', '权限查询', 2, 8, 1, 1),
('system:permission:add', '权限新增', 2, 8, 2, 1),
('system:permission:edit', '权限修改', 2, 8, 3, 1),
('system:permission:delete', '权限删除', 2, 8, 4, 1),

-- 个人中心按钮权限
('user:profile:view', '查看资料', 2, 10, 1, 1),
('user:profile:edit', '修改资料', 2, 10, 2, 1),
('user:security:view', '查看安全设置', 2, 11, 1, 1),
('user:security:edit', '修改安全设置', 2, 11, 2, 1),
('user:security:change_password', '修改密码', 2, 11, 3, 1),
('user:third_party:view', '查看绑定', 2, 12, 1, 1),
('user:third_party:bind', '绑定账号', 2, 12, 2, 1),
('user:third_party:unbind', '解绑账号', 2, 12, 3, 1),
('user:device:view', '查看设备', 2, 13, 1, 1),
('user:device:remove', '移除设备', 2, 13, 2, 1),

-- 企业管理按钮权限
('enterprise:info:view', '查看企业信息', 2, 14, 1, 1),
('enterprise:info:edit', '修改企业信息', 2, 14, 2, 1),
('enterprise:member:list', '成员列表', 2, 15, 1, 1),
('enterprise:member:add', '添加成员', 2, 15, 2, 1),
('enterprise:member:remove', '移除成员', 2, 15, 3, 1),

-- 航司管理按钮权限
('airline:info:view', '查看航司信息', 2, 17, 1, 1),
('airline:info:edit', '修改航司信息', 2, 17, 2, 1),
('airline:flight:list', '航班列表', 2, 18, 1, 1),
('airline:flight:add', '添加航班', 2, 18, 2, 1),
('airline:flight:edit', '修改航班', 2, 18, 3, 1),
('airline:passenger:list', '乘客列表', 2, 19, 1, 1),
('airline:passenger:view', '查看乘客', 2, 19, 2, 1)
ON DUPLICATE KEY UPDATE permission_code=permission_code;

-- ========================================
-- 角色权限分配
-- ========================================

-- 6. 管理员角色权限分配（拥有所有权限）
INSERT INTO role_permission_entity (role_id, permission_id, create_by)
SELECT 1, id, 1 FROM permission_entity
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 7. 个人用户角色权限分配（只有用户中心权限）
INSERT INTO role_permission_entity (role_id, permission_id, create_by)
SELECT 2, id, 1 FROM permission_entity 
WHERE permission_code LIKE 'user:%'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 8. 企业用户角色权限分配（用户中心 + 企业管理权限）
INSERT INTO role_permission_entity (role_id, permission_id, create_by)
SELECT 3, id, 1 FROM permission_entity 
WHERE permission_code LIKE 'user:%' OR permission_code LIKE 'enterprise:%'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- 9. 航司用户角色权限分配（用户中心 + 航司管理权限）
INSERT INTO role_permission_entity (role_id, permission_id, create_by)
SELECT 4, id, 1 FROM permission_entity 
WHERE permission_code LIKE 'user:%' OR permission_code LIKE 'airline:%'
ON DUPLICATE KEY UPDATE role_id=role_id;

-- ========================================
-- 初始管理员账号
-- ========================================

-- 10. 插入初始管理员账号（密码: admin123456, BCrypt加密）
INSERT INTO user_account_entity (username, password, real_name, email, phone, status, user_type, password_update_time) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXIgBINrS.QSLbm8Qs8Ue6.4NeO', '系统管理员', 'admin@sso.com', '13800000000', 1, 1, NOW())
ON DUPLICATE KEY UPDATE username=username;

-- 11. 为管理员分配管理员角色
INSERT INTO user_account_entity_roles (user_id, role_id, create_by) VALUES
(1, 1, 1)
ON DUPLICATE KEY UPDATE user_id=user_id;

-- ========================================
-- 示例用户数据（可选）
-- ========================================

-- 12. 插入示例用户（密码: 123456, BCrypt加密）
INSERT INTO user_account_entity (username, password, real_name, email, phone, status, user_type, password_update_time) VALUES
('personal_user', '$2a$10$7JB720yubVSOfvVMe6/YqO4wkhWGEn67XVb1TqHGjpQYqHrDfbpBG', '个人用户示例', 'personal@example.com', '13800000001', 1, 1, NOW()),
('enterprise_user', '$2a$10$7JB720yubVSOfvVMe6/YqO4wkhWGEn67XVb1TqHGjpQYqHrDfbpBG', '企业用户示例', 'enterprise@example.com', '13800000002', 1, 2, NOW()),
('airline_user', '$2a$10$7JB720yubVSOfvVMe6/YqO4wkhWGEn67XVb1TqHGjpQYqHrDfbpBG', '航司用户示例', 'airline@example.com', '13800000003', 1, 3, NOW())
ON DUPLICATE KEY UPDATE username=username;

-- 13. 为示例用户分配对应角色
INSERT INTO user_account_entity_roles (user_id, role_id, create_by) VALUES
(2, 2, 1), -- 个人用户角色
(3, 3, 1), -- 企业用户角色
(4, 4, 1)  -- 航司用户角色
ON DUPLICATE KEY UPDATE user_id=user_id;

-- ========================================
-- 数据完整性检查
-- ========================================

-- 检查初始化结果
SELECT '用户统计' as '检查项', COUNT(*) as '数量' FROM user_account_entity
UNION ALL
SELECT '角色统计', COUNT(*) FROM role_entity  
UNION ALL
SELECT '权限统计', COUNT(*) FROM permission_entity
UNION ALL
SELECT '用户角色关联', COUNT(*) FROM user_account_entity_roles
UNION ALL
SELECT '角色权限关联', COUNT(*) FROM role_permission_entity
UNION ALL
SELECT '系统配置', COUNT(*) FROM system_config_entity;

-- 检查权限分配情况
SELECT 
    u.username as '用户名',
    r.role_name as '角色名',
    COUNT(rp.permission_id) as '权限数量'
FROM user_account_entity u
LEFT JOIN user_account_entity_roles ur ON u.id = ur.user_id
LEFT JOIN role_entity r ON ur.role_id = r.id
LEFT JOIN role_permission_entity rp ON r.id = rp.role_id
GROUP BY u.id, r.id
ORDER BY u.id;

COMMIT;
