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
      path: '/',
      name: '主页',
      component: () => import('../pages/HomePage.vue'),
      meta: {
        hideInMenu: true,
      },
    },
    {
      path: '/picture/:id',
      name: '图片详情',
      component: () => import('../pages/picture/PictureDetailPage.vue'),
      props: true,
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
      path: '/admin/userManage',
      name: '用户管理',
      component: () => import('../pages/admin/UserManagePage.vue'),
      meta: {
        access: AccessEnum.ADMIN,
      },
    },
    {
      path: '/admin/pictureManage',
      name: '图片管理',
      component: () => import('../pages/admin/PictureManagePage.vue'),
      meta: {
        access: AccessEnum.ADMIN,
      },
    },
    {
      path: '/admin/tags',
      name: '标签管理',
      component: () => import('../pages/admin/TagManagePage.vue'),
      meta: {
        access: AccessEnum.ADMIN,
      },
    },
    {
      path: '/admin/category',
      name: '分类管理',
      component: () => import('../pages/admin/CategoryManagePage.vue'),
      meta: {
        access: AccessEnum.ADMIN,
      },
    },
    {
      path: '/add_picture',
      name: '创建图片',
      component: () => import('../pages/picture/AddPicturePage.vue'),
    },
    {
      path: '/noAuth',
      name: 'NoAuth',
      component: () => import('../pages/noauth/NoAuthPage.vue'),
      meta: {
        hideInMenu: true,
      },
    },
  ],
})

export default router
