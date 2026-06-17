<template>
  <div class="playlist-card" data-testid="playlist-card">
    <div class="cover" data-testid="playlist-cover">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
        <path d="M15 6H3v2h12V6zm0 4H3v2h12v-2zM3 16h8v-2H3v2zM17 6v8.18c-.31-.11-.65-.18-1-.18-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3V8h3V6h-5z"/>
      </svg>
    </div>
    <div class="info" data-testid="playlist-info">
      <p class="title" data-testid="playlist-title">{{ playlist.playlistTitle }}</p>
      <p class="meta" data-testid="playlist-track-count">{{ trackCount }} {{ trackCountText }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  playlist: { type: Object, required: true }
})

const trackCount = computed(() => props.playlist.trackCount || 0)
const trackCountText = computed(() => {
  const c = trackCount.value
  if (c === 1) return 'трек'
  if (c >= 2 && c <= 4) return 'трека'
  return 'треков'
})
</script>

<style scoped>
.playlist-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: white;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  cursor: pointer;
}

.playlist-card:hover {
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

.meta {
  font-size: 12px;
  color: #666;
}
</style>