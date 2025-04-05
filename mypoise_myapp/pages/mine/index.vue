<template>
  <view class="mine-container">
    <!-- 用户信息区域 -->
    <view class="user-info-section">
      <view v-if="userInfo" class="user-info">
        <image class="avatar" :src="userInfo.avatarUrl" mode="aspectFill" />
        <view class="info">
          <text class="nickname">{{ userInfo.nickName }}</text>
        </view>
      </view>
      <view v-else class="login-btn" @click="handleLogin">
        <text>点击登录</text>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="function-list">
      <view class="function-item" @click="handleMyPhotos">
        <text class="icon">🖼️</text>
        <text class="title">我的照片</text>
        <text class="arrow">></text>
      </view>
      <view class="function-item" @click="handleSettings">
        <text class="icon">⚙️</text>
        <text class="title">设置</text>
        <text class="arrow">></text>
      </view>
    </view>

    <!-- 退出登录按钮 -->
    <view v-if="userInfo" class="logout-btn" @click="handleLogout">
      <text>退出登录</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { userInfo, wxLogin, logout, getUserInfo } from '@/utils/auth';

// 处理登录
const handleLogin = async () => {
  try {
    await wxLogin();
    uni.showToast({
      title: '登录成功',
      icon: 'success'
    });
  } catch (error) {
    uni.showToast({
      title: '登录失败',
      icon: 'none'
    });
  }
};

// 处理退出登录
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        logout();
        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        });
      }
    }
  });
};

// 处理我的照片点击
const handleMyPhotos = () => {
  if (!userInfo.value) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    });
    return;
  }
  uni.navigateTo({
    url: '/pages/privateAlbum/index'
  });
};

// 处理设置点击
const handleSettings = () => {
  uni.navigateTo({
    url: '/pages/settings/index'
  });
};

// 页面加载时获取用户信息
onMounted(() => {
  getUserInfo();
});
</script>

<style scoped>
.mine-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.user-info-section {
  background-color: #ffffff;
  padding: 40rpx;
  margin-bottom: 20rpx;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  margin-right: 20rpx;
}

.info .nickname {
  font-size: 32rpx;
  font-weight: bold;
}

.login-btn {
  text-align: center;
  padding: 20rpx;
  font-size: 32rpx;
  color: #007AFF;
}

.function-list {
  background-color: #ffffff;
}

.function-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.function-item .icon {
  margin-right: 20rpx;
  font-size: 40rpx;
}

.function-item .title {
  flex: 1;
  font-size: 28rpx;
}

.function-item .arrow {
  color: #999;
}

.logout-btn {
  margin: 40rpx;
  background-color: #ffffff;
  text-align: center;
  padding: 20rpx;
  border-radius: 8rpx;
  color: #ff4d4f;
}
</style> 