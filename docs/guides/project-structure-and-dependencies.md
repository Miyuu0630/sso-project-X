# 项目结构和依赖配置指南

## 📁 完整项目目录结构

```
sso-project-X/
│
├── 📚 docs/                                    # 项目文档
│   ├── architecture/                           # 架构设计文档
│   │   ├── architecture-design.md
│   │   └── sso-interaction-specification.md
│   ├── analysis/                               # 分析报告
│   │   ├── sso-client-analysis-report.md
│   │   ├── check-project.md
│   │   └── feature-comparison.md
│   ├── guides/                                 # 实施指南
│   │   ├── SSO_INTEGRATION_GUIDE.md
│   │   ├── server-implementation-guide.md
│   │   ├── client-minimal-implementation.md
│   │   ├── sso-feature-upgrade-plan.md
│   │   ├── sso-troubleshooting.md
│   │   ├── sso-flow-implementation.md
│   │   ├── security-configuration.md
│   │   ├── third-party-integration.md
│   │   └── project-structure-and-dependencies.md
│   └── deployment/                             # 部署文档
│       ├── deploy.md
│       └── git-setup.md
│
├── 🗄️ db/                                      # 数据库脚本
│   ├── sso-server/                             # 服务端数据库
│   │   ├── sso_server_create_tables.sql
│   │   ├── sso_server_init_data.sql
│   │   └── sso_complete_database_design.sql
│   └── sso-client/                             # 客户端数据库
│       └── README.md
│
├── 🔐 sso-server/                              # SSO认证中心 (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/org/example/ssoserver/
│   │   │   │   ├── SsoServerApplication.java   # 启动类
│   │   │   │   ├── config/                     # 配置类
│   │   │   │   │   ├── SaTokenConfig.java
│   │   │   │   │   ├── CorsConfig.java
│   │   │   │   │   ├── RedisConfig.java
│   │   │   │   │   └── MyBatisConfig.java
│   │   │   │   ├── controller/                 # 控制器
│   │   │   │   │   ├── SsoServerController.java
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── UserController.java
│   │   │   │   │   └── oauth/
│   │   │   │   │       ├── WechatOAuthController.java
│   │   │   │   │       ├── AlipayOAuthController.java
│   │   │   │   │       └── GithubOAuthController.java
│   │   │   │   ├── service/                    # 服务层
│   │   │   │   │   ├── SysUserService.java
│   │   │   │   │   ├── LoginLogService.java
│   │   │   │   │   ├── DeviceManagementService.java
│   │   │   │   │   ├── OAuthService.java
│   │   │   │   │   ├── UserOAuthBindingService.java
│   │   │   │   │   └── impl/
│   │   │   │   │       ├── SysUserServiceImpl.java
│   │   │   │   │       ├── LoginLogServiceImpl.java
│   │   │   │   │       └── DeviceManagementServiceImpl.java
│   │   │   │   ├── mapper/                     # MyBatis映射器
│   │   │   │   │   ├── SysUserMapper.java
│   │   │   │   │   ├── SysRoleMapper.java
│   │   │   │   │   ├── SysMenuMapper.java
│   │   │   │   │   ├── LoginLogMapper.java
│   │   │   │   │   ├── UserDeviceMapper.java
│   │   │   │   │   └── UserOAuthBindingMapper.java
│   │   │   │   ├── entity/                     # 实体类
│   │   │   │   │   ├── SysUser.java
│   │   │   │   │   ├── SysRole.java
│   │   │   │   │   ├── SysMenu.java
│   │   │   │   │   ├── SysLoginLog.java
│   │   │   │   │   ├── UserDevice.java
│   │   │   │   │   └── UserOAuthBinding.java
│   │   │   │   ├── dto/                        # 数据传输对象
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── LoginResponse.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   ├── WechatUserInfo.java
│   │   │   │   │   └── AlipayUserInfo.java
│   │   │   │   ├── common/                     # 公共类
│   │   │   │   │   ├── Result.java
│   │   │   │   │   ├── PageResult.java
│   │   │   │   │   └── Constants.java
│   │   │   │   ├── exception/                  # 异常处理
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   └── BusinessException.java
│   │   │   │   └── util/                       # 工具类
│   │   │   │       ├── IpUtils.java
│   │   │   │       ├── DeviceUtils.java
│   │   │   │       └── EncryptionUtils.java
│   │   │   └── resources/
│   │   │       ├── application.yml             # 主配置文件
│   │   │       ├── application-dev.yml         # 开发环境配置
│   │   │       ├── application-prod.yml        # 生产环境配置
│   │   │       ├── mapper/                     # MyBatis映射文件
│   │   │       │   ├── SysUserMapper.xml
│   │   │       │   ├── SysRoleMapper.xml
│   │   │       │   ├── SysMenuMapper.xml
│   │   │       │   ├── LoginLogMapper.xml
│   │   │       │   ├── UserDeviceMapper.xml
│   │   │       │   └── UserOAuthBindingMapper.xml
│   │   │       └── static/                     # 静态资源
│   │   └── test/                               # 测试代码
│   │       └── java/org/example/ssoserver/
│   │           ├── SsoServerApplicationTests.java
│   │           ├── service/
│   │           └── controller/
│   ├── pom.xml                                 # Maven依赖配置
│   ├── README.md                               # 项目说明
│   └── Dockerfile                              # Docker配置
│
├── 🔧 sso-client-backend/                      # SSO客户端后端 (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/org/example/ssoclient/
│   │   │   │   ├── SsoClientApplication.java   # 启动类
│   │   │   │   ├── config/                     # 配置类
│   │   │   │   │   ├── SaTokenConfig.java
│   │   │   │   │   └── CorsConfig.java
│   │   │   │   ├── controller/                 # 控制器
│   │   │   │   │   ├── SsoClientController.java
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   └── BusinessController.java
│   │   │   │   ├── service/                    # 服务层
│   │   │   │   │   ├── UserInfoService.java
│   │   │   │   │   └── BusinessService.java
│   │   │   │   ├── entity/                     # 实体类
│   │   │   │   │   └── BusinessEntity.java
│   │   │   │   ├── dto/                        # 数据传输对象
│   │   │   │   │   └── UserInfoDto.java
│   │   │   │   └── util/                       # 工具类
│   │   │   │       └── HttpUtils.java
│   │   │   └── resources/
│   │   │       ├── application.yml             # 配置文件
│   │   │       └── application-dev.yml
│   │   └── test/
│   ├── pom.xml
│   ├── README.md
│   └── Dockerfile
│
├── 🎨 sso-client-frontend/                     # SSO客户端前端 (Vue3 + Vite)
│   ├── src/
│   │   ├── main.js                             # 入口文件
│   │   ├── App.vue                             # 根组件
│   │   ├── components/                         # 公共组件
│   │   │   ├── Layout/
│   │   │   │   ├── Header.vue
│   │   │   │   ├── Sidebar.vue
│   │   │   │   └── Footer.vue
│   │   │   ├── Auth/
│   │   │   │   ├── LoginForm.vue
│   │   │   │   ├── RegisterForm.vue
│   │   │   │   └── ThirdPartyLogin.vue
│   │   │   └── Common/
│   │   │       ├── Loading.vue
│   │   │       └── ErrorBoundary.vue
│   │   ├── views/                              # 页面组件
│   │   │   ├── Home.vue
│   │   │   ├── Login.vue
│   │   │   ├── Register.vue
│   │   │   ├── Callback.vue
│   │   │   ├── Dashboard.vue
│   │   │   ├── Profile.vue
│   │   │   ├── Users.vue
│   │   │   ├── SsoTest.vue
│   │   │   └── user/
│   │   │       ├── AccountBinding.vue
│   │   │       ├── DeviceManagement.vue
│   │   │       └── LoginHistory.vue
│   │   ├── router/                             # 路由配置
│   │   │   └── index.js
│   │   ├── stores/                             # Pinia状态管理
│   │   │   ├── auth.js
│   │   │   ├── user.js
│   │   │   └── app.js
│   │   ├── utils/                              # 工具函数
│   │   │   ├── request.js                      # Axios配置
│   │   │   ├── auth.js                         # 认证工具
│   │   │   ├── storage.js                      # 存储工具
│   │   │   └── device.js                       # 设备工具
│   │   ├── config/                             # 配置文件
│   │   │   ├── sso.js                          # SSO配置
│   │   │   └── constants.js                    # 常量配置
│   │   ├── assets/                             # 静态资源
│   │   │   ├── styles/
│   │   │   │   ├── main.css
│   │   │   │   └── variables.css
│   │   │   └── images/
│   │   └── plugins/                            # 插件配置
│   │       └── element-plus.js
│   ├── public/                                 # 公共资源
│   │   ├── index.html
│   │   └── favicon.ico
│   ├── package.json                            # 依赖配置
│   ├── vite.config.js                          # Vite配置
│   ├── README.md
│   └── Dockerfile
│
├── 📄 templates/                               # 模板文件
│   ├── login.html
│   ├── register.html
│   ├── vue-auth-store.js
│   ├── vue-login-page.vue
│   ├── vue-router-config.js
│   ├── server-auth-controller-design.java
│   └── sso-server-controller-enhanced.java
│
├── 📝 logs/                                    # 日志文件
│   ├── sso-server.log
│   └── sso-client.log
│
├── 🐳 docker/                                  # Docker配置
│   ├── docker-compose.yml
│   ├── nginx/
│   │   └── nginx.conf
│   └── mysql/
│       └── init.sql
│
├── 📋 README.md                                # 项目总说明
├── .gitignore                                  # Git忽略规则
└── LICENSE                                     # 许可证
```

## 📦 依赖配置

### 1. SSO服务端依赖 (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>org.example</groupId>
    <artifactId>sso-server</artifactId>
    <version>1.0.0</version>
    <name>sso-server</name>
    <description>SSO认证中心</description>
    
    <properties>
        <java.version>17</java.version>
        <sa-token.version>1.37.0</sa-token.version>
        <mybatis-plus.version>3.5.4.1</mybatis-plus.version>
        <hutool.version>5.8.22</hutool.version>
        <alipay-sdk.version>4.38.10.ALL</alipay-sdk.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        
        <!-- Sa-Token 权限认证 -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-spring-boot3-starter</artifactId>
            <version>${sa-token.version}</version>
        </dependency>
        
        <!-- Sa-Token 整合 Redis -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-redis-jackson</artifactId>
            <version>${sa-token.version}</version>
        </dependency>
        
        <!-- Sa-Token SSO -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-sso</artifactId>
            <version>${sa-token.version}</version>
        </dependency>
        
        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        
        <!-- MySQL驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- 连接池 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-3-starter</artifactId>
            <version>1.2.20</version>
        </dependency>
        
        <!-- 工具类 -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>${hutool.version}</version>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- 支付宝SDK -->
        <dependency>
            <groupId>com.alipay.sdk</groupId>
            <artifactId>alipay-sdk-java</artifactId>
            <version>${alipay-sdk.version}</version>
        </dependency>
        
        <!-- 微信SDK -->
        <dependency>
            <groupId>com.github.binarywang</groupId>
            <artifactId>weixin-java-mp</artifactId>
            <version>4.5.0</version>
        </dependency>
        
        <!-- 测试依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2. 客户端前端依赖 (package.json)

```json
{
  "name": "sso-client-frontend",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext vue,js,jsx,cjs,mjs --fix --ignore-path .gitignore",
    "format": "prettier --write src/"
  },
  "dependencies": {
    "vue": "^3.3.8",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.7",
    "element-plus": "^2.4.4",
    "axios": "^1.6.2",
    "js-cookie": "^3.0.5",
    "@element-plus/icons-vue": "^2.1.0",
    "nprogress": "^0.2.0",
    "dayjs": "^1.11.10"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.5.0",
    "vite": "^5.0.0",
    "eslint": "^8.54.0",
    "eslint-plugin-vue": "^9.18.1",
    "prettier": "^3.1.0",
    "sass": "^1.69.5",
    "unplugin-auto-import": "^0.17.2",
    "unplugin-vue-components": "^0.25.2"
  }
}
```

### 3. Docker Compose 配置

```yaml
# docker-compose.yml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: sso-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123456
      MYSQL_DATABASE: sso_db
      MYSQL_USER: sso_user
      MYSQL_PASSWORD: sso_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./db/sso-server:/docker-entrypoint-initdb.d
    networks:
      - sso-network

  # Redis缓存
  redis:
    image: redis:7-alpine
    container_name: sso-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - sso-network

  # SSO服务端
  sso-server:
    build:
      context: ./sso-server
      dockerfile: Dockerfile
    container_name: sso-server
    ports:
      - "8081:8081"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      MYSQL_HOST: mysql
      REDIS_HOST: redis
    depends_on:
      - mysql
      - redis
    networks:
      - sso-network

  # SSO客户端后端
  sso-client-backend:
    build:
      context: ./sso-client-backend
      dockerfile: Dockerfile
    container_name: sso-client-backend
    ports:
      - "8082:8082"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SSO_SERVER_URL: http://sso-server:8081
      REDIS_HOST: redis
    depends_on:
      - sso-server
      - redis
    networks:
      - sso-network

  # SSO客户端前端
  sso-client-frontend:
    build:
      context: ./sso-client-frontend
      dockerfile: Dockerfile
    container_name: sso-client-frontend
    ports:
      - "5173:80"
    depends_on:
      - sso-client-backend
    networks:
      - sso-network

  # Nginx反向代理
  nginx:
    image: nginx:alpine
    container_name: sso-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./docker/nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./docker/nginx/ssl:/etc/nginx/ssl
    depends_on:
      - sso-server
      - sso-client-backend
      - sso-client-frontend
    networks:
      - sso-network

volumes:
  mysql_data:
  redis_data:

networks:
  sso-network:
    driver: bridge
```

## 🚀 快速启动指南

### 1. 开发环境启动

```bash
# 1. 启动数据库和Redis
docker-compose up -d mysql redis

# 2. 启动SSO服务端
cd sso-server
mvn spring-boot:run

# 3. 启动客户端后端
cd ../sso-client-backend
mvn spring-boot:run

# 4. 启动客户端前端
cd ../sso-client-frontend
npm install
npm run dev
```

### 2. 生产环境部署

```bash
# 一键启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f sso-server
```

### 3. 访问地址

- **SSO服务端**: http://localhost:8081
- **客户端后端**: http://localhost:8082
- **客户端前端**: http://localhost:5173
- **Nginx代理**: http://localhost

这个完整的项目结构和配置提供了：
- 📁 清晰的目录组织结构
- 📦 完整的依赖配置
- 🐳 Docker容器化部署
- 🚀 快速启动指南
- 🔧 开发和生产环境配置

通过这个结构，您可以快速搭建和部署一个完整的企业级SSO系统。
