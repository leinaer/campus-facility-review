// app.js
App({
  onLaunch() {
    // 检查登录状态
    this.checkLoginStatus();
  },

  globalData: {
    userInfo: null,
    token: null
  },

  // 检查登录状态
  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    
    if (token && userInfo) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
    }
  },

  // 更新全局数据
  updateGlobalData(token, userInfo) {
    this.globalData.token = token;
    this.globalData.userInfo = userInfo;
  }
});
