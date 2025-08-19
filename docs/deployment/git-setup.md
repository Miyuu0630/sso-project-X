# Git 版本管理设置指南

## 🚀 快速开始

### 1. 初始化Git仓库

```bash
# 在项目根目录下执行
git init

# 添加远程仓库（替换为您的GitHub仓库地址）
git remote add origin https://github.com/yourusername/sso-project-x.git
```

### 2. 首次提交

```bash
# 添加所有文件到暂存区
git add .

# 检查要提交的文件
git status

# 提交重构后的项目结构
git commit -m "feat: 重构项目目录结构

- 📚 文档归档：按类型分类到docs目录
- 🗄️ 数据库脚本统一：集中到db目录
- 🔧 前后端分离：独立的backend和frontend项目
- 📄 模板文件集中：统一管理模板和示例代码
- 📝 配置文件修正：统一端口和路径配置
- 📖 README完善：为各子项目添加详细说明

BREAKING CHANGE: 原sso-client目录已拆分为sso-client-backend和sso-client-frontend"

# 推送到GitHub
git push -u origin main
```

## 📋 .gitignore 说明

已为您创建的 `.gitignore` 文件包含以下忽略规则：

### Java/Maven 相关
- `target/` - Maven构建输出
- `*.jar`, `*.war` - 编译后的包文件
- `.mvn/` - Maven wrapper配置

### Node.js/Vue.js 相关
- `node_modules/` - 依赖包
- `dist/`, `build/` - 构建输出
- `.env*` - 环境变量文件

### IDE 相关
- `.idea/` - IntelliJ IDEA配置
- `.vscode/` - VS Code配置
- `*.iml` - IntelliJ模块文件

### 系统文件
- `.DS_Store` - macOS系统文件
- `Thumbs.db` - Windows缩略图
- `*.log` - 日志文件

### 安全相关
- `*.key`, `*.pem` - 密钥文件
- `*-local.properties` - 本地配置文件

## 🔧 推荐的Git工作流

### 分支策略

```bash
# 主分支
main          # 生产环境代码

# 开发分支
develop       # 开发环境代码

# 功能分支
feature/xxx   # 新功能开发
bugfix/xxx    # Bug修复
hotfix/xxx    # 紧急修复
```

### 常用命令

```bash
# 创建并切换到新分支
git checkout -b feature/new-feature

# 查看状态
git status

# 添加文件
git add .
git add specific-file.java

# 提交
git commit -m "feat: 添加新功能"

# 推送分支
git push origin feature/new-feature

# 合并分支
git checkout main
git merge feature/new-feature

# 删除已合并的分支
git branch -d feature/new-feature
```

## 📝 提交信息规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### 类型说明
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

### 示例
```bash
git commit -m "feat(auth): 添加JWT令牌验证功能"
git commit -m "fix(login): 修复登录页面验证码显示问题"
git commit -m "docs: 更新API文档"
git commit -m "refactor(user): 重构用户管理模块"
```

## 🔒 敏感信息处理

### 环境变量文件
创建 `.env.example` 文件作为模板：

```bash
# .env.example
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sso_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=your_jwt_secret
```

### 配置文件模板
为敏感配置创建模板文件：

```bash
# application-template.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sso_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

## 🚨 注意事项

1. **首次推送前检查**：确保没有提交敏感信息
2. **定期备份**：重要节点创建tag标记
3. **分支保护**：在GitHub设置main分支保护规则
4. **代码审查**：重要功能通过Pull Request合并

## 📚 相关资源

- [Git官方文档](https://git-scm.com/doc)
- [GitHub使用指南](https://docs.github.com/)
- [Conventional Commits规范](https://www.conventionalcommits.org/)
- [Git工作流指南](https://www.atlassian.com/git/tutorials/comparing-workflows)
