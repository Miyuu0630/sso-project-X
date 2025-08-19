/**
 * Vue 前端认证状态管理
 * 基于 Pinia 的 SSO 认证 Store
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

export const useAuthStore = defineStore('auth', () => {
  // ========================================
  // 状态定义
  // ========================================
  
  const token = ref(localStorage.getItem('sso_token') || '')
  const userInfo = ref(null)
  const isLoading = ref(false)
  const loginError = ref('')

  // ========================================
  // 计算属性
  // ========================================
  
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '')

  // ========================================
  // SSO 配置
  // ========================================
  
  const SSO_CONFIG = {
    SERVER_URL: 'http://localhost:8081',
    CLIENT_URL: 'http://localhost:8082',
    LOGIN_PATH: '/api/auth/login',
    VERIFY_PATH: '/api/auth/verify',
    USERINFO_PATH: '/api/auth/userinfo',
    LOGOUT_PATH: '/api/auth/logout'
  }

  // ========================================
  // 核心方法
  // ========================================

  /**
   * 检查 Token 有效性
   */
  const checkTokenValidity = async () => {
    if (!token.value) {
      return false
    }

    try {
      const response = await axios.get(`${SSO_CONFIG.SERVER_URL}${SSO_CONFIG.VERIFY_PATH}`, {
        headers: {
          'Authorization': `Bearer ${token.value}`
        }
      })

      if (response.data.code === 200 && response.data.data.valid) {
        // Token 有效，更新用户信息
        userInfo.value = response.data.data.userInfo
        return true
      } else {
        // Token 无效，清除本地存储
        clearAuth()
        return false
      }
    } catch (error) {
      console.error('Token 验证失败:', error)
      clearAuth()
      return false
    }
  }

  /**
   * 重定向到 SSO 登录页面
   */
  const redirectToLogin = (returnUrl = window.location.href) => {
    const callbackUrl = `${SSO_CONFIG.CLIENT_URL}/auth/callback`
    const loginUrl = `${SSO_CONFIG.SERVER_URL}/login?redirect=${encodeURIComponent(callbackUrl)}&return_url=${encodeURIComponent(returnUrl)}`
    
    console.log('重定向到 SSO 登录页面:', loginUrl)
    window.location.href = loginUrl
  }

  /**
   * 处理 SSO 登录回调
   */
  const handleSsoCallback = async (urlParams) => {
    const token = urlParams.get('token')
    const returnUrl = urlParams.get('return_url') || '/'

    if (token) {
      try {
        // 存储 Token
        setToken(token)
        
        // 验证 Token 并获取用户信息
        const isValid = await checkTokenValidity()
        
        if (isValid) {
          console.log('SSO 登录成功，跳转到:', returnUrl)
          // 跳转到原始页面
          window.location.href = returnUrl
        } else {
          throw new Error('Token 验证失败')
        }
      } catch (error) {
        console.error('SSO 回调处理失败:', error)
        loginError.value = 'SSO 登录失败，请重试'
        clearAuth()
      }
    } else {
      loginError.value = '未收到有效的登录凭证'
    }
  }

  /**
   * 直接登录（用于登录页面表单提交）
   */
  const directLogin = async (loginForm) => {
    isLoading.value = true
    loginError.value = ''

    try {
      const response = await axios.post(`${SSO_CONFIG.SERVER_URL}${SSO_CONFIG.LOGIN_PATH}`, {
        loginType: loginForm.loginType || 'username',
        credential: loginForm.credential,
        password: loginForm.password,
        clientId: 'vue-client',
        redirectUri: `${SSO_CONFIG.CLIENT_URL}/auth/callback`
      })

      if (response.data.code === 200) {
        const { accessToken, userInfo: userData, redirectUri } = response.data.data
        
        // 存储 Token 和用户信息
        setToken(accessToken)
        userInfo.value = userData
        
        console.log('直接登录成功')
        
        // 如果有重定向地址，则跳转
        if (redirectUri) {
          window.location.href = redirectUri
        } else {
          // 否则跳转到首页
          window.location.href = '/'
        }
        
        return true
      } else {
        loginError.value = response.data.message || '登录失败'
        return false
      }
    } catch (error) {
      console.error('登录请求失败:', error)
      loginError.value = error.response?.data?.message || '网络错误，请重试'
      return false
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 获取用户详细信息
   */
  const fetchUserInfo = async () => {
    if (!token.value) {
      return null
    }

    try {
      const response = await axios.get(`${SSO_CONFIG.SERVER_URL}${SSO_CONFIG.USERINFO_PATH}`, {
        headers: {
          'Authorization': `Bearer ${token.value}`
        }
      })

      if (response.data.code === 200) {
        userInfo.value = response.data.data
        return response.data.data
      } else {
        throw new Error(response.data.message)
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    }
  }

  /**
   * 登出
   */
  const logout = async () => {
    try {
      if (token.value) {
        await axios.post(`${SSO_CONFIG.SERVER_URL}${SSO_CONFIG.LOGOUT_PATH}`, {
          redirectUri: SSO_CONFIG.CLIENT_URL
        }, {
          headers: {
            'Authorization': `Bearer ${token.value}`
          }
        })
      }
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      // 无论请求是否成功，都清除本地状态
      clearAuth()
      // 跳转到首页
      window.location.href = '/'
    }
  }

  /**
   * 设置 Token
   */
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('sso_token', newToken)
    
    // 设置 axios 默认请求头
    axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
  }

  /**
   * 清除认证状态
   */
  const clearAuth = () => {
    token.value = ''
    userInfo.value = null
    loginError.value = ''
    localStorage.removeItem('sso_token')
    delete axios.defaults.headers.common['Authorization']
  }

  /**
   * 初始化认证状态
   */
  const initAuth = async () => {
    if (token.value) {
      // 设置 axios 默认请求头
      axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
      
      // 验证 Token 有效性
      const isValid = await checkTokenValidity()
      
      if (!isValid) {
        console.log('Token 已失效，需要重新登录')
      }
    }
  }

  // ========================================
  // 返回 Store 接口
  // ========================================

  return {
    // 状态
    token,
    userInfo,
    isLoading,
    loginError,
    
    // 计算属性
    isLoggedIn,
    userName,
    
    // 方法
    checkTokenValidity,
    redirectToLogin,
    handleSsoCallback,
    directLogin,
    fetchUserInfo,
    logout,
    setToken,
    clearAuth,
    initAuth,
    
    // 配置
    SSO_CONFIG
  }
})
