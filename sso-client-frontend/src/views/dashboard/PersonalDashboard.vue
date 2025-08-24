<template>
  <div class="personal-dashboard">
    <!-- 个人欢迎头部 -->
    <div class="dashboard-header">
      <div class="header-content">
        <div class="user-info">
          <div class="avatar-section">
            <div class="avatar">
              <el-icon size="32"><User /></el-icon>
            </div>
            <div class="user-details">
              <h1>欢迎回来，{{ userInfo.nickname }}！</h1>
              <p>今天是 {{ currentDate }}，祝您使用愉快！</p>
              <div class="user-status">
                <span class="status-badge online">
                  <el-icon><CircleCheck /></el-icon>
                  在线
                </span>
                <span class="last-login">上次登录：{{ userInfo.lastLoginTime }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="navigateTo('/user/profile')">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
          <el-button type="success" @click="navigateTo('/user/security')">
            <el-icon><Lock /></el-icon>
            安全设置
          </el-button>
          <el-button type="warning" @click="navigateTo('/user/device')">
            <el-icon><Monitor /></el-icon>
            设备管理
          </el-button>
        </div>
      </div>
    </div>

    <!-- 个人统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon profile">
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ personalStats.profileComplete }}%</div>
            <div class="stat-label">资料完整度</div>
            <div class="stat-trend" :class="personalStats.profileComplete > 80 ? 'positive' : 'warning'">
              <el-icon><InfoFilled /></el-icon>
              {{ personalStats.profileComplete > 80 ? '完整' : '需完善' }}
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon activity">
            <el-icon size="32"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ personalStats.loginDays }}</div>
            <div class="stat-label">连续登录天数</div>
            <div class="stat-trend positive">
              <el-icon><TrendCharts /></el-icon>
              +{{ personalStats.streakDays }} 天
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon security">
            <el-icon size="32"><Lock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ personalStats.securityScore }}</div>
            <div class="stat-label">安全评分</div>
            <div class="stat-trend" :class="personalStats.securityScore > 80 ? 'positive' : 'warning'">
              <el-icon><Check /></el-icon>
              {{ personalStats.securityScore > 80 ? '优秀' : '需加强' }}
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon points">
            <el-icon size="32"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ personalStats.points }}</div>
            <div class="stat-label">积分余额</div>
            <div class="stat-trend positive">
              <el-icon><ArrowUp /></el-icon>
              +{{ personalStats.pointsEarned }} 本月获得
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧快捷功能 -->
      <div class="quick-functions">
        <h3 class="section-title">快捷功能</h3>
        <div class="functions-grid">
          <div class="function-card" @click="navigateTo('/user/profile')">
            <div class="function-icon">
              <el-icon size="24"><User /></el-icon>
            </div>
            <div class="function-content">
              <h4>个人资料</h4>
              <p>查看和编辑个人信息</p>
            </div>
          </div>

          <div class="function-card" @click="navigateTo('/user/security')">
            <div class="function-icon">
              <el-icon size="24"><Lock /></el-icon>
            </div>
            <div class="function-content">
              <h4>安全设置</h4>
              <p>密码修改、安全验证</p>
            </div>
          </div>

          <div class="function-card" @click="navigateTo('/user/oauth')">
            <div class="function-icon">
              <el-icon size="24"><Link /></el-icon>
            </div>
            <div class="function-content">
              <h4>第三方绑定</h4>
              <p>绑定微信、QQ等账号</p>
            </div>
          </div>

          <div class="function-card" @click="navigateTo('/user/device')">
            <div class="function-icon">
              <el-icon size="24"><Monitor /></el-icon>
            </div>
            <div class="function-content">
              <h4>设备管理</h4>
              <p>管理登录设备</p>
            </div>
          </div>

          <div class="function-card" @click="navigateTo('/user/loginlog')">
            <div class="function-icon">
              <el-icon size="24"><Document /></el-icon>
            </div>
            <div class="function-content">
              <h4>登录记录</h4>
              <p>查看登录历史</p>
            </div>
          </div>

          <div class="function-card" @click="navigateTo('/user/notification')">
            <div class="function-icon">
              <el-icon size="24"><Bell /></el-icon>
            </div>
            <div class="function-content">
              <h4>消息通知</h4>
              <p>系统消息和提醒</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧个人信息和活动 -->
      <div class="personal-sidebar">
        <!-- 个人信息卡片 -->
        <div class="info-card">
          <h3 class="section-title">个人信息</h3>
          <div class="info-content">
            <div class="info-item">
              <span class="label">用户名:</span>
              <span class="value">{{ userInfo.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">昵称:</span>
              <span class="value">{{ userInfo.nickname }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱:</span>
              <span class="value">{{ userInfo.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机:</span>
              <span class="value">{{ userInfo.phone }}</span>
            </div>
            <div class="info-item">
              <span class="label">注册时间:</span>
              <span class="value">{{ userInfo.createTime }}</span>
            </div>
            <div class="info-item">
              <span class="label">用户等级:</span>
              <span class="value level-{{ userInfo.level }}">{{ userInfo.levelText }}</span>
            </div>
          </div>
        </div>

        <!-- 最近活动 -->
        <div class="recent-activities">
          <h4>最近活动</h4>
          <div class="activity-list">
            <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
              <div class="activity-icon" :class="activity.type">
                <el-icon v-if="activity.type === 'login'"><User /></el-icon>
                <el-icon v-else-if="activity.type === 'profile'"><Edit /></el-icon>
                <el-icon v-else-if="activity.type === 'security'"><Lock /></el-icon>
                <el-icon v-else><InfoFilled /></el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-time">{{ activity.time }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 系统通知 -->
        <div class="system-notifications">
          <h4>系统通知</h4>
          <div class="notification-list">
            <div v-for="notification in systemNotifications" :key="notification.id" class="notification-item" :class="notification.level">
              <div class="notification-icon">
                <el-icon v-if="notification.level === 'info'"><InfoFilled /></el-icon>
                <el-icon v-else-if="notification.level === 'warning'"><WarningFilled /></el-icon>
                <el-icon v-else><Bell /></el-icon>
              </div>
              <div class="notification-content">
                <div class="notification-title">{{ notification.title }}</div>
                <div class="notification-message">{{ notification.message }}</div>
                <div class="notification-time">{{ notification.time }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部个人数据图表 -->
    <div class="charts-section">
      <h3 class="section-title">个人数据统计</h3>
      <div class="charts-grid">
        <div class="chart-card">
          <h4>登录活跃度</h4>
          <div class="chart-placeholder">
            <el-icon size="48" color="#909399"><TrendCharts /></el-icon>
            <p>登录活跃度趋势图表</p>
          </div>
        </div>
        
        <div class="chart-card">
          <h4>积分获取记录</h4>
          <div class="chart-placeholder">
            <el-icon size="48" color="#909399"><Star /></el-icon>
            <p>积分获取记录图表</p>
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
  User, Edit, Lock, Link, Monitor, Document, Bell,
  Clock, TrendCharts, Check, ArrowUp, Star, InfoFilled,
  WarningFilled, CircleCheck
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
  username: 'personal_user',
  nickname: '张三',
  email: 'personal@example.com',
  phone: '138****0002',
  createTime: '2024-01-15',
  lastLoginTime: '2025-08-23 12:30:00',
  level: 'gold',
  levelText: '黄金会员'
})

// 个人统计
const personalStats = ref({
  profileComplete: 85,
  loginDays: 28,
  streakDays: 5,
  securityScore: 88,
  points: 1250,
  pointsEarned: 180
})

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    type: 'login',
    title: '登录系统',
    time: '2小时前'
  },
  {
    id: 2,
    type: 'profile',
    title: '更新个人资料',
    time: '1天前'
  },
  {
    id: 3,
    type: 'security',
    title: '修改密码',
    time: '3天前'
  },
  {
    id: 4,
    type: 'info',
    title: '查看登录记录',
    time: '5天前'
  }
])

// 系统通知
const systemNotifications = ref([
  {
    id: 1,
    level: 'info',
    title: '系统维护通知',
    message: '系统将于今晚22:00-24:00进行维护，期间可能影响正常使用',
    time: '1小时前'
  },
  {
    id: 2,
    level: 'warning',
    title: '安全提醒',
    message: '检测到您的账号在异地登录，如非本人操作，请及时修改密码',
    time: '2小时前'
  },
  {
    id: 3,
    level: 'info',
    title: '积分到账通知',
    message: '您本月获得180积分已到账，可用于兑换礼品',
    time: '1天前'
  }
])

// 导航方法
const navigateTo = (path) => {
  router.push(path)
}

// 模拟数据更新
onMounted(() => {
  console.log('个人用户仪表板已加载')
  
  // 模拟实时数据更新
  setInterval(() => {
    personalStats.value.loginDays = Math.floor(Math.random() * 10) + 25
    personalStats.value.points = Math.floor(Math.random() * 100) + 1200
  }, 60000)
})
</script>

<style scoped>
.personal-dashboard {
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

.user-info {
  flex: 1;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.user-details h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.user-details p {
  margin: 0 0 16px 0;
  opacity: 0.9;
  font-size: 16px;
}

.user-status {
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

.status-badge.online {
  background: rgba(103, 194, 58, 0.2);
  color: #67c23a;
}

.last-login {
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

.stat-icon.profile { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.activity { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.security { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-icon.points { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

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

/* 快捷功能 */
.quick-functions {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.functions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.function-card {
  padding: 20px;
  border: 1px solid #e1e8ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}

.function-card:hover {
  border-color: #667eea;
  background: #f8f9ff;
  transform: translateY(-2px);
}

.function-icon {
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

.function-content h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 16px;
}

.function-content p {
  margin: 0;
  color: #7f8c8d;
  font-size: 14px;
}

/* 个人信息侧边栏 */
.personal-sidebar {
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

.level-gold { color: #f39c12; }
.level-silver { color: #95a5a6; }
.level-bronze { color: #e67e22; }

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

.activity-icon.login { background: #67c23a; }
.activity-icon.profile { background: #409eff; }
.activity-icon.security { background: #e6a23c; }
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

/* 系统通知 */
.system-notifications {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.system-notifications h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  padding: 12px;
  border-radius: 8px;
  background: #f8f9fa;
  border-left: 4px solid #409eff;
}

.notification-item.warning {
  border-left-color: #e6a23c;
}

.notification-item.info {
  border-left-color: #409eff;
}

.notification-icon {
  color: #7f8c8d;
  margin-bottom: 8px;
}

.notification-title {
  font-size: 14px;
  color: #2c3e50;
  font-weight: 500;
  margin-bottom: 4px;
}

.notification-message {
  font-size: 13px;
  color: #7f8c8d;
  margin-bottom: 8px;
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  color: #bdc3c7;
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
  .personal-dashboard {
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
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
  
  .header-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .functions-grid {
    grid-template-columns: 1fr;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
