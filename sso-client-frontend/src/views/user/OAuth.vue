<template>
  <div class="oauth-container">
    <el-card class="oauth-card">
      <template #header>
        <div class="card-header">
          <el-icon><Link /></el-icon>
          <span>第三方账号绑定</span>
        </div>
      </template>

      <div class="oauth-content">
        <div class="oauth-description">
          <el-alert
            title="第三方账号绑定"
            description="绑定第三方账号后，您可以使用这些账号快速登录系统，提升使用体验。"
            type="info"
            :closable="false"
            show-icon
          />
        </div>

        <div class="oauth-providers">
          <div
            v-for="provider in oauthProviders"
            :key="provider.type"
            class="provider-item"
          >
            <div class="provider-info">
              <div class="provider-icon">
                <el-icon :size="32" :color="provider.color">
                  <component :is="provider.icon" />
                </el-icon>
              </div>
              <div class="provider-details">
                <h4>{{ provider.name }}</h4>
                <p>{{ provider.description }}</p>
                <div v-if="provider.bound" class="bound-info">
                  <el-tag type="success" size="small">
                    <el-icon><Check /></el-icon>
                    已绑定
                  </el-tag>
                  <span class="bound-account">{{ provider.boundAccount }}</span>
                </div>
                <div v-else class="unbound-info">
                  <el-tag type="info" size="small">未绑定</el-tag>
                </div>
              </div>
            </div>
            <div class="provider-actions">
              <el-button
                v-if="!provider.bound"
                type="primary"
                @click="bindProvider(provider.type)"
                :loading="provider.loading"
              >
                <el-icon><Plus /></el-icon>
                绑定
              </el-button>
              <el-button
                v-else
                type="danger"
                @click="unbindProvider(provider.type)"
                :loading="provider.loading"
              >
                <el-icon><Close /></el-icon>
                解绑
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 绑定记录 -->
    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <el-icon><Clock /></el-icon>
          <span>绑定记录</span>
          <el-button type="text" size="small" @click="loadBindingHistory" style="margin-left: auto;">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-timeline>
        <el-timeline-item
          v-for="record in bindingHistory"
          :key="record.id"
          :timestamp="record.time"
          :type="record.type === 'bind' ? 'success' : 'warning'"
        >
          <div class="history-content">
            <h4>
              {{ record.type === 'bind' ? '绑定' : '解绑' }} {{ record.providerName }}
            </h4>
            <p>{{ record.description }}</p>
            <span class="history-ip">IP: {{ record.ipAddress }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>

      <div v-if="bindingHistory.length === 0" class="empty-history">
        <el-empty description="暂无绑定记录" :image-size="80" />
      </div>
    </el-card>

    <!-- 安全提示 -->
    <el-card class="tips-card">
      <template #header>
        <div class="card-header">
          <el-icon><Warning /></el-icon>
          <span>安全提示</span>
        </div>
      </template>

      <div class="security-tips">
        <el-alert
          title="安全建议"
          type="warning"
          :closable="false"
          show-icon
        >
          <ul class="tips-list">
            <li>请确保您的第三方账号安全，避免被他人恶意使用</li>
            <li>如果发现异常登录，请及时解绑相关账号并修改密码</li>
            <li>建议定期检查绑定的第三方账号，及时清理不需要的绑定</li>
            <li>绑定的第三方账号仅用于登录验证，不会获取您的隐私信息</li>
          </ul>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Link, Check, Plus, Close, Clock, Refresh, Warning 
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const oauthProviders = ref([
  {
    type: 'wechat',
    name: '微信',
    description: '使用微信账号快速登录',
    icon: 'ChatDotRound',
    color: '#07c160',
    bound: false,
    boundAccount: '',
    loading: false
  },
  {
    type: 'qq',
    name: 'QQ',
    description: '使用QQ账号快速登录',
    icon: 'ChatRound',
    color: '#12b7f5',
    bound: false,
    boundAccount: '',
    loading: false
  },
  {
    type: 'weibo',
    name: '微博',
    description: '使用微博账号快速登录',
    icon: 'Share',
    color: '#e6162d',
    bound: false,
    boundAccount: '',
    loading: false
  },
  {
    type: 'github',
    name: 'GitHub',
    description: '使用GitHub账号快速登录',
    icon: 'Platform',
    color: '#24292e',
    bound: false,
    boundAccount: '',
    loading: false
  },
  {
    type: 'google',
    name: 'Google',
    description: '使用Google账号快速登录',
    icon: 'Search',
    color: '#4285f4',
    bound: false,
    boundAccount: '',
    loading: false
  },
  {
    type: 'dingtalk',
    name: '钉钉',
    description: '使用钉钉账号快速登录',
    icon: 'Message',
    color: '#0089ff',
    bound: false,
    boundAccount: '',
    loading: false
  }
])

const bindingHistory = ref([])

// 方法
const bindProvider = async (providerType) => {
  const provider = oauthProviders.value.find(p => p.type === providerType)
  if (!provider) return

  try {
    provider.loading = true

    // 模拟绑定流程
    await ElMessageBox.confirm(
      `确定要绑定${provider.name}账号吗？这将打开新窗口进行授权。`,
      '确认绑定',
      {
        type: 'info'
      }
    )

    // 实际项目中这里应该打开OAuth授权窗口
    // window.open(`/api/oauth/${providerType}/authorize`, '_blank')
    
    // 模拟绑定成功
    setTimeout(() => {
      provider.bound = true
      provider.boundAccount = `${provider.name}用户_${Math.random().toString(36).substr(2, 8)}`
      ElMessage.success(`${provider.name}账号绑定成功`)
      loadBindingHistory()
    }, 2000)

  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${provider.name}账号绑定失败`)
    }
  } finally {
    provider.loading = false
  }
}

const unbindProvider = async (providerType) => {
  const provider = oauthProviders.value.find(p => p.type === providerType)
  if (!provider) return

  try {
    await ElMessageBox.confirm(
      `确定要解绑${provider.name}账号吗？解绑后将无法使用该账号登录。`,
      '确认解绑',
      {
        type: 'warning'
      }
    )

    provider.loading = true

    const response = await request.delete(`/api/oauth/${providerType}/unbind`)
    if (response.data.code === 200) {
      provider.bound = false
      provider.boundAccount = ''
      ElMessage.success(`${provider.name}账号解绑成功`)
      loadBindingHistory()
    } else {
      ElMessage.error(response.data.message || `${provider.name}账号解绑失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${provider.name}账号解绑失败`)
    }
  } finally {
    provider.loading = false
  }
}

const loadOAuthStatus = async () => {
  try {
    const response = await request.get('/api/oauth/status')
    if (response.data.code === 200) {
      const statusData = response.data.data || {}
      
      oauthProviders.value.forEach(provider => {
        const status = statusData[provider.type]
        if (status) {
          provider.bound = status.bound
          provider.boundAccount = status.account || ''
        }
      })
    }
  } catch (error) {
    console.error('获取OAuth状态失败:', error)
  }
}

const loadBindingHistory = async () => {
  try {
    const response = await request.get('/api/oauth/history')
    if (response.data.code === 200) {
      bindingHistory.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取绑定记录失败:', error)
  }
}

// 生命周期
onMounted(() => {
  loadOAuthStatus()
  loadBindingHistory()
})
</script>

<style scoped>
.oauth-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.oauth-card, .history-card, .tips-card {
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

.oauth-description {
  margin-bottom: 24px;
}

.oauth-providers {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.provider-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.provider-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.provider-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.provider-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background-color: #f8f9fa;
}

.provider-details h4 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 16px;
}

.provider-details p {
  margin: 0 0 8px 0;
  color: #909399;
  font-size: 14px;
}

.bound-info, .unbound-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bound-account {
  color: #67c23a;
  font-size: 12px;
}

.history-content h4 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 14px;
}

.history-content p {
  margin: 0 0 4px 0;
  color: #606266;
  font-size: 13px;
}

.history-ip {
  color: #909399;
  font-size: 12px;
}

.security-tips {
  margin-top: 16px;
}

.tips-list {
  margin: 12px 0 0 0;
  padding-left: 20px;
}

.tips-list li {
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.empty-history {
  text-align: center;
  padding: 40px 0;
}

@media (max-width: 768px) {
  .oauth-container {
    padding: 10px;
  }
  
  .provider-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .provider-info {
    width: 100%;
  }
  
  .provider-actions {
    width: 100%;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
