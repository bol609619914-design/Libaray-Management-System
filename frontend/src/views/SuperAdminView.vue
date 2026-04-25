<template>
  <section class="content-split">
    <article class="table-panel">
      <div class="section-heading">
        <h2>管理员权限</h2>
        <button class="ghost-btn" type="button" @click="createAdmin">新增管理员</button>
      </div>
      <div class="responsive-table">
        <table>
          <thead>
            <tr>
              <th>账号</th>
              <th>姓名</th>
              <th>角色</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in managers" :key="user.id">
              <td>{{ user.username }}</td>
              <td>{{ user.realName }}</td>
              <td>{{ user.role }}</td>
              <td>{{ user.status }}</td>
              <td><button class="ghost-btn" type="button" @click="disable(user)">禁用</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>
    <article class="section-block">
      <div class="section-heading">
        <h2>系统配置</h2>
      </div>
      <form class="settings-list" @submit.prevent>
        <label v-for="config in configs" :key="config.id">
          {{ config.description || config.configKey }}
          <input v-model="config.configValue" @blur="saveConfig(config)" />
        </label>
      </form>
    </article>
  </section>
  <section class="table-panel">
    <div class="section-heading">
      <h2>系统日志</h2>
    </div>
    <ul class="log-list">
      <li v-for="log in logs" :key="log.id">
        <span>{{ log.createdAt }}</span>
        <strong>{{ log.operatorName || 'system' }}</strong>
        <p>{{ log.action }} {{ log.detail }}</p>
      </li>
    </ul>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../api/library'

const users = ref([])
const configs = ref([])
const logs = ref([])
const managers = computed(() => users.value.filter((user) => user.role !== 'READER'))

async function load() {
  users.value = (await api.users()).data
  configs.value = (await api.configs()).data
  logs.value = (await api.logs()).data
}

async function createAdmin() {
  const suffix = Date.now().toString().slice(-4)
  await api.createUser({
    username: `admin${suffix}`,
    password: '123456',
    realName: `管理员${suffix}`,
    role: 'LIBRARIAN',
    status: 'ACTIVE'
  })
  await load()
}

async function disable(user) {
  await api.disableUser(user.id)
  await load()
}

async function saveConfig(config) {
  await api.updateConfig(config)
}

onMounted(load)
</script>

