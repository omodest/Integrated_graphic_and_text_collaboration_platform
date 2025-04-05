const { request } = require('../../utils/request');
const app = getApp();

Page({
  data: {
    userAccount: '',
    password: '',
    email: '',
    captcha: '',
    countdown: 0,
    canSendCaptcha: false,
    canRegister: false
  },

  // 监听账号输入
  onAccountInput(e) {
    const userAccount = e.detail.value;
    this.setData({
      userAccount,
      canRegister: this.checkCanRegister(userAccount, this.data.password, this.data.email, this.data.captcha)
    });
  },

  // 监听密码输入
  onPasswordInput(e) {
    const password = e.detail.value;
    this.setData({
      password,
      canRegister: this.checkCanRegister(this.data.userAccount, password, this.data.email, this.data.captcha)
    });
  },

  // 监听邮箱输入
  onEmailInput(e) {
    const email = e.detail.value;
    this.setData({
      email,
      canSendCaptcha: this.checkEmail(email),
      canRegister: this.checkCanRegister(this.data.userAccount, this.data.password, email, this.data.captcha)
    });
  },

  // 监听验证码输入
  onCaptchaInput(e) {
    const captcha = e.detail.value;
    this.setData({
      captcha,
      canRegister: this.checkCanRegister(this.data.userAccount, this.data.password, this.data.email, captcha)
    });
  },

  // 检查邮箱格式
  checkEmail(email) {
    const emailRegex = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
    return emailRegex.test(email);
  },

  // 检查是否可以注册
  checkCanRegister(account, password, email, captcha) {
    return account.length > 0 && 
           password.length > 0 && 
           this.checkEmail(email) && 
           captcha.length > 0;
  },

  // 发送验证码
  async sendCaptcha() {
    if (!this.data.canSendCaptcha || this.data.countdown > 0) return;

    try {
      wx.showLoading({
        title: '发送中...',
        mask: true
      });

      const res = await request({
        url: '/api/user/captcha',
        method: 'GET',
        data: {
          email: this.data.email,
          repeat: false
        }
      });

      wx.hideLoading();

      if (res.data.code === 0) {
        wx.showToast({
          title: '验证码已发送',
          icon: 'success'
        });

        // 开始倒计时
        this.setData({ countdown: 60 });
        this.startCountdown();
      } else {
        wx.showToast({
          title: res.data.message || '发送失败',
          icon: 'none'
        });
      }
    } catch (error) {
      wx.hideLoading();
      wx.showToast({
        title: '发送失败，请重试',
        icon: 'none'
      });
    }
  },

  // 倒计时
  startCountdown() {
    if (this.data.countdown <= 0) return;

    setTimeout(() => {
      this.setData({
        countdown: this.data.countdown - 1
      });
      this.startCountdown();
    }, 1000);
  },

  // 处理注册
  async handleRegister() {
    const { userAccount, password, email, captcha } = this.data;

    console.log('注册信息：', { userAccount, password, email, captcha });
    console.log('按钮状态：', this.data.canRegister);

    if (!this.data.canRegister) {
      wx.showToast({
        title: '请填写完整信息',
        icon: 'none'
      });
      return;
    }

    try {
      wx.showLoading({
        title: '注册中...',
        mask: true
      });

      const res = await request({
        url: '/api/user/register',
        method: 'POST',
        data: {
          userAccount,
          userPassword: password,
          email,
          captcha
        }
      });

      wx.hideLoading();

      if (res.data.code === 0) {
        wx.showToast({
          title: '注册成功',
          icon: 'success'
        });

        // 延迟跳转到登录页
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      } else {
        wx.showToast({
          title: res.data.message || '注册失败',
          icon: 'none'
        });
      }
    } catch (error) {
      wx.hideLoading();
      wx.showToast({
        title: '注册失败，请重试',
        icon: 'none'
      });
    }
  },

  // 返回登录页
  goToLogin() {
    wx.navigateBack();
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