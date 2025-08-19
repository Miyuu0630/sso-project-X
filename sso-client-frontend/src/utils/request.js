import axios from 'axios'
import { ElMessage } from 'element-plus'
import Cookies from 'js-cookie'
import { useAuthStore } from '../stores/auth'

// 创建axios实例
const request = axios.create({
  baseURL: '/',
  timeout: 10000,
  withCredentials: true
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加Token到请求头
    const token = Cookies.get('satoken')
    if (token) {
      config.headers['satoken'] = token
    }
    
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const data = response.data
    
    // 如果是文件下载等特殊响应，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 处理业务错误
    if (data.code && data.code !== 200) {
      // 401未授权，跳转登录
      if (data.code === 401) {
        const authStore = useAuthStore()
        authStore.clearAuth()
        authStore.redirectToLogin()
        return Promise.reject(new Error('未授权'))
      }
      
      // 403权限不足
      if (data.code === 403) {
        ElMessage.error('权限不足')
        return Promise.reject(new Error('权限不足'))
      }
      
      // 其他业务错误
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    
    return response
  },
  (error) => {
    console.error('响应错误:', error)
    
    // 网络错误
    if (!error.response) {
      ElMessage.error('网络连接失败')
      return Promise.reject(error)
    }
    
    const { status, data } = error.response
    
    switch (status) {
      case 401:
        // 未授权，清除本地认证信息并跳转登录
        const authStore = useAuthStore()
        authStore.clearAuth()
        authStore.redirectToLogin()
        ElMessage.error('登录已过期，请重新登录')
        break
      case 403:
        ElMessage.error('权限不足')
        break
      case 404:
        ElMessage.error('请求的资源不存在')
        break
      case 500:
        ElMessage.error('服务器内部错误')
        break
      default:
        ElMessage.error(data?.message || `请求失败 (${status})`)
    }
    
    return Promise.reject(error)
  }
)

export default request
