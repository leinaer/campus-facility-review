// pages/evaluate/evaluate.js
const { request, BASE_URL } = require('../../utils/request');
const { isLogin } = require('../../utils/auth');

Page({
  data: {
    facilityId: null,
    facility: {},
    rating: 0,
    ratingText: '点击星星评分',
    mood: '',
    content: '',
    images: [],
    tags: [],
    customTags: [],
    canSubmit: false,
    moods: [
      { key: 'happy', icon: '😊', label: '超满意' },
      { key: 'normal', icon: '😐', label: '一般般' },
      { key: 'sad', icon: '😢', label: '有点失望' },
      { key: 'angry', icon: '😡', label: '很生气' }
    ],
    popularTags: ['环境好', '服务棒', '性价比高', '排队久', '味道赞', '推荐', '踩雷']
  },
  
  onLoad(options) {
    if (!options.facilityId) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }
    
    this.setData({ facilityId: options.facilityId });
    this.loadFacilityInfo();
    this.checkCanSubmit();
  },
  
  // 加载设施信息
  loadFacilityInfo() {
    request({
      url: `/api/facility/${this.data.facilityId}`
    }).then((data) => {
      this.setData({ facility: data });
      wx.setNavigationBarTitle({ title: `评价${data.name}` });
    }).catch(() => {
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },
  
  // 评分选择
  onRatingSelect(e) {
    const rating = e.currentTarget.dataset.rating;
    const ratingTexts = ['', '很差', '较差', '一般', '不错', '超棒'];
    this.setData({ 
      rating,
      ratingText: ratingTexts[rating]
    });
    this.checkCanSubmit();
  },
  
  // 心情选择
  onMoodSelect(e) {
    const mood = e.currentTarget.dataset.mood;
    this.setData({ 
      mood: this.data.mood === mood ? '' : mood 
    });
  },
  
  // 内容输入
  onContentInput(e) {
    this.setData({ content: e.detail.value });
    this.checkCanSubmit();
  },
  
   // 检查是否可以提交
   checkCanSubmit() {
    const hasRating = this.data.rating >= 1 && this.data.rating <= 5;
    this.setData({ canSubmit: hasRating });
  },
  
  // 选择图片
  chooseImages() {
    wx.chooseMedia({
      count: 9 - this.data.images.length,
      mediaType: ['image'],
      success: (res) => {
        const newImages = res.tempFiles.map(f => f.tempFilePath);
        this.setData({ images: [...this.data.images, ...newImages] });
      }
    });
  },
  
  // 预览图片
  previewImage(e) {
    const index = e.currentTarget.dataset.index;
    wx.previewImage({
      current: this.data.images[index],
      urls: this.data.images
    });
  },
  
  // 删除图片
  deleteImage(e) {
    const index = e.currentTarget.dataset.index;
    const images = this.data.images.filter((_, i) => i !== index);
    this.setData({ images });
  },
  
  // 上传图片到服务器
  uploadImages() {
    if (this.data.images.length === 0) {
      return Promise.resolve([]);
    }
    
    const token = wx.getStorageSync('token');
    const uploadPromises = this.data.images.map((filePath, index) => {
      return new Promise((resolve, reject) => {
        console.log(`开始上传第 ${index + 1} 张图片:`, filePath);
        
        wx.uploadFile({
          url: `${BASE_URL}/api/upload/image`,
          filePath: filePath,
          name: 'file',
          formData: { type: 'evaluation' },
          header: { 'Authorization': `Bearer ${token}` },
          success: (res) => {
            console.log(`第 ${index + 1} 张图片上传响应:`, res);
            
            try {
              const data = JSON.parse(res.data);
              console.log(`第 ${index + 1} 张图片解析后:`, data);
              
              if (data.code === 200 && data.data) {
                resolve(data.data);
              } else {
                console.error(`第 ${index + 1} 张图片上传失败:`, data.msg);
                reject(new Error(data.msg || '图片上传失败'));
              }
            } catch (e) {
              console.error(`第 ${index + 1} 张图片响应解析失败:`, e);
              reject(new Error('服务器响应解析失败'));
            }
          },
          fail: (err) => {
            console.error(`第 ${index + 1} 张图片请求失败:`, err);
            reject(err);
          }
        });
      });
    });
    
    return Promise.all(uploadPromises);
  },

  
  // 标签选择
  onTagSelect(e) {
    const tag = e.currentTarget.dataset.tag;
    const tags = this.data.tags.indexOf(tag) > -1
      ? this.data.tags.filter(t => t !== tag)
      : [...this.data.tags, tag];
    this.setData({ tags });
    
  },
  
  // 添加自定义标签
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
          
          // 检查是否已存在
          const allTags = [...this.data.popularTags, ...this.data.customTags];
          if (allTags.includes(tagName)) {
            wx.showToast({ title: '标签已存在', icon: 'none' });
            return;
          }
          
          // 添加到自定义标签列表并选中
          const customTags = [...this.data.customTags, tagName];
          const tags = [...this.data.tags, tagName];
          
          this.setData({ 
            customTags,
            tags 
          });
          
          // wx.vibrateShort({ type: 'light' });
          wx.showToast({ title: '添加成功', icon: 'success' });
        }
      }
    });
  },
  
  // 删除标签
  deleteTag(e) {
    const tag = e.currentTarget.dataset.tag;
    const isCustom = this.data.customTags.includes(tag);
    
    if (isCustom) {
      // 自定义标签：直接删除
      const customTags = this.data.customTags.filter(t => t !== tag);
      const tags = this.data.tags.filter(t => t !== tag);
      
      this.setData({ 
        customTags,
        tags 
      });
    } else {
      // 预设标签：取消选中
      const tags = this.data.tags.filter(t => t !== tag);
      this.setData({ tags });
    }
    
  },
  
      // 提交评价
  async onSubmit() {
    if (!this.data.canSubmit) {
      wx.showToast({ title: '请先完成评分', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '发布中...' });

    try {
      // 上传图片
      let imageUrls = [];
      if (this.data.images.length > 0) {
        try {
          imageUrls = await this.uploadImages();
          console.log('所有图片上传成功，URL列表:', imageUrls);

          // 检查是否有null值
          if (imageUrls.some(url => !url)) {
            throw new Error('部分图片上传失败');
          }
        } catch (uploadErr) {
          wx.hideLoading();
          console.error('图片上传失败:', uploadErr);
          wx.showToast({ title: '图片上传失败，请重试', icon: 'none' });
          return;
        }
      }

      // 构建提交数据
      const submitData = {
        type: 'EVALUATION',
        facilityId: this.data.facilityId,
        rating: this.data.rating,
        mood: this.data.mood,
        content: this.data.content.trim() || null
      };

      // 只有有图片时才添加images字段
      if (imageUrls.length > 0) {
        submitData.images = JSON.stringify(imageUrls);
      }

      // 只有有标签时才添加tags字段
      if (this.data.tags.length > 0) {
        submitData.tags = JSON.stringify(this.data.tags);
      }

      console.log('提交评价数据:', submitData);

      // 提交评价
      await request({
        url: '/api/evaluation/publish',
        method: 'POST',
        data: submitData
      });

      wx.hideLoading();
      wx.showToast({ title: '发布成功', icon: 'success' });

      // 设置刷新标识，通知上游页面需要刷新
      wx.setStorageSync('needRefresh', true);

      setTimeout(() => {
        wx.navigateBack();
      }, 1500);

    } catch (err) {
      wx.hideLoading();
      console.error('发布失败:', err);
      wx.showToast({ title: '发布失败，请重试', icon: 'none' });
    }
  }

});