<template>
  <div class="profile">
    <el-row :gutter="20">
      <!-- 用户信息卡片 -->
      <el-col :span="8">
        <el-card class="user-card">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          
          <div class="user-info">
            <div class="avatar-section">
              <el-avatar :src="userInfo?.avatar" :size="80">
                {{ userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <h3>{{ userInfo?.realName || userInfo?.username }}</h3>
              <el-tag :type="userInfo?.status === 1 ? 'success' : 'danger'">
                {{ userInfo?.statusDesc }}
              </el-tag>
            </div>
            
            <el-divider />
            
            <div class="info-list">
              <div class="info-item">
                <span class="label">用户名:</span>
                <span class="value">{{ userInfo?.username }}</span>
              </div>
              <div class="info-item">
                <span class="label">手机号:</span>
                <span class="value">{{ userInfo?.phone || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">邮箱:</span>
                <span class="value">{{ userInfo?.email || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">用户类型:</span>
                <span class="value">{{ userInfo?.userTypeDesc }}</span>
              </div>
              <div class="info-item">
                <span class="label">最后登录:</span>
                <span class="value">{{ formatDate(userInfo?.lastLoginTime) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 权限信息 -->
      <el-col :span="16">
        <el-card class="permissions-card">
          <template #header>
            <div class="card-header">
              <span>权限信息</span>
              <el-button size="small" @click="refreshPermissions">刷新</el-button>
            </div>
          </template>
          
          <el-tabs v-model="activeTab">
            <el-tab-pane label="角色信息" name="roles">
              <div class="roles-section">
                <el-empty v-if="roles.length === 0" description="暂无角色信息" />
                <div v-else class="roles-list">
                  <el-tag
                    v-for="role in roles"
                    :key="role"
                    size="large"
                    class="role-tag"
                  >
                    {{ role }}
                  </el-tag>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="权限列表" name="permissions">
              <div class="permissions-section">
                <el-empty v-if="permissions.length === 0" description="暂无权限信息" />
                <div v-else class="permissions-list">
                  <el-tag
                    v-for="permission in permissions"
                    :key="permission"
                    type="info"
                    size="small"
                    class="permission-tag"
                  >
                    {{ permission }}
                  </el-tag>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
        
        <!-- 操作按钮 -->
        <el-card class="actions-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>账号操作</span>
            </div>
          </template>
          
          <el-space>
            <el-button type="primary" @click="refreshToken">
              刷新Token
            </el-button>
            <el-button type="warning" @click="logout">
              退出登录
            </el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref('roles')

const userInfo = computed(() => authStore.userInfo)
const roles = computed(() => authStore.roles)
const permissions = computed(() => authStore.permissions)

const formatDate = (dateStr) => {
  if (!dateStr) return '未知'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const refreshPermissions = async () => {
  try {
    await authStore.fetchPermissions()
    ElMessage.success('权限信息刷新成功')
  } catch (error) {
    ElMessage.error('权限信息刷新失败')
  }
}

const refreshToken = async () => {
  try {
    await authStore.refreshToken()
    ElMessage.success('Token刷新成功')
  } catch (error) {
    ElMessage.error('Token刷新失败')
  }
}

const logout = async () => {
  await authStore.logout()
  router.push('/')
}
</script>

<style scoped>
.profile {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.user-info {
  text-align: center;
}

.avatar-section h3 {
  margin: 15px 0 10px;
  color: #303133;
}

.info-list {
  text-align: left;
  margin-top: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  color: #606266;
  font-weight: 500;
}

.value {
  color: #303133;
}

.roles-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.role-tag {
  margin: 5px;
}

.permissions-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.permission-tag {
  margin: 3px;
}
</style>
