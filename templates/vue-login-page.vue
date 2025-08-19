<!--
  Vue 前端 SSO 登录页面
  企业级空壳登录页面实现
-->

<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 头部 Logo 和标题 -->
      <div class="login-header">
        <div class="logo">
          <img src="/logo.png" alt="Logo" class="logo-img">
        </div>
        <h1 class="login-title">企业统一认证中心</h1>
        <p class="login-subtitle">Single Sign-On Portal</p>
      </div>

      <!-- 登录表单 -->
      <div class="login-form">
        <el-form 
          ref="loginFormRef" 
          :model="loginForm" 
          :rules="loginRules"
          size="large"
          @submit.prevent="handleLogin"
        >
          <!-- 登录方式切换 -->
          <div class="login-type-tabs">
            <el-radio-group v-model="loginForm.loginType" class="login-type-group">
              <el-radio-button label="username">用户名</el-radio-button>
              <el-radio-button label="phone">手机号</el-radio-button>
              <el-radio-button label="email">邮箱</el-radio-button>
            </el-radio-group>
          </div>

          <!-- 用户名/手机号/邮箱输入 -->
          <el-form-item prop="credential">
            <el-input
              v-model="loginForm.credential"
              :placeholder="getCredentialPlaceholder()"
              :prefix-icon="getCredentialIcon()"
              clearable
              autocomplete="username"
            />
          </el-form-item>

          <!-- 密码输入 -->
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              clearable
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <!-- 记住登录状态 -->
          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="loginForm.rememberMe">
                记住登录状态
              </el-checkbox>
              <el-link type="primary" class="forgot-password">
                忘记密码？
              </el-link>
            </div>
          </el-form-item>

          <!-- 错误信息显示 -->
          <el-alert
            v-if="authStore.loginError"
            :title="authStore.loginError"
            type="error"
            :closable="false"
            class="login-error"
          />

          <!-- 登录按钮 -->
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="authStore.isLoading"
              @click="handleLogin"
              class="login-button"
            >
              {{ authStore.isLoading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 其他登录方式 -->
        <div class="other-login">
          <el-divider>其他登录方式</el-divider>
          <div class="social-login">
            <el-button circle disabled>
              <i class="fab fa-wechat"></i>
            </el-button>
            <el-button circle disabled>
              <i class="fab fa-alipay"></i>
            </el-button>
            <el-button circle disabled>
              <i class="fab fa-qq"></i>
            </el-button>
          </div>
          <p class="social-login-tip">第三方登录功能开发中...</p>
        </div>
      </div>

      <!-- 页脚信息 -->
      <div class="login-footer">
        <p class="copyright">
          © 2025 企业SSO认证中心. All rights reserved.
        </p>
        <div class="footer-links">
          <el-link type="info">服务条款</el-link>
          <el-divider direction="vertical" />
          <el-link type="info">隐私政策</el-link>
          <el-divider direction="vertical" />
          <el-link type="info">帮助中心</el-link>
        </div>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="login-background">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

// ========================================
// 组件状态
// ========================================

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref()

// 登录表单数据
const loginForm = reactive({
  loginType: 'username',
  credential: '',
  password: '',
  rememberMe: false
})

// 表单验证规则
const loginRules = {
  credential: [
    { required: true, message: '请输入登录凭证', trigger: 'blur' },
    { validator: validateCredential, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// ========================================
// 计算属性
// ========================================

const getCredentialPlaceholder = () => {
  const placeholders = {
    username: '请输入用户名',
    phone: '请输入手机号',
    email: '请输入邮箱地址'
  }
  return placeholders[loginForm.loginType]
}

const getCredentialIcon = () => {
  const icons = {
    username: 'User',
    phone: 'Phone',
    email: 'Message'
  }
  return icons[loginForm.loginType]
}

// ========================================
// 方法
// ========================================

/**
 * 验证登录凭证格式
 */
function validateCredential(rule, value, callback) {
  if (!value) {
    callback(new Error('请输入登录凭证'))
    return
  }

  switch (loginForm.loginType) {
    case 'phone':
      if (!/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error('请输入正确的手机号'))
      }
      break
    case 'email':
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        callback(new Error('请输入正确的邮箱地址'))
      }
      break
    case 'username':
      if (!/^[a-zA-Z0-9_]{3,20}$/.test(value)) {
        callback(new Error('用户名只能包含字母、数字和下划线，长度3-20位'))
      }
      break
  }
  callback()
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  try {
    // 表单验证
    const valid = await loginFormRef.value.validate()
    if (!valid) {
      return
    }

    // 执行登录
    const success = await authStore.directLogin(loginForm)
    
    if (success) {
      ElMessage.success('登录成功')
      // 登录成功后的跳转由 authStore 处理
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请重试')
  }
}

/**
 * 处理 SSO 回调
 */
const handleSsoCallback = () => {
  const urlParams = new URLSearchParams(window.location.search)
  if (urlParams.has('token')) {
    authStore.handleSsoCallback(urlParams)
  }
}

// ========================================
// 生命周期
// ========================================

onMounted(() => {
  // 检查是否是 SSO 回调
  if (route.query.token) {
    handleSsoCallback()
    return
  }

  // 如果已经登录，跳转到首页
  if (authStore.isLoggedIn) {
    router.push('/')
  }

  // 清除之前的错误信息
  authStore.loginError = ''
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-card {
  width: 400px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  position: relative;
  z-index: 10;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-img {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.login-subtitle {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
}

.login-type-tabs {
  margin-bottom: 24px;
}

.login-type-group {
  width: 100%;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-error {
  margin-bottom: 16px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
}

.other-login {
  margin-top: 32px;
  text-align: center;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin: 16px 0;
}

.social-login-tip {
  color: #95a5a6;
  font-size: 12px;
  margin: 8px 0 0 0;
}

.login-footer {
  margin-top: 32px;
  text-align: center;
}

.copyright {
  color: #7f8c8d;
  font-size: 12px;
  margin: 0 0 8px 0;
}

.footer-links {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

/* 背景装饰 */
.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 10%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 24px;
  }
  
  .login-title {
    font-size: 20px;
  }
}
</style>
