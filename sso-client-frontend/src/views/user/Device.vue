<template>
  <div class="device-container">
    <el-card class="device-card">
      <template #header>
        <div class="card-header">
          <el-icon><Monitor /></el-icon>
          <span>设备管理</span>
          <el-button type="text" size="small" @click="refreshDevices" style="margin-left: auto;">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <div class="device-content">
        <div class="device-description">
          <el-alert
            title="设备管理"
            description="这里显示了所有登录过您账号的设备。您可以查看设备详情并移除不信任的设备。"
            type="info"
            :closable="false"
            show-icon
          />
        </div>

        <div class="device-stats">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-statistic title="总设备数" :value="deviceStats.total" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="在线设备" :value="deviceStats.online" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="本月新增" :value="deviceStats.thisMonth" />
            </el-col>
          </el-row>
        </div>

        <div class="device-list">
          <div
            v-for="device in devices"
            :key="device.id"
            class="device-item"
            :class="{ 'current-device': device.isCurrent }"
          >
            <div class="device-icon">
              <el-icon :size="32" :color="getDeviceIconColor(device.deviceType)">
                <component :is="getDeviceIcon(device.deviceType)" />
              </el-icon>
            </div>
            
            <div class="device-info">
              <div class="device-header">
                <h4>{{ device.deviceName }}</h4>
                <div class="device-badges">
                  <el-tag v-if="device.isCurrent" type="success" size="small">
                    <el-icon><Check /></el-icon>
                    当前设备
                  </el-tag>
                  <el-tag :type="device.isOnline ? 'success' : 'info'" size="small">
                    {{ device.isOnline ? '在线' : '离线' }}
                  </el-tag>
                </div>
              </div>
              
              <div class="device-details">
                <p class="device-location">
                  <el-icon><Location /></el-icon>
                  {{ device.location }}
                </p>
                <p class="device-time">
                  <el-icon><Clock /></el-icon>
                  最后活跃：{{ formatTime(device.lastActiveTime) }}
                </p>
                <p class="device-ip">
                  <el-icon><Connection /></el-icon>
                  IP地址：{{ device.ipAddress }}
                </p>
                <p class="device-browser">
                  <el-icon><Monitor /></el-icon>
                  {{ device.browser }} · {{ device.os }}
                </p>
              </div>
            </div>
            
            <div class="device-actions">
              <el-dropdown v-if="!device.isCurrent" @command="handleDeviceAction">
                <el-button type="text">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="`detail-${device.id}`">
                      <el-icon><View /></el-icon>
                      查看详情
                    </el-dropdown-item>
                    <el-dropdown-item :command="`remove-${device.id}`" divided>
                      <el-icon><Delete /></el-icon>
                      移除设备
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              
              <el-button v-if="device.isCurrent" type="text" disabled>
                当前设备
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="devices.length === 0" class="empty-devices">
          <el-empty description="暂无设备记录" :image-size="100">
            <el-button type="primary" @click="refreshDevices">
              <el-icon><Refresh /></el-icon>
              刷新设备列表
            </el-button>
          </el-empty>
        </div>
      </div>
    </el-card>

    <!-- 设备详情对话框 -->
    <el-dialog
      v-model="deviceDetailVisible"
      title="设备详情"
      width="600px"
      :before-close="closeDeviceDetail"
    >
      <div v-if="selectedDevice" class="device-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="设备名称">
            {{ selectedDevice.deviceName }}
          </el-descriptions-item>
          <el-descriptions-item label="设备类型">
            <el-tag>{{ getDeviceTypeName(selectedDevice.deviceType) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作系统">
            {{ selectedDevice.os }}
          </el-descriptions-item>
          <el-descriptions-item label="浏览器">
            {{ selectedDevice.browser }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ selectedDevice.ipAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="地理位置">
            {{ selectedDevice.location }}
          </el-descriptions-item>
          <el-descriptions-item label="首次登录">
            {{ formatTime(selectedDevice.firstLoginTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="最后活跃">
            {{ formatTime(selectedDevice.lastActiveTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="登录次数">
            {{ selectedDevice.loginCount }} 次
          </el-descriptions-item>
          <el-descriptions-item label="设备状态">
            <el-tag :type="selectedDevice.isOnline ? 'success' : 'info'">
              {{ selectedDevice.isOnline ? '在线' : '离线' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDeviceDetail">关闭</el-button>
          <el-button
            v-if="selectedDevice && !selectedDevice.isCurrent"
            type="danger"
            @click="removeDevice(selectedDevice.id)"
          >
            <el-icon><Delete /></el-icon>
            移除设备
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Monitor, Refresh, Check, Location, Clock, Connection, 
  MoreFilled, View, Delete 
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const devices = ref([])
const deviceStats = reactive({
  total: 0,
  online: 0,
  thisMonth: 0
})

const deviceDetailVisible = ref(false)
const selectedDevice = ref(null)

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
    'laptop': '笔记本电脑',
    'mobile': '手机',
    'tablet': '平板电脑',
    'unknown': '未知设备'
  }
  return nameMap[deviceType] || '未知设备'
}

const formatTime = (timeString) => {
  if (!timeString) return '未知'
  return new Date(timeString).toLocaleString('zh-CN')
}

const refreshDevices = async () => {
  try {
    const response = await request.get('/api/user/devices')
    if (response.data.code === 200) {
      devices.value = response.data.data || []
      updateDeviceStats()
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('获取设备列表失败')
  }
}

const updateDeviceStats = () => {
  deviceStats.total = devices.value.length
  deviceStats.online = devices.value.filter(d => d.isOnline).length
  
  const thisMonth = new Date()
  thisMonth.setDate(1)
  thisMonth.setHours(0, 0, 0, 0)
  
  deviceStats.thisMonth = devices.value.filter(d => {
    const firstLogin = new Date(d.firstLoginTime)
    return firstLogin >= thisMonth
  }).length
}

const handleDeviceAction = (command) => {
  const [action, deviceId] = command.split('-')
  const device = devices.value.find(d => d.id === deviceId)
  
  if (!device) return
  
  switch (action) {
    case 'detail':
      showDeviceDetail(device)
      break
    case 'remove':
      removeDevice(deviceId)
      break
  }
}

const showDeviceDetail = (device) => {
  selectedDevice.value = device
  deviceDetailVisible.value = true
}

const closeDeviceDetail = () => {
  deviceDetailVisible.value = false
  selectedDevice.value = null
}

const removeDevice = async (deviceId) => {
  try {
    await ElMessageBox.confirm(
      '确定要移除此设备吗？移除后该设备将无法继续访问您的账号。',
      '确认移除',
      {
        type: 'warning'
      }
    )

    const response = await request.delete(`/api/user/device/${deviceId}`)
    if (response.data.code === 200) {
      ElMessage.success('设备已移除')
      closeDeviceDetail()
      refreshDevices()
    } else {
      ElMessage.error(response.data.message || '移除设备失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除设备失败')
    }
  }
}

// 模拟数据加载
const loadMockData = () => {
  devices.value = [
    {
      id: '1',
      deviceName: 'Windows PC - Chrome',
      deviceType: 'desktop',
      os: 'Windows 11',
      browser: 'Chrome 120.0',
      ipAddress: '192.168.1.100',
      location: '北京市 海淀区',
      firstLoginTime: '2024-01-15 09:30:00',
      lastActiveTime: new Date().toISOString(),
      loginCount: 45,
      isOnline: true,
      isCurrent: true
    },
    {
      id: '2',
      deviceName: 'iPhone 15 Pro - Safari',
      deviceType: 'mobile',
      os: 'iOS 17.2',
      browser: 'Safari 17.0',
      ipAddress: '192.168.1.101',
      location: '北京市 朝阳区',
      firstLoginTime: '2024-01-10 14:20:00',
      lastActiveTime: '2024-01-20 16:45:00',
      loginCount: 23,
      isOnline: false,
      isCurrent: false
    },
    {
      id: '3',
      deviceName: 'MacBook Pro - Safari',
      deviceType: 'laptop',
      os: 'macOS 14.2',
      browser: 'Safari 17.2',
      ipAddress: '10.0.1.50',
      location: '上海市 浦东新区',
      firstLoginTime: '2024-01-05 11:15:00',
      lastActiveTime: '2024-01-18 20:30:00',
      loginCount: 12,
      isOnline: false,
      isCurrent: false
    }
  ]
  updateDeviceStats()
}

// 生命周期
onMounted(() => {
  // 尝试从API加载，失败则使用模拟数据
  refreshDevices().catch(() => {
    loadMockData()
  })
})
</script>

<style scoped>
.device-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.device-card {
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

.device-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.device-description {
  margin-bottom: 16px;
}

.device-stats {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.device-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.device-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.device-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.device-item.current-device {
  border-color: #67c23a;
  background-color: #f0f9ff;
}

.device-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background-color: #f8f9fa;
  flex-shrink: 0;
}

.device-info {
  flex: 1;
}

.device-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.device-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.device-badges {
  display: flex;
  gap: 8px;
}

.device-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.device-details p {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 13px;
}

.device-actions {
  display: flex;
  align-items: center;
}

.empty-devices {
  text-align: center;
  padding: 60px 0;
}

.device-detail {
  margin: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .device-container {
    padding: 10px;
  }
  
  .device-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .device-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .device-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
