/**
 * Vue Router 配置
 * 包含 SSO 认证路由守卫和回调处理
 */

import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// ========================================
// 路由定义
// ========================================

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/auth/callback',
    name: 'AuthCallback',
    component: () => import('@/views/AuthCallback.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/business',
    name: 'Business',
    children: [
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/business/Orders.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/business/Reports.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN', 'ENTERPRISE_USER'] }
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/404.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// ========================================
// 创建路由实例
// ========================================

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// ========================================
// 全局路由守卫
// ========================================

/**
 * 全局前置守卫 - SSO 认证检查
 */
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  console.log(`路由跳转: ${from.path} -> ${to.path}`)
  
  // 不需要认证的页面直接放行
  if (!to.meta.requiresAuth) {
    next()
    return
  }
  
  // 检查是否已登录
  if (!authStore.isLoggedIn) {
    console.log('用户未登录，重定向到 SSO 登录')
    authStore.redirectToLogin(window.location.href)
    return
  }
  
  // 验证 Token 有效性
  const isTokenValid = await authStore.checkTokenValidity()
  if (!isTokenValid) {
    console.log('Token 无效，重定向到 SSO 登录')
    authStore.redirectToLogin(window.location.href)
    return
  }
  
  // 检查角色权限
  if (to.meta.roles && to.meta.roles.length > 0) {
    const userRoles = authStore.userInfo?.roles || []
    const hasRequiredRole = to.meta.roles.some(role => userRoles.includes(role))
    
    if (!hasRequiredRole) {
      console.log('用户权限不足，跳转到首页')
      next('/')
      return
    }
  }
  
  // 通过所有检查，允许访问
  next()
})

/**
 * 全局后置守卫 - 页面标题设置
 */
router.afterEach((to) => {
  // 设置页面标题
  const defaultTitle = '企业业务系统'
  document.title = to.meta.title ? `${to.meta.title} - ${defaultTitle}` : defaultTitle
})

export default router

// ========================================
// SSO 回调页面组件
// ========================================

/**
 * AuthCallback.vue - SSO 登录回调处理页面
 */
export const AuthCallbackComponent = {
  template: `
    <div class="auth-callback-container">
      <div class="callback-content">
        <div class="loading-spinner">
          <div class="spinner"></div>
        </div>
        <h2 class="callback-title">正在处理登录信息...</h2>
        <p class="callback-message">{{ message }}</p>
        
        <div v-if="error" class="error-section">
          <el-alert
            :title="error"
            type="error"
            :closable="false"
          />
          <el-button 
            type="primary" 
            @click="retryLogin"
            style="margin-top: 16px;"
          >
            重新登录
          </el-button>
        </div>
      </div>
    </div>
  `,
  
  setup() {
    import { ref, onMounted } from 'vue'
    import { useRouter } from 'vue-router'
    import { useAuthStore } from '@/stores/auth'
    
    const router = useRouter()
    const authStore = useAuthStore()
    
    const message = ref('正在验证登录凭证...')
    const error = ref('')
    
    const handleCallback = async () => {
      try {
        const urlParams = new URLSearchParams(window.location.search)
        const token = urlParams.get('token')
        const returnUrl = urlParams.get('return_url') || '/'
        
        if (!token) {
          throw new Error('未收到有效的登录凭证')
        }
        
        message.value = '正在验证 Token...'
        
        // 存储 Token
        authStore.setToken(token)
        
        // 验证 Token 有效性
        const isValid = await authStore.checkTokenValidity()
        
        if (isValid) {
          message.value = '登录成功，正在跳转...'
          
          // 延迟跳转，让用户看到成功信息
          setTimeout(() => {
            window.location.href = returnUrl
          }, 1000)
        } else {
          throw new Error('Token 验证失败')
        }
        
      } catch (err) {
        console.error('SSO 回调处理失败:', err)
        error.value = err.message || 'SSO 登录处理失败'
        authStore.clearAuth()
      }
    }
    
    const retryLogin = () => {
      authStore.redirectToLogin()
    }
    
    onMounted(() => {
      handleCallback()
    })
    
    return {
      message,
      error,
      retryLogin
    }
  }
}

// ========================================
// 路由工具函数
// ========================================

/**
 * 检查用户是否有指定权限
 */
export const hasPermission = (permission) => {
  const authStore = useAuthStore()
  const userPermissions = authStore.userInfo?.permissions || []
  return userPermissions.includes(permission)
}

/**
 * 检查用户是否有指定角色
 */
export const hasRole = (role) => {
  const authStore = useAuthStore()
  const userRoles = authStore.userInfo?.roles || []
  return userRoles.includes(role)
}

/**
 * 获取当前用户信息
 */
export const getCurrentUser = () => {
  const authStore = useAuthStore()
  return authStore.userInfo
}

/**
 * 安全跳转 - 检查权限后跳转
 */
export const safeNavigate = (to, requiredRoles = []) => {
  const authStore = useAuthStore()
  
  if (!authStore.isLoggedIn) {
    authStore.redirectToLogin()
    return false
  }
  
  if (requiredRoles.length > 0) {
    const userRoles = authStore.userInfo?.roles || []
    const hasRequiredRole = requiredRoles.some(role => userRoles.includes(role))
    
    if (!hasRequiredRole) {
      console.warn('权限不足，无法访问该页面')
      return false
    }
  }
  
  router.push(to)
  return true
}
