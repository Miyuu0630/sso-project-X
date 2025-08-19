# SSO Client 数据库脚本

## 说明

SSO客户端后端目前主要依赖SSO服务端进行认证和授权，不需要独立的数据库表。

## 配置选项

### 选项1：无独立数据库（推荐）
客户端直接使用SSO服务端的认证服务，无需独立数据库。这是当前的默认配置。

### 选项2：独立业务数据库
如果客户端需要存储自己的业务数据，可以在 `sso-client-backend/src/main/resources/application.yml` 中启用数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/business_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 选项3：共享SSO数据库
如果需要访问SSO服务端的用户数据，可以配置连接到同一个sso_db数据库：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sso_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 业务数据库示例

如果需要创建独立的业务数据库，可以参考以下脚本：

```sql
-- 创建业务数据库
CREATE DATABASE IF NOT EXISTS business_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE business_db;

-- 示例：用户扩展信息表
CREATE TABLE user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    sso_user_id BIGINT NOT NULL COMMENT 'SSO用户ID',
    company VARCHAR(100) COMMENT '公司名称',
    department VARCHAR(50) COMMENT '部门',
    position VARCHAR(50) COMMENT '职位',
    address VARCHAR(200) COMMENT '地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sso_user_id (sso_user_id)
) COMMENT '用户扩展信息表';

-- 示例：业务日志表
CREATE TABLE business_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT COMMENT '用户ID',
    action VARCHAR(50) COMMENT '操作类型',
    description VARCHAR(200) COMMENT '操作描述',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_action (action),
    INDEX idx_create_time (create_time)
) COMMENT '业务操作日志表';
```

## 注意事项

1. **认证统一**：无论是否使用独立数据库，用户认证都应该通过SSO服务端进行
2. **数据同步**：如果使用独立数据库存储用户信息，需要考虑与SSO服务端的数据同步
3. **权限控制**：业务权限可以在客户端数据库中管理，但基础认证权限应该在SSO服务端统一管理
