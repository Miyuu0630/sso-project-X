# SSO Client Frontend

SSO客户端应用的前端界面，基于Vue 3和Vite构建。

## 🎨 技术栈

- **框架**: Vue 3
- **构建工具**: Vite
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **UI组件**: 原生CSS + 组件化设计
- **HTTP客户端**: Axios

## 🚀 快速开始

### 前置要求
- Node.js 16+
- npm 或 yarn

### 安装依赖
```bash
npm install
# 或
yarn install
```

### 开发环境
```bash
npm run dev
# 或
yarn dev
```

应用将在 http://localhost:5173 启动

### 生产构建
```bash
npm run build
# 或
yarn build
```

### 预览生产构建
```bash
npm run preview
# 或
yarn preview
```

## ⚙️ 配置

### 环境变量
创建 `.env.local` 文件配置环境变量：

```env
# SSO服务端地址
VITE_SSO_SERVER_URL=http://localhost:8080

# 客户端后端地址
VITE_API_BASE_URL=http://localhost:8081

# 客户端ID
VITE_CLIENT_ID=your_client_id
```

### SSO配置
SSO相关配置位于 `src/config/sso.js`

## 📁 项目结构

```
src/
├── components/     # 可复用组件
├── views/          # 页面组件
├── router/         # 路由配置
├── stores/         # Pinia状态管理
├── utils/          # 工具函数
├── config/         # 配置文件
└── assets/         # 静态资源
```

## 🔐 认证流程

1. 用户访问受保护页面
2. 检查本地认证状态
3. 未认证则重定向到SSO登录页
4. SSO认证成功后回调到客户端
5. 获取用户信息并更新状态

## 🧪 测试

```bash
# 运行单元测试
npm run test:unit

# 运行端到端测试
npm run test:e2e
```

## 📱 页面说明

- `/` - 首页
- `/login` - 登录页面（重定向到SSO）
- `/dashboard` - 用户仪表板
- `/profile` - 用户资料
- `/users` - 用户管理（管理员）
- `/callback` - SSO回调处理

## 🔗 相关项目

- [SSO Server](../sso-server/) - SSO认证服务器
- [SSO Client Backend](../sso-client-backend/) - 客户端后端API

## 🛠️ 开发工具

推荐使用以下VS Code扩展：
- Vetur 或 Volar (Vue 3支持)
- ESLint
- Prettier
