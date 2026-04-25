<template>
  <main class="login-page">
    <section class="login-copy" aria-labelledby="login-title">
      <p class="eyebrow">Smart Library Console</p>
      <h1 id="login-title">图书馆管理系统</h1>
      <p>统一管理图书检索、借阅流转、逾期罚款、读者反馈、权限配置与公告发布。</p>
      <div class="demo-accounts" aria-label="演示账号">
        <span>reader / reader123</span>
        <span>librarian / admin123</span>
        <span>superadmin / root123</span>
      </div>
    </section>
    <form class="login-card" @submit.prevent="submit" aria-describedby="login-helper">
      <div>
        <h2>登录</h2>
        <p id="login-helper">请选择角色账号进入对应工作台。</p>
      </div>
      <label>
        用户名
        <input v-model.trim="form.username" autocomplete="username" required />
      </label>
      <label>
        密码
        <input v-model="form.password" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" required />
      </label>
      <label class="checkbox-row">
        <input v-model="showPassword" type="checkbox" />
        显示密码
      </label>
      <p v-if="error" class="form-error" role="alert">{{ error }}</p>
      <button class="primary-btn" type="submit" :disabled="loading">{{ loading ? '登录中...' : '进入系统' }}</button>
    </form>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const form = reactive({ username: 'reader', password: 'reader123' })
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await auth.login(form)
    router.push('/')
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>

