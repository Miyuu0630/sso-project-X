import request from '@/utils/request'

/**
 * 用户认证相关API
 */

// 用户注册
export function register(data) {
  return request({
    url: '/api/auth/register',
    method: 'post',
    data
  })
}

// 用户登录
export function login(data) {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

// 用户登出
export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

// 检查用户名是否存在
export function checkUsername(username) {
  return request({
    url: '/api/auth/check-username',
    method: 'get',
    params: { username }
  })
}

// 检查手机号是否存在
export function checkPhone(phone) {
  return request({
    url: '/api/auth/check-phone',
    method: 'get',
    params: { phone }
  })
}

// 检查邮箱是否存在
export function checkEmail(email) {
  return request({
    url: '/api/auth/check-email',
    method: 'get',
    params: { email }
  })
}

// 获取当前用户信息
export function getUserInfo() {
  return request({
    url: '/api/auth/user-info',
    method: 'get'
  })
}

// 密码加密测试（仅开发环境）
export function testPassword(password) {
  return request({
    url: '/api/auth/test-password',
    method: 'post',
    params: { password }
  })
}
