import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  { path: '/login', component: () => import('@/views/user/Login.vue'), meta: { public: true } },
  { path: '/register', component: () => import('@/views/user/Register.vue'), meta: { public: true } },
  { path: '/', component: () => import('@/views/user/Home.vue') },
  { path: '/menu', component: () => import('@/views/user/Menu.vue') },
  { path: '/cart', component: () => import('@/views/user/Cart.vue') },
  { path: '/pay/:id', component: () => import('@/views/user/Payment.vue') },
  { path: '/orders', component: () => import('@/views/user/Orders.vue') },
  { path: '/orders/:id', component: () => import('@/views/user/OrderDetail.vue') },
  { path: '/profile', component: () => import('@/views/user/Profile.vue') },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { admin: true },
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'categories', component: () => import('@/views/admin/CategoryManage.vue') },
      { path: 'products', component: () => import('@/views/admin/ProductManage.vue') },
      { path: 'orders', component: () => import('@/views/admin/OrderManage.vue') },
      { path: 'users', component: () => import('@/views/admin/UserManage.vue') },
      { path: 'banners', component: () => import('@/views/admin/BannerManage.vue') },
      { path: 'knowledge', component: () => import('@/views/admin/KnowledgeManage.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.public) {
    next()
    return
  }

  if (!userStore.token) {
    next('/login')
    return
  }

  if (to.meta.admin && !userStore.isAdmin()) {
    next('/')
    return
  }

  next()
})

export default router
