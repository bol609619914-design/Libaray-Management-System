<template>
  <main class="login-page">
    <section class="login-copy" aria-labelledby="login-title">
      <div class="login-brand">
        <span class="brand-mark">LF</span>
        <strong>LibraFlow</strong>
      </div>
      <h1 id="login-title">现代图书馆管理系统</h1>
      <p>高效 · 智能 · 连接每一座图书馆</p>
      <div class="login-illustration" aria-hidden="true">
        <div class="shelf">
          <span></span><span></span><span></span><span></span>
        </div>
        <div class="desk-screen"></div>
        <div class="desk-base"></div>
        <div class="light-disc"></div>
      </div>
    </section>
    <form class="login-card" @submit.prevent="submit" aria-describedby="login-helper">
      <div>
        <h2>欢迎登录 LibraFlow</h2>
        <p id="login-helper">请使用您的账号登录系统</p>
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
        记住我
        <a href="#" @click.prevent>忘记密码?</a>
      </label>
      <p v-if="error" class="form-error" role="alert">{{ error }}</p>
      <button class="primary-btn" type="submit" :disabled="loading">{{ loading ? '登录中...' : '进入系统' }}</button>
      <div class="login-divider"><span>或</span></div>
      <button class="sso-btn" type="button">SSO 单点登录</button>
      <a class="contact-admin" href="#" @click.prevent>联系管理员</a>
      <div class="demo-accounts" aria-label="演示账号">
        <span>reader / reader123</span>
        <span>librarian / admin123</span>
        <span>superadmin / root123</span>
      </div>
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
