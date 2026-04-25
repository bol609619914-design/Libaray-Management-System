<template>
  <template v-if="auth.role === 'READER'">
    <section class="reader-hero">
      <div>
        <h2>你好，{{ auth.user?.realName || '同学' }}</h2>
        <p>徜徉知识，遇见更好的自己</p>
      </div>
      <form class="reader-search" @submit.prevent>
        <input aria-label="资源检索" placeholder="搜索书名、作者、ISBN、主题词" />
        <RouterLink class="primary-btn" to="/books">搜索</RouterLink>
      </form>
      <div class="hot-words">
        <span>热门搜索：</span>
        <a href="#">人工智能</a>
        <a href="#">数据结构</a>
        <a href="#">百年孤独</a>
        <a href="#">经济学原理</a>
      </div>
      <div class="hero-books" aria-hidden="true">
        <span></span><span></span><span></span>
      </div>
    </section>
    <section class="reader-cards">
      <article v-for="item in readerStats" :key="item.label" class="reader-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }} <small>册</small></strong>
        <RouterLink :to="item.path">{{ item.action }}</RouterLink>
      </article>
    </section>
    <section class="table-panel">
      <div class="section-heading">
        <h2>推荐图书</h2>
        <button class="link-button" type="button">换一批 ›</button>
      </div>
      <div class="recommend-grid">
        <article v-for="book in recommendedBooks" :key="book.title" class="recommend-card">
          <div class="book-cover" :style="{ '--cover': book.color }">{{ book.short }}</div>
          <strong>{{ book.title }}</strong>
          <span>{{ book.author }}</span>
          <small>★★★★★ {{ book.score }}</small>
          <em>可借</em>
        </article>
      </div>
    </section>
    <section class="section-block">
      <div class="section-heading">
        <h2>最近浏览</h2>
        <RouterLink class="link-button" to="/books">查看全部 ›</RouterLink>
      </div>
      <ul class="recent-list">
        <li v-for="item in recentBooks" :key="item.title">
          <div class="mini-cover" :style="{ '--cover': item.color }"></div>
          <div>
            <strong>{{ item.title }}</strong>
            <p>{{ item.author }} · 浏览于 {{ item.time }}</p>
          </div>
        </li>
      </ul>
    </section>
  </template>
  <template v-else>
    <section class="ops-grid">
      <article class="table-panel todo-panel">
        <div class="section-heading">
          <h2>待处理借阅</h2>
          <RouterLink class="link-button" to="/borrows">查看全部 (12) ›</RouterLink>
        </div>
        <ul class="todo-list">
          <li v-for="item in todoItems" :key="item.code">
            <div class="mini-cover" :style="{ '--cover': item.color }"></div>
            <div>
              <strong>{{ item.title }}</strong>
              <p>条码 {{ item.code }} · {{ item.note }}</p>
            </div>
            <span class="status" :class="item.state">{{ item.stateText }}</span>
          </li>
        </ul>
      </article>
      <article class="table-panel reader-overview">
        <div class="section-heading">
          <h2>读者概览</h2>
          <select aria-label="统计周期">
            <option>本月</option>
          </select>
        </div>
        <div class="stat-pair">
          <div>
            <span>本月借阅</span>
            <strong>1,284</strong>
            <small>较上月 ↑ 12.5%</small>
            <svg viewBox="0 0 140 46" aria-hidden="true"><polyline points="0,36 15,28 30,30 45,21 60,24 75,16 90,23 105,10 120,17 140,6"/></svg>
          </div>
          <div>
            <span>活跃读者</span>
            <strong>327</strong>
            <small>较上月 ↑ 8.3%</small>
            <svg class="green" viewBox="0 0 140 46" aria-hidden="true"><polyline points="0,35 18,31 30,23 46,25 62,19 78,8 94,20 110,13 126,17 140,6"/></svg>
          </div>
        </div>
      </article>
      <article class="metric mini"><span>超期提醒</span><strong>24 <small>册</small></strong><small>较上月 ↓ 5.6%</small></article>
      <article class="metric mini"><span>采购审批中</span><strong>9 <small>项</small></strong><small>较上月 ↑ 12.0%</small></article>
    </section>
    <section class="table-panel">
      <div class="section-heading">
        <h2>采购审批列表</h2>
        <button class="link-button" type="button">查看全部</button>
      </div>
      <div class="responsive-table">
        <table>
          <thead>
            <tr><th>申请单</th><th>申请人</th><th>申请时间</th><th>金额</th><th>状态</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="item in purchaseRows" :key="item.no">
              <td>{{ item.no }}</td><td>{{ item.user }}</td><td>{{ item.time }}</td><td>{{ item.amount }}</td>
              <td><span class="status" :class="item.statusClass">{{ item.status }}</span></td>
              <td><button class="tiny-btn" type="button">{{ item.action }}</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </template>
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
const readerStats = [
  { label: '我的借阅', value: 3, action: '查看全部借阅 ›', path: '/borrows' },
  { label: '我的预约', value: 2, action: '查看预约 ›', path: '/books' },
  { label: '到期提醒', value: 1, action: '即将到期 ›', path: '/borrows' }
]
const recommendedBooks = [
  { title: '人类简史', author: '尤瓦尔·赫拉利', score: '9.1', short: '人类', color: '#f6efe2' },
  { title: '置身事内', author: '中国政府与经济发展', score: '8.7', short: '置身', color: '#f3dfcc' },
  { title: '被讨厌的勇气', author: '岸见一郎', score: '8.6', short: '勇气', color: '#2b78d0' },
  { title: '乡土中国', author: '费孝通', score: '8.8', short: '乡土', color: '#efe4cf' },
  { title: '未来简史', author: '尤瓦尔·赫拉利', score: '8.9', short: '未来', color: '#111827' },
  { title: '原则', author: '瑞·达利欧', score: '8.6', short: '原则', color: '#fafafa' }
]
const recentBooks = [
  { title: '围城', author: '钱钟书', time: '2024-05-20 15:30', color: '#2b5c8a' },
  { title: '活着', author: '余华', time: '2024-05-19 10:21', color: '#254c5f' },
  { title: '人性的弱点', author: '戴尔·卡耐基', time: '2024-05-18 09:14', color: '#d7d7d7' }
]
const todoItems = [
  { title: '数据库系统概论', code: 'B-20491', note: '预约到馆', stateText: '待取书', state: 'blue', color: '#1c55c9' },
  { title: '人机交互设计', code: 'A-11352', note: '今日到期', stateText: '待提醒', state: 'orange', color: '#0a8f74' },
  { title: '云计算导论', code: 'C-01983', note: '归还异常', stateText: '待核验', state: 'red', color: '#2a95d8' }
]
const purchaseRows = [
  { no: '图书采购申请（第2024-045号）', user: '李老师', time: '2024-05-20 10:21', amount: '¥8,560.00', status: '审批中', statusClass: 'blue', action: '处理' },
  { no: '电子资源采购（第2024-018号）', user: '王老师', time: '2024-05-19 16:47', amount: '¥12,300.00', status: '审批中', statusClass: 'blue', action: '处理' },
  { no: '期刊采购申请（第2024-017号）', user: '陈老师', time: '2024-05-18 11:09', amount: '¥3,420.00', status: '待取货', statusClass: 'orange', action: '处理' },
  { no: '图书采购申请（第2024-016号）', user: '刘老师', time: '2024-05-17 09:32', amount: '¥6,780.00', status: '已通过', statusClass: 'green', action: '查看' }
]

onMounted(async () => {
  announcements.value = (await api.announcements()).data
  if (['LIBRARIAN', 'SUPER_ADMIN'].includes(auth.role)) {
    stats.value = (await api.stats()).data
  }
})
</script>
