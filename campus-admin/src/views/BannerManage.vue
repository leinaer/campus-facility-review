<template>
  <div class="banner-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>轮播图管理</span>
          <el-button type="primary" @click="showAddDialog">添加轮播图</el-button>
        </div>
      </template>

      <el-table :data="bannerList" v-loading="loading" stripe>
        <el-table-column prop="bannerId" label="ID" width="80" />
        <el-table-column label="标题" width="200">
          <template #default="{ row }">{{ row.title || '未命名' }}</template>
        </el-table-column>
        <el-table-column label="图片预览" width="150">
          <template #default="{ row }">
            <el-image :src="getImageUrl(row.imageUrl)" fit="cover" style="width: 120px; height: 60px; border-radius: 4px" :preview-src-list="[getImageUrl(row.imageUrl)]" />
          </template>
        </el-table-column>
        <el-table-column label="跳转设施" min-width="200">
          <template #default="{ row }">
            <el-tag v-if="row.facilityId" type="primary">{{ getFacilityName(row.facilityId) }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="80" align="center">
          <template #default="{ row }">{{ row.sortOrder }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '已上架' : '已下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteBanner(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑轮播图' : '添加轮播图'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="轮播图片" required>
          <el-upload
            class="banner-uploader"
            :show-file-list="false"
            :http-request="handleUpload"
            :before-upload="beforeUpload"
            name="file"
            accept="image/*"
          >
            <img v-if="form.imageUrl" :src="getImageUrl(form.imageUrl)" class="banner-image" />
            <el-icon v-else class="banner-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸：750x360px，支持 JPG、PNG 格式，大小不超过 5MB</div>
        </el-form-item>
        <el-form-item label="跳转设施">
          <el-select v-model="form.facilityId" placeholder="选择设施（可选）" clearable filterable style="width: 100%">
            <el-option
              v-for="facility in facilityList"
              :key="facility.facilityId"
              :label="facility.name"
              :value="facility.facilityId"
            >
              <span style="float: left">{{ facility.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ facility.categoryName }}</span>
            </el-option>
          </el-select>
          <div class="form-tip">选择后点击轮播图将跳转到对应设施详情页</div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
          <div class="form-tip">数字越小排序越靠前</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
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
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const bannerList = ref([])
const facilityList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const uploading = ref(false)

const form = ref({
  title: '',
  imageUrl: '',
  facilityId: null,
  sortOrder: 0,
  status: 1
})

// 获取图片完整URL
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  // 如果后端返回的是相对路径，需要拼接基础URL
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return baseUrl + url
}

// 根据 facilityId 获取设施名称
const getFacilityName = (facilityId) => {
  const facility = facilityList.value.find(f => f.facilityId === facilityId)
  return facility ? facility.name : `设施ID: ${facilityId}`
}

// 上传前校验
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

// 自定义上传方法
const handleUpload = async (options) => {
  const { file } = options

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', 'banner')

    const response = await request({
      url: '/api/upload/image',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    // 后端返回的是图片URL
    form.value.imageUrl = response.data || response
    ElMessage.success('上传成功')
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

// 加载设施列表
const loadFacilityList = async () => {
  try {
    const response = await request({
      url: '/api/facility/admin/list',
      method: 'GET',
      params: {
        page: 1,
        size: 1000  // 获取所有设施
      }
    })
    // 后端返回的格式是 { list: [...], total: xx, page: xx, size: xx }
    facilityList.value = response.list || response.data?.list || []
  } catch (error) {
    console.error('加载设施列表失败:', error)
  }
}
// 加载轮播图列表
const loadBannerList = async () => {
  loading.value = true
  try {
    const data = await request({ url: '/api/banner/admin/list', method: 'GET' })
    bannerList.value = data || []
  } catch (error) {
    console.error('加载轮播图失败:', error)
    ElMessage.error('加载轮播图失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  editId.value = null
  form.value = { title: '', imageUrl: '', facilityId: null, sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.bannerId
  form.value = {
    title: row.title || '',
    imageUrl: row.imageUrl || '',
    facilityId: row.facilityId || null,
    sortOrder: row.sortOrder || 0,
    status: row.status
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.imageUrl) {
    ElMessage.warning('请上传轮播图片')
    return
  }
  submitting.value = true
  try {
    const url = isEdit.value ? '/api/banner/update' : '/api/banner/add'
    const method = isEdit.value ? 'PUT' : 'POST'
    const data = isEdit.value ? { ...form.value, bannerId: editId.value } : form.value
    await request({ url, method, data })
    ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
    dialogVisible.value = false
    loadBannerList()
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
    await ElMessageBox.confirm(`确定要${actionText}这个轮播图吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request({ url: `/api/banner/status/${row.bannerId}`, method: 'PUT', data: { status: newStatus } })
    ElMessage.success(`${actionText}成功`)
    loadBannerList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const deleteBanner = async (row) => {
  try {
    await ElMessageBox.confirm('删除后无法恢复，确定要删除这个轮播图吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request({ url: `/api/banner/delete/${row.bannerId}`, method: 'DELETE' })
    ElMessage.success('删除成功')
    loadBannerList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadBannerList()
  loadFacilityList()
})
</script>

<style scoped>
.banner-manage { padding: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.banner-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.banner-uploader:hover {
  border-color: #409EFF;
}

.banner-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 86px;
  text-align: center;
  line-height: 86px;
}

.banner-image {
  width: 178px;
  height: 86px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
