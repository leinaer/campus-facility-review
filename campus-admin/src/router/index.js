import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '仪表盘' }
  },
  {
    path: '/post',
    name: 'PostManage',
    component: () => import('../views/PostManage.vue'),
    meta: { title: '帖子管理' }
  },
  {
    path: '/evaluation',
    name: 'EvaluationManage',
    component: () => import('../views/EvaluationManage.vue'),
    meta: { title: '评价管理' }
  },
  {
    path: '/activity',
    name: 'ActivityManage',
    component: () => import('../views/ActivityManage.vue'),
    meta: { title: '活动管理' }
  },
  {
    path: '/topic',
    name: 'TopicManage',
    component: () => import('../views/TopicManage.vue'),
    meta: { title: '话题管理' }
  },
  {
    path: '/tag',
    name: 'TagManage',
    component: () => import('../views/TagManage.vue'),
    meta: { title: '标签管理' }
  },
  {
    path: '/banner',
    name: 'BannerManage',
    component: () => import('../views/BannerManage.vue'),
    meta: { title: '轮播图管理' }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('../views/Statistics.vue'),
    meta: { title: '数据统计' }
  },
  {
    path: '/user',
    name: 'UserManage',
    component: () => import('../views/UserManage.vue'),
    meta: { title: '用户管理' }
  },
  {
    path: '/facility',
    name: 'FacilityManage',
    component: () => import('../views/FacilityManage.vue'),
    meta: { title: '设施管理' }
  },
  {
    path: '/category',
    name: 'CategoryManage',
    component: () => import('../views/CategoryManage.vue'),
    meta: { title: '分类管理' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
