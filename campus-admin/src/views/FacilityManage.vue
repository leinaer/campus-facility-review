<template>
  <div class="facility-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <div style="display: flex; gap: 10px; align-items: center">
            <el-select v-model="filterCategory" placeholder="全部分类" style="width: 150px" clearable @change="handleFilterChange">
              <el-option label="全部分类" :value="null" />
              <el-option v-for="cat in categoryList" :key="cat.categoryId" :label="cat.name" :value="cat.categoryId" />
            </el-select>
            <el-select v-model="filterCampus" placeholder="全部校区" style="width: 150px" clearable @change="handleFilterChange">
              <el-option label="全部校区" :value="null" />
              <el-option label="东校区" value="东校区" />
              <el-option label="西校区" value="西校区" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="全部状态" style="width: 150px" clearable @change="handleFilterChange">
              <el-option label="全部状态" :value="null" />
              <el-option label="正常" :value="1" />
              <el-option label="下架" :value="0" />
            </el-select>
          </div>
          <div style="display: flex; gap: 10px">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索设施名称"
              style="width: 300px"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
            <el-button type="primary" @click="showAddDialog">添加设施</el-button>
          </div>
        </div>
      </template>

      <el-table :data="facilityList" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="facilityId" label="ID" width="80" />
        <el-table-column label="设施封面" width="150">
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
        <el-table-column prop="name" label="设施名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.categoryName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" width="180" show-overflow-tooltip />
        <el-table-column prop="campus" label="校区" width="100" align="center" />
        <el-table-column label="评分" width="100" align="center">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score text-color="#ff9900" score-template="{value}" />
          </template>
        </el-table-column>
        <el-table-column prop="reviewCount" label="评价数" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '下架' }}
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
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteFacility(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center">
        <div>
          <el-button type="danger" size="small" :disabled="selectedFacilities.length === 0" @click="batchDelete">
            批量删除 ({{ selectedFacilities.length }})
          </el-button>
          <el-button type="warning" size="small" :disabled="selectedFacilities.length === 0" @click="batchOffline">
            批量下架
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
      :title="isEdit ? '编辑设施' : '添加设施'"
      width="700px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="设施名称" required>
          <el-input v-model="form.name" placeholder="请输入设施名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="设施分类" required>
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option
              v-for="cat in categoryList"
              :key="cat.categoryId"
              :label="cat.name"
              :value="cat.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设施封面">
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
        <el-form-item label="位置描述" required>
          <el-input v-model="form.location" placeholder="如：南校区东门旁" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="所属校区" required>
          <el-radio-group v-model="form.campus">
            <el-radio label="东校区">东校区</el-radio>
            <el-radio label="西校区">西校区</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="综合评分">
          <el-rate v-model="form.rating" allow-half :max="5" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">下架</el-radio>
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
const facilityList = ref([])
const categoryList = ref([])
const searchKeyword = ref('')
const filterCategory = ref(null)
const filterCampus = ref(null)
const filterStatus = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const selectedFacilities = ref([])

const form = ref({
  name: '',
  categoryId: null,
  coverImage: '',
  location: '',
  campus: '南校区',
  rating: 0,
  reviewCount: 0,
  status: 1
})

const loadCategories = async () => {
  try {
    const data = await request({
      url: '/api/category/list',
      method: 'GET'
    })
    categoryList.value = data || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadFacilities = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
      categoryId: filterCategory.value,
      campus: filterCampus.value,
      status: filterStatus.value
    }

    const data = await request({
      url: '/api/facility/admin/list',
      method: 'GET',
      params
    })

    const listData = data.data || data
    facilityList.value = (listData.list || listData.records || []).map(item => {
      const category = categoryList.value.find(c => c.categoryId === item.categoryId)
      return {
        ...item,
        categoryName: category ? category.name : '未分类'
      }
    })
    total.value = listData.total || 0
  } catch (error) {
    console.error('加载设施失败:', error)
    ElMessage.error('加载设施失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadFacilities()
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadFacilities()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadFacilities()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadFacilities()
}

const handleSelectionChange = (selection) => {
  selectedFacilities.value = selection
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
    formData.append('type', 'facility')

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

const showAddDialog = () => {
  isEdit.value = false
  editId.value = null
  form.value = {
    name: '',
    categoryId: null,
    coverImage: '',
    location: '',
    campus: '南校区',
    rating: 0,
    reviewCount: 0,
    status: 1
  }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.facilityId
  form.value = {
    name: row.name || '',
    categoryId: row.categoryId || null,
    coverImage: row.coverImage || '',
    location: row.location || '',
    campus: row.campus || '南校区',
    rating: parseFloat(row.rating) || 0,
    reviewCount: row.reviewCount || 0,
    status: row.status
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入设施名称')
    return
  }
  if (!form.value.categoryId) {
    ElMessage.warning('请选择设施分类')
    return
  }
  if (!form.value.location) {
    ElMessage.warning('请输入位置描述')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await request({
        url: '/api/facility/update',
        method: 'PUT',
        data: { ...form.value, facilityId: editId.value }
      })
      ElMessage.success('更新成功')
    } else {
      await request({
        url: '/api/facility/add',
        method: 'POST',
        data: form.value
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadFacilities()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '上架' : '下架'

  try {
    await ElMessageBox.confirm(`确定要${actionText}这个设施吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (newStatus === 0) {
      await request({
        url: `/api/facility/offline/${row.facilityId}`,
        method: 'PUT'
      })
    } else {
      await request({
        url: '/api/facility/update',
        method: 'PUT',
        data: { facilityId: row.facilityId, status: 1 }
      })
    }

    ElMessage.success(`${actionText}成功`)
    loadFacilities()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const deleteFacility = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除设施 "${row.name}" 吗？此操作不可恢复！`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request({
      url: `/api/facility/delete/${row.facilityId}`,
      method: 'DELETE'
    })
    ElMessage.success('删除成功')
    loadFacilities()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedFacilities.value.length} 个设施吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedFacilities.value.map(f => f.facilityId)
    await request({
      url: '/api/facility/admin/batch-delete',
      method: 'POST',
      data: { ids }
    })
    ElMessage.success('批量删除成功')
    loadFacilities()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const batchOffline = async () => {
  try {
    await ElMessageBox.confirm(`确定要下架选中的 ${selectedFacilities.value.length} 个设施吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedFacilities.value.map(f => f.facilityId)
    await request({
      url: '/api/facility/admin/batch-offline',
      method: 'POST',
      data: { ids }
    })
    ElMessage.success('批量下架成功')
    loadFacilities()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量操作失败:', error)
      ElMessage.error('批量操作失败')
    }
  }
}

onMounted(() => {
  loadCategories()
  loadFacilities()
})
</script>

<style scoped>
.facility-manage {
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
</style>
