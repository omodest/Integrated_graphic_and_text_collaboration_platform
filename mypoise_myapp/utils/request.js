const app = getApp();
const BASE_URL = 'http://localhost:8101'; // 替换为你的API域名

// 请求函数
const request = (options = {}) => {
  return new Promise((resolve, reject) => {
    // 合并请求头
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    };

    console.log('发送请求:', {
      url: `${BASE_URL}${options.url}`,
      method: options.method,
      data: options.data,
      header
    });

    wx.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header,
      success: (res) => {
        console.log('请求响应:', res);
        resolve(res);
      },
      fail: (error) => {
        console.error('请求失败:', error);
        reject(error);
      }
    });
  });
};

module.exports = {
  request
}; 