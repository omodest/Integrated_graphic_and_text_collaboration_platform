const { request } = require('../../utils/request');
const app = getApp();

Page({
  data: {
    isLogin: false,
    hasPrivateSpace: false,
    spaceId: null,
    pictures: [],
    current: 1,
    pageSize: 10,
    loading: false,
    hasMore: true
  },

  onLoad() {
    this.checkLoginAndInitialize();
  },

  onShow() {
    // 每次页面显示时都重新检查登录状态
    this.checkLoginAndInitialize();
  },

  // 检查登录状态并初始化
  async checkLoginAndInitialize() {
    try {
      // 从全局获取登录状态
      const isLogin = app.isLoggedIn();
      const userInfo = app.getUserInfo();
      console.log('当前登录状态:', isLogin, '用户信息:', userInfo);

      // 更新页面状态
      this.setData({ isLogin });
      
      if (isLogin) {
        await this.loadPrivateSpace();
      } else {
        // 清空数据
        this.setData({
          hasPrivateSpace: false,
          spaceId: null,
          pictures: []
        });
      }
    } catch (error) {
      console.error('检查登录状态失败:', error);
      // 发生错误时，默认为未登录状态
      this.setData({
        isLogin: false,
        hasPrivateSpace: false,
        spaceId: null,
        pictures: []
      });
    }
  },

  // 跳转到登录页面
  goToLogin() {
    wx.switchTab({
      url: '/pages/mine/index'
    });
  },

  // 加载私人空间信息
  async loadPrivateSpace() {
    if (!this.data.isLogin) {
      console.log('未登录，不加载私人空间');
      return;
    }

    try {
      // 获取用户的私人空间
      const spaceRes = await request({
        url: '/api/space/list/page/vo',
        method: 'POST',
        data: {
          current: 1,
          pageSize: 1,
          spaceType: 0
        }
      });

      console.log('获取私人空间结果:', spaceRes);

      if (spaceRes.data.code === 0 && spaceRes.data.data.records && spaceRes.data.data.records.length > 0) {
        // 有私人空间
        this.setData({
          hasPrivateSpace: true,
          spaceId: spaceRes.data.data.records[0].id
        });
        // 加载图片列表
        await this.loadPictureList(true);
      } else {
        // 没有私人空间
        this.setData({
          hasPrivateSpace: false,
          spaceId: null,
          pictures: []
        });
      }
    } catch (error) {
      console.error('加载私人空间失败:', error);
      wx.showToast({
        title: error.message || '加载失败',
        icon: 'none'
      });
    }
  },

  // 加载图片列表
  async loadPictureList(refresh = false) {
    if (this.data.loading || (!refresh && !this.data.hasMore)) return;

    try {
      this.setData({ loading: true });
      
      if (refresh) {
        this.setData({
          current: 1,
          hasMore: true
        });
      }

      const res = await request({
        url: '/api/picture/list/page/vo',
        method: 'POST',
        data: {
          current: this.data.current,
          pageSize: this.data.pageSize,
          spaceId: this.data.spaceId,
          name: "小程序直通参数"
        }
      });
      console.log(res)
      if (res.data.code === 0) {
        const newPictures = res.data.data.records || [];
        this.setData({
          pictures: refresh ? newPictures : [...this.data.pictures, ...newPictures],
          current: this.data.current + 1,
          hasMore: newPictures.length === this.data.pageSize
        });
      }
    } catch (error) {
      console.error('加载图片失败:', error);
      wx.showToast({
        title: '加载图片失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  // 创建私人空间
  async createPrivateSpace() {
    if (!this.data.isLogin) {
      wx.showModal({
        title: '提示',
        content: '请先登录后再创建私人空间',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            this.goToLogin();
          }
        }
      });
      return;
    }

    try {
      wx.showLoading({
        title: '创建中...',
        mask: true
      });

      const res = await request({
        url: '/api/space/add',
        method: 'POST',
        data: {
          spaceName: '这是我的私人空间',
          spaceLevel: 0,
          spaceType: 0
        }
      });

      wx.hideLoading();

      if (res.data.code === 0) {
        wx.showToast({
          title: '创建成功',
          icon: 'success'
        });
        // 重新加载私人空间信息
        await this.loadPrivateSpace();
      } else {
        throw new Error(res.data.message || '创建失败');
      }
    } catch (error) {
      wx.hideLoading();
      wx.showToast({
        title: error.message || '创建失败',
        icon: 'none'
      });
    }
  },

  // 预览图片
  previewImage(e) {
    const url = e.currentTarget.dataset.url;
    if (!url) return;
    
    const urls = this.data.pictures.map(pic => pic.url);
    wx.previewImage({
      urls: urls,
      current: url
    });
  },

  // 下拉刷新
  async onPullDownRefresh() {
    if (this.data.isLogin && this.data.hasPrivateSpace) {
      await this.loadPictureList(true);
    }
    wx.stopPullDownRefresh();
  },

  // 触底加载更多
  onReachBottom() {
    if (this.data.isLogin && this.data.hasPrivateSpace) {
      this.loadPictureList();
    }
  }
}); 