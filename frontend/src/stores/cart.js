import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from './auth'
import { userApi } from '@/services/api'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const albums = ref([])
  const loading = ref(false)
  const error = ref(null)

  const total = computed(() => {
    const trackTotal = items.value.reduce((sum, track) => sum + (track.trackPrice || 0), 0)
    const albumTotal = albums.value.reduce((sum, album) => sum + (album.albumPrice || 0), 0)
    return trackTotal + albumTotal
  })

  const count = computed(() => items.value.length + albums.value.length)

  const isEmpty = computed(() => items.value.length === 0 && albums.value.length === 0)

  function addItem(track) {
    if (!items.value.find(t => t.trackId === track.trackId)) {
      items.value.push({ ...track })
    }
  }

  function addAlbum(album) {
    if (!albums.value.find(a => a.albumId === album.albumId)) {
      albums.value.push({ ...album })
    }
  }

  function removeItem(trackId) {
    items.value = items.value.filter(t => t.trackId !== trackId)
  }

  function removeAlbum(albumId) {
    albums.value = albums.value.filter(a => a.albumId !== albumId)
  }

  function clear() {
    items.value = []
    albums.value = []
  }

  function isInCart(trackId) {
    return items.value.some(t => t.trackId === trackId)
  }

  function isAlbumInCart(albumId) {
    return albums.value.some(a => a.albumId === albumId)
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
      const albumIds = albums.value.map(a => a.albumId)
      
      if (trackIds.length > 0) {
        await userApi.buyTracks(authStore.userId, trackIds)
      }
      if (albumIds.length > 0) {
        for (const albumId of albumIds) {
          await userApi.buyAlbum(authStore.userId, albumId)
        }
      }
      
      items.value = []
      albums.value = []
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
    albums,
    loading,
    error,
    total,
    count,
    isEmpty,
    addItem,
    addAlbum,
    removeItem,
    removeAlbum,
    clear,
    isInCart,
    isAlbumInCart,
    checkout
  }
})
