// pages/notifications/notifications.js
const { request } = require('../../utils/request');

Page({
  data: {
    notifications: [],
    unreadCount: 0,
    page: 1,
    size: 20,
    hasMore: true,
    loading: false
  },

  onLoad() {
    this.loadNotifications();
    this.loadUnreadCount();
  },

  onShow() {
    this.loadUnreadCount();
  },

  loadNotifications() {
    if (this.data.loading) return;
    
    this.setData({ loading: true });
    
    request({
      url: '/api/notification/list',
      data: {
        page: this.data.page,
        size: this.data.size
      }
    }).then((data) => {
      const newList = data.list || [];
      const notifications = this.data.page === 1 
        ? newList 
        : [...this.data.notifications, ...newList];

      this.setData({
        notifications,
        hasMore: newList.length === this.data.size,
        loading: false
      });
    }).catch((err) => {
      console.error('加载通知失败:', err);
      this.setData({ loading: false });
    });
  },

  loadUnreadCount() {
    request({
      url: '/api/notification/unread-count'
    }).then((count) => {
      this.setData({ unreadCount: count });
      
      // 更新tabBar角标
      if (count > 0) {
        wx.setTabBarBadge({
          index: 3,
          text: count > 99 ? '99+' : String(count)
        });
      } else {
        wx.removeTabBarBadge({ index: 3 });
      }
    }).catch((err) => {
      console.error('加载未读数失败:', err);
    });
  },

  loadMore() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({ page: this.data.page + 1 });
      this.loadNotifications();
    }
  },

  onNotificationTap(e) {
    const { id, type, relatedId, relatedType } = e.currentTarget.dataset;
    
    // 标记为已读
    this.markAsRead(id);
    
    // 跳转到相关页面
    if (relatedType === 'EVALUATION') {
      wx.navigateTo({
        url: `/pages/facility-detail/facility-detail?evaluationId=${relatedId}`
      });
    } else if (relatedType === 'POST') {
      wx.navigateTo({
        url: `/pages/post-detail/post-detail?postId=${relatedId}`
      });
    }
  },

  markAsRead(notificationId) {
    request({
      url: `/api/notification/mark-read/${notificationId}`,
      method: 'POST'
    }).then(() => {
      const notifications = this.data.notifications.map(n => {
        if (n.notificationId === notificationId) {
          return { ...n, isRead: 1 };
        }
        return n;
      });
      
      this.setData({ notifications });
      this.loadUnreadCount();
    }).catch((err) => {
      console.error('标记已读失败:', err);
    });
  },

  markAllAsRead() {
    wx.showLoading({ title: '处理中...' });
    
    request({
      url: '/api/notification/mark-all-read',
      method: 'POST'
    }).then(() => {
      wx.hideLoading();
      wx.showToast({ title: '已全部标记为已读', icon: 'success' });
      
      const notifications = this.data.notifications.map(n => ({
        ...n,
        isRead: 1
      }));
      
      this.setData({ notifications, unreadCount: 0 });
      wx.removeTabBarBadge({ index: 3 });
    }).catch((err) => {
      wx.hideLoading();
      console.error('全部已读失败:', err);
    });
  },

  deleteNotification(e) {
    const notificationId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '提示',
      content: '确定删除这条通知吗？',
      success: (res) => {
        if (res.confirm) {
          request({
            url: `/api/notification/${notificationId}`,
            method: 'DELETE'
          }).then(() => {
            wx.showToast({ title: '删除成功', icon: 'success' });
            
            const notifications = this.data.notifications.filter(
              n => n.notificationId !== notificationId
            );
            
            this.setData({ notifications });
            this.loadUnreadCount();
          }).catch((err) => {
            console.error('删除失败:', err);
          });
        }
      }
    });
  },

  getIconByType(type) {
    const icons = {
      'LIKE': '❤️',
      'COMMENT': '💬',
      'REPLY': '↩️',
      'MENTION': '@',
      'OFFICIAL_REPLY': '📢',
      'SYSTEM': '🔔'
    };
    return icons[type] || '🔔';
  },

  formatTime(timeStr) {
    if (!timeStr) return '';
    
    const now = new Date();
    const time = new Date(timeStr);
    const diff = now - time;
    
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);
    
    if (minutes < 1) return '刚刚';
    if (minutes < 60) return `${minutes}分钟前`;
    if (hours < 24) return `${hours}小时前`;
    if (days < 7) return `${days}天前`;
    
    return time.toLocaleDateString();
  }
});
