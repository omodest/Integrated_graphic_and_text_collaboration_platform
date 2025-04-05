const { request } = require('../../utils/request');
const app = getApp();

Page({
  data: {
    isLogin: false,
    userInfo: null,
    userAccount: '',
    password: '',
    canLogin: false
  },

  onLoad() {
    this.checkLoginStatus();
  },

  onShow() {
    this.checkLoginStatus();
  },

  // 检查登录状态
  checkLoginStatus() {
    const isLogin = app.isUserLoggedIn();
    const userInfo = app.getCurrentUser();
    
    this.setData({
      isLogin,
      userInfo,
      userAccount: '',
      password: '',
      canLogin: false
    });
  },

  // 监听账号输入
  onAccountInput(e) {
    const userAccount = e.detail.value;
    this.setData({
      userAccount,
      canLogin: userAccount.length > 0 && this.data.password.length > 0
    });
  },

  // 监听密码输入
  onPasswordInput(e) {
    const password = e.detail.value;
    this.setData({
      password,
      canLogin: password.length > 0 && this.data.userAccount.length > 0
    });
  },

  // 处理登录
  async handleLogin() {
    const { userAccount, password } = this.data;
    
    if (!userAccount || !password) {
      wx.showToast({
        title: '请输入账号和密码',
        icon: 'none'
      });
      return;
    }

    try {
      wx.showLoading({
        title: '登录中...',
        mask: true
      });

      const res = await request({
        url: '/api/user/login',
        method: 'POST',
        data: {
          userAccount,
          userPassword: password
        }
      });

      wx.hideLoading();

      if (res.data.code === 0) {
        const userInfo = res.data.data;
        
        // 使用全局方法保存用户信息
        if (app.saveUserInfo(userInfo)) {
          // 更新页面状态
          this.setData({
            isLogin: true,
            userInfo: userInfo
          });

          wx.showToast({
            title: '登录成功',
            icon: 'success'
          });
        } else {
          wx.showToast({
            title: '登录信息保存失败',
            icon: 'none'
          });
        }
      } else {
        wx.showToast({
          title: res.data.message || '登录失败',
          icon: 'none'
        });
      }
    } catch (error) {
      wx.hideLoading();
      wx.showToast({
        title: '登录失败，请重试',
        icon: 'none'
      });
    }
  },

  // 处理退出登录
  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 使用全局方法清除用户信息
          if (app.clearUserInfo()) {
            // 更新页面状态
            this.setData({
              isLogin: false,
              userInfo: null,
              userAccount: '',
              password: '',
              canLogin: false
            });

            wx.showToast({
              title: '已退出登录',
              icon: 'success'
            });
          } else {
            wx.showToast({
              title: '退出登录失败',
              icon: 'none'
            });
          }
        }
      }
    });
  },

  // 跳转到注册页面
  goToRegister() {
    wx.navigateTo({
      url: '/pages/register/index'
    });
  },

  // 查看用户协议
  handleUserAgreement() {
    wx.navigateTo({
      url: '/pages/agreement/user'
    });
  },

  // 查看隐私政策
  handlePrivacyPolicy() {
    wx.navigateTo({
      url: '/pages/agreement/privacy'
    });
  }
}); 