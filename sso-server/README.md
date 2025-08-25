## SSO服务器

基于Sa-Token的单点登录系统服务器端

### Refresh Token功能

#### 功能概述
- 支持Access Token和Refresh Token双令牌机制
- Refresh Token有效期可配置（默认7天）
- 支持设备指纹验证和安全检查
- 提供完整的Refresh Token生命周期管理

#### 配置项
```yaml
# Refresh Token 配置
refresh-token:
  # Refresh Token有效期（天）
  expire-days: 7
  # 是否启用Refresh Token自动续期
  auto-renewal: true
  # 续期阈值（在Refresh Token过期前多少天自动续期）
  renewal-threshold-days: 1
  # 是否启用设备指纹验证
  device-fingerprint-check: true
```

#### API接口

##### 1. 用户登录
```http
POST /auth/login
```

**响应示例：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "satoken_xxx",
    "refreshToken": "refresh_xxx",
    "tokenType": "Bearer",
    "expiresIn": 2592000,
    "refreshTokenExpiresIn": 604800,
    "userId": 1,
    "username": "admin",
    // ... 其他用户信息
  }
}
```

##### 2. 刷新Access Token
```http
POST /auth/refresh?refreshToken=refresh_xxx
```

**响应示例：**
```json
{
  "code": 200,
  "message": "Token刷新成功",
  "data": {
    "accessToken": "satoken_new_xxx",
    "tokenType": "Bearer",
    "expiresIn": 2592000
  }
}
```

##### 3. 撤销Refresh Token
```http
POST /auth/revoke?refreshToken=refresh_xxx
```

##### 4. 获取Refresh Token状态（调试用）
```http
GET /auth/refresh-token/status?refreshToken=refresh_xxx
```

#### 使用流程

1. **用户登录**：调用登录接口，获得access token和refresh token
2. **访问API**：使用access token调用业务接口
3. **Token过期**：当access token过期时，使用refresh token刷新获得新的access token
4. **安全退出**：调用撤销接口或登出接口清理refresh token

#### 安全特性

- Refresh Token存储在Redis中，支持分布式部署
- 支持设备指纹验证，防止token盗用
- Refresh Token过期后自动清理
- 支持管理员强制撤销特定用户的refresh token
- 并发安全控制，防止重复刷新
- 自动续期机制，在过期前自动延长有效期

### 客户端集成指南

#### JavaScript客户端示例

项目提供了完整的JavaScript客户端实现（`RefreshTokenClientExample.js`），包含以下功能：

- 自动token刷新
- 设备指纹生成
- 请求重试机制
- 并发安全控制
- 本地存储管理

#### 基本使用流程

```javascript
// 1. 初始化客户端
const tokenManager = new TokenManager();
const apiClient = new ApiClient(tokenManager);
const authManager = new AuthManager(tokenManager, apiClient);

// 2. 用户登录
const loginResult = await authManager.login('username', 'password');
console.log('登录成功:', loginResult);

// 3. 发送API请求（自动处理token刷新）
const userInfo = await apiClient.get('/auth/userinfo');
console.log('用户信息:', userInfo);

// 4. 用户登出
await authManager.logout();
```

#### 配置选项

```javascript
const CONFIG = {
    baseURL: 'http://localhost:8081',      // 服务端地址
    tokenStorageKey: 'sso_tokens',         // 本地存储key
    refreshThreshold: 300000,             // 刷新阈值（毫秒）
    maxRetryAttempts: 3,                  // 最大重试次数
    retryDelay: 1000                      // 重试延迟（毫秒）
};
```

#### 高级功能

- **自动刷新**：在access token过期前自动刷新
- **设备指纹**：生成唯一设备标识，增强安全性
- **请求重试**：网络错误时自动重试
- **并发控制**：防止多个并发刷新请求

### 最佳实践

#### 1. 错误处理
```javascript
try {
    const data = await apiClient.get('/api/data');
} catch (error) {
    if (error.message.includes('Authentication failed')) {
        // 跳转到登录页面
        window.location.href = '/login';
    } else {
        // 处理其他错误
        console.error('API请求失败:', error);
    }
}
```

#### 2. 监听token变化
```javascript
// 监听token过期事件
window.addEventListener('tokenExpired', () => {
    console.log('Token已过期，需要重新登录');
    // 显示重新登录提示
});
```

#### 3. 安全考虑
- 在HTTPS环境下使用
- 定期清理本地存储
- 敏感操作前验证用户状态
- 实现用户会话超时处理

### 监控与运维

#### 统计接口
- `GET /auth/refresh-token/stats` - 获取使用统计
- `POST /auth/refresh-token/cleanup` - 清理过期token

#### 日志监控
系统会记录详细的操作日志，包括：
- Token生成和刷新
- 设备指纹验证
- 并发控制信息
- 错误和异常信息
