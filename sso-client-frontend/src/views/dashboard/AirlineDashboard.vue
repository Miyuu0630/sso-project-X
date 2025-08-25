<template>
  <div class="airline-dashboard">
    <!-- 航司概览头部 -->
    <div class="dashboard-header">
      <div class="header-content">
        <div class="airline-info">
          <div class="airline-logo">
            <el-icon size="32"><Ship /></el-icon>
          </div>
          <div class="airline-details">
            <h1>{{ airlineInfo.name }} - 航司运营控制台</h1>
            <p>欢迎回来，{{ userInfo.nickname }}！今天是 {{ currentDate }}</p>
            <div class="airline-status">
              <span class="status-badge active">
                <el-icon><Check /></el-icon>
                运营正常
              </span>
              <span class="airline-code">{{ airlineInfo.code }}</span>
              <span class="fleet-size">机队：{{ airlineInfo.fleetSize }}架</span>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="navigateTo('/airline/flight')">
            <el-icon><Calendar /></el-icon>
            航班管理
          </el-button>
          <el-button type="success" @click="navigateTo('/airline/passenger')">
            <el-icon><User /></el-icon>
            乘客管理
          </el-button>
          <el-button type="warning" @click="navigateTo('/airline/route')">
            <el-icon><Location /></el-icon>
            航线管理
          </el-button>
        </div>
      </div>
    </div>

    <!-- 航司统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon flights">
            <el-icon size="32"><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ airlineStats.totalFlights }}</div>
            <div class="stat-label">今日航班</div>
            <div class="stat-trend positive">
              <el-icon><ArrowUp /></el-icon>
              {{ airlineStats.onTimeRate }}% 准点率
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon passengers">
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ airlineStats.totalPassengers }}</div>
            <div class="stat-label">今日乘客</div>
            <div class="stat-trend positive">
              <el-icon><TrendCharts /></el-icon>
              +{{ airlineStats.newBookings }} 新预订
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon revenue">
            <el-icon size="32"><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">¥{{ airlineStats.dailyRevenue }}</div>
            <div class="stat-label">日营收</div>
            <div class="stat-trend" :class="airlineStats.revenueGrowth > 0 ? 'positive' : 'warning'">
              <el-icon><TrendCharts /></el-icon>
              {{ airlineStats.revenueGrowth > 0 ? '+' : '' }}{{ airlineStats.revenueGrowth }}% 较昨日
            </div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon routes">
            <el-icon size="32"><Location /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ airlineStats.activeRoutes }}</div>
            <div class="stat-label">活跃航线</div>
            <div class="stat-trend positive">
              <el-icon><Check /></el-icon>
              {{ airlineStats.routeUtilization }}% 利用率
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧运营概览 -->
      <div class="operations-overview">
        <h3 class="section-title">运营概览</h3>
        <div class="overview-grid">
          <div class="overview-card" @click="navigateTo('/airline/flight')">
            <div class="overview-icon">
              <el-icon size="24"><Calendar /></el-icon>
            </div>
            <div class="overview-content">
              <h4>航班管理</h4>
              <p>航班调度、状态监控、延误处理</p>
              <div class="overview-stats">
                <span>起飞：{{ airlineStats.departures }}</span>
                <span>降落：{{ airlineStats.arrivals }}</span>
              </div>
            </div>
          </div>

          <div class="overview-card" @click="navigateTo('/airline/passenger')">
            <div class="overview-icon">
              <el-icon size="24"><User /></el-icon>
            </div>
            <div class="overview-content">
              <h4>乘客管理</h4>
              <p>订票管理、登机手续、行李托运</p>
              <div class="overview-stats">
                <span>已登机：{{ airlineStats.checkedIn }}</span>
                <span>待登机：{{ airlineStats.pendingCheckIn }}</span>
              </div>
            </div>
          </div>

          <div class="overview-card" @click="navigateTo('/airline/route')">
            <div class="overview-icon">
              <el-icon size="24"><Location /></el-icon>
            </div>
            <div class="overview-content">
              <h4>航线管理</h4>
              <p>航线规划、时刻表、价格策略</p>
              <div class="overview-stats">
                <span>国内：{{ airlineStats.domesticRoutes }}</span>
                <span>国际：{{ airlineStats.internationalRoutes }}</span>
              </div>
            </div>
          </div>

          <div class="overview-card" @click="navigateTo('/airline/maintenance')">
            <div class="overview-icon">
              <el-icon size="24"><Tools /></el-icon>
            </div>
            <div class="overview-content">
              <h4>机务维护</h4>
              <p>飞机维护、安全检查、故障处理</p>
              <div class="overview-stats">
                <span>正常：{{ airlineStats.aircraftNormal }}</span>
                <span>维护：{{ airlineStats.aircraftMaintenance }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧航司信息和实时状态 -->
      <div class="airline-sidebar">
        <!-- 航司信息卡片 -->
        <div class="info-card">
          <h3 class="section-title">航司信息</h3>
          <div class="info-content">
            <div class="info-item">
              <span class="label">航司名称:</span>
              <span class="value">{{ airlineInfo.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">航司代码:</span>
              <span class="value">{{ airlineInfo.code }}</span>
            </div>
            <div class="info-item">
              <span class="label">成立时间:</span>
              <span class="value">{{ airlineInfo.establishDate }}</span>
            </div>
            <div class="info-item">
              <span class="label">机队规模:</span>
              <span class="value">{{ airlineInfo.fleetSize }}架</span>
            </div>
            <div class="info-item">
              <span class="label">运营状态:</span>
              <span class="value status-active">正常运营</span>
            </div>
            <div class="info-item">
              <span class="label">总部地址:</span>
              <span class="value">{{ airlineInfo.headquarters }}</span>
            </div>
          </div>
        </div>

        <!-- 实时航班状态 -->
        <div class="real-time-status">
          <h4>实时航班状态</h4>
          <div class="status-summary">
            <div class="status-item">
              <div class="status-number">{{ airlineStats.departures }}</div>
              <div class="status-label">已起飞</div>
            </div>
            <div class="status-item">
              <div class="status-number">{{ airlineStats.arrivals }}</div>
              <div class="status-label">已降落</div>
            </div>
            <div class="status-item">
              <div class="status-number">{{ airlineStats.delayed }}</div>
              <div class="status-label">延误</div>
            </div>
          </div>
          <div class="status-actions">
            <el-button size="small" @click="navigateTo('/airline/monitor')">
              <el-icon><Monitor /></el-icon>
              实时监控
            </el-button>
            <el-button size="small" @click="navigateTo('/airline/alert')">
              <el-icon><Bell /></el-icon>
              告警中心
            </el-button>
          </div>
        </div>

        <!-- 热门航线 -->
        <div class="popular-routes">
          <h4>热门航线</h4>
          <div class="route-list">
            <div v-for="route in popularRoutes" :key="route.id" class="route-item">
              <div class="route-info">
                <div class="route-cities">
                  <span class="departure">{{ route.departure }}</span>
                  <el-icon class="arrow"><ArrowRight /></el-icon>
                  <span class="arrival">{{ route.arrival }}</span>
                </div>
                <div class="route-details">
                  <span class="frequency">{{ route.frequency }}班/天</span>
                  <span class="load-factor">{{ route.loadFactor }}% 上座率</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部图表区域 -->
    <div class="charts-section">
      <h3 class="section-title">运营数据图表</h3>
      <div class="charts-grid">
        <div class="chart-card">
          <h4>航班准点率趋势</h4>
          <div class="chart-placeholder">
            <el-icon size="48" color="#909399"><TrendCharts /></el-icon>
            <p>航班准点率趋势图表</p>
          </div>
        </div>
        
        <div class="chart-card">
          <h4>乘客流量统计</h4>
          <div class="chart-placeholder">
            <el-icon size="48" color="#909399"><User /></el-icon>
            <p>乘客流量统计图表</p>
          </div>
        </div>
        
        <div class="chart-card">
          <h4>航线收益分析</h4>
          <div class="chart-placeholder">
            <el-icon size="48" color="#909399"><Money /></el-icon>
            <p>航线收益分析图表</p>
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
  Ship, Calendar, User, Location, Money, TrendCharts,
  Check, ArrowUp, Tools, Monitor, Bell, ArrowRight
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
  nickname: '航司用户'
})

// 航司信息
const airlineInfo = ref({
  name: '蓝天航空有限公司',
  code: 'BL',
  establishDate: '2015-06-20',
  fleetSize: 45,
  headquarters: '北京市朝阳区'
})

// 航司统计
const airlineStats = ref({
  totalFlights: 128,
  onTimeRate: 94.5,
  totalPassengers: 15680,
  newBookings: 234,
  dailyRevenue: '2,850,000',
  revenueGrowth: 8.7,
  activeRoutes: 68,
  routeUtilization: 92.3,
  departures: 64,
  arrivals: 64,
  checkedIn: 12450,
  pendingCheckIn: 3230,
  domesticRoutes: 52,
  internationalRoutes: 16,
  aircraftNormal: 38,
  aircraftMaintenance: 7,
  delayed: 6
})

// 热门航线
const popularRoutes = ref([
  {
    id: 1,
    departure: '北京',
    arrival: '上海',
    frequency: 12,
    loadFactor: 95.2
  },
  {
    id: 2,
    departure: '广州',
    arrival: '深圳',
    frequency: 8,
    loadFactor: 88.7
  },
  {
    id: 3,
    departure: '成都',
    arrival: '重庆',
    frequency: 6,
    loadFactor: 92.1
  },
  {
    id: 4,
    departure: '杭州',
    arrival: '南京',
    frequency: 5,
    loadFactor: 85.4
  }
])

// 导航方法
const navigateTo = (path) => {
  router.push(path)
}

// 模拟数据更新
onMounted(() => {
  console.log('航司用户仪表板已加载')
  
  // 模拟实时数据更新
  setInterval(() => {
    airlineStats.value.totalFlights = Math.floor(Math.random() * 20) + 120
    airlineStats.value.totalPassengers = Math.floor(Math.random() * 1000) + 15000
    airlineStats.value.onTimeRate = Math.floor(Math.random() * 10) + 90
  }, 30000)
})
</script>

<style scoped>
.airline-dashboard {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 仪表板头部 */
.dashboard-header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
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

.airline-info {
  flex: 1;
}

.airline-logo {
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

.airline-details h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.airline-details p {
  margin: 0 0 16px 0;
  opacity: 0.9;
  font-size: 16px;
}

.airline-status {
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

.airline-code {
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
}

.fleet-size {
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

.stat-icon.flights { background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); }
.stat-icon.passengers { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.revenue { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.routes { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }

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

/* 运营概览 */
.operations-overview {
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
  border-color: #1e3c72;
  background: #f8f9ff;
  transform: translateY(-2px);
}

.overview-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
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

/* 航司信息侧边栏 */
.airline-sidebar {
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

.status-active { color: #67c23a; }

/* 实时航班状态 */
.real-time-status {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.real-time-status h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.status-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.status-item {
  text-align: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.status-item .status-number {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 4px;
}

.status-item .status-label {
  font-size: 12px;
  color: #7f8c8d;
}

.status-actions {
  display: flex;
  gap: 12px;
}

/* 热门航线 */
.popular-routes {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.popular-routes h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.route-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.route-item {
  padding: 12px;
  border-radius: 8px;
  background: #f8f9fa;
}

.route-cities {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

.arrow {
  color: #7f8c8d;
  font-size: 12px;
}

.route-details {
  display: flex;
  gap: 16px;
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
  .airline-dashboard {
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
  
  .airline-status {
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
  
  .status-summary {
    grid-template-columns: 1fr;
  }
}
</style>
