<template>
  <div class="app-container">
    <template v-if="isLoggedIn">
      <Sidebar />
      <div class="main-content">
        <Header />
        <div class="page-content">
          <router-view />
        </div>
      </div>
    </template>
    <template v-else>
      <router-view />
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from './stores/user'
import Sidebar from './components/Sidebar.vue'
import Header from './components/Header.vue'

const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.token)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f7fa;
}

.app-container {
  min-height: 100vh;
}

.main-content {
  margin-left: 220px;
  padding-top: 60px;
  min-height: 100vh;
}

.page-content {
  padding: 20px;
}

/* 用户信息单元格样式 */
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

</style>
