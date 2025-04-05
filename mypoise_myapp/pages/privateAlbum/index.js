const { request } = require('../../utils/request');

Page({
  data: {
    hasPrivateSpace: false,
    spaceList: [],
    current: 1,
    pageSize: 10
  },

  onLoad() {
    this.loadPrivateSpace()
  },

    // 1. 创建私人空间(当前登录用户用户没有私人空间)
    async createPrivateSpace() {
      try {
        const res = await request({
          url: `/api/space/add`,
          method: 'POST',
          data: {
            spaceName: '我的私人空间',
            spaceLevel: 0,
            spaceType: 0
          }
        })
        console.log(res)
        if (res.data.code === 0) {
          wx.showToast({
            title: '创建成功',
            icon: 'success'
          })
          // 重新加载数据
          this.loadPrivateSpace()
        } else {
          wx.showToast({
            title: res.data.message || '创建失败',
            icon: 'none'
          })
        }
      } catch (error) {
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    },

  // 2. 加载私人空间数据(当前登录用户)
  async loadPrivateSpace() {
    // 拿到当前登录用户
    // 根据当前用户找到它的私人空间，拿到私人空间id
    // 在图片表根据私人空间进行过滤
    try {
      const res = await request({
        url: `/api/space/list/page/vo`,
        method: 'POST',
        data: {
          current: this.data.current,
          pageSize: this.data.pageSize,
          spaceLevel: 0  // 查询私人空间
        }
      })
      console.log(res)
      if (res.data.code === 0) {
        const { records = [] } = res.data.data
        this.setData({
          spaceList: records,
          hasPrivateSpace: records.length > 0
        })
      } else {
        wx.showToast({
          title: res.data.message || '加载失败',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      })
    }
  },
}) 