# SSO系统安全配置指南

## 🔐 Sa-Token 安全机制

### 1. 基础安全配置

```yaml
# application.yml - SSO服务端
sa-token:
  # Token名称 (同时也是cookie名称)
  token-name: satoken
  # Token有效期，单位s 默认30天, -1代表永不过期
  timeout: 2592000
  # Token临时有效期 (指定时间内无操作就视为token过期) 单位: 秒
  activity-timeout: 1800
  # 是否允许同一账号并发登录 (为true时允许一起登录, 为false时新登录挤掉旧登录)
  is-concurrent: false
  # 在多人登录同一账号时，是否共用一个token (为true时所有登录共用一个token, 为false时每次登录新建一个token)
  is-share: true
  # Token风格
  token-style: uuid
  # 是否输出操作日志
  is-log: true
  # 是否从cookie中读取token
  is-read-cookie: true
  # 是否从header中读取token
  is-read-header: true
  # 是否从body中读取token
  is-read-body: true
  
  # SSO-Server端 相关配置
  sso-server:
    # Ticket有效期 (单位: 秒)，默认五分钟
    ticket-timeout: 300
    # 所有允许的授权回调地址
    allow-url: "*"
    # 是否打开单点注销功能
    is-slo: true
    # 接口调用秘钥 (用于SSO模式三的单点注销功能)
    secretkey: "kQwIOrYvnXsVDhwOh2tPzI9at3cKs6VuOqQyHEz0c1Q"
```

### 2. 防重放攻击

```java
// 自定义拦截器防止重放攻击
@Component
public class ReplayAttackInterceptor implements HandlerInterceptor {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求时间戳和随机数
        String timestamp = request.getHeader("X-Timestamp");
        String nonce = request.getHeader("X-Nonce");
        
        if (timestamp == null || nonce == null) {
            throw new SecurityException("缺少安全头信息");
        }
        
        // 检查时间戳是否在允许范围内（5分钟）
        long requestTime = Long.parseLong(timestamp);
        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - requestTime) > 300000) {
            throw new SecurityException("请求时间戳无效");
        }
        
        // 检查nonce是否已使用
        String nonceKey = "nonce:" + nonce;
        if (redisTemplate.hasKey(nonceKey)) {
            throw new SecurityException("重复请求");
        }
        
        // 存储nonce，5分钟过期
        redisTemplate.opsForValue().set(nonceKey, "1", 300, TimeUnit.SECONDS);
        
        return true;
    }
}
```

### 3. 踢人下线机制

```java
// 设备管理服务
@Service
public class DeviceManagementService {
    
    /**
     * 踢出指定设备
     */
    public Result<Void> kickoutDevice(Long userId, Long deviceId) {
        try {
            // 1. 查询设备信息
            UserDevice device = userDeviceMapper.selectById(deviceId);
            if (device == null || !device.getUserId().equals(userId)) {
                return Result.error("设备不存在");
            }
            
            // 2. 获取该设备的所有token
            List<String> tokenList = StpUtil.searchTokenValue("", userId, -1);
            
            // 3. 踢出该设备的所有token
            for (String token : tokenList) {
                // 检查token是否属于该设备
                String tokenDevice = StpUtil.getExtra(token, "deviceId").toString();
                if (device.getDeviceId().equals(tokenDevice)) {
                    StpUtil.kickoutByTokenValue(token);
                }
            }
            
            // 4. 更新设备状态
            device.setIsActive(false);
            device.setUpdateTime(LocalDateTime.now());
            userDeviceMapper.updateById(device);
            
            return Result.success("设备已踢出");
        } catch (Exception e) {
            log.error("踢出设备失败", e);
            return Result.error("踢出设备失败：" + e.getMessage());
        }
    }
    
    /**
     * 限制同时在线设备数量
     */
    public void limitConcurrentDevices(Long userId, int maxDevices) {
        List<UserDevice> activeDevices = userDeviceMapper.selectActiveDevices(userId);
        
        if (activeDevices.size() >= maxDevices) {
            // 踢出最早登录的设备
            UserDevice oldestDevice = activeDevices.get(0);
            kickoutDevice(userId, oldestDevice.getId());
        }
    }
}
```

### 4. 临时Token机制

```java
// 临时Token服务
@Service
public class TemporaryTokenService {
    
    /**
     * 生成临时Token（用于敏感操作）
     */
    public String generateTempToken(Long userId, String operation, int timeoutSeconds) {
        // 生成临时token
        String tempToken = SaSecureUtil.md5(userId + operation + System.currentTimeMillis());
        
        // 存储到Redis，设置过期时间
        String key = "temp_token:" + tempToken;
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("userId", userId);
        tokenInfo.put("operation", operation);
        tokenInfo.put("createTime", System.currentTimeMillis());
        
        redisTemplate.opsForValue().set(key, tokenInfo, timeoutSeconds, TimeUnit.SECONDS);
        
        return tempToken;
    }
    
    /**
     * 验证并消费临时Token
     */
    public boolean verifyAndConsumeTempToken(String tempToken, Long userId, String operation) {
        String key = "temp_token:" + tempToken;
        Map<String, Object> tokenInfo = (Map<String, Object>) redisTemplate.opsForValue().get(key);
        
        if (tokenInfo == null) {
            return false;
        }
        
        // 验证用户ID和操作类型
        if (!userId.equals(tokenInfo.get("userId")) || !operation.equals(tokenInfo.get("operation"))) {
            return false;
        }
        
        // 消费token（删除）
        redisTemplate.delete(key);
        
        return true;
    }
}
```

## 🌐 HTTPS 和 CORS 配置

### 1. HTTPS 配置

```yaml
# application-prod.yml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:ssl/keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: sso-server
  # HTTP重定向到HTTPS
  http:
    port: 8080
```

```java
// HTTPS重定向配置
@Configuration
public class HttpsRedirectConfig {
    
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        
        tomcat.addAdditionalTomcatConnectors(redirectConnector());
        return tomcat;
    }
    
    private Connector redirectConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }
}
```

### 2. CORS 配置

```java
// 全局CORS配置
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的域名
        config.addAllowedOriginPattern("https://*.yourdomain.com");
        config.addAllowedOriginPattern("http://localhost:*");
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 允许的请求方法
        config.addAllowedMethod("*");
        
        // 允许携带凭证
        config.setAllowCredentials(true);
        
        // 预检请求的缓存时间
        config.setMaxAge(3600L);
        
        // 暴露的响应头
        config.addExposedHeader("satoken");
        config.addExposedHeader("X-Total-Count");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
```

### 3. CSRF 防护

```java
// CSRF Token生成和验证
@Component
public class CsrfTokenManager {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * 生成CSRF Token
     */
    public String generateCsrfToken(HttpServletRequest request) {
        String sessionId = request.getSession().getId();
        String csrfToken = SaSecureUtil.md5(sessionId + System.currentTimeMillis());
        
        // 存储到Redis，30分钟过期
        String key = "csrf_token:" + sessionId;
        redisTemplate.opsForValue().set(key, csrfToken, 30, TimeUnit.MINUTES);
        
        return csrfToken;
    }
    
    /**
     * 验证CSRF Token
     */
    public boolean verifyCsrfToken(HttpServletRequest request, String csrfToken) {
        String sessionId = request.getSession().getId();
        String key = "csrf_token:" + sessionId;
        String storedToken = redisTemplate.opsForValue().get(key);
        
        return csrfToken != null && csrfToken.equals(storedToken);
    }
}

// CSRF拦截器
@Component
public class CsrfInterceptor implements HandlerInterceptor {
    
    @Autowired
    private CsrfTokenManager csrfTokenManager;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 只对POST、PUT、DELETE请求进行CSRF验证
        String method = request.getMethod();
        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
            String csrfToken = request.getHeader("X-CSRF-Token");
            if (!csrfTokenManager.verifyCsrfToken(request, csrfToken)) {
                throw new SecurityException("CSRF Token验证失败");
            }
        }
        
        return true;
    }
}
```

## 🔒 数据加密和脱敏

### 1. 敏感数据加密

```java
// 敏感数据加密工具
@Component
public class SensitiveDataEncryption {
    
    @Value("${app.security.encrypt-key}")
    private String encryptKey;
    
    /**
     * 加密手机号
     */
    public String encryptPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return phone;
        }
        return SaSecureUtil.aesEncrypt(encryptKey, phone);
    }
    
    /**
     * 解密手机号
     */
    public String decryptPhone(String encryptedPhone) {
        if (StringUtils.isEmpty(encryptedPhone)) {
            return encryptedPhone;
        }
        return SaSecureUtil.aesDecrypt(encryptKey, encryptedPhone);
    }
    
    /**
     * 手机号脱敏显示
     */
    public String maskPhone(String phone) {
        if (StringUtils.isEmpty(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    /**
     * 邮箱脱敏显示
     */
    public String maskEmail(String email) {
        if (StringUtils.isEmpty(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return email;
        }
        
        String maskedUsername = username.substring(0, 2) + "***";
        return maskedUsername + "@" + domain;
    }
}
```

### 2. 数据库字段加密

```java
// MyBatis类型处理器 - 自动加密解密
@MappedTypes({String.class})
public class EncryptTypeHandler extends BaseTypeHandler<String> {
    
    @Autowired
    private SensitiveDataEncryption encryption;
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        // 存储时加密
        ps.setString(i, encryption.encryptPhone(parameter));
    }
    
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String encrypted = rs.getString(columnName);
        // 查询时解密
        return encryption.decryptPhone(encrypted);
    }
    
    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String encrypted = rs.getString(columnIndex);
        return encryption.decryptPhone(encrypted);
    }
    
    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String encrypted = cs.getString(columnIndex);
        return encryption.decryptPhone(encrypted);
    }
}
```

## 🚨 安全监控和告警

### 1. 异常登录检测

```java
// 异常登录检测服务
@Service
public class AbnormalLoginDetectionService {
    
    /**
     * 检测异常登录
     */
    public boolean detectAbnormalLogin(Long userId, String loginIp, String deviceFingerprint) {
        // 1. 检查IP地理位置变化
        if (isLocationChanged(userId, loginIp)) {
            return true;
        }
        
        // 2. 检查登录时间异常
        if (isLoginTimeAbnormal(userId)) {
            return true;
        }
        
        // 3. 检查设备指纹变化
        if (isDeviceChanged(userId, deviceFingerprint)) {
            return true;
        }
        
        // 4. 检查登录频率异常
        if (isLoginFrequencyAbnormal(userId)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 发送安全告警
     */
    public void sendSecurityAlert(Long userId, String alertType, String details) {
        // 1. 记录安全事件
        SecurityEvent event = new SecurityEvent();
        event.setUserId(userId);
        event.setEventType(alertType);
        event.setDetails(details);
        event.setCreateTime(LocalDateTime.now());
        securityEventMapper.insert(event);
        
        // 2. 发送邮件/短信告警
        SysUser user = sysUserService.getById(userId);
        if (user != null) {
            // 发送邮件
            if (StringUtils.hasText(user.getEmail())) {
                emailService.sendSecurityAlert(user.getEmail(), alertType, details);
            }
            
            // 发送短信
            if (StringUtils.hasText(user.getPhone())) {
                smsService.sendSecurityAlert(user.getPhone(), alertType, details);
            }
        }
        
        // 3. 推送到管理员
        notificationService.pushToAdmin("安全告警", "用户" + userId + "发生" + alertType);
    }
}
```

### 2. 安全事件记录

```java
// 安全事件实体
@Entity
@Table(name = "security_event")
public class SecurityEvent {
    private Long id;
    private Long userId;
    private String eventType; // LOGIN_ABNORMAL, PASSWORD_CHANGE, DEVICE_CHANGE
    private String details;
    private String sourceIp;
    private String userAgent;
    private LocalDateTime createTime;
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private Boolean isHandled;
    private String handleResult;
    private LocalDateTime handleTime;
}
```

这个安全配置指南涵盖了：
- 🔐 Sa-Token的完整安全配置
- 🛡️ 防重放、踢人下线、临时Token等机制
- 🌐 HTTPS和CORS的正确配置
- 🔒 数据加密和脱敏处理
- 🚨 异常检测和安全告警

通过这些配置，您的SSO系统将具备企业级的安全防护能力。
