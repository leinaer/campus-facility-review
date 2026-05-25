// utils/auth.js
const app = getApp();

function isLogin() {
  const token = wx.getStorageSync('token') || app.globalData.token;
  return !!token;
}

function getUserInfo() {
  return wx.getStorageSync('userInfo') || app.globalData.userInfo || {};
}

function isAdmin() {
  const user = getUserInfo();
  return user.role === 'ADMIN';
}

function logout() {
  wx.removeStorageSync('token');
  wx.removeStorageSync('userInfo');
  app.globalData.token = null;
  app.globalData.userInfo = null;
  
  wx.reLaunch({
    url: '/pages/login/login'
  });
}

module.exports = {
  isLogin,
  getUserInfo,
  isAdmin,
  logout
};
