import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUserUsingGet } from '@/api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 1. 用户状态
  const loginUser = ref<API.UserInfoVO>({
    userName: '未登录',
  })
  // 2. 获取登录用户信息
  async function fetchLoginUser() {
    const res = await getLoginUserUsingGet()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }
  // 3. 设置用户信息
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})
