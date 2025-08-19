import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      requiresAuth: false,
      title: '首页'
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: {
      requiresAuth: true,
      title: '个人中心'
    }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: {
      requiresAuth: true,
      title: '仪表板',
      requiredRoles: ['ADMIN']
    }
  },
  {
    path: '/users',
    name: 'Users',
    component: () => import('../views/Users.vue'),
    meta: {
      requiresAuth: true,
      title: '用户管理',
      requiredRoles: ['ADMIN'],
      requiredPermission: 'system:user:list'
    }
  },
  {
    path: '/callback',
    name: 'Callback',
    component: () => import('../views/Callback.vue'),
    meta: {
      requiresAuth: false,
      title: '登录回调'
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('../views/404.vue'),
    meta: {
      requiresAuth: false,
      title: '页面未找到'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  console.log(`路由跳转: ${from.path} -> ${to.path}`)

  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    if (!authStore.isLoggedIn) {
      console.log('用户未登录，重定向到 SSO 登录')
      // 未登录，跳转到SSO登录
      authStore.redirectToLogin(to.fullPath)
      return
    }

    // 验证 Token 有效性
    const isTokenValid = await authStore.checkTokenValidity()
    if (!isTokenValid) {
      console.log('Token 无效，重定向到 SSO 登录')
      authStore.redirectToLogin(to.fullPath)
      return
    }

    // 检查角色权限
    if (to.meta.requiredRoles && to.meta.requiredRoles.length > 0) {
      const userRoles = authStore.userInfo?.roles || []
      const hasRequiredRole = to.meta.requiredRoles.some(role => userRoles.includes(role))

      if (!hasRequiredRole) {
        console.log('用户权限不足，跳转到首页')
        next('/')
        return
      }
    }

    // 检查具体权限
    if (to.meta.requiredPermission) {
      const hasPermission = await authStore.checkPermission(to.meta.requiredPermission)
      if (!hasPermission) {
        console.log('用户权限不足，跳转到首页')
        next('/')
        return
      }
    }
  }

  next()
})

// 全局后置守卫 - 设置页面标题
router.afterEach((to) => {
  const defaultTitle = '企业业务系统'
  document.title = to.meta.title ? `${to.meta.title} - ${defaultTitle}` : defaultTitle
})

export default router
