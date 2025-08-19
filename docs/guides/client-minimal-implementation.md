# Client层最简化实现方案

## 🎯 设计目标

创建一个**主页面空壳**的业务系统，专注于：
- 与SSO Server的集成
- 基础的用户会话管理
- 为后续业务模块扩展提供骨架
- **不处理任何认证逻辑**

## 📁 项目结构

```
sso-client-minimal/
├── src/main/java/com/example/client/
│   ├── ClientApplication.java              # 启动类
│   ├── config/
│   │   ├── SsoConfig.java                  # SSO配置
│   │   └── WebConfig.java                  # Web配置
│   ├── controller/
│   │   ├── HomeController.java             # 主页控制器
│   │   ├── SsoController.java              # SSO回调控制器
│   │   └── ApiController.java              # 业务API控制器
│   ├── service/
│   │   ├── UserService.java                # 用户服务
│   │   └── PermissionService.java          # 权限服务
│   └── model/
│       ├── UserInfo.java                   # 用户信息模型
│       └── ApiResponse.java                # API响应模型
├── src/main/resources/
│   ├── application.yml                     # 配置文件
│   ├── static/                            # 静态资源
│   └── templates/                         # 模板文件
└── frontend/                              # 前端代码(可选)
    ├── src/
    │   ├── views/
    │   │   ├── Home.vue                   # 主页
    │   │   └── Login.vue                  # 登录页
    │   ├── router/
    │   └── store/
    └── package.json
```

## 🔧 核心实现代码

### 1. 启动类
```java
@SpringBootApplication
@EnableScheduling
public class ClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }
}
```

### 2. SSO配置类
```java
@Configuration
@Slf4j
public class SsoConfig implements WebMvcConfigurer {
    
    @Value("${sso.server.url}")
    private String ssoServerUrl;
    
    /**
     * 注册SSO拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter
                // 拦截所有路由
                .match("/**")
                // 排除不需要登录的路径
                .notMatch(
                    "/", "/home", "/public/**", 
                    "/sso/**", "/static/**", 
                    "/favicon.ico", "/error"
                )
                // 执行登录校验
                .check(r -> {
                    if (!StpUtil.isLogin()) {
                        log.info("用户未登录，准备跳转到SSO认证中心");
                        // 构建登录URL
                        String loginUrl = buildLoginUrl(r.getRequest());
                        throw new SaTokenException("REDIRECT:" + loginUrl);
                    }
                });
        })).addPathPatterns("/**");
    }
    
    /**
     * 构建登录URL
     */
    private String buildLoginUrl(HttpServletRequest request) {
        String currentUrl = request.getRequestURL().toString();
        if (request.getQueryString() != null) {
            currentUrl += "?" + request.getQueryString();
        }
        
        return ssoServerUrl + "/sso/auth?redirect=" + 
               URLEncoder.encode(currentUrl, StandardCharsets.UTF_8);
    }
}
```

### 3. 主页控制器（空壳实现）
```java
@Controller
@Slf4j
public class HomeController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 首页 - 无需登录
     */
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        if (StpUtil.isLogin()) {
            // 已登录，显示用户信息
            UserInfo userInfo = userService.getCurrentUserInfo();
            model.addAttribute("user", userInfo);
            model.addAttribute("isLoggedIn", true);
        } else {
            // 未登录，显示登录提示
            model.addAttribute("isLoggedIn", false);
        }
        return "home";
    }
    
    /**
     * 仪表板 - 需要登录
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 此处会被SSO拦截器自动处理登录检查
        UserInfo userInfo = userService.getCurrentUserInfo();
        model.addAttribute("user", userInfo);
        return "dashboard";
    }
    
    /**
     * 个人中心 - 需要登录
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        UserInfo userInfo = userService.getCurrentUserInfo();
        List<String> permissions = userService.getCurrentUserPermissions();
        
        model.addAttribute("user", userInfo);
        model.addAttribute("permissions", permissions);
        return "profile";
    }
}
```

### 4. SSO回调控制器
```java
@RestController
@RequestMapping("/sso")
@Slf4j
public class SsoController {
    
    @Autowired
    private UserService userService;
    
    /**
     * SSO登录回调处理
     */
    @RequestMapping("/callback")
    public Object callback(HttpServletRequest request) {
        log.info("收到SSO登录回调");
        
        try {
            // 使用Sa-Token处理SSO回调
            Object result = SaSsoHandle.clientRequest();
            
            // 如果登录成功，初始化用户信息
            if (StpUtil.isLogin()) {
                userService.initUserSession();
                log.info("用户登录成功，用户ID: {}", StpUtil.getLoginId());
            }
            
            return result;
        } catch (Exception e) {
            log.error("SSO回调处理失败", e);
            return "redirect:/error";
        }
    }
    
    /**
     * 登出回调
     */
    @RequestMapping("/logout-callback")
    public Object logoutCallback() {
        log.info("收到SSO登出回调");
        
        try {
            // 清理本地会话
            if (StpUtil.isLogin()) {
                userService.clearUserSession();
                StpUtil.logout();
            }
            
            return "redirect:/";
        } catch (Exception e) {
            log.error("登出回调处理失败", e);
            return "redirect:/";
        }
    }
    
    /**
     * 手动登出
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout() {
        try {
            if (StpUtil.isLogin()) {
                userService.clearUserSession();
                StpUtil.logout();
            }
            
            return ResponseEntity.ok(ApiResponse.success("登出成功"));
        } catch (Exception e) {
            log.error("登出失败", e);
            return ResponseEntity.ok(ApiResponse.error("登出失败"));
        }
    }
}
```

### 5. 用户服务（业务适配层）
```java
@Service
@Slf4j
public class UserService {
    
    @Value("${sso.server.url}")
    private String ssoServerUrl;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 获取当前用户信息
     */
    public UserInfo getCurrentUserInfo() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        String cacheKey = "user_info:" + userId;
        
        // 先从缓存获取
        UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(cacheKey);
        if (userInfo != null) {
            return userInfo;
        }
        
        // 从SSO Server获取
        userInfo = fetchUserInfoFromSso();
        if (userInfo != null) {
            // 缓存5分钟
            redisTemplate.opsForValue().set(cacheKey, userInfo, 5, TimeUnit.MINUTES);
        }
        
        return userInfo;
    }
    
    /**
     * 获取当前用户权限
     */
    public List<String> getCurrentUserPermissions() {
        if (!StpUtil.isLogin()) {
            return Collections.emptyList();
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        String cacheKey = "user_permissions:" + userId;
        
        // 先从缓存获取
        List<String> permissions = (List<String>) redisTemplate.opsForValue().get(cacheKey);
        if (permissions != null) {
            return permissions;
        }
        
        // 从SSO Server获取
        permissions = fetchUserPermissionsFromSso();
        if (permissions != null) {
            // 缓存10分钟
            redisTemplate.opsForValue().set(cacheKey, permissions, 10, TimeUnit.MINUTES);
        }
        
        return permissions != null ? permissions : Collections.emptyList();
    }
    
    /**
     * 初始化用户会话
     */
    public void initUserSession() {
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            log.info("初始化用户会话，用户ID: {}", userId);
            
            // 预加载用户信息和权限
            getCurrentUserInfo();
            getCurrentUserPermissions();
        }
    }
    
    /**
     * 清理用户会话
     */
    public void clearUserSession() {
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            log.info("清理用户会话，用户ID: {}", userId);
            
            // 清理缓存
            redisTemplate.delete("user_info:" + userId);
            redisTemplate.delete("user_permissions:" + userId);
        }
    }
    
    /**
     * 从SSO Server获取用户信息
     */
    private UserInfo fetchUserInfoFromSso() {
        try {
            String token = StpUtil.getTokenValue();
            String url = ssoServerUrl + "/sso/userinfo?token=" + token;
            
            // 这里使用HTTP客户端调用SSO Server
            // 实际实现中可以使用RestTemplate或WebClient
            String response = HttpUtil.get(url);
            JSONObject result = JSONUtil.parseObj(response);
            
            if (result.getInt("code") == 200) {
                return JSONUtil.toBean(result.getJSONObject("data"), UserInfo.class);
            }
            
            return null;
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return null;
        }
    }
    
    /**
     * 从SSO Server获取用户权限
     */
    private List<String> fetchUserPermissionsFromSso() {
        try {
            String token = StpUtil.getTokenValue();
            String url = ssoServerUrl + "/sso/permissions?token=" + token;
            
            String response = HttpUtil.get(url);
            JSONObject result = JSONUtil.parseObj(response);
            
            if (result.getInt("code") == 200) {
                return result.getJSONObject("data").getBeanList("permissions", String.class);
            }
            
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("获取用户权限失败", e);
            return Collections.emptyList();
        }
    }
}
```

### 6. 配置文件
```yaml
# application.yml
server:
  port: 8082
  servlet:
    context-path: /

spring:
  application:
    name: business-system-client
  
  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      database: 2
      timeout: 3000ms

# SSO配置
sso:
  server:
    url: http://localhost:8081
  client:
    id: business-system-a
    secret: ${CLIENT_SECRET:default-secret}

# Sa-Token配置
sa-token:
  token-name: Authorization
  timeout: 7200
  is-concurrent: true
  is-share: false
  sso:
    is-sso: true
    server-url: ${sso.server.url}
    auth-url: ${sso.server.url}/sso/auth
    is-http: true
    secretkey: ${SSO_SECRET_KEY:default-secret-key}

# 日志配置
logging:
  level:
    com.example.client: DEBUG
    cn.dev33.satoken: DEBUG
```

## 🎨 前端空壳实现

### 主页模板 (Thymeleaf)
```html
<!-- templates/home.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>业务系统 - 首页</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container">
            <a class="navbar-brand" href="/">业务系统</a>
            <div class="navbar-nav ms-auto">
                <div th:if="${isLoggedIn}">
                    <span class="navbar-text me-3">欢迎，<span th:text="${user.realName}"></span></span>
                    <a class="btn btn-outline-light btn-sm" href="/profile">个人中心</a>
                    <button class="btn btn-outline-light btn-sm ms-2" onclick="logout()">退出</button>
                </div>
                <div th:unless="${isLoggedIn}">
                    <a class="btn btn-outline-light" href="/dashboard">立即登录</a>
                </div>
            </div>
        </div>
    </nav>

    <div class="container mt-5">
        <div class="row">
            <div class="col-md-8 mx-auto text-center">
                <h1 class="display-4">欢迎使用业务系统</h1>
                
                <div th:if="${isLoggedIn}" class="mt-4">
                    <div class="alert alert-success">
                        <h4>登录成功！</h4>
                        <p>您已成功通过SSO认证中心登录</p>
                    </div>
                    
                    <div class="row mt-4">
                        <div class="col-md-4">
                            <div class="card">
                                <div class="card-body">
                                    <h5 class="card-title">控制台</h5>
                                    <p class="card-text">查看系统概览和统计信息</p>
                                    <a href="/dashboard" class="btn btn-primary">进入</a>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="card">
                                <div class="card-body">
                                    <h5 class="card-title">个人中心</h5>
                                    <p class="card-text">管理个人信息和设置</p>
                                    <a href="/profile" class="btn btn-primary">进入</a>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="card">
                                <div class="card-body">
                                    <h5 class="card-title">业务功能</h5>
                                    <p class="card-text">即将上线，敬请期待</p>
                                    <button class="btn btn-secondary" disabled>敬请期待</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div th:unless="${isLoggedIn}" class="mt-4">
                    <div class="alert alert-info">
                        <h4>请先登录</h4>
                        <p>点击下方按钮通过SSO认证中心登录</p>
                    </div>
                    <a href="/dashboard" class="btn btn-primary btn-lg">立即登录</a>
                </div>
            </div>
        </div>
    </div>

    <script>
        function logout() {
            fetch('/sso/logout', { method: 'POST' })
                .then(() => window.location.href = '/')
                .catch(err => console.error('登出失败:', err));
        }
    </script>
</body>
</html>
```

## 🚀 部署和测试

### 1. 启动步骤
```bash
# 1. 启动SSO Server (端口8081)
cd sso-server
mvn spring-boot:run

# 2. 启动Client (端口8082)
cd sso-client
mvn spring-boot:run
```

### 2. 测试流程
1. 访问 http://localhost:8082
2. 点击"立即登录"或访问 http://localhost:8082/dashboard
3. 自动跳转到SSO认证中心登录页
4. 登录成功后自动跳转回业务系统
5. 验证用户信息和权限显示

这个最简化实现确保了Client层开发者可以专注于业务逻辑开发，而所有认证相关的复杂性都由SSO Server处理。
