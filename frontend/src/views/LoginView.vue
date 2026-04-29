<template>
  <main class="login-page">
    <section class="login-copy" aria-labelledby="login-title">
      <div class="login-brand">
        <img class="brand-logo" src="/library-logo.png" alt="" />
        <strong>LibraFlow</strong>
      </div>
      <h1 id="login-title">现代图书馆管理系统</h1>
      <p>高效 · 智能 · 连接每一座图书馆</p>
      <img class="login-illustration" src="/login-library-scene.png" alt="" aria-hidden="true" />
    </section>
    <form class="login-card" @submit.prevent="submit" aria-describedby="login-helper">
      <div>
        <h2>{{ isRegistering ? '注册读者账号' : '欢迎登录 LibraFlow' }}</h2>
        <p id="login-helper">{{ isRegistering ? '注册后默认拥有普通读者权限' : '请使用您的账号登录系统' }}</p>
      </div>
      <label v-if="isRegistering">
        姓名
        <input v-model.trim="form.realName" autocomplete="name" :required="isRegistering" />
      </label>
      <label>
        用户名
        <input v-model.trim="form.username" autocomplete="username" required />
      </label>
      <label>
        密码
        <input v-model="form.password" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" required />
      </label>
      <label v-if="isRegistering">
        邮箱
        <input v-model.trim="form.email" type="email" autocomplete="email" />
      </label>
      <label class="checkbox-row">
        <input v-model="showPassword" type="checkbox" />
        显示密码
        <span class="password-hint">忘记密码请联系管理员</span>
      </label>
      <p v-if="error" class="form-error" role="alert">{{ error }}</p>
      <button class="primary-btn" type="submit" :disabled="loading">{{ submitText }}</button>
      <button class="link-button auth-switch" type="button" @click="toggleMode">
        {{ isRegistering ? '已有账号，返回登录' : '没有账号？注册读者账号' }}
      </button>
    </form>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const form = reactive({ username: '', password: '', realName: '', email: '' })
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)
const isRegistering = ref(false)

const submitText = computed(() => {
  if (loading.value) return isRegistering.value ? '注册中...' : '登录中...'
  return isRegistering.value ? '注册并进入系统' : '进入系统'
})

function toggleMode() {
  error.value = ''
  isRegistering.value = !isRegistering.value
  if (isRegistering.value) {
    form.username = ''
    form.password = ''
    form.realName = ''
    form.email = ''
  } else {
    form.username = ''
    form.password = ''
  }
}

async function submit() {
  loading.value = true
  error.value = ''
  try {
    if (isRegistering.value) {
      await auth.register(form)
    } else {
      await auth.login(form)
    }
    router.push('/')
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>
