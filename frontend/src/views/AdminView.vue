<template>
  <section class="admin-page">
    <section class="dashboard-grid compact admin-metrics">
      <article class="metric" v-for="item in metrics" :key="item.label">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </article>
    </section>
    <section class="content-split admin-split">
      <article class="table-panel">
        <div class="section-heading">
          <div>
            <h2>读者与账号</h2>
            <p>查看读者、馆员与账号状态，快速定位异常账号。</p>
          </div>
        </div>
        <div class="responsive-table">
          <table>
            <thead>
              <tr>
                <th>用户名</th>
                <th>姓名</th>
                <th>角色</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.id">
                <td>{{ user.username }}</td>
                <td>{{ user.realName }}</td>
                <td>{{ roleText(user.role) }}</td>
                <td><span class="status" :class="user.status === 'ACTIVE' ? 'green' : 'red'">{{ statusText(user.status) }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="!users.length" class="empty-state compact-empty">
          <div class="empty-icon" aria-hidden="true">LF</div>
          <h3>暂无账号数据</h3>
        </div>
      </article>
      <article class="section-block">
        <div class="section-heading">
          <div>
            <h2>读者反馈</h2>
            <p>跟进读者问题、借阅异常与馆藏建议。</p>
          </div>
        </div>
        <ul v-if="feedback.length" class="notice-list">
          <li v-for="item in feedback" :key="item.id">
            <strong>{{ item.title }}</strong>
            <p>{{ item.content }}</p>
            <span class="status">{{ item.status }}</span>
          </li>
        </ul>
        <div v-else class="empty-state compact-empty">
          <div class="empty-icon" aria-hidden="true">LF</div>
          <h3>暂无读者反馈</h3>
        </div>
      </article>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../api/library'

const stats = ref({ readers: 0, books: 0, activeBorrows: 0, overdueBorrows: 0, unpaidFines: 0 })
const users = ref([])
const feedback = ref([])
const metrics = computed(() => [
  { label: '读者数', value: stats.value.readers },
  { label: '馆藏数', value: stats.value.books },
  { label: '在借', value: stats.value.activeBorrows },
  { label: '未缴罚款', value: `¥${stats.value.unpaidFines || 0}` }
])

function roleText(role) {
  return { READER: '读者', LIBRARIAN: '图书管理员', SUPER_ADMIN: '超级管理员' }[role] || role
}

function statusText(status) {
  return { ACTIVE: '正常', DISABLED: '禁用' }[status] || status
}

onMounted(async () => {
  stats.value = (await api.stats()).data
  users.value = (await api.users()).data
  feedback.value = (await api.feedbackList()).data
})
</script>
