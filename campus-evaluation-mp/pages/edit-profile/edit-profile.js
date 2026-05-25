const { request } = require('../../utils/request');
const { getUserInfo } = require('../../utils/auth');

Page({
  data: {
    form: {
      nickname: '',
      avatarUrl: '',
      signature: '',
      gender: 0
    },
    saving: false
  },

  onLoad() {
    const userInfo = getUserInfo();
    this.setData({
      form: {
        nickname: userInfo.nickname || '',
        avatarUrl: userInfo.avatarUrl || '',
        signature: userInfo.signature || '',
        gender: userInfo.gender || 0
      }
    });
  },

  onNicknameInput(e) {
    this.setData({ 'form.nickname': e.detail.value });
  },

  onSignatureInput(e) {
    this.setData({ 'form.signature': e.detail.value });
  },

  onGenderChange(e) {
    this.setData({ 'form.gender': parseInt(e.detail.value) });
  },

  chooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.setData({
          'form.avatarUrl': res.tempFiles[0].tempFilePath
        });
      }
    });
  },

  saveProfile() {
    const { form } = this.data;

    if (!form.nickname || form.nickname.trim() === '') {
      wx.showToast({ title: '昵称不能为空', icon: 'none' });
      return;
    }

    this.setData({ saving: true });

    // 如果头像选择了本地图片，需要先上传
    if (form.avatarUrl && !form.avatarUrl.startsWith('http')) {
      this.uploadAvatarAndSave();
    } else {
      this.updateProfile();
    }
  },

  uploadAvatarAndSave() {
    const token = wx.getStorageSync('token');
    
    wx.uploadFile({
      url: 'http://localhost:8080/api/user/avatar',
      filePath: this.data.form.avatarUrl,
      name: 'file',
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            this.data.form.avatarUrl = data.data.avatarUrl;
            this.updateProfile();
          } else {
            wx.hideLoading();
            this.setData({ saving: false });
            wx.showToast({ title: data.msg || '头像上传失败', icon: 'none' });
          }
        } catch (e) {
          wx.hideLoading();
          this.setData({ saving: false });
          wx.showToast({ title: '上传失败', icon: 'none' });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        this.setData({ saving: false });
        console.error('上传头像失败:', err);
        wx.showToast({ title: '上传失败', icon: 'none' });
      }
    });
  },

  updateProfile() {
    const { form } = this.data;

    request({
      url: '/api/user/profile',
      method: 'PUT',
      data: {
        nickname: form.nickname,
        avatarUrl: form.avatarUrl,
        signature: form.signature,
        gender: form.gender
      }
    }).then(() => {
      // 更新本地存储
      const userInfo = getUserInfo();
      userInfo.nickname = form.nickname;
      userInfo.avatarUrl = form.avatarUrl;
      userInfo.signature = form.signature;
      userInfo.gender = form.gender;
      wx.setStorageSync('userInfo', userInfo);

      wx.showToast({ title: '保存成功', icon: 'success' });

      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }).catch((err) => {
      console.error('保存失败:', err);
      wx.showToast({ title: '保存失败', icon: 'none' });
    }).finally(() => {
      this.setData({ saving: false });
    });
  }
});
