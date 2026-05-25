import request from './request'

export function getPostList(params) {
  return request({
    url: '/api/post/admin/list',
    method: 'GET',
    params
  })
}

export function deletePost(postId) {
  return request({
    url: `/api/post/${postId}`,
    method: 'DELETE'
  })
}

export function togglePostStatus(postId, status) {
  return request({
    url: `/api/post/admin/status/${postId}`,
    method: 'PUT',
    data: { status }
  })
}