<template>
  <div class="login-log">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>登录日志</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.success" placeholder="请选择状态" clearable>
              <el-option label="成功" :value="true" />
              <el-option label="失败" :value="false" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadLogs">
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

      <!-- 登录日志表格 -->
      <el-table :data="logs" v-loading="loading" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="location" label="登录地点" width="120" />
        <el-table-column prop="browser" label="浏览器" width="150" />
        <el-table-column prop="os" label="操作系统" width="120" />
        <el-table-column prop="loginTime" label="登录时间" width="160" />
        <el-table-column prop="success" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.success ? 'success' : 'danger'">
              {{ scope.row.success ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="备注" show-overflow-tooltip />
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadLogs"
          @current-change="loadLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Search, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const logs = ref([])

const searchForm = reactive({
  username: '',
  success: null,
  dateRange: []
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 方法
const loadLogs = async () => {
  try {
    loading.value = true
    
    // 模拟登录日志数据
    const mockLogs = [
      {
        id: 1,
        username: 'admin',
        ip: '192.168.1.100',
        location: '北京市',
        browser: 'Chrome 120',
        os: 'Windows 10',
        loginTime: '2024-01-19 14:30:00',
        success: true,
        message: '登录成功'
      },
      {
        id: 2,
        username: 'test_user',
        ip: '192.168.1.101',
        location: '上海市',
        browser: 'Firefox 121',
        os: 'macOS',
        loginTime: '2024-01-19 14:25:00',
        success: false,
        message: '密码错误'
      }
    ]

    logs.value = mockLogs
    pagination.total = mockLogs.length

  } catch (error) {
    console.error('加载登录日志失败:', error)
    ElMessage.error('加载登录日志失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  Object.assign(searchForm, {
    username: '',
    success: null,
    dateRange: []
  })
  pagination.page = 1
  loadLogs()
}

// 生命周期
onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.login-log {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
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
