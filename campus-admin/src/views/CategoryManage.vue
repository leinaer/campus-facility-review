<template>
  <div class="category-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>分类管理</span>
          <el-button type="primary" @click="showAddDialog">添加分类</el-button>
        </div>
      </template>

      <el-table :data="categoryList" v-loading="loading" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="categoryId" label="ID" width="80" />
        <el-table-column label="分类图标" width="100" align="center">
          <template #default="{ row }">
            <el-icon :size="32" v-if="row.iconUrl">
              <Food v-if="row.iconUrl === 'Food'" />
              <Reading v-else-if="row.iconUrl === 'Reading'" />
              <School v-else-if="row.iconUrl === 'School'" />
              <Basketball v-else-if="row.iconUrl === 'Basketball'" />
              <FirstAidKit v-else-if="row.iconUrl === 'FirstAidKit'" />
              <ShoppingCart v-else-if="row.iconUrl === 'ShoppingCart'" />
              <Coffee v-else-if="row.iconUrl === 'Coffee'" />
              <OfficeBuilding v-else-if="row.iconUrl === 'OfficeBuilding'" />
              <Van v-else-if="row.iconUrl === 'Van'" />
              <Postcard v-else-if="row.iconUrl === 'Postcard'" />
              <House v-else-if="row.iconUrl === 'House'" />
            </el-icon>
            <span v-else style="color: #999">无图标</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="sortOrder" label="排序" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.sortOrder }}</el-tag>
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center">
        <div>
          <el-button type="danger" size="small" :disabled="selectedCategories.length === 0" @click="batchDelete">
            批量删除 ({{ selectedCategories.length }})
          </el-button>
        </div>
        <div style="color: #909399; font-size: 14px">
          共 {{ total }} 个分类
        </div>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.name" placeholder="请输入分类名称，如：食堂" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="分类图标" required>
          <div class="icon-selector">
            <div
              v-for="icon in iconOptions"
              :key="icon.name"
              class="icon-item"
              :class="{ active: form.iconUrl === icon.name }"
              @click="form.iconUrl = icon.name"
            >
              <el-icon :size="28">
                <component :is="icon.component" />
              </el-icon>
              <div class="icon-name">{{ icon.label }}</div>
            </div>
          </div>
          <div class="form-tip">点击选择图标，适合用于分类展示</div>
        </el-form-item>
        <el-form-item label="排序号" required>
          <el-input-number v-model="form.sortOrder" :min="1" :max="999" />
          <div class="form-tip">数字越小排序越靠前</div>
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
import {
  House,
  OfficeBuilding,
  Reading,
  ShoppingCart,
  Basketball,
  Food,
  Coffee,
  Van,
  School,
  FirstAidKit,
  Postcard
} from '@element-plus/icons-vue'
import request from '../api/request'

const loading = ref(false)
const submitting = ref(false)
const categoryList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const selectedCategories = ref([])

const form = ref({
  name: '',
  iconUrl: '',
  sortOrder: 1,
  status: 1
})

const iconOptions = [
  { name: 'Food', label: '食堂', component: Food },
  { name: 'Reading', label: '图书馆', component: Reading },
  { name: 'School', label: '教学楼', component: School },
  { name: 'Basketball', label: '体育馆', component: Basketball },
  { name: 'FirstAidKit', label: '医务室', component: FirstAidKit },
  { name: 'ShoppingCart', label: '超市', component: ShoppingCart },
  { name: 'Coffee', label: '咖啡厅', component: Coffee },
  { name: 'OfficeBuilding', label: '办公楼', component: OfficeBuilding },
  { name: 'Van', label: '快递点', component: Van },
  { name: 'Postcard', label: '活动中心', component: Postcard },
  { name: 'House', label: '宿舍', component: House }
]

const getIconComponent = (iconName) => {
  const icon = iconOptions.find(i => i.name === iconName)
  return icon ? icon.component : null
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const loadCategories = async () => {
  loading.value = true
  try {
    const data = await request({
      url: '/api/category/admin/list',
      method: 'GET'
    })
    categoryList.value = data || []
    total.value = categoryList.value.length
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection) => {
  selectedCategories.value = selection
}

const showAddDialog = () => {
  isEdit.value = false
  editId.value = null
  form.value = {
    name: '',
    iconUrl: '',
    sortOrder: categoryList.value.length + 1,
    status: 1
  }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.categoryId
  form.value = {
    name: row.name || '',
    iconUrl: row.iconUrl || '',
    sortOrder: row.sortOrder || 1,
    status: row.status
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  if (!form.value.iconUrl) {
    ElMessage.warning('请选择分类图标')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await request({
        url: '/api/category/update',
        method: 'PUT',
        data: { ...form.value, categoryId: editId.value }
      })
      ElMessage.success('更新成功')
    } else {
      await request({
        url: '/api/category/add',
        method: 'POST',
        data: form.value
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadCategories()
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
    await ElMessageBox.confirm(`确定要${actionText}这个分类吗？禁用后该分类下的设施将不可见。`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request({
      url: `/api/category/status/${row.categoryId}`,
      method: 'PUT',
      data: { status: newStatus }
    })
    ElMessage.success(`${actionText}成功`)
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const deleteCategory = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除分类 "${row.name}" 吗？删除后该分类下的设施将失去分类。`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request({
      url: `/api/category/delete/${row.categoryId}`,
      method: 'DELETE'
    })
    ElMessage.success('删除成功')
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedCategories.value.length} 个分类吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedCategories.value.map(c => c.categoryId)
    for (let id of ids) {
      await request({
        url: `/api/category/delete/${id}`,
        method: 'DELETE'
      })
    }
    ElMessage.success('批量删除成功')
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-manage {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.icon-selector {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  max-height: 300px;
  overflow-y: auto;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
  border: 2px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f7fa;
}

.icon-item:hover {
  border-color: #409EFF;
  background: #ecf5ff;
}

.icon-item.active {
  border-color: #409EFF;
  background: #ecf5ff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.icon-item .el-icon {
  margin-bottom: 6px;
  color: #409EFF;
}

.icon-name {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
