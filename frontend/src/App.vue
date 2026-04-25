<template>
  <RouterView v-if="$route.path === '/login'" />
  <div v-else class="app-shell">
    <aside class="sidebar" aria-label="主导航">
      <RouterLink to="/" class="brand" aria-label="返回工作台">
        <img class="brand-logo" src="/library-logo.png" alt="" />
        <span>
          <strong>LibraFlow</strong>
          <small>现代图书馆管理系统</small>
        </span>
      </RouterLink>
      <nav class="nav-list">
        <RouterLink v-for="item in menu" :key="item.path" :to="item.path">
          <component :is="item.icon" aria-hidden="true" :size="18" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>
    <main ref="mainPanel" class="main-panel" id="main">
      <header class="topbar" :class="{ 'no-search': auth.role === 'READER' }">
        <label v-if="auth.role !== 'READER'" class="top-search">
          <span class="sr-only">全局搜索</span>
          <Search aria-hidden="true" :size="18" />
          <input placeholder="搜索书名 / ISBN / 条码 / 读者证号" />
        </label>
        <div class="top-actions">
          <div class="user-chip" aria-label="当前用户">
            <span class="avatar" aria-hidden="true">{{ avatarInitial }}</span>
            <span>{{ auth.user?.realName }}</span>
          </div>
          <div class="notice-wrap">
            <button class="circle-button" type="button" :aria-expanded="noticeOpen" aria-label="查看通知" @click="noticeOpen = !noticeOpen">
              <Bell aria-hidden="true" :size="18" />
              <span class="dot" aria-hidden="true"></span>
            </button>
            <div v-if="noticeOpen" class="notice-popover" role="status">
              <strong>系统通知</strong>
              <p>{{ noticeText }}</p>
            </div>
          </div>
          <button class="circle-button" type="button" aria-label="退出登录" @click="logout">
            <LogOut aria-hidden="true" :size="17" />
          </button>
        </div>
      </header>
      <div class="page-heading">
        <h1>{{ pageTitle }}</h1>
        <p>{{ roleName }} · LibraFlow</p>
      </div>
      <RouterView />
      <footer class="app-footer">© 2024 LibraFlow 现代图书馆管理系统</footer>
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bell, BookOpen, ClipboardList, LayoutDashboard, LogOut, Search, Shield, UserCog, UserRound } from 'lucide-vue-next'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const mainPanel = ref(null)
const noticeOpen = ref(false)

const menu = computed(() => {
  const canManage = ['LIBRARIAN', 'SUPER_ADMIN'].includes(auth.role)
  const items = [
    { path: '/', label: '工作台', icon: LayoutDashboard },
    { path: '/books', label: canManage ? '编目管理' : '资源检索', icon: BookOpen },
    { path: '/borrows', label: canManage ? '借阅业务' : '我的借阅', icon: ClipboardList },
    { path: '/profile', label: '个人中心', icon: UserRound }
  ]
  if (canManage) items.splice(3, 0, { path: '/admin', label: auth.role === 'SUPER_ADMIN' ? '馆员管理' : '读者管理', icon: UserCog })
  if (auth.role === 'SUPER_ADMIN') items.splice(4, 0, { path: '/super', label: '系统设置', icon: Shield })
  return items
})

const roleName = computed(() => ({
  READER: '读者',
  LIBRARIAN: '图书管理员',
  SUPER_ADMIN: '超级管理员'
}[auth.role] || '访客'))

const pageTitle = computed(() => menu.value.find((item) => item.path === route.path)?.label || '图书馆管理系统')
const avatarInitial = computed(() => auth.user?.realName?.slice(0, 1) || 'U')
const noticeText = computed(() => {
  if (auth.role === 'LIBRARIAN') return '今日有 4 条待核验归还记录，请在借阅业务中处理。'
  if (auth.role === 'SUPER_ADMIN') return '系统配置和操作日志均正常，建议下班前导出一次备份。'
  return '你有 1 本图书即将到期，可在我的借阅中办理续借。'
})

function logout() {
  auth.logout()
  router.push('/login')
}

watch(
  () => route.fullPath,
  async () => {
    noticeOpen.value = false
    await nextTick()
    mainPanel.value?.scrollTo({ top: 0, left: 0 })
  }
)
</script>
