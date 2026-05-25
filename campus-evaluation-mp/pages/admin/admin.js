// pages/admin/admin.js
const { request } = require('../../utils/request');
const { getUserInfo, logout } = require('../../utils/auth');

Page({
  data: {
    userInfo: {},
    statistics: {
      categoryCount: 0,
      facilityCount: 0,
      evaluationCount: 0,
      userCount: 0
    }
  },

  onLoad() {
    this.setData({
      userInfo: getUserInfo()
    });
    this.loadStatistics();
  },

  onShow() {
    this.loadStatistics();
  },

  // 加载统计数据
  loadStatistics() {
    request({
      url: '/api/statistics/admin/overview'
    }).then((data) => {
      if (data) {
        this.setData({ statistics: data });
      }
    }).catch((err) => {
      console.error('加载统计数据失败:', err);
    });
  },

  // 分类管理
  goCategoryManage() {
    wx.navigateTo({
      url: '/pages/category/category'
    });
  },

  // 设施管理
  goFacilityManage() {
    wx.navigateTo({
      url: '/pages/add-facility/add-facility'
    });
  },

  // 评价管理
  goEvaluationManage() {
    wx.navigateTo({
      url: '/pages/admin/evaluation-manage'
    });
  },
  
  // 轮播图管理
  goBannerManage() {
    wx.navigateTo({
      url: '/pages/admin/banner-manage'
    });
  },

  // 数据统计
  goStatistics() {
    wx.navigateTo({
      url: '/pages/admin/statistics'
    });
  },

  // 切换到用户视角
  switchToUserView() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },

  // 退出登录
  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          logout();
        }
      }
    });
  }
});
