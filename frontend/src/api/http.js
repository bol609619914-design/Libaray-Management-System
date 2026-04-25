import axios from 'axios'

export const http = axios.create({
  baseURL: '/api',
  timeout: 12000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('library_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.message || '请求失败，请稍后重试'
    return Promise.reject(new Error(message))
  }
)

