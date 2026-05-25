// pages/facility-detail/facility-detail.js
const { request, getImageUrl } = require('../../utils/request');
const { previewImages } = require('../../utils/image');
const { isLogin } = require('../../utils/auth');
const { formatRelativeTime } = require('../../utils/formatTime');

Page({
  data: {
    facilityId: null,
    facility: {},
    evaluations: [],
    sortBy: 'latest',
    page: 1,
    hasMore: true,
    tags: [],
    isFavorite: false,
    
    showCommentInput: false,
    commentContent: '',
    canSubmit: false,
    replyToCommentId: null,
    replyToUserId: null,
    replyToUserName: '',
    currentEvaluationId: null,
    comments: [],
    commentsLoading: false,
    commentSortBy: 'time',
    
    // 性能优化：评论缓存
    commentsCache: {}
  },


  getMoodIcon(mood) {
    const moodMap = {
      'happy': '😊',
      'sad': '',
      'angry': '',
      'neutral': '',
      'excited': ''
    };
    return moodMap[mood] || '';
  },

  onLoad(options) {
    const id = options.id || options.facilityId;
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }
    
    this.setData({ facilityId: id });
    this.loadFacilityDetail();
    this.loadEvaluations();
    this.checkFavorite();
  },
  
  onShow() {
    const needRefresh = wx.getStorageSync('needRefresh');
    if (needRefresh) {
      this.loadFacilityDetail();
      this.loadEvaluations();
      this.checkFavorite();
      wx.removeStorageSync('needRefresh');
    }
  },
  
  loadFacilityDetail() {
    request({ url: `/api/facility/${this.data.facilityId}` }).then((data) => {
      this.setData({ facility: data });
      wx.setNavigationBarTitle({ title: data.name });
    }).catch(err => {
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  checkFavorite() {
    if (!isLogin()) {
      return;
    }

    request({
      url: `/api/favorite/is-favorite/${this.data.facilityId}`,
      method: 'GET'
    }).then((data) => {
      this.setData({ isFavorite: data === true || data === 'true' });
    }).catch(err => {
      console.error('检查收藏状态失败:', err);
    });
  },

  toggleFavorite() {
    if (!isLogin()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '处理中...', mask: true });

    request({
      url: `/api/favorite/toggle/${this.data.facilityId}`,
      method: 'POST'
    }).then((data) => {
      wx.hideLoading();
      const isFavorite = data === true || data === 'true';
      this.setData({ isFavorite });
      
      wx.showToast({
        title: isFavorite ? '收藏成功' : '已取消收藏',
        icon: 'success'
      });
    }).catch(err => {
      wx.hideLoading();
      console.error('收藏操作失败:', err);
      wx.showToast({ title: '操作失败', icon: 'none' });
    });
  },

  loadEvaluations() {
    const { facilityId, sortBy, page } = this.data;

    request({
      url: `/api/evaluation/by-facility/${facilityId}`,
      data: { sortBy, page, size: 10 }
    }).then((data) => {
      const newList = (page === 1 ? data.list : [...this.data.evaluations, ...data.list]).map(item => ({
        ...item,
        createTime: formatRelativeTime(item.createTime)
      }));

      this.setData({
        evaluations: newList,
        hasMore: data.list.length === 10
      });
    }).catch(err => {
      wx.showToast({ title: '加载评价失败', icon: 'none' });
    });
  },

  onSortChange(e) {
    const sort = e.currentTarget.dataset.sort;
    this.setData({
      sortBy: sort,
      page: 1,
      evaluations: []
    });
    this.loadEvaluations();
  },

  onLoadMore() {
    if (this.data.hasMore) {
      this.setData({ page: this.data.page + 1 });
      this.loadEvaluations();
    }
  },

  goToPublish() {
    if (!isLogin()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: `/pages/evaluate/evaluate?facilityId=${this.data.facilityId}`
    });
  },

  async onComment(e) {
    const evaluationId = e.currentTarget.dataset.id;
    
    if (!isLogin()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    this.setData({
      currentEvaluationId: evaluationId,
      showCommentInput: true,
      replyToCommentId: null,
      replyToUserId: null,
      replyToUserName: '',
      commentContent: '',
      commentsLoading: true,
      commentSortBy: 'time'
    });

    // 使用缓存
    const cacheKey = `${evaluationId}_time`;
    if (this.data.commentsCache[cacheKey]) {
      this.setData({
        comments: this.data.commentsCache[cacheKey],
        commentsLoading: false
      });
    } else {
      await this.loadComments(evaluationId, 'time');
    }
  },

  onCommentSortChange(e) {
    const sortBy = e.currentTarget.dataset.sort;
    
    if (!this.data.currentEvaluationId) {
      wx.showToast({ title: '请先选择评价', icon: 'none' });
      return;
    }
    
    // 防抖：如果已经是当前排序，不重新加载
    if (sortBy === this.data.commentSortBy) {
      return;
    }
    
    this.setData({ commentSortBy: sortBy });
    
    // 检查缓存
    const cacheKey = `${this.data.currentEvaluationId}_${sortBy}`;
    if (this.data.commentsCache[cacheKey]) {
      this.setData({
        comments: this.data.commentsCache[cacheKey]
      });
    } else {
      this.loadComments(this.data.currentEvaluationId, sortBy);
    }
  },

  async loadComments(evaluationId, sortBy = 'time') {
    try {
      const res = await request({
        url: `/api/comment/by-evaluation/${evaluationId}`,
        method: 'GET',
        data: { sortBy }
      });

      let comments = [];
      if (res && res.code === 200) {
        comments = res.data || [];
      } else if (Array.isArray(res)) {
        comments = res;
      }

      const formattedComments = comments.map(comment => ({
        ...comment,
        createTime: formatRelativeTime(comment.createTime)
      }));

      // 存入缓存
      const cacheKey = `${evaluationId}_${sortBy}`;
      const commentsCache = {
        ...this.data.commentsCache,
        [cacheKey]: formattedComments
      };

      this.setData({
        comments: formattedComments,
        commentsCache,
        commentsLoading: false
      });
    } catch (err) {
      console.error('加载评论失败:', err);
      this.setData({
        comments: [],
        commentsLoading: false
      });
    }
  },


  // 隐藏评论输入框
  hideCommentInput() {
    this.setData({
      showCommentInput: false,
      commentContent: '',
      replyToCommentId: null,
      replyToUserId: null,
      replyToUserName: '',
      commentsLoading: false
    });
  },

  // 回复评论
  onReplyComment(e) {
    const commentId = e.currentTarget.dataset.commentId;
    const userId = e.currentTarget.dataset.userId;
    const userName = e.currentTarget.dataset.userName;
    
    this.setData({
      replyToCommentId: commentId,
      replyToUserId: userId,
      replyToUserName: userName,
      commentContent: ''
    });
  },
  
  async onCommentVote(e) {
    const commentId = e.currentTarget.dataset.commentId;
    const voteType = parseInt(e.currentTarget.dataset.voteType);

    if (!isLogin()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const comments = [...this.data.comments];
    const index = comments.findIndex(c => c.commentId === commentId);
    if (index === -1) return;

    const currentVote = comments[index].userVoteType;

    try {
      const data = await request({
        url: `/api/comment/vote/${commentId}?voteType=${voteType}`,
        method: 'POST'
      });

      if (currentVote === voteType) {
        comments[index].userVoteType = null;
      } else {
        comments[index].userVoteType = voteType;
      }
      comments[index].likeCount = data.likeCount || 0;
      comments[index].dislikeCount = data.dislikeCount || 0;

      this.setData({ comments });
    } catch (err) {
      console.error('投票失败:', err);
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  
  // 显示评论更多操作菜单
  onShowCommentMore(e) {
    const commentId = e.currentTarget.dataset.commentId;
    const userId = e.currentTarget.dataset.userId;
    
    const comment = this.data.comments.find(c => c.commentId === commentId);
    if (!comment) return;

    const userInfo = wx.getStorageSync('userInfo');
    const isOwner = comment.userId === userInfo?.userId;
    
    const itemList = isOwner 
      ? ['删除评论', '复制链接']
      : ['举报', '复制链接'];

    wx.showActionSheet({
      itemList: itemList,
      success: async (res) => {
        if (isOwner) {
          if (res.tapIndex === 0) {
            await this.deleteComment(commentId);
          } else if (res.tapIndex === 1) {
            this.copyCommentLink(commentId);
          }
        } else {
          if (res.tapIndex === 0) {
            this.reportComment(commentId);
          } else if (res.tapIndex === 1) {
            this.copyCommentLink(commentId);
          }
        }
      }
    });
  },

  // 删除评论
  async deleteComment(commentId) {
    wx.showModal({
      title: '确认删除',
      content: '删除后无法恢复，确定要删除吗？',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '删除中...' });
          
          try {
            await request({
              url: `/api/comment/delete/${commentId}`,
              method: 'DELETE'
            });
            
            wx.hideLoading();
            wx.showToast({ title: '删除成功', icon: 'success' });
            
            const comments = this.data.comments.filter(c => c.commentId !== commentId);
            this.setData({ comments });
          } catch (err) {
            wx.hideLoading();
            console.error('删除失败:', err);
            wx.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      }
    });
  },

  // 举报评论
  reportComment(commentId) {
    wx.showModal({
      title: '举报评论',
      content: '请选择举报原因',
      editable: true,
      placeholderText: '请输入举报原因（选填）',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '提交中...' });
          
          try {
            await request({
              url: `/api/comment/report/${commentId}`,
              method: 'POST',
              data: { reason: res.content || '违规评论' }
            });
            
            wx.hideLoading();
            wx.showToast({ title: '举报成功', icon: 'success' });
          } catch (err) {
            wx.hideLoading();
            console.error('举报失败:', err);
            wx.showToast({ title: '举报失败', icon: 'none' });
          }
        }
      }
    });
  },

  // 复制评论链接
  copyCommentLink(commentId) {
    const link = `pages/facility-detail/facility-detail?commentId=${commentId}`;
    
    wx.setClipboardData({
      data: link,
      success: () => {
        wx.showToast({ title: '链接已复制', icon: 'success' });
      },
      fail: () => {
        wx.showToast({ title: '复制失败', icon: 'none' });
      }
    });
  },

  // 点赞评价
  async onLike(e) {
    const evaluationId = e.currentTarget.dataset.id;
    const index = this.data.evaluations.findIndex(ev => ev.evaluationId === evaluationId);
    if (index === -1) return;

    if (!isLogin()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    try {
      const result = await request({
        url: `/api/like/toggle/${evaluationId}`,
        method: 'POST'
      });

      const evaluations = this.data.evaluations;
      evaluations[index].isLiked = result.isLiked;
      evaluations[index].likeCount = result.likeCount || evaluations[index].likeCount;
      
      this.setData({ evaluations });
    } catch (err) {
      console.error('点赞失败:', err);
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  // 点击评价进入详情
  onEvaluationClick(e) {
    const evaluationId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/evaluation-detail/evaluation-detail?evaluationId=${evaluationId}`
    });
  },

  // 有用/无用投票
  async onHelpful(e) {
    const evaluationId = e.currentTarget.dataset.id;
    const voteType = parseInt(e.currentTarget.dataset.type);
    const index = this.data.evaluations.findIndex(ev => ev.evaluationId === evaluationId);
    if (index === -1) return;

    if (!isLogin()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const evaluations = [...this.data.evaluations];
    const currentVote = evaluations[index].userVoteType;

    if (currentVote === voteType) {
      evaluations[index].userVoteType = null;
      if (voteType === 1) {
        evaluations[index].helpfulCount = Math.max(0, (evaluations[index].helpfulCount || 0) - 1);
      } else {
        evaluations[index].unhelpfulCount = Math.max(0, (evaluations[index].unhelpfulCount || 0) - 1);
      }
      
      this.setData({ evaluations });
      
      try {
        await request({
          url: `/api/evaluation/helpful/vote/${evaluationId}?voteType=${voteType}`,
          method: 'POST'
        });
      } catch (err) {
        evaluations[index].userVoteType = currentVote;
        if (voteType === 1) {
          evaluations[index].helpfulCount = (evaluations[index].helpfulCount || 0) + 1;
        } else {
          evaluations[index].unhelpfulCount = (evaluations[index].unhelpfulCount || 0) + 1;
        }
        this.setData({ evaluations });
      }
      return;
    }

    if (currentVote !== null) {
      if (currentVote === 1) {
        evaluations[index].helpfulCount = Math.max(0, (evaluations[index].helpfulCount || 0) - 1);
      } else if (currentVote === 0) {
        evaluations[index].unhelpfulCount = Math.max(0, (evaluations[index].unhelpfulCount || 0) - 1);
      }
    }

    evaluations[index].userVoteType = voteType;
    if (voteType === 1) {
      evaluations[index].helpfulCount = (evaluations[index].helpfulCount || 0) + 1;
    } else {
      evaluations[index].unhelpfulCount = (evaluations[index].unhelpfulCount || 0) + 1;
    }
    
    this.setData({ evaluations });

    try {
      await request({
        url: `/api/evaluation/helpful/vote/${evaluationId}?voteType=${voteType}`,
        method: 'POST'
      });
    } catch (err) {
      if (currentVote !== null) {
        if (currentVote === 1) {
          evaluations[index].helpfulCount = (evaluations[index].helpfulCount || 0) + 1;
        } else if (currentVote === 0) {
          evaluations[index].unhelpfulCount = (evaluations[index].unhelpfulCount || 0) + 1;
        }
      }
      evaluations[index].userVoteType = currentVote;
      if (voteType === 1) {
        evaluations[index].helpfulCount = Math.max(0, (evaluations[index].helpfulCount || 0) - 1);
      } else {
        evaluations[index].unhelpfulCount = Math.max(0, (evaluations[index].unhelpfulCount || 0) - 1);
      }
      this.setData({ evaluations });
    }
  },

  // 显示更多操作菜单
  onShowMore(e) {
    const evaluationId = e.currentTarget.dataset.id;
    const evaluation = this.data.evaluations.find(ev => ev.evaluationId === evaluationId);
    
    if (!evaluation) return;

    const userInfo = wx.getStorageSync('userInfo');
    const isOwner = evaluation.userId === userInfo?.userId;
    
    const itemList = isOwner 
      ? ['删除评价', '复制链接']
      : ['举报', '屏蔽用户', '复制链接'];

    wx.showActionSheet({
      itemList: itemList,
      success: (res) => {
        if (isOwner) {
          if (res.tapIndex === 0) {
            this.deleteEvaluation(evaluationId);
          } else if (res.tapIndex === 1) {
            this.copyLink(evaluationId);
          }
        } else {
          if (res.tapIndex === 0) {
            this.reportEvaluation(evaluationId);
          } else if (res.tapIndex === 1) {
            this.blockUser(evaluation.userId, evaluation.userNickname);
          } else if (res.tapIndex === 2) {
            this.copyLink(evaluationId);
          }
        }
      }
    });
  },

  // 删除评价
  deleteEvaluation(evaluationId) {
    wx.showModal({
      title: '确认删除',
      content: '删除后无法恢复，确定要删除吗？',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '删除中...' });
          
          try {
            await request({
              url: `/api/evaluation/${evaluationId}`,
              method: 'DELETE'
            });
            
            wx.hideLoading();
            wx.showToast({ title: '删除成功', icon: 'success' });
            
            const evaluations = this.data.evaluations.filter(ev => ev.evaluationId !== evaluationId);
            this.setData({ evaluations });
          } catch (err) {
            wx.hideLoading();
            console.error('删除失败:', err);
            wx.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      }
    });
  },

  // 举报评价
  reportEvaluation(evaluationId) {
    wx.showModal({
      title: '举报评价',
      content: '请选择举报原因',
      editable: true,
      placeholderText: '请输入举报原因（选填）',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '提交中...' });
          
          try {
            await request({
              url: `/api/evaluation/report/${evaluationId}`,
              method: 'POST',
              data: { reason: res.content || '恶意评价' }
            });
            
            wx.hideLoading();
            wx.showToast({ title: '举报成功', icon: 'success' });
          } catch (err) {
            wx.hideLoading();
            console.error('举报失败:', err);
            wx.showToast({ title: '举报失败', icon: 'none' });
          }
        }
      }
    });
  },

  // 屏蔽用户
  blockUser(userId, nickname) {
    wx.showModal({
      title: '屏蔽用户',
      content: `确定要屏蔽用户"${nickname}"吗？屏蔽后将不再看到该用户的评价`,
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '屏蔽中...' });
          
          try {
            await request({
              url: `/api/user/block/${userId}`,
              method: 'POST'
            });
            
            wx.hideLoading();
            wx.showToast({ title: '已屏蔽', icon: 'success' });
          } catch (err) {
            wx.hideLoading();
            console.error('屏蔽失败:', err);
            wx.showToast({ title: '操作失败', icon: 'none' });
          }
        }
      }
    });
  },

  // 复制链接
  copyLink(evaluationId) {
    const link = `pages/evaluation-detail/evaluation-detail?evaluationId=${evaluationId}`;
    
    wx.setClipboardData({
      data: link,
      success: () => {
        wx.showToast({ title: '链接已复制', icon: 'success' });
      },
      fail: () => {
        wx.showToast({ title: '复制失败', icon: 'none' });
      }
    });
  },

  // 预览图片
  previewImage(e) {
    const index = e.currentTarget.dataset.index;
    const evaluationId = e.currentTarget.dataset.id;
    
    const evaluation = this.data.evaluations.find(ev => ev.evaluationId === evaluationId);
    if (!evaluation || !evaluation.images) return;

    wx.previewImage({
      current: evaluation.images[index],
      urls: evaluation.images
    });
  },

  // 评论输入
  onCommentInput(e) {
    const content = e.detail.value;
    this.setData({ 
      commentContent: content,
      canSubmit: content.trim().length > 0
    });
  },

  // 提交评论
  async submitComment() {
    if (!this.data.commentContent.trim()) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '发布中...' });

    try {
      const data = {
        evaluationId: this.data.currentEvaluationId,
        content: this.data.commentContent.trim()
      };

      if (this.data.replyToCommentId) {
        data.parentId = this.data.replyToCommentId;
        data.replyToUserId = this.data.replyToUserId;
      }

      await request({
        url: '/api/comment/publish',
        method: 'POST',
        data: data
      });

      wx.hideLoading();
      wx.showToast({ title: '评论成功', icon: 'success' });

      this.setData({
        commentContent: '',
        replyToCommentId: null,
        replyToUserId: null,
        replyToUserName: ''
      });

      // 清除缓存
      const commentsCache = {};
      Object.keys(this.data.commentsCache).forEach(key => {
        if (!key.startsWith(`${this.data.currentEvaluationId}_`)) {
          commentsCache[key] = this.data.commentsCache[key];
        }
      });

      // 重新加载评论
      await this.loadComments(this.data.currentEvaluationId, this.data.commentSortBy);
      
      // 本地更新评价的评论数
      const evaluations = this.data.evaluations.map(ev => {
        if (ev.evaluationId === this.data.currentEvaluationId) {
          return {
            ...ev,
            commentCount: (ev.commentCount || 0) + 1
          };
        }
        return ev;
      });
      this.setData({ evaluations, commentsCache });

    } catch (err) {
      wx.hideLoading();
      console.error('评论失败:', err);
      wx.showToast({ title: '评论失败，请重试', icon: 'none' });
    }
  }
});
