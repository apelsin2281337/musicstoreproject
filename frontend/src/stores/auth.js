import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(false)
  const error = ref(null)

  const isLoggedIn = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.userRole === 'ADMIN')
  const userId = computed(() => user.value?.userId)
  const username = computed(() => user.value?.userUsername)

  function init() {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      try {
        user.value = JSON.parse(savedUser)
      } catch {
        localStorage.removeItem('user')
      }
    }
  }

  async function login(username, password) {
    loading.value = true
    error.value = null
    try {
      const userData = await authApi.login(username, password)
      user.value = userData
      localStorage.setItem('user', JSON.stringify(userData))
      localStorage.setItem('token', userData.userId || '')
      return true
    } catch (e) {
      error.value = e.message
      return false
    } finally {
      loading.value = false
    }
  }

  async function register(username, password, role = 'USER') {
    loading.value = true
    error.value = null
    try {
      const userData = await authApi.register(username, password, role)
      return userData
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  function logout() {
    user.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }

  return {
    user,
    loading,
    error,
    isLoggedIn,
    isAdmin,
    userId,
    username,
    init,
    login,
    register,
    logout
  }
})
