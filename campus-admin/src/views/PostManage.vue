<template>
  <div class="post-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>帖子管理</span>
          <div style="display: flex; gap: 10px">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索帖子内容或作者"
              style="width: 300px"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
            <el-button type="primary" @click="handleRefresh">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="postList" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="postId" label="ID" width="80" />
        <el-table-column label="标题" width="180">
          <template #default="{ row }">
            <span style="font-weight: 500; color: #303133">{{ row.title || '无标题' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="帖子内容" min-width="250">
          <template #default="{ row }">
            <div class="post-info">
              <div class="post-content">
                <el-tooltip :content="row.content" placement="top" :disabled="!row.content || row.content.length < 50">
                  <span>{{ row.content ? (row.content.length > 50 ? row.content.substring(0, 50) + '...' : row.content) : '-' }}</span>
                </el-tooltip>
              </div>
              <div v-if="row.images && parseImages(row.images).length > 0" class="post-images">
                <el-image
                  v-for="(img, index) in parseImages(row.images).slice(0, 3)"
                  :key="index"
                  :src="getImageUrl(img)"
                  :preview-src-list="parseImages(row.images).map(i => getImageUrl(i))"
                  fit="cover"
                  style="width: 50px; height: 50px; border-radius: 4px; margin-right: 5px"
                />
                <span v-if="parseImages(row.images).length > 3" style="font-size: 12px; color: #909399">+{{ parseImages(row.images).length - 3 }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="作者信息" width="200">
          <template #default="{ row }">
            <div class="user-info-cell">
              <el-avatar :size="32" :src="row.user?.avatarUrl || defaultAvatar" />
              <div class="user-detail">
                <div class="user-name">{{ row.user?.nickname || '匿名用户' }}</div>
                <div class="user-id">ID: {{ row.userId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="点赞" width="80" align="center">
          <template #default="{ row }">{{ row.likeCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="评论" width="80" align="center">
          <template #default="{ row }">{{ row.commentCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '已隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewUser(row.userId)">查看用户</el-button>
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '隐藏' : '恢复' }}
            </el-button>
            <el-button size="small" type="danger" @click="deletePost(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center">
        <div>
          <el-button type="danger" size="small" :disabled="selectedPosts.length === 0" @click="batchDelete">
            批量删除 ({{ selectedPosts.length }})
          </el-button>
          <el-button type="warning" size="small" :disabled="selectedPosts.length === 0" @click="batchHide">
            批量隐藏
          </el-button>
        </div>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 帖子详情对话框 -->
    <el-dialog v-model="detailVisible" title="帖子详情" width="700px">
      <div v-if="currentPost" class="post-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="帖子ID">{{ currentPost.postId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentPost.status === 1 ? 'success' : 'danger'">
              {{ currentPost.status === 1 ? '正常' : '已隐藏' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">
            <span style="font-size: 16px; font-weight: bold">{{ currentPost.title || '无标题' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="点赞数">{{ currentPost.likeCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="评论数">{{ currentPost.commentCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDateTime(currentPost.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentPost.user?.nickname || '匿名用户' }}</el-descriptions-item>
          <el-descriptions-item label="帖子内容" :span="2">
            <div style="white-space: pre-wrap; padding: 10px; background: #f5f7fa; border-radius: 4px">
              {{ currentPost.content || '暂无内容' }}
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="图片" :span="2" v-if="currentPost.images && parseImages(currentPost.images).length > 0">
            <div class="image-gallery">
              <el-image
                v-for="(img, index) in parseImages(currentPost.images)"
                :key="index"
                :src="getImageUrl(img)"
                :preview-src-list="parseImages(currentPost.images).map(i => getImageUrl(i))"
                fit="cover"
                style="width: 120px; height: 120px; margin: 5px; border-radius: 4px"
              />
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 20px; display: flex; gap: 10px; justify-content: flex-end">
          <el-button type="primary" @click="detailVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog v-model="userDialogVisible" title="用户详情" width="600px">
      <div v-if="currentUser" class="user-detail-dialog">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ currentUser.userId }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone || '未绑定' }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="currentUser.role === 'ADMIN' ? 'danger' : 'info'">
              {{ currentUser.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
              {{ currentUser.status === 1 ? '正常' : '已封禁' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDateTime(currentUser.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="发帖数">{{ currentUser.postCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="评价数">{{ currentUser.evaluationCount || 0 }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 20px; display: flex; gap: 10px; justify-content: flex-end">
          <el-button
            :type="currentUser.status === 1 ? 'danger' : 'success'"
            @click="toggleUserStatus"
          >
            {{ currentUser.status === 1 ? '封禁用户' : '解封用户' }}
          </el-button>
          <el-button @click="userDialogVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const loading = ref(false)
const postList = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedPosts = ref([])
const userDialogVisible = ref(false)
const currentUser = ref(null)
const detailVisible = ref(false)
const currentPost = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const parseImages = (imagesStr) => {
  if (!imagesStr) return []
  try {
    const images = typeof imagesStr === 'string' ? JSON.parse(imagesStr) : imagesStr
    return Array.isArray(images) ? images : []
  } catch (e) {
    return []
  }
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_API_BASE_URL + url
}

const loadPosts = async () => {
  loading.value = true
  try {
    const data = await request({
      url: '/api/post/admin/list',
      method: 'GET',
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value
      }
    })

    // request.js 已经返回了 res.data，直接使用
    postList.value = data.list || data.records || []
    total.value = data.total || 0
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}



const handleSearch = () => {
  currentPage.value = 1
  loadPosts()
}

const handleRefresh = () => {
  currentPage.value = 1
  searchKeyword.value = ''
  loadPosts()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadPosts()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadPosts()
}

const handleSelectionChange = (selection) => {
  selectedPosts.value = selection
}

const viewUser = async (userId) => {
  try {
    const response = await request({
      url: `/api/user/admin/detail/${userId}`,
      method: 'GET'
    })
    currentUser.value = response.data || response
    userDialogVisible.value = true
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

const viewDetail = (row) => {
  currentPost.value = row
  detailVisible.value = true
}

const toggleUserStatus = async () => {
  if (!currentUser.value) return

  const newStatus = currentUser.value.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '解封' : '封禁'

  try {
    await ElMessageBox.confirm(`确定要${actionText}这个用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request({
      url: `/api/user/admin/status/${currentUser.value.userId}`,
      method: 'PUT',
      data: { status: newStatus }
    })

    ElMessage.success(`${actionText}成功`)
    currentUser.value.status = newStatus
    loadPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const toggleStatus = async (row) => {
  try {
    await request({
      url: `/api/post/admin/status/${row.postId}`,
      method: 'PUT',
      data: { status: row.status === 1 ? 0 : 1 }
    })
    ElMessage.success('操作成功')
    loadPosts()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const deletePost = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request({
      url: `/api/post/${row.postId}`,
      method: 'DELETE'
    })
    ElMessage.success('删除成功')
    loadPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedPosts.value.length} 个帖子吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedPosts.value.map(p => p.postId)
    await request({
      url: '/api/post/admin/batch-delete',
      method: 'POST',
      data: { ids }
    })
    ElMessage.success('批量删除成功')
    loadPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const batchHide = async () => {
  try {
    await ElMessageBox.confirm(`确定要隐藏选中的 ${selectedPosts.value.length} 个帖子吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedPosts.value.map(p => p.postId)
    await request({
      url: '/api/post/admin/batch-status',
      method: 'POST',
      data: { ids, status: 0 }
    })
    ElMessage.success('批量隐藏成功')
    loadPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量隐藏失败:', error)
      ElMessage.error('批量隐藏失败')
    }
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.post-manage {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.post-content {
  font-size: 14px;
  color: #303133;
}

.post-images {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.user-info-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.user-id {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.user-detail-dialog {
  padding: 10px 0;
}

.post-detail {
  padding: 10px 0;
}
</style>
