<template>
  <div class="personal-dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
      <p class="page-subtitle">欢迎回来，{{ userInfo.nickname }}！今天是 {{ currentDate }}</p>
    </div>

    <!-- 页面内容 -->
    <div class="page-content">

    <!-- 个人统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon profile">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ personalStats.profileComplete }}%</div>
              <div class="stat-label">资料完整度</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon activity">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ personalStats.loginDays }}</div>
              <div class="stat-label">连续登录天数</div>
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
              <div class="stat-number">{{ personalStats.securityScore }}</div>
              <div class="stat-label">安全评分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon points">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ personalStats.points }}</div>
              <div class="stat-label">积分余额</div>
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
              <span>个人功能</span>
            </div>
          </template>
          <el-row :gutter="16">
            <el-col :span="8">
              <div class="module-item" @click="navigateTo('/user/profile')">
                <div class="module-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="module-content">
                  <h4>个人资料</h4>
                  <p>查看和编辑个人信息</p>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="module-item" @click="navigateTo('/user/security')">
                <div class="module-icon">
                  <el-icon><Lock /></el-icon>
                </div>
                <div class="module-content">
                  <h4>安全设置</h4>
                  <p>密码修改、安全验证</p>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="module-item" @click="navigateTo('/user/oauth')">
                <div class="module-icon">
                  <el-icon><Link /></el-icon>
                </div>
                <div class="module-content">
                  <h4>第三方绑定</h4>
                  <p>绑定微信、QQ等账号</p>
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
              <span>账户管理</span>
            </div>
          </template>
          <div class="monitor-list">
            <div class="monitor-item" @click="navigateTo('/user/device')">
              <el-icon><Monitor /></el-icon>
              <span>设备管理</span>
            </div>
            <div class="monitor-item" @click="navigateTo('/user/loginlog')">
              <el-icon><Document /></el-icon>
              <span>登录记录</span>
            </div>
            <div class="monitor-item" @click="navigateTo('/user/notification')">
              <el-icon><Bell /></el-icon>
              <span>消息通知</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 个人信息 -->
    <el-row :gutter="20" class="info-row">
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <div class="info-list">
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
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
            </div>
          </template>
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

.stat-icon.profile { background: #409eff; }
.stat-icon.activity { background: #67c23a; }
.stat-icon.security { background: #e6a23c; }
.stat-icon.points { background: #f56c6c; }

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

.level-gold { color: #f39c12; }
.level-silver { color: #95a5a6; }
.level-bronze { color: #e67e22; }

/* 活动列表 */
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
  border-radius: 6px;
  background: #f8f9fa;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
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
  color: #303133;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .modules-row .el-col:first-child {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .personal-dashboard {
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
