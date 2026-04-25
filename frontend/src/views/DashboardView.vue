<template>
  <section class="dashboard-grid">
    <article class="overview-panel">
      <p class="eyebrow">今日概览</p>
      <h2>{{ greeting }}</h2>
      <p>你可以从这里进入检索、借阅、库存维护、读者反馈和系统配置。</p>
      <div class="quick-actions">
        <RouterLink class="primary-btn" to="/books">检索图书</RouterLink>
        <RouterLink class="ghost-btn" to="/borrows">查看借阅</RouterLink>
      </div>
    </article>
    <article class="metric" v-for="item in metrics" :key="item.label">
      <span>{{ item.label }}</span>
      <strong>{{ item.value }}</strong>
    </article>
  </section>
  <section class="content-split">
    <article class="section-block">
      <div class="section-heading">
        <h2>系统公告</h2>
      </div>
      <ul class="notice-list">
        <li v-for="notice in announcements" :key="notice.id">
          <strong>{{ notice.title }}</strong>
          <p>{{ notice.content }}</p>
        </li>
      </ul>
    </article>
    <article class="section-block">
      <div class="section-heading">
        <h2>角色功能</h2>
      </div>
      <ul class="feature-list">
        <li v-for="feature in features" :key="feature">{{ feature }}</li>
      </ul>
    </article>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../api/library'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const stats = ref({ readers: 0, books: 0, activeBorrows: 0, overdueBorrows: 0, unpaidFines: 0 })
const announcements = ref([])

const greeting = computed(() => `${auth.user?.realName || '你好'}，欢迎回来`)
const metrics = computed(() => [
  { label: '馆藏图书', value: stats.value.books },
  { label: '在借记录', value: stats.value.activeBorrows },
  { label: '逾期记录', value: stats.value.overdueBorrows },
  { label: '未缴罚款', value: `¥${stats.value.unpaidFines || 0}` }
])
const features = computed(() => {
  if (auth.role === 'SUPER_ADMIN') return ['权限管理与管理员分配', '借阅规则与逾期收费配置', '全量数据管理、备份恢复、系统日志']
  if (auth.role === 'LIBRARIAN') return ['图书新增编辑、上下架和库存维护', '线下借还、人工续借、罚款处理', '读者管理、异常账号限制、反馈处理']
  return ['图书检索、分类查询和详情查看', '线上借阅、续借、归还申请', '在借图书、历史记录、逾期和罚款明细']
})

onMounted(async () => {
  announcements.value = (await api.announcements()).data
  if (['LIBRARIAN', 'SUPER_ADMIN'].includes(auth.role)) {
    stats.value = (await api.stats()).data
  }
})
</script>

