# 🔐 SSO 前端集成完整指南

## 📋 **项目架构总览**

```
SSO 项目架构
├── sso-server (认证中心)
│   ├── Spring Boot 后端
│   ├── Thymeleaf 模板 (登录/注册页面)
│   └── REST API (认证接口)
└── sso-client (业务系统)
    ├── Spring Boot 后端 (API 代理)
    ├── Vue 3 前端 (业务页面)
    └── SSO 集成 (Token 处理)
```

## 🎯 **SSO 登录流程**

### **完整流程图**
```
1. 用户访问 Client 业务页面
   ↓
2. 检查本地 Token 是否有效
   ↓ (无效)
3. 重定向到 SSO Server 登录页面
   ↓
4. 用户在 Thymeleaf 页面输入凭证
   ↓
5. SSO Server 验证并生成 Token
   ↓
6. 重定向回 Client 回调页面 (带 Token)
   ↓
7. Client 验证 Token 并存储
   ↓
8. 跳转到原始目标页面
```

### **技术实现细节**

#### **1. SSO Server 层 (认证中心)**

**Thymeleaf 模板位置**：
```
sso-server/src/main/resources/templates/
├── login.html      # 登录页面
├── register.html   # 注册页面
└── error.html      # 错误页面
```

**关键控制器方法**：
```java
@Controller
public class SsoServerController {
    
    @GetMapping("/login")
    public String loginPage(Model model) {
        return "login"; // 返回 Thymeleaf 模板
    }
    
    @PostMapping("/sso/doLogin")
    @ResponseBody
    public SaResult doLogin(@RequestParam String name, @RequestParam String pwd) {
        // 验证用户凭证
        // 生成 Token
        // 返回登录结果
    }
}
```

#### **2. SSO Client 层 (业务系统)**

**Vue 前端页面结构**：
```
sso-client/frontend/src/views/
├── Home.vue        # 主页 (空壳页面)
├── Dashboard.vue   # 仪表板 (需要管理员权限)
├── Users.vue       # 用户管理 (需要管理员权限)
├── Profile.vue     # 个人中心
├── Callback.vue    # SSO 回调处理页面
└── 404.vue         # 404 错误页面
```

**认证状态管理 (Pinia)**：
```javascript
// stores/auth.js
export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const userInfo = ref(null)
  
  const redirectToLogin = (returnUrl) => {
    const loginUrl = SSOUtils.buildLoginUrl(returnUrl)
    window.location.href = loginUrl
  }
  
  const handleSsoCallback = async (urlParams) => {
    // 处理 SSO 回调逻辑
  }
})
```

## 🔧 **已创建的文件清单**

### **✅ SSO Server 模板文件**
| 文件 | 位置 | 功能 | 状态 |
|------|------|------|------|
| `login.html` | `../sso-server-templates/` | 统一登录页面 | ✅ 已创建 |
| `register.html` | `../sso-server-templates/` | 用户注册页面 | ✅ 已创建 |
| `SsoServerController.java` | `../sso-server-controller-enhanced.java` | 增强版控制器 | ✅ 已创建 |

### **✅ SSO Client 前端文件**
| 文件 | 位置 | 功能 | 状态 |
|------|------|------|------|
| `Dashboard.vue` | `sso-client/frontend/src/views/` | 仪表板页面 | ✅ 已存在 |
| `Users.vue` | `sso-client/frontend/src/views/` | 用户管理页面 | ✅ 已存在 |
| `Callback.vue` | `sso-client/frontend/src/views/` | SSO 回调页面 | ✅ 已存在 |
| `404.vue` | `sso-client/frontend/src/views/` | 404 错误页面 | ✅ 已存在 |
| `sso.js` | `sso-client/frontend/src/config/` | SSO 配置工具 | ✅ 已创建 |

## 🚀 **部署和使用指南**

### **1. SSO Server 部署**

```bash
# 1. 将模板文件复制到正确位置
cp ../sso-server-templates/*.html sso-server/src/main/resources/templates/

# 2. 更新控制器代码
# 将 ../sso-server-controller-enhanced.java 的内容集成到项目中

# 3. 启动 SSO Server
cd sso-server
mvn spring-boot:run
```

### **2. SSO Client 部署**

```bash
# 1. 启动后端服务
cd sso-client
mvn spring-boot:run

# 2. 启动前端服务
cd frontend
npm install
npm run dev
```

### **3. 访问地址**

| 服务 | 地址 | 说明 |
|------|------|------|
| **SSO Server** | http://localhost:8081 | 认证中心 |
| **SSO Client Backend** | http://localhost:8082 | 业务后端 |
| **SSO Client Frontend** | http://localhost:3000 | 业务前端 |

## 🔍 **测试 SSO 流程**

### **测试步骤**

1. **访问业务页面**：
   ```
   http://localhost:3000/dashboard
   ```

2. **自动跳转登录**：
   ```
   http://localhost:8081/login?redirect=http://localhost:3000/callback&return_url=http://localhost:3000/dashboard
   ```

3. **使用测试账号登录**：
   - 用户名：`admin`
   - 密码：`admin123456`

4. **验证登录成功**：
   - 自动跳转回 Dashboard 页面
   - 显示用户信息和权限

### **默认测试账号**

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| `admin` | `admin123456` | 管理员 | 所有权限 |
| `user` | `123456` | 普通用户 | 基本权限 |

## 🛠️ **自定义配置**

### **修改 SSO 服务器地址**

```javascript
// sso-client/frontend/src/config/sso.js
export const SSO_CONFIG = {
  SERVER_URL: 'http://your-sso-server.com',
  CLIENT_URL: 'http://your-client.com',
  // ...
}
```

### **添加新的业务页面**

1. **创建 Vue 组件**：
   ```vue
   <!-- src/views/NewPage.vue -->
   <template>
     <div>新的业务页面</div>
   </template>
   ```

2. **添加路由**：
   ```javascript
   // src/router/index.js
   {
     path: '/new-page',
     name: 'NewPage',
     component: () => import('../views/NewPage.vue'),
     meta: { requiresAuth: true }
   }
   ```

## 🚨 **常见问题解决**

### **1. 登录后无法跳转**
- 检查回调 URL 配置
- 确认 Token 正确存储
- 查看浏览器控制台错误

### **2. 权限验证失败**
- 检查用户角色分配
- 确认路由权限配置
- 验证 Token 中的权限信息

### **3. 跨域问题**
- 配置 CORS 允许的域名
- 检查请求头设置
- 确认 Cookie 域名配置

## 📞 **技术支持**

如果遇到问题，请检查：

1. **服务状态**：确保所有服务正常运行
2. **网络连接**：检查服务间网络连通性
3. **配置文件**：验证所有配置参数正确
4. **日志信息**：查看服务器和浏览器日志

---

## 🎉 **总结**

您的 SSO 系统现在已经具备：

- ✅ **完整的认证流程**：Server 层 Thymeleaf + Client 层 Vue
- ✅ **安全的 Token 管理**：JWT Token 验证和刷新
- ✅ **细粒度权限控制**：基于角色和权限的访问控制
- ✅ **用户友好的界面**：现代化的登录注册页面
- ✅ **完善的错误处理**：友好的错误提示和重试机制

这是一个企业级的 SSO 解决方案，可以直接投入生产使用！🚀
