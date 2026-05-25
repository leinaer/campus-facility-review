import axios from 'axios'
import { ElMessage } from 'element-plus'

const baseURL = 'http://localhost:8080'

export function getCategoryStats() {
  return axios.get(`${baseURL}/api/statistics/admin/category-stats`, {
    headers: {
      Authorization: `Bearer ${localStorage.getItem('token')}`
    }
  }).then(res => res.data.data)
}

export function exportCategoryStats() {
  const token = localStorage.getItem('token')
  
  return fetch(`${baseURL}/api/statistics/admin/export-category-stats`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    credentials: 'include'
  }).then(response => {
    if (!response.ok) {
      throw new Error('导出失败')
    }
    return response.blob()
  }).then(blob => {
    if (blob.size === 0) {
      throw new Error('导出的文件为空')
    }
    return blob
  }).catch(error => {
    console.error('导出错误:', error)
    ElMessage.error(error.message || '导出失败，请稍后重试')
    throw error
  })
}