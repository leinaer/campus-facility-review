// components/evaluation-card/evaluation-card.js
const { request, getImageUrl } = require('../../utils/request');
const { isLogin } = require('../../utils/auth');

Component({
  properties: {
    evaluation: {
      type: Object,
      value: {}
    }
  },

  // data: {
  //   processedEvaluation: {}
  // },
  data: {
    evaluation: {},
    comments: [],
    showComments: false
  },


  observers: {
    'evaluation': function(evaluation) {
      if (!evaluation || Object.keys(evaluation).length === 0) {
        this.setData({ processedEvaluation: {} });
        return;
      }
      
      const processed = {
        evaluationId: evaluation.evaluationId,
        userAvatar: evaluation.userAvatar,
        userNickname: evaluation.userNickname || '匿名用户',
        rating: evaluation.rating,
        content: evaluation.content,
        mood: evaluation.mood,
        likeCount: evaluation.likeCount || 0,
        commentCount: evaluation.commentCount || 0,
        isLiked: evaluation.isLiked || false,
        createTime: evaluation.createTime
      };
      
      // 处理图片
      if (evaluation.images) {
        if (typeof evaluation.images === 'string') {
          try {
            const images = JSON.parse(evaluation.images);
            processed.images = Array.isArray(images) ? images.map(img => this.getImageUrlSafe(img)) : [];
          } catch (e) {
            processed.images = [];
          }
        } else if (Array.isArray(evaluation.images)) {
          processed.images = evaluation.images.map(img => this.getImageUrlSafe(img));
        } else {
          processed.images = [];
        }
      } else {
        processed.images = [];
      }
      
      // 处理标签
      if (evaluation.tags) {
        if (typeof evaluation.tags === 'string') {
          try {
            processed.tags = JSON.parse(evaluation.tags);
          } catch (e) {
            processed.tags = [];
          }
        } else if (Array.isArray(evaluation.tags)) {
          processed.tags = evaluation.tags;
        } else {
          processed.tags = [];
        }
      } else {
        processed.tags = [];
      }
      
      this.setData({ processedEvaluation: processed });
    }
  },

  methods: {
    getImageUrlSafe(url) {
      if (!url) return '';
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      return getImageUrl(url);
    },

    getMoodIcon(mood) {
      const moodMap = {
        'happy': '😊',
        'sad': '😢',
        'angry': '😡',
        'neutral': '😐',
        'excited': '🤩'
      };
      return moodMap[mood] || '😐';
    },

    previewImage(e) {
      const index = e.currentTarget.dataset.index;
      const images = this.data.processedEvaluation.images || [];
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

      const evaluationId = this.data.processedEvaluation.evaluationId;
      const originalLiked = this.data.processedEvaluation.isLiked;
      const originalCount = this.data.processedEvaluation.likeCount || 0;

      this.setData({
        'processedEvaluation.isLiked': !originalLiked,
        'processedEvaluation.likeCount': originalLiked ? originalCount - 1 : originalCount + 1
      });

      try {
        const result = await request({
          url: `/api/like/toggle/${evaluationId}`,
          method: 'POST'
        });

        if (result.isLiked !== this.data.processedEvaluation.isLiked) {
          this.setData({
            'processedEvaluation.isLiked': result.isLiked,
            'processedEvaluation.likeCount': result.isLiked ? originalCount + 1 : originalCount - 1
          });
        }
      } catch (err) {
        this.setData({
          'processedEvaluation.isLiked': originalLiked,
          'processedEvaluation.likeCount': originalCount
        });
        wx.showToast({ title: '操作失败', icon: 'none' });
      }
    },

    onComment() {
      // console.log('👉 组件内 onComment 被触发');
      const showComments = !this.data.showComments;
      this.setData({ showComments });
      
      if (showComments && this.data.comments.length === 0) {
        this.loadComments();
      }
      
      this.triggerEvent('comment', {
        evaluationId: this.data.processedEvaluation.evaluationId,
        show: showComments
      });
    },

    async loadComments() {
      try {
        const evaluationId = this.data.processedEvaluation.evaluationId;
        const comments = await request({
          url: `/api/comment/by-evaluation/${evaluationId}`
        });
        
        if (Array.isArray(comments)) {
          this.setData({ comments: comments });
        }
      } catch (err) {
        console.error('加载评论失败:', err);
      }
    },


    onReply(e) {
      const { commentId, userId, userName } = e.currentTarget.dataset;
      
      this.triggerEvent('reply', {
        commentId: commentId,
        userId: userId,
        userName: userName,
        evaluationId: this.data.processedEvaluation.evaluationId
      });
    },

    async onCommentLike(e) {
      if (!isLogin()) {
        wx.showToast({ title: '请先登录', icon: 'none' });
        return;
      }

      const commentId = e.currentTarget.dataset.commentId;
      const comments = this.data.comments.map(comment => {
        if (comment.commentId === commentId) {
          const originalLiked = comment.isLiked || false;
          const originalCount = comment.likeCount || 0;
          return {
            ...comment,
            isLiked: !originalLiked,
            likeCount: originalLiked ? originalCount - 1 : originalCount + 1
          };
        }
        return comment;
      });
      this.setData({ comments });

      try {
        const result = await request({
          url: `/api/comment/like/toggle/${commentId}`,
          method: 'POST'
        });

        if (result.isLiked !== undefined) {
          const updatedComments = this.data.comments.map(comment => {
            if (comment.commentId === commentId) {
              return {
                ...comment,
                isLiked: result.isLiked,
                likeCount: result.likeCount || comment.likeCount
              };
            }
            return comment;
          });
          this.setData({ comments: updatedComments });
        }
      } catch (err) {
        const originalComments = this.data.comments.map(comment => {
          if (comment.commentId === commentId) {
            const wasLiked = !comment.isLiked;
            const wasCount = comment.isLiked ? comment.likeCount + 1 : comment.likeCount - 1;
            return {
              ...comment,
              isLiked: wasLiked,
              likeCount: wasCount
            };
          }
          return comment;
        });
        this.setData({ comments: originalComments });
        wx.showToast({ title: '操作失败', icon: 'none' });
      }
    },

    async onSubCommentLike(e) {
      if (!isLogin()) {
        wx.showToast({ title: '请先登录', icon: 'none' });
        return;
      }

      const commentId = e.currentTarget.dataset.commentId;
      const parentId = e.currentTarget.dataset.parentId;

      const comments = this.data.comments.map(comment => {
        if (comment.commentId === parentId && comment.replies) {
          const updatedReplies = comment.replies.map(reply => {
            if (reply.commentId === commentId) {
              const originalLiked = reply.isLiked || false;
              const originalCount = reply.likeCount || 0;
              return {
                ...reply,
                isLiked: !originalLiked,
                likeCount: originalLiked ? originalCount - 1 : originalCount + 1
              };
            }
            return reply;
          });
          return { ...comment, replies: updatedReplies };
        }
        return comment;
      });
      this.setData({ comments });

      try {
        const result = await request({
          url: `/api/comment/like/toggle/${commentId}`,
          method: 'POST'
        });

        if (result.isLiked !== undefined) {
          const updatedComments = this.data.comments.map(comment => {
            if (comment.commentId === parentId && comment.replies) {
              const updatedReplies = comment.replies.map(reply => {
                if (reply.commentId === commentId) {
                  return {
                    ...reply,
                    isLiked: result.isLiked,
                    likeCount: result.likeCount || reply.likeCount
                  };
                }
                return reply;
              });
              return { ...comment, replies: updatedReplies };
            }
            return comment;
          });
          this.setData({ comments: updatedComments });
        }
      } catch (err) {
        const rollbackComments = this.data.comments.map(comment => {
          if (comment.commentId === parentId && comment.replies) {
            const rollbackReplies = comment.replies.map(reply => {
              if (reply.commentId === commentId) {
                const wasLiked = !reply.isLiked;
                const wasCount = reply.isLiked ? reply.likeCount + 1 : reply.likeCount - 1;
                return {
                  ...reply,
                  isLiked: wasLiked,
                  likeCount: wasCount
                };
              }
              return reply;
            });
            return { ...comment, replies: rollbackReplies };
          }
          return comment;
        });
        this.setData({ comments: rollbackComments });
        wx.showToast({ title: '操作失败', icon: 'none' });
      }
    },
// 显示更多操作菜单
    onShowMore(e) {
      const evaluationId = e.currentTarget.dataset.id;

      wx.showActionSheet({
        itemList: ['举报', '复制链接'],
        itemColor: '#333',
        success: (res) => {
          if (res.tapIndex === 0) {
            // 举报
            this.reportEvaluation(evaluationId);
          } else if (res.tapIndex === 1) {
            // 复制链接
            this.copyLink(evaluationId);
          }
        }
      });
    },

    // 举报评价
    reportEvaluation(evaluationId) {
      wx.showModal({
        title: '举报评价',
        content: '确定要举报这条评价吗？',
        confirmText: '举报',
        confirmColor: '#ff4d4f',
        success: (res) => {
          if (res.confirm) {
            // TODO: 调用举报接口
            wx.showToast({
              title: '举报成功',
              icon: 'success'
            });
          }
        }
      });
    },

    // 复制链接
    copyLink(evaluationId) {
      const link = `pages/facility-detail/facility-detail?evaluationId=${evaluationId}`;
      wx.setClipboardData({
        data: link,
        success: () => {
          wx.showToast({
            title: '链接已复制',
            icon: 'success'
          });
        }
      });
    }
  }
});