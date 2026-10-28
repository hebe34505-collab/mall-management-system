import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior() {
    return { top: 0 }
  },
  routes: [
    {
      path: '/',
      component: () => import('../layouts/StoreLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('../views/store/HomeView.vue'), meta: { public: true, title: '首页' } },
        { path: 'products', name: 'products', component: () => import('../views/store/ProductListView.vue'), meta: { public: true, title: '商品' } },
        { path: 'products/:id', name: 'product', component: () => import('../views/store/ProductDetailView.vue'), meta: { public: true, title: '商品详情' } },
        { path: 'cart', name: 'cart', component: () => import('../views/store/CartView.vue'), meta: { title: '购物车' } },
        { path: 'checkout', name: 'checkout', component: () => import('../views/store/CheckoutView.vue'), meta: { title: '确认订单' } },
        { path: 'orders', name: 'orders', component: () => import('../views/store/OrderListView.vue'), meta: { title: '我的订单' } },
        { path: 'orders/:id', name: 'order-detail', component: () => import('../views/store/OrderDetailView.vue'), meta: { title: '订单详情' } },
        { path: 'profile', name: 'profile', component: () => import('../views/store/ProfileView.vue'), meta: { title: '个人中心' } }
      ]
    },
    { path: '/login', name: 'login', component: () => import('../views/auth/LoginView.vue'), meta: { public: true, title: '登录' } },
    { path: '/register', name: 'register', component: () => import('../views/auth/RegisterView.vue'), meta: { public: true, title: '注册' } },
    { path: '/admin/login', name: 'admin-login', component: () => import('../views/auth/LoginView.vue'), meta: { public: true, admin: true, title: '管理登录' } },
    {
      path: '/admin',
      component: () => import('../layouts/AdminLayout.vue'),
      meta: { role: 'ADMIN', title: '管理后台' },
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', component: () => import('../views/admin/DashboardView.vue'), meta: { role: 'ADMIN', title: '数据总览' } },
        { path: 'products', component: () => import('../views/admin/ProductManageView.vue'), meta: { role: 'ADMIN', title: '商品管理' } },
        { path: 'products/new', component: () => import('../views/admin/ProductFormView.vue'), meta: { role: 'ADMIN', title: '新增商品' } },
        { path: 'products/:id/edit', component: () => import('../views/admin/ProductFormView.vue'), meta: { role: 'ADMIN', title: '编辑商品' } },
        { path: 'categories', component: () => import('../views/admin/CategoryManageView.vue'), meta: { role: 'ADMIN', title: '分类管理' } },
        { path: 'stock', component: () => import('../views/admin/StockManageView.vue'), meta: { role: 'ADMIN', title: '库存管理' } },
        { path: 'orders', component: () => import('../views/admin/OrderManageView.vue'), meta: { role: 'ADMIN', title: '订单管理' } },
        { path: 'users', component: () => import('../views/admin/UserManageView.vue'), meta: { role: 'ADMIN', title: '用户管理' } },
        { path: 'stats', component: () => import('../views/admin/StatsView.vue'), meta: { role: 'ADMIN', title: '销售统计' } },
        { path: 'logs', component: () => import('../views/admin/LogView.vue'), meta: { role: 'ADMIN', title: '操作日志' } },
        { path: 'profile', component: () => import('../views/admin/AdminProfileView.vue'), meta: { role: 'ADMIN', title: '个人资料' } }
      ]
    },
    { path: '/403', name: 'forbidden', component: () => import('../views/ForbiddenView.vue'), meta: { public: true, title: '无权限' } },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../views/NotFoundView.vue'), meta: { public: true, title: '页面不存在' } }
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if ((to.path === '/login' || to.path === '/admin/login' || to.path === '/register') && auth.token && auth.user) {
    return auth.user.role === 'ADMIN' && to.path === '/admin/login' ? '/admin/dashboard' : (to.path === '/login' || to.path === '/register' ? '/' : true)
  }
  if (to.meta.public) return true
  if (!auth.token) {
    return { path: to.path.startsWith('/admin') ? '/admin/login' : '/login', query: { redirect: to.fullPath } }
  }
  if (!auth.user) {
    try {
      await auth.fetchMe()
    } catch {
      return { path: to.path.startsWith('/admin') ? '/admin/login' : '/login', query: { redirect: to.fullPath } }
    }
  }
  const requiredRole = to.matched.map((item) => item.meta.role).find(Boolean)
  if (requiredRole && auth.user?.role !== requiredRole) return '/403'
  return true
})

router.afterEach((to) => {
  const title = to.meta.title ? `${to.meta.title} - 禾木商城` : '禾木商城'
  document.title = title
})

export default router
