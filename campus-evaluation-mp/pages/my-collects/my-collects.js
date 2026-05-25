const { request, getImageUrl } = require('../../utils/request');
const { isLogin } = require('../../utils/auth');

Page({
  data: {
    activeTab: 'posts',
    collectedPosts: [],
    collectedFacilities: [],
    loading: false
  },

  onLoad() {
    if (!isLogin()) {
      wx.redirectTo({ url: '/pages/login/login' });
      return;
    }
    this.loadData();
  },

  onShow() {
    this.loadData();
  },

  onPullDownRefresh() {
    this.loadData().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (this.data.activeTab === tab) return;
    
    this.setData({ activeTab: tab });
    this.loadData();
  },

  loadData() {
    if (this.data.activeTab === 'posts') {
      return this.loadCollectedPosts();
    } else {
      return this.loadCollectedFacilities();
    }
  },

  loadCollectedPosts() {
    this.setData({ loading: true });
    
    return request({
      url: '/api/collect/my-collects'
    }).then((data) => {
      // 后端返回的是分页对象 {list: [], total: 0, ...}
      const postsArray = data && data.list ? data.list : (Array.isArray(data) ? data : []);
      
      const posts = postsArray.map(item => {
        let imagesArray = [];
        
        if (Array.isArray(item.images)) {
          imagesArray = item.images.filter(img => img && typeof img === 'string' && img.trim() !== '' && img !== 'null');
        } else if (typeof item.images === 'string' && item.images.trim()) {
          try {
            imagesArray = JSON.parse(item.images);
            imagesArray = imagesArray.filter(img => img && typeof img === 'string' && img.trim() !== '' && img !== 'null');
          } catch (e) {
            console.error('解析图片失败:', e);
          }
        }
        
        return {
          ...item,
          images: imagesArray,
          coverImage: item.coverImage || (imagesArray.length > 0 ? imagesArray[0] : null),
          title: item.title || item.content || '',
          tags: Array.isArray(item.tags) ? item.tags : (typeof item.tags === 'string' ? JSON.parse(item.tags) : [])
        };
      });

      this.setData({
        collectedPosts: posts,
        loading: false
      });
    }).catch((err) => {
      console.error('加载收藏帖子失败:', err);
      this.setData({ collectedPosts: [], loading: false });
    });
  },

  loadCollectedFacilities() {
    this.setData({ loading: true });
    
    return request({
      url: '/api/favorite/my-favorites'
    }).then((data) => {
      const facilitiesArray = data && data.list ? data.list : (Array.isArray(data) ? data : []);
      
      const facilities = facilitiesArray.map(item => ({
        ...item,
        coverImage: item.coverImage ? getImageUrl(item.coverImage) : ''
      }));

      this.setData({
        collectedFacilities: facilities,
        loading: false
      });
    }).catch((err) => {
      console.error('加载收藏设施失败:', err);
      this.setData({ collectedFacilities: [], loading: false });
    });
  },

  goToPost(e) {
    const postId = e.currentTarget.dataset.id;
    if (postId) {
      wx.navigateTo({
        url: `/pages/post-detail/post-detail?postId=${postId}`
      });
    }
  },

  goToFacility(e) {
    const facilityId = e.currentTarget.dataset.id;
    if (facilityId) {
      wx.navigateTo({
        url: `/pages/facility-detail/facility-detail?facilityId=${facilityId}`
      });
    }
  },

  cancelCollect(e) {
    const postId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '取消收藏',
      content: '确定要取消收藏这个帖子吗？',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '处理中...' });
          
          request({
            url: `/api/collect/toggle/${postId}`,
            method: 'POST'
          }).then(() => {
            wx.hideLoading();
            wx.showToast({ title: '已取消收藏', icon: 'success' });
            
            this.loadCollectedPosts();
          }).catch(err => {
            wx.hideLoading();
            console.error('取消收藏失败:', err);
            wx.showToast({ title: '操作失败', icon: 'none' });
          });
        }
      }
    });
  },

  cancelFacilityCollect(e) {
    const facilityId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '取消收藏',
      content: '确定要取消收藏这个设施吗？',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '处理中...' });
          
          request({
            url: `/api/favorite/toggle/${facilityId}`,
            method: 'POST'
          }).then(() => {
            wx.hideLoading();
            wx.showToast({ title: '已取消收藏', icon: 'success' });
            
            this.loadCollectedFacilities();
          }).catch(err => {
            wx.hideLoading();
            console.error('取消收藏失败:', err);
            wx.showToast({ title: '操作失败', icon: 'none' });
          });
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
