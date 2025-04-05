// pages/publicAlbum/index.js
const { request } = require('../../utils/request');

Page({

  /**
   * 1. 页面的初始数据
   */
  data: {
    imageList: [],
    loadMoreStatus: 'more', // more-加载前 loading-加载中 noMore-没有更多
    current: 1,
    pageSize: 10,
    columnWidth: 0, // 列宽
    searchKeyword: '', // 搜索关键词
    searchTimer: null // 用于防抖
  },

  /**
   * 2. 生命周期函数--监听页面加载（初始化数据）
   */
  onLoad(options) {
    // 获取屏幕宽度，计算列宽
    const systemInfo = wx.getSystemInfoSync();
    const screenWidth = systemInfo.windowWidth;
    const columnWidth = (screenWidth - 40) / 2; // 40是左右padding的总和
    this.setData({ columnWidth });
    
    this.getImageList(true);
  },

  /**
   * 3. 页面相关事件处理函数--监听用户下拉动作(页面刷新重新加载数据)
   */
  async onPullDownRefresh() {
    await this.getImageList(true);
    wx.stopPullDownRefresh();
  },

  /**
   * 4. 页面上拉触底事件的处理函数(页面刷新重新加载数据)
   */
  onReachBottom() {
    if (this.data.loadMoreStatus === 'more') {
      this.getImageList();
    }
  },

  // 5. 搜索输入
  onSearchInput(e) {
    const keyword = e.detail.value;
    this.setData({ searchKeyword: keyword });
    
    // 防抖处理
    if (this.data.searchTimer) {
      clearTimeout(this.data.searchTimer);
    }
    
    this.data.searchTimer = setTimeout(() => {
      this.getImageList(true);
    }, 500);
  },

  // 6. 搜索确认
  onSearchConfirm() {
    this.getImageList(true);
  },

  // 7. 清除搜索
  clearSearch() {
    this.setData({ searchKeyword: '' });
    this.getImageList(true);
  },

  // 8. 获取图片列表
  async getImageList(isRefresh = false) {
    // 防止重复加载图片列表，只有more才会加载
    if (this.data.loadMoreStatus === 'loading') return;
    
    this.setData({
      loadMoreStatus: 'loading'
    });

    if (isRefresh) {
      this.setData({
        current: 1
      });
    }

    try {
      const res = await request({
        url: '/api/picture/list/page/vo',
        method: 'POST',
        data: {
          current: this.data.current,
          pageSize: this.data.pageSize,
          spaceId: null, // 不传spaceId表示查询公共图库
          sortField: 'createTime',
          sortOrder: 'desc',
          searchText: this.data.searchKeyword || undefined, // 搜索关键词
        }
      });
      const responseData = res.data;
      if (responseData.code === 0 && responseData.data) {
        const { records, total } = responseData.data;
        
        // 处理图片数据
        const formattedRecords = records.map(item => {
          // 计算图片高度，保持原始比例
          const scale = item.picWidth / item.picHeight;
          const displayHeight = Math.floor(this.data.columnWidth / scale);
          
          return {
            ...item,
            imageUrl: item.url, // 适配瀑布流组件的字段
            height: displayHeight // 根据列宽计算等比例高度
          };
        });
        
        this.setData({
          imageList: isRefresh ? formattedRecords : [...this.data.imageList, ...formattedRecords],
          loadMoreStatus: this.data.imageList.length >= total ? 'noMore' : 'more',
          current: this.data.imageList.length >= total ? this.data.current : this.data.current + 1
        });
      } else {
        wx.showToast({
          title: responseData.message || '加载失败',
          icon: 'none'
        });
      }
    } catch (error) {
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
      this.setData({
        loadMoreStatus: 'more'
      });
    }
  },

  // 处理图片点击
  handleImageClick(e) {
    const item = e.detail;
    wx.previewImage({
      urls: this.data.imageList.map(img => img.url),
      current: item.url
    });
  },

    /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {
  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {
  },

    /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {
  },
})