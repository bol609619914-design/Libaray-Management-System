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
              <span v-if="hasNotice" class="dot" aria-hidden="true"></span>
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
import { api } from './api/library'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const mainPanel = ref(null)
const noticeOpen = ref(false)
const noticeSummary = ref(null)

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
const hasNotice = computed(() => {
  if (!auth.token || route.path === '/login') return false
  if (!noticeSummary.value) return true
  return noticeSummary.value.count > 0
})
const noticeText = computed(() => {
  if (noticeSummary.value?.text) return noticeSummary.value.text
  if (auth.role === 'LIBRARIAN') return '借阅业务正在同步，请稍后查看待处理记录。'
  if (auth.role === 'SUPER_ADMIN') return '系统运行状态正在同步，请稍后查看统计与日志。'
  return '借阅提醒正在同步，请稍后查看我的借阅。'
})

function logout() {
  auth.logout()
  router.push('/login')
}

async function loadNotice() {
  if (!auth.token || route.path === '/login') {
    noticeSummary.value = null
    return
  }
  try {
    if (auth.role === 'READER') {
      const records = (await api.myBorrows({ status: 'BORROWED' })).data || []
      const now = Date.now()
      const day = 24 * 60 * 60 * 1000
      const dueSoon = records.filter((record) => {
        const due = new Date(record.dueAt).getTime()
        return !Number.isNaN(due) && due >= now && due - now <= 7 * day
      }).length
      const overdue = records.filter((record) => new Date(record.dueAt).getTime() < now).length
      const count = dueSoon + overdue
      noticeSummary.value = {
        count,
        text: count
          ? `你有 ${dueSoon} 本图书 7 天内到期，${overdue} 本已超过应还日期，可在我的借阅中处理。`
          : '当前没有即将到期或逾期图书，借阅状态良好。'
      }
      return
    }

    const stats = (await api.stats()).data
    const overdue = Number(stats?.overdueBorrows || 0)
    const fines = Number(stats?.unpaidFines || 0)
    const count = overdue + (fines > 0 ? 1 : 0)
    noticeSummary.value = {
      count,
      text: count
        ? `当前有 ${overdue} 条逾期借阅，未缴罚款 ${fines.toFixed(2)} 元，请优先跟进流通业务。`
        : '当前没有逾期借阅或未缴罚款，流通业务状态正常。'
    }
  } catch {
    noticeSummary.value = null
  }
}

watch(
  () => route.fullPath,
  async () => {
    noticeOpen.value = false
    await nextTick()
    mainPanel.value?.scrollTo({ top: 0, left: 0 })
    await loadNotice()
  }
)

watch(
  () => auth.token,
  loadNotice,
  { immediate: true }
)
</script>
