# SSO Common 层使用说明

## 📋 概述

SSO Common 层是一个纯粹的依赖库模块，提供了 SSO 系统的公共组件，包括实体类、数据访问层、工具类等。该模块不包含任何应用配置，需要在使用它的模块中进行相应配置。

## 🏗️ 模块结构

```
sso-common/
├── src/main/java/org/example/common/
│   ├── entity/              # 实体类
│   │   ├── SysUser.java
│   │   ├── SysRole.java
│   │   ├── SysMenu.java
│   │   ├── SysLoginLog.java
│   │   ├── UserDevice.java
│   │   ├── UserOauthBinding.java
│   │   ├── VerificationCode.java
│   │   ├── UserPasswordHistory.java
│   │   ├── SysUserRole.java
│   │   └── SysRoleMenu.java
│   ├── mapper/              # MyBatis Mapper 接口
│   │   ├── SysUserMapper.java
│   │   ├── SysRoleMapper.java
│   │   ├── SysMenuMapper.java
│   │   ├── SysLoginLogMapper.java
│   │   ├── UserDeviceMapper.java
│   │   ├── UserOauthBindingMapper.java
│   │   ├── VerificationCodeMapper.java
│   │   ├── UserPasswordHistoryMapper.java
│   │   ├── SysUserRoleMapper.java
│   │   └── SysRoleMenuMapper.java
│   ├── dto/                 # 数据传输对象
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   └── RegisterRequest.java
│   ├── result/              # 统一响应结果
│   │   ├── Result.java
│   │   ├── PageResult.java
│   │   └── ResultCode.java
│   ├── util/                # 工具类
│   │   ├── DeviceUtil.java
│   │   └── EncryptUtil.java
│   └── annotation/          # 自定义注解
│       ├── RequirePermission.java
│       └── RequireRole.java
└── pom.xml                  # Maven 配置（无启动插件）
```

## 🔧 在其他模块中使用

### 1. 添加依赖

在需要使用 Common 层的模块的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>sso-common</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 配置 MyBatis 扫描

在使用模块的主启动类或配置类中添加 Mapper 扫描：

```java
@SpringBootApplication
@MapperScan("org.example.common.mapper")
public class SsoServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SsoServerApplication.class, args);
    }
}
```

或者使用配置类：

```java
@Configuration
@MapperScan("org.example.common.mapper")
public class MyBatisConfig {
    // MyBatis 相关配置
}
```

### 3. 数据库配置

在使用模块的 `application.yml` 中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sso_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    
mybatis:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  type-aliases-package: org.example.common.entity
```

### 4. 使用示例

#### 在 Service 中使用 Mapper：

```java
@Service
public class UserService {
    
    @Autowired
    private SysUserMapper sysUserMapper;
    
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    
    public SysUser getUserById(Long id) {
        return sysUserMapper.selectById(id);
    }
    
    public void assignRole(Long userId, Long roleId) {
        SysUserRole userRole = new SysUserRole(userId, roleId);
        sysUserRoleMapper.insert(userRole);
    }
}
```

#### 使用工具类：

```java
@Service
public class LoginService {
    
    public void recordLogin(String userAgent, String ip) {
        // 使用设备工具类解析设备信息
        Map<String, String> deviceInfo = DeviceUtil.parseUserAgent(userAgent);
        String deviceFingerprint = DeviceUtil.generateDeviceFingerprint(userAgent, ip);
        
        // 使用加密工具类
        String maskedPhone = EncryptUtil.maskPhone("13812345678");
    }
}
```

#### 使用统一响应结果：

```java
@RestController
public class UserController {
    
    @GetMapping("/user/{id}")
    public Result<SysUser> getUser(@PathVariable Long id) {
        SysUser user = userService.getUserById(id);
        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error("用户不存在");
        }
    }
}
```

## ⚠️ 注意事项

### 1. 不要在 Common 层中：
- ❌ 添加 `application.yml` 配置文件
- ❌ 添加 Spring Boot 启动类
- ❌ 添加具体的业务逻辑
- ❌ 添加 Controller 层代码
- ❌ 添加 Service 实现类

### 2. Common 层应该包含：
- ✅ 实体类 (Entity)
- ✅ 数据访问接口 (Mapper)
- ✅ 数据传输对象 (DTO)
- ✅ 工具类 (Util)
- ✅ 自定义注解 (Annotation)
- ✅ 统一响应结果类
- ✅ 常量定义

### 3. 版本管理：
- Common 层的版本变更需要同步更新所有依赖模块
- 建议使用语义化版本控制
- 重大变更需要向后兼容或提供迁移指南

## 🔄 依赖关系

```
sso-server     ──┐
                 ├──► sso-common
sso-client     ──┘
```

- `sso-server` 依赖 `sso-common`
- `sso-client-backend` 依赖 `sso-common`
- `sso-common` 不依赖任何业务模块

## 📦 打包说明

Common 层会被打包成 JAR 文件，供其他模块依赖使用：

```bash
# 编译并安装到本地仓库
mvn clean install

# 只编译
mvn clean compile
```

## 🚀 最佳实践

1. **保持纯净性**：Common 层应该只包含公共组件，不包含业务逻辑
2. **接口优先**：定义清晰的接口，便于不同模块实现
3. **文档完善**：为公共组件提供详细的文档和使用示例
4. **版本控制**：谨慎管理版本变更，确保向后兼容
5. **测试覆盖**：为工具类和核心组件提供充分的单元测试

---

**更新时间**：2025-08-20  
**适用版本**：sso-common 0.0.1-SNAPSHOT
