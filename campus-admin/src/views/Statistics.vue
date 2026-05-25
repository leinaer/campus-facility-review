<template>
  <div class="statistics">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-wrapper">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">📝</div>
            <div class="stat-info">
              <div class="stat-num">{{ overview.todayEvaluations }}</div>
              <div class="stat-label">今日评价</div>
              <div class="stat-trend" :class="overview.todayEvaluationsGrowth >= 0 ? 'up' : 'down'">
                <span v-if="overview.todayEvaluationsGrowth">{{ overview.todayEvaluationsGrowth >= 0 ? '↑' : '↓' }} {{ Math.abs(overview.todayEvaluationsGrowth) }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-wrapper">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">👥</div>
            <div class="stat-info">
              <div class="stat-num">{{ overview.todayUsers }}</div>
              <div class="stat-label">今日新增用户</div>
              <div class="stat-trend" :class="overview.todayUsersGrowth >= 0 ? 'up' : 'down'">
                <span v-if="overview.todayUsersGrowth">{{ overview.todayUsersGrowth >= 0 ? '↑' : '↓' }} {{ Math.abs(overview.todayUsersGrowth) }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-wrapper">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">🏫</div>
            <div class="stat-info">
              <div class="stat-num">{{ overview.totalFacilities }}</div>
              <div class="stat-label">设施总数</div>
              <div class="stat-trend">
                <span>共 {{ overview.activeFacilities || 0 }} 个在用</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card-wrapper">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">⭐</div>
            <div class="stat-info">
              <div class="stat-num">{{ overview.totalEvaluations }}</div>
              <div class="stat-label">评价总数</div>
              <div class="stat-trend">
                <span>均分 {{ overview.avgRating || '0.0' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>评价趋势（{{ trendDays === 7 ? '近7天' : '近30天' }}）</span>
              <el-radio-group v-model="trendDays" size="small" @change="handleTrendChange">
                <el-radio-button :label="7">7天</el-radio-button>
                <el-radio-button :label="30">30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="barChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>分类评价占比</span>
          </template>
          <div ref="pieChartRef" class="pie-chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>评分分布</span>
          </template>
          <div ref="ratingChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>用户增长趋势</span>
          </template>
          <div ref="userChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类统计表 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>分类统计</span>
        </div>
      </template>
      <el-table :data="categoryStats" stripe>
        <el-table-column prop="categoryName" label="分类名称" />
        <el-table-column prop="facilityCount" label="设施数量" width="120" align="center" />
        <el-table-column prop="evaluationCount" label="评价数量" width="120" align="center" />
        <el-table-column prop="avgRating" label="平均评分" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f5a623; font-weight: bold">{{ row.avgRating || '0.0' }} ⭐</span>
          </template>
        </el-table-column>
        <el-table-column label="占比" width="250">
          <template #default="{ row, $index }">
            <el-progress :percentage="parseFloat(row.percent)" :color="colors[$index % colors.length]" :stroke-width="10" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 热门设施 TOP 10 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>🏆 热门设施 TOP 10</span>
          <el-tag type="success">按评价数排序</el-tag>
        </div>
      </template>
      <el-table :data="topFacilities" stripe>
        <el-table-column label="排名" width="100" align="center">
          <template #default="{ $index }">
            <div class="rank-badge" :class="{ 'top3': $index < 3 }">
              <span v-if="$index === 0">🥇</span>
              <span v-else-if="$index === 1">🥈</span>
              <span v-else-if="$index === 2">🥉</span>
              <span v-else>{{ $index + 1 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="设施名称">
          <template #default="{ row }">
            <div class="facility-name">
              <el-avatar :src="row.coverImage" size="small" v-if="row.coverImage" />
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="evaluationCount" label="评价数" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.evaluationCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="avgRating" label="平均评分" width="150" align="center">
          <template #default="{ row }">
            <div class="rating-display">
              <el-rate v-model="row.avgRating" disabled show-score text-color="#f5a623" score-template="{value}" />
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import request from '../api/request'

const overview = ref({
  todayEvaluations: 0,
  todayUsers: 0,
  totalFacilities: 0,
  totalEvaluations: 0,
  avgRating: '0.0',
  activeFacilities: 0
})

const trendDays = ref(7)
const evaluationTrend = ref([])
const categoryStats = ref([])
const topFacilities = ref([])
const userTrend = ref([])
const ratingDistribution = ref([])
const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#43e97b', '#fa709a', '#fee140']

const barChartRef = ref(null)
const pieChartRef = ref(null)
const ratingChartRef = ref(null)
const userChartRef = ref(null)
let barChart = null
let pieChart = null
let ratingChart = null
let userChart = null

const loadOverview = async () => {
  try {
    const data = await request({ url: '/api/statistics/admin/overview', method: 'GET' })
    overview.value = {
      todayEvaluations: data?.todayEvaluations || 0,
      todayUsers: data?.todayUsers || 0,
      totalFacilities: data?.totalFacilities || 0,
      totalEvaluations: data?.totalEvaluations || 0,
      avgRating: data?.avgRating || '0.0',
      activeFacilities: data?.activeFacilities || 0,
      todayEvaluationsGrowth: data?.todayEvaluationsGrowth || 0,
      todayUsersGrowth: data?.todayUsersGrowth || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadEvaluationTrend = async () => {
  try {
    const data = await request({
      url: '/api/statistics/admin/evaluation-trend',
      method: 'GET',
      params: { days: trendDays.value }
    })
    evaluationTrend.value = data || []
    renderBarChart()
  } catch (error) {
    console.error('加载评价趋势失败:', error)
  }
}

const loadCategoryStats = async () => {
  try {
    const data = await request({ url: '/api/statistics/admin/category-stats', method: 'GET' })
    const categoryData = data || []
    const total = categoryData.reduce((sum, item) => sum + (item.facilityCount || 0), 0)
    categoryStats.value = categoryData.map(item => ({
      ...item,
      percent: total > 0 ? ((item.facilityCount || 0) / total * 100).toFixed(1) : 0
    }))
    renderPieChart()
  } catch (error) {
    console.error('加载分类统计失败:', error)
  }
}

const loadTopFacilities = async () => {
  try {
    const data = await request({ url: '/api/statistics/top-facilities', method: 'GET', params: { limit: 10 } })
    topFacilities.value = (data || []).map(item => ({
      ...item,
      avgRating: parseFloat(item.avgRating) || 0
    }))
  } catch (error) {
    console.error('加载热门设施失败:', error)
  }
}

const loadUserTrend = async () => {
  try {
    const data = await request({ url: '/api/statistics/admin/user-trend', method: 'GET', params: { days: 7 } })
    userTrend.value = data || []
    renderUserChart()
  } catch (error) {
    console.error('加载用户趋势失败:', error)
  }
}

const loadRatingDistribution = async () => {
  try {
    const data = await request({ url: '/api/statistics/admin/rating-distribution', method: 'GET' })
    ratingDistribution.value = data || [
      { rating: '5星', count: 0 },
      { rating: '4星', count: 0 },
      { rating: '3星', count: 0 },
      { rating: '2星', count: 0 },
      { rating: '1星', count: 0 }
    ]
    renderRatingChart()
  } catch (error) {
    console.error('加载评分分布失败:', error)
  }
}

const handleTrendChange = () => {
  loadEvaluationTrend()
}

const renderBarChart = () => {
  if (!barChartRef.value || !evaluationTrend.value.length) return

  if (!barChart) {
    barChart = echarts.init(barChartRef.value)
  }

  const dates = evaluationTrend.value.map(item => item.date)
  const counts = evaluationTrend.value.map(item => item.count || 0)

  const rotateLabel = trendDays.value > 7 ? 45 : 0
  const labelInterval = trendDays.value > 7 ? Math.floor(dates.length / 10) : 0

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
        shadowStyle: { color: 'rgba(102, 126, 234, 0.1)' }
      },
      formatter: '{b}<br/>评价数：{c}',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: '#667eea',
      borderWidth: 1,
      textStyle: { color: '#fff', fontSize: 13 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: trendDays.value > 7 ? '20%' : '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisLabel: {
        color: '#666',
        fontSize: 12,
        interval: labelInterval,
        rotate: rotateLabel,
        formatter: function(value) {
          if (value && value.includes('-')) {
            const parts = value.split('-')
            return parts[1] + '-' + parts[2]
          }
          return value
        }
      },
      axisTick: { alignWithLabel: true }
    },
    yAxis: {
      type: 'value',
      name: '评价数',
      nameTextStyle: { color: '#999', fontSize: 12 },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }
    },
    series: [{
      name: '评价数',
      type: 'bar',
      data: counts,
      barWidth: trendDays.value > 7 ? '30%' : '45%',
      itemStyle: {
        borderRadius: [8, 8, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f093fb' },
            { offset: 1, color: '#f5576c' }
          ]),
          shadowBlur: 10,
          shadowColor: 'rgba(102, 126, 234, 0.5)'
        }
      },
      label: {
        show: trendDays.value <= 7,
        position: 'top',
        color: '#667eea',
        fontSize: 14,
        fontWeight: 'bold'
      },
      animationDuration: 1500,
      animationEasing: 'elasticOut'
    }]
  }

  barChart.setOption(option, true)
}

const renderPieChart = () => {
  if (!pieChartRef.value || !categoryStats.value.length) return

  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: '#667eea',
      borderWidth: 1,
      textStyle: { color: '#fff', fontSize: 13 }
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { fontSize: 12, color: '#666' },
      itemGap: 15,
      icon: 'circle'
    },
    series: [{
      name: '分类占比',
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 3
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        fontSize: 12,
        color: '#666'
      },
      labelLine: {
        show: true,
        length: 20,
        length2: 15,
        lineStyle: { color: '#999' }
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold',
          color: '#333'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      data: categoryStats.value.map((item, index) => ({
        value: item.facilityCount || 0,
        name: item.categoryName || '未分类',
        itemStyle: { color: colors[index % colors.length] }
      })),
      animationType: 'scale',
      animationEasing: 'elasticOut',
      animationDuration: 1500
    }]
  }

  pieChart.setOption(option)
}

const renderRatingChart = () => {
  if (!ratingChartRef.value) return

  if (!ratingChart) {
    ratingChart = echarts.init(ratingChartRef.value)
  }

  const ratings = ratingDistribution.value.map(item => item.rating)
  const counts = ratingDistribution.value.map(item => item.count || 0)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c} 条评价',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: '#f5a623',
      borderWidth: 1,
      textStyle: { color: '#fff', fontSize: 13 }
    },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ratings,
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisLabel: { color: '#666', fontSize: 13 }
    },
    yAxis: {
      type: 'value',
      name: '评价数',
      nameTextStyle: { color: '#999', fontSize: 12 },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }
    },
    series: [{
      name: '评价数',
      type: 'bar',
      data: counts,
      barWidth: '50%',
      itemStyle: {
        borderRadius: [8, 8, 0, 0],
        color: function(params) {
          const colorList = ['#f5a623', '#43e97b', '#4facfe', '#fa709a', '#f5576c']
          return colorList[params.dataIndex] || '#667eea'
        }
      },
      label: {
        show: true,
        position: 'top',
        color: '#666',
        fontSize: 14,
        fontWeight: 'bold'
      },
      animationDuration: 1500,
      animationEasing: 'bounceOut'
    }]
  }

  ratingChart.setOption(option)
}

const renderUserChart = () => {
  if (!userChartRef.value || !userTrend.value.length) return

  if (!userChart) {
    userChart = echarts.init(userChartRef.value)
  }

  const dates = userTrend.value.map(item => item.date)
  const counts = userTrend.value.map(item => item.count || 0)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>新增用户：{c}',
      backgroundColor: 'rgba(50, 50, 50, 0.9)',
      borderColor: '#43e97b',
      borderWidth: 1,
      textStyle: { color: '#fff', fontSize: 13 }
    },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisLabel: { color: '#666', fontSize: 12, interval: 0 }
    },
    yAxis: {
      type: 'value',
      name: '用户数',
      nameTextStyle: { color: '#999', fontSize: 12 },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#666', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }
    },
    series: [{
      name: '新增用户',
      type: 'line',
      data: counts,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        color: '#43e97b',
        width: 3
      },
      itemStyle: {
        color: '#43e97b',
        borderWidth: 2,
        borderColor: '#fff'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(67, 233, 123, 0.3)' },
          { offset: 1, color: 'rgba(67, 233, 123, 0.05)' }
        ])
      },
      label: {
        show: true,
        position: 'top',
        color: '#43e97b',
        fontSize: 13,
        fontWeight: 'bold'
      },
      animationDuration: 1500,
      animationEasing: 'cubicOut'
    }]
  }

  userChart.setOption(option)
}

onMounted(() => {
  loadOverview()
  loadEvaluationTrend()
  loadCategoryStats()
  loadTopFacilities()
  loadUserTrend()
  loadRatingDistribution()

  window.addEventListener('resize', () => {
    barChart?.resize()
    pieChart?.resize()
    ratingChart?.resize()
    userChart?.resize()
  })
})

onBeforeUnmount(() => {
  barChart?.dispose()
  pieChart?.dispose()
  ratingChart?.dispose()
  userChart?.dispose()
  window.removeEventListener('resize', () => {
    barChart?.resize()
    pieChart?.resize()
    ratingChart?.resize()
    userChart?.resize()
  })
})
</script>

<style scoped>
.statistics { padding: 0; }

.stat-card-wrapper { transition: all 0.3s; }
.stat-card-wrapper:hover { transform: translateY(-5px); }

.stat-card { display: flex; align-items: center; gap: 15px; }
.stat-icon { width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 28px; flex-shrink: 0; }
.stat-info { flex: 1; }
.stat-num { font-size: 32px; font-weight: bold; color: #303133; line-height: 1; }
.stat-label { font-size: 14px; color: #909399; margin-top: 8px; }
.stat-trend { font-size: 12px; margin-top: 6px; }
.stat-trend.up { color: #67c23a; }
.stat-trend.down { color: #f56c6c; }

.card-header { display: flex; justify-content: space-between; align-items: center; }

.chart-container, .pie-chart-container { width: 100%; height: 350px; padding: 10px 0; }

.rank-badge { font-size: 18px; font-weight: bold; }
.rank-badge.top3 { font-size: 24px; }

.facility-name { display: flex; align-items: center; gap: 10px; }

.rating-display { display: flex; justify-content: center; }
</style>
