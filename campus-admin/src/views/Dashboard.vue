<template>
  <div class="dashboard">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statistics" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: item.color }">
              {{ item.icon }}
            </div>
            <div class="stat-info">
              <div class="stat-num">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作和系统通知 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 快捷操作 -->
      <el-col :span="16">
        <el-card shadow="hover" class="quick-actions">
          <template #header>
            <span>⚡ 快捷操作</span>
          </template>
          <div class="action-grid">
            <div class="action-item" @click="router.push('/post')">
              <div class="action-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
                📝
              </div>
              <div class="action-label">帖子管理</div>
            </div>
            <div class="action-item" @click="router.push('/evaluation')">
              <div class="action-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
                💬
              </div>
              <div class="action-label">评价管理</div>
            </div>
            <div class="action-item" @click="router.push('/facility')">
              <div class="action-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
                🏫
              </div>
              <div class="action-label">设施管理</div>
            </div>
            <div class="action-item" @click="router.push('/topic')">
              <div class="action-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
                🏷️
              </div>
              <div class="action-label">话题管理</div>
            </div>
            <div class="action-item" @click="router.push('/activity')">
              <div class="action-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
                🎉
              </div>
              <div class="action-label">活动管理</div>
            </div>
            <div class="action-item" @click="router.push('/category')">
              <div class="action-icon" style="background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)">
                📂
              </div>
              <div class="action-label">分类管理</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 系统通知 -->
      <el-col :span="8">
        <el-card shadow="hover" class="notifications">
          <template #header>
            <div class="card-header">
              <span>🔔 系统通知</span>
              <el-button type="primary" link size="small">查看全部</el-button>
            </div>
          </template>
          <div class="notification-list">
            <div class="notification-item" v-for="(item, index) in notifications" :key="index">
              <div class="notification-dot" :class="item.type"></div>
              <div class="notification-content">
                <div class="notification-text">{{ item.text }}</div>
                <div class="notification-time">{{ item.time }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近评价和热门设施 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>📝 最近评价</span>
              <el-button type="primary" link size="small" @click="router.push('/evaluation')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentEvaluations" stripe size="small">
            <el-table-column prop="userName" label="用户" width="120" />
            <el-table-column prop="facilityName" label="设施" />
            <el-table-column prop="rating" label="评分" width="120" align="center">
              <template #default="{ row }">
                <span style="color: #f5a623">{{ row.rating }}⭐</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="160" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>🔥 热门设施 TOP 5</span>
              <el-button type="primary" link size="small" @click="router.push('/statistics')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="topFacilities" stripe size="small">
            <el-table-column label="排名" width="80" align="center">
              <template #default="{ $index }">
                <el-tag :type="$index < 3 ? 'danger' : 'info'" size="small">{{ $index + 1 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="设施名称" />
            <el-table-column prop="reviewCount" label="评价数" width="100" align="center" />
            <el-table-column prop="rating" label="评分" width="100" align="center">
              <template #default="{ row }">
                <span style="color: #f5a623; font-weight: bold">{{ row.rating }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import request from '../api/request'
import { getNoticeList } from '../api/notice'

const router = useRouter()

const statistics = ref([
  { label: '帖子数', value: 0, icon: '📝', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { label: '评价数', value: 0, icon: '💬', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { label: '用户数', value: 0, icon: '👥', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { label: '设施数', value: 0, icon: '🏫', color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }
])

const recentEvaluations = ref([])
const topFacilities = ref([])

const notifications = ref([])

const formatTime = (time) => {
  if (!time) return ''
  const now = new Date()
  const noticeTime = new Date(time)
  const diff = now - noticeTime
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return time.split('T')[0]
}

onMounted(async () => {
  try {
    const data = await request({
      url: '/api/statistics/admin/overview',
      method: 'GET'
    })

    statistics.value[0].value = data.totalPosts || 0
    statistics.value[1].value = data.totalEvaluations || 0
    statistics.value[2].value = data.totalUsers || 0
    statistics.value[3].value = data.totalFacilities || 0

    recentEvaluations.value = (data.recentEvaluations || []).slice(0, 5)
    topFacilities.value = (data.topFacilities || []).slice(0, 5)

    const noticeList = await getNoticeList({ size: 5 })
    notifications.value = noticeList.map(item => ({
      ...item,
      time: formatTime(item.publishTime),
      type: item.isTop ? 'danger' : 'success'
    }))
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-num {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions {
  height: 100%;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 15px 10px;
  border-radius: 8px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: #ecf5ff;
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.action-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.notifications {
  height: 100%;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  gap: 10px;
  padding: 10px;
  border-radius: 6px;
  background: #f5f7fa;
}

.notification-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.notification-dot.success {
  background: #67c23a;
}

.notification-dot.warning {
  background: #e6a23c;
}

.notification-dot.info {
  background: #409eff;
}

.notification-dot.danger {
  background: #f56c6c;
}

.notification-content {
  flex: 1;
}

.notification-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.notification-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
