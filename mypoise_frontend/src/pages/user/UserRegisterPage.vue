<template>
  <div id="userRegisterPage">
    <h2 class="title">云图库 - 用户注册</h2>
    <div class="desc">协同云图库平台</div>
    <!-- 获取用户输入-->
    <a-form
      :model="formState"
      name="basic"
      label-align="left"
      autocomplete="off"
      @finish="handleSubmit"
    >
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <!-- 添加QQ邮箱字段和发送验证码按钮 -->
      <a-form-item
        name="email"
        :rules="[
          { required: true, message: '请输入邮箱' },
          { type: 'email', message: '请输入有效的邮箱地址' },
          { pattern: /^[a-zA-Z0-9._%+-]+@qq\.com$/, message: '请输入有效的QQ邮箱地址' }
        ]"
      >
        <a-input-group compact>
          <a-input style="width: calc(100% - 120px)" v-model:value="formState.email" placeholder="请输入QQ邮箱" />
          &nbsp;&nbsp;&nbsp;
          <a-button
            type="default"
            @click="sendCaptcha"
            :loading="isSendingCaptcha"
            :disabled="isButtonDisabled"
            style="width: 100px"
          >
            {{ buttonText }}
          </a-button>
        </a-input-group>
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

      <a-form-item
        name="captcha"
        :rules="[
          { required: true, message: '请输入验证码' },
        ]"
      >
        <a-input v-model:value="formState.captcha" placeholder="请输入验证码" />
      </a-form-item>

      <div class="tips">
        已有账号？
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { doEmailRegisterUsingPost, getCaptchaUsingGet } from "@/api/userController";
import { useRouter } from "vue-router";

const router = useRouter();
// 用户注册的表单状态
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  email: "",
  captcha: '',
});

// 控制发送验证码按钮的加载状态
const isSendingCaptcha = ref(false);
// 控制按钮是否禁用
const isButtonDisabled = ref(false);
// 控制按钮的显示文本
const buttonText = ref("发送验证码");

// 发送验证码的函数
const sendCaptcha = async () => {
  // 没输邮箱 直接返回
  console.log(formState.email)
  if (formState.email == "") {
    message.error("请输入邮箱")
    return
  };
  if (isButtonDisabled.value) return; // 防止重复点击

  // 设置按钮为禁用状态，并开始倒计时
  isButtonDisabled.value = true;
  let countdown = 60;
  buttonText.value = `${countdown}秒后重试`;

  // 启动倒计时
  const interval = setInterval(() => {
    countdown--;
    buttonText.value = `${countdown}秒后重试`;
    if (countdown <= 0) {
      clearInterval(interval);
      isButtonDisabled.value = false;
      buttonText.value = "发送验证码"; // 恢复按钮文本
    }
  }, 1000);

  // 调用发送验证码的API
  const flag = await getCaptchaUsingGet({
    email: formState.email,
    repeat: false
  });
  if (flag.data.code === 0) {
    message.success('验证码已发送！');
  } else {
    message.error("错误操作");
  }
};

// 提交表单时的处理函数
const handleSubmit = async () => {
  const response = await doEmailRegisterUsingPost(formState);
  if (response.data.code === 0) {
    message.success("注册成功!");
    await router.push({
      path: "/login/user",
      replace: true
    });
  } else {
    message.error(response.data.message);
  }
};
</script>

<style scoped>
/* 页面样式 */
#userRegisterPage {
  max-width: 500px;
  margin: 0 auto;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
}

.title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 20px;
}

.desc {
  text-align: center;
  color: #888;
  margin-bottom: 30px;
}

.tips {
  text-align: center;
  margin-top: 10px;
}

.tips a {
  color: #1890ff;
}

.a-input-group {
  display: flex;
  align-items: center;
}

.a-input-group .a-input {
  flex: 1;
}

.a-input-group .a-button {
  margin-left: 10px;
}
</style>
