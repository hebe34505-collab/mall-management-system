import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '../api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(sessionStorage.getItem('mall_token') || '')
  const user = ref(JSON.parse(sessionStorage.getItem('mall_user') || 'null'))
  const isLogin = computed(() => Boolean(token.value))
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function persist() {
    if (token.value) sessionStorage.setItem('mall_token', token.value)
    else sessionStorage.removeItem('mall_token')
    if (user.value) sessionStorage.setItem('mall_user', JSON.stringify(user.value))
    else sessionStorage.removeItem('mall_user')
  }

  async function login(payload) {
    const data = await authApi.login(payload)
    token.value = data.token
    user.value = data.user
    persist()
    return data.user
  }

  async function fetchMe() {
    const data = await authApi.me()
    user.value = data
    persist()
    return data
  }

  async function logout() {
    try {
      if (token.value) await authApi.logout()
    } catch {
      // 令牌已经失效时，本地仍然要清掉登录态。
    }
    token.value = ''
    user.value = null
    persist()
  }

  function updateUser(next) {
    user.value = next
    persist()
  }

  return { token, user, isLogin, isAdmin, login, fetchMe, logout, updateUser }
})
