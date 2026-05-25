// pages/category/category.js
const { request } = require('../../utils/request');
const { convertIcon } = require('../../utils/constants');

Page({
  data: {
    categories: [],
    showDialog: false,
    isEdit: false,
    editId: null,
    form: {
      name: '',
      iconUrl: '',
      sortOrder: 0,
      status: 1
    },
    // 可选图标列表（使用Emoji）
    iconOptions: [
      { name: '', label: '食堂' },
      { name: '📖', label: '图书馆' },
      { name: '🏫', label: '教学楼' },
      { name: '🏀', label: '体育馆' },
      { name: '🏥', label: '医务室' },
      { name: '', label: '超市' },
      { name: '☕', label: '咖啡厅' },
      { name: '🏢', label: '办公楼' },
      { name: '📦', label: '快递点' },
      { name: '🎭', label: '活动中心' },
      { name: '🏠', label: '宿舍' },
      { name: '', label: '实验室' },
      { name: '🎨', label: '艺术楼' },
      { name: '💻', label: '机房' },
      { name: '', label: '音乐厅' }
    ]
  },

  onLoad() {
    this.loadCategories();
  },

  onShow() {
    this.loadCategories();
  },

  // 加载分类列表
  loadCategories() {
    wx.showLoading({ title: '加载中...' });
    request({
      url: '/api/category/admin/list'
    }).then((data) => {
      // 转换图标显示
      const categories = (data || []).map(item => ({
        ...item,
        displayIcon: convertIcon(item.iconUrl)
      }));
      wx.hideLoading();
      this.setData({ categories });
    }).catch((err) => {
      wx.hideLoading();
      console.error('加载分类失败:', err);
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
        name: '',
        iconUrl: '',
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
      editId: item.categoryId,
      form: {
        name: item.name || '',
        iconUrl: item.iconUrl || '',
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

  // 选择图标
  selectIcon(e) {
    const icon = e.currentTarget.dataset.icon;
    this.setData({ 'form.iconUrl': icon });
  },

  // 表单输入
  onNameInput(e) {
    this.setData({ 'form.name': e.detail.value });
  },

  onSortOrderInput(e) {
    this.setData({ 'form.sortOrder': parseInt(e.detail.value) || 0 });
  },

  onStatusChange(e) {
    this.setData({ 'form.status': parseInt(e.detail.value) });
  },

  // 保存分类
  saveCategory() {
    const { form, isEdit, editId } = this.data;

    if (!form.name || !form.name.trim()) {
      wx.showToast({
        title: '请输入分类名称',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: isEdit ? '保存中...' : '添加中...' });

    const url = isEdit ? '/api/category/update' : '/api/category/add';
    const method = isEdit ? 'PUT' : 'POST';
    const data = isEdit ? { ...form, categoryId: editId } : form;

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
      this.loadCategories();
    }).catch((err) => {
      wx.hideLoading();
      wx.showToast({
        title: err.msg || '操作失败',
        icon: 'none'
      });
    });
  },

  // 切换状态
  toggleStatus(e) {
    const item = e.currentTarget.dataset.item;
    const newStatus = item.status === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? '启用' : '禁用';

    wx.showModal({
      title: '确认操作',
      content: `确定要${actionText}这个分类吗？`,
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: `${actionText}中...` });
          
          request({
            url: `/api/category/status/${item.categoryId}`,
            method: 'PUT',
            data: { status: newStatus }
          }).then(() => {
            wx.hideLoading();
            wx.showToast({
              title: `${actionText}成功`,
              icon: 'success'
            });
            this.loadCategories();
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

  // 删除分类
  deleteCategory(e) {
    const item = e.currentTarget.dataset.item;

    wx.showModal({
      title: '确认删除',
      content: `确定要删除"${item.name}"吗？删除后无法恢复！`,
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '删除中...' });
          
          request({
            url: `/api/category/delete/${item.categoryId}`,
            method: 'DELETE'
          }).then(() => {
            wx.hideLoading();
            wx.showToast({
              title: '删除成功',
              icon: 'success'
            });
            this.loadCategories();
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
