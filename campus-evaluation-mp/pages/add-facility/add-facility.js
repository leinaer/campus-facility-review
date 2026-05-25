// pages/add-facility/add-facility.js
const { request, BASE_URL, getImageUrl } = require('../../utils/request');

Page({
  data: {
    facilities: [],
    categories: [],
    showEditDialog: false,
    isEdit: false,
    editId: null,
    showCategoryModal: false,
    newCategoryName: '',
    form: {
      name: '',
      categoryId: null,
      categoryIndex: -1,
      campus: '',
      campusIndex: -1,
      location: '',
      coverImage: ''
    },
    campusOptions: ['东校区', '西校区', '南校区', '北校区']
  },

  onLoad() {
    this.loadFacilities();
    this.loadCategories();
  },

  onShow() {
    this.loadFacilities();
  },

  // 加载设施列表
  loadFacilities() {
    wx.showLoading({ title: '加载中...' });
    request({ url: '/api/facility/admin/list' }).then((data) => {
      wx.hideLoading();
      // 处理图片 URL
      const facilities = (data || []).map(item => ({
        ...item,
        coverImage: getImageUrl(item.coverImage)
      }));
      this.setData({ facilities });
    }).catch((err) => {
      wx.hideLoading();
      console.error('加载设施失败:', err);
      this.setData({ facilities: [] });
    });
  },

  // 加载分类列表
  loadCategories() {
    request({ url: '/api/category/list' }).then((data) => {
      this.setData({ categories: data || [] });
    }).catch((err) => {
      console.error('加载分类失败:', err);
    });
  },

  // 获取分类名称
  getCategoryName(categoryId) {
    const category = this.data.categories.find(c => c.categoryId === categoryId);
    return category ? category.name : '未知分类';
  },

  // 显示添加设施对话框
  showAddDialog() {
    this.setData({
      showEditDialog: true,
      isEdit: false,
      editId: null,
      form: {
        name: '',
        categoryId: null,
        categoryIndex: -1,
        campus: '',
        campusIndex: -1,
        location: '',
        coverImage: ''
      }
    });
  },

  // 显示编辑设施对话框
  showEditDialog(e) {
    const item = e.currentTarget.dataset.item;
    const categoryIndex = this.data.categories.findIndex(c => c.categoryId === item.categoryId);
    const campusIndex = this.data.campusOptions.indexOf(item.campus);

    this.setData({
      showEditDialog: true,
      isEdit: true,
      editId: item.facilityId,
      form: {
        name: item.name,
        categoryId: item.categoryId,
        categoryIndex: categoryIndex,
        campus: item.campus || '',
        campusIndex: campusIndex >= 0 ? campusIndex : -1,
        location: item.location || '',
        coverImage: item.coverImage || ''
      }
    });
  },

  // 关闭对话框
  hideDialog() {
    this.setData({ showEditDialog: false });
  },

  // 阻止事件冒泡
  stopPropagation() {
    return false;
  },

  // 表单输入
  onNameInput(e) {
    this.setData({ 'form.name': e.detail.value });
  },

  onLocationInput(e) {
    this.setData({ 'form.location': e.detail.value });
  },

  onCategoryChange(e) {
    const index = parseInt(e.detail.value);
    if (index >= 0 && index < this.data.categories.length) {
      this.setData({
        'form.categoryIndex': index,
        'form.categoryId': this.data.categories[index].categoryId
      });
    }
  },

  onCampusChange(e) {
    const index = parseInt(e.detail.value);
    this.setData({
      'form.campusIndex': index,
      'form.campus': this.data.campusOptions[index]
    });
  },

  // 选择封面图
  chooseCover() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        this.setData({ 'form.coverImage': res.tempFiles[0].tempFilePath });
      }
    });
  },

  // 删除封面图
  deleteCover() {
    this.setData({ 'form.coverImage': '' });
  },

  // 上传图片
  uploadCover() {
    return new Promise((resolve, reject) => {
      if (!this.data.form.coverImage || this.data.form.coverImage.startsWith('http')) {
        resolve(this.data.form.coverImage);
        return;
      }

      const token = wx.getStorageSync('token');
      wx.uploadFile({
        url: `${BASE_URL}/api/upload/image`,
        filePath: this.data.form.coverImage,
        name: 'file',
        formData: { type: 'facility' },
        header: { 'Authorization': `Bearer ${token}` },
        success: (res) => {
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            resolve(data.data);
          } else {
            reject(new Error(data.msg));
          }
        },
        fail: reject
      });
    });
  },

  // 保存设施
  async saveFacility() {
    const { isEdit, editId, form } = this.data;

    if (!form.name || !form.name.trim()) {
      wx.showToast({ title: '请输入设施名称', icon: 'none' });
      return;
    }
    if (!form.categoryId) {
      wx.showToast({ title: '请选择分类', icon: 'none' });
      return;
    }
    if (!form.campus) {
      wx.showToast({ title: '请选择校区', icon: 'none' });
      return;
    }
    if (!form.location || !form.location.trim()) {
      wx.showToast({ title: '请输入位置', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '保存中...' });

    try {
      const coverImageUrl = await this.uploadCover();

      const url = isEdit ? '/api/facility/update' : '/api/facility/add';
      const method = isEdit ? 'PUT' : 'POST';

      const data = {
        ...form,
        coverImage: coverImageUrl
      };

      if (isEdit) {
        data.facilityId = editId;
      }

      await request({ url, method, data });

      wx.hideLoading();
      wx.showToast({ title: isEdit ? '更新成功' : '添加成功', icon: 'success' });
      this.setData({ showEditDialog: false });
      this.loadFacilities();
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || '保存失败', icon: 'none' });
    }
  },

  // 下架/上架设施
  toggleStatus(e) {
    const item = e.currentTarget.dataset.item;
    const newStatus = item.status === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? '上架' : '下架';

    wx.showModal({
      title: `确认${actionText}`,
      content: `确定要${actionText}设施"${item.name}"吗？`,
      confirmText: actionText,
      confirmColor: newStatus === 1 ? '#4caf50' : '#ff9800',
      success: (res) => {
        if (res.confirm) {
          if (newStatus === 0) {
            // 下架
            wx.showLoading({ title: '下架中...' });
            request({
              url: `/api/facility/offline/${item.facilityId}`,
              method: 'PUT'
            }).then(() => {
              wx.hideLoading();
              wx.showToast({ title: '下架成功', icon: 'success' });
              this.loadFacilities();
            }).catch((err) => {
              wx.hideLoading();
              console.error('下架失败:', err);
            });
          } else {
            // 上架（通过更新状态实现）
            wx.showLoading({ title: '上架中...' });
            request({
              url: '/api/facility/update',
              method: 'PUT',
              data: {
                facilityId: item.facilityId,
                status: 1
              }
            }).then(() => {
              wx.hideLoading();
              wx.showToast({ title: '上架成功', icon: 'success' });
              this.loadFacilities();
            }).catch((err) => {
              wx.hideLoading();
              console.error('上架失败:', err);
            });
          }
        }
      }
    });
  },

  // 删除设施
  deleteFacility(e) {
    const item = e.currentTarget.dataset.item;

    wx.showModal({
      title: '确认删除',
      content: `确定要删除设施"${item.name}"吗？此操作不可恢复！`,
      confirmText: '删除',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '删除中...' });
          // 注意：后端可能需要添加删除接口，这里先用下架代替
          request({
            url: `/api/facility/offline/${item.facilityId}`,
            method: 'PUT'
          }).then(() => {
            wx.hideLoading();
            wx.showToast({ title: '已下架', icon: 'success' });
            this.loadFacilities();
          }).catch((err) => {
            wx.hideLoading();
            console.error('操作失败:', err);
          });
        }
      }
    });
  }
});
