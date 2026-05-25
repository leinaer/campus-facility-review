// pages/login/login.js
const { request } = require('../../utils/request');

Page({
  data: {
    loading: false
  },

  onLoad() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.redirectBasedOnRole();
    }
  },

  // 根据角色跳转
  redirectBasedOnRole() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo && userInfo.role === 'ADMIN') {
      // 管理员页面不在 tabBar 中，使用 reLaunch
      wx.reLaunch({ url: '/pages/admin/admin' });
    } else {
      // 普通用户首页在 tabBar 中，使用 switchTab
      wx.switchTab({ url: '/pages/index/index' });
    }
  },

  handleLogin() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          this.doLogin(loginRes.code);
        } else {
          this.setData({ loading: false });
          wx.showToast({ title: '获取登录凭证失败', icon: 'none' });
        }
      },
      fail: () => {
        this.setData({ loading: false });
        wx.showToast({ title: '微信登录失败', icon: 'none' });
      }
    });
  },

  doLogin(code) {
    request({
      url: '/api/auth/login',
      method: 'POST',
      data: { code: code }
    }).then((data) => {
      if (data && data.token) {
        wx.setStorageSync('token', data.token);
        wx.setStorageSync('userInfo', data.user || {});
        
        wx.showToast({ title: '登录成功', icon: 'success' });
        
        setTimeout(() => {
          // 根据角色跳转
          if (data.user && data.user.role === 'ADMIN') {
            // 管理员页面不在 tabBar 中，使用 reLaunch
            wx.reLaunch({ url: '/pages/admin/admin' });
          } else {
            // 普通用户首页在 tabBar 中，使用 switchTab
            wx.switchTab({ url: '/pages/index/index' });
          }
        }, 1000);
      } else {
        throw new Error('登录返回数据异常');
      }
    }).catch((err) => {
      console.error('登录失败:', err);
    }).finally(() => {
      this.setData({ loading: false });
    });
  }
});
