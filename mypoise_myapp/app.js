App({
  globalData: {
    userInfo: null,
    isLogin: false,
    baseUrl: 'http://your-api-domain.com'  // 替换为你的API域名
  },

  onLaunch() {
    // 启动时检查登录状态
    this.checkLoginStatus();
  },

  // 检查登录状态
  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    
    if (token && userInfo) {
      this.globalData.isLogin = true;
      this.globalData.userInfo = userInfo;
      return true;
    }
    return false;
  },

  // 保存用户登录信息
  saveUserInfo(userInfo) {
    if (!userInfo) return false;
    
    try {
      // 保存到 Storage
      wx.setStorageSync('userInfo', userInfo);
      wx.setStorageSync('token', userInfo.token);
      
      // 更新全局数据
      this.globalData.userInfo = userInfo;
      this.globalData.isLogin = true;
      
      return true;
    } catch (error) {
      console.error('保存用户信息失败:', error);
      return false;
    }
  },

  // 清除用户登录信息
  clearUserInfo() {
    try {
      // 清除 Storage
      wx.removeStorageSync('token');
      wx.removeStorageSync('userInfo');
      
      // 清除全局数据
      this.globalData.userInfo = null;
      this.globalData.isLogin = false;
      
      return true;
    } catch (error) {
      console.error('清除用户信息失败:', error);
      return false;
    }
  },

  // 获取当前登录用户信息
  getCurrentUser() {
    if (!this.globalData.isLogin) {
      this.checkLoginStatus();
    }
    return this.globalData.userInfo;
  },

  // 判断是否已登录
  isUserLoggedIn() {
    if (!this.globalData.isLogin) {
      return this.checkLoginStatus();
    }
    return this.globalData.isLogin;
  }
}) 