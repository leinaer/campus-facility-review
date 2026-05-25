// pages/publish/publish.js
const { request, BASE_URL } = require('../../utils/request');
const { isLogin } = require('../../utils/auth');

Page({
  data: {
    mood: 'happy',
    title: '',
    content: '',
    images: [],
    tags: [],
    customTags: [],
    canSubmit: false,
    moods: [
      { key: 'happy', icon: '😊', label: '开心' },
      { key: 'normal', icon: '😐', label: '一般' },
      { key: 'sad', icon: '😢', label: '难过' },
      { key: 'angry', icon: '😡', label: '愤怒' }
    ],
    popularTags: [],
    activityTags: [],
    showActivityTags: false,
    maxTags: 3,
    uploadProgress: 0,
    isSubmitting: false,
    draftTimer: null,
    showCoverSelector: false,
    coverIndex: 0,
    isPublished: false,
    availableTopics: [],
    selectedTopic: null
  },
  
  onLoad() {
    this.loadTags();
    this.loadTopics();
    this.checkCanSubmit();
    this.loadDraft();
  },
  
  onShow() {
    const isPublished = wx.getStorageSync('post_published');
    if (isPublished) {
      wx.removeStorageSync('post_published');
      this.setData({
        isPublished: true,
        mood: 'happy',
        title: '',
        content: '',
        images: [],
        tags: [],
        customTags: [],
        coverIndex: 0,
        showCoverSelector: false,
        selectedTopic: null
      });
      this.checkCanSubmit();
      wx.removeStorageSync('post_draft');
    }
  },
  
  onUnload() {
    if (this.data.draftTimer) {
      clearTimeout(this.data.draftTimer);
    }
    if (!this.data.isPublished) {
      this.saveDraft();
    }
  },
  
  loadTopics() {
    return request({
      url: '/api/topic/list',
      data: { sortBy: 'hot' }
    }).then((data) => {
      this.setData({ availableTopics: data || [] });
    }).catch(err => {
      console.error('加载话题失败:', err);
    });
  },
  
  onSelectTopic(e) {
    const topic = e.currentTarget.dataset.topic;
    const isSelected = this.data.selectedTopic && this.data.selectedTopic.topicId === topic.topicId;
    
    this.setData({ 
      selectedTopic: isSelected ? null : topic
    });
    this.autoSaveDraft();
  },
  
  onAddTopic() {
    wx.navigateTo({
      url: '/pages/topics/topics'
    });
  },

  loadTags() {
    Promise.all([
      request({ url: '/api/tag/hot', data: { limit: 20 } }),
      request({ url: '/api/tag/activity' })
    ]).then(([hotTags, activityTags]) => {
      const popularTags = (Array.isArray(hotTags) ? hotTags : []).map(t => t.tagName || t);
      const activityTagsList = Array.isArray(activityTags) ? activityTags : [];
      
      this.setData({ 
        popularTags,
        activityTags: activityTagsList,
        showActivityTags: activityTagsList.length > 0
      });
    }).catch(err => {
      console.error('加载标签失败:', err);
      this.setData({ 
        popularTags: [],
        activityTags: [],
        showActivityTags: false
      });
    });
  },
  
  onMoodSelect(e) {
    this.setData({ mood: e.currentTarget.dataset.mood });
    this.autoSaveDraft();
  },
  
  onTitleInput(e) {
    this.setData({ title: e.detail.value });
    this.checkCanSubmit();
    this.autoSaveDraft();
  },
  
  onContentInput(e) {
    this.setData({ content: e.detail.value });
    this.checkCanSubmit();
    this.autoSaveDraft();
  },
  
  checkCanSubmit() {
    const hasTitle = this.data.title.trim().length > 0;
    const hasContent = this.data.content.trim().length > 0;
    const hasImages = this.data.images.length > 0;
    this.setData({ canSubmit: hasTitle && hasContent && hasImages });
  },
  
  chooseImages() {
    wx.chooseMedia({
      count: 9 - this.data.images.length,
      mediaType: ['image'],
      sizeType: ['compressed'],
      success: (res) => {
        const newImages = res.tempFiles.map(f => f.tempFilePath);
        const images = [...this.data.images, ...newImages];
        this.setData({ 
          images,
          showCoverSelector: images.length > 1,
          coverIndex: 0
        });
        this.checkCanSubmit();
        this.autoSaveDraft();
        wx.vibrateShort({ type: 'light' });
      }
    });
  },
  
  previewImage(e) {
    const index = e.currentTarget.dataset.index;
    wx.previewImage({
      current: this.data.images[index],
      urls: this.data.images
    });
  },
  
  deleteImage(e) {
    const index = e.currentTarget.dataset.index;
    const images = this.data.images.filter((_, i) => i !== index);
    this.setData({ 
      images,
      showCoverSelector: images.length > 1,
      coverIndex: 0
    });
    this.checkCanSubmit();
    this.autoSaveDraft();
  },
  
  selectCover(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ coverIndex: index });
  },
  
  uploadImages() {
    if (this.data.images.length === 0) {
      return Promise.resolve([]);
    }
    
    const token = wx.getStorageSync('token');
    const total = this.data.images.length;
    let uploaded = 0;
    
    const uploadPromises = this.data.images.map(filePath => {
      return new Promise((resolve, reject) => {
        wx.uploadFile({
          url: `${BASE_URL}/api/upload/image`,
          filePath: filePath,
          name: 'file',
          formData: { type: 'post' },
          header: { 'Authorization': `Bearer ${token}` },
          success: (res) => {
            uploaded++;
            const progress = Math.round((uploaded / total) * 100);
            this.setData({ uploadProgress: progress });
            
            try {
              const data = JSON.parse(res.data);
              console.log('图片上传返回:', data);
              
              if (data.code === 200 && data.data) {
                resolve(data.data);
              } else {
                console.error('图片上传失败:', data.msg);
                reject(new Error(data.msg || '上传失败'));
              }
            } catch (e) {
              console.error('解析上传响应失败:', e);
              reject(new Error('上传响应解析失败'));
            }
          },
          fail: (err) => {
            console.error('图片上传请求失败:', err);
            reject(new Error('网络请求失败'));
          }
        });
      });
    });
    
    return Promise.all(uploadPromises);
  },
  
  onTagSelect(e) {
    const tag = e.currentTarget.dataset.tag;
    
    if (this.data.tags.indexOf(tag) > -1) {
      const tags = this.data.tags.filter(t => t !== tag);
      this.setData({ tags });
    } else {
      if (this.data.tags.length >= this.data.maxTags) {
        wx.showToast({ 
          title: `最多选择${this.data.maxTags}个标签`, 
          icon: 'none' 
        });
        return;
      }
      const tags = [...this.data.tags, tag];
      this.setData({ tags });
    }
    
    this.autoSaveDraft();
  },
  
  onActivityTagSelect(e) {
    const tag = e.currentTarget.dataset.tag;
    
    if (this.data.tags.indexOf(tag) > -1) {
      const tags = this.data.tags.filter(t => t !== tag);
      this.setData({ tags });
    } else {
      if (this.data.tags.length >= this.data.maxTags) {
        wx.showToast({ 
          title: `最多选择${this.data.maxTags}个标签`, 
          icon: 'none' 
        });
        return;
      }
      const tags = [...this.data.tags, tag];
      this.setData({ tags });
    }
    
    this.autoSaveDraft();
  },
  
  addCustomTag() {
    wx.showModal({
      title: '添加标签',
      editable: true,
      placeholderText: '请输入标签名称',
      success: (res) => {
        if (res.confirm && res.content) {
          const tagName = res.content.trim();
          
          if (!tagName) {
            wx.showToast({ title: '标签不能为空', icon: 'none' });
            return;
          }
          
          if (tagName.length > 10) {
            wx.showToast({ title: '标签不能超过10个字', icon: 'none' });
            return;
          }
          
          if (this.data.tags.length >= this.data.maxTags) {
            wx.showToast({ 
              title: `最多选择${this.data.maxTags}个标签`, 
              icon: 'none' 
            });
            return;
          }
          
          const allTags = [...this.data.popularTags, ...this.data.customTags];
          if (allTags.includes(tagName)) {
            wx.showToast({ title: '标签已存在', icon: 'none' });
            return;
          }
          
          const customTags = [...this.data.customTags, tagName];
          const tags = [...this.data.tags, tagName];
          
          this.setData({ 
            customTags,
            tags 
          });
          
          this.autoSaveDraft();
          wx.vibrateShort({ type: 'light' });
          wx.showToast({ title: '添加成功', icon: 'success' });
        }
      }
    });
  },
  
  deleteTag(e) {
    const tag = e.currentTarget.dataset.tag;
    const isCustom = this.data.customTags.includes(tag);
    
    if (isCustom) {
      const customTags = this.data.customTags.filter(t => t !== tag);
      const tags = this.data.tags.filter(t => t !== tag);
      
      this.setData({ 
        customTags,
        tags 
      });
    } else {
      const tags = this.data.tags.filter(t => t !== tag);
      this.setData({ tags });
    }
    
    this.autoSaveDraft();
  },
  
  autoSaveDraft() {
    if (this.data.isPublished) {
      return;
    }
    
    if (this.data.draftTimer) {
      clearTimeout(this.data.draftTimer);
    }
    
    this.data.draftTimer = setTimeout(() => {
      this.saveDraft();
    }, 1000);
  },
  
  saveDraft() {
    if (this.data.isPublished) {
      return;
    }

    if (!this.data.title.trim() && !this.data.content.trim() && this.data.images.length === 0) {
      return;
    }

    const draft = {
      mood: this.data.mood,
      title: this.data.title,
      content: this.data.content,
      images: this.data.images,
      coverIndex: this.data.coverIndex,
      tags: this.data.tags,
      customTags: this.data.customTags,
      timestamp: Date.now()
    };

    try {
      wx.setStorageSync('post_draft', draft);
    } catch (e) {
      console.error('保存草稿失败:', e);
    }
  },

  loadDraft() {
    try {
      const draft = wx.getStorageSync('post_draft');
      if (draft && draft.timestamp) {
        const hoursPassed = (Date.now() - draft.timestamp) / (1000 * 60 * 60);

        if (hoursPassed < 24 && (draft.title || draft.content || draft.images?.length > 0)) {
          wx.showModal({
            title: '恢复草稿',
            content: '检测到未发布的草稿，是否恢复？',
            success: (res) => {
              if (res.confirm) {
                this.setData({
                  mood: draft.mood || 'happy',
                  title: draft.title || '',
                  content: draft.content || '',
                  images: draft.images || [],
                  coverIndex: draft.coverIndex || 0,
                  tags: draft.tags || [],
                  customTags: draft.customTags || [],
                  showCoverSelector: (draft.images || []).length > 1
                });
                this.checkCanSubmit();
              } else {
                wx.removeStorageSync('post_draft');
              }
            }
          });
        }
      }
    } catch (e) {
      console.error('加载草稿失败:', e);
    }
  },

  clearDraft() {
    wx.removeStorageSync('post_draft');
  },

  async onSubmit() {
    if (this.data.isSubmitting) {
      return;
    }

    if (!this.data.title.trim()) {
      wx.showToast({ title: '请输入标题', icon: 'none' });
      return;
    }

    if (!this.data.content.trim()) {
      wx.showToast({ title: '请输入内容', icon: 'none' });
      return;
    }

    if (this.data.images.length === 0) {
      wx.showToast({ title: '请至少上传一张图片', icon: 'none' });
      return;
    }

    if (this.data.tags.length > this.data.maxTags) {
      wx.showToast({
        title: `最多选择${this.data.maxTags}个标签`,
        icon: 'none'
      });
      return;
    }

    this.setData({ isSubmitting: true, uploadProgress: 0 });
    wx.showLoading({ title: '发布中...' });

    try {
      const imageUrls = await this.uploadImages();

      const coverImage = imageUrls[this.data.coverIndex] || imageUrls[0];

      const postData = {
        title: this.data.title.trim(),
        content: this.data.content.trim(),
        mood: this.data.mood,
        images: JSON.stringify(imageUrls),
        coverImage: coverImage,
        tags: JSON.stringify(this.data.tags),
        topicId: this.data.selectedTopic ? this.data.selectedTopic.topicId : null
      };

      await request({
        url: '/api/post/publish',
        method: 'POST',
        data: postData
      });

      this.setData({ isSubmitting: false });
      wx.hideLoading();

      wx.showToast({
        title: '发布成功',
        icon: 'success',
        duration: 1500
      });

      wx.removeStorageSync('post_draft');
      wx.setStorageSync('post_published', true);

      setTimeout(() => {
        wx.switchTab({
          url: '/pages/discover/discover'
        });
      }, 1500);
    } catch (err) {
      this.setData({ isSubmitting: false, uploadProgress: 0 });
      wx.hideLoading();
      console.error('发布失败:', err);
      wx.showToast({ title: err.message || '发布失败', icon: 'none' });
    }
  }
});
