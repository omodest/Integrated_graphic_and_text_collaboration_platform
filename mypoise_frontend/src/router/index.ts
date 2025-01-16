import { createRouter, createWebHistory } from 'vue-router'
import AccessEnum from '@/access/accessEnum'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/user/login',
      name: '登录',
      component: () => import('../pages/user/UserLoginPage.vue'),
      meta: {
        hideInMenu: true,
      },
    },
    {
      path: '/user/register',
      name: '注册',
      component: () => import('../pages/user/UserRegisterPage.vue'),
      meta: {
        hideInMenu: true,
      },
    },
    {
      path: '/user/center',
      name: '个人中心',
      component: () => import('../pages/user/UserEditMyPage.vue'),
      meta: {
        hideInMenu: true,
      },
    },
    {
      path: '/noAuth',
      name: 'NoAuth',
      component: () => import('../pages/noauth/NoAuthPage.vue'),
      meta: {
        hideInMenu: true,
      },
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: () => import('../pages/admin/UserManagePage.vue'),
      meta: {
        access: AccessEnum.ADMIN,
      },
    },
  ],
})

export default router
