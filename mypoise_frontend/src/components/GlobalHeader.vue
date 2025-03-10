<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <!--  左侧图表、标题   -->
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="title">云图库</div>
          </div>
        </RouterLink>
      </a-col>
      <!-- 菜单项    -->
      <a-col flex="auto">
        <a-menu v-model:selectedKeys="current" mode="horizontal" >
          <!-- 使用 v-for 渲染菜单项 -->
          <RouterLink v-for="(menu, index) in items" :key="index" :to="menu.path">
            <a-menu-item>
              {{ menu.name }}
            </a-menu-item>
          </RouterLink>
        </a-menu>
      </a-col>
      <!-- 用户信息展示-->
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <ASpace>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </ASpace>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="personalCenter">
                    <UserOutlined />
                    个人中心
                  </a-menu-item>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">未登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import {computed, h, onMounted, ref} from 'vue';
import { MailOutlined,LogoutOutlined,UserOutlined } from '@ant-design/icons-vue';
import {MenuProps, message} from 'ant-design-vue';
import {useRouter} from "vue-router";
import {useLoginUserStore} from "@/stores/user";
import {userLogoutUsingPost} from "@/api/userController";
import checkAccess from "@/access/checkAccess";
import menus from "@/router/index"

const router = useRouter();
// 当前选中的标签
const current = ref<string[]>([]);
// 拿到当前用户信息
const loginUserStore = useLoginUserStore();
loginUserStore.fetchLoginUser();
// 拿到当前选中的标签
router.afterEach((to, from, failure)=>{
  current.value = [to.path]
})

// 1. 过滤菜单项
// 使用 computed 属性来动态计算菜单项
const items = computed(() => {
  return menus.getRoutes().filter((item) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    // 根据用户权限动态过滤菜单项
    return checkAccess(loginUserStore.loginUser, item.meta?.access as string);
  });
});

// 2. 下拉框- 用户注销
const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}

// 3. 下拉框- 个人中心
const personalCenter = () => {
  router.push('/user/center');
}
</script>
<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}

.logo {
  height: 48px;
}
</style>
