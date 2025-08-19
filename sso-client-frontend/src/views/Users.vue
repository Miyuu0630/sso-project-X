<template>
  <div class="users-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><User /></el-icon>
        用户管理
      </h1>
      <p class="page-subtitle">管理系统用户信息和权限</p>
    </div>

    <!-- 搜索和操作栏 -->
    <div class="toolbar">
      <el-card>
        <el-row :gutter="20" align="middle">
          <el-col :span="8">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索用户名、邮箱或手机号"
              :prefix-icon="Search"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-col>
          <el-col :span="4">
            <el-select v-model="searchForm.userType" placeholder="用户类型" clearable>
              <el-option label="个人用户" value="1" />
              <el-option label="企业用户" value="2" />
              <el-option label="航司用户" value="3" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="searchForm.status" placeholder="账号状态" clearable>
              <el-option label="启用" value="1" />
              <el-option label="禁用" value="0" />
              <el-option label="锁定" value="2" />
            </el-select>
          </el-col>
          <el-col :span="8">
            <el-button type="primary" :icon="Search" @click="handleSearch">
              搜索
            </el-button>
            <el-button :icon="Refresh" @click="handleReset">
              重置
            </el-button>
            <el-button type="success" :icon="Plus" @click="handleAdd">
              新增用户
            </el-button>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 用户列表 -->
    <div class="table-container">
      <el-card>
        <el-table
          v-loading="loading"
          :data="userList"
          stripe
          border
          style="width: 100%"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="真实姓名" width="120" />
          <el-table-column prop="email" label="邮箱" width="180" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="userType" label="用户类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getUserTypeTagType(row.userType)">
                {{ getUserTypeText(row.userType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button 
                :type="row.status === 1 ? 'warning' : 'success'" 
                size="small" 
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Search,
  Refresh,
  Plus
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const userList = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  userType: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 模拟用户数据
const mockUsers = [
  {
    id: 1,
    username: 'admin',
    realName: '系统管理员',
    email: 'admin@example.com',
    phone: '13800138000',
    userType: 1,
    status: 1,
    lastLoginTime: '2025-01-19 14:30:00',
    createTime: '2025-01-01 10:00:00'
  },
  {
    id: 2,
    username: 'john_doe',
    realName: '张三',
    email: 'john@example.com',
    phone: '13800138001',
    userType: 1,
    status: 1,
    lastLoginTime: '2025-01-19 12:15:00',
    createTime: '2025-01-10 09:30:00'
  },
  {
    id: 3,
    username: 'enterprise_user',
    realName: '企业用户',
    email: 'enterprise@example.com',
    phone: '13800138002',
    userType: 2,
    status: 1,
    lastLoginTime: '2025-01-18 16:45:00',
    createTime: '2025-01-15 14:20:00'
  },
  {
    id: 4,
    username: 'airline_user',
    realName: '航司用户',
    email: 'airline@example.com',
    phone: '13800138003',
    userType: 3,
    status: 0,
    lastLoginTime: '2025-01-17 11:20:00',
    createTime: '2025-01-12 16:10:00'
  }
]

// 方法
const getUserTypeText = (type) => {
  const typeMap = {
    1: '个人用户',
    2: '企业用户',
    3: '航司用户'
  }
  return typeMap[type] || '未知'
}

const getUserTypeTagType = (type) => {
  const typeMap = {
    1: '',
    2: 'success',
    3: 'warning'
  }
  return typeMap[type] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '禁用',
    1: '启用',
    2: '锁定'
  }
  return statusMap[status] || '未知'
}

const getStatusTagType = (status) => {
  const statusMap = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return statusMap[status] || 'info'
}

const loadUsers = async () => {
  loading.value = true
  try {
    // 模拟 API 调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 应用搜索过滤
    let filteredUsers = [...mockUsers]
    
    if (searchForm.keyword) {
      filteredUsers = filteredUsers.filter(user => 
        user.username.includes(searchForm.keyword) ||
        user.realName.includes(searchForm.keyword) ||
        user.email.includes(searchForm.keyword) ||
        user.phone.includes(searchForm.keyword)
      )
    }
    
    if (searchForm.userType) {
      filteredUsers = filteredUsers.filter(user => 
        user.userType.toString() === searchForm.userType
      )
    }
    
    if (searchForm.status) {
      filteredUsers = filteredUsers.filter(user => 
        user.status.toString() === searchForm.status
      )
    }
    
    // 分页处理
    const start = (pagination.page - 1) * pagination.size
    const end = start + pagination.size
    userList.value = filteredUsers.slice(start, end)
    pagination.total = filteredUsers.length
    
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadUsers()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.userType = ''
  searchForm.status = ''
  pagination.page = 1
  loadUsers()
}

const handleAdd = () => {
  ElMessage.info('新增用户功能开发中...')
}

const handleEdit = (row) => {
  ElMessage.info(`编辑用户: ${row.username}`)
}

const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${row.realName}" 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟 API 调用
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success(`${action}成功`)
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.realName}" 吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟 API 调用
    const index = userList.value.findIndex(user => user.id === row.id)
    if (index > -1) {
      userList.value.splice(index, 1)
      pagination.total--
    }
    ElMessage.success('删除成功')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadUsers()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadUsers()
}

// 生命周期
onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.users-container {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.page-subtitle {
  color: #7f8c8d;
  margin: 0;
}

.toolbar {
  margin-bottom: 16px;
}

.table-container {
  margin-bottom: 24px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .users-container {
    padding: 16px;
  }
  
  .toolbar .el-row {
    flex-direction: column;
    gap: 16px;
  }
  
  .toolbar .el-col {
    width: 100%;
  }
}
</style>
