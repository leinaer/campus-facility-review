// pages/admin/statistics.js
const { request } = require('../../utils/request');

Page({
  data: {
    overview: {
      todayEvaluations: 0,
      todayUsers: 0,
      totalFacilities: 0,
      totalEvaluations: 0,
      totalUsers: 0
    },
    evaluationTrend: [],
    categoryStats: [],
    topFacilities: [],
    maxTrendCount: 1,
    categoryTotal: 0,
    donutGradient: '',
    trendBars: [] // 新增：存储预计算的条形高度
  },

  onLoad() {
    this.loadOverview();
    this.loadEvaluationTrend();
    this.loadCategoryStats();
    this.loadTopFacilities();
  },

  onShow() {
    this.loadOverview();
  },

  loadOverview() {
    request({ url: '/api/statistics/admin/overview' }).then((data) => {
      this.setData({ overview: data });
    }).catch((err) => {
      console.error('加载统计数据失败:', err);
    });
  },

  loadEvaluationTrend() {
    request({ url: '/api/statistics/admin/evaluation-trend', data: { days: 7 } }).then((data) => {
      const trendData = data || [];
      const maxCount = Math.max(...trendData.map(item => item.count || 0), 1);
      
      // 预先计算每个条形的高度百分比
      const trendBars = trendData.map(item => ({
        height: ((item.count || 0) / maxCount * 100).toFixed(1)
      }));
      
      this.setData({ 
        evaluationTrend: trendData,
        maxTrendCount: maxCount,
        trendBars: trendBars
      });
    }).catch((err) => {
      console.error('加载评价趋势失败:', err);
    });
  },

  loadCategoryStats() {
    request({ url: '/api/statistics/admin/category-stats' }).then((data) => {
      const categoryData = data || [];
      const total = categoryData.reduce((sum, item) => sum + (item.facilityCount || 0), 0);
      
      const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#43e97b', '#fa709a', '#fee140'];
      let cumulativePercent = 0;
      
      const categoryWithPercent = categoryData.map((item, index) => {
        const percent = total > 0 ? ((item.facilityCount || 0) / total * 100) : 0;
        const startPercent = cumulativePercent;
        cumulativePercent += percent;
        
        return {
          ...item,
          percent: percent.toFixed(1),
          color: colors[index % colors.length],
          startPercent: startPercent.toFixed(1),
          endPercent: cumulativePercent.toFixed(1)
        };
      });
      
      // 计算环形图渐变字符串
      const gradientParts = categoryWithPercent.map(cat => 
        `${cat.color} ${cat.startPercent}% ${cat.endPercent}%`
      );
      const donutGradient = gradientParts.length > 0 
        ? `background: conic-gradient(${gradientParts.join(', ')});`
        : 'background: #e0e0e0;';
      
      this.setData({ 
        categoryStats: categoryWithPercent,
        categoryTotal: total,
        donutGradient: donutGradient
      });
    }).catch((err) => {
      console.error('加载分类统计失败:', err);
    });
  },


  loadTopFacilities() {
    request({ url: '/api/statistics/top-facilities', data: { limit: 10 } }).then((data) => {
      this.setData({ topFacilities: data || [] });
    }).catch((err) => {
      console.error('加载热门设施失败:', err);
    });
  }
});
