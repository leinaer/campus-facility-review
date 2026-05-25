// utils/request.js
const BASE_URL = 'http://localhost:8080';

// 补全图片完整 URL
function getImageUrl(imagePath) {
  if (!imagePath) return '';
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath;
  }
  if (imagePath.startsWith('/')) {
    return BASE_URL + imagePath;
  }
  return imagePath;
}

// 递归处理数据中的图片 URL
function processImageUrls(data) {
  if (Array.isArray(data)) {
    return data.map(item => processImageUrls(item));
  } else if (data && typeof data === 'object') {
    const result = {};
    for (const key in data) {
      // 处理 images 字段（已经是数组或需要解析的JSON字符串）
      if (key === 'images') {
        if (typeof data[key] === 'string') {
          try {
            const parsed = JSON.parse(data[key]);
            result[key] = Array.isArray(parsed) ? parsed.map(img => getImageUrl(img)) : [];
          } catch (e) {
            result[key] = [];
          }
        } else if (Array.isArray(data[key])) {
          // 如果已经是数组，确保每个URL完整
          result[key] = data[key].map(img => getImageUrl(img));
        } else {
          result[key] = data[key];
        }
      } 
      // 处理 tags 字段
      else if (key === 'tags' && typeof data[key] === 'string') {
        try {
          result[key] = JSON.parse(data[key]);
        } catch (e) {
          result[key] = [];
        }
      } 
      // 处理单个图片URL字段
      else if (key === 'iconUrl' || key === 'icon' || key === 'imageUrl' || key === 'coverImage' || 
               key === 'image' || key === 'photo' || key === 'avatarUrl' || key === 'avatar' || key === 'userAvatar') {
        result[key] = getImageUrl(data[key]);
      } else {
        result[key] = processImageUrls(data[key]);
      }
    }
    return result;
  }
  return data;
}

function request(options) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');

    wx.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      timeout: 10000,
      success: (res) => {
        if (res.data && res.data.code === 200) {
          const processedData = processImageUrls(res.data.data);
          resolve(processedData);
        } else if (res.data && res.data.code === 401) {
          wx.showToast({
            title: '请先登录',
            icon: 'none'
          });
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          
          setTimeout(() => {
            wx.redirectTo({
              url: '/pages/login/login'
            });
          }, 1500);
          reject(res.data);
        } else {
          const msg = (res.data && res.data.msg) || '请求失败';
          wx.showToast({
            title: msg,
            icon: 'none',
            duration: 2000
          });
          reject(res.data);
        }
      },
      fail: (err) => {
        console.error('请求失败:', err);
        wx.showToast({
          title: '网络连接失败，请检查后端是否启动',
          icon: 'none',
          duration: 3000
        });
        reject(err);
      }
    });
  });
}

module.exports = {
  request,
  BASE_URL,
  getImageUrl
};
