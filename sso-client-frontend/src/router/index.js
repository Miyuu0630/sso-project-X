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
  // 角色化仪表板路由
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: {
      requiresAuth: true,
      title: '仪表板'
    }
  },
  {
    path: '/dashboard/admin',
    name: 'AdminDashboard',
    component: () => import('../views/dashboard/AdminDashboard.vue'),
    meta: {
      requiresAuth: true,
      title: '管理员控制台',
      requiredRoles: ['ADMIN']
    }
  },
  {
    path: '/dashboard/personal',
    name: 'PersonalDashboard',
    component: () => import('../views/dashboard/PersonalDashboard.vue'),
    meta: {
      requiresAuth: true,
      title: '个人中心',
      requiredRoles: ['PERSONAL_USER']
    }
  },
  {
    path: '/dashboard/enterprise',
    name: 'EnterpriseDashboard',
    component: () => import('../views/dashboard/EnterpriseDashboard.vue'),
    meta: {
      requiresAuth: true,
      title: '企业管理',
      requiredRoles: ['ENTERPRISE_USER']
    }
  },
  {
    path: '/dashboard/airline',
    name: 'AirlineDashboard',
    component: () => import('../views/dashboard/AirlineDashboard.vue'),
    meta: {
      requiresAuth: true,
      title: '航司管理',
      requiredRoles: ['AIRLINE_USER']
    }
  },
  // 用户中心路由（所有角色共享）
  {
    path: '/user',
    name: 'UserCenter',
    redirect: '/user/profile',
    meta: {
      requiresAuth: true,
      title: '用户中心'
    },
    children: [
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('../views/user/Profile.vue'),
        meta: {
          requiresAuth: true,
          title: '个人资料'
        }
      },
      {
        path: 'security',
        name: 'UserSecurity',
        component: () => import('../views/user/Security.vue'),
        meta: {
          requiresAuth: true,
          title: '安全设置'
        }
      },
      {
        path: 'oauth',
        name: 'UserOAuth',
        component: () => import('../views/user/OAuth.vue'),
        meta: {
          requiresAuth: true,
          title: '第三方绑定'
        }
      },
      {
        path: 'device',
        name: 'UserDevice',
        component: () => import('../views/user/Device.vue'),
        meta: {
          requiresAuth: true,
          title: '设备管理'
        }
      },
      {
        path: 'loginlog',
        name: 'UserLoginLog',
        component: () => import('../views/user/LoginLog.vue'),
        meta: {
          requiresAuth: true,
          title: '登录记录'
        }
      }
    ]
  },
  // 系统管理路由（仅管理员）
  {
    path: '/system',
    name: 'SystemManagement',
    redirect: '/system/user',
    meta: {
      requiresAuth: true,
      title: '系统管理',
      requiredRoles: ['ADMIN']
    },
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('../views/system/User.vue'),
        meta: {
          requiresAuth: true,
          title: '用户管理',
          requiredRoles: ['ADMIN'],
          requiredPermission: 'system:user:list'
        }
      },
      {
        path: 'role',
        name: 'SystemRole',
        component: () => import('../views/system/Role.vue'),
        meta: {
          requiresAuth: true,
          title: '角色管理',
          requiredRoles: ['ADMIN'],
          requiredPermission: 'system:role:list'
        }
      },
      {
        path: 'menu',
        name: 'SystemMenu',
        component: () => import('../views/system/Menu.vue'),
        meta: {
          requiresAuth: true,
          title: '菜单管理',
          requiredRoles: ['ADMIN'],
          requiredPermission: 'system:menu:list'
        }
      }
    ]
  },
  // 企业管理路由（仅企业用户）
  {
    path: '/enterprise',
    name: 'EnterpriseManagement',
    redirect: '/enterprise/info',
    meta: {
      requiresAuth: true,
      title: '企业管理',
      requiredRoles: ['ENTERPRISE_USER']
    },
    children: [
      {
        path: 'info',
        name: 'EnterpriseInfo',
        component: () => import('../views/enterprise/Info.vue'),
        meta: {
          requiresAuth: true,
          title: '企业信息',
          requiredRoles: ['ENTERPRISE_USER']
        }
      },
      {
        path: 'member',
        name: 'EnterpriseMember',
        component: () => import('../views/enterprise/Member.vue'),
        meta: {
          requiresAuth: true,
          title: '成员管理',
          requiredRoles: ['ENTERPRISE_USER']
        }
      },
      {
        path: 'auth',
        name: 'EnterpriseAuth',
        component: () => import('../views/enterprise/Auth.vue'),
        meta: {
          requiresAuth: true,
          title: '企业认证',
          requiredRoles: ['ENTERPRISE_USER']
        }
      }
    ]
  },
  // 航司管理路由（仅航司用户）
  {
    path: '/airline',
    name: 'AirlineManagement',
    redirect: '/airline/info',
    meta: {
      requiresAuth: true,
      title: '航司管理',
      requiredRoles: ['AIRLINE_USER']
    },
    children: [
      {
        path: 'info',
        name: 'AirlineInfo',
        component: () => import('../views/airline/Info.vue'),
        meta: {
          requiresAuth: true,
          title: '航司信息',
          requiredRoles: ['AIRLINE_USER']
        }
      },
      {
        path: 'flight',
        name: 'AirlineFlight',
        component: () => import('../views/airline/Flight.vue'),
        meta: {
          requiresAuth: true,
          title: '航班管理',
          requiredRoles: ['AIRLINE_USER']
        }
      },
      {
        path: 'passenger',
        name: 'AirlinePassenger',
        component: () => import('../views/airline/Passenger.vue'),
        meta: {
          requiresAuth: true,
          title: '乘客管理',
          requiredRoles: ['AIRLINE_USER']
        }
      }
    ]
  },
  // 系统监控路由（仅管理员）
  {
    path: '/monitor',
    name: 'SystemMonitor',
    redirect: '/monitor/online',
    meta: {
      requiresAuth: true,
      title: '系统监控',
      requiredRoles: ['ADMIN']
    },
    children: [
      {
        path: 'online',
        name: 'MonitorOnline',
        component: () => import('../views/monitor/Online.vue'),
        meta: {
          requiresAuth: true,
          title: '在线用户',
          requiredRoles: ['ADMIN']
        }
      },
      {
        path: 'loginlog',
        name: 'MonitorLoginLog',
        component: () => import('../views/monitor/LoginLog.vue'),
        meta: {
          requiresAuth: true,
          title: '登录日志',
          requiredRoles: ['ADMIN']
        }
      },
      {
        path: 'server',
        name: 'MonitorServer',
        component: () => import('../views/monitor/Server.vue'),
        meta: {
          requiresAuth: true,
          title: '服务监控',
          requiredRoles: ['ADMIN']
        }
      }
    ]
  },
  // 通用路由
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
    path: '/sso-test',
    name: 'SsoTest',
    component: () => import('../views/SsoTest.vue'),
    meta: {
      requiresAuth: false,
      title: 'SSO测试'
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

// 角色到仪表板的映射
const roleDashboardMap = {
  'ADMIN': '/dashboard/admin',
  'PERSONAL_USER': '/dashboard/personal',
  'ENTERPRISE_USER': '/dashboard/enterprise',
  'AIRLINE_USER': '/dashboard/airline'
}

// 获取用户主要角色
const getUserPrimaryRole = (userRoles) => {
  const roleHierarchy = ['ADMIN', 'AIRLINE_USER', 'ENTERPRISE_USER', 'PERSONAL_USER']
  for (const role of roleHierarchy) {
    if (userRoles.includes(role)) {
      return role
    }
  }
  return 'PERSONAL_USER' // 默认角色
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  console.log(`路由跳转: ${from.path} -> ${to.path}`)
  console.log('当前认证状态:', {
    isLoggedIn: authStore.isLoggedIn,
    userInfo: authStore.userInfo,
    roles: authStore.roles,
    primaryRole: authStore.primaryRole
  })

  // 特殊处理：访问根路径时，根据用户登录状态决定跳转
  if (to.path === '/') {
    if (authStore.isLoggedIn) {
      // 已登录用户，根据角色重定向到对应仪表板
      const userRoles = authStore.userInfo?.roles || []
      const primaryRole = getUserPrimaryRole(userRoles)
      const dashboardPath = roleDashboardMap[primaryRole]

      if (dashboardPath) {
        console.log(`用户角色 ${primaryRole}，重定向到 ${dashboardPath}`)
        next(dashboardPath)
        return
      }
    } else {
      // 未登录用户，直接跳转到SSO登录页面
      console.log('用户未登录，访问根路径时直接跳转到SSO登录')
      authStore.redirectToLogin('/')
      return
    }
  }

  // 特殊处理：访问通用仪表板路径时，重定向到角色专用仪表板
  if (to.path === '/dashboard' && authStore.isLoggedIn) {
    const userRoles = authStore.userInfo?.roles || []
    const primaryRole = getUserPrimaryRole(userRoles)
    const dashboardPath = roleDashboardMap[primaryRole]

    if (dashboardPath) {
      console.log(`重定向到角色专用仪表板: ${dashboardPath}`)
      next(dashboardPath)
      return
    }
  }

  // 检查是否需要登录（排除根路径和回调页面，因为上面已经处理了）
  if (to.meta.requiresAuth && to.path !== '/' && to.path !== '/callback') {
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

    // 如果用户信息还没有加载完成，先加载用户信息
    if (!authStore.userInfo || !authStore.roles || authStore.roles.length === 0) {
      console.log('用户信息未加载完成，正在加载...')
      try {
        await authStore.fetchUserData()
      } catch (error) {
        console.error('加载用户信息失败:', error)
        authStore.clearAuth()
        authStore.redirectToLogin(to.fullPath)
        return
      }
    }

    // 检查角色权限
    if (to.meta.requiredRoles && to.meta.requiredRoles.length > 0) {
      const userRoles = authStore.userInfo?.roles || []
      console.log('检查角色权限:', {
        requiredRoles: to.meta.requiredRoles,
        userRoles: userRoles,
        currentPath: to.path
      })
      
      const hasRequiredRole = to.meta.requiredRoles.some(role => userRoles.includes(role))

      if (!hasRequiredRole) {
        console.log('用户权限不足，跳转到对应角色仪表板')
        const primaryRole = getUserPrimaryRole(userRoles)
        const dashboardPath = roleDashboardMap[primaryRole] || '/'
        
        console.log('权限检查结果:', {
          primaryRole,
          dashboardPath,
          currentPath: to.path
        })
        
        // 简化逻辑：如果用户要访问的是他们角色对应的仪表板，直接允许
        if (to.path === dashboardPath) {
          console.log('用户访问对应角色仪表板，允许访问')
          next()
          return
        }
        
        // 否则重定向到对应仪表板
        console.log(`重定向到: ${dashboardPath}`)
        next(dashboardPath)
        return
      } else {
        console.log('角色权限检查通过')
      }
    }

    // 检查具体权限
    if (to.meta.requiredPermission) {
      console.log('检查具体权限:', to.meta.requiredPermission)
      const hasPermission = await authStore.checkPermission(to.meta.requiredPermission)
      if (!hasPermission) {
        console.log('用户权限不足，跳转到对应角色仪表板')
        const userRoles = authStore.userInfo?.roles || []
        const primaryRole = getUserPrimaryRole(userRoles)
        const dashboardPath = roleDashboardMap[primaryRole] || '/'
        
        console.log('权限检查结果:', {
          requiredPermission: to.meta.requiredPermission,
          primaryRole,
          dashboardPath,
          currentPath: to.path
        })
        
        // 简化逻辑：如果用户要访问的是他们角色对应的仪表板，直接允许
        if (to.path === dashboardPath) {
          console.log('用户访问对应角色仪表板，允许访问')
          next()
          return
        }
        
        // 否则重定向到对应仪表板
        console.log(`重定向到: ${dashboardPath}`)
        next(dashboardPath)
        return
      } else {
        console.log('具体权限检查通过')
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
