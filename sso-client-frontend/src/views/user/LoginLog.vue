<template>
  <div class="loginlog-container">
    <el-card class="loginlog-card">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>登录记录</span>
          <div class="header-actions">
            <el-button type="text" size="small" @click="exportLogs">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button type="text" size="small" @click="refreshLogs">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <div class="loginlog-content">
        <!-- 筛选条件 -->
        <div class="filter-section">
          <el-form :model="filterForm" inline>
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                @change="handleDateRangeChange"
              />
            </el-form-item>
            <el-form-item label="登录状态">
              <el-select v-model="filterForm.status" placeholder="全部状态" clearable>
                <el-option label="全部" value="" />
                <el-option label="成功" value="success" />
                <el-option label="失败" value="failed" />
              </el-select>
            </el-form-item>
            <el-form-item label="设备类型">
              <el-select v-model="filterForm.deviceType" placeholder="全部设备" clearable>
                <el-option label="全部" value="" />
                <el-option label="桌面电脑" value="desktop" />
                <el-option label="笔记本" value="laptop" />
                <el-option label="手机" value="mobile" />
                <el-option label="平板" value="tablet" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchLogs">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="resetFilter">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 统计信息 -->
        <div class="stats-section">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="总登录次数" :value="stats.totalLogins" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="成功登录" :value="stats.successLogins" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="失败登录" :value="stats.failedLogins" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="今日登录" :value="stats.todayLogins" />
            </el-col>
          </el-row>
        </div>

        <!-- 登录记录表格 -->
        <div class="table-section">
          <el-table
            :data="loginLogs"
            v-loading="loading"
            stripe
            border
            style="width: 100%"
          >
            <el-table-column prop="loginTime" label="登录时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.loginTime) }}
              </template>
            </el-table-column>
            
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
                  {{ scope.row.status === 'success' ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="ipAddress" label="IP地址" width="140" />
            
            <el-table-column prop="location" label="登录地点" width="150" />
            
            <el-table-column prop="deviceType" label="设备类型" width="120">
              <template #default="scope">
                <div class="device-info">
                  <el-icon :color="getDeviceIconColor(scope.row.deviceType)">
                    <component :is="getDeviceIcon(scope.row.deviceType)" />
                  </el-icon>
                  <span>{{ getDeviceTypeName(scope.row.deviceType) }}</span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="browser" label="浏览器" width="150" />
            
            <el-table-column prop="os" label="操作系统" width="120" />
            
            <el-table-column prop="message" label="详情" min-width="200">
              <template #default="scope">
                <span :class="{ 'error-message': scope.row.status === 'failed' }">
                  {{ scope.row.message }}
                </span>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="scope">
                <el-button type="text" size="small" @click="showLogDetail(scope.row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 登录详情对话框 -->
    <el-dialog
      v-model="logDetailVisible"
      title="登录详情"
      width="600px"
      :before-close="closeLogDetail"
    >
      <div v-if="selectedLog" class="log-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="登录时间">
            {{ formatTime(selectedLog.loginTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="登录状态">
            <el-tag :type="selectedLog.status === 'success' ? 'success' : 'danger'">
              {{ selectedLog.status === 'success' ? '登录成功' : '登录失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ selectedLog.ipAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="地理位置">
            {{ selectedLog.location }}
          </el-descriptions-item>
          <el-descriptions-item label="设备类型">
            {{ getDeviceTypeName(selectedLog.deviceType) }}
          </el-descriptions-item>
          <el-descriptions-item label="操作系统">
            {{ selectedLog.os }}
          </el-descriptions-item>
          <el-descriptions-item label="浏览器">
            {{ selectedLog.browser }}
          </el-descriptions-item>
          <el-descriptions-item label="User Agent">
            <div class="user-agent">{{ selectedLog.userAgent }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="详细信息">
            <span :class="{ 'error-message': selectedLog.status === 'failed' }">
              {{ selectedLog.message }}
            </span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeLogDetail">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Document, Download, Refresh, Search 
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)
const loginLogs = ref([])
const logDetailVisible = ref(false)
const selectedLog = ref(null)

const filterForm = reactive({
  dateRange: [],
  status: '',
  deviceType: ''
})

const stats = reactive({
  totalLogins: 0,
  successLogins: 0,
  failedLogins: 0,
  todayLogins: 0
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 方法
const getDeviceIcon = (deviceType) => {
  const iconMap = {
    'desktop': 'Monitor',
    'laptop': 'Laptop',
    'mobile': 'Iphone',
    'tablet': 'Tablet',
    'unknown': 'Monitor'
  }
  return iconMap[deviceType] || 'Monitor'
}

const getDeviceIconColor = (deviceType) => {
  const colorMap = {
    'desktop': '#409eff',
    'laptop': '#67c23a',
    'mobile': '#e6a23c',
    'tablet': '#f56c6c',
    'unknown': '#909399'
  }
  return colorMap[deviceType] || '#909399'
}

const getDeviceTypeName = (deviceType) => {
  const nameMap = {
    'desktop': '桌面电脑',
    'laptop': '笔记本',
    'mobile': '手机',
    'tablet': '平板',
    'unknown': '未知'
  }
  return nameMap[deviceType] || '未知'
}

const formatTime = (timeString) => {
  if (!timeString) return '未知'
  return new Date(timeString).toLocaleString('zh-CN')
}

const refreshLogs = async () => {
  await loadLoginLogs()
}

const loadLoginLogs = async () => {
  try {
    loading.value = true
    
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      ...filterForm
    }
    
    const response = await request.get('/api/user/login-logs', { params })
    if (response.data.code === 200) {
      const data = response.data.data
      loginLogs.value = data.records || []
      pagination.total = data.total || 0
      updateStats()
    }
  } catch (error) {
    console.error('获取登录记录失败:', error)
    // 使用模拟数据
    loadMockData()
  } finally {
    loading.value = false
  }
}

const updateStats = () => {
  stats.totalLogins = loginLogs.value.length
  stats.successLogins = loginLogs.value.filter(log => log.status === 'success').length
  stats.failedLogins = loginLogs.value.filter(log => log.status === 'failed').length
  
  const today = new Date().toDateString()
  stats.todayLogins = loginLogs.value.filter(log => {
    return new Date(log.loginTime).toDateString() === today
  }).length
}

const searchLogs = () => {
  pagination.currentPage = 1
  loadLoginLogs()
}

const resetFilter = () => {
  filterForm.dateRange = []
  filterForm.status = ''
  filterForm.deviceType = ''
  searchLogs()
}

const handleDateRangeChange = () => {
  // 日期范围变化时自动搜索
  searchLogs()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadLoginLogs()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadLoginLogs()
}

const showLogDetail = (log) => {
  selectedLog.value = log
  logDetailVisible.value = true
}

const closeLogDetail = () => {
  logDetailVisible.value = false
  selectedLog.value = null
}

const exportLogs = () => {
  ElMessage.info('导出功能开发中...')
}

// 模拟数据
const loadMockData = () => {
  const mockLogs = []
  const statuses = ['success', 'failed']
  const deviceTypes = ['desktop', 'mobile', 'laptop', 'tablet']
  const browsers = ['Chrome 120.0', 'Safari 17.0', 'Firefox 121.0', 'Edge 120.0']
  const oses = ['Windows 11', 'macOS 14.2', 'iOS 17.2', 'Android 14']
  const locations = ['北京市 海淀区', '上海市 浦东新区', '广州市 天河区', '深圳市 南山区']
  
  for (let i = 0; i < 50; i++) {
    const status = statuses[Math.floor(Math.random() * statuses.length)]
    const deviceType = deviceTypes[Math.floor(Math.random() * deviceTypes.length)]
    const loginTime = new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000)
    
    mockLogs.push({
      id: i + 1,
      loginTime: loginTime.toISOString(),
      status,
      ipAddress: `192.168.1.${Math.floor(Math.random() * 255)}`,
      location: locations[Math.floor(Math.random() * locations.length)],
      deviceType,
      browser: browsers[Math.floor(Math.random() * browsers.length)],
      os: oses[Math.floor(Math.random() * oses.length)],
      userAgent: `Mozilla/5.0 (${oses[Math.floor(Math.random() * oses.length)]}) AppleWebKit/537.36`,
      message: status === 'success' ? '登录成功' : '密码错误'
    })
  }
  
  loginLogs.value = mockLogs.slice(0, pagination.pageSize)
  pagination.total = mockLogs.length
  updateStats()
}

// 生命周期
onMounted(() => {
  loadLoginLogs()
})
</script>

<style scoped>
.loginlog-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.loginlog-card {
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

.header-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.loginlog-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.filter-section {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.stats-section {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.device-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.error-message {
  color: #f56c6c;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.log-detail {
  margin: 20px 0;
}

.user-agent {
  word-break: break-all;
  font-family: monospace;
  font-size: 12px;
  color: #666;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .loginlog-container {
    padding: 10px;
  }
  
  .filter-section .el-form {
    display: flex;
    flex-direction: column;
  }
  
  .filter-section .el-form-item {
    margin-bottom: 16px;
  }
}
</style>
