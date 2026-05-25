// pages/my-evaluations/my-evaluations.js
const { request, getImageUrl } = require('../../utils/request');
const { isLogin } = require('../../utils/auth');
const { formatRelativeTime } = require('../../utils/formatTime');

Page({
  data: {
    evaluations: [],
    filteredEvaluations: [],
    loading: false,
    filterType: 'all',
    sortBy: 'latest',
    stats: {
      all: 0,
      good: 0,
      medium: 0,
      bad: 0
    }
  },

  onLoad() {
    if (!isLogin()) {
      wx.redirectTo({ url: '/pages/login/login' });
      return;
    }
    this.loadMyEvaluations();
  },

  onShow() {
    this.loadMyEvaluations();
  },

  onPullDownRefresh() {
    this.loadMyEvaluations().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  loadMyEvaluations() {
    this.setData({ loading: true });

    return request({
      url: '/api/evaluation/my-evaluations'
    }).then((data) => {
      console.log('我的评价原始数据:', data);
      
      const evaluations = (Array.isArray(data) ? data : []).map(item => {
        let imagesArray = [];
        
        if (Array.isArray(item.images)) {
          imagesArray = item.images.filter(img => img && typeof img === 'string' && img.trim() !== '');
        } else if (typeof item.images === 'string' && item.images.trim()) {
          try {
            imagesArray = JSON.parse(item.images);
            imagesArray = imagesArray.filter(img => img && typeof img === 'string' && img.trim() !== '');
          } catch (e) {
            console.error('解析图片失败:', e);
          }
        }

        return {
          ...item,
          // 设施自己的封面图（后端返回的 facilityCover）
          facilityOwnCover: item.facilityCover,
          // 评价用户自己上传的图片
          evaluationImages: imagesArray,
          createTime: formatRelativeTime(item.createTime)
        };
      });

      console.log('处理后的评价列表:', evaluations);

      // 统计各分类数量
      const stats = {
        all: evaluations.length,
        good: evaluations.filter(e => e.rating >= 4).length,
        medium: evaluations.filter(e => e.rating === 3).length,
        bad: evaluations.filter(e => e.rating <= 2).length
      };

      this.setData({
        evaluations,
        stats,
        loading: false
      });

      // 应用当前筛选
      this.applyFilter();
    }).catch((err) => {
      console.error('加载我的评价失败:', err);
      this.setData({ evaluations: [], loading: false });
    });
  },

  applyFilter() {
    let filtered = [...this.data.evaluations];

    // 按评分筛选
    if (this.data.filterType === 'good') {
      filtered = filtered.filter(e => e.rating >= 4);
    } else if (this.data.filterType === 'medium') {
      filtered = filtered.filter(e => e.rating === 3);
    } else if (this.data.filterType === 'bad') {
      filtered = filtered.filter(e => e.rating <= 2);
    }

    // 按排序
    if (this.data.sortBy === 'latest') {
      filtered.sort((a, b) => {
        const timeA = new Date(a.createTime).getTime();
        const timeB = new Date(b.createTime).getTime();
        return timeB - timeA;
      });
    } else if (this.data.sortBy === 'highest') {
      filtered.sort((a, b) => b.rating - a.rating);
    } else if (this.data.sortBy === 'lowest') {
      filtered.sort((a, b) => a.rating - b.rating);
    }

    this.setData({ filteredEvaluations: filtered });
  },

  switchFilter(e) {
    const type = e.currentTarget.dataset.type;
    if (this.data.filterType === type) return;

    this.setData({ filterType: type });
    this.applyFilter();
  },

  switchSort(e) {
    const sort = e.currentTarget.dataset.sort;
    if (this.data.sortBy === sort) return;

    this.setData({ sortBy: sort });
    this.applyFilter();
  },

  goToFacility(e) {
    const facilityId = e.currentTarget.dataset.facilityId;
    if (facilityId) {
      wx.navigateTo({
        url: `/pages/facility-detail/facility-detail?facilityId=${facilityId}`
      });
    }
  },

  deleteEvaluation(e) {
    const evaluationId = e.currentTarget.dataset.id;

    wx.showModal({
      title: '确认删除',
      content: '删除后无法恢复，确定要删除吗？',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '删除中...' });

          try {
            await request({
              url: `/api/evaluation/${evaluationId}`,
              method: 'DELETE'
            });

            wx.hideLoading();
            wx.showToast({ title: '删除成功', icon: 'success' });

            this.loadMyEvaluations();
          } catch (err) {
            wx.hideLoading();
            console.error('删除失败:', err);
            wx.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      }
    });
  },

  goToDiscover() {
    wx.switchTab({
      url: '/pages/discover/discover'
    });
  }
});
