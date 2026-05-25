// pages/index/index.js
const { request } = require('../../utils/request');
const { DEFAULT_CATEGORIES, PAGE_SIZE, convertIcon } = require('../../utils/constants');

Page({
  data: {
    // 搜索
    searchKeyword: '',
    
    // 公告
    notices: [
      '🎉 新学期一食堂改造完成，欢迎体验！',
      '📢 校园美食节开幕啦，参与评价赢奖品~',
      '🔧 体育馆本周六维护，暂停开放'
    ],
    showNotice: true,
    
    // 分类
    categories: [],
    defaultColors: ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DFE6E9', '#A29BFE', '#FAB1A0'],
    
    // Banner
    banners: [],
    
    // 推荐设施
    topFacilities: [],
    cachedFacilities: [],
    currentBatchIndex: 0,
    
    // 排行榜
    rankings: [],
    
    // 评价列表
    latestEvaluations: [],
    currentPage: 1,
    hasMore: true,
    loadingMore: false,
    loading: false,
    
    // 筛选
    showFilter: false,
    filterType: 'featured',

    // 评论相关
    showCommentInput: false,
    commentContent: '',
    canSubmit: false,
    replyToCommentId: null,
    replyToUserId: null,
    replyToUserName: '',
    currentEvaluationId: null,
    
    // 性能优化：添加上次刷新时间
    lastRefreshTime: 0
  },

  onLoad() {
    this.loadInitialData();
  },

  onShow() {
    const now = Date.now();
    const needRefresh = wx.getStorageSync('needRefresh');
    
    if (needRefresh) {
      wx.removeStorageSync('needRefresh');
      this.loadInitialData();
    } else {
      // 防抖：如果距离上次刷新不到5秒，不刷新
      if (now - this.data.lastRefreshTime < 5000) {
        return;
      }
      
      // 只刷新评价列表，使用静默加载（不显示loading）
      this.loadLatestEvaluations(false, true);
      this.setData({ lastRefreshTime: now });
    }
  },


  onPullDownRefresh() {
    this.loadInitialData().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  // 初始化数据加载
  async loadInitialData() {
    wx.showLoading({ title: '加载中...', mask: true });
    
    try {
      await Promise.all([
        this.loadCategories(),
        this.loadBanners(),
        this.loadTopFacilities(),
        this.loadRankings(),
        this.loadLatestEvaluations()
      ]);
    } catch (error) {
      console.error('加载数据失败:', error);
      wx.showToast({
        title: '部分数据加载失败',
        icon: 'none',
        duration: 2000
      });
    } finally {
      wx.hideLoading();
    }
  },

  // 加载分类
  loadCategories() {
    return request({ url: '/api/category/list', cacheTTL: 3600000 }).then((data) => {
      const categories = data.map((item, index) => ({
        ...item,
        icon: convertIcon(item.iconUrl),
        color: item.color || DEFAULT_CATEGORIES[index % DEFAULT_CATEGORIES.length]?.color
      }));
      this.setData({ categories });
    }).catch(() => {
      this.setData({ categories: DEFAULT_CATEGORIES });
    });
  },

  // 加载Banner
  loadBanners() {
    return request({
      url: '/api/banner/list'
    }).then((data) => {
      this.setData({ banners: data || [] });
    }).catch((err) => {
      console.error('加载Banner失败:', err);
      this.setData({ banners: [] });
    });
  },

  // 加载推荐设施
  loadTopFacilities() {
    return request({
      url: '/api/facility/recommended',
      data: { type: 'high', limit: 10 }
    }).then((data) => {
      const facilities = data.map((item, index) => ({
        ...item,
        tag: index === 0 ? '🔥 热门' : index === 1 ? '⭐ 高分' : ''
      }));
      this.setData({ 
        topFacilities: facilities.slice(0, 3),
        cachedFacilities: facilities
      });
    });
  },

  // 换一批推荐
  refreshRecommendations() {
    const { cachedFacilities, currentBatchIndex } = this.data;
    const nextIndex = currentBatchIndex + 3;
    
    if (nextIndex >= cachedFacilities.length) {
      this.loadTopFacilities();
      return;
    }
    
    this.setData({
      topFacilities: cachedFacilities.slice(nextIndex, nextIndex + 3),
      currentBatchIndex: nextIndex
    });
    
    wx.showToast({ title: '已刷新', icon: 'success', duration: 1000 });
  },

  loadRankings() {
    return request({
      url: '/api/statistics/top-facilities',
      data: { limit: 5 }
    }).then((data) => {
      this.setData({ rankings: data });
    });
  },

  // 刷新排行榜
  refreshRankings() {
    wx.showLoading({ title: '刷新中...', mask: true });
    this.loadRankings().then(() => {
      wx.hideLoading();
      wx.showToast({ title: '已刷新', icon: 'success' });
    });
  },

  // 加载最新评价
  loadLatestEvaluations(isLoadMore = false, silent = false) {
    if (this.data.loadingMore && isLoadMore) return;
    
    if (!silent) {
      if (isLoadMore) {
        this.setData({ loadingMore: true });
      } else {
        this.setData({ loading: true });
      }
    }
    
    const page = isLoadMore ? this.data.currentPage + 1 : 1;
    
    return request({
      url: '/api/evaluation/latest',
      data: { 
        limit: PAGE_SIZE, 
        page: page,
        filterType: 'featured'
      }
    }).then((data) => {
      const evaluations = isLoadMore 
        ? [...this.data.latestEvaluations, ...data]
        : data;
      
      this.setData({
        latestEvaluations: evaluations,
        currentPage: page,
        hasMore: data.length === PAGE_SIZE,
        loadingMore: false,
        loading: false
      });
    }).catch(() => {
      this.setData({ loadingMore: false, loading: false });
    });
  },

  // 加载更多评价
  loadMoreEvaluations() {
    if (this.data.hasMore && !this.data.loadingMore) {
      this.loadLatestEvaluations(true);
    }
  },

  // 搜索输入
  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value });
  },

  // 搜索跳转
  onSearch() {
    if (!this.data.searchKeyword.trim()) {
      wx.showToast({ title: '请输入关键词', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: `/pages/search/search?keyword=${this.data.searchKeyword}`
    });
  },

  // 分类点击
  onCategoryTap(e) {
    const categoryId = e.currentTarget.dataset.id;
    const categoryName = e.currentTarget.dataset.name;
    const categoryIcon = e.currentTarget.dataset.icon;
    
    wx.navigateTo({
      url: `/pages/category/facility-list?categoryId=${categoryId}&categoryName=${encodeURIComponent(categoryName)}&categoryIcon=${encodeURIComponent(categoryIcon || '📋')}`
    });
  },
  
  showAllCategories() {
  },

  // Banner点击
  onBannerTap(e) {
    const linkUrl = e.currentTarget.dataset.link;
    if (linkUrl) {
      wx.navigateTo({
        url: linkUrl
      });
    }
  },
  

  // 设施详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/facility-detail/facility-detail?facilityId=${id}`
    });
  },

  // 跳转到发现页
  goToDiscover() {
    wx.switchTab({
      url: '/pages/discover/discover'
    });
  },

  // 公告点击
  onNoticeTap(e) {
    const index = e.currentTarget.dataset.index;
    wx.showToast({ title: `公告${index + 1}`, icon: 'none' });
  },

  // 关闭公告
  closeNotice() {
    this.setData({ showNotice: false });
  },

  // 切换筛选
  toggleFilter() {
    this.setData({ showFilter: !this.data.showFilter });
  },

  // 设置筛选类型
  setFilter(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({ 
      filterType: type, 
      showFilter: false,
      currentPage: 0,
      latestEvaluations: []
    });
    
    wx.showLoading({ title: '加载中...', mask: true });
    this.loadLatestEvaluations().finally(() => {
      wx.hideLoading();
    });
  },

  // 评价点赞事件
  onEvaluationLike(e) {
    const { evaluationId, isLiked } = e.detail;
    const evaluations = this.data.latestEvaluations.map(item => {
      if (item.evaluationId === evaluationId) {
        return {
          ...item,
          isLiked: isLiked,
          likeCount: isLiked ? item.likeCount + 1 : item.likeCount - 1
        };
      }
      return item;
    });
    this.setData({ latestEvaluations: evaluations });
  },

  // 评价评论事件
  onEvaluationComment(e) {
    const { evaluationId, show } = e.detail;
    
    if (show) {
      this.setData({
        currentEvaluationId: evaluationId,
        showCommentInput: true,
        replyToCommentId: null,
        replyToUserId: null,
        replyToUserName: '',
        commentContent: ''
      });
    } else {
      this.setData({
        showCommentInput: false
      });
    }
  },

  // 评价回复事件
  onEvaluationReply(e) {
    const { commentId, userId, userName, evaluationId } = e.detail;
    
    this.setData({
      currentEvaluationId: evaluationId,
      replyToCommentId: commentId,
      replyToUserId: userId,
      replyToUserName: userName,
      showCommentInput: true,
      commentContent: ''
    });
  },

  // 评论输入
  onCommentInput(e) {
    const content = e.detail.value;
    this.setData({ 
      commentContent: content,
      canSubmit: content.trim().length > 0
    });
  },

  // 提交评论
  async submitComment() {
    if (!this.data.commentContent.trim()) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '发布中...' });

    try {
      const data = {
        evaluationId: this.data.currentEvaluationId,
        content: this.data.commentContent.trim()
      };

      if (this.data.replyToCommentId) {
        data.parentId = this.data.replyToCommentId;
        data.replyToUserId = this.data.replyToUserId;
      }

      await request({
        url: '/api/comment/publish',
        method: 'POST',
        data: data
      });

      wx.hideLoading();
      wx.showToast({ title: '评论成功', icon: 'success' });

      this.setData({
        showCommentInput: false,
        commentContent: '',
        replyToCommentId: null,
        replyToUserId: null,
        replyToUserName: ''
      });

      // 本地更新评论数，避免重新请求
      const evaluations = this.data.latestEvaluations.map(item => {
        if (item.evaluationId === this.data.currentEvaluationId) {
          return {
            ...item,
            commentCount: (item.commentCount || 0) + 1
          };
        }
        return item;
      });
      this.setData({ latestEvaluations: evaluations });

    } catch (err) {
      wx.hideLoading();
      console.error('评论失败:', err);
      wx.showToast({ title: '评论失败，请重试', icon: 'none' });
    }
  }
});


