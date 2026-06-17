<template>
  <router-link :to="`/album/${album.albumId}`" class="album-card" data-testid="album-card">
    <div class="cover" data-testid="album-cover">
      <svg width="32" height="32" viewBox="0 0 24 24" fill="currentColor" opacity="0.4">
        <circle cx="12" cy="12" r="10"/>
        <circle cx="12" cy="12" r="3"/>
      </svg>
    </div>
    <div class="info" data-testid="album-info">
      <h4 data-testid="album-title">{{ album.albumTitle }}</h4>
      <p data-testid="album-artist">{{ album.albumArtist?.artistName || '—' }}</p>
      <span v-if="album.albumReleaseYear" class="year" data-testid="album-year">{{ album.albumReleaseYear }}</span>
    </div>
  </router-link>
</template>

<script setup>
defineProps({ album: { type: Object, required: true } })
</script>

<style scoped>
.album-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: white;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
}

.album-card:hover {
  border-color: #999;
}

.cover {
  width: 64px;
  height: 64px;
  background: #f5f5f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #2563eb;
}

.info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.info h4 {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info p {
  font-size: 13px;
  color: #666;
}

.year {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
</style>
