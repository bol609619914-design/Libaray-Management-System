<template>
  <RouterView v-if="$route.path === '/login'" />
  <div v-else class="app-shell">
    <aside class="sidebar" aria-label="主导航">
      <RouterLink to="/" class="brand" aria-label="返回工作台">
        <span class="brand-mark">L</span>
        <span>
          <strong>图书馆系统</strong>
          <small>{{ roleName }}</small>
        </span>
      </RouterLink>
      <nav class="nav-list">
        <RouterLink v-for="item in menu" :key="item.path" :to="item.path">
          <component :is="item.icon" aria-hidden="true" :size="18" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>
    <main class="main-panel" id="main">
      <header class="topbar">
        <div>
          <p class="eyebrow">Library Operations</p>
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="user-chip">
          <span>{{ auth.user?.realName }}</span>
          <button type="button" @click="logout">退出</button>
        </div>
      </header>
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { BookOpen, ClipboardList, LayoutDashboard, Shield, UserCog, UserRound } from 'lucide-vue-next'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const baseMenu = [
  { path: '/', label: '首页工作台', icon: LayoutDashboard },
  { path: '/books', label: '图书检索', icon: BookOpen },
  { path: '/borrows', label: '借阅记录', icon: ClipboardList },
  { path: '/profile', label: '个人资料', icon: UserRound }
]

const menu = computed(() => {
  const items = [...baseMenu]
  if (['LIBRARIAN', 'SUPER_ADMIN'].includes(auth.role)) items.splice(3, 0, { path: '/admin', label: '馆员管理', icon: UserCog })
  if (auth.role === 'SUPER_ADMIN') items.splice(4, 0, { path: '/super', label: '系统权限', icon: Shield })
  return items
})

const roleName = computed(() => ({
  READER: '读者',
  LIBRARIAN: '图书管理员',
  SUPER_ADMIN: '超级管理员'
}[auth.role] || '访客'))

const pageTitle = computed(() => menu.value.find((item) => item.path === route.path)?.label || '图书馆管理系统')

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

