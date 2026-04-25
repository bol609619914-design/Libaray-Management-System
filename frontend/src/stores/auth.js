import { defineStore } from 'pinia'
import { api } from '../api/library'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('library_token') || '',
    user: JSON.parse(localStorage.getItem('library_user') || 'null')
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token && state.user),
    role: (state) => state.user?.role || 'GUEST'
  },
  actions: {
    async login(form) {
      const res = await api.login(form)
      this.token = res.data.token
      this.user = res.data.user
      localStorage.setItem('library_token', this.token)
      localStorage.setItem('library_user', JSON.stringify(this.user))
    },
    async refreshMe() {
      const res = await api.me()
      this.user = res.data
      localStorage.setItem('library_user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('library_token')
      localStorage.removeItem('library_user')
    }
  }
})

