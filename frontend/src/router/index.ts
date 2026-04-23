import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/views/Dashboard.vue')
    },
    {
      path: '/rules',
      name: 'rules',
      component: () => import('@/views/Rules.vue')
    },
    {
      path: '/rules/:id',
      name: 'rule-detail',
      component: () => import('@/views/RuleDetail.vue')
    },
    {
      path: '/groups',
      name: 'groups',
      component: () => import('@/views/Groups.vue')
    },
    {
      path: '/requests',
      name: 'requests',
      component: () => import('@/views/Requests.vue')
    },
    {
      path: '/requests/:id',
      name: 'request-detail',
      component: () => import('@/views/RequestDetail.vue')
    }
  ]
})

export default router