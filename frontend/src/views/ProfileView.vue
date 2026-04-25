<template>
  <section class="content-split">
    <form class="section-block form-stack" @submit.prevent="saveProfile">
      <div class="section-heading">
        <h2>个人资料</h2>
      </div>
      <label>姓名<input v-model="profile.realName" required /></label>
      <label>电话<input v-model="profile.phone" type="tel" /></label>
      <label>邮箱<input v-model="profile.email" type="email" /></label>
      <button class="primary-btn" type="submit">保存资料</button>
    </form>
    <form class="section-block form-stack" @submit.prevent="changePassword">
      <div class="section-heading">
        <h2>修改密码</h2>
      </div>
      <label>原密码<input v-model="password.oldPassword" type="password" autocomplete="current-password" required /></label>
      <label>新密码<input v-model="password.newPassword" type="password" autocomplete="new-password" required /></label>
      <button class="primary-btn" type="submit">更新密码</button>
      <p v-if="message" class="success-text" role="status">{{ message }}</p>
    </form>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api/library'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const message = ref('')
const profile = reactive({ realName: '', phone: '', email: '' })
const password = reactive({ oldPassword: '', newPassword: '' })

async function saveProfile() {
  await api.updateProfile(profile)
  await auth.refreshMe()
  message.value = '资料已保存'
}

async function changePassword() {
  await api.changePassword(password)
  password.oldPassword = ''
  password.newPassword = ''
  message.value = '密码已更新'
}

onMounted(() => {
  Object.assign(profile, auth.user)
})
</script>
