# 第三方登录集成指南

## 🔗 微信登录集成

### 1. 微信开放平台配置

```yaml
# application.yml
oauth:
  wechat:
    app-id: ${WECHAT_APP_ID}
    app-secret: ${WECHAT_APP_SECRET}
    redirect-uri: ${WECHAT_REDIRECT_URI:http://localhost:8081/oauth/wechat/callback}
    scope: snsapi_login
```

### 2. 微信登录控制器

```java
@RestController
@RequestMapping("/oauth/wechat")
public class WechatOAuthController {
    
    @Value("${oauth.wechat.app-id}")
    private String appId;
    
    @Value("${oauth.wechat.app-secret}")
    private String appSecret;
    
    @Value("${oauth.wechat.redirect-uri}")
    private String redirectUri;
    
    @Autowired
    private OAuthService oAuthService;
    
    /**
     * 获取微信授权URL
     */
    @GetMapping("/authorize")
    public Result<String> getAuthorizeUrl(@RequestParam(required = false) String state) {
        String authorizeUrl = String.format(
            "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
            appId,
            URLEncoder.encode(redirectUri, StandardCharsets.UTF_8),
            StringUtils.hasText(state) ? state : "default"
        );
        
        return Result.success("获取授权URL成功", authorizeUrl);
    }
    
    /**
     * 微信登录回调
     */
    @GetMapping("/callback")
    public Result<LoginResponse> callback(@RequestParam String code, 
                                        @RequestParam(required = false) String state,
                                        HttpServletRequest request) {
        try {
            // 1. 通过code获取access_token
            WechatTokenResponse tokenResponse = getAccessToken(code);
            
            // 2. 通过access_token获取用户信息
            WechatUserInfo userInfo = getUserInfo(tokenResponse.getAccessToken(), tokenResponse.getOpenid());
            
            // 3. 处理登录逻辑
            LoginResponse loginResponse = oAuthService.handleWechatLogin(userInfo, request);
            
            return Result.success("微信登录成功", loginResponse);
        } catch (Exception e) {
            log.error("微信登录失败", e);
            return Result.error("微信登录失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取微信access_token
     */
    private WechatTokenResponse getAccessToken(String code) {
        String url = String.format(
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
            appId, appSecret, code
        );
        
        String response = HttpUtil.get(url);
        return JSONUtil.toBean(response, WechatTokenResponse.class);
    }
    
    /**
     * 获取微信用户信息
     */
    private WechatUserInfo getUserInfo(String accessToken, String openid) {
        String url = String.format(
            "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s",
            accessToken, openid
        );
        
        String response = HttpUtil.get(url);
        return JSONUtil.toBean(response, WechatUserInfo.class);
    }
}
```

### 3. 微信用户信息DTO

```java
@Data
public class WechatUserInfo {
    private String openid;
    private String nickname;
    private Integer sex;
    private String province;
    private String city;
    private String country;
    private String headimgurl;
    private String unionid;
}

@Data
public class WechatTokenResponse {
    private String accessToken;
    private Integer expiresIn;
    private String refreshToken;
    private String openid;
    private String scope;
    private String unionid;
}
```

## 🔗 支付宝登录集成

### 1. 支付宝开放平台配置

```yaml
oauth:
  alipay:
    app-id: ${ALIPAY_APP_ID}
    private-key: ${ALIPAY_PRIVATE_KEY}
    public-key: ${ALIPAY_PUBLIC_KEY}
    redirect-uri: ${ALIPAY_REDIRECT_URI:http://localhost:8081/oauth/alipay/callback}
    gateway-url: https://openapi.alipay.com/gateway.do
```

### 2. 支付宝登录控制器

```java
@RestController
@RequestMapping("/oauth/alipay")
public class AlipayOAuthController {
    
    @Autowired
    private AlipayClient alipayClient;
    
    @Autowired
    private OAuthService oAuthService;
    
    /**
     * 获取支付宝授权URL
     */
    @GetMapping("/authorize")
    public Result<String> getAuthorizeUrl(@RequestParam(required = false) String state) {
        try {
            AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
            request.setGrantType("authorization_code");
            request.setCode("auth_code");
            
            String authorizeUrl = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm" +
                "?app_id=" + alipayAppId +
                "&scope=auth_user" +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&state=" + (StringUtils.hasText(state) ? state : "default");
            
            return Result.success("获取授权URL成功", authorizeUrl);
        } catch (Exception e) {
            log.error("获取支付宝授权URL失败", e);
            return Result.error("获取授权URL失败：" + e.getMessage());
        }
    }
    
    /**
     * 支付宝登录回调
     */
    @GetMapping("/callback")
    public Result<LoginResponse> callback(@RequestParam String auth_code,
                                        @RequestParam(required = false) String state,
                                        HttpServletRequest request) {
        try {
            // 1. 通过auth_code获取access_token
            AlipaySystemOauthTokenRequest tokenRequest = new AlipaySystemOauthTokenRequest();
            tokenRequest.setGrantType("authorization_code");
            tokenRequest.setCode(auth_code);
            
            AlipaySystemOauthTokenResponse tokenResponse = alipayClient.execute(tokenRequest);
            
            if (!tokenResponse.isSuccess()) {
                return Result.error("获取支付宝token失败");
            }
            
            // 2. 通过access_token获取用户信息
            AlipayUserInfoShareRequest userInfoRequest = new AlipayUserInfoShareRequest();
            AlipayUserInfoShareResponse userInfoResponse = alipayClient.execute(userInfoRequest, tokenResponse.getAccessToken());
            
            if (!userInfoResponse.isSuccess()) {
                return Result.error("获取支付宝用户信息失败");
            }
            
            // 3. 处理登录逻辑
            LoginResponse loginResponse = oAuthService.handleAlipayLogin(userInfoResponse, request);
            
            return Result.success("支付宝登录成功", loginResponse);
        } catch (Exception e) {
            log.error("支付宝登录失败", e);
            return Result.error("支付宝登录失败：" + e.getMessage());
        }
    }
}
```

## 🔗 OAuth服务统一处理

### 1. OAuth服务接口

```java
@Service
public class OAuthService {
    
    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private UserOAuthBindingService bindingService;
    
    /**
     * 处理微信登录
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse handleWechatLogin(WechatUserInfo wechatUser, HttpServletRequest request) {
        return handleOAuthLogin("wechat", wechatUser.getOpenid(), wechatUser.getUnionid(), 
                               wechatUser.getNickname(), wechatUser.getHeadimgurl(), request);
    }
    
    /**
     * 处理支付宝登录
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse handleAlipayLogin(AlipayUserInfoShareResponse alipayUser, HttpServletRequest request) {
        return handleOAuthLogin("alipay", alipayUser.getUserId(), null,
                               alipayUser.getNickName(), alipayUser.getAvatar(), request);
    }
    
    /**
     * 统一OAuth登录处理
     */
    private LoginResponse handleOAuthLogin(String provider, String openId, String unionId,
                                         String nickname, String avatar, HttpServletRequest request) {
        // 1. 查找是否已绑定用户
        SysUser existingUser = sysUserService.getByOAuthBinding(provider, openId);
        
        SysUser user;
        if (existingUser != null) {
            // 已绑定用户，直接登录
            user = existingUser;
            
            // 更新第三方信息
            bindingService.updateOAuthInfo(user.getId(), provider, openId, unionId, nickname, avatar);
        } else {
            // 未绑定用户，创建新用户
            user = createUserFromOAuth(provider, openId, unionId, nickname, avatar);
            
            // 创建绑定关系
            bindingService.createBinding(user.getId(), provider, openId, unionId, nickname, avatar);
        }
        
        // 执行登录
        StpUtil.login(user.getId());
        
        // 记录登录设备
        deviceManagementService.recordLoginDevice(user.getId(), request);
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(StpUtil.getTokenValue());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setRoles(sysUserService.getUserRoles(user.getId()));
        
        return response;
    }
    
    /**
     * 从第三方信息创建用户
     */
    private SysUser createUserFromOAuth(String provider, String openId, String unionId, 
                                       String nickname, String avatar) {
        SysUser user = new SysUser();
        user.setUsername(provider + "_" + openId); // 生成唯一用户名
        user.setPassword(BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt())); // 随机密码
        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setUserType("normal");
        user.setStatus("1");
        user.setCreateTime(LocalDateTime.now());
        
        sysUserService.save(user);
        return user;
    }
}
```

### 2. 第三方账号绑定服务

```java
@Service
public class UserOAuthBindingService extends ServiceImpl<UserOAuthBindingMapper, UserOAuthBinding> {
    
    /**
     * 创建绑定关系
     */
    public void createBinding(Long userId, String provider, String openId, String unionId,
                            String nickname, String avatar) {
        UserOAuthBinding binding = new UserOAuthBinding();
        binding.setUserId(userId);
        binding.setProvider(provider);
        binding.setOpenId(openId);
        binding.setUnionId(unionId);
        binding.setNickname(nickname);
        binding.setAvatar(avatar);
        binding.setBindTime(LocalDateTime.now());
        binding.setLastLoginTime(LocalDateTime.now());
        binding.setIsActive(true);
        
        this.save(binding);
    }
    
    /**
     * 更新第三方信息
     */
    public void updateOAuthInfo(Long userId, String provider, String openId, String unionId,
                              String nickname, String avatar) {
        LambdaQueryWrapper<UserOAuthBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserOAuthBinding::getUserId, userId)
               .eq(UserOAuthBinding::getProvider, provider)
               .eq(UserOAuthBinding::getOpenId, openId);
        
        UserOAuthBinding binding = this.getOne(wrapper);
        if (binding != null) {
            binding.setNickname(nickname);
            binding.setAvatar(avatar);
            binding.setLastLoginTime(LocalDateTime.now());
            this.updateById(binding);
        }
    }
    
    /**
     * 解绑第三方账号
     */
    public boolean unbindOAuth(Long userId, String provider) {
        LambdaQueryWrapper<UserOAuthBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserOAuthBinding::getUserId, userId)
               .eq(UserOAuthBinding::getProvider, provider);
        
        UserOAuthBinding binding = this.getOne(wrapper);
        if (binding != null) {
            binding.setIsActive(false);
            return this.updateById(binding);
        }
        
        return false;
    }
    
    /**
     * 获取用户绑定的第三方账号
     */
    public List<UserOAuthBinding> getUserBindings(Long userId) {
        LambdaQueryWrapper<UserOAuthBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserOAuthBinding::getUserId, userId)
               .eq(UserOAuthBinding::getIsActive, true)
               .orderByDesc(UserOAuthBinding::getBindTime);
        
        return this.list(wrapper);
    }
}
```

## 🎯 Vue前端集成

### 1. 第三方登录组件

```vue
<!-- ThirdPartyLogin.vue -->
<template>
  <div class="third-party-login">
    <div class="login-title">第三方登录</div>
    <div class="login-buttons">
      <el-button 
        type="success" 
        @click="loginWithWechat"
        :loading="wechatLoading"
        class="wechat-btn"
      >
        <i class="fab fa-weixin"></i>
        微信登录
      </el-button>
      
      <el-button 
        type="primary" 
        @click="loginWithAlipay"
        :loading="alipayLoading"
        class="alipay-btn"
      >
        <i class="fab fa-alipay"></i>
        支付宝登录
      </el-button>
      
      <el-button 
        type="info" 
        @click="loginWithGithub"
        :loading="githubLoading"
        class="github-btn"
      >
        <i class="fab fa-github"></i>
        GitHub登录
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const wechatLoading = ref(false)
const alipayLoading = ref(false)
const githubLoading = ref(false)

// 微信登录
const loginWithWechat = async () => {
  try {
    wechatLoading.value = true
    
    // 获取微信授权URL
    const response = await axios.get('/api/oauth/wechat/authorize', {
      params: { state: 'login' }
    })
    
    if (response.data.success) {
      // 跳转到微信授权页面
      window.location.href = response.data.data
    } else {
      ElMessage.error('获取微信授权失败')
    }
  } catch (error) {
    console.error('微信登录失败:', error)
    ElMessage.error('微信登录失败')
  } finally {
    wechatLoading.value = false
  }
}

// 支付宝登录
const loginWithAlipay = async () => {
  try {
    alipayLoading.value = true
    
    const response = await axios.get('/api/oauth/alipay/authorize', {
      params: { state: 'login' }
    })
    
    if (response.data.success) {
      window.location.href = response.data.data
    } else {
      ElMessage.error('获取支付宝授权失败')
    }
  } catch (error) {
    console.error('支付宝登录失败:', error)
    ElMessage.error('支付宝登录失败')
  } finally {
    alipayLoading.value = false
  }
}

// GitHub登录
const loginWithGithub = async () => {
  try {
    githubLoading.value = true
    
    const response = await axios.get('/api/oauth/github/authorize', {
      params: { state: 'login' }
    })
    
    if (response.data.success) {
      window.location.href = response.data.data
    } else {
      ElMessage.error('获取GitHub授权失败')
    }
  } catch (error) {
    console.error('GitHub登录失败:', error)
    ElMessage.error('GitHub登录失败')
  } finally {
    githubLoading.value = false
  }
}
</script>

<style scoped>
.third-party-login {
  margin-top: 20px;
  text-align: center;
}

.login-title {
  margin-bottom: 15px;
  color: #666;
  font-size: 14px;
}

.login-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.wechat-btn {
  background-color: #07c160;
  border-color: #07c160;
}

.alipay-btn {
  background-color: #1677ff;
  border-color: #1677ff;
}

.github-btn {
  background-color: #24292e;
  border-color: #24292e;
  color: white;
}

.login-buttons .el-button {
  min-width: 120px;
}

.login-buttons .el-button i {
  margin-right: 5px;
}
</style>
```

### 2. 账号绑定管理页面

```vue
<!-- AccountBinding.vue -->
<template>
  <div class="account-binding">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>第三方账号绑定</span>
          <el-button type="primary" @click="refreshBindings">刷新</el-button>
        </div>
      </template>
      
      <div class="binding-list">
        <div v-for="provider in supportedProviders" :key="provider.key" class="binding-item">
          <div class="provider-info">
            <i :class="provider.icon"></i>
            <span class="provider-name">{{ provider.name }}</span>
          </div>
          
          <div class="binding-status">
            <template v-if="getBinding(provider.key)">
              <el-tag type="success">已绑定</el-tag>
              <span class="binding-time">
                绑定时间: {{ formatTime(getBinding(provider.key).bindTime) }}
              </span>
              <el-button 
                type="danger" 
                size="small" 
                @click="unbindAccount(provider.key)"
                :loading="unbindingProvider === provider.key"
              >
                解绑
              </el-button>
            </template>
            <template v-else>
              <el-tag type="info">未绑定</el-tag>
              <el-button 
                type="primary" 
                size="small" 
                @click="bindAccount(provider.key)"
                :loading="bindingProvider === provider.key"
              >
                绑定
              </el-button>
            </template>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const bindings = ref([])
const bindingProvider = ref('')
const unbindingProvider = ref('')

const supportedProviders = [
  { key: 'wechat', name: '微信', icon: 'fab fa-weixin' },
  { key: 'alipay', name: '支付宝', icon: 'fab fa-alipay' },
  { key: 'github', name: 'GitHub', icon: 'fab fa-github' }
]

// 获取绑定信息
const getBinding = (provider) => {
  return bindings.value.find(b => b.provider === provider)
}

// 刷新绑定列表
const refreshBindings = async () => {
  try {
    const response = await axios.get('/api/user/oauth-bindings')
    if (response.data.success) {
      bindings.value = response.data.data
    }
  } catch (error) {
    console.error('获取绑定信息失败:', error)
    ElMessage.error('获取绑定信息失败')
  }
}

// 绑定账号
const bindAccount = async (provider) => {
  try {
    bindingProvider.value = provider
    
    const response = await axios.get(`/api/oauth/${provider}/authorize`, {
      params: { state: 'bind' }
    })
    
    if (response.data.success) {
      window.location.href = response.data.data
    } else {
      ElMessage.error('获取授权失败')
    }
  } catch (error) {
    console.error('绑定失败:', error)
    ElMessage.error('绑定失败')
  } finally {
    bindingProvider.value = ''
  }
}

// 解绑账号
const unbindAccount = async (provider) => {
  try {
    await ElMessageBox.confirm(
      '确定要解绑该第三方账号吗？解绑后将无法使用该账号登录。',
      '确认解绑',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    unbindingProvider.value = provider
    
    const response = await axios.post(`/api/user/unbind-oauth`, { provider })
    
    if (response.data.success) {
      ElMessage.success('解绑成功')
      refreshBindings()
    } else {
      ElMessage.error('解绑失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解绑失败:', error)
      ElMessage.error('解绑失败')
    }
  } finally {
    unbindingProvider.value = ''
  }
}

// 格式化时间
const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

onMounted(() => {
  refreshBindings()
})
</script>

<style scoped>
.account-binding {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.binding-list {
  space-y: 16px;
}

.binding-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 16px;
}

.provider-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.provider-info i {
  font-size: 24px;
  width: 32px;
  text-align: center;
}

.provider-name {
  font-size: 16px;
  font-weight: 500;
}

.binding-status {
  display: flex;
  align-items: center;
  gap: 12px;
}

.binding-time {
  font-size: 12px;
  color: #666;
}
</style>
```

这个第三方登录集成指南提供了：
- 🔗 微信、支付宝等主流平台的完整集成
- 🎯 统一的OAuth服务处理逻辑
- 🎨 Vue前端的第三方登录组件
- 🔧 账号绑定管理功能
- 📱 移动端适配支持

通过这些实现，您的SSO系统可以轻松扩展支持更多第三方登录平台。
