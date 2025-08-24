<template>
  <div class="server-monitor">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><Monitor /></el-icon>
          <span>服务器监控</span>
          <div class="header-actions">
            <el-button @click="refreshData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 系统信息 -->
      <div class="system-info">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="info-card">
              <template #header>
                <span>CPU使用率</span>
              </template>
              <div class="metric-content">
                <el-progress 
                  type="circle" 
                  :percentage="systemInfo.cpuUsage" 
                  :color="getProgressColor(systemInfo.cpuUsage)"
                />
                <p>{{ systemInfo.cpuUsage }}%</p>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="info-card">
              <template #header>
                <span>内存使用率</span>
              </template>
              <div class="metric-content">
                <el-progress 
                  type="circle" 
                  :percentage="systemInfo.memoryUsage" 
                  :color="getProgressColor(systemInfo.memoryUsage)"
                />
                <p>{{ systemInfo.memoryUsage }}%</p>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="info-card">
              <template #header>
                <span>磁盘使用率</span>
              </template>
              <div class="metric-content">
                <el-progress 
                  type="circle" 
                  :percentage="systemInfo.diskUsage" 
                  :color="getProgressColor(systemInfo.diskUsage)"
                />
                <p>{{ systemInfo.diskUsage }}%</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 服务状态 -->
      <div class="service-status">
        <h3>服务状态</h3>
        <el-table :data="services" style="width: 100%">
          <el-table-column prop="name" label="服务名称" width="200" />
          <el-table-column prop="port" label="端口" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'running' ? 'success' : 'danger'">
                {{ scope.row.status === 'running' ? '运行中' : '已停止' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="uptime" label="运行时间" width="150" />
          <el-table-column prop="memory" label="内存占用" width="120" />
          <el-table-column prop="cpu" label="CPU占用" width="120" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button 
                :type="scope.row.status === 'running' ? 'warning' : 'success'"
                size="small"
                @click="toggleService(scope.row)"
              >
                {{ scope.row.status === 'running' ? '停止' : '启动' }}
              </el-button>
              <el-button type="info" size="small" @click="viewLogs(scope.row)">
                日志
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 系统详细信息 -->
      <div class="system-details">
        <h3>系统详细信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="操作系统">
            {{ systemDetails.os }}
          </el-descriptions-item>
          <el-descriptions-item label="Java版本">
            {{ systemDetails.javaVersion }}
          </el-descriptions-item>
          <el-descriptions-item label="总内存">
            {{ systemDetails.totalMemory }}
          </el-descriptions-item>
          <el-descriptions-item label="可用内存">
            {{ systemDetails.freeMemory }}
          </el-descriptions-item>
          <el-descriptions-item label="CPU核心数">
            {{ systemDetails.cpuCores }}
          </el-descriptions-item>
          <el-descriptions-item label="系统启动时间">
            {{ systemDetails.startTime }}
          </el-descriptions-item>
          <el-descriptions-item label="系统运行时间">
            {{ systemDetails.uptime }}
          </el-descriptions-item>
          <el-descriptions-item label="当前时间">
            {{ currentTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const currentTime = ref('')
const timer = ref(null)

const systemInfo = reactive({
  cpuUsage: 45,
  memoryUsage: 68,
  diskUsage: 32
})

const systemDetails = reactive({
  os: 'Windows 10 Pro',
  javaVersion: 'OpenJDK 17.0.2',
  totalMemory: '16 GB',
  freeMemory: '5.2 GB',
  cpuCores: 8,
  startTime: '2024-01-19 09:00:00',
  uptime: '5小时30分钟'
})

const services = ref([
  {
    id: 1,
    name: 'SSO Server',
    port: 8081,
    status: 'running',
    uptime: '5小时30分钟',
    memory: '256 MB',
    cpu: '12%'
  },
  {
    id: 2,
    name: 'SSO Client Backend',
    port: 8082,
    status: 'running',
    uptime: '5小时25分钟',
    memory: '128 MB',
    cpu: '8%'
  },
  {
    id: 3,
    name: 'Redis',
    port: 6379,
    status: 'running',
    uptime: '2天15小时',
    memory: '64 MB',
    cpu: '2%'
  },
  {
    id: 4,
    name: 'MySQL',
    port: 3306,
    status: 'running',
    uptime: '7天12小时',
    memory: '512 MB',
    cpu: '5%'
  }
])

// 方法
const getProgressColor = (percentage) => {
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

const updateCurrentTime = () => {
  currentTime.value = new Date().toLocaleString('zh-CN')
}

const refreshData = () => {
  // 模拟数据更新
  systemInfo.cpuUsage = Math.floor(Math.random() * 100)
  systemInfo.memoryUsage = Math.floor(Math.random() * 100)
  systemInfo.diskUsage = Math.floor(Math.random() * 100)
  
  ElMessage.success('数据已刷新')
}

const toggleService = async (service) => {
  try {
    const action = service.status === 'running' ? '停止' : '启动'
    await ElMessageBox.confirm(
      `确定要${action}服务 ${service.name} 吗？`,
      '确认操作',
      { type: 'warning' }
    )

    service.status = service.status === 'running' ? 'stopped' : 'running'
    ElMessage.success(`服务 ${service.name} 已${action}`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作服务失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const viewLogs = (service) => {
  ElMessage.info(`查看服务 ${service.name} 的日志功能开发中...`)
}

// 生命周期
onMounted(() => {
  updateCurrentTime()
  timer.value = setInterval(updateCurrentTime, 1000)
})

onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})
</script>

<style scoped>
.server-monitor {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.header-actions {
  margin-left: auto;
}

.system-info {
  margin-bottom: 30px;
}

.info-card {
  text-align: center;
}

.metric-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.metric-content p {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.service-status, .system-details {
  margin-bottom: 30px;
}

.service-status h3, .system-details h3 {
  margin-bottom: 15px;
  color: #303133;
  font-size: 16px;
}
</style>
