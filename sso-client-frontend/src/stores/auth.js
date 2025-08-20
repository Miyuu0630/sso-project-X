import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'
import Cookies from 'js-cookie'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref(Cookies.get('satoken') || '')
  const userInfo = ref(null)
  const permissions = ref([])
  const roles = ref([])

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)

  // 公共函数：获取用户信息 + 权限
  const fetchUserData = async () => {
    await Promise.all([fetchUserInfo(), fetchPermissions()])
  }

  // 初始化认证状态
  const initAuth = async () => {
    if (token.value) {
      try {
        await fetchUserData()
      } catch (error) {
        console.error('初始化认证状态失败:', error)
        clearAuth()
      }
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    try {
      const response = await axios.get('/sso/userinfo')
      if (response.data.code === 200) {
        userInfo.value = response.data.data
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
      const response = await axios.get('/sso/user-permissions')
      if (response.data.code === 200) {
        const data = response.data.data
        permissions.value = data.permissions || []
        roles.value = data.roles || []
      } else {
        throw new Error(response.data.message)
      }
    } catch (error) {
      console.error('获取用户权限失败:', error)
      throw error
    }
  }

  // 重定向到 SSO 登录页面（统一函数）
  const redirectToLogin = (returnUrl = window.location.href) => {
    const ssoServerUrl = 'http://localhost:8081'
    const callbackUrl = `${window.location.origin}/callback`
    // 使用Sa-Token SSO标准地址
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
      const response = await axios.get('/sso/check-login')
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
      const response = await axios.post('/sso/refresh-token')
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
      await axios.post('/sso/logout')
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
    Cookies.remove('satoken')
    axios.defaults.headers.common['Authorization'] = ''
  }

  // 检查权限
  const checkPermission = (permission) => permissions.value.includes(permission)
  const checkRole = (role) => roles.value.includes(role)

  // 验证 Token 有效性
  const checkTokenValidity = async () => {
    if (!token.value) return false
    try {
      const response = await axios.get('/sso/verify', {
        headers: { 'Authorization': `Bearer ${token.value}` }
      })
      if (response.data.code === 200 && response.data.data.valid) {
        if (response.data.data.userInfo) userInfo.value = response.data.data.userInfo
        return true
      } else {
        clearAuth()
        return false
      }
    } catch (error) {
      console.error('Token 验证失败:', error)
      clearAuth()
      return false
    }
  }

  // 设置 Token
  const setToken = (newToken) => {
    token.value = newToken
    Cookies.set('satoken', newToken, { expires: 7 })
    axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
  }

  return {
    token,
    userInfo,
    permissions,
    roles,
    isLoggedIn,
    initAuth,
    fetchUserInfo,
    fetchPermissions,
    fetchUserData,
    redirectToLogin,
    handleSsoCallback,
    refreshToken,
    logout,
    clearAuth,
    checkPermission,
    checkRole,
    checkTokenValidity,
    setToken
  }
})
