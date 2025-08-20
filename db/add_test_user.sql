-- ========================================
-- 添加明文密码测试用户
-- 临时方案，方便快速测试SSO流程
-- ========================================

USE sso_db;

-- 插入测试用户（明文密码）
INSERT INTO sys_user (username, password, real_name, email, phone, status, user_type, create_time, remark) VALUES
('testuser', 'testpass', '测试用户', 'test@example.com', '13900000000', '1', 'normal', NOW(), '明文密码测试账号')
ON DUPLICATE KEY UPDATE password = 'testpass';

-- 为测试用户分配角色（个人用户角色）
INSERT INTO sys_user_role (user_id, role_id) 
SELECT u.id, 2 FROM sys_user u WHERE u.username = 'testuser'
ON DUPLICATE KEY UPDATE role_id = 2;

-- 验证插入结果
SELECT id, username, password, real_name, status FROM sys_user WHERE username = 'testuser';

-- 提示信息
SELECT '测试用户已创建' as '状态', 
       '用户名: testuser' as '账号',
       '密码: testpass' as '密码',
       '这是明文密码，仅用于测试' as '说明';

COMMIT;
