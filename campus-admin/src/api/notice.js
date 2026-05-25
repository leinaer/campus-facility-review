import request from './request'

export function getNoticeList(params) {
  return request({
    url: '/api/admin/notice/list',
    method: 'GET',
    params
  })
}

export function publishNotice(data) {
  return request({
    url: '/api/admin/notice/publish',
    method: 'POST',
    data
  })
}

export function updateNotice(id, data) {
  return request({
    url: `/api/admin/notice/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteNotice(id) {
  return request({
    url: `/api/admin/notice/${id}`,
    method: 'DELETE'
  })
}

export function toggleTop(id, isTop) {
  return request({
    url: `/api/admin/notice/${id}/top`,
    method: 'PUT',
    params: { isTop }
  })
}
