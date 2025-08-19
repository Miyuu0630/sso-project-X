# SSO系统部署指南

## 项目概述

本项目是一个完整的企业级SSO（单点登录）系统，包含以下核心功能：

### ✅ 已完成功能
- **用户管理**: 注册、登录、密码修改、账号启用/停用
- **多登录方式**: 用户名、手机号、邮箱登录，支持第三方登录框架
- **权限管理**: 基于角色的权限控制，支持个人用户、企业用户、航司用户
- **安全机制**: 密码加密、数据脱敏、多设备登录管理
- **SSO认证**: 完整的单点登录认证流程
- **登录记录**: 详细的登录日志和会话管理

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   认证中心       │    │   业务系统1      │    │   业务系统2      │
│  sso-server     │    │  sso-client     │    │  sso-client     │
│   :8081         │◄──►│   :8082         │    │   :8083         │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │
         ▼
┌─────────────────┐
│   MySQL数据库    │
│   sso_db        │
└─────────────────┘
```

## 部署步骤

### 1. 环境准备

**系统要求:**
- Java 17+
- Maven 3.6+
- MySQL 9.2.0
- Redis 6.0+ (可选，用于缓存)

### 2. 数据库初始化

```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 创建数据库
CREATE DATABASE sso_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. 执行建表脚本
mysql -u root -p sso_db < database/sso_database_design.sql

# 4. 执行初始化数据脚本
mysql -u root -p sso_db < database/init_data.sql
```

### 3. 配置文件修改

**sso-server/src/main/resources/application.yml:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sso_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**sso-client/src/main/resources/application.yml:**
```yaml
sa-token:
  sso:
    server-url: http://localhost:8081
```

### 4. 启动服务

**启动认证中心:**
```bash
cd sso-server
mvn clean compile
mvn spring-boot:run
```

**启动业务系统:**
```bash
cd sso-client
mvn clean compile
mvn spring-boot:run
```

### 5. 验证部署

#### 5.1 基础功能测试

**用户注册:**
```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "confirmPassword": "123456",
    "email": "test@example.com",
    "userType": 1,
    "registerType": "username"
  }'
```

**用户登录:**
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "admin",
    "password": "admin123",
    "loginType": "username"
  }'
```

**获取用户信息:**
```bash
curl -X GET http://localhost:8081/auth/userinfo \
  -H "satoken: YOUR_TOKEN"
```

#### 5.2 SSO流程测试

1. **访问业务系统**: http://localhost:8082
2. **点击登录链接**: 会跳转到认证中心
3. **在认证中心登录**: 使用 admin/admin123
4. **自动跳转回业务系统**: 登录状态已同步

#### 5.3 管理功能测试

**查看用户列表:**
```bash
curl -X GET "http://localhost:8081/api/users?page=1&size=10" \
  -H "satoken: YOUR_TOKEN"
```

**查看登录记录:**
```bash
curl -X GET "http://localhost:8081/api/login-logs?page=1&size=10" \
  -H "satoken: YOUR_TOKEN"
```

## 默认账号

**超级管理员:**
- 用户名: admin
- 密码: admin123
- 权限: 所有权限

## API文档

### 认证相关接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /auth/register | POST | 用户注册 |
| /auth/login | POST | 用户登录 |
| /auth/logout | POST | 用户登出 |
| /auth/userinfo | GET | 获取用户信息 |
| /auth/change-password | POST | 修改密码 |

### SSO相关接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /sso/auth | GET | SSO认证入口 |
| /sso/logout | GET | SSO登出 |
| /sso/userinfo | GET | 获取SSO用户信息 |
| /sso/check-ticket | GET | 验证ticket |

### 管理相关接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/users | GET | 用户列表 |
| /api/roles | GET | 角色列表 |
| /api/permissions | GET | 权限列表 |
| /api/login-logs | GET | 登录记录 |

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 确认数据库连接配置正确
   - 验证用户名密码

2. **端口冲突**
   - 修改application.yml中的server.port
   - 确保8081和8082端口未被占用

3. **SSO跳转失败**
   - 检查sso-client中的server-url配置
   - 确认认证中心服务正常运行

### 日志查看

**查看应用日志:**
```bash
tail -f logs/spring.log
```

**查看错误日志:**
```bash
grep ERROR logs/spring.log
```

## 扩展功能

### 待实现功能

1. **第三方登录**
   - 微信登录集成
   - 支付宝登录集成
   - QQ登录集成

2. **短信邮件服务**
   - 阿里云短信服务
   - 邮件验证码服务

3. **前端界面**
   - Vue.js管理后台
   - 用户登录注册页面

4. **监控告警**
   - 系统监控面板
   - 异常告警机制

## 技术支持

如有问题，请检查：
1. 日志文件中的错误信息
2. 数据库连接状态
3. 网络连接情况
4. 配置文件是否正确

## 版本信息

- 当前版本: v1.0.0
- 更新时间: 2025-01-19
- 技术栈: Spring Boot 3.5.4 + Sa-Token 1.44.0 + MySQL 9.2.0
