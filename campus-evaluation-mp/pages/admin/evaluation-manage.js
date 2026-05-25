// pages/admin/evaluation-manage.js
const { request, getImageUrl } = require('../../utils/request');

Page({
  data: {
    evaluations: [],
    facilities: [],
    loading: true
  },

  onLoad() {
    this.loadEvaluations();
    this.loadFacilities();
  },

  onShow() {
    this.loadEvaluations();
  },

  // 加载评价列表
  loadEvaluations() {
    this.setData({ loading: true });
    request({ url: '/api/evaluation/admin/list' }).then((data) => {
      this.setData({ 
        evaluations: (data.records || []).map(item => ({
          ...item,
          images: this.parseImages(item.images)
        })),
        loading: false 
      });
    }).catch((err) => {
      console.error('加载评价失败:', err);
      this.setData({ evaluations: [], loading: false });
    });
  },

  // 加载设施列表
  loadFacilities() {
    request({ url: '/api/facility/admin/list' }).then((data) => {
      this.setData({ facilities: data || [] });
    }).catch((err) => {
      console.error('加载设施失败:', err);
    });
  },

  // 获取设施名称
  getFacilityName(facilityId) {
    const facility = this.data.facilities.find(f => f.facilityId === facilityId);
    return facility ? facility.name : '未知设施';
  },

  // 解析图片 JSON
  parseImages(imagesStr) {
    if (!imagesStr) return [];
    try {
      const images = JSON.parse(imagesStr);
      return Array.isArray(images) ? images.map(img => getImageUrl(img)) : [];
    } catch (e) {
      return [];
    }
  },

  // 解析标签 JSON
  parseTags(tagsStr) {
    if (!tagsStr) return [];
    try {
      return JSON.parse(tagsStr);
    } catch (e) {
      return [];
    }
  },

  // 删除评价
  deleteEvaluation(e) {
    const item = e.currentTarget.dataset.item;

    wx.showModal({
      title: '确认删除',
      content: `确定要删除这条评价吗？此操作不可恢复！`,
      confirmText: '删除',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '删除中...' });
          request({
            url: `/api/evaluation/admin/delete/${item.evaluationId}`,
            method: 'DELETE'
          }).then(() => {
            wx.hideLoading();
            wx.showToast({ title: '删除成功', icon: 'success' });
            this.loadEvaluations();
          }).catch((err) => {
            wx.hideLoading();
            console.error('删除失败:', err);
          });
        }
      }
    });
  }
});
