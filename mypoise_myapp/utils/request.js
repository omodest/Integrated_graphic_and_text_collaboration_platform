const app = getApp();
const BASE_URL = 'http://localhost:8101'; // 替换为你的API域名

// 请求函数
const request = (options = {}) => {
  return new Promise((resolve, reject) => {
    // 获取token
    const token = wx.getStorageSync('token');
    
    // 合并请求头
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    };
    
    // 如果有token，添加到请求头
    if (token) {
      header.Authorization = `Bearer ${token}`;
    }

    wx.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header,
      success: (res) => {
        // 如果返回未登录状态码
        if (res.data.code === 40100) {
          // 清除登录信息
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          app.globalData.userInfo = null;
          app.globalData.isLogin = false;

          // 跳转到登录页
          wx.navigateTo({
            url: '/pages/login/index'
          });
          
          reject(new Error('未登录'));
          return;
        }
        resolve(res);
      },
      fail: reject
    });
  });
};

module.exports = {
  request
}; 