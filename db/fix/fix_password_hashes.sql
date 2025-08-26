-- 修复用户密码哈希值
-- 此脚本用于修复数据库中错误的密码哈希值

USE sso_db;

-- 备份当前密码数据（可选）
-- CREATE TABLE sys_user_password_backup AS SELECT id, username, password, salt FROM sys_user;

-- 更新正确的密码哈希值
-- 注意：这些哈希值是通过 MD5(password + salt) 计算得出的

-- 1. 更新 testuser 密码哈希
-- 密码: testpass, 盐值: b1c2d3e4f5g6789012345678901234bc
UPDATE sys_user SET password = 'ba40cb89c46b065fc284889afc92c31a' WHERE username = 'testuser';

-- 2. 更新 personal_user 密码哈希  
-- 密码: 123456, 盐值: c2d3e4f5g6h789012345678901234cd
UPDATE sys_user SET password = 'bfea7dbb7c6b5971627bb2e92846abbc' WHERE username = 'personal_user';

-- 3. 更新 enterprise_user 密码哈希
-- 密码: 123456, 盐值: d3e4f5g6h7i89012345678901234de  
UPDATE sys_user SET password = '6da98a86e9a6227a4d5f3ec53e882856' WHERE username = 'enterprise_user';

-- 4. 更新 airline_user 密码哈希
-- 密码: 123456, 盐值: e4f5g6h7i8j9012345678901234ef
UPDATE sys_user SET password = 'd4fc244c5ee1544c0d6e0e0952004ded' WHERE username = 'airline_user';

-- 5. 更新密码更新时间
UPDATE sys_user SET password_update_time = NOW() 
WHERE username IN ('testuser', 'personal_user', 'enterprise_user', 'airline_user');

-- 6. 重置登录失败次数（如果有的话）
UPDATE sys_user SET failed_login_count = 0, last_failed_login_time = NULL 
WHERE username IN ('testuser', 'personal_user', 'enterprise_user', 'airline_user');

-- 验证更新结果
SELECT 
    username as '用户名',
    password as '密码哈希',
    salt as '盐值',
    password_update_time as '密码更新时间',
    failed_login_count as '失败次数'
FROM sys_user 
WHERE username IN ('admin', 'testuser', 'personal_user', 'enterprise_user', 'airline_user')
ORDER BY id;

-- 显示修复完成信息
SELECT '密码哈希修复完成！' as '状态', NOW() as '修复时间';
