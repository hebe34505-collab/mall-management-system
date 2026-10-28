import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('mall_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body.code === 'number' && body.code !== 0) {
      const message = body.message || '请求失败'
      if (!response.config.silent) ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return body?.data
  },
  async (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || '网络异常，请稍后重试'
    if (status === 401) {
      sessionStorage.removeItem('mall_token')
      sessionStorage.removeItem('mall_user')
      const { default: router } = await import('../router')
      const current = router.currentRoute.value
      const target = current.path.startsWith('/admin') ? '/admin/login' : '/login'
      if (current.path !== target && current.path !== '/login' && current.path !== '/register') {
        router.push({ path: target, query: { redirect: current.fullPath } })
      }
    }
    if (!error.config?.silent && status !== 401) {
      ElMessage.error(message)
    }
    return Promise.reject(new Error(message))
  }
)

export default http
