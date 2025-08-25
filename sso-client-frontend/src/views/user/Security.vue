<template>
  <div class="security-container">
    <!-- 密码修改 -->
    <el-card class="security-card">
      <template #header>
        <div class="card-header">
          <el-icon><Lock /></el-icon>
          <span>修改密码</span>
        </div>
      </template>

      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="120px"
        class="password-form"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="changePassword" :loading="passwordLoading">
            <el-icon><Check /></el-icon>
            修改密码
          </el-button>
          <el-button @click="resetPasswordForm">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 安全设置 -->
    <el-card class="security-card">
      <template #header>
        <div class="card-header">
          <el-icon><Setting /></el-icon>
          <span>安全设置</span>
        </div>
      </template>

      <div class="security-settings">
        <div class="setting-item">
          <div class="setting-info">
            <h4>双因素认证</h4>
            <p>为您的账号添加额外的安全保护</p>
          </div>
          <div class="setting-action">
            <el-switch
              v-model="securitySettings.twoFactorEnabled"
              @change="toggleTwoFactor"
              :loading="twoFactorLoading"
            />
          </div>
        </div>

        <el-divider />

        <div class="setting-item">
          <div class="setting-info">
            <h4>登录通知</h4>
            <p>当有新设备登录时发送邮件通知</p>
          </div>
          <div class="setting-action">
            <el-switch
              v-model="securitySettings.loginNotification"
              @change="toggleLoginNotification"
              :loading="notificationLoading"
            />
          </div>
        </div>

        <el-divider />

        <div class="setting-item">
          <div class="setting-info">
            <h4>异常登录保护</h4>
            <p>检测到异常登录时自动锁定账号</p>
          </div>
          <div class="setting-action">
            <el-switch
              v-model="securitySettings.abnormalLoginProtection"
              @change="toggleAbnormalProtection"
              :loading="protectionLoading"
            />
          </div>
        </div>
      </div>
    </el-card>

    <!-- 登录设备管理 -->
    <el-card class="security-card">
      <template #header>
        <div class="card-header">
          <el-icon><Monitor /></el-icon>
          <span>登录设备</span>
          <el-button type="text" size="small" @click="refreshDevices" style="margin-left: auto;">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <div class="devices-list">
        <div
          v-for="device in loginDevices"
          :key="device.id"
          class="device-item"
        >
          <div class="device-icon">
            <el-icon size="24">
              <component :is="getDeviceIcon(device.deviceType)" />
            </el-icon>
          </div>
          <div class="device-info">
            <h4>{{ device.deviceName }}</h4>
            <p>{{ device.location }} · {{ device.lastActiveTime }}</p>
            <p class="device-details">{{ device.userAgent }}</p>
          </div>
          <div class="device-status">
            <el-tag :type="device.isActive ? 'success' : 'info'">
              {{ device.isActive ? '当前设备' : '离线' }}
            </el-tag>
          </div>
          <div class="device-actions">
            <el-button
              v-if="!device.isActive"
              type="danger"
              size="small"
              @click="removeDevice(device.id)"
            >
              移除
            </el-button>
          </div>
        </div>

        <div v-if="loginDevices.length === 0" class="empty-devices">
          <el-empty description="暂无登录设备记录" :image-size="80" />
        </div>
      </div>
    </el-card>

    <!-- 安全日志 -->
    <el-card class="security-card">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>安全日志</span>
          <el-button type="text" size="small" @click="loadSecurityLogs" style="margin-left: auto;">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-timeline>
        <el-timeline-item
          v-for="log in securityLogs"
          :key="log.id"
          :timestamp="log.time"
          :type="getLogType(log.type)"
        >
          <div class="log-content">
            <h4>{{ log.title }}</h4>
            <p>{{ log.description }}</p>
            <span class="log-ip">IP: {{ log.ipAddress }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>

      <div v-if="securityLogs.length === 0" class="empty-logs">
        <el-empty description="暂无安全日志" :image-size="80" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Lock, Setting, Monitor, Document, Check, Refresh
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const passwordFormRef = ref()
const passwordLoading = ref(false)
const twoFactorLoading = ref(false)
const notificationLoading = ref(false)
const protectionLoading = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const securitySettings = reactive({
  twoFactorEnabled: false,
  loginNotification: true,
  abnormalLoginProtection: true
})

const loginDevices = ref([])
const securityLogs = ref([])

// 表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    { 
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,}$/,
      message: '密码必须包含大小写字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 方法
const changePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true

    const response = await request.put('/api/user/change-password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    if (response.data.code === 200) {
      ElMessage.success('密码修改成功')
      resetPasswordForm()
    } else {
      ElMessage.error(response.data.message || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

const toggleTwoFactor = async (enabled) => {
  try {
    twoFactorLoading.value = true
    
    const response = await request.put('/api/user/two-factor', { enabled })
    if (response.data.code === 200) {
      ElMessage.success(enabled ? '双因素认证已开启' : '双因素认证已关闭')
    } else {
      securitySettings.twoFactorEnabled = !enabled
      ElMessage.error(response.data.message || '设置失败')
    }
  } catch (error) {
    securitySettings.twoFactorEnabled = !enabled
    ElMessage.error('设置失败')
  } finally {
    twoFactorLoading.value = false
  }
}

const toggleLoginNotification = async (enabled) => {
  try {
    notificationLoading.value = true
    
    const response = await request.put('/api/user/login-notification', { enabled })
    if (response.data.code === 200) {
      ElMessage.success(enabled ? '登录通知已开启' : '登录通知已关闭')
    } else {
      securitySettings.loginNotification = !enabled
      ElMessage.error(response.data.message || '设置失败')
    }
  } catch (error) {
    securitySettings.loginNotification = !enabled
    ElMessage.error('设置失败')
  } finally {
    notificationLoading.value = false
  }
}

const toggleAbnormalProtection = async (enabled) => {
  try {
    protectionLoading.value = true
    
    const response = await request.put('/api/user/abnormal-protection', { enabled })
    if (response.data.code === 200) {
      ElMessage.success(enabled ? '异常登录保护已开启' : '异常登录保护已关闭')
    } else {
      securitySettings.abnormalLoginProtection = !enabled
      ElMessage.error(response.data.message || '设置失败')
    }
  } catch (error) {
    securitySettings.abnormalLoginProtection = !enabled
    ElMessage.error('设置失败')
  } finally {
    protectionLoading.value = false
  }
}

const getDeviceIcon = (deviceType) => {
  const iconMap = {
    'desktop': 'Monitor',
    'mobile': 'Iphone',
    'tablet': 'Tablet',
    'unknown': 'Monitor'
  }
  return iconMap[deviceType] || 'Monitor'
}

const removeDevice = async (deviceId) => {
  try {
    await ElMessageBox.confirm('确定要移除此设备吗？', '确认操作', {
      type: 'warning'
    })

    const response = await request.delete(`/api/user/device/${deviceId}`)
    if (response.data.code === 200) {
      ElMessage.success('设备已移除')
      refreshDevices()
    } else {
      ElMessage.error(response.data.message || '移除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

const refreshDevices = async () => {
  try {
    const response = await request.get('/api/user/devices')
    if (response.data.code === 200) {
      loginDevices.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
  }
}

const loadSecurityLogs = async () => {
  try {
    const response = await request.get('/api/user/security-logs')
    if (response.data.code === 200) {
      securityLogs.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取安全日志失败:', error)
  }
}

const getLogType = (type) => {
  const typeMap = {
    'login': 'success',
    'logout': 'info',
    'password_change': 'warning',
    'security_alert': 'danger'
  }
  return typeMap[type] || 'info'
}

const loadSecuritySettings = async () => {
  try {
    const response = await request.get('/api/user/security-settings')
    if (response.data.code === 200) {
      Object.assign(securitySettings, response.data.data)
    }
  } catch (error) {
    console.error('获取安全设置失败:', error)
  }
}

// 生命周期
onMounted(() => {
  loadSecuritySettings()
  refreshDevices()
  loadSecurityLogs()
})
</script>

<style scoped>
.security-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.security-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}

.password-form {
  max-width: 500px;
}

.security-settings {
  display: flex;
  flex-direction: column;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.setting-info h4 {
  margin: 0 0 4px 0;
  color: #303133;
}

.setting-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.devices-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.device-icon {
  color: #909399;
}

.device-info {
  flex: 1;
}

.device-info h4 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 14px;
}

.device-info p {
  margin: 0 0 2px 0;
  color: #909399;
  font-size: 12px;
}

.device-details {
  color: #c0c4cc !important;
  font-size: 11px !important;
}

.log-content h4 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 14px;
}

.log-content p {
  margin: 0 0 4px 0;
  color: #606266;
  font-size: 13px;
}

.log-ip {
  color: #909399;
  font-size: 12px;
}

.empty-devices, .empty-logs {
  text-align: center;
  padding: 40px 0;
}

@media (max-width: 768px) {
  .security-container {
    padding: 10px;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .device-item {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
