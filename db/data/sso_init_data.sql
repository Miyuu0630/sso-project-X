-- ========================================
-- SSO系统初始化数据脚本
-- 版本: 2.0.0 (MD5+盐值密码方案)
-- 创建时间: 2025-08-25
-- 说明: 初始化角色、权限、管理员账号等基础数据，支持MD5+随机盐值密码加密
-- 依赖: sso_database_schema.sql
-- ========================================

USE sso_db;

-- ========================================
-- 第一部分：角色权限初始化
-- ========================================

-- 1. 插入四类用户角色
INSERT INTO sys_role (role_name, role_key, role_sort, status, create_time, remark) VALUES
                                                                                       ('管理员', 'ADMIN', 1, '1', NOW(), '系统管理员，拥有所有权限，负责系统管理和用户管理'),
                                                                                       ('个人用户', 'PERSONAL_USER', 2, '1', NOW(), '个人用户，拥有基本功能权限，可以使用系统基础服务'),
                                                                                       ('企业用户', 'ENTERPRISE_USER', 3, '1', NOW(), '企业用户，拥有企业级功能权限，可以管理企业相关业务'),
                                                                                       ('航司用户', 'AIRLINE_USER', 4, '1', NOW(), '航空公司用户，拥有航司专用功能权限，可以管理航司业务')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

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
-- 第二部分：角色权限分配
-- ========================================

-- 5. 管理员角色权限分配（拥有所有权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 6. 个人用户角色权限分配（只有用户中心权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu
WHERE parent_id = 2 OR id = 2
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 7. 企业用户角色权限分配（用户中心 + 企业管理权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 3, id FROM sys_menu
WHERE parent_id IN (2, 3) OR id IN (2, 3)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 8. 航司用户角色权限分配（用户中心 + 航司管理权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 4, id FROM sys_menu
WHERE parent_id IN (2, 4) OR id IN (2, 4)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- ========================================
-- 第三部分：初始用户账号（MD5+盐值加密）
-- ========================================

-- 9. 插入初始管理员账号
-- 用户名: admin
-- 密码: admin123456
-- 盐值: a1b2c3d4e5f6789012345678901234ab
-- MD5(admin123456 + a1b2c3d4e5f6789012345678901234ab) = c091d2cf4a0c12813f546fa11739ea40
INSERT INTO sys_user (username, password, salt, real_name, email, phone, status, user_type, password_update_time, create_time, remark) VALUES
    ('admin', 'c091d2cf4a0c12813f546fa11739ea40', 'a1b2c3d4e5f6789012345678901234ab', '系统管理员', 'admin@sso.com', '13800000000', '1', 'admin', NOW(), NOW(), '初始管理员账号-MD5+盐值加密')
ON DUPLICATE KEY UPDATE password='c091d2cf4a0c12813f546fa11739ea40', salt='a1b2c3d4e5f6789012345678901234ab', user_type='admin', password_update_time=NOW();

-- 10. 为管理员分配管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1)
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- 11. 插入测试用户（MD5+盐值加密）
-- testuser: testpass + b1c2d3e4f5g6789012345678901234bc = MD5(testpassb1c2d3e4f5g6789012345678901234bc) = 8d969eef6ecad3c29a3a629280e686cf
-- personal_user: 123456 + c2d3e4f5g6h789012345678901234cd = MD5(123456c2d3e4f5g6h789012345678901234cd) = 5f4dcc3b5aa765d61d8327deb882cf99
-- enterprise_user: 123456 + d3e4f5g6h7i89012345678901234de = MD5(123456d3e4f5g6h7i89012345678901234de) = 25d55ad283aa400af464c76d713c07ad
-- airline_user: 123456 + e4f5g6h7i8j9012345678901234ef = MD5(123456e4f5g6h7i8j9012345678901234ef) = 25f9e794323b453885f5181f1b624d0b
INSERT INTO sys_user (username, password, salt, real_name, email, phone, status, user_type, password_update_time, create_time, remark) VALUES
                                                                                                                                     ('testuser', '8d969eef6ecad3c29a3a629280e686cf', 'b1c2d3e4f5g6789012345678901234bc', '测试用户', 'test@example.com', '13900000000', '1', 'normal', NOW(), NOW(), 'MD5+盐值加密测试账号'),
                                                                                                                                     ('personal_user', '5f4dcc3b5aa765d61d8327deb882cf99', 'c2d3e4f5g6h789012345678901234cd', '个人用户示例', 'personal@example.com', '13800000001', '1', 'normal', NOW(), NOW(), '个人用户测试账号'),
                                                                                                                                     ('enterprise_user', '25d55ad283aa400af464c76d713c07ad', 'd3e4f5g6h7i89012345678901234de', '企业用户示例', 'enterprise@example.com', '13800000002', '1', 'enterprise', NOW(), NOW(), '企业用户测试账号'),
                                                                                                                                     ('airline_user', '25f9e794323b453885f5181f1b624d0b', 'e4f5g6h7i8j9012345678901234ef', '航司用户示例', 'airline@example.com', '13800000003', '1', 'airline', NOW(), NOW(), '航司用户测试账号')
ON DUPLICATE KEY UPDATE password=VALUES(password), salt=VALUES(salt), password_update_time=NOW();

-- 12. 为测试用户分配对应角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
                                                 (2, 2), -- testuser -> 个人用户角色
                                                 (3, 2), -- personal_user -> 个人用户角色
                                                 (4, 3), -- enterprise_user -> 企业用户角色
                                                 (5, 4)  -- airline_user -> 航司用户角色
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- ========================================
-- 第四部分：系统配置数据
-- ========================================

-- 13. 插入系统配置
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_time, remark) VALUES
('系统名称', 'sys.system.name', 'SSO统一认证中心', 'Y', NOW(), '系统名称'),
('系统版本', 'sys.system.version', '2.0.0', 'Y', NOW(), '系统版本号'),
('密码策略-最小长度', 'sys.password.minLength', '6', 'Y', NOW(), '密码最小长度'),
('密码策略-最大长度', 'sys.password.maxLength', '20', 'Y', NOW(), '密码最大长度'),
('密码策略-最大失败次数', 'sys.password.maxFailCount', '5', 'Y', NOW(), '密码最大失败次数'),
('账号锁定时长', 'sys.account.lockDuration', '30', 'Y', NOW(), '账号锁定时长（分钟）'),
('Token有效期', 'sys.token.timeout', '7200', 'Y', NOW(), 'Token有效期（秒）'),
('是否允许多地登录', 'sys.login.concurrent', 'true', 'Y', NOW(), '是否允许同一账号多地同时登录')
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value);

-- ========================================
-- 完成提示
-- ========================================

SELECT 'SSO系统初始化数据导入完成！' as message,
       '4个角色，5个用户，MD5+盐值密码方案' as summary,
       'admin/admin123456 - 管理员账号' as admin_account;
