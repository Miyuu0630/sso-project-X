<template>
  <div class="callback-container">
    <div class="callback-content">
      <!-- 加载状态 -->
      <div v-if="!error && !success" class="loading-section">
        <div class="loading-spinner">
          <div class="spinner"></div>
        </div>
        <h2 class="callback-title">正在处理登录信息...</h2>
        <p class="callback-message">{{ message }}</p>
        <div class="progress-steps">
          <div class="step" :class="{ active: currentStep >= 1 }">
            <div class="step-icon">1</div>
            <div class="step-text">验证凭证</div>
          </div>
          <div class="step" :class="{ active: currentStep >= 2 }">
            <div class="step-icon">2</div>
            <div class="step-text">获取用户信息</div>
          </div>
          <div class="step" :class="{ active: currentStep >= 3 }">
            <div class="step-icon">3</div>
            <div class="step-text">跳转页面</div>
          </div>
        </div>
      </div>

      <!-- 成功状态 -->
      <div v-if="success" class="success-section">
        <div class="success-icon">
          <el-icon size="64" color="#67c23a"><SuccessFilled /></el-icon>
        </div>
        <h2 class="success-title">登录成功！</h2>
        <p class="success-message">正在跳转到目标页面...</p>
        <div class="countdown">
          <span>{{ countdown }} 秒后自动跳转</span>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-if="error" class="error-section">
        <div class="error-icon">
          <el-icon size="64" color="#f56c6c"><CircleCloseFilled /></el-icon>
        </div>
        <h2 class="error-title">登录失败</h2>
        <p class="error-message">{{ error }}</p>
        <div class="error-actions">
          <el-button type="primary" @click="retryLogin">
            重新登录
          </el-button>
          <el-button @click="goHome">
            返回首页
          </el-button>
        </div>
      </div>

      <!-- 调试信息 (开发环境) -->
      <div v-if="showDebugInfo" class="debug-info">
        <el-collapse>
          <el-collapse-item title="调试信息" name="debug">
            <div class="debug-content">
              <p><strong>URL 参数:</strong></p>
              <pre>{{ JSON.stringify(urlParams, null, 2) }}</pre>
              <p><strong>处理步骤:</strong></p>
              <ul>
                <li v-for="step in debugSteps" :key="step.id">
                  [{{ step.time }}] {{ step.message }}
                </li>
              </ul>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { SuccessFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 路由和状态管理
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const message = ref('正在验证登录凭证...')
const error = ref('')
const success = ref(false)
const currentStep = ref(1)
const countdown = ref(3)
const showDebugInfo = ref(process.env.NODE_ENV === 'development')

// 调试信息
const urlParams = ref({})
const debugSteps = ref([])

// 添加调试步骤
const addDebugStep = (message) => {
  debugSteps.value.push({
    id: Date.now(),
    time: new Date().toLocaleTimeString(),
    message
  })
}

// 处理 SSO 回调
const handleSsoCallback = async () => {
  try {
    addDebugStep('开始处理 SSO 回调')

    // 获取 URL 参数
    const query = route.query
    urlParams.value = { ...query }

    addDebugStep(`URL 参数: ${JSON.stringify(query)}`)

    // 检查必要参数
    const ticket = query.ticket
    const returnUrl = query.return_url || query.redirect || '/'

    if (!ticket) {
      throw new Error('未收到有效的登录凭证 (ticket)')
    }

    addDebugStep(`Ticket: ${ticket.substring(0, 20)}...`)
    addDebugStep(`返回地址: ${returnUrl}`)

    // 步骤 1: 验证Ticket
    currentStep.value = 1
    message.value = '正在验证登录凭证...'

    // 使用SSO服务器验证ticket
    const ssoValidateResponse = await request.post('http://localhost:8081/sso/validate', null, {
      params: { ticket }
    })
    if (ssoValidateResponse.data.code !== 200) {
      throw new Error(ssoValidateResponse.data.message || 'Ticket验证失败')
    }

    const userInfo = ssoValidateResponse.data.data
    addDebugStep(`Ticket验证成功，用户: ${userInfo.username}`)

    // 步骤 2: 本地登录处理
    currentStep.value = 2
    message.value = '正在处理本地登录...'

    // 设置用户信息到authStore
    authStore.userInfo = userInfo
    authStore.roles = userInfo.roles || []
    authStore.primaryRole = getPrimaryRole(userInfo.roles || [])

    // 设置token - 从ticket生成唯一token
    const clientToken = 'sso_client_' + ticket.substring(0, 8) + '_' + Date.now()
    authStore.setToken(clientToken)

    // 通知后端建立本地会话
    try {
      const sessionResponse = await request.post('/sso/establish-session', {
        ticket: ticket,
        token: clientToken,
        userInfo: userInfo
      })
      if (sessionResponse.data.code === 200) {
        addDebugStep('本地会话建立成功')
      } else {
        addDebugStep('本地会话建立失败，但继续处理')
      }
    } catch (error) {
      addDebugStep('本地会话建立失败，但继续处理: ' + error.message)
    }

    addDebugStep(`本地用户信息已设置，角色: ${authStore.primaryRole}`)

    // 步骤 3: 准备跳转
    currentStep.value = 3
    message.value = '登录成功，正在跳转...'
    success.value = true

    addDebugStep(`准备跳转到: ${returnUrl}`)

    // 跳转到对应仪表板或原始URL
    setTimeout(() => {
      if (returnUrl && returnUrl !== '/' && returnUrl !== window.location.origin + '/') {
        // 如果有具体的returnUrl，跳转到该URL
        addDebugStep(`跳转到原始URL: ${returnUrl}`)
        // 对于外部URL，直接跳转
        if (returnUrl.startsWith('http')) {
          window.location.href = returnUrl
        } else {
          // 对于相对路径，使用Vue Router
          router.push(returnUrl)
        }
      } else {
        // 否则跳转到对应仪表板
        const dashboardPath = getDashboardPath(authStore.primaryRole)
        addDebugStep(`跳转到仪表板: ${dashboardPath}`)
        router.push(dashboardPath)
      }
    }, 1000)
    
  } catch (err) {
    console.error('SSO 回调处理失败:', err)
    addDebugStep(`错误: ${err.message}`)
    
    error.value = err.message || 'SSO 登录处理失败'
    authStore.clearAuth()
    
    // 记录错误日志
    ElMessage.error(error.value)
  }
}

// 重新登录
const retryLogin = () => {
  addDebugStep('用户点击重新登录')
  authStore.redirectToLogin()
}

// 返回首页
const goHome = () => {
  addDebugStep('用户点击返回首页')
  router.push('/')
}

// 获取主要角色（按权限优先级排序，与数据库role_sort字段对应）
const getPrimaryRole = (userRoles) => {
  const roleHierarchy = ['ADMIN', 'PERSONAL_USER', 'ENTERPRISE_USER', 'AIRLINE_USER']
  for (const role of roleHierarchy) {
    if (userRoles.includes(role)) {
      return role
    }
  }
  return 'PERSONAL_USER' // 默认角色
}

// 获取仪表板路径
const getDashboardPath = (primaryRole) => {
  const roleDashboardMap = {
    'ADMIN': '/dashboard/admin',
    'PERSONAL_USER': '/dashboard/personal',
    'ENTERPRISE_USER': '/dashboard/enterprise',
    'AIRLINE_USER': '/dashboard/airline'
  }
  return roleDashboardMap[primaryRole] || '/dashboard/personal'
}

// 生命周期
onMounted(() => {
  addDebugStep('Callback 页面已挂载')

  // 延迟处理，让用户看到加载状态
  setTimeout(() => {
    handleSsoCallback()
  }, 500)
})
</script>

<style scoped>
.callback-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.callback-content {
  max-width: 500px;
  width: 100%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  text-align: center;
}

/* 加载状态 */
.loading-section {
  padding: 20px 0;
}

.loading-spinner {
  margin-bottom: 24px;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.callback-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 16px 0;
}

.callback-message {
  color: #7f8c8d;
  font-size: 16px;
  margin: 0 0 32px 0;
}

/* 进度步骤 */
.progress-steps {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 32px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  position: relative;
}

.step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 20px;
  right: -50%;
  width: 100%;
  height: 2px;
  background: #e0e0e0;
  z-index: 1;
}

.step.active:not(:last-child)::after {
  background: #667eea;
}

.step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 8px;
  position: relative;
  z-index: 2;
}

.step.active .step-icon {
  background: #667eea;
  color: white;
}

.step-text {
  font-size: 12px;
  color: #999;
}

.step.active .step-text {
  color: #667eea;
  font-weight: 500;
}

/* 成功状态 */
.success-section {
  padding: 20px 0;
}

.success-icon {
  margin-bottom: 24px;
}

.success-title {
  font-size: 24px;
  font-weight: 600;
  color: #67c23a;
  margin: 0 0 16px 0;
}

.success-message {
  color: #7f8c8d;
  font-size: 16px;
  margin: 0 0 24px 0;
}

.countdown {
  font-size: 14px;
  color: #909399;
  background: #f5f7fa;
  padding: 8px 16px;
  border-radius: 8px;
  display: inline-block;
}

/* 错误状态 */
.error-section {
  padding: 20px 0;
}

.error-icon {
  margin-bottom: 24px;
}

.error-title {
  font-size: 24px;
  font-weight: 600;
  color: #f56c6c;
  margin: 0 0 16px 0;
}

.error-message {
  color: #7f8c8d;
  font-size: 16px;
  margin: 0 0 32px 0;
  word-break: break-word;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* 调试信息 */
.debug-info {
  margin-top: 32px;
  text-align: left;
}

.debug-content {
  font-size: 12px;
  color: #666;
}

.debug-content pre {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  overflow-x: auto;
}

.debug-content ul {
  max-height: 200px;
  overflow-y: auto;
  padding-left: 20px;
}

.debug-content li {
  margin-bottom: 4px;
  font-family: monospace;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .callback-content {
    padding: 24px;
  }
  
  .progress-steps {
    flex-direction: column;
    gap: 16px;
  }
  
  .step::after {
    display: none;
  }
  
  .error-actions {
    flex-direction: column;
  }
}
</style>
