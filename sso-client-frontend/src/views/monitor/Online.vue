<template>
  <div class="online-users">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><UserFilled /></el-icon>
          <span>在线用户监控</span>
          <div class="header-actions">
            <el-button @click="refreshData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计信息 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="在线用户总数" :value="stats.totalOnline" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="今日登录" :value="stats.todayLogin" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="活跃用户" :value="stats.activeUsers" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="平均在线时长" :value="stats.avgOnlineTime" suffix="分钟" />
          </el-col>
        </el-row>
      </div>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="IP地址">
            <el-input v-model="searchForm.ip" placeholder="请输入IP地址" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadOnlineUsers">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 在线用户表格 -->
      <el-table :data="onlineUsers" v-loading="loading" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="location" label="登录地点" width="120" />
        <el-table-column prop="browser" label="浏览器" width="120" />
        <el-table-column prop="os" label="操作系统" width="120" />
        <el-table-column prop="loginTime" label="登录时间" width="160" />
        <el-table-column prop="lastActiveTime" label="最后活动" width="160" />
        <el-table-column prop="onlineDuration" label="在线时长" width="100">
          <template #default="scope">
            {{ formatDuration(scope.row.onlineDuration) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              type="danger" 
              size="small" 
              @click="forceLogout(scope.row)"
              :disabled="scope.row.username === currentUser"
            >
              强制下线
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadOnlineUsers"
          @current-change="loadOnlineUsers"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, Refresh, Search } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const onlineUsers = ref([])

const stats = reactive({
  totalOnline: 0,
  todayLogin: 0,
  activeUsers: 0,
  avgOnlineTime: 0
})

const searchForm = reactive({
  username: '',
  ip: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 计算属性
const currentUser = computed(() => authStore.userInfo?.username)

// 方法
const formatDuration = (minutes) => {
  if (minutes < 60) {
    return `${minutes}分钟`
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}小时${mins}分钟`
}

const loadOnlineUsers = async () => {
  try {
    loading.value = true
    
    // 模拟在线用户数据
    const mockUsers = [
      {
        id: 1,
        username: 'admin',
        realName: '系统管理员',
        ip: '192.168.1.100',
        location: '北京市',
        browser: 'Chrome 120',
        os: 'Windows 10',
        loginTime: '2024-01-19 09:00:00',
        lastActiveTime: '2024-01-19 14:30:00',
        onlineDuration: 330
      },
      {
        id: 2,
        username: 'airline_user',
        realName: '航司用户',
        ip: '192.168.1.101',
        location: '上海市',
        browser: 'Firefox 121',
        os: 'macOS',
        loginTime: '2024-01-19 10:15:00',
        lastActiveTime: '2024-01-19 14:25:00',
        onlineDuration: 250
      }
    ]

    onlineUsers.value = mockUsers
    pagination.total = mockUsers.length

    // 更新统计信息
    stats.totalOnline = mockUsers.length
    stats.todayLogin = 89
    stats.activeUsers = mockUsers.filter(u => u.onlineDuration > 60).length
    stats.avgOnlineTime = Math.round(mockUsers.reduce((sum, u) => sum + u.onlineDuration, 0) / mockUsers.length)

  } catch (error) {
    console.error('加载在线用户失败:', error)
    ElMessage.error('加载在线用户失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  Object.assign(searchForm, {
    username: '',
    ip: ''
  })
  pagination.page = 1
  loadOnlineUsers()
}

const forceLogout = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要强制用户 ${user.username} 下线吗？`,
      '确认操作',
      { type: 'warning' }
    )

    // 模拟强制下线操作
    ElMessage.success(`用户 ${user.username} 已被强制下线`)
    loadOnlineUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('强制下线失败:', error)
      ElMessage.error('强制下线失败')
    }
  }
}

const refreshData = () => {
  loadOnlineUsers()
  ElMessage.success('数据已刷新')
}

// 生命周期
onMounted(() => {
  loadOnlineUsers()
  
  // 定时刷新数据
  setInterval(() => {
    loadOnlineUsers()
  }, 30000) // 30秒刷新一次
})
</script>

<style scoped>
.online-users {
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

.stats-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
