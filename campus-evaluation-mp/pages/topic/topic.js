const { request, getImageUrl } = require('../../utils/request');
const { isLogin } = require('../../utils/auth');

Page({
  data: {
    topicId: null,
    topic: {},
    posts: [],
    loading: true,
    page: 1,
    hasMore: true,
    isFollowed: false
  },

  onLoad(options) {
    const topicId = options.topicId;
    if (!topicId) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }
    
    this.setData({ topicId });
    this.loadTopicInfo();
    this.loadTopicPosts();
  },

  onShow() {
    const needRefresh = wx.getStorageSync('needRefresh');
    if (needRefresh) {
      this.setData({ page: 1, posts: [] });
      this.loadTopicPosts();
      wx.removeStorageSync('needRefresh');
    }
  },

  onPullDownRefresh() {
    this.setData({ page: 1, posts: [] });
    Promise.all([
      this.loadTopicInfo(),
      this.loadTopicPosts()
    ]).then(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 });
      this.loadTopicPosts();
    }
  },

  loadTopicInfo() {
    return request({
      url: `/api/topic/${this.data.topicId}`
    }).then((data) => {
      this.setData({ 
        topic: data,
        isFollowed: data.isFollowed || false,
        loading: false
      });
      
      wx.setNavigationBarTitle({ 
        title: `#${data.name}` 
      });
    }).catch(err => {
      console.error('加载话题信息失败:', err);
      this.setData({ loading: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  loadTopicPosts() {
    if (this.data.loading) return;
    
    this.setData({ loading: true });
    
    request({
      url: `/api/topic/${this.data.topicId}/posts`,
      data: {
        page: this.data.page,
        size: 10
      }
    }).then((data) => {
      const newList = (data.list || []).map(item => ({
        ...item,
        images: this.parseImages(item.images),
        tags: this.parseTags(item.tags)
      }));

      const posts = this.data.page === 1 
        ? newList 
        : [...this.data.posts, ...newList];

      this.setData({ 
        posts,
        hasMore: newList.length === 10,
        loading: false
      });
    }).catch(err => {
      console.error('加载话题帖子失败:', err);
      this.setData({ 
        posts: this.data.page === 1 ? [] : this.data.posts,
        loading: false
      });
    });
  },

  parseImages(imagesStr) {
    if (!imagesStr) return [];
    try {
      const images = JSON.parse(imagesStr);
      return Array.isArray(images) ? images.map(img => getImageUrl(img)) : [];
    } catch (e) {
      return [];
    }
  },

  parseTags(tagsStr) {
    if (!tagsStr) return [];
    try {
      const tags = JSON.parse(tagsStr);
      return Array.isArray(tags) ? tags : [];
    } catch (e) {
      return [];
    }
  },

  async onFollow() {
    if (!isLogin()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const originalFollowed = this.data.isFollowed;
    const originalCount = this.data.topic.followCount || 0;

    this.setData({
      isFollowed: !originalFollowed,
      'topic.followCount': originalFollowed ? originalCount - 1 : originalCount + 1
    });

    try {
      const result = await request({
        url: `/api/topic/follow/toggle/${this.data.topicId}`,
        method: 'POST'
      });

      if (result.isFollowed !== undefined) {
        this.setData({
          isFollowed: result.isFollowed,
          'topic.followCount': result.followCount || (result.isFollowed ? originalCount + 1 : originalCount - 1)
        });
      }

      wx.showToast({ 
        title: result.isFollowed ? '关注成功' : '已取消关注', 
        icon: 'success' 
      });
    } catch (err) {
      this.setData({
        isFollowed: originalFollowed,
        'topic.followCount': originalCount
      });
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  goToPostDetail(e) {
    const postId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/post-detail/post-detail?postId=${postId}`
    });
  },

  goToUserProfile(e) {
    const userId = e.currentTarget.dataset.userid;
    if (userId) {
      wx.navigateTo({
        url: `/pages/user-profile/user-profile?userId=${userId}`
      });
    }
  },

  onShareAppMessage() {
    return {
      title: `#${this.data.topic.name} - ${this.data.topic.description || '精彩话题'}`,
      path: `/pages/topic/topic?topicId=${this.data.topicId}`,
      imageUrl: this.data.topic.icon || ''
    };
  }
});
