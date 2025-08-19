# SSO Client Backend

SSO客户端应用的后端服务，基于Spring Boot构建。

## 🔧 技术栈

- **框架**: Spring Boot 2.7+
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0+
- **构建工具**: Maven 3.6+

## 🚀 快速开始

### 前置要求
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### 运行应用
```bash
# 安装依赖
mvn clean install

# 启动应用
mvn spring-boot:run
```

应用将在 http://localhost:8082 启动

## ⚙️ 配置

主要配置文件位于 `src/main/resources/application.yml`

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sso_client
    username: your_username
    password: your_password
```

### SSO服务端配置
```yaml
sso:
  server:
    url: http://localhost:8081
    client-id: your_client_id
    client-secret: your_client_secret
```

## 📁 项目结构

```
src/main/java/
├── controller/     # REST API控制器
├── service/        # 业务逻辑服务
├── config/         # 配置类
├── security/       # 安全相关配置
├── model/          # 数据模型
└── util/           # 工具类
```

## 🔐 安全特性

- JWT令牌验证
- SSO单点登录集成
- 角色权限控制
- 会话管理

## 🧪 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 📝 API文档

启动应用后，访问 http://localhost:8082/swagger-ui.html 查看API文档

## 🔗 相关项目

- [SSO Server](../sso-server/) - SSO认证服务器
- [SSO Client Frontend](../sso-client-frontend/) - 客户端前端应用
