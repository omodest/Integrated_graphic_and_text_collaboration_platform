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
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <!-- 用户信息展示-->
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser?.id">
          {{loginUserStore.loginUser.userName}}
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { h, ref } from 'vue';
import { MailOutlined } from '@ant-design/icons-vue';
import { MenuProps } from 'ant-design-vue';
import {useRouter} from "vue-router";
import {useLoginUserStore} from "@/stores/user";

const router = useRouter();
// 当前选中的标签
const current = ref<string[]>([]);

// 头部导航栏
const items = ref<MenuProps['items']>([
  {
    key: '/',
    icon: () => h(MailOutlined),
    label: '首页',
    title: '首页',
  },
  {
    key: '/about',
    label: '关于',
    title: '关于',
  },
  {
    key: '/other',
    label: h('a',{href: 'https://aaa.com', target: '_blank'}, "其他"),
    title: '其他',
  }
]);

// 导航栏切换
const doMenuClick = ({key}: {key: string}) => {
  router.push({
    path: key,
  })
}

// 拿到当前选中的标签
router.afterEach((to, from, failure)=>{
  current.value = [to.path]
})

// 拿到当前用户信息
const loginUserStore = useLoginUserStore();
loginUserStore.fetchLoginUser();

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
