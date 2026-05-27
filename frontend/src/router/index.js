import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  { path: '/login', component: () => import('@/views/user/Login.vue'), meta: { public: true } },
  { path: '/register', component: () => import('@/views/user/Register.vue'), meta: { public: true } },
  { path: '/', component: () => import('@/views/user/Home.vue') },
  { path: '/menu', component: () => import('@/views/user/Menu.vue') },
  { path: '/cart', component: () => import('@/views/user/Cart.vue') },
  { path: '/orders', component: () => import('@/views/user/Orders.vue') },
  { path: '/orders/:id', component: () => import('@/views/user/OrderDetail.vue') },
  { path: '/profile', component: () => import('@/views/user/Profile.vue') },
  { path: '/admin', redirect: '/admin/dashboard' },
  { path: '/admin/dashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { admin: true } },
  { path: '/admin/categories', component: () => import('@/views/admin/CategoryManage.vue'), meta: { admin: true } },
  { path: '/admin/products', component: () => import('@/views/admin/ProductManage.vue'), meta: { admin: true } },
  { path: '/admin/orders', component: () => import('@/views/admin/OrderManage.vue'), meta: { admin: true } },
  { path: '/admin/users', component: () => import('@/views/admin/UserManage.vue'), meta: { admin: true } },
  { path: '/admin/banners', component: () => import('@/views/admin/BannerManage.vue'), meta: { admin: true } },
]

const router = createRouter({
  history: createWebHistory(),
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
