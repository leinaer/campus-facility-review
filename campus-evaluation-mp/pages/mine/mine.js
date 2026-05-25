const { request } = require('../../utils/request');
const { getUserInfo, isAdmin, logout } = require('../../utils/auth');

Page({
  data: {
    userInfo: {},
    myEvaluations: [],
    showAdmin: false,
    userLevel: null,
    stats: {
      evaluationCount: 0,
      postCount: 0,
      collectCount: 0,
      likeReceived: 0
    },
    unreadNotificationCount: 0,
    lastLoadTime: 0,
    loading: false
  },
  
  onLoad() {
    this.setData({
      userInfo: getUserInfo(),
      showAdmin: isAdmin()
    });
    this.loadUserData();
  },
  
  onShow() {
    const now = Date.now();
    const needRefresh = wx.getStorageSync('needRefresh');
    
    // 只更新本地缓存的用户信息，不重复请求
    this.setData({
      userInfo: getUserInfo(),
      showAdmin: isAdmin()
    });
    
    // 防抖：10秒内不重复加载（从5秒改为10秒）
    if (needRefresh) {
      wx.removeStorageSync('needRefresh');
      this.loadUserData();
    } else if (now - this.data.lastLoadTime > 10000) {
      this.loadUserData(true);
    }
  },

  onPullDownRefresh() {
    this.loadUserData().then(() => {
      wx.stopPullDownRefresh();
    }).catch(() => {
      wx.stopPullDownRefresh();
    });
  },

  async loadUserData(isQuick = false) {
    if (this.data.loading) return;

    this.setData({ loading: true });

    if (!isQuick) {
      wx.showLoading({ title: '加载中...', mask: true });
    }
    
    try {
      if (isQuick) {
        // 快速刷新：只加载核心数据
        await Promise.all([
          this.loadMyEvaluations(),
          this.loadUnreadNotificationCount()
        ]);
      } else {
        // 完整加载
        await Promise.all([
          this.loadMyEvaluations(),
          this.loadUserStats(),
          this.loadUserLevel(),
          this.loadUnreadNotificationCount()
        ]);
      }
      
      this.setData({ lastLoadTime: Date.now() });
    } catch (err) {
      console.error('加载用户数据失败:', err);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
      if (!isQuick) {
        wx.hideLoading();
      }
    }
  },

  loadUnreadNotificationCount() {
    return request({
      url: '/api/notification/unread-count'
    }).then((count) => {
      this.setData({ unreadNotificationCount: count || 0 });
    }).catch((err) => {
      console.error('加载未读消息数失败:', err);
      this.setData({ unreadNotificationCount: 0 });
    });
  },
  
  loadMyEvaluations() {
    return request({ url: '/api/evaluation/my-evaluations' }).then((data) => {
      const evaluations = Array.isArray(data) ? data : [];
      // 直接截取前3个，避免先 setData 全部再截取
      this.setData({ 
        myEvaluations: evaluations.slice(0, 3),
        'stats.evaluationCount': evaluations.length
      });
    }).catch((err) => {
      console.error('加载我的评价失败:', err);
      this.setData({ myEvaluations: [], 'stats.evaluationCount': 0 });
    });
  },
  
  async loadUserStats() {
    try {
      const stats = await request({
        url: '/api/auth/user/stats',
        method: 'GET'
      });
      
      if (stats && typeof stats === 'object') {
        this.setData({ 
          stats: {
            evaluationCount: stats.evaluationCount || 0,
            postCount: stats.postCount || 0,
            collectCount: stats.collectCount || 0,
            likeReceived: stats.likeReceived || 0
          }
        });
      }
    } catch (err) {
      console.error('加载统计数据失败:', err);
    }
  },
  
  async loadUserLevel() {
    try {
      const level = await request({
        url: '/api/auth/user/level',
        method: 'GET'
      });
      
      if (level && typeof level === 'object') {
        this.setData({ userLevel: level });
      } else {
        this.calculateUserLevel();
      }
    } catch (err) {
      console.error('加载用户等级失败，使用本地计算:', err);
      this.calculateUserLevel();
    }
  },

  calculateUserLevel() {
    const totalScore = this.data.stats.evaluationCount * 10 +
                      this.data.stats.postCount * 15 +
                      this.data.stats.likeReceived * 5;

    let level = 1;
    let title = '新手';

    if (totalScore >= 1000) {
      level = 5;
      title = '达人';
    } else if (totalScore >= 500) {
      level = 4;
      title = '专家';
    } else if (totalScore >= 200) {
      level = 3;
      title = '活跃';
    } else if (totalScore >= 50) {
      level = 2;
      title = '进阶';
    }

    this.setData({
      userLevel: { level, title, score: totalScore }
    });
  },

  goMyEvaluations() {
    wx.navigateTo({
      url: '/pages/my-evaluations/my-evaluations'
    });
  },

  goMyPosts() {
    wx.navigateTo({
      url: '/pages/my-posts/my-posts'
    });
  },
  
  goMyCollects() {
    wx.navigateTo({
      url: '/pages/my-collects/my-collects'
    });
  },
  
  goNotifications() {
    wx.navigateTo({
      url: '/pages/notifications/notifications'
    });
  },
  
  viewEvaluationDetail(e) {
    const evaluationId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/facility-detail/facility-detail?evaluationId=${evaluationId}`
    });
  },
  
  goAdmin() {
    if (isAdmin()) {
      wx.reLaunch({ url: '/pages/admin/admin' });
    }
  },
  
  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          logout();
          wx.reLaunch({ url: '/pages/index/index' });
        }
      }
    });
  },

  // 跳转到编辑资料页面
  goEditProfile() {
    wx.navigateTo({
      url: '/pages/edit-profile/edit-profile'
    });
  },

  // 更换头像
  changeAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        this.uploadAvatar(tempFilePath);
      },
      fail: (err) => {
        console.error('选择图片失败:', err);
      }
    });
  },

  // 上传头像
  uploadAvatar(filePath) {
    wx.showLoading({ title: '上传中...', mask: true });

    const token = wx.getStorageSync('token');

    wx.uploadFile({
      url: 'http://localhost:8080/api/user/avatar',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        wx.hideLoading();

        try {
          const data = JSON.parse(res.data);

          if (data.code === 200) {
            // 更新本地用户信息
            const userInfo = wx.getStorageSync('userInfo') || {};
            userInfo.avatarUrl = data.data || filePath;
            wx.setStorageSync('userInfo', userInfo);

            this.setData({
              'userInfo.avatarUrl': userInfo.avatarUrl
            });

            wx.showToast({
              title: '头像更新成功',
              icon: 'success'
            });

            // 标记需要刷新
            wx.setStorageSync('needRefresh', true);
          } else {
            wx.showToast({
              title: data.msg || '上传失败',
              icon: 'none'
            });
          }
        } catch (err) {
          console.error('解析响应失败:', err);
          wx.showToast({
            title: '上传失败',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('上传失败:', err);
        wx.showToast({
          title: '上传失败，请检查网络',
          icon: 'none'
        });
      }
    });
  }
});
