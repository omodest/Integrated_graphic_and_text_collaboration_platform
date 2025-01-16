<template>
  <a-card title="个人信息展示" class="user-edit-form">
    <a-form
      :model="form"
      :label-align="'right'"
      :label-col="6"
      :wrapper-col="16"
    >
      <!-- 用户头像 -->
      <a-form-item label="用户头像">
          <a-image  :src="form.userAvatar" alt="用户头像" class="avatar-preview-img" width="20px"/>
      </a-form-item>
      <!-- 昵称 -->
      <a-form-item label="昵称">&nbsp;&nbsp;
        <a-input v-model:value="form.userName" style="width: 150px"></a-input>
      </a-form-item>
      <!-- 用户名 -->
      <a-form-item label="用户名">
        <a-input v-model:value="form.userAccount" style="width: 150px"></a-input>
      </a-form-item>
      <!-- 简介 -->
      <a-form-item label="简介">&nbsp;&nbsp;
        <a-textarea v-model:value="form.userProfile" style="width: 300px"></a-textarea>
      </a-form-item>
      <!-- 用户权限 -->
      <a-form-item label="用户权限">
        <div v-if="form.userRole === 'admin'" class="form-text">
          <a-tag color="red">{{form.userRole}}</a-tag>
        </div>
        <div v-else-if="form.userRole === 'vip' || form.userRole === 'fvip'" class="form-text">
          <a-tag color="yellow">{{form.userRole}}</a-tag>
        </div>
        <div v-else>
          <a-tag color="blue">user</a-tag>
        </div>
      </a-form-item>
      <!-- 权限过期时间 -->
      <a-form-item label="权限过期时间">
        <div v-if="form.vip_expire" class="form-text">
          {{ form.vip_expire.substring(0, 10) }}
          {{ form.vip_expire.substring(11, 19)}}
        </div>
        <div v-else>
          永久
        </div>
      </a-form-item>

      <!-- 邮箱 -->
      <a-form-item label="用户邮箱">
        <div class="form-text">
          {{ form.email }}&nbsp;&nbsp;
          <a-button type="primary" @click="bindEmail">绑定邮箱</a-button>
        </div>
      </a-form-item>
      <!-- 模态框 -->
      <a-modal
        v-model:open="isModalVisible"
        title="输入内容"
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <a-input v-model:value="inputValue" placeholder="请输入正确QQ邮箱" style="width: 300px"/>
        &nbsp;&nbsp;
        <a-button type="primary" @click="getCaptcha" :disabled="isButtonDisabled">获取验证码</a-button>
        <br/>
        <br/>
        <a-input v-model:value="captchaValue" placeholder="请输入验证码" style="width: 300px"/>
      </a-modal>

      <a-form-item label="微信是否绑定">
        <div v-if="!form.wechat_id" class="form-text">
          未绑定
          <!--  todo <a-button>绑定微信</a-button>-->
        </div>
        <div v-else>
          已绑定
        </div>
      </a-form-item>

      <a-form-item label="注册时间">
        <div v-if="form.createTime" class="form-text">
          {{ form.createTime.substring(0, 10) }}
          {{ form.createTime.substring(11, 19)}}
        </div>
      </a-form-item>

      <a-form-item label="已签到：">
        {{getSignInNum}}
        &nbsp;&nbsp;&nbsp;
        <a-button type="primary" @click="doSignIn" >签到</a-button>
      </a-form-item>

      <!-- 提交和重置按钮 -->
      <a-form-item>
        <a-button type="primary" :loading="isSubmitting" @click="handleSubmit" class="submit-btn">提交</a-button>
        &nbsp;&nbsp;
        <a-button type="primary" @click="emailEdit" class="submit-btn">修改密码</a-button>
        <!-- 模态框 -->
        <a-modal
          v-model:open="isPasswordModalVisible"
          title="输入内容"
          @ok="handlePWDOk"
          @cancel="handlePWDCancel"
        >
          <a-input v-model:value="inputValue" placeholder="请输入正确QQ邮箱" style="width: 300px"/>
          &nbsp;&nbsp;
          <a-button type="primary" @click="getCaptchaRepeat" :disabled="isButtonDisabled">获取验证码</a-button>
          <br/>
          <br/>
          <a-input v-model:value="passwordValue" placeholder="请输入密码" style="width: 300px"/>
          <br/>
          <br/>
          <a-input v-model:value="captchaValue" placeholder="请输入验证码" style="width: 300px"/>
        </a-modal>
        &nbsp;&nbsp;
      </a-form-item>

      <!-- 返回主页按钮 -->
      <a-form-item>
        <a-button type="default" @click="goHome" class="back-btn">
          返回主页
        </a-button>
      </a-form-item>
    </a-form>
  </a-card>
</template>
<script setup lang="ts">
import {ref, onMounted, reactive} from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import {
  doSignUsingPost,
  getCaptchaUsingGet,
  getLoginUserUsingGet, getSignNumUsingGet,
  userEditMyVoUsingPost,
  userEmailBindUsingPost, userEmailEditPasswordUsingPost
} from "@/api/userController";

// 获取 router 实例
const router = useRouter();
// 用户表单数据
const form = ref<API.UserInfoVO>({});
// 提交时的加载状态
const isSubmitting = ref(false);
// 在组件挂载时获取用户数据
onMounted(() => {
  fetchUserData();
  handleSignIn();
});
// 返回主页的函数
const goHome = () => {
  router.push('/');
};
// 存储上传的文件列表
const fileList = ref([]);

// 1. 获取用户信息
const fetchUserData = async () => {
  try {
    const response = await getLoginUserUsingGet();
    form.value.userAvatar = response.data.data?.userAvatar || '';
    form.value.userName = response.data.data?.userName || '';
    form.value.userAccount = response.data.data?.userAccount || '';
    form.value.userProfile = response.data.data?.userProfile || '';
    form.value.userRole = response.data.data?.userRole || '';
    form.value.vip_expire = response.data.data?.vip_expire || '';
    form.value.email = response.data.data?.email || '';
    form.value.wechat_id = response.data.data?.wechat_id || '';
    form.value.createTime = response.data.data?.createTime || '';
    form.value.id = response.data.data?.id || '';

    fileList.value.shift();
    fileList.value.push(response.data.data?.userAvatar || '');
  } catch (error) {
    message.error('获取用户数据失败');
  }
};

// 2. 提交表单
const handleSubmit = async () => {
  try {
    isSubmitting.value = true;
    await userEditMyVoUsingPost(form.value);
    message.success('用户信息更新成功！');
  } catch (error) {
    message.error('更新失败，请稍后再试');
  } finally {
    isSubmitting.value = false;
  }
};

// 3. todo 文件校验上传
// console.log(fileList.value)
// const beforeUpload = async (file: File) => {
//   const response = await (file, 'user_avatar');
//   if (response.code === 0) {
//     form.value.userAvatar = response.data;
//     message.success('上传成功');
//
//     await updateMyUserUsingPost(form.value);
//   } else {
//     message.error('上传失败');
//   }
//
//   // 模拟刷新页面
//   history.pushState(null, '', location.href);
//   history.back();
//
//   return true;
// };
//
// const handleCustomUpload = ({ onSuccess }: any) => {
//   onSuccess(); // 标记上传成功，实际上不会上传文件
// };

// 4. 邮箱换绑
const isModalVisible = ref(false); // 控制模态框的显隐
const isButtonDisabled = ref(false);
const inputValue = ref(''); // 绑定输入框的值
const captchaValue = ref('')
// 打开输入框
const bindEmail = () => {
  showModal();
}
// 显示模态框
const showModal = () => {
  isModalVisible.value = true;
};
// 获取验证码
const getCaptcha = async () => {
  const response = await getCaptchaUsingGet({
    email: inputValue.value,
    repeat: false
  });
  isButtonDisabled.value = true;
  if (response.data.code === 0){
    message.success("验证码发送成功")
  }else {
    message.error(response.data.code)
  }
}

const getCaptchaRepeat = async () => {
  const response = await getCaptchaUsingGet({
    email: inputValue.value,
    repeat: true
  });
  isButtonDisabled.value = true;
  if (response.data.code === 0){
    message.success("验证码发送成功")
  }else {
    message.error(response.data.code)
  }
}
// 确认按钮操作
const handleOk = async () => {
  const response = await userEmailBindUsingPost({
    email: inputValue.value,
    captcha: captchaValue.value
  })
  if (response.data.code === 0){
    message.success("绑定成功")
  }else {
    message.error(response.data.message);
  }

  isButtonDisabled.value = false;
  isModalVisible.value = false;
};
// 取消按钮操作
const handleCancel = () => {
  isModalVisible.value = false;
};

// 5. 修改密码
const isPasswordModalVisible = ref(false); // 控制模态框的显隐
const isPasswordButtonDisabled = ref(false);
const passwordValue = ref(''); // 绑定输入框的值
// 打开输入框
const emailEdit = () => {
  isPasswordModalVisible.value = true
}

// 确认按钮操作
const handlePWDOk = async () => {
  const response = await userEmailEditPasswordUsingPost({
    email: inputValue.value,
    captcha: captchaValue.value,
    userPassword:passwordValue.value
  })
  if (response.data.code === 0){
    message.success("修改成功")
  }else {
    message.error(response.data.message);
  }
  isPasswordButtonDisabled.value = false;
  isPasswordModalVisible.value = false;
};
// 取消按钮操作
const handlePWDCancel = () => {
  isPasswordModalVisible.value = false;
};

// 5. 签到
const getSignInNum = ref(0);
// 拿到签到天数
const handleSignIn = async () => {
  const response = await getSignNumUsingGet()
  if (response.data.code === 0){
    getSignInNum.value = response.data.data
  }
}
// 执行签到操作
const doSignIn = async () => {
  const respnse = await doSignUsingPost();
  if (respnse.data.code === 0){
    message.success("签到成功")
  }else {
    message.error(respnse.data.message)
  }
}
// todo 7. 微信绑定 （来一个图片二维码、微信扫码绑定）
</script>
<style scoped>
/* 整体布局 */
.user-edit-form {
  max-width: 600px;
  margin: auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

a-form-item {
  margin-bottom: 20px;
}

/* 输入框样式 */
a-input,
a-textarea {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  box-shadow: none;
  transition: border-color 0.3s;
}

a-input:focus,
a-textarea:focus {
  border-color: #40a9ff;
  box-shadow: 0 0 5px rgba(24, 144, 255, 0.2);
}

/* 用户头像预览 */
.avatar-preview {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}

.avatar-preview-img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #f0f0f0;
}

/* 文本展示 */
.form-text {
  font-size: 14px;
  color: #595959;
}

/* 按钮样式 */
.submit-btn,
.reset-btn,
.back-btn {
  width: 100px;
  border-radius: 4px;
  padding: 8px 16px;
  font-weight: 600;
}

.submit-btn {
  background-color: #1890ff;
  color: white;
  border: none;
}

.submit-btn:hover {
  background-color: #40a9ff;
}

.reset-btn {
  background-color: #f5f5f5;
  color: #595959;
  border: 1px solid #d9d9d9;
}

.reset-btn:hover {
  background-color: #e6e6e6;
}

.back-btn {
  border: 1px solid #d9d9d9;
  background-color: #fff;
  color: #595959;
}

.back-btn:hover {
  background-color: #f0f0f0;
}

/* 提示文本样式 */
.upload-tip {
  font-size: 12px;
  color: #888;
  margin-top: 5px;
}
</style>
