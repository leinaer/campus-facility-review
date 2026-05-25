// pages/search/search.js
const { request } = require('../../utils/request');

const HISTORY_KEY = 'search_history';
const MAX_HISTORY = 10;

Page({
  data: {
    keyword: '',
    resultList: [],
    total: 0,
    page: 1,
    size: 10,
    hasMore: false,
    loading: false,
    hasSearched: false,
    searchHistory: [],
    hotKeywords: []
  },

  onLoad(options) {
    this.loadSearchHistory();
    this.loadHotKeywords();
    
    if (options.keyword) {
      this.setData({
        keyword: options.keyword
      });
      this.doSearch();
    }
  },

  onInput(e) {
    this.setData({
      keyword: e.detail.value
    });
  },

  onSearch() {
    if (!this.data.keyword.trim()) {
      wx.showToast({ title: '请输入关键词', icon: 'none' });
      return;
    }
    
    const keyword = this.data.keyword.trim();
    this.saveToHistory(keyword);
    
    this.setData({
      page: 1,
      resultList: [],
      hasSearched: true
    });
    
    this.doSearch();
  },

  doSearch() {
    this.setData({ loading: true });
    
    request({
      url: '/api/facility/search',
      data: {
        keyword: this.data.keyword,
        page: this.data.page,
        size: this.data.size
      }
    }).then((data) => {
      const newList = this.data.page === 1 ? data.list : [...this.data.resultList, ...data.list];
      
      this.setData({
        resultList: newList,
        total: data.total,
        hasMore: data.page < data.pages,
        loading: false
      });
    }).catch(() => {
      this.setData({ loading: false });
    });
  },

  loadMore() {
    if (this.data.loading || !this.data.hasMore) return;
    
    this.setData({
      page: this.data.page + 1
    });
    
    this.doSearch();
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/facility-detail/facility-detail?facilityId=${id}`
    });
  },

  loadSearchHistory() {
    try {
      const history = wx.getStorageSync(HISTORY_KEY) || [];
      this.setData({ searchHistory: history });
    } catch (e) {
      console.error('加载搜索历史失败', e);
    }
  },

  saveToHistory(keyword) {
    try {
      let history = wx.getStorageSync(HISTORY_KEY) || [];
      
      history = history.filter(item => item !== keyword);
      
      history.unshift(keyword);
      
      if (history.length > MAX_HISTORY) {
        history = history.slice(0, MAX_HISTORY);
      }
      
      wx.setStorageSync(HISTORY_KEY, history);
      this.setData({ searchHistory: history });
    } catch (e) {
      console.error('保存搜索历史失败', e);
    }
  },

  clearHistory() {
    wx.showModal({
      title: '提示',
      content: '确定清空搜索历史吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync(HISTORY_KEY);
          this.setData({ searchHistory: [] });
          wx.showToast({ title: '已清空', icon: 'success' });
        }
      }
    });
  },

  onHistoryTap(e) {
    const keyword = e.currentTarget.dataset.keyword;
    this.setData({ keyword });
    this.onSearch();
  },

  onHotKeywordTap(e) {
    const keyword = e.currentTarget.dataset.keyword;
    this.setData({ keyword });
    this.onSearch();
  },

  loadHotKeywords() {
    request({
      url: '/api/facility/hot-search',
      data: { limit: 10 }
    }).then((data) => {
      this.setData({ hotKeywords: data });
    }).catch(() => {
      console.error('加载热门搜索失败');
    });
  },

  onDeleteHistoryItem(e) {
    const index = e.currentTarget.dataset.index;
    let history = this.data.searchHistory;
    history.splice(index, 1);
    wx.setStorageSync(HISTORY_KEY, history);
    this.setData({ searchHistory: history });
  }
});
