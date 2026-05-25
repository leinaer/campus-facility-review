// utils/constants.js

// Element Plus图标名称到Emoji的映射表
const ICON_MAPPING = {
  'Food': '🍜',
  'Reading': '📖',
  'School': '🏫',
  'Basketball': '🏀',
  'FirstAidKit': '🏥',
  'ShoppingCart': '🛒',
  'Coffee': '☕',
  'OfficeBuilding': '🏢',
  'Van': '📦',
  'Postcard': '🎭',
  'House': ''
};

// 默认分类配置
const DEFAULT_CATEGORIES = [
  { id: 1, name: '教学楼', icon: '', color: '#FF6B6B' },
  { id: 2, name: '食堂', icon: '🍜', color: '#4ECDC4' },
  { id: 3, name: '图书馆', icon: '📖', color: '#45B7D1' },
  { id: 4, name: '体育馆', icon: '🏀', color: '#96CEB4' },
  { id: 5, name: '宿舍', icon: '🏠', color: '#FFEAA7' },
  { id: 6, name: '实验室', icon: '🔬', color: '#A29BFE' }
];

// 分页配置
const PAGE_SIZE = 10;

// 图片上传限制
const UPLOAD_CONFIG = {
  maxSize: 5 * 1024 * 1024, // 5MB
  maxCount: 9,
  allowedTypes: ['jpg', 'jpeg', 'png', 'gif']
};

// 图标转换函数：将Element Plus图标名称转换为Emoji
function convertIcon(iconName) {
  if (!iconName) return '';
  
  // 如果已经是Emoji（包含特殊字符），直接返回
  if (/[\u{1F300}-\u{1F9FF}]|[\u{2600}-\u{27BF}]|[\u{FE00}-\u{FE0F}]/u.test(iconName)) {
    return iconName;
  }
  
  // 从映射表中查找对应的Emoji
  return ICON_MAPPING[iconName] || '📋';
}

module.exports = {
  DEFAULT_CATEGORIES,
  PAGE_SIZE,
  UPLOAD_CONFIG,
  ICON_MAPPING,
  convertIcon
};
