const { request } = require('../../utils/request');
const { isLogin } = require('../../utils/auth');

Page({
  data: {
    topics: [],
    loading: false,
    searchKeyword: '',
    filterType: 'hot'
  },

  onLoad() {
    this.loadTopics();
  },

  onShow() {
    const needRefresh = wx.getStorageSync('needRefresh');
    if (needRefresh) {
      this.loadTopics();
      wx.removeStorageSync('needRefresh');
    }
  },

  onPullDownRefresh() {
    this.loadTopics().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value });
  },

  onSearch() {
    if (this.data.searchKeyword.trim()) {
      this.loadTopics(this.data.searchKeyword);
    } else {
      this.loadTopics();
    }
  },

  setFilter(e) {
    const filterType = e.currentTarget.dataset.type;
    if (this.data.filterType === filterType) return;
    
    this.setData({ filterType });
    this.loadTopics();
  },

  loadTopics(keyword = '') {
    this.setData({ loading: true });
    
    const params = {
      sortBy: this.data.filterType
    };
    
    if (keyword) {
      params.keyword = keyword;
    }

    return request({
      url: '/api/topic/list',
      data: params
    }).then((data) => {
      this.setData({ 
        topics: data || [],
        loading: false
      });
    }).catch(err => {
      console.error('加载话题列表失败:', err);
      this.setData({ 
        topics: [],
        loading: false
      });
    });
  },

  async onFollow(e) {
    if (!isLogin()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const topicId = e.currentTarget.dataset.id;
    const index = e.currentTarget.dataset.index;
    const topic = this.data.topics[index];
    
    const originalFollowed = topic.isFollowed;
    const originalCount = topic.followCount || 0;

    const topics = [...this.data.topics];
    topics[index] = {
      ...topic,
      isFollowed: !originalFollowed,
      followCount: originalFollowed ? originalCount - 1 : originalCount + 1
    };
    
    this.setData({ topics });

    try {
      const result = await request({
        url: `/api/topic/follow/toggle/${topicId}`,
        method: 'POST'
      });

      if (result.isFollowed !== undefined) {
        const updatedTopics = [...this.data.topics];
        updatedTopics[index] = {
          ...updatedTopics[index],
          isFollowed: result.isFollowed,
          followCount: result.followCount || (result.isFollowed ? originalCount + 1 : originalCount - 1)
        };
        this.setData({ topics: updatedTopics });
      }

      wx.showToast({ 
        title: result.isFollowed ? '关注成功' : '已取消关注', 
        icon: 'success' 
      });
    } catch (err) {
      const rollbackTopics = [...this.data.topics];
      rollbackTopics[index] = {
        ...rollbackTopics[index],
        isFollowed: originalFollowed,
        followCount: originalCount
      };
      this.setData({ topics: rollbackTopics });
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  goToTopic(e) {
    const topicId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/topic/topic?topicId=${topicId}`
    });
  },

  clearSearch() {
    this.setData({ searchKeyword: '' });
    this.loadTopics();
  }
});
