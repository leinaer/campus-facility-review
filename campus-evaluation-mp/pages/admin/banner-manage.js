// pages/admin/banner-manage.js
const { request } = require('../../utils/request');

Page({
  data: {
    bannerList: [],
    loading: false,
    showDialog: false,
    isEdit: false,
    editId: null,
    form: {
      title: '',
      imageUrl: '',
      linkUrl: '',
      sortOrder: 0,
      status: 1
    }
  },

  onLoad() {
    this.loadBannerList();
  },

  onShow() {
    this.loadBannerList();
  },

  // 加载Banner列表
  loadBannerList() {
    this.setData({ loading: true });
    
    request({
      url: '/api/banner/admin/list'
    }).then((data) => {
      this.setData({
        bannerList: data || [],
        loading: false
      });
    }).catch((err) => {
      console.error('加载Banner列表失败:', err);
      this.setData({ loading: false });
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    });
  },

  // 显示添加对话框
  showAddDialog() {
    this.setData({
      showDialog: true,
      isEdit: false,
      editId: null,
      form: {
        title: '',
        imageUrl: '',
        linkUrl: '',
        sortOrder: 0,
        status: 1
      }
    });
  },

  // 显示编辑对话框
  showEditDialog(e) {
    const item = e.currentTarget.dataset.item;
    this.setData({
      showDialog: true,
      isEdit: true,
      editId: item.bannerId,
      form: {
        title: item.title || '',
        imageUrl: item.imageUrl || '',
        linkUrl: item.linkUrl || '',
        sortOrder: item.sortOrder || 0,
        status: item.status
      }
    });
  },

  // 关闭对话框
  hideDialog() {
    this.setData({ showDialog: false });
  },

  // 阻止事件冒泡
  stopPropagation() {
    return false;
  },

  // 表单输入
  onTitleInput(e) {
    this.setData({ 'form.title': e.detail.value });
  },

  onImageUrlInput(e) {
    this.setData({ 'form.imageUrl': e.detail.value });
  },

  onLinkUrlInput(e) {
    this.setData({ 'form.linkUrl': e.detail.value });
  },

  onSortOrderInput(e) {
    this.setData({ 'form.sortOrder': parseInt(e.detail.value) || 0 });
  },

  setStatus(e) {
    const status = parseInt(e.currentTarget.dataset.status);
    this.setData({ 'form.status': status });
  },

  // 使用示例图片
  useExampleImage(e) {
    const url = e.currentTarget.dataset.url;
    this.setData({ 'form.imageUrl': url });
  },

  // 图片加载失败
  onImageError() {
    wx.showToast({
      title: '图片加载失败，请检查URL',
      icon: 'none',
      duration: 2000
    });
  },

  // 提交表单
  submitForm() {
    const { form, isEdit, editId } = this.data;

    // 验证
    if (!form.imageUrl || !form.imageUrl.trim()) {
      wx.showToast({
        title: '请输入图片URL',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: isEdit ? '保存中...' : '添加中...' });

    const url = isEdit ? '/api/banner/update' : '/api/banner/add';
    const method = isEdit ? 'PUT' : 'POST';
    
    const data = isEdit 
      ? { ...form, bannerId: editId }
      : form;

    request({
      url,
      method,
      data
    }).then(() => {
      wx.hideLoading();
      wx.showToast({
        title: isEdit ? '更新成功' : '添加成功',
        icon: 'success'
      });
      this.setData({ showDialog: false });
      this.loadBannerList();
    }).catch((err) => {
      wx.hideLoading();
      wx.showToast({
        title: err.msg || '操作失败',
        icon: 'none'
      });
    });
  },

  // 切换上下架状态
  toggleStatus(e) {
    const bannerId = e.currentTarget.dataset.id;
    const currentStatus = e.currentTarget.dataset.status;
    const newStatus = currentStatus === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? '上架' : '下架';

    wx.showModal({
      title: '确认操作',
      content: `确定要${actionText}这个轮播图吗？`,
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: `${actionText}中...` });
          
          request({
            url: `/api/banner/status/${bannerId}`,
            method: 'PUT',
            data: { status: newStatus }
          }).then(() => {
            wx.hideLoading();
            wx.showToast({
              title: `${actionText}成功`,
              icon: 'success'
            });
            this.loadBannerList();
          }).catch((err) => {
            wx.hideLoading();
            wx.showToast({
              title: err.msg || `${actionText}失败`,
              icon: 'none'
            });
          });
        }
      }
    });
  },

  // 删除Banner
  deleteBanner(e) {
    const bannerId = e.currentTarget.dataset.id;

    wx.showModal({
      title: '确认删除',
      content: '删除后无法恢复，确定要删除这个轮播图吗？',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '删除中...' });
          
          request({
            url: `/api/banner/delete/${bannerId}`,
            method: 'DELETE'
          }).then(() => {
            wx.hideLoading();
            wx.showToast({
              title: '删除成功',
              icon: 'success'
            });
            this.loadBannerList();
          }).catch((err) => {
            wx.hideLoading();
            wx.showToast({
              title: err.msg || '删除失败',
              icon: 'none'
            });
          });
        }
      }
    });
  }
});
