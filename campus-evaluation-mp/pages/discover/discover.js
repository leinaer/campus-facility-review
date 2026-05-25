// pages/discover/discover.js
const { request, getImageUrl } = require('../../utils/request');

Page({
  data: {
    posts: [],
    leftColumn: [],
    rightColumn: [],
    loading: false,
    sortBy: 'hot',
    page: 1,
    hasMore: true
  },

  onLoad() {
    this.loadData();
  },

  onShow() {
    const needRefresh = wx.getStorageSync('needRefresh');
    if (needRefresh) {
      wx.removeStorageSync('needRefresh');
    }
    
    this.setData({ page: 1 });
    this.setData({ posts: [], leftColumn: [], rightColumn: [] });
    this.loadData();
  },

  onPullDownRefresh() {
    this.setData({ page: 1 });
    this.setData({ posts: [], leftColumn: [], rightColumn: [] });
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

  setSort(e) {
    const sortBy = e.currentTarget.dataset.sort;
    if (this.data.sortBy === sortBy) return;
    
    this.setData({ sortBy });
    this.setData({ page: 1 });
    this.setData({ posts: [], leftColumn: [], rightColumn: [] });
    this.loadData();
  },

  loadData() {
    if (this.data.loading) return;
    
    this.setData({ loading: true });
    
    const sortBy = this.data.sortBy === 'hot' ? 'hot' : 'latest';

    return request({
      url: '/api/post/discover',
      data: { 
        sortBy: sortBy,
        page: this.data.page,
        size: 10
      }
    }).then((data) => {
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

      const posts = this.data.page === 1 
        ? newList 
        : [...this.data.posts, ...newList];

      // 直接计算分列，一次 setData
      const leftColumn = [];
      const rightColumn = [];
      
      posts.forEach((post, index) => {
        if (index % 2 === 0) {
          leftColumn.push(post);
        } else {
          rightColumn.push(post);
        }
      });

      // 一次性更新所有数据，避免多次渲染
      this.setData({ 
        posts,
        leftColumn,
        rightColumn,
        hasMore: newList.length === 10,
        loading: false
      });
      
    }).catch((err) => {
      console.error('加载发现页数据失败:', err);
      this.setData({ loading: false });
    });
  },

  goToPostDetail(e) {
    const postId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/post-detail/post-detail?postId=${postId}`
    });
  },

  goToSearch() {
    wx.navigateTo({
      url: '/pages/search/search?type=post'
    });
  },
  
  async onLike(e) {
    const postId = e.currentTarget.dataset.id;
    const index = this.data.posts.findIndex(p => p.postId === postId);
    if (index === -1) return;

    try {
      const result = await request({
        url: `/api/post/like/toggle/${postId}`,
        method: 'POST'
      });

      const isLiked = result;
      const posts = [...this.data.posts];
      posts[index].isLiked = isLiked;
      posts[index].likeCount = isLiked ? (posts[index].likeCount || 0) + 1 : Math.max(0, (posts[index].likeCount || 0) - 1);
      
      // 局部更新，只更新变化的数据
      this.setData({
        [`posts[${index}].isLiked`]: isLiked,
        [`posts[${index}].likeCount`]: posts[index].likeCount
      });
      
      // 异步更新分列数据
      setTimeout(() => {
        const leftColumn = [];
        const rightColumn = [];
        this.data.posts.forEach((post, idx) => {
          if (idx % 2 === 0) {
            leftColumn.push(post);
          } else {
            rightColumn.push(post);
          }
        });
        this.setData({ leftColumn, rightColumn });
      }, 0);
      
      wx.vibrateShort({ type: 'light' });
    } catch (err) {
      console.error('点赞失败:', err);
    }
  }

});
