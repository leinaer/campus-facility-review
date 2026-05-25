<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <h2>🎓 校园生活圈管理</h2>
    </div>

    <el-menu
      :default-active="activeMenu"
      router
      class="sidebar-menu"
    >
      <el-menu-item index="/dashboard">
        <el-icon><DataBoard /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>

      <el-sub-menu index="content">
        <template #title>
          <el-icon><Document /></el-icon>
          <span>内容管理</span>
        </template>
        <el-menu-item index="/post">帖子管理</el-menu-item>
        <el-menu-item index="/evaluation">评价管理</el-menu-item>
        <el-menu-item index="/topic">话题管理</el-menu-item>
        <el-menu-item index="/activity">活动管理</el-menu-item>
        <el-menu-item index="/tag">标签管理</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="facility">
        <template #title>
          <el-icon><OfficeBuilding /></el-icon>
          <span>设施管理</span>
        </template>
        <el-menu-item index="/facility">设施列表</el-menu-item>
        <el-menu-item index="/category">分类管理</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="user">
        <template #title>
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </template>
        <el-menu-item index="/user">用户列表</el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/banner">
        <el-icon><Picture /></el-icon>
        <span>轮播图管理</span>
      </el-menu-item>

      <el-menu-item index="/statistics">
        <el-icon><TrendCharts /></el-icon>
        <span>数据统计</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  DataBoard,
  Document,
  OfficeBuilding,
  User,
  Picture,
  TrendCharts
} from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar {
  width: 220px;
  height: 100vh;
  background-color: #ffffff;
  border-right: 1px solid #e6e6e6;
  position: fixed;
  left: 0;
  top: 0;
  overflow-y: auto;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #e6e6e6;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.sidebar-menu {
  border-right: none;
}
</style>
