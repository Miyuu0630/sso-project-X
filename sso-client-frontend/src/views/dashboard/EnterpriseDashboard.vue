<template>
  <div class="enterprise-dashboard">
    <!-- 企业概览头部 -->
    <div class="dashboard-header">
      <div class="header-content">
        <div class="enterprise-info">
          <div class="enterprise-logo">
            <el-icon size="32"><OfficeBuilding /></el-icon>
          </div>
          <div class="enterprise-details">
            <h1>{{ enterpriseInfo.name }} - 企业控制台</h1>
            <p>欢迎回来，{{ userInfo.nickname }}！今天是 {{ currentDate }}</p>
            <div class="enterprise-status">
              <span class="status-badge active">
                <el-icon><Check /></el-icon>
                企业认证通过
              </span>
              <span class="enterprise-type">{{ enterpriseInfo.type }}</span>
              <span class="member-count">成员：{{ enterpriseInfo.memberCount }}人</span>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="navigateTo('/enterprise/info')">
            <el-icon><Edit /></el-icon>
            企业信息
          </el-button>
          <el-button type="success" @click="navigateTo('/enterprise/member')">
            <el-icon><User /></el-icon>
            成员管理
          </el-button>
          <el-button type="warning" @click="navigateTo('/enterprise/project')">
            <el-icon><Folder /></el-icon>
            项目管理
          </el-button>
        </div>
      </div>
    </div>

    <!-- 企业统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon members">
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ enterpriseStats.totalMembers }}</div>
            <div class="stat-label">总成员数</div>
            <div class="stat-trend positive">
              <el-icon><ArrowUp /></el-icon>
              +{{ enterpriseStats.newMembers }} 本月新增
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon projects">
            <el-icon size="32"><Folder /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ enterpriseStats.activeProjects }}</div>
            <div class="stat-label">活跃项目</div>
            <div class="stat-trend positive">
              <el-icon><TrendCharts /></el-icon>
              {{ enterpriseStats.projectProgress }}% 完成度
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon revenue">
            <el-icon size="32"><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">¥{{ enterpriseStats.monthlyRevenue }}</div>
            <div class="stat-label">月度营收</div>
            <div class="stat-trend" :class="enterpriseStats.revenueGrowth > 0 ? 'positive' : 'warning'">
              <el-icon><TrendCharts /></el-icon>
              {{ enterpriseStats.revenueGrowth > 0 ? '+' : '' }}{{ enterpriseStats.revenueGrowth }}% 较上月
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon efficiency">
            <el-icon size="32"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ enterpriseStats.teamEfficiency }}%</div>
            <div class="stat-label">团队效率</div>
            <div class="stat-trend" :class="enterpriseStats.teamEfficiency > 80 ? 'positive' : 'warning'">
              <el-icon><Check /></el-icon>
              {{ enterpriseStats.teamEfficiency > 80 ? '优秀' : '需提升' }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧业务概览 -->
      <div class="business-overview">
        <h3 class="section-title">业务概览</h3>
        <div class="overview-grid">
          <div class="overview-card" @click="navigateTo('/enterprise/member')">
            <div class="overview-icon">
              <el-icon size="24"><User /></el-icon>
            </div>
            <div class="overview-content">
              <h4>成员管理</h4>
              <p>管理企业成员、权限分配</p>
              <div class="overview-stats">
                <span>在线：{{ enterpriseStats.onlineMembers }}</span>
                <span>新增：{{ enterpriseStats.newMembers }}</span>
              </div>
            </div>
          </div>

          <div class="overview-card" @click="navigateTo('/enterprise/project')">
            <div class="overview-icon">
              <el-icon size="24"><Folder /></el-icon>
            </div>
            <div class="overview-content">
              <h4>项目管理</h4>
              <p>项目进度跟踪、任务分配</p>
              <div class="overview-stats">
                <span>进行中：{{ enterpriseStats.activeProjects }}</span>
                <span>已完成：{{ enterpriseStats.completedProjects }}</span>
              </div>
            </div>
          </div>

          <div class="overview-card" @click="navigateTo('/enterprise/finance')">
            <div class="overview-icon">
              <el-icon size="24"><Money /></el-icon>
            </div>
            <div class="overview-content">
              <h4>财务管理</h4>
              <p>收支统计、成本分析</p>
              <div class="overview-stats">
                <span>收入：¥{{ enterpriseStats.monthlyRevenue }}</span>
                <span>支出：¥{{ enterpriseStats.monthlyExpense }}</span>
              </div>
            </div>
          </div>

          <div class="overview-card" @click="navigateTo('/enterprise/analytics')">
            <div class="overview-icon">
              <el-icon size="24"><TrendCharts /></el-icon>
            </div>
            <div class="overview-content">
              <h4>数据分析</h4>
              <p>业务数据、趋势分析</p>
              <div class="overview-stats">
                <span>增长率：{{ enterpriseStats.revenueGrowth }}%</span>
                <span>效率：{{ enterpriseStats.teamEfficiency }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧企业信息和活动 -->
      <div class="enterprise-sidebar">
        <!-- 企业信息卡片 -->
        <div class="info-card">
          <h3 class="section-title">企业信息</h3>
          <div class="info-content">
            <div class="info-item">
              <span class="label">企业名称:</span>
              <span class="value">{{ enterpriseInfo.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">企业类型:</span>
              <span class="value">{{ enterpriseInfo.type }}</span>
            </div>
            <div class="info-item">
              <span class="label">成立时间:</span>
              <span class="value">{{ enterpriseInfo.establishDate }}</span>
            </div>
            <div class="info-item">
              <span class="label">企业规模:</span>
              <span class="value">{{ enterpriseInfo.scale }}</span>
            </div>
            <div class="info-item">
              <span class="label">认证状态:</span>
              <span class="value status-verified">已认证</span>
            </div>
            <div class="info-item">
              <span class="label">企业地址:</span>
              <span class="value">{{ enterpriseInfo.address }}</span>
            </div>
          </div>
        </div>

        <!-- 最近活动 -->
        <div class="recent-activities">
          <h4>企业动态</h4>
          <div class="activity-list">
            <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
              <div class="activity-icon" :class="activity.type">
                <el-icon v-if="activity.type === 'member'"><User /></el-icon>
                <el-icon v-else-if="activity.type === 'project'"><Folder /></el-icon>
                <el-icon v-else-if="activity.type === 'finance'"><Money /></el-icon>
                <el-icon v-else><InfoFilled /></el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-time">{{ activity.time }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 团队协作 -->
        <div class="team-collaboration">
          <h4>团队协作</h4>
          <div class="collaboration-stats">
            <div class="collab-stat">
              <div class="stat-number">{{ teamStats.onlineMembers }}</div>
              <div class="stat-label">在线成员</div>
            </div>
            <div class="collab-stat">
              <div class="stat-number">{{ teamStats.activeTasks }}</div>
              <div class="stat-label">进行中任务</div>
            </div>
            <div class="collab-stat">
              <div class="stat-number">{{ teamStats.completedTasks }}</div>
              <div class="stat-label">已完成任务</div>
            </div>
          </div>
          <div class="team-actions">
            <el-button size="small" @click="navigateTo('/enterprise/chat')">
              <el-icon><ChatDotRound /></el-icon>
              团队沟通
            </el-button>
            <el-button size="small" @click="navigateTo('/enterprise/calendar')">
              <el-icon><Calendar /></el-icon>
              日程安排
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部图表区域 -->
    <div class="charts-section">
      <h3 class="section-title">业务数据图表</h3>
      <div class="charts-grid">
        <div class="chart-card">
          <h4>营收趋势分析</h4>
          <div class="chart-placeholder">
            <el-icon size="48" color="#909399"><TrendCharts /></el-icon>
            <p>月度营收趋势图表</p>
          </div>
        </div>
        
        <div class="chart-card">
          <h4>项目进度统计</h4>
          <div class="chart-placeholder">
            <el-icon size="48" color="#909399"><Folder /></el-icon>
            <p>项目进度统计图表</p>
          </div>
        </div>
        
        <div class="chart-card">
          <h4>团队效率分析</h4>
          <div class="chart-placeholder">
            <el-icon size="48" color="#909399"><User /></el-icon>
            <p>团队效率分析图表</p>
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
  OfficeBuilding, Edit, User, Folder, Money, TrendCharts,
  Check, ArrowUp, InfoFilled, ChatDotRound, Calendar
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

// 用户信息
const userInfo = ref({
  nickname: '企业用户'
})

// 企业信息
const enterpriseInfo = ref({
  name: '科技创新有限公司',
  type: '科技企业',
  establishDate: '2020-03-15',
  scale: '100-499人',
  memberCount: 156,
  address: '北京市朝阳区科技园区'
})

// 企业统计
const enterpriseStats = ref({
  totalMembers: 156,
  newMembers: 12,
  onlineMembers: 89,
  activeProjects: 8,
  completedProjects: 15,
  projectProgress: 75,
  monthlyRevenue: '2,580,000',
  monthlyExpense: '1,850,000',
  revenueGrowth: 12.5,
  teamEfficiency: 87
})

// 团队统计
const teamStats = ref({
  onlineMembers: 89,
  activeTasks: 45,
  completedTasks: 128
})

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    type: 'member',
    title: '新成员张三加入企业',
    time: '2小时前'
  },
  {
    id: 2,
    type: 'project',
    title: '项目A完成里程碑1',
    time: '4小时前'
  },
  {
    id: 3,
    type: 'finance',
    title: '月度财务报表生成',
    time: '1天前'
  },
  {
    id: 4,
    type: 'info',
    title: '企业认证状态更新',
    time: '2天前'
  }
])

// 导航方法
const navigateTo = (path) => {
  router.push(path)
}

// 模拟数据更新
onMounted(() => {
  console.log('企业用户仪表板已加载')
  
  // 模拟实时数据更新
  setInterval(() => {
    enterpriseStats.value.onlineMembers = Math.floor(Math.random() * 30) + 70
    enterpriseStats.value.teamEfficiency = Math.floor(Math.random() * 20) + 75
  }, 60000)
})
</script>

<style scoped>
.enterprise-dashboard {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 仪表板头部 */
.dashboard-header {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
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

.enterprise-info {
  flex: 1;
}

.enterprise-logo {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  border: 3px solid rgba(255, 255, 255, 0.3);
  margin-bottom: 20px;
}

.enterprise-details h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.enterprise-details p {
  margin: 0 0 16px 0;
  opacity: 0.9;
  font-size: 16px;
}

.enterprise-status {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.status-badge.active {
  background: rgba(103, 194, 58, 0.2);
  color: #67c23a;
}

.enterprise-type {
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
}

.member-count {
  font-size: 14px;
  opacity: 0.8;
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

.stat-icon.members { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.projects { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.revenue { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.efficiency { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

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

/* 业务概览 */
.business-overview {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.overview-card {
  padding: 24px;
  border: 1px solid #e1e8ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.overview-card:hover {
  border-color: #2c3e50;
  background: #f8f9ff;
  transform: translateY(-2px);
}

.overview-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 16px;
}

.overview-content h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.overview-content p {
  margin: 0 0 16px 0;
  color: #7f8c8d;
  font-size: 14px;
  line-height: 1.5;
}

.overview-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #2c3e50;
}

.overview-stats span {
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 6px;
}

/* 企业信息侧边栏 */
.enterprise-sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.info-content {
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
  font-size: 14px;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: #7f8c8d;
  font-weight: 500;
}

.info-item .value {
  color: #2c3e50;
  font-weight: 500;
}

.status-verified { color: #67c23a; }

/* 最近活动 */
.recent-activities {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.recent-activities h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f8f9fa;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
}

.activity-icon.member { background: #67c23a; }
.activity-icon.project { background: #409eff; }
.activity-icon.finance { background: #e6a23c; }
.activity-icon.info { background: #909399; }

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  color: #2c3e50;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #7f8c8d;
}

/* 团队协作 */
.team-collaboration {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.team-collaboration h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.collaboration-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.collab-stat {
  text-align: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.collab-stat .stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 4px;
}

.collab-stat .stat-label {
  font-size: 12px;
  color: #7f8c8d;
}

.team-actions {
  display: flex;
  gap: 12px;
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
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
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
  .enterprise-dashboard {
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
  
  .enterprise-status {
    flex-direction: column;
    gap: 12px;
  }
  
  .header-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .overview-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .collaboration-stats {
    grid-template-columns: 1fr;
  }
}
</style>
