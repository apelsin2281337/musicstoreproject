import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from './auth'
import { userApi } from '@/services/api'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)
  const error = ref(null)

  const total = computed(() => {
    return items.value.reduce((sum, track) => sum + (track.trackPrice || 0), 0)
  })

  const count = computed(() => items.value.length)

  const isEmpty = computed(() => items.value.length === 0)

  function addItem(track) {
    if (!items.value.find(t => t.trackId === track.trackId)) {
      items.value.push({ ...track })
    }
  }

  function removeItem(trackId) {
    items.value = items.value.filter(t => t.trackId !== trackId)
  }

  function clear() {
    items.value = []
  }

  function isInCart(trackId) {
    return items.value.some(t => t.trackId === trackId)
  }

  async function checkout() {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn || !authStore.userId) {
      throw new Error('Необходимо авторизоваться')
    }
    if (isEmpty.value) {
      throw new Error('Корзина пуста')
    }

    loading.value = true
    error.value = null
    try {
      const trackIds = items.value.map(t => t.trackId)
      await userApi.buyTracks(authStore.userId, trackIds)
      items.value = []
      return true
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    items,
    loading,
    error,
    total,
    count,
    isEmpty,
    addItem,
    removeItem,
    clear,
    isInCart,
    checkout
  }
})
