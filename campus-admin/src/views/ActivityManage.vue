<template>
  <div class="activity-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>活动管理</span>
          <el-button type="primary" @click="showAddDialog">创建活动</el-button>
        </div>
      </template>

      <el-table :data="activityList" v-loading="loading" stripe>
        <el-table-column prop="activityId" label="ID" width="80" />
        <el-table-column label="活动封面" width="150">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              fit="cover"
              style="width: 120px; height: 60px; border-radius: 4px"
              :preview-src-list="[row.coverImage]"
            />
            <span v-else style="color: #999">无封面</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="活动标题" min-width="200" />
        <el-table-column label="关联标签" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.tagName" type="success" size="small">
              {{ row.tagName }}
            </el-tag>
            <span v-else style="color: #999; font-size: 12px">无标签</span>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="280">
          <template #default="{ row }">
            <div>{{ formatDate(row.startTime) }}</div>
            <div style="color: #999; font-size: 12px">至 {{ formatDate(row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '结束' : '开始' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteActivity(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑活动' : '创建活动'"
      width="700px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="活动标题" required>
          <el-input v-model="form.title" placeholder="请输入活动标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="活动封面" required>
          <el-upload
            class="cover-uploader"
            :http-request="handleUpload"
            :show-file-list="false"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-image" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持 jpg、png 格式，大小不超过 5MB</div>
        </el-form-item>
        <el-form-item label="活动标签">
          <el-select
            v-model="form.tagId"
            placeholder="选择活动标签（可选）"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="tag in activityTags"
              :key="tag.tagId"
              :label="tag.tagName"
              :value="tag.tagId"
            >
              <span style="float: left">{{ tag.tagName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                {{ tag.useCount }} 次使用
              </span>
            </el-option>
          </el-select>
          <div class="tag-tip">
            选择后，用户发布帖子时可以选择此活动标签，类似小红书/B站的活动话题
          </div>
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入活动描述"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">进行中</el-radio>
            <el-radio :label="0">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../api/request'

const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const activityList = ref([])
const activityTags = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const form = ref({
  title: '',
  coverImage: '',
  tagId: null,
  description: '',
  startTime: '',
  endTime: '',
  status: 1
})


const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

const handleUpload = async (options) => {
  const { file } = options

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', 'activity')

    const token = localStorage.getItem('token')
    const response = await request({
      url: '/api/upload/image',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
        Authorization: token ? `Bearer ${token}` : ''
      }
    })

    form.value.coverImage = response
    ElMessage.success('上传成功')
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

const loadActivityTags = async () => {
  try {
    const data = await request({
      url: '/api/tag/activity',
      method: 'GET'
    })
    activityTags.value = data || []
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const loadActivities = async () => {
  loading.value = true
  try {
    const data = await request({
      url: '/api/activity/list',
      method: 'GET',
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    activityList.value = data.list || data.records || []
    total.value = data.total || 0
  } catch (error) {
    console.error('加载活动失败:', error)
    ElMessage.error('加载活动失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadActivities()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadActivities()
}

const showAddDialog = () => {
  isEdit.value = false
  editId.value = null
  form.value = {
    title: '',
    coverImage: '',
    tagId: null,
    description: '',
    startTime: '',
    endTime: '',
    status: 1
  }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.activityId
  form.value = {
    title: row.title || '',
    coverImage: row.coverImage || '',
    tagId: row.tagId || null,
    description: row.description || '',
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    status: row.status
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入活动标题')
    return
  }
  if (!form.value.coverImage) {
    ElMessage.warning('请上传活动封面')
    return
  }
  if (!form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请选择活动时间')
    return
  }

  submitting.value = true
  try {
    console.log('=== 开始创建活动 ===')
    console.log('Token:', localStorage.getItem('token'))

    const submitData = { ...form.value }

    if (isEdit.value) {
      await request({
        url: '/api/activity/update',
        method: 'PUT',
        data: { ...submitData, activityId: editId.value }
      })
      ElMessage.success('更新成功')
    } else {
      await request({
        url: '/api/activity/add',
        method: 'POST',
        data: submitData
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadActivities()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败：' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '开始' : '结束'

  try {
    await ElMessageBox.confirm(`确定要${actionText}这个活动吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request({
      url: '/api/activity/status',
      method: 'PUT',
      data: { activityId: row.activityId, status: newStatus }
    })
    ElMessage.success(`${actionText}成功`)
    loadActivities()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const deleteActivity = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个活动吗？此操作不可恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request({
      url: `/api/activity/delete/${row.activityId}`,
      method: 'DELETE'
    })
    ElMessage.success('删除成功')
    loadActivities()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadActivities()
  loadActivityTags()
})
</script>

<style scoped>
.activity-manage {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 300px;
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-uploader:hover {
  border-color: #409EFF;
}

.cover-uploader-icon {
  font-size: 48px;
  color: #8c939d;
  text-align: center;
}

.cover-image {
  width: 300px;
  height: 150px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.tag-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  line-height: 1.5;
}
</style>
