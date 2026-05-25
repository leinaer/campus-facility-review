import request from './request'

export function login(data) {
  return request({
    url: '/api/auth/admin/login',
    method: 'POST',
    data: {
      token: data.token || 'admin-secret-token'
    }
  })
}

export function getUserInfo() {
  return request({
    url: '/api/auth/user/info',
    method: 'GET'
  })
}

export function logout() {
  return request({
    url: '/api/auth/logout',
    method: 'POST'
  })
}
