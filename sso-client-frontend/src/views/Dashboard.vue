<template>
  <div class="dashboard-container">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <h1 class="page-title">
        <el-icon><Odometer /></el-icon>
        仪表板
      </h1>
      <p class="page-subtitle">欢迎使用企业 SSO 业务系统</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon primary">
                <el-icon><User /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ userStats.total }}</h3>
                <p>总用户数</p>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon success">
                <el-icon><UserFilled /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ userStats.online }}</h3>
                <p>在线用户</p>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon warning">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ loginStats.today }}</h3>
                <p>今日登录</p>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon danger">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ securityStats.alerts }}</h3>
                <p>安全告警</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>登录趋势</span>
                <el-button type="text">查看详情</el-button>
              </div>
            </template>
            <div class="chart-placeholder">
              <el-icon size="48" color="#ddd"><TrendCharts /></el-icon>
              <p>登录趋势图表</p>
              <el-text type="info">图表功能开发中...</el-text>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>用户分布</span>
                <el-button type="text">查看详情</el-button>
              </div>
            </template>
            <div class="chart-placeholder">
              <el-icon size="48" color="#ddd"><PieChart /></el-icon>
              <p>用户分布图表</p>
              <el-text type="info">图表功能开发中...</el-text>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>快速操作</span>
          </div>
        </template>
        <div class="actions-grid">
          <el-button type="primary" :icon="Plus" @click="handleAction('addUser')">
            添加用户
          </el-button>
          <el-button type="success" :icon="View" @click="handleAction('viewLogs')">
            查看日志
          </el-button>
          <el-button type="warning" :icon="Setting" @click="handleAction('systemConfig')">
            系统配置
          </el-button>
          <el-button type="info" :icon="Download" @click="handleAction('exportData')">
            导出数据
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activities">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>最近活动</span>
            <el-button type="text">查看全部</el-button>
          </div>
        </template>
        <el-timeline>
          <el-timeline-item
            v-for="activity in recentActivities"
            :key="activity.id"
            :timestamp="activity.time"
            :type="activity.type"
          >
            {{ activity.description }}
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import {
  Odometer,
  User,
  UserFilled,
  Document,
  Warning,
  TrendCharts,
  PieChart,
  Plus,
  View,
  Setting,
  Download
} from '@element-plus/icons-vue'

// 状态管理
const authStore = useAuthStore()

// 统计数据
const userStats = ref({
  total: 1248,
  online: 156
})

const loginStats = ref({
  today: 89
})

const securityStats = ref({
  alerts: 3
})

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    description: '用户 admin 登录系统',
    time: '2025-01-19 14:30',
    type: 'primary'
  },
  {
    id: 2,
    description: '新用户 john_doe 注册成功',
    time: '2025-01-19 14:15',
    type: 'success'
  },
  {
    id: 3,
    description: '检测到异常登录尝试',
    time: '2025-01-19 14:00',
    type: 'warning'
  },
  {
    id: 4,
    description: '系统配置已更新',
    time: '2025-01-19 13:45',
    type: 'info'
  }
])

// 方法
const handleAction = (action) => {
  switch (action) {
    case 'addUser':
      ElMessage.info('跳转到用户管理页面...')
      break
    case 'viewLogs':
      ElMessage.info('跳转到日志查看页面...')
      break
    case 'systemConfig':
      ElMessage.info('跳转到系统配置页面...')
      break
    case 'exportData':
      ElMessage.info('开始导出数据...')
      break
    default:
      ElMessage.info('功能开发中...')
  }
}

// 加载数据
const loadDashboardData = async () => {
  try {
    // 这里可以调用 API 获取真实数据
    console.log('加载仪表板数据...')
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 生命周期
onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.dashboard-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.page-subtitle {
  color: #7f8c8d;
  margin: 0;
}

.stats-grid {
  margin-bottom: 24px;
}

.stats-card {
  height: 120px;
}

.stats-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stats-icon.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.success {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.warning {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stats-icon.danger {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa726 100%);
}

.stats-info h3 {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #2c3e50;
}

.stats-info p {
  color: #7f8c8d;
  margin: 0;
  font-size: 14px;
}

.charts-section {
  margin-bottom: 24px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #bbb;
}

.chart-placeholder p {
  margin: 16px 0 8px 0;
  font-size: 16px;
}

.quick-actions {
  margin-bottom: 24px;
}

.actions-grid {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.recent-activities {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }
  
  .actions-grid {
    flex-direction: column;
  }
  
  .actions-grid .el-button {
    width: 100%;
  }
}
</style>
