import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import BooksView from '../views/BooksView.vue'
import BorrowView from '../views/BorrowView.vue'
import AdminView from '../views/AdminView.vue'
import SuperAdminView from '../views/SuperAdminView.vue'
import ProfileView from '../views/ProfileView.vue'

const routes = [
  { path: '/login', component: LoginView, meta: { public: true } },
  { path: '/', component: DashboardView },
  { path: '/books', component: BooksView },
  { path: '/borrows', component: BorrowView },
  { path: '/admin', component: AdminView, meta: { roles: ['LIBRARIAN', 'SUPER_ADMIN'] } },
  { path: '/super', component: SuperAdminView, meta: { roles: ['SUPER_ADMIN'] } },
  { path: '/profile', component: ProfileView }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isLoggedIn) return '/login'
  if (to.meta.roles && !to.meta.roles.includes(auth.role)) return '/'
  if (to.path === '/login' && auth.isLoggedIn) return '/'
  return true
})

export default router
