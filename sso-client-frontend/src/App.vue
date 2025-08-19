<template>
  <div id="app">
    <el-container class="layout-container">
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-content">
          <div class="logo">
            <h2>SSO Client - 业务系统</h2>
          </div>
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
      </el-header>

      <!-- 主要内容区域 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from './stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const userInfo = computed(() => authStore.userInfo)

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
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
}

.layout-header {
  background-color: #545c64;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.logo h2 {
  margin: 0;
  color: white;
}

.user-info {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  color: white;
  cursor: pointer;
}

.username {
  margin: 0 8px;
}

.layout-main {
  padding: 20px;
  background-color: #f5f5f5;
}
</style>
