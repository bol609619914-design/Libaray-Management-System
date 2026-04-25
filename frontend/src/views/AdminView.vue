<template>
  <section class="dashboard-grid compact">
    <article class="metric" v-for="item in metrics" :key="item.label">
      <span>{{ item.label }}</span>
      <strong>{{ item.value }}</strong>
    </article>
  </section>
  <section class="content-split">
    <article class="table-panel">
      <div class="section-heading">
        <h2>读者与账号</h2>
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
              <td><span class="status">{{ user.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>
    <article class="section-block">
      <div class="section-heading">
        <h2>读者反馈</h2>
      </div>
      <ul class="notice-list">
        <li v-for="item in feedback" :key="item.id">
          <strong>{{ item.title }}</strong>
          <p>{{ item.content }}</p>
          <span class="status">{{ item.status }}</span>
        </li>
      </ul>
    </article>
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

onMounted(async () => {
  stats.value = (await api.stats()).data
  users.value = (await api.users()).data
  feedback.value = (await api.feedbackList()).data
})
</script>

