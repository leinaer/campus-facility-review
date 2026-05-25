<template>
  <div class="evaluation-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <div style="display: flex; gap: 10px; align-items: center">
            <el-select v-model="filterStatus" placeholder="筛选状态" style="width: 120px" clearable @change="handleFilterChange">
              <el-option label="全部" :value="null" />
              <el-option label="正常" :value="1" />
              <el-option label="待审核" :value="2" />
              <el-option label="已拒绝" :value="0" />
            </el-select>
            <el-select v-model="filterRating" placeholder="评分筛选" style="width: 120px" clearable @change="handleFilterChange">
              <el-option label="全部" :value="null" />
              <el-option label="5星" :value="5" />
              <el-option label="4星" :value="4" />
              <el-option label="3星" :value="3" />
              <el-option label="2星" :value="2" />
              <el-option label="1星" :value="1" />
            </el-select>
          </div>
          <div style="display: flex; gap: 10px">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索评价内容或作者"
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

      <el-table :data="evaluationList" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="evaluationId" label="ID" width="80" />
        <el-table-column label="设施信息" width="200">
          <template #default="{ row }">
            <div class="facility-info">
              <el-tag :type="getCategoryType(row.categoryName)" size="small">
                {{ row.categoryName || '未分类' }}
              </el-tag>
              <span style="font-size: 13px; color: #303133">{{ row.facilityName || '未知设施' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评价内容" min-width="200">
          <template #default="{ row }">
            <div class="evaluation-content">
              <el-tooltip :content="row.content" placement="top" :disabled="!row.content || row.content.length < 50">
                <span>{{ row.content ? (row.content.length > 50 ? row.content.substring(0, 50) + '...' : row.content) : '-' }}</span>
              </el-tooltip>
              <div v-if="row.images && parseImages(row.images).length > 0" class="content-images">
                <el-image
                  v-for="(img, index) in parseImages(row.images).slice(0, 3)"
                  :key="index"
                  :src="getImageUrl(img)"
                  :preview-src-list="parseImages(row.images).map(i => getImageUrl(i))"
                  fit="cover"
                  style="width: 40px; height: 40px; border-radius: 4px; margin-right: 5px"
                />
                <span v-if="parseImages(row.images).length > 3" style="font-size: 12px; color: #909399">+{{ parseImages(row.images).length - 3 }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" width="200">
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
        <el-table-column label="评分" width="80" align="center">
          <template #default="{ row }">
            <span style="color: #f5a623; font-weight: bold">{{ row.rating || 0 }}⭐</span>
          </template>
        </el-table-column>
        <el-table-column label="点赞" width="80" align="center">
          <template #default="{ row }">{{ row.likeCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="评论" width="80" align="center">
          <template #default="{ row }">{{ row.commentCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'">
              {{ row.status === 1 ? '正常' : row.status === 2 ? '待审核' : '已拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewUser(row.userId)">查看用户</el-button>
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button
              v-if="row.status !== 1"
              size="small"
              type="success"
              @click="approveEvaluation(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status !== 0"
              size="small"
              type="danger"
              @click="rejectEvaluation(row)"
            >
              拒绝
            </el-button>
            <el-button size="small" type="danger" @click="deleteEvaluation(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center">
        <div>
          <el-button type="danger" size="small" :disabled="selectedEvaluations.length === 0" @click="batchDelete">
            批量删除 ({{ selectedEvaluations.length }})
          </el-button>
          <el-button type="success" size="small" :disabled="selectedEvaluations.length === 0" @click="batchApprove">
            批量通过
          </el-button>
          <el-button type="warning" size="small" :disabled="selectedEvaluations.length === 0" @click="batchReject">
            批量拒绝
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

    <!-- 评价详情对话框 -->
    <el-dialog v-model="detailVisible" title="评价详情" width="700px">
      <div v-if="currentEvaluation" class="evaluation-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="评价ID">{{ currentEvaluation.evaluationId }}</el-descriptions-item>
          <el-descriptions-item label="评分">
            <span style="color: #f5a623; font-size: 16px">{{ currentEvaluation.rating }}⭐</span>
          </el-descriptions-item>
          <el-descriptions-item label="设施">{{ currentEvaluation.facilityName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentEvaluation.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentEvaluation.status === 1 ? 'success' : currentEvaluation.status === 2 ? 'warning' : 'danger'">
              {{ currentEvaluation.status === 1 ? '正常' : currentEvaluation.status === 2 ? '待审核' : '已拒绝' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDateTime(currentEvaluation.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="点赞数">{{ currentEvaluation.likeCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="评论数">{{ currentEvaluation.commentCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="评价内容" :span="2">
            <div style="white-space: pre-wrap; padding: 10px; background: #f5f7fa; border-radius: 4px">
              {{ currentEvaluation.content || '暂无内容' }}
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="图片" :span="2" v-if="currentEvaluation.images && parseImages(currentEvaluation.images).length > 0">
            <div class="image-gallery">
              <el-image
                v-for="(img, index) in parseImages(currentEvaluation.images)"
                :key="index"
                :src="getImageUrl(img)"
                :preview-src-list="parseImages(currentEvaluation.images).map(i => getImageUrl(i))"
                fit="cover"
                style="width: 100px; height: 100px; margin: 5px; border-radius: 4px"
              />
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 20px; display: flex; gap: 10px; justify-content: flex-end">
          <el-button
            v-if="currentEvaluation.status !== 1"
            type="success"
            @click="approveEvaluation(currentEvaluation); detailVisible = false"
          >
            通过审核
          </el-button>
          <el-button
            v-if="currentEvaluation.status !== 0"
            type="danger"
            @click="rejectEvaluation(currentEvaluation); detailVisible = false"
          >
            拒绝
          </el-button>
          <el-button @click="detailVisible = false">关闭</el-button>
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
const evaluationList = ref([])
const searchKeyword = ref('')
const filterStatus = ref(null)
const filterRating = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedEvaluations = ref([])
const detailVisible = ref(false)
const currentEvaluation = ref(null)
const userDialogVisible = ref(false)
const currentUser = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getCategoryType = (category) => {
  const types = {
    '食堂': 'warning',
    '图书馆': 'success',
    '教学楼': 'info',
    '健身房': 'danger',
    '超市': ''
  }
  return types[category] || ''
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

const loadEvaluations = async () => {
  loading.value = true
  try {
    const data = await request({
      url: '/api/evaluation/admin/list',
      method: 'GET',
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value,
        status: filterStatus.value,
        rating: filterRating.value
      }
    })

    // 调试：打印后端返回的数据
    console.log('后端返回数据:', data)
    console.log('records:', data.records || data.list)
    console.log('total:', data.total)

    // request.js 已经返回了 res.data，直接使用
    evaluationList.value = data.records || data.list || []
    total.value = data.total || 0

    console.log('设置后的 total:', total.value)
    console.log('设置后的列表长度:', evaluationList.value.length)
  } catch (error) {
    console.error('加载评价失败:', error)
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}




const handleSearch = () => {
  currentPage.value = 1
  loadEvaluations()
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadEvaluations()
}

const handleRefresh = () => {
  currentPage.value = 1
  searchKeyword.value = ''
  filterStatus.value = null
  filterRating.value = null
  loadEvaluations()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadEvaluations()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadEvaluations()
}

const handleSelectionChange = (selection) => {
  selectedEvaluations.value = selection
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
    loadEvaluations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const viewDetail = (row) => {
  currentEvaluation.value = row
  detailVisible.value = true
}

const approveEvaluation = async (row) => {
  try {
    await request({
      url: `/api/evaluation/admin/status/${row.evaluationId}`,
      method: 'PUT',
      data: { status: 1 }
    })
    ElMessage.success('审核通过')
    loadEvaluations()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

const rejectEvaluation = async (row) => {
  try {
    await ElMessageBox.confirm('确定要拒绝这条评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request({
      url: `/api/evaluation/admin/status/${row.evaluationId}`,
      method: 'PUT',
      data: { status: 0 }
    })
    ElMessage.success('已拒绝')
    loadEvaluations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const deleteEvaluation = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评价吗？此操作不可恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request({
      url: `/api/evaluation/admin/delete/${row.evaluationId}`,
      method: 'DELETE'
    })
    ElMessage.success('删除成功')
    loadEvaluations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedEvaluations.value.length} 条评价吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedEvaluations.value.map(e => e.evaluationId)
    await request({
      url: '/api/evaluation/admin/batch-delete',
      method: 'POST',
      data: { ids }
    })
    ElMessage.success('批量删除成功')
    loadEvaluations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const batchApprove = async () => {
  try {
    await ElMessageBox.confirm(`确定要通过选中的 ${selectedEvaluations.value.length} 条评价吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedEvaluations.value.map(e => e.evaluationId)
    await request({
      url: '/api/evaluation/admin/batch-status',
      method: 'POST',
      data: { ids, status: 1 }
    })
    ElMessage.success('批量通过成功')
    loadEvaluations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量操作失败:', error)
      ElMessage.error('批量操作失败')
    }
  }
}

const batchReject = async () => {
  try {
    await ElMessageBox.confirm(`确定要拒绝选中的 ${selectedEvaluations.value.length} 条评价吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedEvaluations.value.map(e => e.evaluationId)
    await request({
      url: '/api/evaluation/admin/batch-status',
      method: 'POST',
      data: { ids, status: 0 }
    })
    ElMessage.success('批量拒绝成功')
    loadEvaluations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量操作失败:', error)
      ElMessage.error('批量操作失败')
    }
  }
}

onMounted(() => {
  loadEvaluations()
})
</script>

<style scoped>
.evaluation-manage {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  cursor: pointer;
}

.user-name:hover {
  color: #409EFF;
}

.user-id {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.facility-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.evaluation-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.content-images {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.user-detail-dialog {
  padding: 10px 0;
}

.evaluation-detail {
  padding: 10px 0;
}
</style>
