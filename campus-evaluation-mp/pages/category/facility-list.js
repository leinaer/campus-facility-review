// pages/category/facility-list.js
const { request, getImageUrl } = require('../../utils/request');

Page({
  data: {
    categoryId: null,
    categoryName: '',
    categoryIcon: '📋',
    facilities: [],
    filteredFacilities: [],
    selectedFilter: 'default',
    campusOptions: [],
    selectedCampusIndex: -1,
    loading: false
  },

  onLoad(options) {
    const categoryId = parseInt(options.categoryId);
    const categoryName = decodeURIComponent(options.categoryName || '分类设施');
    const categoryIcon = decodeURIComponent(options.categoryIcon || '📋');
    
    this.setData({
      categoryId,
      categoryName,
      categoryIcon
    });

    // 设置页面标题
    wx.setNavigationBarTitle({
      title: categoryName
    });

    this.loadFacilities();
  },

  onShow() {
    const needRefresh = wx.getStorageSync('needRefresh');
    if (needRefresh) {
      wx.removeStorageSync('needRefresh');
      this.loadFacilities();
    }
  },



  onPullDownRefresh() {
    this.loadFacilities().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  loadFacilities() {
    this.setData({ loading: true });
    
    return request({
      url: `/api/facility/by-category/${this.data.categoryId}`
    }).then((data) => {
      // 处理图片 URL
      const facilities = (data || []).map(item => ({
        ...item,
        coverImage: getImageUrl(item.coverImage)
      }));
      
      // 提取所有校区选项（去重）
      const campusSet = new Set();
      facilities.forEach(facility => {
        if (facility.campus) {
          campusSet.add(facility.campus);
        }
      });
      const campusOptions = Array.from(campusSet);
      
      this.setData({
        facilities,
        campusOptions,
        loading: false
      });
      
      // 应用当前筛选
      this.applyFilter();
    }).catch((err) => {
      console.error('加载设施失败:', err);
      this.setData({
        facilities: [],
        filteredFacilities: [],
        campusOptions: [],
        loading: false
      });
    });
  },

  // 设置筛选
  setFilter(e) {
    const filter = e.currentTarget.dataset.filter;
    this.setData({ selectedFilter: filter });
    this.applyFilter();
  },

  // 校区选择
  onCampusChange(e) {
    const index = parseInt(e.detail.value);
    this.setData({ selectedCampusIndex: index });
    this.applyFilter();
  },

  // 应用筛选
  applyFilter() {
    const { facilities, selectedFilter, selectedCampusIndex, campusOptions } = this.data;
    let filtered = [...facilities];

    // 先应用校区筛选
    if (selectedCampusIndex >= 0) {
      const selectedCampus = campusOptions[selectedCampusIndex];
      filtered = filtered.filter(facility => facility.campus === selectedCampus);
    }

    // 再应用排序筛选
    switch (selectedFilter) {
      case 'rating_desc':
        filtered.sort((a, b) => (b.rating || 0) - (a.rating || 0));
        break;
      case 'rating_asc':
        filtered.sort((a, b) => (a.rating || 0) - (b.rating || 0));
        break;
      case 'review_desc':
        filtered.sort((a, b) => (b.reviewCount || 0) - (a.reviewCount || 0));
        break;
      case 'review_asc':
        filtered.sort((a, b) => (a.reviewCount || 0) - (b.reviewCount || 0));
        break;
      case 'name_asc':
        filtered.sort((a, b) => (a.name || '').localeCompare(b.name || '', 'zh-CN'));
        break;
      default:
        // 默认按评分和评价数综合排序
        filtered.sort((a, b) => {
          const scoreA = (a.rating || 0) * 0.7 + (a.reviewCount || 0) * 0.3;
          const scoreB = (b.rating || 0) * 0.7 + (b.reviewCount || 0) * 0.3;
          return scoreB - scoreA;
        });
    }

    this.setData({ filteredFacilities: filtered });
  },

  // 刷新
  onRefresh() {
    wx.showLoading({ title: '刷新中...' });
    this.loadFacilities().then(() => {
      wx.hideLoading();
      wx.showToast({ title: '已刷新', icon: 'success', duration: 1000 });
    });
  },

  goToDetail(e) {
    const facilityId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/facility-detail/facility-detail?facilityId=${facilityId}`
    });
  }
});
