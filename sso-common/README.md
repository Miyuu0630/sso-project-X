# SSO Common Module

SSO 系统的公共依赖库，提供通用的实体类、数据访问层、工具类等。该模块不包含任何应用配置，作为纯粹的依赖库供其他模块使用。

## 📁 模块结构

```
sso-common/
├── src/main/java/org/example/common/
│   ├── entity/              # 实体类
│   │   ├── SysUser.java            # 用户实体
│   │   ├── SysRole.java            # 角色实体
│   │   ├── SysMenu.java            # 菜单实体
│   │   ├── SysLoginLog.java        # 登录日志实体
│   │   ├── UserDevice.java         # 用户设备实体
│   │   ├── UserOauthBinding.java   # 第三方绑定实体
│   │   ├── VerificationCode.java   # 验证码实体
│   │   ├── UserPasswordHistory.java # 密码历史实体
│   │   ├── SysUserRole.java        # 用户角色关联实体
│   │   └── SysRoleMenu.java        # 角色菜单关联实体
│   ├── mapper/             # MyBatis Mapper接口
│   │   ├── SysUserMapper.java      # 用户数据访问
│   │   ├── SysRoleMapper.java      # 角色数据访问
│   │   ├── SysMenuMapper.java      # 菜单数据访问
│   │   ├── SysLoginLogMapper.java  # 登录日志数据访问
│   │   ├── UserDeviceMapper.java   # 用户设备数据访问
│   │   ├── UserOauthBindingMapper.java # 第三方绑定数据访问
│   │   ├── VerificationCodeMapper.java # 验证码数据访问
│   │   ├── UserPasswordHistoryMapper.java # 密码历史数据访问
│   │   ├── SysUserRoleMapper.java  # 用户角色关联数据访问
│   │   └── SysRoleMenuMapper.java  # 角色菜单关联数据访问
│   ├── dto/                # 数据传输对象
│   │   ├── LoginRequest.java       # 登录请求DTO
│   │   ├── LoginResponse.java      # 登录响应DTO
│   │   └── RegisterRequest.java    # 注册请求DTO
│   ├── result/             # 统一响应结果
│   │   ├── Result.java             # 统一响应结果
│   │   ├── PageResult.java         # 分页响应结果
│   │   └── ResultCode.java         # 响应码枚举
│   ├── util/               # 工具类
│   │   ├── DeviceUtil.java         # 设备信息工具
│   │   └── EncryptUtil.java        # 加密工具
│   └── annotation/         # 自定义注解
│       ├── RequirePermission.java  # 权限校验注解
│       └── RequireRole.java        # 角色校验注解
└── pom.xml                 # Maven配置（纯依赖库）
```

## 🔧 主要功能

### 1. 实体类 (Entity)
- **用户管理**：SysUser、SysRole、SysMenu
- **关联关系**：SysUserRole、SysRoleMenu
- **日志记录**：SysLoginLog
- **设备管理**：UserDevice
- **第三方集成**：UserOauthBinding
- **安全功能**：VerificationCode、UserPasswordHistory

### 2. 数据访问层 (Mapper)
- **完整的 CRUD 操作**：支持所有实体的增删改查
- **复杂查询**：分页查询、条件查询、关联查询
- **批量操作**：批量插入、批量更新
- **统计功能**：数据统计、计数查询

### 3. 工具类 (Utils)
- **设备工具**：DeviceUtil - 解析 User-Agent，生成设备指纹
- **加密工具**：EncryptUtil - AES 加密解密，数据脱敏

### 4. 统一响应 (Result)
- **统一响应**：Result<T> - 标准化 API 响应格式
- **分页响应**：PageResult<T> - 分页数据响应
- **响应码**：ResultCode - 统一的响应状态码

### 5. 数据传输对象 (DTO)
- **登录相关**：LoginRequest、LoginResponse
- **注册相关**：RegisterRequest

### 6. 自定义注解 (Annotation)
- **权限控制**：RequirePermission、RequireRole

## 📦 依赖说明

此模块作为纯依赖库，被其他模块引用：

```
sso-server     ──┐
                 ├──► sso-common
sso-client     ──┘
```

## 🚀 使用方式

### 1. 添加依赖

在其他模块的 pom.xml 中添加依赖：

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>sso-common</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 配置扫描

在主启动类添加 Mapper 扫描：

```java
@SpringBootApplication
@MapperScan("org.example.common.mapper")
public class SsoServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SsoServerApplication.class, args);
    }
}
```

## ⚠️ 重要说明

### Common 层特点：
- ✅ **纯依赖库**：不包含任何应用配置
- ✅ **无启动类**：不能独立运行
- ✅ **无配置文件**：不包含 application.yml
- ✅ **接口定义**：只定义接口和数据结构

### 使用注意事项：
- 🚫 不要在 Common 层添加业务逻辑
- 🚫 不要在 Common 层添加 Controller
- 🚫 不要在 Common 层添加 Service 实现
- ✅ 只包含公共的数据结构和接口定义

## 📝 开发规范

1. **实体类**：必须与数据库表结构完全一致
2. **Mapper接口**：使用 MyBatis 注解方式，包含完整字段
3. **工具类**：方法必须是静态方法，无状态设计
4. **命名规范**：遵循 Java 命名约定
5. **文档完善**：为所有公共接口提供详细注释

---

**详细使用说明请参考**: [COMMON_LAYER_USAGE.md](COMMON_LAYER_USAGE.md)
