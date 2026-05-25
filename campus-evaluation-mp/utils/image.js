/**
 * 图片预览工具函数
 */
function previewImages(images, currentIndex) {
  if (!images || images.length === 0) {
    return;
  }

  wx.previewImage({
    urls: images,
    current: images[currentIndex] || images[0]
  });
}

module.exports = {
  previewImages
};
