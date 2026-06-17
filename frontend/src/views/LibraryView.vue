<template>
  <div class="library-page container" data-testid="library-page">
    <h1 class="page-title" data-testid="library-title">Медиатека</h1>

    <div v-if="libraryStore.loading" class="loading" data-testid="loading-spinner"><div class="spinner"></div></div>
    <div v-else-if="libraryStore.tracks.length === 0" class="empty-state" data-testid="library-empty">
      <p>Нет купленных треков</p>
      <router-link to="/browse" class="btn" data-testid="library-catalog-link">Каталог</router-link>
    </div>
    <div v-else class="track-list" data-testid="library-track-list">
      <div v-for="t in libraryStore.tracks" :key="t.trackId" class="track-item" data-testid="library-track-item">
        <div class="track-info">
          <h4 data-testid="library-track-title">{{ t.trackTitle }}</h4>
          <p data-testid="library-track-artist">{{ t.trackArtist?.artistName }}</p>
        </div>
        <div class="actions">
          <button class="btn btn-sm btn-playlist" @click="openPlaylistModal(t)" data-testid="library-playlist-btn">+</button>
          <button class="btn btn-sm btn-success" @click="download(t)" data-testid="library-download-btn">↓</button>
        </div>
      </div>
    </div>

    <div v-if="showPlaylistModal" class="modal" @click.self="showPlaylistModal = false" data-testid="library-playlist-modal">
      <div class="modal-content" data-testid="library-playlist-modal-content">
        <h3 data-testid="library-playlist-modal-title">Добавить в плейлист</h3>
        <div class="playlist-list" data-testid="library-playlist-list">
          <button v-for="p in libraryStore.playlists" :key="p.playlistId" class="playlist-btn" @click="addToPlaylist(p)" data-testid="library-playlist-item">
            {{ p.playlistTitle }}
          </button>
        </div>
        <button class="btn btn-secondary" @click="showPlaylistModal = false" data-testid="library-playlist-cancel">Отмена</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useLibraryStore } from '@/stores/library'
import { useToast } from '@/composables/useToast'

const libraryStore = useLibraryStore()
const toast = useToast()
const showPlaylistModal = ref(false)
const selectedTrack = ref(null)

function download(track) {
  libraryStore.downloadTrack(track.trackId)
}

function openPlaylistModal(track) {
  selectedTrack.value = track
  showPlaylistModal.value = true
}

async function addToPlaylist(playlist) {
  if (libraryStore.playlists.length === 0) {
    await libraryStore.fetchPlaylists()
  }
  await libraryStore.addToPlaylist(playlist.playlistId, selectedTrack.value.trackId)
  toast.success(`Добавлен в "${playlist.playlistTitle}"`)
  showPlaylistModal.value = false
}

onMounted(async () => {
  await libraryStore.fetchLibrary()
  if (libraryStore.playlists.length === 0) {
    await libraryStore.fetchPlaylists()
  }
})

onMounted(() => libraryStore.fetchLibrary())
</script>

<style scoped>
.track-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.track-item {
  background: white;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.track-info { flex: 1; }
.track-info h4 { font-size: 14px; margin: 0; }
.track-info p { font-size: 13px; color: #888; margin: 0; }
.actions { display: flex; gap: 8px; }
.btn-playlist { background: #7c3aed; }
.btn-playlist:hover { background: #6d28d9; }
.modal { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: white; padding: 24px; border-radius: 8px; width: 300px; }
.modal-content h3 { font-size: 16px; margin: 0 0 16px; }
.playlist-list { display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px; }
.playlist-btn { text-align: left; padding: 10px 12px; background: #f5f5f5; border: 1px solid #e5e5e5; border-radius: 4px; cursor: pointer; font-size: 14px; }
.playlist-btn:hover { background: #e5e5e5; }
</style>
