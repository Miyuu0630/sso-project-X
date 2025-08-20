-- ========================================
-- SSO系统完整数据库初始化数据
-- 配套 sso_complete_database_design.sql 使用
-- 版本: 1.0.0
-- 创建时间: 2025-08-20
-- 说明: 初始化角色、权限、管理员账号等基础数据
-- ========================================

USE sso_db;

-- ========================================
-- 角色初始化（四类用户角色）
-- ========================================

-- 1. 插入四类用户角色
INSERT INTO sys_role (role_name, role_key, role_sort, status, create_time, remark) VALUES
('管理员', 'ADMIN', 1, '1', NOW(), '系统管理员，拥有所有权限，负责系统管理和用户管理'),
('个人用户', 'PERSONAL_USER', 2, '1', NOW(), '个人用户，拥有基本功能权限，可以使用系统基础服务'),
('企业用户', 'ENTERPRISE_USER', 3, '1', NOW(), '企业用户，拥有企业级功能权限，可以管理企业相关业务'),
('航司用户', 'AIRLINE_USER', 4, '1', NOW(), '航空公司用户，拥有航司专用功能权限，可以管理航司业务')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

-- ========================================
-- 权限菜单初始化（分层级权限体系）
-- ========================================

-- 2. 插入一级菜单权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_time, remark) VALUES
-- 系统管理模块（管理员专用）
('系统管理', 0, 1, '/system', 'Layout', 'M', '1', '1', '', 'system', NOW(), '系统管理目录'),
-- 用户中心模块（所有用户）
('用户中心', 0, 2, '/user', 'Layout', 'M', '1', '1', '', 'user', NOW(), '用户中心目录'),
-- 企业管理模块（企业用户专用）
('企业管理', 0, 3, '/enterprise', 'Layout', 'M', '1', '1', '', 'enterprise', NOW(), '企业管理目录'),
-- 航司管理模块（航司用户专用）
('航司管理', 0, 4, '/airline', 'Layout', 'M', '1', '1', '', 'airline', NOW(), '航司管理目录'),
-- 系统监控模块（管理员专用）
('系统监控', 0, 5, '/monitor', 'Layout', 'M', '1', '1', '', 'monitor', NOW(), '系统监控目录')
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name);

-- 3. 插入二级菜单权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_time, remark) VALUES
-- 系统管理子菜单
('用户管理', 1, 1, '/system/user', 'system/user/index', 'C', '1', '1', 'system:user:list', 'user', NOW(), '用户管理菜单'),
('角色管理', 1, 2, '/system/role', 'system/role/index', 'C', '1', '1', 'system:role:list', 'role', NOW(), '角色管理菜单'),
('菜单管理', 1, 3, '/system/menu', 'system/menu/index', 'C', '1', '1', 'system:menu:list', 'menu', NOW(), '菜单管理菜单'),

-- 用户中心子菜单
('个人资料', 2, 1, '/user/profile', 'user/profile/index', 'C', '1', '1', 'user:profile:view', 'profile', NOW(), '个人资料菜单'),
('安全设置', 2, 2, '/user/security', 'user/security/index', 'C', '1', '1', 'user:security:view', 'security', NOW(), '安全设置菜单'),
('第三方绑定', 2, 3, '/user/oauth', 'user/oauth/index', 'C', '1', '1', 'user:oauth:view', 'oauth', NOW(), '第三方绑定菜单'),
('设备管理', 2, 4, '/user/device', 'user/device/index', 'C', '1', '1', 'user:device:view', 'device', NOW(), '设备管理菜单'),
('登录记录', 2, 5, '/user/loginlog', 'user/loginlog/index', 'C', '1', '1', 'user:loginlog:view', 'loginlog', NOW(), '登录记录菜单'),

-- 企业管理子菜单
('企业信息', 3, 1, '/enterprise/info', 'enterprise/info/index', 'C', '1', '1', 'enterprise:info:view', 'enterprise-info', NOW(), '企业信息菜单'),
('成员管理', 3, 2, '/enterprise/member', 'enterprise/member/index', 'C', '1', '1', 'enterprise:member:list', 'member', NOW(), '成员管理菜单'),
('企业认证', 3, 3, '/enterprise/auth', 'enterprise/auth/index', 'C', '1', '1', 'enterprise:auth:view', 'auth', NOW(), '企业认证菜单'),

-- 航司管理子菜单
('航司信息', 4, 1, '/airline/info', 'airline/info/index', 'C', '1', '1', 'airline:info:view', 'airline-info', NOW(), '航司信息菜单'),
('航班管理', 4, 2, '/airline/flight', 'airline/flight/index', 'C', '1', '1', 'airline:flight:list', 'flight', NOW(), '航班管理菜单'),
('乘客管理', 4, 3, '/airline/passenger', 'airline/passenger/index', 'C', '1', '1', 'airline:passenger:list', 'passenger', NOW(), '乘客管理菜单'),

-- 系统监控子菜单
('在线用户', 5, 1, '/monitor/online', 'monitor/online/index', 'C', '1', '1', 'monitor:online:list', 'online', NOW(), '在线用户菜单'),
('登录日志', 5, 2, '/monitor/loginlog', 'monitor/loginlog/index', 'C', '1', '1', 'monitor:loginlog:list', 'loginlog', NOW(), '登录日志菜单'),
('服务监控', 5, 3, '/monitor/server', 'monitor/server/index', 'C', '1', '1', 'monitor:server:view', 'server', NOW(), '服务监控菜单')
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name);

-- 4. 插入按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, create_time) VALUES
-- 用户管理按钮权限
('用户查询', 6, 1, 'F', '1', '1', 'system:user:query', NOW()),
('用户新增', 6, 2, 'F', '1', '1', 'system:user:add', NOW()),
('用户修改', 6, 3, 'F', '1', '1', 'system:user:edit', NOW()),
('用户删除', 6, 4, 'F', '1', '1', 'system:user:remove', NOW()),
('重置密码', 6, 5, 'F', '1', '1', 'system:user:resetPwd', NOW()),

-- 角色管理按钮权限
('角色查询', 7, 1, 'F', '1', '1', 'system:role:query', NOW()),
('角色新增', 7, 2, 'F', '1', '1', 'system:role:add', NOW()),
('角色修改', 7, 3, 'F', '1', '1', 'system:role:edit', NOW()),
('角色删除', 7, 4, 'F', '1', '1', 'system:role:remove', NOW()),

-- 菜单管理按钮权限
('菜单查询', 8, 1, 'F', '1', '1', 'system:menu:query', NOW()),
('菜单新增', 8, 2, 'F', '1', '1', 'system:menu:add', NOW()),
('菜单修改', 8, 3, 'F', '1', '1', 'system:menu:edit', NOW()),
('菜单删除', 8, 4, 'F', '1', '1', 'system:menu:remove', NOW())
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name);

-- ========================================
-- 角色权限分配
-- ========================================

-- 5. 管理员角色权限分配（拥有所有权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 6. 个人用户角色权限分配（只有用户中心权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu 
WHERE id IN (2, 9, 10, 11, 12, 13) -- 用户中心及其子菜单
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 7. 企业用户角色权限分配（用户中心 + 企业管理权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 3, id FROM sys_menu 
WHERE id IN (2, 3, 9, 10, 11, 12, 13, 14, 15, 16) -- 用户中心 + 企业管理
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 8. 航司用户角色权限分配（用户中心 + 航司管理权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 4, id FROM sys_menu 
WHERE id IN (2, 4, 9, 10, 11, 12, 13, 17, 18, 19) -- 用户中心 + 航司管理
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- ========================================
-- 初始管理员账号
-- ========================================

-- 9. 插入初始管理员账号（密码: admin123456, BCrypt加密）
INSERT INTO sys_user (username, password, real_name, email, phone, status, user_type, create_time, remark) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXIgBINrS.QSLbm8Qs8Ue6.4NeO', '系统管理员', 'admin@sso.com', '13800000000', '1', 'normal', NOW(), '初始管理员账号')
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 10. 为管理员分配管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1)
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- ========================================
-- 示例用户数据（可选）
-- ========================================

-- 11. 插入示例用户（密码: 123456, BCrypt加密）
INSERT INTO sys_user (username, password, real_name, email, phone, status, user_type, create_time, remark) VALUES
('personal_user', '$2a$10$7JB720yubVSOfvVMe6/YqO4wkhWGEn67XVb1TqHGjpQYqHrDfbpBG', '个人用户示例', 'personal@example.com', '13800000001', '1', 'normal', NOW(), '个人用户测试账号'),
('enterprise_user', '$2a$10$7JB720yubVSOfvVMe6/YqO4wkhWGEn67XVb1TqHGjpQYqHrDfbpBG', '企业用户示例', 'enterprise@example.com', '13800000002', '1', 'enterprise', NOW(), '企业用户测试账号'),
('airline_user', '$2a$10$7JB720yubVSOfvVMe6/YqO4wkhWGEn67XVb1TqHGjpQYqHrDfbpBG', '航司用户示例', 'airline@example.com', '13800000003', '1', 'airline', NOW(), '航司用户测试账号')
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 12. 为示例用户分配对应角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(2, 2), -- 个人用户角色
(3, 3), -- 企业用户角色
(4, 4)  -- 航司用户角色
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- ========================================
-- 第三方账号绑定示例数据（可选）
-- ========================================

-- 13. 插入第三方账号绑定示例
INSERT INTO user_oauth_binding (user_id, provider, open_id, nickname, avatar, bind_time, is_active) VALUES
(2, 'wechat', 'wx_openid_123456', '微信用户', 'https://example.com/avatar1.jpg', NOW(), 1),
(3, 'alipay', 'alipay_userid_789012', '支付宝用户', 'https://example.com/avatar2.jpg', NOW(), 1)
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- ========================================
-- 验证码示例数据（可选）
-- ========================================

-- 14. 插入验证码示例（已过期的测试数据）
INSERT INTO verification_code (code_type, target, code, purpose, is_used, expire_time, create_time) VALUES
('sms', '13800000001', '123456', 'login', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('email', 'test@example.com', '654321', 'register', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR))
ON DUPLICATE KEY UPDATE code_type=VALUES(code_type);

-- ========================================
-- 数据完整性检查
-- ========================================

-- 检查初始化结果
SELECT '用户统计' as '检查项', COUNT(*) as '数量' FROM sys_user
UNION ALL
SELECT '角色统计', COUNT(*) FROM sys_role  
UNION ALL
SELECT '菜单权限统计', COUNT(*) FROM sys_menu
UNION ALL
SELECT '用户角色关联', COUNT(*) FROM sys_user_role
UNION ALL
SELECT '角色菜单关联', COUNT(*) FROM sys_role_menu
UNION ALL
SELECT '第三方绑定', COUNT(*) FROM user_oauth_binding
UNION ALL
SELECT '验证码记录', COUNT(*) FROM verification_code;

-- 检查权限分配情况
SELECT 
    u.username as '用户名',
    u.user_type as '用户类型',
    r.role_name as '角色名',
    COUNT(rm.menu_id) as '权限数量'
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
LEFT JOIN sys_role_menu rm ON r.id = rm.role_id
GROUP BY u.id, r.id
ORDER BY u.id;

COMMIT;
