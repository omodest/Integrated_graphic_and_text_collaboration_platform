const { request } = require('../../utils/request');
const app = getApp();

Page({
  data: {
    isLogin: false,
    hasPrivateSpace: false,
    spaceId: null,
    pictures: [],
    featuredPictures: [], // 精选图片，用于轮播图
    current: 1,
    pageSize: 10,
    loading: false,
    hasMore: true,
    uploading: false
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
          pictures: [],
          featuredPictures: []
        });
      }
    } catch (error) {
      console.error('检查登录状态失败:', error);
      // 发生错误时，默认为未登录状态
      this.setData({
        isLogin: false,
        hasPrivateSpace: false,
        spaceId: null,
        pictures: [],
        featuredPictures: []
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
        // 加载精选图片
        await this.loadFeaturedPictures();
      } else {
        // 没有私人空间
        this.setData({
          hasPrivateSpace: false,
          spaceId: null,
          pictures: [],
          featuredPictures: []
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

  // 加载精选图片（用于轮播图）
  async loadFeaturedPictures() {
    if (!this.data.isLogin || !this.data.hasPrivateSpace) {
      return;
    }

    try {
      // 获取最新的5张图片作为精选图片
      const res = await request({
        url: '/api/picture/list/page/vo',
        method: 'POST',
        data: {
          current: 1,
          pageSize: 5,
          spaceId: this.data.spaceId,
          name: "小程序直通参数",
          userId: app.getUserInfo().id
        }
      });

      if (res.data.code === 0 && res.data.data.records) {
        this.setData({
          featuredPictures: res.data.data.records
        });
      }
    } catch (error) {
      console.error('加载精选图片失败:', error);
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
          name: "小程序直通参数",
          userId: app.getUserInfo().id
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
          spaceName: app.getUserInfo().id,
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

  // 选择图片
  chooseImage() {
    if (!this.data.isLogin || !this.data.hasPrivateSpace) {
      wx.showToast({
        title: '请先登录并创建私人空间',
        icon: 'none'
      });
      return;
    }

    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        console.log('选择图片成功:', res);
        if (res.tempFiles && res.tempFiles.length > 0) {
          // 上传前校验
          if (this.beforeUpload(res.tempFiles[0])) {
            this.uploadImage(res.tempFiles[0]);
          }
        }
      },
      fail: (err) => {
        console.error('选择图片失败:', err);
        wx.showToast({
          title: '选择图片失败',
          icon: 'none'
        });
      }
    });
  },

  // 上传前校验
  beforeUpload(file) {
    // 检查文件类型
    const isJpgOrPng = file.tempFilePath.endsWith('.jpg') || 
                       file.tempFilePath.endsWith('.jpeg') || 
                       file.tempFilePath.endsWith('.png');
    
    if (!isJpgOrPng) {
      wx.showToast({
        title: '不支持上传该格式的图片，推荐 jpg 或 png',
        icon: 'none'
      });
      return false;
    }
    
    // 检查文件大小
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isLt2M) {
      wx.showToast({
        title: '不能上传超过 2M 的图片',
        icon: 'none'
      });
      return false;
    }
    
    return true;
  },

  // 上传图片
  async uploadImage(file) {
    if (this.data.uploading) return;
    
    this.setData({ uploading: true });
    wx.showLoading({
      title: '上传中...',
      mask: true
    });

    try {
      const uploadTask = wx.uploadFile({
        url: app.globalData.baseUrl + '/api/picture/upload/picture',
        filePath: file.tempFilePath,
        name: 'file',
        formData: {
          spaceId: this.data.spaceId
        },
        header: {
          'content-type': 'multipart/form-data',
          'Authorization': app.globalData.token || ''
        },
        success: (res) => {
          console.log('上传图片成功:', res);
          try {
            const data = JSON.parse(res.data);
            if (data.code === 0) {
              wx.showToast({
                title: '上传成功',
                icon: 'success'
              });
              // 刷新图片列表
              this.loadPictureList(true);
              // 刷新精选图片
              this.loadFeaturedPictures();
            } else {
              wx.showToast({
                title: data.message || '上传失败',
                icon: 'none'
              });
            }
          } catch (e) {
            wx.showToast({
              title: '解析响应失败',
              icon: 'none'
            });
          }
        },
        fail: (err) => {
          console.error('上传图片失败:', err);
          wx.showToast({
            title: '上传失败',
            icon: 'none'
          });
        },
        complete: () => {
          wx.hideLoading();
          this.setData({ uploading: false });
        }
      });

      // 监听上传进度
      uploadTask.onProgressUpdate((res) => {
        console.log('上传进度:', res.progress);
      });
    } catch (error) {
      console.error('上传图片失败:', error);
      wx.hideLoading();
      wx.showToast({
        title: error.message || '上传失败',
        icon: 'none'
      });
      this.setData({ uploading: false });
    }
  },

  // 下拉刷新
  async onPullDownRefresh() {
    if (this.data.isLogin && this.data.hasPrivateSpace) {
      await this.loadPictureList(true);
      await this.loadFeaturedPictures();
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