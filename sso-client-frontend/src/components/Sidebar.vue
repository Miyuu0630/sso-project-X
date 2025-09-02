<template>
  <el-aside class="sidebar" :width="isCollapse ? '64px' : '200px'">
    <div class="sidebar-header">
      <div class="logo" v-if="!isCollapse">
        <h3>业务系统</h3>
      </div>
      <div class="logo-collapsed" v-else>
        <el-icon size="24"><Monitor /></el-icon>
      </div>
    </div>
    
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :unique-opened="true"
      :router="true"
      class="sidebar-menu"
    >
      <!-- 仪表板 -->
      <el-menu-item index="/dashboard" v-if="hasPermission('dashboard')">
        <el-icon><House /></el-icon>
        <template #title>仪表板</template>
      </el-menu-item>

      <!-- 用户中心 -->
      <el-sub-menu index="user" v-if="hasPermission('user')">
        <template #title>
          <el-icon><User /></el-icon>
          <span>用户中心</span>
        </template>
        <el-menu-item index="/user/profile">个人资料</el-menu-item>
        <el-menu-item index="/user/security">安全设置</el-menu-item>
        <el-menu-item index="/user/oauth">第三方绑定</el-menu-item>
        <el-menu-item index="/user/device">设备管理</el-menu-item>
        <el-menu-item index="/user/loginlog">登录记录</el-menu-item>
      </el-sub-menu>

      <!-- 系统管理 -->
      <el-sub-menu index="system" v-if="hasPermission('system')">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/system/user">用户管理</el-menu-item>
        <el-menu-item index="/system/role">角色管理</el-menu-item>
        <el-menu-item index="/system/menu">菜单管理</el-menu-item>
      </el-sub-menu>

      <!-- 企业管理 -->
      <el-sub-menu index="enterprise" v-if="hasPermission('enterprise')">
        <template #title>
          <el-icon><OfficeBuilding /></el-icon>
          <span>企业管理</span>
        </template>
        <el-menu-item index="/enterprise/info">企业信息</el-menu-item>
        <el-menu-item index="/enterprise/member">成员管理</el-menu-item>
        <el-menu-item index="/enterprise/auth">企业认证</el-menu-item>
      </el-sub-menu>

      <!-- 航司管理 -->
      <el-sub-menu index="airline" v-if="hasPermission('airline')">
        <template #title>
          <el-icon><Ship /></el-icon>
          <span>航司管理</span>
        </template>
        <el-menu-item index="/airline/info">航司信息</el-menu-item>
        <el-menu-item index="/airline/flight">航班管理</el-menu-item>
        <el-menu-item index="/airline/passenger">乘客管理</el-menu-item>
      </el-sub-menu>

      <!-- 系统监控 -->
      <el-sub-menu index="monitor" v-if="hasPermission('monitor')">
        <template #title>
          <el-icon><Monitor /></el-icon>
          <span>系统监控</span>
        </template>
        <el-menu-item index="/monitor/online">在线用户</el-menu-item>
        <el-menu-item index="/monitor/loginlog">登录日志</el-menu-item>
        <el-menu-item index="/monitor/server">服务监控</el-menu-item>
      </el-sub-menu>
    </el-menu>

    <!-- 折叠按钮 -->
    <div class="sidebar-footer">
      <el-button 
        type="text" 
        @click="toggleCollapse"
        class="collapse-btn"
      >
        <el-icon>
          <component :is="isCollapse ? 'Expand' : 'Fold'" />
        </el-icon>
      </el-button>
    </div>
  </el-aside>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Monitor, House, User, Setting, OfficeBuilding, Ship,
  Expand, Fold
} from '@element-plus/icons-vue'

const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const isCollapse = ref(false)

// 计算属性
const activeMenu = computed(() => {
  return route.path
})

// 权限检查
const hasPermission = (module) => {
  const userRoles = authStore.userInfo?.roles || []
  
  // 根据用户角色判断是否有权限访问对应模块
  switch (module) {
    case 'dashboard':
      return true // 所有用户都可以访问仪表板
    case 'user':
      return true // 所有用户都可以访问用户中心
    case 'system':
      return userRoles.includes('ADMIN')
    case 'enterprise':
      return userRoles.includes('ENTERPRISE_USER') || userRoles.includes('ADMIN')
    case 'airline':
      return userRoles.includes('AIRLINE_USER') || userRoles.includes('ADMIN')
    case 'monitor':
      return userRoles.includes('ADMIN')
    default:
      return false
  }
}

// 方法
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 监听路由变化，自动展开对应的菜单
watch(route, (newRoute) => {
  // 根据当前路由自动展开对应的子菜单
  const path = newRoute.path
  if (path.startsWith('/user')) {
    // 用户中心菜单会自动展开
  } else if (path.startsWith('/system')) {
    // 系统管理菜单会自动展开
  } else if (path.startsWith('/enterprise')) {
    // 企业管理菜单会自动展开
  } else if (path.startsWith('/airline')) {
    // 航司管理菜单会自动展开
  } else if (path.startsWith('/monitor')) {
    // 系统监控菜单会自动展开
  }
}, { immediate: true })
</script>

<style scoped>
.sidebar {
  background-color: #ffffff;
  transition: width 0.3s;
  overflow: hidden;
  border-right: 1px solid #e4e7ed;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e4e7ed;
  background-color: #ffffff;
}

.logo h3 {
  color: #303133;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.logo-collapsed {
  color: #303133;
}

.sidebar-menu {
  border: none;
  background-color: #ffffff;
  height: calc(100vh - 120px);
  overflow-y: auto;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

.sidebar-menu .el-menu-item,
.sidebar-menu .el-sub-menu__title {
  color: #606266;
  background-color: #ffffff;
  border-bottom: 1px solid #f5f7fa;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-sub-menu__title:hover {
  background-color: #f0f9ff;
  color: #409eff;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #409eff;
  color: #ffffff;
  border-right: 3px solid #409eff;
}

.sidebar-menu .el-sub-menu .el-menu-item {
  background-color: #fafafa;
  color: #606266;
  padding-left: 50px !important;
}

.sidebar-menu .el-sub-menu .el-menu-item:hover {
  background-color: #f0f9ff;
  color: #409eff;
}

.sidebar-menu .el-sub-menu .el-menu-item.is-active {
  background-color: #409eff;
  color: #ffffff;
  border-right: 3px solid #409eff;
}

.sidebar-footer {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid #e4e7ed;
  background-color: #ffffff;
}

.collapse-btn {
  color: #606266;
  font-size: 18px;
  padding: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.collapse-btn:hover {
  color: #409eff;
  background-color: #f0f9ff;
}

/* 滚动条样式 */
.sidebar-menu::-webkit-scrollbar {
  width: 6px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: #f5f7fa;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

/* 子菜单展开图标样式 */
.sidebar-menu .el-sub-menu .el-sub-menu__icon-arrow {
  color: #606266;
}

.sidebar-menu .el-sub-menu:hover .el-sub-menu__icon-arrow {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s;
  }
  
  .sidebar.mobile-open {
    transform: translateX(0);
  }
}
</style>
