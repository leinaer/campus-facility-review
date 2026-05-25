const { request, getImageUrl } = require('../../utils/request');
const { isLogin } = require('../../utils/auth');
const { formatRelativeTime } = require('../../utils/formatTime');

Page({
  data: {
    postId: null,
    post: {},
    comments: [],
    loading: true,
    page: 1,
    hasMore: false,
    
    showCommentInput: false,
    commentContent: '',
    canSubmit: false,
    replyToCommentId: null,
    replyToUserId: null,
    replyToUserName: '',
    
    commentSortBy: 'time'
  },

  onLoad(options) {
    const postId = options.postId;
    if (!postId) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }
    
    this.setData({ postId });
    this.loadPostDetail();
    this.loadComments();
  },

  onShow() {
    const needRefresh = wx.getStorageSync('needRefresh');
    if (needRefresh) {
      this.loadPostDetail();
      this.loadComments();
      wx.removeStorageSync('needRefresh');
    }
  },

  loadPostDetail() {
    this.setData({ loading: true });
    
    request({
      url: `/api/post/detail/${this.data.postId}`
    }).then((data) => {
      console.log('帖子详情后端返回数据:', data);
      console.log('原始图片字段:', data.images);
      
      const post = {
        ...data,
        images: this.parseImages(data.images),
        tags: Array.isArray(data.tags) ? data.tags : this.parseTags(data.tags),
        topics: this.parseTopics(data.topicIds)
      };
      
      console.log('处理后的帖子数据:', post);
      
      this.setData({ 
        post,
        loading: false 
      });
      
      wx.setNavigationBarTitle({ 
        title: `${post.userNickname || '用户'}的动态` 
      });
    }).catch(err => {
      console.error('加载帖子详情失败:', err);
      this.setData({ loading: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  loadComments() {
    wx.showLoading({ title: '加载中...' });

    request({
      url: `/api/post/comment/by-post/${this.data.postId}?sortBy=${this.data.commentSortBy}`
    }).then((data) => {
      let comments = [];

      if (Array.isArray(data)) {
        comments = data;
      } else if (data && Array.isArray(data.list)) {
        comments = data.list;
      } else if (data && Array.isArray(data.data)) {
        comments = data.data;
      }

      const formattedComments = comments.map(comment => {
        const formattedComment = {
          ...comment,
          createTime: formatRelativeTime(comment.createTime)
        };

        if (comment.replies && comment.replies.length > 0) {
          formattedComment.replies = comment.replies.map(reply => ({
            ...reply,
            createTime: formatRelativeTime(reply.createTime)
          }));
        }

        return formattedComment;
      });

      this.setData({
        comments: formattedComments,
        hasMore: false
      });

      wx.hideLoading();
    }).catch(err => {
      console.error('加载评论失败:', err);
      wx.hideLoading();
      this.setData({
        comments: [],
        hasMore: false
      });
    });
  },

  parseImages(imagesStr) {
    if (!imagesStr) return [];
    if (Array.isArray(imagesStr)) {
      return imagesStr
        .filter(img => img && typeof img === 'string' && img.trim() !== '')
        .map(img => getImageUrl(img));
    }
    
    try {
      const images = JSON.parse(imagesStr);
      return Array.isArray(images) 
        ? images.filter(img => img && typeof img === 'string' && img.trim() !== '')
                .map(img => getImageUrl(img))
        : [];
    } catch (e) {
      console.error('解析图片失败:', e);
      return [];
    }
  },

  parseTags(tagsStr) {
    if (!tagsStr) return [];
    if (Array.isArray(tagsStr)) return tagsStr;
    
    try {
      const tags = JSON.parse(tagsStr);
      return Array.isArray(tags) ? tags : [];
    } catch (e) {
      console.error('解析标签失败:', e);
      return [];
    }
  },

  parseTopics(topicIdsStr) {
    if (!topicIdsStr) return [];
    try {
      const topicIds = JSON.parse(topicIdsStr);
      return Array.isArray(topicIds) ? topicIds : [];
    } catch (e) {
      return [];
    }
  },

  previewImage(e) {
    const index = e.currentTarget.dataset.index;
    const images = this.data.post.images || [];
    if (images.length > 0) {
      wx.previewImage({
        current: images[index],
        urls: images
      });
    }
  },

  async onLike() {
    if (!isLogin()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const postId = this.data.post.postId;
    const originalLiked = this.data.post.isLiked;
    const originalCount = this.data.post.likeCount || 0;

    this.setData({
      'post.isLiked': !originalLiked,
      'post.likeCount': originalLiked ? originalCount - 1 : originalCount + 1
    });

    try {
      const result = await request({
        url: `/api/post/like/toggle/${postId}`,
        method: 'POST'
      });

      if (result.isLiked !== undefined) {
        this.setData({
          'post.isLiked': result.isLiked,
          'post.likeCount': result.likeCount || (result.isLiked ? originalCount + 1 : originalCount - 1)
        });
      }
    } catch (err) {
      this.setData({
        'post.isLiked': originalLiked,
        'post.likeCount': originalCount
      });
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  async onCollect() {
    if (!isLogin()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const postId = this.data.post.postId;
    const originalCollected = this.data.post.isCollected;
    const originalCount = this.data.post.collectCount || 0;

    this.setData({
      'post.isCollected': !originalCollected,
      'post.collectCount': originalCollected ? originalCount - 1 : originalCount + 1
    });

    try {
      const result = await request({
        url: `/api/collect/toggle/${postId}`,
        method: 'POST'
      });

      if (result !== undefined) {
        this.setData({
          'post.isCollected': result,
          'post.collectCount': result ? originalCount + 1 : originalCount - 1
        });
      }

      wx.showToast({ 
        title: result ? '收藏成功' : '取消收藏', 
        icon: 'success' 
      });
    } catch (err) {
      this.setData({
        'post.isCollected': originalCollected,
        'post.collectCount': originalCount
      });
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  onComment() {
    if (!isLogin()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    this.setData({
      showCommentInput: true,
      replyToCommentId: null,
      replyToUserId: null,
      replyToUserName: '',
      commentContent: ''
    });
  },

  onReply(e) {
    const { commentId, userId, userName } = e.currentTarget.dataset;
    
    this.setData({
      replyToCommentId: commentId,
      replyToUserId: userId,
      replyToUserName: userName,
      showCommentInput: true,
      commentContent: ''
    });
  },

  onCommentInput(e) {
    const content = e.detail.value;
    this.setData({ 
      commentContent: content,
      canSubmit: content.trim().length > 0
    });
  },

  async submitComment() {
    if (!this.data.commentContent.trim()) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' });
      return;
    }

    const token = wx.getStorageSync('token');

    wx.showLoading({ title: '发布中...' });

    try {
      const data = {
        postId: this.data.postId,
        content: this.data.commentContent.trim()
      };

      if (this.data.replyToCommentId) {
        data.parentId = this.data.replyToCommentId;
        data.replyToUserId = this.data.replyToUserId;
      }

      const result = await request({
        url: '/api/post/comment/publish',
        method: 'POST',
        data: data
      });

      console.log('评论接口返回:', result);

      wx.hideLoading();
      wx.showToast({ title: '评论成功', icon: 'success' });

      this.setData({
        showCommentInput: false,
        commentContent: '',
        replyToCommentId: null,
        replyToUserId: null,
        replyToUserName: '',
        page: 1
      });

      this.loadComments();
      
      this.setData({
        'post.commentCount': (this.data.post.commentCount || 0) + 1
      });

    } catch (err) {
      wx.hideLoading();
      console.error('评论失败:', err);
      
      if (err.code === 401) {
        this.loadComments();
        this.setData({
          showCommentInput: false,
          commentContent: ''
        });
      } else {
        wx.showToast({ title: '评论失败，请重试', icon: 'none' });
      }
    }
  },

  onCloseCommentInput() {
    this.setData({
      showCommentInput: false,
      commentContent: '',
      replyToCommentId: null,
      replyToUserId: null,
      replyToUserName: ''
    });
  },

  onCommentSortChange(e) {
    const sortBy = e.currentTarget.dataset.sort;
    this.setData({
      commentSortBy: sortBy,
      page: 1
    });
    this.loadComments();
  },

  async onCommentLike(e) {
    if (!isLogin()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const commentId = e.currentTarget.dataset.commentId;
    
    wx.showLoading({ title: '操作中...', mask: true });

    try {
      const result = await request({
        url: `/api/post/comment/like/toggle/${commentId}`,
        method: 'POST'
      });

      wx.hideLoading();

      // result 是 { isLiked: true/false, likeCount: number }
      if (result && result.isLiked !== undefined) {
        const comments = this.data.comments.map(comment => {
          if (comment.commentId === commentId) {
            return {
              ...comment,
              isLiked: result.isLiked,
              likeCount: result.likeCount
            };
          }
          return comment;
        });
        
        this.setData({ comments });
      }
      
    } catch (err) {
      wx.hideLoading();
      console.error('点赞失败:', err);
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  onShowCommentMore(e) {
    const commentId = e.currentTarget.dataset.commentId;
    const userId = e.currentTarget.dataset.userId;
    
    const userInfo = wx.getStorageSync('userInfo');
    const isOwner = userInfo && userInfo.userId === userId;

    const itemList = isOwner ? ['删除评论', '举报', '复制链接'] : ['举报', '复制链接'];

    wx.showActionSheet({
      itemList: itemList,
      itemColor: '#333',
      success: (res) => {
        if (isOwner && res.tapIndex === 0) {
          this.deleteComment(commentId);
        } else if ((isOwner && res.tapIndex === 1) || (!isOwner && res.tapIndex === 0)) {
          this.reportComment(commentId);
        } else if ((isOwner && res.tapIndex === 2) || (!isOwner && res.tapIndex === 1)) {
          this.copyCommentLink(commentId);
        }
      }
    });
  },

  deleteComment(commentId) {
    wx.showModal({
      title: '删除评论',
      content: '确定要删除这条评论吗？',
      confirmText: '删除',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          try {
            await request({
              url: `/api/post/comment/delete/${commentId}`,
              method: 'DELETE'
            });

            wx.showToast({
              title: '删除成功',
              icon: 'success'
            });

            this.loadComments();
            
            this.setData({
              'post.commentCount': Math.max(0, (this.data.post.commentCount || 0) - 1)
            });
          } catch (err) {
            console.error('删除失败:', err);
            wx.showToast({
              title: '删除失败',
              icon: 'none'
            });
          }
        }
      }
    });
  },

  reportComment(commentId) {
    wx.showModal({
      title: '举报评论',
      content: '确定要举报这条评论吗？',
      confirmText: '举报',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          wx.showToast({
            title: '举报成功',
            icon: 'success'
          });
        }
      }
    });
  },

  copyCommentLink(commentId) {
    const link = `pages/post-detail/post-detail?postId=${this.data.postId}&commentId=${commentId}`;
    wx.setClipboardData({
      data: link,
      success: () => {
        wx.showToast({
          title: '链接已复制',
          icon: 'success'
        });
      }
    });
  },

  onShareAppMessage() {
    return {
      title: this.data.post.content || '发现精彩校园生活',
      path: `/pages/post-detail/post-detail?postId=${this.data.postId}`,
      imageUrl: this.data.post.images && this.data.post.images.length > 0 
        ? this.data.post.images[0] 
        : ''
    };
  },

  onShareTimeline() {
    return {
      title: this.data.post.content || '发现精彩校园生活',
      query: `postId=${this.data.postId}`
    };
  },

  goToUserProfile(e) {
    const userId = e.currentTarget.dataset.userid;
    if (userId) {
      wx.navigateTo({
        url: `/pages/user-profile/user-profile?userId=${userId}`
      });
    }
  },

  goToTopic(e) {
    const topicId = e.currentTarget.dataset.topicid;
    if (topicId) {
      wx.navigateTo({
        url: `/pages/topic/topic?topicId=${topicId}`
      });
    }
  }
});
