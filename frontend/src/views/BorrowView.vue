<template>
  <section class="borrow-page">
    <div class="borrow-summary">
      <article v-for="item in summaryCards" :key="item.label" class="borrow-stat">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </article>
    </div>

    <section class="table-panel borrow-panel">
      <div class="borrow-panel-head">
        <div>
          <h2>{{ canManage ? '借阅业务列表' : '我的借阅记录' }}</h2>
          <p>{{ canManage ? '集中处理线下还书、人工续借、逾期罚款与异常记录。' : '查看当前借阅、历史归还、续借和罚款状态。' }}</p>
        </div>
        <div class="borrow-tools">
          <select v-model="status" aria-label="借阅状态筛选" @change="load">
            <option value="">全部状态</option>
            <option value="BORROWED">在借</option>
            <option value="OVERDUE">逾期</option>
            <option value="RETURNED">已归还</option>
          </select>
          <RouterLink class="ghost-btn" to="/books">{{ canManage ? '办理读者借阅' : '去借书' }}</RouterLink>
        </div>
      </div>
      <div class="responsive-table">
      <table>
        <thead>
          <tr>
            <th>记录</th>
            <th>读者</th>
            <th>图书</th>
            <th>到期时间</th>
            <th>逾期</th>
            <th>罚款</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-if="records.length">
          <tr v-for="record in records" :key="record.id">
            <td>
              <span class="record-code">#{{ record.id }}</span>
            </td>
            <td>
              <div class="table-main">{{ record.readerName }}</div>
              <div class="table-sub">@{{ record.readerUsername }}</div>
            </td>
            <td>
              <div class="table-main">{{ record.bookTitle }}</div>
              <div class="table-sub">{{ record.bookAuthor }} · ISBN {{ record.isbn }}</div>
            </td>
            <td>
              <div class="table-main">{{ formatDate(record.dueAt) }}</div>
              <div class="table-sub">续借 {{ record.renewCount || 0 }} 次</div>
            </td>
            <td :class="{ 'danger-text': record.overdueDays > 0 }">{{ record.overdueDays || 0 }} 天</td>
            <td>
              <div class="table-main">{{ formatMoney(record.fineAmount) }}</div>
              <div class="table-sub">{{ fineText(record.fineStatus) }}</div>
            </td>
            <td><span class="status" :class="record.status">{{ statusText(record.status) }}</span></td>
            <td>
              <div class="row-actions">
                <button v-if="record.status !== 'RETURNED'" class="ghost-btn" type="button" @click="renew(record)">续借</button>
                <button v-if="canManage && record.status !== 'RETURNED'" class="ghost-btn" type="button" @click="returnBook(record)">还书</button>
                <button v-if="canManage && record.fineStatus === 'UNPAID'" class="ghost-btn" type="button" @click="pay(record)">缴费</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      </div>
      <div v-if="!records.length" class="empty-state">
        <div class="empty-icon" aria-hidden="true">LF</div>
        <h3>{{ emptyTitle }}</h3>
        <p>{{ canManage ? '当前筛选条件下没有待处理记录。管理员只能为读者办理借阅、还书、续借和罚款处理。' : '你当前没有匹配的借阅记录，可以先去检索并借阅一本书。' }}</p>
        <RouterLink class="primary-btn" to="/books">{{ canManage ? '进入编目与流通' : '检索图书' }}</RouterLink>
      </div>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../api/library'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const records = ref([])
const status = ref('')
const canManage = computed(() => ['LIBRARIAN', 'SUPER_ADMIN'].includes(auth.role))
const activeCount = computed(() => records.value.filter((item) => item.status === 'BORROWED').length)
const overdueCount = computed(() => records.value.filter((item) => item.status === 'OVERDUE').length)
const returnedCount = computed(() => records.value.filter((item) => item.status === 'RETURNED').length)
const unpaidFine = computed(() => records.value.filter((item) => item.fineStatus === 'UNPAID').reduce((sum, item) => sum + Number(item.fineAmount || 0), 0).toFixed(2))
const summaryCards = computed(() => [
  { label: canManage.value ? '业务记录' : '全部记录', value: records.value.length, hint: '当前筛选结果' },
  { label: '在借', value: activeCount.value, hint: '可线上续借' },
  { label: '逾期', value: overdueCount.value, hint: '需要跟进提醒' },
  { label: canManage.value ? '未缴罚款' : '罚款金额', value: `¥${unpaidFine.value}`, hint: returnedCount.value ? `${returnedCount.value} 条已归还` : '暂无归还记录' }
])
const emptyTitle = computed(() => status.value ? `暂无${statusText(status.value)}记录` : '暂无借阅业务记录')

function statusText(value) {
  return { BORROWED: '在借', OVERDUE: '逾期', RETURNED: '已归还' }[value] || value
}

function fineText(value) {
  return { NONE: '无罚款', UNPAID: '待缴费', PAID: '已缴清' }[value] || '未登记'
}

function formatMoney(value) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function formatDate(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

async function load() {
  const request = canManage.value ? api.allBorrows : api.myBorrows
  records.value = (await request({ status: status.value || undefined })).data
}

async function renew(record) {
  await api.renew(record.id)
  await load()
}

async function returnBook(record) {
  await api.returnBook(record.id)
  await load()
}

async function pay(record) {
  await api.markFinePaid(record.id)
  await load()
}

onMounted(load)
</script>
