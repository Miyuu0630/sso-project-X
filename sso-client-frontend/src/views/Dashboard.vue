<template>
  <div class="dashboard-container">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-loading-spinner size="large" />
      <p>正在加载您的专属仪表板...</p>
    </div>

    <!-- 角色识别失败 -->
    <div v-else-if="!userRole" class="error-container">
      <el-result
        icon="warning"
        title="无法识别用户角色"
        sub-title="请联系管理员检查您的账号权限设置"
      >
        <template #extra>
          <el-button type="primary" @click="refreshUserInfo">
            重新获取用户信息
          </el-button>
          <el-button @click="logout">
            重新登录
          </el-button>
        </template>
      </el-result>
    </div>

    <!-- 动态加载对应角色的仪表板组件 -->
    <component
      v-else
      :is="dashboardComponent"
      :key="userRole"
      @role-changed="handleRoleChanged"
    />
  </div>

</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

// 导入各角色仪表板组件
import AdminDashboard from './dashboard/AdminDashboard.vue'
import PersonalDashboard from './dashboard/PersonalDashboard.vue'
import EnterpriseDashboard from './dashboard/EnterpriseDashboard.vue'
import AirlineDashboard from './dashboard/AirlineDashboard.vue'

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loading = ref(true)
const userRole = ref('')

// 角色到组件的映射
const dashboardComponents = {
  'ADMIN': AdminDashboard,
  'PERSONAL_USER': PersonalDashboard,
  'ENTERPRISE_USER': EnterpriseDashboard,
  'AIRLINE_USER': AirlineDashboard
}

// 角色到路径的映射
const roleDashboardMap = {
  'ADMIN': '/dashboard/admin',
  'PERSONAL_USER': '/dashboard/personal',
  'ENTERPRISE_USER': '/dashboard/enterprise',
  'AIRLINE_USER': '/dashboard/airline'
}

// 计算属性
const dashboardComponent = computed(() => {
  return dashboardComponents[userRole.value] || PersonalDashboard
})

// 获取用户主要角色
const getUserPrimaryRole = (userRoles) => {
  if (!userRoles || userRoles.length === 0) {
    return null
  }

  // 角色优先级：管理员 > 航司用户 > 企业用户 > 个人用户
  const roleHierarchy = ['ADMIN', 'AIRLINE_USER', 'ENTERPRISE_USER', 'PERSONAL_USER']

  for (const role of roleHierarchy) {
    if (userRoles.includes(role)) {
      return role
    }
  }

  // 如果没有匹配到预定义角色，返回第一个角色
  return userRoles[0]
}

// 初始化用户角色
const initializeUserRole = async () => {
  try {
    loading.value = true

    // 确保用户信息已加载
    if (!authStore.userInfo) {
      await authStore.fetchUserData()
    }

    const userRoles = authStore.userInfo?.roles || []
    const primaryRole = getUserPrimaryRole(userRoles)

    if (primaryRole) {
      userRole.value = primaryRole

      // 如果当前路径是通用仪表板路径，重定向到角色专用路径
      if (router.currentRoute.value.path === '/dashboard') {
        const targetPath = roleDashboardMap[primaryRole]
        if (targetPath) {
          await router.replace(targetPath)
        }
      }
    } else {
      console.warn('用户没有有效的角色')
      userRole.value = null
    }
  } catch (error) {
    console.error('初始化用户角色失败:', error)
    ElMessage.error('加载用户信息失败')
    userRole.value = null
  } finally {
    loading.value = false
  }
}

// 刷新用户信息
const refreshUserInfo = async () => {
  try {
    await authStore.fetchUserData()
    await initializeUserRole()
    ElMessage.success('用户信息已更新')
  } catch (error) {
    ElMessage.error('刷新用户信息失败')
  }
}

// 登出
const logout = async () => {
  await authStore.logout()
}

// 处理角色变更
const handleRoleChanged = async () => {
  await initializeUserRole()
}

// 监听用户信息变化
watch(
  () => authStore.userInfo,
  (newUserInfo) => {
    if (newUserInfo) {
      const userRoles = newUserInfo.roles || []
      const primaryRole = getUserPrimaryRole(userRoles)
      if (primaryRole !== userRole.value) {
        userRole.value = primaryRole
      }
    }
  },
  { deep: true }
)

// 生命周期
onMounted(() => {
  initializeUserRole()
})
</script>

<style scoped>
.dashboard-container {
  min-height: calc(100vh - 60px);
  background-color: #f5f7fa;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: calc(100vh - 60px);
  gap: 20px;
}

.loading-container p {
  color: #909399;
  font-size: 16px;
  margin: 0;
}

.error-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: calc(100vh - 60px);
  padding: 20px;
}

/* 确保动态组件占满容器 */
.dashboard-container > * {
  width: 100%;
  height: 100%;
}
</style>
