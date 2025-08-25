<template>
  <div class="admin-dashboard">
    <!-- 系统概览头部 -->
    <div class="dashboard-header">
      <div class="header-content">
        <div class="header-title">
          <h1>系统管理控制台</h1>
          <p>欢迎回来，系统管理员！今天是 {{ currentDate }}</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="navigateTo('/system/user')">
            <el-icon><User /></el-icon>
            用户管理
          </el-button>
          <el-button type="success" @click="navigateTo('/monitor/online')">
            <el-icon><Monitor /></el-icon>
            系统监控
          </el-button>
          <el-button type="warning" @click="navigateTo('/system/role')">
            <el-icon><Setting /></el-icon>
            角色管理
          </el-button>
        </div>
      </div>
    </div>

    <!-- 系统统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon users">
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ systemStats.totalUsers }}</div>
            <div class="stat-label">总用户数</div>
            <div class="stat-trend positive">
              <el-icon><ArrowUp /></el-icon>
              +{{ systemStats.newUsers }} 本月新增
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon online">
            <el-icon size="32"><Monitor /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ systemStats.onlineUsers }}</div>
            <div class="stat-label">在线用户</div>
            <div class="stat-trend">
              <el-icon><Clock /></el-icon>
              实时更新
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon performance">
            <el-icon size="32"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ systemStats.systemLoad }}%</div>
            <div class="stat-label">系统负载</div>
            <div class="stat-trend" :class="systemStats.systemLoad > 80 ? 'warning' : 'normal'">
              <el-icon><InfoFilled /></el-icon>
              {{ systemStats.systemLoad > 80 ? '较高' : '正常' }}
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon security">
            <el-icon size="32"><Lock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ systemStats.securityScore }}</div>
            <div class="stat-label">安全评分</div>
            <div class="stat-trend" :class="systemStats.securityScore > 80 ? 'positive' : 'warning'">
              <el-icon><Check /></el-icon>
              {{ systemStats.securityScore > 80 ? '优秀' : '需关注' }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧快速操作 -->
      <div class="quick-actions">
        <h3 class="section-title">快速操作</h3>
        <div class="actions-grid">
          <div class="action-card" @click="navigateTo('/system/user')">
            <div class="action-icon">
              <el-icon size="24"><User /></el-icon>
            </div>
            <div class="action-content">
              <h4>用户管理</h4>
              <p>管理系统用户、权限分配</p>
            </div>
          </div>

          <div class="action-card" @click="navigateTo('/system/role')">
            <div class="action-icon">
              <el-icon size="24"><Setting /></el-icon>
            </div>
            <div class="action-content">
              <h4>角色管理</h4>
              <p>配置用户角色和权限</p>
            </div>
          </div>

          <div class="action-card" @click="navigateTo('/system/menu')">
            <div class="action-icon">
              <el-icon size="24"><Menu /></el-icon>
            </div>
            <div class="action-content">
              <h4>菜单管理</h4>
              <p>配置系统菜单结构</p>
            </div>
          </div>

          <div class="action-card" @click="navigateTo('/monitor/online')">
            <div class="action-icon">
              <el-icon size="24"><Monitor /></el-icon>
            </div>
            <div class="action-content">
              <h4>在线监控</h4>
              <p>查看在线用户状态</p>
            </div>
          </div>

          <div class="action-card" @click="navigateTo('/monitor/server')">
            <div class="action-icon">
              <el-icon size="24"><Cpu /></el-icon>
            </div>
            <div class="action-content">
              <h4>服务监控</h4>
              <p>监控系统服务状态</p>
            </div>
          </div>

          <div class="action-card" @click="navigateTo('/monitor/loginlog')">
            <div class="action-icon">
              <el-icon size="24"><Document /></el-icon>
            </div>
            <div class="action-content">
              <h4>登录日志</h4>
              <p>查看用户登录记录</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧系统信息 -->
      <div class="system-info">
        <h3 class="section-title">系统信息</h3>
        <div class="info-cards">
          <div class="info-card">
            <h4>服务器状态</h4>
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

          <div class="info-card">
            <h4>应用信息</h4>
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
        </div>

        <!-- 最近系统事件 -->
        <div class="recent-events">
          <h4>最近系统事件</h4>
          <div class="event-list">
            <div v-for="event in recentEvents" :key="event.id" class="event-item" :class="event.level">
              <div class="event-icon">
                <el-icon v-if="event.level === 'error'"><CircleCloseFilled /></el-icon>
                <el-icon v-else-if="event.level === 'warning'"><WarningFilled /></el-icon>
                <el-icon v-else><InfoFilled /></el-icon>
              </div>
              <div class="event-content">
                <div class="event-title">{{ event.title }}</div>
                <div class="event-time">{{ event.time }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部图表区域 -->
    <div class="charts-section">
      <h3 class="section-title">系统监控图表</h3>
      <div class="charts-grid">
        <div class="chart-card">
          <h4>用户活跃度趋势</h4>
          <div class="chart-placeholder">
            <el-icon size="48" color="#909399"><TrendCharts /></el-icon>
            <p>用户活跃度趋势图表</p>
          </div>
        </div>
        
        <div class="chart-card">
          <h4>系统性能监控</h4>
          <div class="chart-placeholder">
            <el-icon size="48" color="#909399"><Monitor /></el-icon>
            <p>系统性能监控图表</p>
          </div>
        </div>
      </div>
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
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 仪表板头部 */
.dashboard-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  color: white;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.header-title p {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 16px;
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.users { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.online { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.performance { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.security { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-label {
  color: #7f8c8d;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #7f8c8d;
}

.stat-trend.positive { color: #67c23a; }
.stat-trend.warning { color: #e6a23c; }
.stat-trend.normal { color: #409eff; }

/* 主要内容区域 */
.main-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #e1e8ed;
}

/* 快速操作 */
.quick-actions {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.action-card {
  padding: 20px;
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}

.action-card:hover {
  border-color: #667eea;
  background: #f8f9ff;
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin: 0 auto 16px;
}

.action-content h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 16px;
}

.action-content p {
  margin: 0;
  color: #7f8c8d;
  font-size: 14px;
}

/* 系统信息 */
.system-info {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.info-cards {
  margin-bottom: 24px;
}

.info-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.info-card h4 {
  margin: 0 0 12px 0;
  color: #2c3e50;
  font-size: 14px;
  font-weight: 600;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item .label {
  color: #7f8c8d;
}

.info-item .value {
  font-weight: 500;
  color: #2c3e50;
}

.value.success { color: #67c23a; }
.value.warning { color: #e6a23c; }
.value.error { color: #f56c6c; }

/* 最近事件 */
.recent-events h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 14px;
  font-weight: 600;
}

.event-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.event-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f8f9fa;
}

.event-item.info { border-left: 4px solid #409eff; }
.event-item.warning { border-left: 4px solid #e6a23c; }
.event-item.error { border-left: 4px solid #f56c6c; }

.event-icon {
  color: #7f8c8d;
}

.event-content {
  flex: 1;
}

.event-title {
  font-size: 14px;
  color: #2c3e50;
  margin-bottom: 4px;
}

.event-time {
  font-size: 12px;
  color: #7f8c8d;
}

/* 图表区域 */
.charts-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.chart-card {
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  padding: 20px;
}

.chart-card h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: #f8f9fa;
  border-radius: 8px;
}

.chart-placeholder p {
  margin: 16px 0 0 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .admin-dashboard {
    padding: 16px;
  }
  
  .dashboard-header {
    padding: 24px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .header-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
