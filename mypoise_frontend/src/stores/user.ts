import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 1. 用户状态
  const loginUser = ref<any>({
    userName: '未登录',
  })
  // 2. 获取登录用户信息
  async function fetchLoginUser() {
    // todo
    // const res = await getCurrent()
    // if (res.data.code === 0 && res.data.data) {
    //   loginUser.value = res.data.data
    // }
  }
  // 3. 设置用户信息
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})
