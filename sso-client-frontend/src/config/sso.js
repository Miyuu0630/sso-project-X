/**
 * SSO 配置文件
 * 统一管理 SSO 相关配置和工具函数
 */

// SSO 服务器配置
export const SSO_CONFIG = {
  // SSO Server 地址
  SERVER_URL: process.env.VUE_APP_SSO_SERVER_URL || 'http://localhost:8081',
  
  // Client 地址
  CLIENT_URL: process.env.VUE_APP_CLIENT_URL || 'http://localhost:5137',
  
  // API 路径
  API_PATHS: {
    LOGIN: '/sso/auth',
    LOGOUT: '/sso/logout',
    VERIFY: '/sso/verify',
    USER_INFO: '/sso/userinfo',
    REGISTER: '/register'
  },
  
  // Token 配置
  TOKEN: {
    NAME: 'satoken',
    EXPIRES_DAYS: 7,
    REFRESH_THRESHOLD: 5 * 60 * 1000 // 5分钟
  },
  
  // 路由配置
  ROUTES: {
    CALLBACK: '/callback',
    LOGIN_SUCCESS: '/',
    LOGIN_FAILURE: '/login',
    UNAUTHORIZED: '/'
  }
}

/**
 * SSO 工具函数
 */
export class SSOUtils {
  
  /**
   * 构建 SSO 登录 URL
   * @param {string} returnUrl 登录成功后返回的 URL
   * @returns {string} 完整的登录 URL
   */
  static buildLoginUrl(returnUrl = window.location.href) {
    const callbackUrl = `${SSO_CONFIG.CLIENT_URL}${SSO_CONFIG.ROUTES.CALLBACK}`
    const loginUrl = new URL(`${SSO_CONFIG.SERVER_URL}${SSO_CONFIG.API_PATHS.LOGIN}`)
    
    loginUrl.searchParams.set('redirect', callbackUrl)
    loginUrl.searchParams.set('return_url', returnUrl)
    
    return loginUrl.toString()
  }
  
  /**
   * 构建 SSO 登出 URL
   * @param {string} returnUrl 登出后返回的 URL
   * @returns {string} 完整的登出 URL
   */
  static buildLogoutUrl(returnUrl = SSO_CONFIG.CLIENT_URL) {
    const logoutUrl = new URL(`${SSO_CONFIG.SERVER_URL}${SSO_CONFIG.API_PATHS.LOGOUT}`)
    logoutUrl.searchParams.set('redirect', returnUrl)
    
    return logoutUrl.toString()
  }
  
  /**
   * 解析回调 URL 参数
   * @param {string} url 回调 URL
   * @returns {Object} 解析后的参数对象
   */
  static parseCallbackParams(url = window.location.href) {
    const urlObj = new URL(url)
    const params = new URLSearchParams(urlObj.search)
    
    return {
      token: params.get('token'),
      returnUrl: params.get('return_url') || SSO_CONFIG.ROUTES.LOGIN_SUCCESS,
      error: params.get('error'),
      message: params.get('message')
    }
  }
  
  /**
   * 检查是否为 SSO 回调
   * @returns {boolean} 是否为回调
   */
  static isCallback() {
    return window.location.pathname === SSO_CONFIG.ROUTES.CALLBACK
  }
  
  /**
   * 重定向到 SSO 登录页面
   * @param {string} returnUrl 返回 URL
   */
  static redirectToLogin(returnUrl) {
    const loginUrl = this.buildLoginUrl(returnUrl)
    console.log('重定向到 SSO 登录页面:', loginUrl)
    window.location.href = loginUrl
  }
  
  /**
   * 重定向到 SSO 登出页面
   * @param {string} returnUrl 返回 URL
   */
  static redirectToLogout(returnUrl) {
    const logoutUrl = this.buildLogoutUrl(returnUrl)
    console.log('重定向到 SSO 登出页面:', logoutUrl)
    window.location.href = logoutUrl
  }
  
  /**
   * 安全跳转
   * @param {string} url 目标 URL
   */
  static safeRedirect(url) {
    // 检查 URL 是否安全（同域或配置的可信域）
    try {
      const targetUrl = new URL(url, window.location.origin)
      const currentOrigin = window.location.origin
      const ssoOrigin = new URL(SSO_CONFIG.SERVER_URL).origin
      
      // 允许同域和 SSO 服务器域
      if (targetUrl.origin === currentOrigin || targetUrl.origin === ssoOrigin) {
        window.location.href = url
      } else {
        console.warn('不安全的重定向 URL:', url)
        window.location.href = SSO_CONFIG.ROUTES.LOGIN_SUCCESS
      }
    } catch (error) {
      console.error('URL 解析失败:', error)
      window.location.href = SSO_CONFIG.ROUTES.LOGIN_SUCCESS
    }
  }
  
  /**
   * 获取 Token 过期时间
   * @param {string} token JWT Token
   * @returns {number|null} 过期时间戳
   */
  static getTokenExpiry(token) {
    try {
      if (!token) return null
      
      // 简单的 JWT 解析（仅用于获取过期时间）
      const parts = token.split('.')
      if (parts.length !== 3) return null
      
      const payload = JSON.parse(atob(parts[1]))
      return payload.exp ? payload.exp * 1000 : null
    } catch (error) {
      console.error('Token 解析失败:', error)
      return null
    }
  }
  
  /**
   * 检查 Token 是否即将过期
   * @param {string} token JWT Token
   * @returns {boolean} 是否即将过期
   */
  static isTokenExpiringSoon(token) {
    const expiry = this.getTokenExpiry(token)
    if (!expiry) return false
    
    const now = Date.now()
    const timeUntilExpiry = expiry - now
    
    return timeUntilExpiry < SSO_CONFIG.TOKEN.REFRESH_THRESHOLD
  }
  
  /**
   * 格式化用户类型
   * @param {number} userType 用户类型
   * @returns {string} 格式化后的用户类型
   */
  static formatUserType(userType) {
    const typeMap = {
      1: '个人用户',
      2: '企业用户',
      3: '航司用户'
    }
    return typeMap[userType] || '未知用户'
  }
  
  /**
   * 格式化账号状态
   * @param {number} status 账号状态
   * @returns {string} 格式化后的状态
   */
  static formatUserStatus(status) {
    const statusMap = {
      0: '已禁用',
      1: '正常',
      2: '已锁定'
    }
    return statusMap[status] || '未知状态'
  }
  
  /**
   * 检查用户是否有指定角色
   * @param {Array} userRoles 用户角色列表
   * @param {string|Array} requiredRoles 需要的角色
   * @returns {boolean} 是否有权限
   */
  static hasRole(userRoles = [], requiredRoles) {
    if (!requiredRoles) return true
    
    const roles = Array.isArray(requiredRoles) ? requiredRoles : [requiredRoles]
    return roles.some(role => userRoles.includes(role))
  }
  
  /**
   * 检查用户是否有指定权限
   * @param {Array} userPermissions 用户权限列表
   * @param {string|Array} requiredPermissions 需要的权限
   * @returns {boolean} 是否有权限
   */
  static hasPermission(userPermissions = [], requiredPermissions) {
    if (!requiredPermissions) return true
    
    const permissions = Array.isArray(requiredPermissions) ? requiredPermissions : [requiredPermissions]
    return permissions.some(permission => userPermissions.includes(permission))
  }
}

/**
 * SSO 事件管理器
 */
export class SSOEventManager {
  static events = new Map()
  
  /**
   * 监听 SSO 事件
   * @param {string} event 事件名称
   * @param {Function} handler 事件处理函数
   */
  static on(event, handler) {
    if (!this.events.has(event)) {
      this.events.set(event, [])
    }
    this.events.get(event).push(handler)
  }
  
  /**
   * 移除 SSO 事件监听
   * @param {string} event 事件名称
   * @param {Function} handler 事件处理函数
   */
  static off(event, handler) {
    if (this.events.has(event)) {
      const handlers = this.events.get(event)
      const index = handlers.indexOf(handler)
      if (index > -1) {
        handlers.splice(index, 1)
      }
    }
  }
  
  /**
   * 触发 SSO 事件
   * @param {string} event 事件名称
   * @param {*} data 事件数据
   */
  static emit(event, data) {
    if (this.events.has(event)) {
      this.events.get(event).forEach(handler => {
        try {
          handler(data)
        } catch (error) {
          console.error(`SSO 事件处理失败 [${event}]:`, error)
        }
      })
    }
  }
}

// 导出默认配置
export default SSO_CONFIG
