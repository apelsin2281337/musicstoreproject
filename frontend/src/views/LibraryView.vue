<template>
  <div class="library-page container">
    <h1 class="page-title">Медиатека</h1>

    <div v-if="libraryStore.loading" class="loading"><div class="spinner"></div></div>
    <div v-else-if="libraryStore.tracks.length === 0" class="empty-state">
      <p>Нет купленных треков</p>
      <router-link to="/browse" class="btn">Каталог</router-link>
    </div>
    <div v-else class="track-list">
      <div v-for="(t, i) in libraryStore.tracks" :key="t.trackId" class="track-row">
        <span>{{ i + 1 }}</span>
        <div class="info">
          <h4>{{ t.trackTitle }}</h4>
          <p>{{ t.trackArtist?.artistName }}</p>
        </div>
        <button class="btn btn-sm btn-success" @click="download(t)">Скачать</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useLibraryStore } from '@/stores/library'

const libraryStore = useLibraryStore()

function download(track) {
  libraryStore.downloadTrack(track.trackId)
}

onMounted(() => libraryStore.fetchLibrary())
</script>

<style scoped>
.track-list { background: white; border: 1px solid #e5e5e5; border-radius: 4px; }
.track-row { display: flex; align-items: center; gap: 12px; padding: 12px 16px; border-bottom: 1px solid #e5e5e5; }
.track-row:last-child { border-bottom: none; }
.track-row span:first-child { width: 24px; color: #888; }
.track-row .info { flex: 1; }
.track-row h4 { font-size: 14px; }
.track-row p { font-size: 13px; color: #888; }
</style>
