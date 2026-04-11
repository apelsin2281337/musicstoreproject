<template>
  <div class="track-card">
    <div class="cover">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>
      </svg>
    </div>
    <div class="info">
      <h4 class="title">{{ track.trackTitle }}</h4>
      <p class="artist">{{ track.trackArtist?.artistName || '—' }}</p>
    </div>
    <div class="right">
      <span class="price">{{ formatPrice(track.trackPrice) }}</span>
      <template v-if="authStore.isLoggedIn && libraryStore.isOwned(track.trackId)">
        <button class="btn btn-sm btn-success" @click="download">↓</button>
      </template>
      <template v-else-if="authStore.isLoggedIn">
        <button v-if="!cartStore.isInCart(track.trackId)" class="btn btn-sm" @click="addToCart">+</button>
        <span v-else class="added">✓</span>
      </template>
      <template v-else>
        <router-link to="/login" class="btn btn-sm">Войти</router-link>
      </template>
    </div>
  </div>
</template>

<script setup>
import { useCartStore } from '@/stores/cart'
import { useLibraryStore } from '@/stores/library'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const props = defineProps({ track: { type: Object, required: true } })
const cartStore = useCartStore()
const libraryStore = useLibraryStore()
const authStore = useAuthStore()
const toast = useToast()

function formatPrice(p) { return p ? `$${p.toFixed(2)}` : '—' }
function addToCart() { cartStore.addItem(props.track); toast.success('Добавлено') }
function download() { libraryStore.downloadTrack(props.track.trackId) }
</script>

<style scoped>
.track-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: white;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
}

.track-card:hover {
  border-color: #ccc;
}

.cover {
  width: 48px;
  height: 48px;
  background: #f0f0f0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
}

.title {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.artist {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.price {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.added {
  color: #16a34a;
  font-size: 16px;
}

.btn-sm {
  padding: 6px 10px;
  font-size: 16px;
}
</style>
