<template>
  <div class="admin-dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">系统管理控制台</h1>
      <p class="page-subtitle">欢迎回来，系统管理员！今天是 {{ currentDate }}</p>
    </div>

    <!-- 页面内容 -->
    <div class="page-content">

    <!-- 系统概览卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon users">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ systemStats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon online">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ systemStats.onlineUsers }}</div>
              <div class="stat-label">在线用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon performance">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ systemStats.systemLoad }}%</div>
              <div class="stat-label">系统负载</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon security">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ systemStats.securityScore }}</div>
              <div class="stat-label">安全评分</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 功能模块 -->
    <el-row :gutter="20" class="modules-row">
      <el-col :span="16">
        <el-card class="module-card">
          <template #header>
            <div class="card-header">
              <span>系统管理</span>
            </div>
          </template>
          <el-row :gutter="16">
            <el-col :span="8">
              <div class="module-item" @click="navigateTo('/system/user')">
                <div class="module-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="module-content">
                  <h4>用户管理</h4>
                  <p>管理系统用户、权限分配</p>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="module-item" @click="navigateTo('/system/role')">
                <div class="module-icon">
                  <el-icon><Setting /></el-icon>
                </div>
                <div class="module-content">
                  <h4>角色管理</h4>
                  <p>配置用户角色和权限</p>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="module-item" @click="navigateTo('/system/menu')">
                <div class="module-icon">
                  <el-icon><Menu /></el-icon>
                </div>
                <div class="module-content">
                  <h4>菜单管理</h4>
                  <p>配置系统菜单结构</p>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="module-card">
          <template #header>
            <div class="card-header">
              <span>系统监控</span>
            </div>
          </template>
          <div class="monitor-list">
            <div class="monitor-item" @click="navigateTo('/monitor/online')">
              <el-icon><Monitor /></el-icon>
              <span>在线用户</span>
            </div>
            <div class="monitor-item" @click="navigateTo('/monitor/server')">
              <el-icon><Cpu /></el-icon>
              <span>服务监控</span>
            </div>
            <div class="monitor-item" @click="navigateTo('/monitor/loginlog')">
              <el-icon><Document /></el-icon>
              <span>登录日志</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统信息 -->
    <el-row :gutter="20" class="info-row">
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>服务器状态</span>
            </div>
          </template>
          <div class="info-list">
            <div class="info-item">
              <span class="label">CPU使用率:</span>
              <span class="value">{{ serverInfo.cpuUsage }}%</span>
            </div>
            <div class="info-item">
              <span class="label">内存使用:</span>
              <span class="value">{{ serverInfo.memoryUsage }}%</span>
            </div>
            <div class="info-item">
              <span class="label">磁盘使用:</span>
              <span class="value">{{ serverInfo.diskUsage }}%</span>
            </div>
            <div class="info-item">
              <span class="label">网络状态:</span>
              <span class="value" :class="serverInfo.networkStatus">{{ serverInfo.networkStatusText }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>应用信息</span>
            </div>
          </template>
          <div class="info-list">
            <div class="info-item">
              <span class="label">版本:</span>
              <span class="value">{{ appInfo.version }}</span>
            </div>
            <div class="info-item">
              <span class="label">运行时间:</span>
              <span class="value">{{ appInfo.uptime }}</span>
            </div>
            <div class="info-item">
              <span class="label">最后更新:</span>
              <span class="value">{{ appInfo.lastUpdate }}</span>
            </div>
            <div class="info-item">
              <span class="label">状态:</span>
              <span class="value success">运行正常</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  User, Monitor, Setting, Menu, Cpu, Document,
  TrendCharts, ArrowUp, InfoFilled, Check, Clock,
  CircleCloseFilled, WarningFilled
} from '@element-plus/icons-vue'

const router = useRouter()

// 当前日期
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 系统统计
const systemStats = ref({
  totalUsers: 1256,
  newUsers: 89,
  onlineUsers: 342,
  systemLoad: 65,
  securityScore: 92
})

// 服务器信息
const serverInfo = ref({
  cpuUsage: 45,
  memoryUsage: 68,
  diskUsage: 42,
  networkStatus: 'normal',
  networkStatusText: '正常'
})

// 应用信息
const appInfo = ref({
  version: 'v2.1.0',
  uptime: '15天 8小时 32分钟',
  lastUpdate: '2025-08-20 14:30:00'
})

// 最近系统事件
const recentEvents = ref([
  {
    id: 1,
    level: 'info',
    title: '系统备份完成',
    time: '2小时前'
  },
  {
    id: 2,
    level: 'warning',
    title: '磁盘空间使用率超过80%',
    time: '4小时前'
  },
  {
    id: 3,
    level: 'info',
    title: '新用户注册：user_1234',
    time: '6小时前'
  },
  {
    id: 4,
    level: 'error',
    title: '数据库连接超时',
    time: '8小时前'
  }
])

// 导航方法
const navigateTo = (path) => {
  router.push(path)
}

// 模拟数据更新
onMounted(() => {
  console.log('管理员仪表板已加载')
  
  // 模拟实时数据更新
  setInterval(() => {
    systemStats.value.onlineUsers = Math.floor(Math.random() * 100) + 300
    systemStats.value.systemLoad = Math.floor(Math.random() * 30) + 50
  }, 30000)
})
</script>

<style scoped>
.admin-dashboard {
  padding: 0;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}

/* 页面标题 */
.page-header {
  padding: 20px 20px 0 20px;
  margin-bottom: 20px;
}

/* 页面内容 */
.page-content {
  padding: 0 20px 20px 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-subtitle {
  color: #606266;
  margin: 0;
  font-size: 14px;
}

/* 统计卡片行 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border: 1px solid #e4e7ed;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.stat-icon.users { background: #409eff; }
.stat-icon.online { background: #67c23a; }
.stat-icon.performance { background: #e6a23c; }
.stat-icon.security { background: #f56c6c; }

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

/* 功能模块行 */
.modules-row {
  margin-bottom: 20px;
}

.module-card {
  border: 1px solid #e4e7ed;
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.module-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 12px;
}

.module-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.module-item:last-child {
  margin-bottom: 0;
}

.module-icon {
  width: 40px;
  height: 40px;
  background: #409eff;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.module-content h4 {
  margin: 0 0 4px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 500;
}

.module-content p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

/* 监控列表 */
.monitor-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.monitor-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.monitor-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.monitor-item .el-icon {
  color: #409eff;
  font-size: 16px;
}

.monitor-item span {
  color: #303133;
  font-size: 14px;
}

/* 信息卡片行 */
.info-row {
  margin-bottom: 20px;
}

.info-card {
  border: 1px solid #e4e7ed;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: #606266;
  font-size: 14px;
}

.info-item .value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.value.success { color: #67c23a; }
.value.warning { color: #e6a23c; }
.value.error { color: #f56c6c; }

/* 响应式设计 */
@media (max-width: 1200px) {
  .modules-row .el-col:first-child {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .admin-dashboard {
    padding: 16px;
  }
  
  .stats-row .el-col {
    margin-bottom: 16px;
  }
  
  .modules-row .el-col {
    margin-bottom: 16px;
  }
  
  .info-row .el-col {
    margin-bottom: 16px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .stat-number {
    font-size: 20px;
  }
}
</style>
