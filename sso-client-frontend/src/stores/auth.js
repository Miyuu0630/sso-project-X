import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'
import Cookies from 'js-cookie'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref(Cookies.get('satoken') || '')
  const userInfo = ref(null)
  const permissions = ref([])
  const roles = ref([])
  const primaryRole = ref('')
  const dashboardPath = ref('/dashboard/personal')
  const userMenus = ref([])
  const roleFeatures = ref({})

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)

  // 公共函数：获取用户信息 + 权限
  const fetchUserData = async () => {
    try {
      // 先获取用户基本信息
      await fetchUserInfo()
      
      // 然后获取角色和权限信息
      await Promise.all([
        fetchPermissions(),
        fetchRoleInfo(),
        fetchUserMenus(),
        fetchRoleFeatures()
      ])
      
      console.log('用户数据获取完成:', {
        userInfo: userInfo.value,
        roles: roles.value,
        primaryRole: primaryRole.value,
        dashboardPath: dashboardPath.value
      })
    } catch (error) {
      console.error('获取用户数据失败:', error)
      throw error
    }
  }

  // 初始化认证状态
  const initAuth = async () => {
    // 如果在callback页面，跳过初始化检查，让callback页面处理登录流程
    if (window.location.pathname === '/callback') {
      console.log('在callback页面，跳过初始化检查')
      return
    }

    console.log('初始化认证状态，当前token:', token.value ? '存在' : '不存在')

    // 无论是否有token，都尝试验证登录状态
    // 因为SSO登录后可能有用户信息但token是临时的
    try {
      console.log('尝试验证登录状态...')
      const isValid = await checkTokenValidity()

      if (isValid && userInfo.value) {
        console.log('登录状态验证成功')
        // 如果用户信息不完整，尝试重新获取
        if (!userInfo.value.roles || userInfo.value.roles.length === 0) {
          console.log('用户信息不完整，重新获取...')
          await fetchRoleInfo()
        }
      } else {
        console.log('登录状态验证失败')
      }
    } catch (error) {
      console.error('初始化认证状态失败:', error)
      console.log('清除认证信息')
      clearAuth()
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    try {
      const response = await request.get('/sso/userinfo')
      if (response.data.code === 200) {
        userInfo.value = response.data.data
        console.log('获取用户信息成功:', response.data.data)
      } else {
        throw new Error(response.data.message)
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  // 获取用户权限
  const fetchPermissions = async () => {
    try {
      const response = await request.get('/api/role/permissions')
      if (response.data.code === 200) {
        permissions.value = response.data.data || []
        console.log('用户权限:', permissions.value)
      }
    } catch (error) {
      console.error('获取用户权限失败:', error)
      permissions.value = []
    }
  }

  // 获取角色信息
  const fetchRoleInfo = async () => {
    try {
      const response = await request.get('/api/role/current')
      if (response.data.code === 200) {
        const roleInfo = response.data.data
        roles.value = roleInfo.roles || []
        primaryRole.value = roleInfo.primaryRole || ''
        dashboardPath.value = roleInfo.dashboardPath || '/dashboard/personal'
        console.log('角色信息:', roleInfo)
        return roleInfo
      }
    } catch (error) {
      console.error('获取角色信息失败:', error)
      roles.value = []
      primaryRole.value = ''
      dashboardPath.value = '/dashboard/personal'
    }
    return null
  }

  // 获取用户菜单
  const fetchUserMenus = async () => {
    try {
      const response = await request.get('/api/role/menus')
      if (response.data.code === 200) {
        userMenus.value = response.data.data || []
        console.log('用户菜单:', userMenus.value)
      }
    } catch (error) {
      console.error('获取用户菜单失败:', error)
      userMenus.value = []
    }
  }

  // 获取角色功能配置
  const fetchRoleFeatures = async () => {
    try {
      const response = await request.get('/api/role/features')
      if (response.data.code === 200) {
        roleFeatures.value = response.data.data || {}
        console.log('角色功能配置:', roleFeatures.value)
      }
    } catch (error) {
      console.error('获取角色功能配置失败:', error)
      roleFeatures.value = {}
    }
  }

  // 重定向到 SSO 登录页面（统一函数）
  const redirectToLogin = (returnUrl = window.location.href) => {
    const ssoServerUrl = 'http://localhost:8081'
    const callbackUrl = `${window.location.origin}/callback`
    // 使用Sa-Token SSO标准地址 - 修正redirect参数
    const loginUrl = `${ssoServerUrl}/sso/auth?redirect=${encodeURIComponent(callbackUrl)}&return_url=${encodeURIComponent(returnUrl)}`
    console.log('重定向到SSO登录页面:', loginUrl)

    // 清除本地认证信息
    clearAuth()

    // 跳转到SSO登录页面
    window.location.href = loginUrl
  }

  // 处理SSO回调
  const handleSsoCallback = async () => {
    try {
      const response = await request.get('/sso/check-login')
      if (response.data.isLogin) {
        setToken(response.data.token)
        await fetchUserData()

        // 清除URL中的回调参数
        const url = new URL(window.location)
        url.searchParams.delete('ticket')
        url.searchParams.delete('code')
        window.history.replaceState({}, '', url.toString())
      }
    } catch (error) {
      console.error('处理SSO回调失败:', error)
    }
  }

  // 刷新Token
  const refreshToken = async () => {
    try {
      const response = await request.post('/sso/refresh-token')
      if (response.data.code === 200) {
        await fetchUserData()
      } else {
        throw new Error(response.data.message)
      }
    } catch (error) {
      console.error('刷新Token失败:', error)
      throw error
    }
  }

  // 登出
  const logout = async () => {
    try {
      await request.post('/sso/logout')
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      clearAuth()
      window.location.href = 'http://localhost:8081/sso/logout'
    }
  }

  // 清除认证信息
  const clearAuth = () => {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    roles.value = []
    primaryRole.value = ''
    dashboardPath.value = '/dashboard/personal'
    userMenus.value = []
    roleFeatures.value = {}
    Cookies.remove('satoken')
    // request 实例会通过拦截器自动处理 token，无需手动设置
  }

  // 检查权限（异步版本，发送请求到后端）
  const checkPermission = async (permission) => {
    try {
      const response = await request.post('/api/role/check-permission', { permission })
      return response.data.code === 200 && response.data.data === true
    } catch (error) {
      console.error('权限检查失败:', error)
      return false
    }
  }

  // 检查角色（异步版本，发送请求到后端）
  const checkRole = async (roleCode) => {
    try {
      const response = await request.post('/api/role/check-role', { roleCode })
      return response.data.code === 200 && response.data.data === true
    } catch (error) {
      console.error('角色检查失败:', error)
      return false
    }
  }

  // 本地权限检查（不发送请求）
  const hasPermission = (permission) => {
    return permissions.value.includes(permission)
  }

  // 本地角色检查（不发送请求）
  const hasRole = (roleCode) => {
    return roles.value.includes(roleCode)
  }

  // 检查是否为管理员
  const isAdmin = () => {
    return primaryRole.value === 'ADMIN'
  }

  // 检查是否为航司用户
  const isAirlineUser = () => {
    return primaryRole.value === 'AIRLINE_USER'
  }

  // 检查是否为企业用户
  const isEnterpriseUser = () => {
    return primaryRole.value === 'ENTERPRISE_USER'
  }

  // 检查是否为个人用户
  const isPersonalUser = () => {
    return primaryRole.value === 'PERSONAL_USER'
  }

  // 批量检查权限
  const batchCheckPermissions = async (permissionList) => {
    try {
      const response = await request.post('/api/role/batch-check-permissions', {
        permissions: permissionList
      })
      return response.data.code === 200 ? response.data.data : {}
    } catch (error) {
      console.error('批量权限检查失败:', error)
      return {}
    }
  }

  // 验证 Token 有效性
  const checkTokenValidity = async () => {
    // 优先检查是否有完整的用户信息
    if (userInfo.value && userInfo.value.id && userInfo.value.username) {
      console.log('用户信息完整，验证通过')
      return true
    }

    // 如果有token但用户信息不完整，尝试获取
    if (token.value && (!userInfo.value || !userInfo.value.id)) {
      try {
        console.log('Token存在但用户信息不完整，尝试获取')
        await fetchUserData()
        return !!(userInfo.value && userInfo.value.id)
      } catch (error) {
        console.error('获取用户信息失败:', error)
        // 修复：不立即清除认证信息，给用户一个机会
        console.log('获取用户信息失败，但不立即清除认证信息')
        return false
      }
    }

    // 如果没有token或用户信息，验证失败
    if (!token.value || !userInfo.value) {
      console.log('Token或用户信息不存在')
      return false
    }

    // 最后尝试从后端验证登录状态
    try {
      console.log('从后端验证登录状态')
      const response = await request.get('/sso/check-login')
      if (response.data && response.data.isLogin) {
        // 确保用户信息是最新的
        if (!userInfo.value || !userInfo.value.roles) {
          await fetchUserData()
        }
        return true
      } else {
        console.log('后端验证失败，但不立即清除认证信息')
        // 修复：不立即清除认证信息，给用户一个机会
        return false
      }
    } catch (error) {
      console.error('后端验证失败:', error)
      // 修复：不立即清除认证信息，给用户一个机会
      console.log('后端验证异常，但不立即清除认证信息')
      return false
    }
  }

  // 设置 Token
  const setToken = (newToken) => {
    token.value = newToken
    Cookies.set('satoken', newToken, { expires: 7 })
    // request 实例会通过拦截器自动处理 token，无需手动设置
  }

  return {
    // 状态
    token,
    userInfo,
    permissions,
    roles,
    primaryRole,
    dashboardPath,
    userMenus,
    roleFeatures,
    isLoggedIn,

    // 初始化和数据获取
    initAuth,
    fetchUserInfo,
    fetchPermissions,
    fetchRoleInfo,
    fetchUserMenus,
    fetchRoleFeatures,
    fetchUserData,

    // 认证相关
    redirectToLogin,
    handleSsoCallback,
    refreshToken,
    logout,
    clearAuth,
    setToken,
    checkTokenValidity,

    // 权限检查
    checkPermission,
    checkRole,
    hasPermission,
    hasRole,
    batchCheckPermissions,

    // 角色检查
    isAdmin,
    isAirlineUser,
    isEnterpriseUser,
    isPersonalUser
  }
})
