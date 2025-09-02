<template>
  <div id="app">
    <el-container class="layout-container">
      <!-- 左侧导航菜单 -->
      <Sidebar v-if="authStore.isLoggedIn" />
      
      <el-container class="main-container">
        <!-- 顶部导航 -->
        <el-header class="layout-header">
          <div class="header-content">
            <div class="header-left">
              <el-button 
                v-if="authStore.isLoggedIn" 
                type="text" 
                @click="toggleSidebar"
                class="sidebar-toggle"
              >
                <el-icon><Expand /></el-icon>
              </el-button>
              <div class="logo">
                <h2>SSO Client - 业务系统</h2>
              </div>
            </div>
            <div class="header-right">
              <div class="user-info" v-if="authStore.isLoggedIn">
                <el-dropdown @command="handleCommand">
                  <span class="el-dropdown-link">
                    <el-avatar :src="userInfo?.avatar" :size="32">
                      {{ userInfo?.realName?.charAt(0) || 'U' }}
                    </el-avatar>
                    <span class="username">{{ userInfo?.realName || userInfo?.username }}</span>
                    <el-icon class="el-icon--right">
                      <arrow-down />
                    </el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                      <el-dropdown-item command="refresh">刷新Token</el-dropdown-item>
                      <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
              <div class="login-btn" v-else>
                <el-button type="primary" @click="login">登录</el-button>
              </div>
            </div>
          </div>
        </el-header>

        <!-- 主要内容区域 -->
        <el-main class="layout-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Expand } from '@element-plus/icons-vue'
import { useAuthStore } from './stores/auth'
import Sidebar from './components/Sidebar.vue'

const router = useRouter()
const authStore = useAuthStore()

const userInfo = computed(() => authStore.userInfo)

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'refresh':
      try {
        await authStore.refreshToken()
        ElMessage.success('Token刷新成功')
      } catch (error) {
        ElMessage.error('Token刷新失败')
      }
      break
    case 'logout':
      await authStore.logout()
      router.push('/')
      break
  }
}

const login = () => {
  authStore.redirectToLogin()
}

const toggleSidebar = () => {
  // 这里可以添加侧边栏折叠/展开的逻辑
  // 由于Sidebar组件内部已经处理了折叠逻辑，这里暂时留空
  console.log('Toggle sidebar')
}

onMounted(() => {
  // 检查URL中是否有SSO回调参数
  const urlParams = new URLSearchParams(window.location.search)
  if (urlParams.has('ticket') || urlParams.has('code')) {
    authStore.handleSsoCallback()
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f0f2f5;
}

.layout-header {
  background-color: #fff;
  color: #303133;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  height: 60px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sidebar-toggle {
  color: #606266;
  font-size: 18px;
  padding: 8px;
}

.sidebar-toggle:hover {
  color: #409eff;
  background-color: #f0f9ff;
}

.logo h2 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  color: #303133;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.2s;
}

.el-dropdown-link:hover {
  background-color: #f0f9ff;
  color: #409eff;
}

.username {
  margin: 0 8px;
  font-size: 14px;
}

.layout-main {
  padding: 20px;
  background-color: #f0f2f5;
  flex: 1;
  overflow-y: auto;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-header {
    padding: 0 16px;
  }
  
  .logo h2 {
    font-size: 16px;
  }
  
  .layout-main {
    padding: 16px;
  }
  
  .username {
    display: none;
  }
}
</style>
