<template>
  <div class="playlist-view container" data-testid="playlist-view">
    <div v-if="loading" class="loading" data-testid="loading-spinner"><div class="spinner"></div></div>
    <template v-else-if="playlist">
      <div class="header">
        <div>
          <h1 class="page-title" data-testid="playlist-title">{{ playlist.playlistTitle }}</h1>
          <p class="subtitle" data-testid="playlist-track-count">{{ tracks.length }} треков</p>
        </div>
        <button class="btn btn-secondary" @click="goBack" data-testid="playlist-back-btn">Назад</button>
      </div>

<div v-if="tracks.length === 0" class="empty-state" data-testid="playlist-empty">
          <p>В плейлисте нет треков</p>
          <router-link to="/browse" class="btn" data-testid="playlist-catalog-link">Каталог</router-link>
        </div>
        <div v-else class="track-list" data-testid="playlist-track-list">
        <div v-for="t in tracks" :key="t.trackId" class="track-item" data-testid="playlist-track-item">
          <div class="track-info">
            <router-link :to="`/track/${t.trackId}`" class="track-title" data-testid="playlist-track-link">{{ t.trackTitle }}</router-link>
            <p data-testid="playlist-track-artist">{{ t.trackArtist?.artistName }}</p>
          </div>
          <button class="btn btn-sm btn-success" @click="download(t)" data-testid="playlist-download-btn">↓</button>
          <button class="btn btn-sm btn-danger" @click="removeTrack(t)" data-testid="playlist-remove-btn">×</button>
        </div>
      </div>
    </template>
    <div v-else class="empty-state" data-testid="playlist-not-found"><p>Плейлист не найден</p></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '@/services/api'
import { useLibraryStore } from '@/stores/library'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const libraryStore = useLibraryStore()
const authStore = useAuthStore()
const toast = useToast()

const playlist = ref(null)
const tracks = ref([])
const loading = ref(true)

async function fetchPlaylist() {
  loading.value = true
  try {
    playlist.value = await userApi.getPlaylist(route.params.id)
    if (playlist.value.playlistOwner?.userId !== authStore.userId) {
      toast.error('Нет доступа')
      router.push('/playlists')
      return
    }
    tracks.value = playlist.value.playlistTracks || []
  } catch (e) {
    toast.error('Ошибка загрузки')
  } finally {
    loading.value = false
  }
}

async function removeTrack(track) {
  if (playlist.value?.playlistOwner?.userId !== authStore.userId) {
    toast.error('Нет доступа')
    return
  }
  if (!confirm(`Удалить "${track.trackTitle}" из плейлиста?`)) return
  try {
    await libraryStore.removeFromPlaylist(route.params.id, track.trackId)
    tracks.value = tracks.value.filter(t => t.trackId !== track.trackId)
    toast.success('Удален')
  } catch (e) {
    toast.error('Ошибка')
  }
}

function download(track) {
  libraryStore.downloadTrack(track.trackId)
}

function goBack() {
  router.back()
}

onMounted(fetchPlaylist)
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.page-title { font-size: 24px; margin: 0; }
.subtitle { color: #888; font-size: 14px; margin: 4px 0 0; }
.track-list { display: flex; flex-direction: column; gap: 8px; }
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
.track-title { font-size: 14px; font-weight: 500; color: inherit; text-decoration: none; display: block; }
.track-title:hover { text-decoration: underline; }
.track-info p { font-size: 13px; color: #888; margin: 0; }
.actions { display: flex; gap: 8px; }
</style>