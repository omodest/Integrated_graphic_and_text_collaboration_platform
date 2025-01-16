<template>
  <div id="userLoginPage">
    <h2 class="title">云图库 - 用户登录</h2>
    <div class="desc">协同云图库平台</div>

    <!-- Tab 切换 -->
    <a-tabs v-model="activeTab" @change="handleTabChange">
      <a-tab-pane key="account" tab="账号密码登录">
        <!-- 账号密码登录表单 -->
        <a-form :model="formState" name="basic" autocomplete="off" ref="accountForm" @finish="handleSubmit">
          <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
            <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
          </a-form-item>
          <a-form-item
            name="userPassword"
            :rules="[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码不能小于 8 位' },
            ]"
          >
            <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
          </a-form-item>
          <div class="tips">
            没有账号？
            <RouterLink to="/user/register">去注册</RouterLink>
          </div>
        </a-form>
      </a-tab-pane>

      <a-tab-pane key="email" tab="邮箱登录">
        <!-- 邮箱登录表单 -->
        <a-form name="emailLogin" autocomplete="off" ref="emailForm" @finish="handleSubmit">
          <a-form-item name="userEmail">
            <a-input v-model:value="emailValue" placeholder="请输入邮箱" style="width: 240px"/>&nbsp;&nbsp;&nbsp;
            <a-button type="primary" @click="getCaptchaRepeat" :disabled="isButtonDisabled">获取验证码</a-button>
          </a-form-item>
          <a-form-item
            name="captchaValue"
          >
            <a-input v-model:value="captchaValue" placeholder="请输入验证码" />
          </a-form-item>

          <div class="tips">
            没有账号？
            <RouterLink to="/user/register">去注册</RouterLink>
          </div>
        </a-form>
      </a-tab-pane>
    </a-tabs>

    <!-- 共用的登录按钮 -->
    <a-form-item>
      <a-button type="primary" html-type="submit" style="width: 100%" @click="handleSubmit">登录</a-button>
    </a-form-item>
  </div>
</template>
<script setup lang="ts">
import {reactive, ref} from "vue";
import {useRouter} from "vue-router";
import {useLoginUserStore} from "@/stores/user";
import {getCaptchaUsingGet, userLoginByEmailUsingPost, userLoginUsingPost} from "@/api/userController";
import {message} from "ant-design-vue";

// vue-router路由库，实现页面跳转
const router = useRouter();
// 拿到用户输入
const captchaValue = ref()
const emailValue = ref()
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

// 1. 登录按钮
const handleSubmit = async () => {
  console.log(emailValue.value)
  let  response;
  if (emailValue.value !== undefined && captchaValue.value !== undefined){
    response = await userLoginByEmailUsingPost({
      email: emailValue.value,
      captcha: captchaValue.value
    })
  }else {
    response = await userLoginUsingPost(
      formState
    );
  }
  if (response.data.code == 0){
    message.success("欢迎!!!");
    // 刷新下当前用户状态
    await useLoginUserStore().fetchLoginUser();
    // 跳转到主页
    await router.push({
      path: '/',
      replace: true
    })
  }else {
    message.error("账号或密码错误，请重新输入！")
  }
}

// 2. 用户邮箱登录
// 发送验证码
const isButtonDisabled = ref(false);
const getCaptchaRepeat = async () => {
  const response = await getCaptchaUsingGet({
    email: emailValue.value,
    repeat: true
  });
  isButtonDisabled.value = true;
  if (response.data.code === 0){
    message.success("验证码发送成功")
  }else {
    message.error(response.data.code)
  }
}


</script>

<style scoped>
#userLoginPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}

</style>
