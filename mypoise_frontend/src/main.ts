// import './assets/main.css' // 这个注释掉，否则会出现样式污染

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

// 创建vue实例，并绑定到根组件App中
const app = createApp(App)
// 给根组件页面注册 组件库、状态管理库、路由配置等
app.use(Antd)
app.use(createPinia())
app.use(router)

app.mount('#app')
