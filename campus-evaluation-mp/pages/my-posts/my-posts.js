const { request, getImageUrl } = require('../../utils/request');
const { isLogin } = require('../../utils/auth');

Page({
  data: {
    publishedPosts: [],
    loading: false,
    page: 1,
    hasMore: true
  },

  onLoad() {
    if (!isLogin()) {
      wx.showModal({
        title: '提示',
        content: '请先登录',
        showCancel: false,
        success: () => {
          wx.navigateBack();
        }
      });
      return;
    }
    
    this.loadData();
  },

  onShow() {
    const needRefresh = wx.getStorageSync('needRefresh');
    if (needRefresh) {
      wx.removeStorageSync('needRefresh');
      this.setData({ page: 1 });
      this.setData({ publishedPosts: [] });
      this.loadData();
    }
  },

  onPullDownRefresh() {
    this.setData({ page: 1 });
    this.setData({ publishedPosts: [] });
    this.loadData().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 });
      this.loadData();
    }
  },

  loadData() {
    if (this.data.loading) return;
    
    this.setData({ loading: true });

    return request({
      url: '/api/post/my-posts',
      data: { 
        page: this.data.page,
        size: 10
      }
    }).then((data) => {
      console.log('我的帖子后端返回数据:', data);
      
      const newList = (data.list || []).map(item => {
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
          images: imagesArray,
          coverImage: item.coverImage || (imagesArray.length > 0 ? imagesArray[0] : null),
          title: item.title || item.content || '',
          tags: Array.isArray(item.tags) ? item.tags : (typeof item.tags === 'string' ? JSON.parse(item.tags) : [])
        };
      });

      console.log('处理后的帖子列表:', newList);

      const posts = this.data.page === 1 
        ? newList 
        : [...this.data.publishedPosts, ...newList];

      this.setData({ 
        publishedPosts: posts,
        hasMore: newList.length === 10,
        loading: false
      });
      
    }).catch((err) => {
      console.error('加载我的帖子数据失败:', err);
      this.setData({ 
        publishedPosts: this.data.page === 1 ? [] : this.data.publishedPosts,
        loading: false
      });
    });
  },

  goToPostDetail(e) {
    const postId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/post-detail/post-detail?postId=${postId}`
    });
  },

  goToPublish() {
    wx.navigateTo({
      url: '/pages/publish/publish'
    });
  },

  deletePost(e) {
    const postId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '确认删除',
      content: '删除后无法恢复，确定要删除吗？',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '删除中...' });
          
          try {
            await request({
              url: `/api/post/${postId}`,
              method: 'DELETE'
            });
            
            wx.hideLoading();
            wx.showToast({ title: '删除成功', icon: 'success' });
            
            this.setData({
              page: 1,
              publishedPosts: []
            });
            this.loadData();
            
          } catch (err) {
            wx.hideLoading();
            console.error('删除失败:', err);
            wx.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      }
    });
  }
});
