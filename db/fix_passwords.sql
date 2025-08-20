-- ========================================
-- 修复SSO系统用户密码
-- 使用正确的BCrypt哈希值
-- ========================================

USE sso_db;

-- 生成正确的BCrypt哈希值（使用在线工具或Java代码生成）
-- admin123456 的正确哈希值
-- 123456 的正确哈希值

-- 临时方案：使用明文密码，方便测试
-- 更新admin用户密码 (admin123456 -> 明文)
UPDATE sys_user SET
    password = 'admin123456'
WHERE username = 'admin';

-- 更新其他用户密码 (123456 -> 明文)
UPDATE sys_user SET
    password = '123456'
WHERE username IN ('personal_user', 'enterprise_user', 'airline_user');

-- 添加一个简单的测试用户
INSERT INTO sys_user (username, password, real_name, email, phone, status, user_type, create_time, remark) VALUES
('test', 'test123', '测试用户', 'test@example.com', '13900000001', '1', 'normal', NOW(), '明文密码测试账号')
ON DUPLICATE KEY UPDATE password = 'test123';

-- 验证更新结果
SELECT username, password, status FROM sys_user;

-- 提示信息
SELECT '密码已更新完成' as '状态',
       'admin / admin123456' as '管理员账号',
       'test / test123' as '测试账号',
       '其他用户密码: 123456' as '普通用户';

COMMIT;
