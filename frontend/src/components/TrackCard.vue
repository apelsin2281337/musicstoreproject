<template>
  <div class="track-card">
    <div class="cover">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>
      </svg>
    </div>
    <div class="info">
      <router-link :to="`/track/${track.trackId}`" class="title-link">{{ track.trackTitle }}</router-link>
      <p class="artist">{{ track.trackArtist?.artistName || '—' }}</p>
    </div>
    <div class="right">
      <span class="price">{{ formatPrice(track.trackPrice) }}</span>
      <template v-if="authStore.isLoggedIn && libraryStore.isOwned(track.trackId)">
        <button class="btn btn-sm btn-success" @click="download">↓</button>
        <button class="btn btn-sm btn-playlist" @click="showPlaylistModal = true" title="В плейлист">+</button>
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

  <div v-if="showPlaylistModal" class="modal" @click.self="showPlaylistModal = false">
    <div class="modal-content">
      <h3>Добавить в плейлист</h3>
      <div class="playlist-list">
        <button v-for="p in libraryStore.playlists" :key="p.playlistId" class="playlist-btn" :class="{ 'in-playlist': isInPlaylist(p) }" @click="addToPlaylist(p)">
          <span v-if="isInPlaylist(p)" class="check">✓</span>
          {{ p.playlistTitle }}
        </button>
      </div>
      <button class="btn btn-secondary" @click="showPlaylistModal = false">Отмена</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useCartStore } from '@/stores/cart'
import { useLibraryStore } from '@/stores/library'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const props = defineProps({ track: { type: Object, required: true } })
const cartStore = useCartStore()
const libraryStore = useLibraryStore()
const authStore = useAuthStore()
const toast = useToast()
const showPlaylistModal = ref(false)

function formatPrice(p) { return p ? `$${p.toFixed(2)}` : '—' }
function addToCart() { cartStore.addItem(props.track); toast.success('Добавлено') }
function download() { libraryStore.downloadTrack(props.track.trackId) }
async function addToPlaylist(playlist) {
  if (libraryStore.playlists.length === 0) {
    await libraryStore.fetchPlaylists()
  }
  await libraryStore.addToPlaylist(playlist.playlistId, props.track.trackId)
  toast.success(`Добавлен в "${playlist.playlistTitle}"`)
  showPlaylistModal.value = false
}
function isInPlaylist(playlist) {
  return libraryStore.isInPlaylist(playlist.playlistId, props.track.trackId)
}
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

.title-link {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: inherit;
  text-decoration: none;
}

.title-link:hover {
  text-decoration: underline;
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

.btn-playlist {
  background: #7c3aed;
}

.btn-playlist:hover {
  background: #6d28d9;
}

.modal {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  width: 300px;
}

.modal-content h3 {
  font-size: 16px;
  margin: 0 0 16px;
}

.playlist-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.playlist-btn {
  text-align: left;
  padding: 10px 12px;
  background: #f5f5f5;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.playlist-btn:hover {
  background: #e5e5e5;
}

.playlist-btn.in-playlist {
  background: #dcfce7;
  border-color: #16a34a;
}

.playlist-btn .check {
  color: #16a34a;
  margin-right: 8px;
}
</style>
