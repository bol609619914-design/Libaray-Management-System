<template>
  <section class="super-page">
    <section class="super-hero">
      <div>
        <p class="eyebrow">系统控制台</p>
        <h2>权限、规则与审计</h2>
        <p>集中管理管理员账号、借阅规则、全局配置和操作日志。</p>
      </div>
      <button class="primary-btn" type="button" @click="createAdmin">新增管理员</button>
    </section>

    <section class="super-metrics" aria-label="系统概览">
      <article v-for="item in metrics" :key="item.label">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </article>
    </section>

    <section class="super-grid">
      <article class="table-panel">
        <div class="section-heading">
          <div>
            <h2>管理员权限</h2>
            <p>新增、禁用管理员并查看当前权限状态。</p>
          </div>
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
            <tbody v-if="managers.length">
              <tr v-for="user in managers" :key="user.id">
                <td>{{ user.username }}</td>
                <td>{{ user.realName }}</td>
                <td>{{ roleText(user.role) }}</td>
                <td><span class="status" :class="user.status === 'ACTIVE' ? 'green' : 'red'">{{ statusText(user.status) }}</span></td>
                <td><button class="tiny-btn danger-btn" type="button" @click="disable(user)">禁用</button></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-if="!managers.length" class="empty-state compact-empty">
          <div class="empty-icon" aria-hidden="true">LF</div>
          <h3>暂无管理员账号</h3>
          <p>点击新增管理员，为图书馆后台分配运营账号。</p>
        </div>
      </article>

      <article class="section-block">
        <div class="section-heading">
          <div>
            <h2>系统配置</h2>
            <p>借阅时长、续借次数和逾期收费规则。</p>
          </div>
        </div>
        <form class="settings-list" @submit.prevent>
          <label v-for="config in configs" :key="config.id">
            {{ config.description || config.configKey }}
            <input v-model="config.configValue" @blur="saveConfig(config)" />
          </label>
        </form>
      </article>
    </section>

    <section class="table-panel log-panel">
      <div class="section-heading">
        <div>
          <h2>系统日志</h2>
          <p>记录关键操作、配置变更与账号管理行为。</p>
        </div>
      </div>
      <ul v-if="logs.length" class="log-list audit-list">
        <li v-for="log in logs" :key="log.id">
          <span>{{ log.createdAt }}</span>
          <strong>{{ log.operatorName || 'system' }}</strong>
          <p>{{ log.action }} {{ log.detail }}</p>
        </li>
      </ul>
      <div v-else class="empty-state compact-empty">
        <div class="empty-icon" aria-hidden="true">LF</div>
        <h3>暂无系统日志</h3>
      </div>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../api/library'

const users = ref([])
const configs = ref([])
const logs = ref([])
const managers = computed(() => users.value.filter((user) => user.role !== 'READER'))
const metrics = computed(() => [
  { label: '管理员', value: managers.value.length, hint: '含超级管理员' },
  { label: '系统配置', value: configs.value.length, hint: '规则项' },
  { label: '操作日志', value: logs.value.length, hint: '最近记录' },
  { label: '备份恢复', value: '就绪', hint: '数据库可导出' }
])

function roleText(role) {
  return { LIBRARIAN: '图书管理员', SUPER_ADMIN: '超级管理员' }[role] || role
}

function statusText(status) {
  return { ACTIVE: '正常', DISABLED: '禁用' }[status] || status
}

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
