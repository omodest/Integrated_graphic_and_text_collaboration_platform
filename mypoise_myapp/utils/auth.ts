import { ref } from 'vue';

// 用户信息
export const userInfo = ref<any>(null);

// 检查登录状态
export const checkLogin = () => {
  const token = uni.getStorageSync('token');
  return !!token;
};

// 微信登录
export const wxLogin = () => {
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: async (loginRes) => {
        try {
          // 获取code
          const code = loginRes.code;
          // 调用后端接口进行登录
          const res = await uni.request({
            url: 'your-api-url/login',
            method: 'POST',
            data: {
              code
            }
          });
          
          // 保存token
          const { token, userInfo: user } = res.data;
          uni.setStorageSync('token', token);
          userInfo.value = user;
          
          resolve(res.data);
        } catch (error) {
          reject(error);
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
};

// 退出登录
export const logout = () => {
  uni.removeStorageSync('token');
  userInfo.value = null;
};

// 获取用户信息
export const getUserInfo = async () => {
  try {
    const res = await uni.request({
      url: 'your-api-url/user/info',
      header: {
        Authorization: uni.getStorageSync('token')
      }
    });
    userInfo.value = res.data;
    return res.data;
  } catch (error) {
    console.error('获取用户信息失败', error);
    return null;
  }
}; 