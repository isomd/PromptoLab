import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/auth',
      name: 'Auth',
      component: () => import('../views/AuthView.vue'),
      meta: {
        title: '登录注册 - AI诗人'
      }
    },
    {
      path: '/prompt-builder',
      name: 'prompt-builder',
      component: () => import('../views/PromptBuilderView.vue'),
      meta: {
        title: 'AI构建器 - PromptoLab'
      }
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('../views/SettingsView.vue'),
      meta: {
        title: '系统设置 - AI诗人'
      }
    },
    {
      path: '/settings/api-config',
      name: 'api-config',
      component: () => import('../views/ApiConfigView.vue'),
      meta: {
        title: 'API配置 - AI诗人'
      }
    },
  ]
})

export default router
