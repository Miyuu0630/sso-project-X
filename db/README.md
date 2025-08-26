# SSO 数据库文件说明

本目录包含 SSO 统一认证系统的数据库相关文件。

## 📁 文件结构

```
db/
├── README.md                           # 本说明文件
├── schema/                             # 数据库结构文件
│   ├── sso_database_schema.sql         # 当前使用：MD5+盐值方案数据库结构
│   └── sso_complete_schema_original.sql # 原始文件：BCrypt方案前的数据库结构
└── data/                               # 初始化数据文件
    ├── sso_init_data.sql               # 当前使用：MD5+盐值方案初始化数据
    └── sso_init_data_original.sql      # 原始文件：BCrypt方案前的初始化数据
```

## 🚀 使用说明

### 1. 数据库初始化（推荐方式）

**第一步：创建数据库结构**
```bash
mysql -u root -p < db/schema/sso_database_schema.sql
```

**第二步：导入初始化数据**
```bash
mysql -u root -p < db/data/sso_init_data.sql
```

### 2. 一键重建数据库

如果需要完全重建数据库：
```bash
# 1. 登录 MySQL
mysql -u root -p

# 2. 删除并重建数据库
DROP DATABASE IF EXISTS sso_db;
source db/schema/sso_database_schema.sql
source db/data/sso_init_data.sql
```

## 🔐 密码加密方案升级

### 当前方案：MD5 + 随机盐值

- **算法**: MD5(password + salt)
- **密码字段**: VARCHAR(32) - 存储32位MD5哈希值
- **盐值字段**: VARCHAR(32) - 存储32位随机字符串
- **拼接顺序**: password + salt（注意顺序一致性）

### 升级前方案对比

| 方案 | 密码字段长度 | 盐值字段 | 加密算法 |
|------|-------------|----------|----------|
| 原始方案 | VARCHAR(255) | 无 | 明文存储 |
| 当前方案 | VARCHAR(32) | VARCHAR(32) | MD5(password + salt) |

### 安全特性

1. **随机盐值**: 每个用户都有唯一的32位随机盐值
2. **防彩虹表**: 盐值有效防止彩虹表攻击
3. **密码历史**: 记录密码变更历史，防止重复使用
4. **登录保护**: 失败次数限制和账号锁定机制

## 🔑 默认账号

系统初始化后包含以下测试账号：

| 用户名 | 密码 | 角色 | 盐值示例 | MD5哈希示例 |
|--------|------|------|----------|-------------|
| admin | admin123456 | 超级管理员 | a1b2c3d4e5f6... | c091d2cf4a0c... |
| system | system123456 | 系统管理员 | b2c3d4e5f678... | 4073a37cb717... |
| testuser | test123456 | 普通用户 | c3d4e5f67890... | acfec54f2c97... |

**⚠️ 注意**: 生产环境请立即修改默认密码！

## 💻 数据库要求

- **MySQL版本**: 5.7+ 或 8.0+
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **存储引擎**: InnoDB

## 🔧 重要提醒

### 密码验证逻辑
```java
// 注册时
String salt = generateRandomSalt(32);
String hashedPassword = MD5(inputPassword + salt);
// 存储 hashedPassword 和 salt 到数据库

// 登录验证时
String storedSalt = getUserSalt(username);
String inputHash = MD5(inputPassword + storedSalt);
boolean isValid = inputHash.equals(storedPasswordHash);
```

### 注意事项

1. **备份重要**: 执行任何数据库操作前请先备份
2. **权限检查**: 确保数据库用户有足够的权限
3. **密码安全**: 生产环境必须修改默认密码
4. **拼接顺序**: 密码+盐值的拼接顺序必须保持一致
5. **定期维护**: 建议定期清理过期的令牌和日志数据

## 📋 文件版本说明

- **当前文件**: 支持MD5+盐值加密方案，适用于生产环境
- **原始文件**: 保留作为参考，包含BCrypt升级前的原始结构

## 🔧 故障排查

如果登录失败，检查：
1. 数据库中密码是否为32位MD5哈希值
2. 盐值字段是否存在且不为空
3. Spring Boot应用是否使用正确的MD5+盐值验证逻辑
4. 用户状态是否为启用状态（status = '1'）
