# SSO 单点登录流程实现指南

## 🔄 完整的SSO流程

### 1. 用户访问业务系统

```javascript
// 前端路由守卫 - sso-client-frontend/src/router/index.js
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    // 检查本地是否有有效token
    const isLoggedIn = await authStore.checkTokenValidity()
    
    if (!isLoggedIn) {
      // 没有有效token，重定向到SSO登录
      const returnUrl = to.fullPath
      authStore.redirectToLogin(returnUrl)
      return
    }
  }
  
  next()
})
```

### 2. 重定向到SSO认证中心

```javascript
// sso-client-frontend/src/stores/auth.js
const redirectToLogin = (returnUrl = window.location.href) => {
  const ssoServerUrl = 'http://localhost:8081'
  const callbackUrl = `${window.location.origin}/callback`
  
  // 构建SSO登录URL
  const loginUrl = `${ssoServerUrl}/sso/auth?redirect=${encodeURIComponent(callbackUrl)}&return_url=${encodeURIComponent(returnUrl)}`
  
  console.log('重定向到SSO登录页面:', loginUrl)
  
  // 清除本地认证信息
  clearAuth()
  
  // 跳转到SSO登录页面
  window.location.href = loginUrl
}
```

### 3. SSO服务端处理登录

```java
// sso-server/src/main/java/org/example/ssoserver/controller/SsoServerController.java
@Autowired
private void configSso(SaSsoServerTemplate ssoServerTemplate) {
    // 配置：未登录时返回的View
    ssoServerTemplate.strategy.notLoginView = () -> {
        // 返回登录页面HTML
        return buildLoginPageHtml();
    };

    // 配置：登录处理函数
    ssoServerTemplate.strategy.doLoginHandle = (name, pwd) -> {
        try {
            // 查询数据库进行真实登录验证
            SysUser user = sysUserService.login(name, pwd);
            if (user != null) {
                // 登录成功，设置会话
                StpUtil.login(user.getId());
                
                // 记录登录日志
                loginLogService.recordLogin(user, request);
                
                // 更新最后登录信息
                sysUserService.updateLastLoginInfo(user.getId(), getClientIp(request));
                
                return SaResult.ok("登录成功！").setData(StpUtil.getTokenValue());
            }
            return SaResult.error("用户名或密码错误！");
        } catch (Exception e) {
            log.error("登录失败", e);
            return SaResult.error("登录失败：" + e.getMessage());
        }
    };
}
```

### 4. 生成ticket并回调

```java
// Sa-Token自动处理ticket生成和回调
// 登录成功后，Sa-Token会：
// 1. 生成临时ticket
// 2. 重定向到客户端回调地址：http://localhost:5173/callback?ticket=xxx
```

### 5. 客户端处理回调

```vue
<!-- sso-client-frontend/src/views/Callback.vue -->
<template>
  <div class="callback-container">
    <div class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>正在处理登录回调...</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
  try {
    const ticket = route.query.ticket
    const returnUrl = route.query.return_url || '/'
    
    if (ticket) {
      // 发送ticket到后端验证
      const success = await authStore.handleSsoCallback(ticket)
      
      if (success) {
        ElMessage.success('登录成功')
        // 跳转到原始页面
        router.replace(returnUrl)
      } else {
        ElMessage.error('登录验证失败')
        router.replace('/login')
      }
    } else {
      ElMessage.error('缺少登录凭证')
      router.replace('/login')
    }
  } catch (error) {
    console.error('处理登录回调失败:', error)
    ElMessage.error('登录处理失败')
    router.replace('/login')
  }
})
</script>
```

### 6. 客户端后端验证ticket

```java
// sso-client-backend/src/main/java/org/example/ssoclient/controller/SsoClientController.java
@PostMapping("/sso/verify-ticket")
public Result<LoginResponse> verifyTicket(@RequestParam String ticket, HttpServletRequest request) {
    try {
        // 1. 调用SSO服务端验证ticket
        String url = ssoServerUrl + "/sso/check-ticket?ticket=" + ticket;
        String response = HttpUtil.get(url);
        JSONObject result = JSONUtil.parseObj(response);

        if (result.getInt("code") == 200) {
            JSONObject data = result.getJSONObject("data");
            if (data.getBool("valid")) {
                // 2. ticket有效，获取用户信息
                Long userId = data.getLong("loginId");
                
                // 3. 创建本地会话
                StpUtil.login(userId);
                
                // 4. 获取用户详细信息
                SysUser userInfo = userInfoService.getUserInfo(userId);
                
                // 5. 记录登录设备
                deviceManagementService.recordLoginDevice(userId, request);
                
                // 6. 构建响应
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setToken(StpUtil.getTokenValue());
                loginResponse.setUserId(userId);
                loginResponse.setUsername(userInfo.getUsername());
                loginResponse.setNickname(userInfo.getNickname());
                loginResponse.setAvatar(userInfo.getAvatar());
                
                return Result.success("登录成功", loginResponse);
            }
        }
        
        return Result.error("ticket验证失败");
    } catch (Exception e) {
        log.error("验证ticket失败", e);
        return Result.error("验证失败：" + e.getMessage());
    }
}
```

### 7. 前端存储token并跳转

```javascript
// sso-client-frontend/src/stores/auth.js
const handleSsoCallback = async (ticket) => {
  try {
    // 发送ticket到后端验证
    const response = await axios.post('/api/sso/verify-ticket', { ticket })
    
    if (response.data.success) {
      const loginData = response.data.data
      
      // 存储token和用户信息
      token.value = loginData.token
      userInfo.value = {
        userId: loginData.userId,
        username: loginData.username,
        nickname: loginData.nickname,
        avatar: loginData.avatar
      }
      
      // 存储到Cookie
      Cookies.set('satoken', loginData.token, { expires: 7 })
      
      // 设置axios默认header
      axios.defaults.headers.common['satoken'] = loginData.token
      
      return true
    }
    
    return false
  } catch (error) {
    console.error('SSO回调处理失败:', error)
    return false
  }
}
```

## 🔐 单点登出流程

### 1. 前端发起登出

```javascript
// sso-client-frontend/src/stores/auth.js
const logout = async () => {
  try {
    // 1. 调用本地登出接口
    await axios.post('/api/auth/logout')
    
    // 2. 清除本地认证信息
    clearAuth()
    
    // 3. 重定向到SSO登出页面（可选）
    const ssoLogoutUrl = `${ssoServerUrl}/sso/signout?redirect=${encodeURIComponent(window.location.origin)}`
    window.location.href = ssoLogoutUrl
    
  } catch (error) {
    console.error('登出失败:', error)
    // 即使登出失败也清除本地信息
    clearAuth()
  }
}
```

### 2. 客户端后端处理登出

```java
// sso-client-backend/src/main/java/org/example/ssoclient/controller/AuthController.java
@PostMapping("/auth/logout")
@SaCheckLogin
public Result<String> logout() {
    try {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 1. 记录登出日志
        loginLogService.recordLogout(userId);
        
        // 2. 执行本地登出
        StpUtil.logout();
        
        return Result.success("登出成功");
    } catch (Exception e) {
        log.error("登出失败", e);
        return Result.error("登出失败：" + e.getMessage());
    }
}
```

### 3. SSO服务端处理全局登出

```java
// sso-server配置中已启用单点登出
sa-token:
  sso-server:
    is-slo: true  # 开启单点登出
```

## 🛡️ 安全性考虑

### 1. Ticket安全

```yaml
# application.yml
sa-token:
  sso-server:
    ticket-timeout: 300  # ticket有效期5分钟
    secretkey: "your-secret-key"  # 接口调用秘钥
```

### 2. 跨域配置

```java
// sso-server全局跨域配置
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
```

### 3. HTTPS配置

```yaml
# 生产环境配置
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: your-password
    key-store-type: PKCS12
```

## 📊 监控和日志

### 1. 登录日志记录

```java
@Service
public class LoginLogService {
    
    public void recordLogin(SysUser user, HttpServletRequest request) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUserId(user.getId());
        loginLog.setUsername(user.getUsername());
        loginLog.setLoginType("password");
        loginLog.setLoginIp(getClientIp(request));
        loginLog.setLoginLocation(getLocationByIp(loginLog.getLoginIp()));
        loginLog.setBrowser(getBrowser(request));
        loginLog.setOs(getOs(request));
        loginLog.setDeviceType(getDeviceType(request));
        loginLog.setStatus("1");
        loginLog.setLoginTime(LocalDateTime.now());
        
        // 检查是否为新设备
        String deviceFingerprint = generateDeviceFingerprint(request);
        loginLog.setDeviceFingerprint(deviceFingerprint);
        loginLog.setIsNewDevice(deviceManagementService.isNewDevice(user.getId(), deviceFingerprint));
        
        // 保存登录日志
        this.save(loginLog);
    }
}
```

### 2. 性能监控

```java
// 使用AOP记录接口性能
@Aspect
@Component
public class PerformanceAspect {
    
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = point.proceed();
            long endTime = System.currentTimeMillis();
            
            log.info("接口 {} 执行时间: {}ms", point.getSignature().getName(), endTime - startTime);
            
            return result;
        } catch (Exception e) {
            log.error("接口 {} 执行异常", point.getSignature().getName(), e);
            throw e;
        }
    }
}
```

这个完整的SSO流程实现了：
- 🔐 安全的票据验证机制
- 🔄 完整的登录登出流程
- 📱 多设备登录管理
- 📊 详细的日志记录
- 🛡️ 全面的安全防护
