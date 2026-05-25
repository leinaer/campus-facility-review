<template>
  <div class="tag-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <div style="display: flex; gap: 10px; align-items: center">
            <el-select v-model="filterType" placeholder="标签类型" style="width: 150px" clearable @change="handleFilterChange">
              <el-option label="全部" :value="null" />
              <el-option label="普通标签" :value="1" />
              <el-option label="活动标签" :value="2" />
            </el-select>
          </div>
          <div style="display: flex; gap: 10px">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索标签名称"
              style="width: 300px"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
            <el-button type="primary" @click="showAddDialog">添加标签</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tagList" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="tagId" label="ID" width="80" />
        <el-table-column prop="tagName" label="标签名称" width="180">
          <template #default="{ row }">
            <el-tag :color="row.color" :style="{ color: getTextColor(row.color) }">
              {{ row.icon }} {{ row.tagName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.tagType === 2 ? 'success' : 'info'" size="small">
              {{ row.tagType === 2 ? '活动标签' : '普通标签' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="useCount" label="使用次数" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteTag(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center">
        <div>
          <el-button type="danger" size="small" :disabled="selectedTags.length === 0" @click="batchDelete">
            批量删除 ({{ selectedTags.length }})
          </el-button>
          <el-button type="success" size="small" :disabled="selectedTags.length === 0" @click="batchEnable">
            批量启用
          </el-button>
          <el-button type="warning" size="small" :disabled="selectedTags.length === 0" @click="batchDisable">
            批量禁用
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

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑标签' : '添加标签'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="标签名称" required>
          <el-input v-model="form.tagName" placeholder="请输入标签名称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="标签图标">
          <el-input v-model="form.icon" placeholder="请输入Emoji图标，如：🎉" maxlength="10" />
          <div class="form-tip">支持Emoji表情，留空则不显示图标</div>
        </el-form-item>
        <el-form-item label="标签颜色">
          <el-color-picker v-model="form.color" show-alpha />
          <div class="form-tip">选择标签背景颜色</div>
        </el-form-item>
        <el-form-item label="标签类型" required>
          <el-radio-group v-model="form.tagType">
            <el-radio :label="1">普通标签</el-radio>
            <el-radio :label="2">活动标签</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关联活动" v-if="form.tagType === 2">
          <el-select v-model="form.activityId" placeholder="选择关联的活动" clearable style="width: 100%">
            <el-option
              v-for="activity in activityList"
              :key="activity.activityId"
              :label="activity.title"
              :value="activity.activityId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
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
import request from '../api/request'

const loading = ref(false)
const submitting = ref(false)
const tagList = ref([])
const activityList = ref([])
const searchKeyword = ref('')
const filterType = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const selectedTags = ref([])

const form = ref({
  tagName: '',
  icon: '',
  color: '#409EFF',
  tagType: 1,
  activityId: null,
  status: 1
})

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getTextColor = (bgColor) => {
  if (!bgColor) return '#ffffff'
  const r = parseInt(bgColor.slice(1, 3), 16)
  const g = parseInt(bgColor.slice(3, 5), 16)
  const b = parseInt(bgColor.slice(5, 7), 16)
  const brightness = (r * 299 + g * 587 + b * 114) / 1000
  return brightness > 128 ? '#000000' : '#ffffff'
}

const loadTags = async () => {
  loading.value = true
  try {
    const response = await request({
      url: '/api/tag/admin/list',
      method: 'GET',
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchKeyword.value,
        tagType: filterType.value
      }
    })
    const data = response.data || response
    tagList.value = data.list || data.records || []
    total.value = data.total || 0
  } catch (error) {
    console.error('加载标签失败:', error)
    ElMessage.error('加载标签失败')
  } finally {
    loading.value = false
  }
}

const loadActivities = async () => {
  try {
    const data = await request({
      url: '/api/activity/list',
      method: 'GET',
      params: { page: 1, size: 100 }
    })
    activityList.value = data.list || data.records || []
  } catch (error) {
    console.error('加载活动列表失败:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadTags()
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadTags()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadTags()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadTags()
}

const handleSelectionChange = (selection) => {
  selectedTags.value = selection
}

const showAddDialog = () => {
  isEdit.value = false
  editId.value = null
  form.value = {
    tagName: '',
    icon: '',
    color: '#409EFF',
    tagType: 1,
    activityId: null,
    status: 1
  }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.tagId
  form.value = {
    tagName: row.tagName || '',
    icon: row.icon || '',
    color: row.color || '#409EFF',
    tagType: row.tagType || 1,
    activityId: row.activityId || null,
    status: row.status
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.tagName) {
    ElMessage.warning('请输入标签名称')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await request({
        url: '/api/tag/admin/update',
        method: 'PUT',
        data: { ...form.value, tagId: editId.value }
      })
      ElMessage.success('更新成功')
    } else {
      await request({
        url: '/api/tag/admin/add',
        method: 'POST',
        data: form.value
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTags()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确定要${actionText}这个标签吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request({
      url: `/api/tag/admin/status/${row.tagId}`,
      method: 'PUT',
      data: { status: newStatus }
    })
    ElMessage.success(`${actionText}成功`)
    loadTags()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const deleteTag = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除标签 "${row.tagName}" 吗？此操作不可恢复！`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request({
      url: `/api/tag/admin/delete/${row.tagId}`,
      method: 'DELETE'
    })
    ElMessage.success('删除成功')
    loadTags()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedTags.value.length} 个标签吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedTags.value.map(t => t.tagId)
    await request({
      url: '/api/tag/admin/batch-delete',
      method: 'POST',
      data: { ids }
    })
    ElMessage.success('批量删除成功')
    loadTags()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const batchEnable = async () => {
  try {
    await ElMessageBox.confirm(`确定要启用选中的 ${selectedTags.value.length} 个标签吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedTags.value.map(t => t.tagId)
    await request({
      url: '/api/tag/admin/batch-status',
      method: 'POST',
      data: { ids, status: 1 }
    })
    ElMessage.success('批量启用成功')
    loadTags()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量操作失败:', error)
      ElMessage.error('批量操作失败')
    }
  }
}

const batchDisable = async () => {
  try {
    await ElMessageBox.confirm(`确定要禁用选中的 ${selectedTags.value.length} 个标签吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedTags.value.map(t => t.tagId)
    await request({
      url: '/api/tag/admin/batch-status',
      method: 'POST',
      data: { ids, status: 0 }
    })
    ElMessage.success('批量禁用成功')
    loadTags()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量操作失败:', error)
      ElMessage.error('批量操作失败')
    }
  }
}

onMounted(() => {
  loadTags()
  loadActivities()
})
</script>

<style scoped>
.tag-manage {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
