# SSO 系统 MD5+盐值密码加密完整实现方案

## 📋 概述

本文档提供了从 BCrypt 平滑迁移到 MD5+随机盐值密码加密方案的完整实现代码和配置。

## 🔐 密码加密方案

### 算法说明
- **加密算法**: MD5(password + salt)
- **盐值生成**: 32位随机字符串
- **存储方式**: 密码哈希值(32位) + 盐值(32位) 分别存储
- **拼接顺序**: password + salt（必须保持一致）

### 安全特性
1. **随机盐值**: 每个用户都有唯一的32位随机盐值
2. **防彩虹表**: 盐值有效防止彩虹表攻击
3. **兼容性**: 支持旧密码格式的平滑迁移

## 🛠️ 核心实现代码

### 1. 密码服务实现

```java
@Service
@Slf4j
public class PasswordServiceImpl implements PasswordService {
    
    /**
     * 生成32位随机盐值
     */
    @Override
    public String generateSalt() {
        return RandomUtil.randomString(32);
    }
    
    /**
     * 使用MD5+盐值加密密码
     * @param rawPassword 原始密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public String encodePasswordWithSalt(String rawPassword, String salt) {
        if (StrUtil.isBlank(rawPassword)) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (StrUtil.isBlank(salt)) {
            throw new IllegalArgumentException("盐值不能为空");
        }

        try {
            // MD5(密码 + 盐值)
            String passwordWithSalt = rawPassword + salt;
            return DigestUtil.md5Hex(passwordWithSalt);
        } catch (Exception e) {
            log.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 验证密码（MD5+盐值方式）
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @param salt 盐值
     * @return 是否匹配
     */
    public boolean matchesWithSalt(String rawPassword, String encodedPassword, String salt) {
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(encodedPassword) || StrUtil.isBlank(salt)) {
            return false;
        }
        
        try {
            String hashedInput = encodePasswordWithSalt(rawPassword, salt);
            return hashedInput.equals(encodedPassword);
        } catch (Exception e) {
            log.error("密码验证失败", e);
            return false;
        }
    }
    
    /**
     * 兼容旧的BCrypt密码验证
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(encodedPassword)) {
            return false;
        }
        
        try {
            // 检查是否为BCrypt格式
            if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$")) {
                return BCrypt.checkpw(rawPassword, encodedPassword);
            }
            
            // 检查是否为明文密码（兼容模式）
            if (encodedPassword.equals(rawPassword)) {
                log.warn("检测到明文密码，建议升级到MD5+盐值方案");
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("密码验证异常", e);
            return false;
        }
    }
}
```

### 2. 用户注册实现

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class UserRegisterServiceImpl implements UserRegisterService {
    
    private final PasswordService passwordService;
    private final SysUserMapper sysUserMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest registerRequest) {
        try {
            // 1. 验证注册数据
            validateRegisterData(registerRequest);

            // 2. 创建用户实体
            SysUser user = buildUserFromRequest(registerRequest);

            // 3. 保存用户
            int result = sysUserMapper.insert(user);
            if (result <= 0) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户注册失败");
            }

            // 4. 分配默认角色
            assignDefaultRole(user.getId(), registerRequest.getUserType());

            // 5. 构建响应
            RegisterResponse response = buildRegisterResponse(user);

            log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
            return response;

        } catch (BusinessException e) {
            log.warn("用户注册失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("用户注册异常: username={}", registerRequest.getUsername(), e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "注册失败，请稍后重试");
        }
    }
    
    /**
     * 从注册请求构建用户实体
     */
    private SysUser buildUserFromRequest(RegisterRequest registerRequest) {
        // 生成随机盐值
        String salt = passwordService.generateSalt();
        
        // 使用MD5+盐值加密密码
        String encryptedPassword = passwordService.encodePasswordWithSalt(registerRequest.getPassword(), salt);
        
        SysUser user = new SysUser();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(encryptedPassword);
        user.setSalt(salt);  // 设置盐值
        user.setRealName(registerRequest.getRealName());
        user.setPhone(registerRequest.getPhone());
        user.setEmail(registerRequest.getEmail());
        user.setUserType(registerRequest.getUserType());
        user.setStatus("1");
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setCreateTime(LocalDateTime.now());
        
        return user;
    }
}
```

### 3. 登录验证实现

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {
    
    private final PasswordService passwordService;
    private final SysUserMapper userMapper;
    
    @Override
    public SysUser validateUser(String account, String password) {
        try {
            // 根据账号查询用户
            SysUser user = getUserByAccount(account);
            if (user == null) {
                return null;
            }
            
            // 验证密码 - 使用MD5+盐值验证
            if (user.getSalt() != null && !user.getSalt().isEmpty()) {
                // 使用新的MD5+盐值验证方式
                if (passwordService.matchesWithSalt(password, user.getPassword(), user.getSalt())) {
                    return user;
                }
            } else {
                // 兼容旧的密码验证方式（如果用户没有盐值）
                log.warn("用户 {} 没有盐值，使用兼容模式验证密码", account);
                if (passwordService.matches(password, user.getPassword())) {
                    // 验证成功后，为用户生成盐值并重新加密密码
                    updateUserPasswordWithSalt(user.getId(), password);
                    return user;
                }
            }

            // 记录登录失败
            recordLoginFailure(user.getId());
            return null;
        } catch (Exception e) {
            log.error("验证用户失败: account={}", account, e);
            return null;
        }
    }
    
    /**
     * 为用户更新密码并生成盐值（用于兼容性升级）
     */
    private void updateUserPasswordWithSalt(Long userId, String rawPassword) {
        try {
            String newSalt = passwordService.generateSalt();
            String encryptedPassword = passwordService.encodePasswordWithSalt(rawPassword, newSalt);
            
            SysUser updateUser = new SysUser();
            updateUser.setId(userId);
            updateUser.setPassword(encryptedPassword);
            updateUser.setSalt(newSalt);
            updateUser.setPasswordUpdateTime(LocalDateTime.now());
            
            userMapper.updateById(updateUser);
            log.info("用户密码已升级到MD5+盐值方案: userId={}", userId);
        } catch (Exception e) {
            log.error("升级用户密码失败: userId={}", userId, e);
        }
    }
}
```

## 🔄 前端模板迁移方案

### 1. 模板文件迁移

将模板文件从根目录 `templates/` 迁移到 `sso-server/src/main/resources/templates/`：

```bash
# 创建目标目录
mkdir -p sso-server/src/main/resources/templates

# 迁移模板文件
mv templates/login.html sso-server/src/main/resources/templates/
mv templates/register.html sso-server/src/main/resources/templates/
```

### 2. Spring Boot 配置

在 `sso-server/src/main/resources/application.yml` 中配置模板引擎：

```yaml
spring:
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    mode: HTML
    encoding: UTF-8
    cache: false  # 开发环境关闭缓存
    servlet:
      content-type: text/html
```

### 3. 控制器配置

```java
@Controller
@RequestMapping("/sso")
public class SsoPageController {
    
    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        // 获取重定向URL
        String redirectUrl = request.getParameter("redirect_url");
        if (StrUtil.isNotBlank(redirectUrl)) {
            model.addAttribute("redirectUrl", redirectUrl);
        }
        
        return "login";  // 返回 templates/login.html
    }
    
    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        return "register";  // 返回 templates/register.html
    }
}
```

## ⚠️ 重要注意事项

### 1. 密码拼接顺序
```java
// 正确的拼接顺序：password + salt
String passwordWithSalt = rawPassword + salt;
String hash = DigestUtil.md5Hex(passwordWithSalt);
```

### 2. 数据库字段要求
```sql
-- 确保数据库字段长度正确
password VARCHAR(32) NOT NULL COMMENT '密码（MD5+盐值加密，32位MD5哈希）',
salt VARCHAR(32) NOT NULL COMMENT '密码盐值，32位随机字符串'
```

### 3. 兼容性处理
- 支持旧密码格式的验证
- 登录成功后自动升级到新格式
- 保持向后兼容性

### 4. 安全建议
- 生产环境立即修改默认密码
- 定期检查密码强度
- 监控异常登录行为
- 考虑升级到更强的加密算法（如 SHA-256 + PBKDF2）

## 🧪 测试验证

### 1. 密码加密测试
```java
@Test
public void testPasswordEncryption() {
    String password = "admin123456";
    String salt = "a1b2c3d4e5f6789012345678901234ab";
    String expected = "c091d2cf4a0c12813f546fa11739ea40";
    
    String actual = passwordService.encodePasswordWithSalt(password, salt);
    assertEquals(expected, actual);
}
```

### 2. 登录验证测试
```java
@Test
public void testPasswordValidation() {
    String password = "admin123456";
    String salt = "a1b2c3d4e5f6789012345678901234ab";
    String hash = "c091d2cf4a0c12813f546fa11739ea40";
    
    boolean isValid = passwordService.matchesWithSalt(password, hash, salt);
    assertTrue(isValid);
}
```
