# Server层（认证中心）实现方案

## 🎯 设计目标

构建企业级SSO认证中心，提供：
- 统一用户管理和认证服务
- 多种登录方式支持
- 完整的权限管理体系
- 高可用和高性能的认证服务
- 标准化的SSO协议支持

## 🏗️ 核心架构设计

```
SSO-Server 架构层次
┌─────────────────────────────────────────┐
│              API Gateway                │  ← 统一入口
├─────────────────────────────────────────┤
│          Controller Layer               │  ← 接口层
│  ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │   Auth  │ │   User  │ │   SSO   │   │
│  │   API   │ │   API   │ │   API   │   │
│  └─────────┘ └─────────┘ └─────────┘   │
├─────────────────────────────────────────┤
│           Service Layer                 │  ← 业务层
│  ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │  Auth   │ │  User   │ │ Permission│ │
│  │ Service │ │ Service │ │  Service  │ │
│  └─────────┘ └─────────┘ └─────────┘   │
├─────────────────────────────────────────┤
│            Data Layer                   │  ← 数据层
│  ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │  MySQL  │ │  Redis  │ │  Cache  │   │
│  │Database │ │ Session │ │ Manager │   │
│  └─────────┘ └─────────┘ └─────────┘   │
└─────────────────────────────────────────┘
```

## 📋 核心模块设计

### 1. 认证模块 (Authentication Module)

#### 职责
- 用户身份验证
- 多种登录方式支持
- Token生成和管理
- 会话管理

#### 核心接口
```java
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return authService.authenticate(request);
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }
    
    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<TokenResponse> refreshToken(@RequestParam String refreshToken) {
        return authService.refreshToken(refreshToken);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        return authService.logout(token);
    }
    
    /**
     * 验证Token
     */
    @GetMapping("/validate")
    public Result<TokenValidationResponse> validateToken(@RequestParam String token) {
        return authService.validateToken(token);
    }
}
```

### 2. SSO协议模块 (SSO Protocol Module)

#### 职责
- SSO认证流程处理
- 跨域登录支持
- 单点登出实现
- 客户端管理

#### 核心接口
```java
@RestController
@RequestMapping("/sso")
@Slf4j
public class SsoController {
    
    @Autowired
    private SsoService ssoService;
    
    /**
     * SSO认证入口
     */
    @RequestMapping("/auth")
    public Object ssoAuth(HttpServletRequest request, HttpServletResponse response) {
        return SaSsoHandle.serverRequest();
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/userinfo")
    public Result<UserInfo> getUserInfo(@RequestParam String token) {
        return ssoService.getUserInfoByToken(token);
    }
    
    /**
     * 获取用户权限
     */
    @GetMapping("/permissions")
    public Result<PermissionResponse> getUserPermissions(@RequestParam String token) {
        return ssoService.getUserPermissionsByToken(token);
    }
    
    /**
     * SSO登出
     */
    @RequestMapping("/logout")
    public Object ssoLogout(HttpServletRequest request) {
        return SaSsoHandle.serverRequest();
    }
    
    /**
     * 客户端注册
     */
    @PostMapping("/client/register")
    public Result<ClientResponse> registerClient(@RequestBody @Valid ClientRequest request) {
        return ssoService.registerClient(request);
    }
}
```

### 3. 用户管理模块 (User Management Module)

#### 职责
- 用户CRUD操作
- 用户信息维护
- 用户状态管理
- 用户分组管理

#### 核心服务
```java
@Service
@Transactional
@Slf4j
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 创建用户
     */
    public Result<User> createUser(CreateUserRequest request) {
        // 1. 验证用户名/邮箱/手机号唯一性
        if (userMapper.existsByUsername(request.getUsername())) {
            return Result.error("用户名已存在");
        }
        
        // 2. 创建用户对象
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE);
        user.setCreateTime(LocalDateTime.now());
        
        // 3. 保存到数据库
        userMapper.insert(user);
        
        // 4. 分配默认角色
        assignDefaultRole(user.getId(), request.getUserType());
        
        log.info("用户创建成功: {}", user.getUsername());
        return Result.success(user);
    }
    
    /**
     * 用户认证
     */
    public Result<User> authenticate(String account, String password, LoginType loginType) {
        // 1. 根据登录类型查找用户
        User user = findUserByAccount(account, loginType);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 2. 检查用户状态
        if (!user.isActive()) {
            return Result.error("账号已被禁用");
        }
        
        // 3. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 记录登录失败
            recordLoginFailure(user.getId());
            return Result.error("密码错误");
        }
        
        // 4. 检查账号锁定状态
        if (isAccountLocked(user.getId())) {
            return Result.error("账号已被锁定");
        }
        
        // 5. 更新登录信息
        updateLoginInfo(user.getId());
        
        return Result.success(user);
    }
    
    /**
     * 获取用户权限
     */
    @Cacheable(value = "user_permissions", key = "#userId")
    public List<String> getUserPermissions(Long userId) {
        return userMapper.selectUserPermissions(userId);
    }
    
    /**
     * 获取用户角色
     */
    @Cacheable(value = "user_roles", key = "#userId")
    public List<String> getUserRoles(Long userId) {
        return userMapper.selectUserRoles(userId);
    }
    
    // 私有方法实现...
}
```

### 4. 权限管理模块 (Permission Management Module)

#### 职责
- 角色权限分配
- 权限校验
- 动态权限管理
- 权限继承

#### 核心服务
```java
@Service
@Slf4j
public class PermissionService {
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    /**
     * 检查用户权限
     */
    public boolean hasPermission(Long userId, String permission) {
        List<String> userPermissions = getUserPermissions(userId);
        return userPermissions.contains(permission);
    }
    
    /**
     * 检查用户角色
     */
    public boolean hasRole(Long userId, String role) {
        List<String> userRoles = getUserRoles(userId);
        return userRoles.contains(role);
    }
    
    /**
     * 为角色分配权限
     */
    @Transactional
    public Result<Void> assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 1. 验证角色存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            return Result.error("角色不存在");
        }
        
        // 2. 清除现有权限
        permissionMapper.deleteRolePermissions(roleId);
        
        // 3. 分配新权限
        for (Long permissionId : permissionIds) {
            permissionMapper.insertRolePermission(roleId, permissionId);
        }
        
        // 4. 清除相关缓存
        clearPermissionCache(roleId);
        
        return Result.success();
    }
    
    /**
     * 获取权限树
     */
    public Result<List<PermissionNode>> getPermissionTree() {
        List<Permission> allPermissions = permissionMapper.selectAll();
        List<PermissionNode> tree = buildPermissionTree(allPermissions);
        return Result.success(tree);
    }
}
```

### 5. Token管理模块 (Token Management Module)

#### 职责
- JWT Token生成
- Token验证和解析
- Token刷新机制
- Token黑名单管理

#### 核心服务
```java
@Service
@Slf4j
public class TokenService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 生成访问Token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("userType", user.getUserType());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    
    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();
        String key = "refresh_token:" + refreshToken;
        
        // 存储到Redis，有效期30天
        redisTemplate.opsForValue().set(key, user.getId(), 30, TimeUnit.DAYS);
        
        return refreshToken;
    }
    
    /**
     * 验证Token
     */
    public TokenValidationResult validateToken(String token) {
        try {
            // 1. 检查黑名单
            if (isTokenBlacklisted(token)) {
                return TokenValidationResult.invalid("Token已被撤销");
            }
            
            // 2. 解析Token
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
            
            // 3. 检查过期时间
            if (claims.getExpiration().before(new Date())) {
                return TokenValidationResult.expired();
            }
            
            // 4. 构建验证结果
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();
            
            return TokenValidationResult.valid(userId, username);
            
        } catch (JwtException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return TokenValidationResult.invalid("Token格式错误");
        }
    }
    
    /**
     * 刷新Token
     */
    public Result<TokenResponse> refreshToken(String refreshToken) {
        String key = "refresh_token:" + refreshToken;
        Long userId = (Long) redisTemplate.opsForValue().get(key);
        
        if (userId == null) {
            return Result.error("刷新Token无效或已过期");
        }
        
        // 获取用户信息
        User user = userService.getUserById(userId);
        if (user == null || !user.isActive()) {
            return Result.error("用户不存在或已被禁用");
        }
        
        // 生成新的Token
        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);
        
        // 删除旧的刷新Token
        redisTemplate.delete(key);
        
        TokenResponse response = new TokenResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(jwtExpiration / 1000);
        
        return Result.success(response);
    }
    
    /**
     * 撤销Token
     */
    public void revokeToken(String token) {
        String key = "blacklist_token:" + DigestUtils.md5Hex(token);
        redisTemplate.opsForValue().set(key, true, jwtExpiration, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 检查Token是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        String key = "blacklist_token:" + DigestUtils.md5Hex(token);
        return redisTemplate.hasKey(key);
    }
}
```

## 🔧 配置和部署

### 1. 核心配置
```yaml
# application.yml
server:
  port: 8081

spring:
  application:
    name: sso-server
  
  datasource:
    url: jdbc:mysql://localhost:3306/sso_db?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: ${DB_PASSWORD:your_password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}
      database: 0

# JWT配置
jwt:
  secret: ${JWT_SECRET:mySecretKey}
  expiration: 7200000  # 2小时

# Sa-Token配置
sa-token:
  token-name: Authorization
  timeout: 7200
  is-concurrent: true
  is-share: false
  sso:
    is-sso: true
    auth-url: /sso/auth
    is-http: true
    secretkey: ${SSO_SECRET_KEY:ssoSecretKey}

# 第三方登录配置
third-party:
  wechat:
    app-id: ${WECHAT_APP_ID:}
    app-secret: ${WECHAT_APP_SECRET:}
  alipay:
    app-id: ${ALIPAY_APP_ID:}
    private-key: ${ALIPAY_PRIVATE_KEY:}

# 安全配置
security:
  password:
    min-length: 6
    max-length: 20
  login:
    max-retry: 5
    lock-time: 30  # 分钟
```

### 2. 启动类配置
```java
@SpringBootApplication
@EnableScheduling
@EnableCaching
@MapperScan("com.example.sso.mapper")
public class SsoServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SsoServerApplication.class, args);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        // 设置序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        return template;
    }
}
```

## 🚀 部署和监控

### 1. Docker部署
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/sso-server.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. 监控配置
```yaml
# 监控端点
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

# 日志配置
logging:
  level:
    com.example.sso: DEBUG
    org.springframework.security: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/sso-server.log
```

这个Server层实现方案提供了完整的企业级SSO认证中心功能，确保了高可用性、安全性和可扩展性，同时为Client层提供了标准化的认证服务接口。
