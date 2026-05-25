<template>
  <div class="topic-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>话题管理</span>
          <div style="display: flex; gap: 10px">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索话题名称"
              style="width: 250px"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
            <el-button type="primary" @click="showAddDialog">添加话题</el-button>
          </div>
        </div>
      </template>

      <el-table :data="topicList" v-loading="loading" stripe>
        <el-table-column prop="topicId" label="ID" width="80" />
        <el-table-column label="话题图标" width="100" align="center">
          <template #default="{ row }">
            <span style="font-size: 28px">{{ row.icon || '️' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="话题名称" width="180" />
        <el-table-column prop="description" label="话题描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="帖子数" width="100" align="center">
          <template #default="{ row }">
            {{ row.postCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="关注数" width="100" align="center">
          <template #default="{ row }">
            {{ row.followCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
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
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteTopic(row)">删除</el-button>
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑话题' : '添加话题'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="话题图标" required>
          <el-input v-model="form.icon" placeholder="请输入Emoji图标，如：🍔" style="width: 200px" />
          <span style="font-size: 32px; margin-left: 10px">{{ form.icon || '🏷️' }}</span>
        </el-form-item>
        <el-form-item label="话题名称" required>
          <el-input v-model="form.name" placeholder="请输入话题名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="话题描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入话题描述"
            maxlength="500"
            show-word-limit
          />
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
const topicList = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const form = ref({
  name: '',
  icon: '',
  description: '',
  status: 1
})

const loadTopics = async () => {
  loading.value = true
  try {
    const data = await request({
      url: '/api/topic/list',
      method: 'GET',
      params: {
        sortBy: 'hot',
        keyword: searchKeyword.value
      }
    })
    topicList.value = data || []
    total.value = data.length || 0
  } catch (error) {
    console.error('加载话题失败:', error)
    ElMessage.error('加载话题失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadTopics()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadTopics()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadTopics()
}

const showAddDialog = () => {
  isEdit.value = false
  editId.value = null
  form.value = { name: '', icon: '', description: '', status: 1 }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.topicId
  form.value = {
    name: row.name || '',
    icon: row.icon || '',
    description: row.description || '',
    status: row.status
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入话题名称')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await request({
        url: '/api/topic/update',
        method: 'PUT',
        data: { ...form.value, topicId: editId.value }
      })
      ElMessage.success('更新成功')
    } else {
      await request({
        url: '/api/topic/add',
        method: 'POST',
        data: form.value
      })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadTopics()
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
    await ElMessageBox.confirm(`确定要${actionText}这个话题吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request({
      url: '/api/topic/status',
      method: 'PUT',
      data: { topicId: row.topicId, status: newStatus }
    })
    ElMessage.success(`${actionText}成功`)
    loadTopics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const deleteTopic = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个话题吗？此操作不可恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request({
      url: `/api/topic/delete/${row.topicId}`,
      method: 'DELETE'
    })
    ElMessage.success('删除成功')
    loadTopics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadTopics()
})
</script>

<style scoped>
.topic-manage {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
