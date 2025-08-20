<template>
  <div class="sso-test">
    <el-card class="test-card">
      <template #header>
        <div class="card-header">
          <span>SSO 连接测试</span>
          <el-button type="primary" @click="refreshStatus">刷新状态</el-button>
        </div>
      </template>
      
      <div class="test-content">
        <!-- 当前状态 -->
        <el-alert
          :title="statusTitle"
          :type="statusType"
          :description="statusDescription"
          show-icon
          :closable="false"
        />
        
        <el-divider />
        
        <!-- 配置信息 -->
        <div class="config-info">
          <h3>配置信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="前端地址">{{ frontendUrl }}</el-descriptions-item>
            <el-descriptions-item label="后端地址">{{ backendUrl }}</el-descriptions-item>
            <el-descriptions-item label="SSO服务端">{{ ssoServerUrl }}</el-descriptions-item>
            <el-descriptions-item label="当前Token">{{ currentToken || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <el-divider />
        
        <!-- 测试按钮 -->
        <div class="test-actions">
          <h3>测试操作</h3>
          <el-space wrap>
            <el-button type="primary" @click="testSsoLogin">
              测试SSO登录
            </el-button>
            <el-button type="success" @click="testTokenVerify" :disabled="!currentToken">
              验证Token
            </el-button>
            <el-button type="info" @click="testUserInfo" :disabled="!currentToken">
              获取用户信息
            </el-button>
            <el-button type="warning" @click="clearAuth">
              清除认证
            </el-button>
          </el-space>
        </div>
        
        <el-divider />
        
        <!-- 测试日志 -->
        <div class="test-logs">
          <h3>测试日志</h3>
          <el-scrollbar height="300px">
            <div class="log-container">
              <div
                v-for="log in logs"
                :key="log.id"
                :class="['log-item', `log-${log.type}`]"
              >
                <span class="log-time">{{ log.time }}</span>
                <span class="log-message">{{ log.message }}</span>
              </div>
            </div>
          </el-scrollbar>
          <el-button type="danger" size="small" @click="clearLogs" style="margin-top: 10px;">
            清空日志
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import axios from 'axios'

const authStore = useAuthStore()

// 状态数据
const logs = ref([])
const frontendUrl = ref(window.location.origin)
const backendUrl = ref('http://localhost:8082')
const ssoServerUrl = ref('http://localhost:8081')

// 计算属性
const currentToken = computed(() => authStore.token)
const isLoggedIn = computed(() => authStore.isLoggedIn)

const statusTitle = computed(() => {
  return isLoggedIn.value ? '已登录' : '未登录'
})

const statusType = computed(() => {
  return isLoggedIn.value ? 'success' : 'warning'
})

const statusDescription = computed(() => {
  if (isLoggedIn.value) {
    return `用户: ${authStore.userInfo?.username || '未知'}`
  }
  return '请先进行SSO登录测试'
})

// 添加日志
const addLog = (message, type = 'info') => {
  logs.value.unshift({
    id: Date.now(),
    time: new Date().toLocaleTimeString(),
    message,
    type
  })
}

// 刷新状态
const refreshStatus = () => {
  addLog('刷新状态', 'info')
  authStore.initAuth()
}

// 测试SSO登录
const testSsoLogin = () => {
  addLog('开始SSO登录测试', 'info')
  addLog(`跳转到: ${ssoServerUrl.value}/sso/auth`, 'info')
  authStore.redirectToLogin()
}

// 验证Token
const testTokenVerify = async () => {
  try {
    addLog('开始验证Token', 'info')
    const isValid = await authStore.checkTokenValidity()
    if (isValid) {
      addLog('Token验证成功', 'success')
      ElMessage.success('Token有效')
    } else {
      addLog('Token验证失败', 'error')
      ElMessage.error('Token无效')
    }
  } catch (error) {
    addLog(`Token验证异常: ${error.message}`, 'error')
    ElMessage.error('Token验证失败')
  }
}

// 获取用户信息
const testUserInfo = async () => {
  try {
    addLog('开始获取用户信息', 'info')
    await authStore.fetchUserInfo()
    addLog('用户信息获取成功', 'success')
    ElMessage.success('用户信息获取成功')
  } catch (error) {
    addLog(`用户信息获取失败: ${error.message}`, 'error')
    ElMessage.error('用户信息获取失败')
  }
}

// 清除认证
const clearAuth = () => {
  addLog('清除认证信息', 'warning')
  authStore.clearAuth()
  ElMessage.success('认证信息已清除')
}

// 清空日志
const clearLogs = () => {
  logs.value = []
}

// 生命周期
onMounted(() => {
  addLog('SSO测试页面已加载', 'info')
  addLog(`前端地址: ${frontendUrl.value}`, 'info')
  addLog(`后端地址: ${backendUrl.value}`, 'info')
  addLog(`SSO服务端: ${ssoServerUrl.value}`, 'info')
})
</script>

<style scoped>
.sso-test {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.test-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.test-content {
  padding: 20px 0;
}

.config-info,
.test-actions,
.test-logs {
  margin-bottom: 20px;
}

.log-container {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.log-item {
  margin-bottom: 5px;
  padding: 2px 0;
}

.log-time {
  color: #666;
  margin-right: 10px;
}

.log-info .log-message {
  color: #409EFF;
}

.log-success .log-message {
  color: #67C23A;
}

.log-warning .log-message {
  color: #E6A23C;
}

.log-error .log-message {
  color: #F56C6C;
}
</style>
