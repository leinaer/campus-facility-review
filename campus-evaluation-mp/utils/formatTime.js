const formatTime = {
  formatRelativeTime: function(timeStr) {
    if (!timeStr) return '';

    const now = new Date();
    const targetTime = new Date(timeStr);
    const diff = now.getTime() - targetTime.getTime();

    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (seconds < 60) {
      return '刚刚';
    } else if (minutes < 60) {
      return `${minutes}分钟前`;
    } else if (hours < 24) {
      return `${hours}小时前`;
    } else if (days < 3) {
      return `${days}天前`;
    } else {
      const targetYear = targetTime.getFullYear();
      const currentYear = now.getFullYear();

      const month = targetTime.getMonth() + 1;
      const day = targetTime.getDate();

      if (targetYear !== currentYear) {
        return `${targetYear}年${month}月${day}日`;
      } else {
        return `${month}月${day}日`;
      }
    }
  },

  formatDateTime: function(timeStr) {
    if (!timeStr) return '';

    const date = new Date(timeStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}`;
  }
};

module.exports = {
  formatRelativeTime: formatTime.formatRelativeTime,
  formatDateTime: formatTime.formatDateTime
};
