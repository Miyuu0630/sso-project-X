# SSO Project X

一个完整的单点登录（SSO）系统解决方案，包含服务端和客户端实现。

## 🏗️ 项目架构

```
sso-project-X/
├── 📚 docs/                    # 项目文档
├── 🗄️ db/                      # 数据库脚本
├── 🔐 sso-server/              # SSO服务端 (Spring Boot)
├── 🔧 sso-client-backend/      # SSO客户端后端 (Spring Boot)
├── 🎨 sso-client-frontend/     # SSO客户端前端 (Vue3 + Vite)
├── 📄 templates/               # 模板文件
└── 📝 logs/                    # 日志文件
```

## 🚀 快速开始

### 前置要求
- Java 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 1. 数据库初始化
```bash
# 执行服务端数据库脚本
mysql -u root -p < db/sso-server/sso_server_create_tables.sql
mysql -u root -p < db/sso-server/sso_server_init_data.sql
```

### 2. 启动SSO服务端
```bash
cd sso-server
mvn spring-boot:run
```
服务端将在 http://localhost:8081 启动

### 3. 启动客户端后端
```bash
cd sso-client-backend
mvn spring-boot:run
```
客户端后端将在 http://localhost:8082 启动

### 4. 启动客户端前端
```bash
cd sso-client-frontend
npm install
npm run dev
```
客户端前端将在 http://localhost:5173 启动

## 📖 文档

- [架构设计](docs/architecture/) - 系统架构和交互规范
- [分析报告](docs/analysis/) - 项目分析和检查报告
- [实施指南](docs/guides/) - 集成和实施指南
- [部署文档](docs/deployment/) - 部署相关文档

## 🔧 开发

### 技术栈
- **后端**: Spring Boot, Spring Security, JWT
- **前端**: Vue 3, Vite, Vue Router, Pinia
- **数据库**: MySQL
- **构建工具**: Maven, Vite

### 项目结构说明
- `sso-server/`: 独立的SSO认证服务器
- `sso-client-backend/`: 客户端应用的后端API
- `sso-client-frontend/`: 客户端应用的前端界面
- `docs/`: 按类型分类的项目文档
- `db/`: 按服务分类的数据库脚本
- `templates/`: 可复用的模板文件

## 📝 日志

应用日志统一存放在 `logs/` 目录下：
- `sso-server.log`: SSO服务端日志
- `sso-client.log`: SSO客户端日志

## 🤝 贡献

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
