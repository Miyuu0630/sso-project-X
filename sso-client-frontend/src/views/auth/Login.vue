<template>
  <div class="login-container">
    <!-- Logo 和标题 -->
    <div class="logo">
      <img src="/static/images/logo.png" alt="Logo" @error="hideLogo">
      <h1>欢迎登录</h1>
      <p>SSO 统一认证中心</p>
    </div>

    <!-- 错误/成功消息 -->
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>

    <!-- 登录表单 -->
    <el-form
      ref="loginFormRef"
      :model="loginForm"
      :rules="loginRules"
      class="login-form"
      label-position="top"
      @submit.prevent="handleLogin"
    >
      <!-- 账号输入 -->
      <el-form-item label="账号 *" prop="account">
        <el-input
          v-model="loginForm.account"
          placeholder="用户名、手机号或邮箱"
          clearable
          size="large"
        />
      </el-form-item>

      <!-- 密码输入 -->
      <el-form-item label="密码 *" prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          show-password
          size="large"
          @keyup.enter="handleLogin"
        />
      </el-form-item>

      <!-- 登录类型选择 -->
      <el-form-item label="登录类型" prop="loginType">
        <el-radio-group v-model="loginForm.loginType" size="small">
          <el-radio value="username">用户名</el-radio>
          <el-radio value="phone">手机号</el-radio>
          <el-radio value="email">邮箱</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 记住我 -->
      <el-form-item>
        <el-checkbox v-model="loginForm.rememberMe">
          记住我
        </el-checkbox>
      </el-form-item>

      <!-- 登录按钮 -->
      <el-form-item>
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          @click="handleLogin"
          style="width: 100%"
        >
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 页脚链接 -->
    <div class="form-footer">
      <el-link type="primary" @click="goToRegister">没有账号？立即注册</el-link>
      <el-divider direction="vertical" />
      <el-link type="primary" @click="forgotPassword">忘记密码？</el-link>
    </div>

    <!-- 第三方登录 -->
    <div class="oauth-login">
      <el-divider>其他登录方式</el-divider>
      <div class="oauth-buttons">
        <el-button circle @click="oauthLogin('wechat')">
          <i class="icon-wechat"></i>
        </el-button>
        <el-button circle @click="oauthLogin('alipay')">
          <i class="icon-alipay"></i>
        </el-button>
        <el-button circle @click="oauthLogin('qq')">
          <i class="icon-qq"></i>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { login } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 表单引用
const loginFormRef = ref()

// 加载状态
const loading = ref(false)

// 消息状态
const errorMessage = ref('')
const successMessage = ref('')

// 登录表单数据
const loginForm = reactive({
  account: '',
  password: '',
  loginType: 'username',
  rememberMe: false
})

// 表单验证规则
const loginRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  loginType: [
    { required: true, message: '请选择登录类型', trigger: 'change' }
  ]
}

// 方法
const hideLogo = (event) => {
  event.target.style.display = 'none'
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 验证表单
    await loginFormRef.value.validate()
    
    loading.value = true
    errorMessage.value = ''
    successMessage.value = ''

    // 调用登录API
    const response = await login(loginForm)
    
    if (response.code === 200) {
      // 登录成功，保存用户信息
      authStore.setUserInfo(response.data)
      authStore.setToken(response.data.accessToken)
      
      successMessage.value = '登录成功！正在跳转...'
      ElMessage.success('登录成功！')
      
      // 根据用户角色跳转到对应仪表板
      const userRoles = response.data.roles || []
      const primaryRole = getUserPrimaryRole(userRoles)
      const dashboardPath = getDashboardPath(primaryRole)
      
      setTimeout(() => {
        router.push(dashboardPath)
      }, 1000)
    } else {
      errorMessage.value = response.message || '登录失败，请重试'
      ElMessage.error(response.message || '登录失败，请重试')
    }
  } catch (error) {
    console.error('登录失败:', error)
    errorMessage.value = '登录失败，请检查账号和密码'
    ElMessage.error('登录失败，请检查账号和密码')
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}

const forgotPassword = () => {
  ElMessage.info('忘记密码功能暂未开放')
}

const oauthLogin = (provider) => {
  ElMessage.info(`${provider} 登录功能暂未开放`)
}

// 获取用户主要角色
const getUserPrimaryRole = (userRoles) => {
  const roleHierarchy = ['ADMIN', 'PERSONAL_USER', 'ENTERPRISE_USER', 'AIRLINE_USER']
  for (const role of roleHierarchy) {
    if (userRoles.includes(role)) {
      return role
    }
  }
  return 'PERSONAL_USER'
}

// 获取仪表板路径
const getDashboardPath = (role) => {
  const roleDashboardMap = {
    'ADMIN': '/dashboard/admin',
    'PERSONAL_USER': '/dashboard/personal',
    'ENTERPRISE_USER': '/dashboard/enterprise',
    'AIRLINE_USER': '/dashboard/airline'
  }
  return roleDashboardMap[role] || '/dashboard/personal'
}

// 页面初始化
onMounted(() => {
  // 检查是否有重定向参数
  const redirectUrl = route.query.redirect_url
  if (redirectUrl) {
    console.log('检测到重定向URL:', redirectUrl)
  }
  
  // 检查是否有提示消息
  const message = route.query.message
  if (message) {
    successMessage.value = decodeURIComponent(message)
  }
})
</script>

<style scoped>
.login-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.logo {
  text-align: center;
  margin-bottom: 32px;
}

.logo img {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
}

.logo h1 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.logo p {
  color: #7f8c8d;
  font-size: 14px;
}

.login-form {
  margin-bottom: 24px;
}

.error-message {
  background: #fee;
  color: #e74c3c;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #e74c3c;
  font-size: 14px;
}

.success-message {
  background: #efe;
  color: #27ae60;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #27ae60;
  font-size: 14px;
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.oauth-login {
  margin-top: 32px;
}

.oauth-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.oauth-buttons .el-button {
  width: 48px;
  height: 48px;
  font-size: 20px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-container {
    padding: 24px;
    margin: 10px;
  }
  
  .logo h1 {
    font-size: 20px;
  }
  
  .form-footer {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
