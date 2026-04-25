<template>
  <section class="table-panel">
    <div class="section-heading">
      <h2>{{ canManage ? '全部借阅业务' : '我的借阅记录' }}</h2>
      <select v-model="status" aria-label="借阅状态筛选" @change="load">
        <option value="">全部状态</option>
        <option value="BORROWED">在借</option>
        <option value="OVERDUE">逾期</option>
        <option value="RETURNED">已归还</option>
      </select>
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
        <tbody>
          <tr v-for="record in records" :key="record.id">
            <td>#{{ record.id }}</td>
            <td>{{ record.userId }}</td>
            <td>{{ record.bookId }}</td>
            <td>{{ record.dueAt }}</td>
            <td>{{ record.overdueDays }} 天</td>
            <td>¥{{ record.fineAmount }}</td>
            <td><span class="status">{{ statusText(record.status) }}</span></td>
            <td>
              <div class="row-actions">
                <button class="ghost-btn" type="button" @click="renew(record)">续借</button>
                <button v-if="canManage" class="ghost-btn" type="button" @click="returnBook(record)">还书</button>
                <button v-if="canManage && record.fineStatus === 'UNPAID'" class="ghost-btn" type="button" @click="pay(record)">缴费</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
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

function statusText(value) {
  return { BORROWED: '在借', OVERDUE: '逾期', RETURNED: '已归还' }[value] || value
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

