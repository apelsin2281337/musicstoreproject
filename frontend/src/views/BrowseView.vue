<template>
  <div class="browse container">
    <div class="page-header">
      <h1 class="page-title">Каталог</h1>
    </div>

    <div class="filters">
      <input v-model="searchQuery" type="text" placeholder="Поиск..." class="form-input" style="max-width: 300px;" />
      <div class="tabs">
        <button :class="{ active: activeTab === 'tracks' }" @click="activeTab = 'tracks'">Треки</button>
        <button :class="{ active: activeTab === 'artists' }" @click="activeTab = 'artists'">Артисты</button>
        <button :class="{ active: activeTab === 'albums' }" @click="activeTab = 'albums'">Альбомы</button>
      </div>
    </div>

    <div v-if="musicStore.loading" class="loading"><div class="spinner"></div></div>
    <div v-else-if="activeTab === 'tracks'">
      <div v-if="filteredTracks.length === 0" class="empty-state"><p>Не найдено</p></div>
      <div v-else class="grid"><TrackCard v-for="t in filteredTracks" :key="t.trackId" :track="t" /></div>
    </div>
    <div v-else-if="activeTab === 'artists'">
      <div v-if="filteredArtists.length === 0" class="empty-state"><p>Не найдено</p></div>
      <div v-else class="grid"><ArtistCard v-for="a in filteredArtists" :key="a.artistId" :artist="a" /></div>
    </div>
    <div v-else>
      <div v-if="filteredAlbums.length === 0" class="empty-state"><p>Не найдено</p></div>
      <div v-else class="grid"><AlbumCard v-for="a in filteredAlbums" :key="a.albumId" :album="a" /></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useMusicStore } from '@/stores/music'
import TrackCard from '@/components/TrackCard.vue'
import ArtistCard from '@/components/ArtistCard.vue'
import AlbumCard from '@/components/AlbumCard.vue'

const musicStore = useMusicStore()
const searchQuery = ref('')
const activeTab = ref('tracks')

const filteredTracks = computed(() => {
  if (!searchQuery.value) return musicStore.popularTracks
  const q = searchQuery.value.toLowerCase()
  return musicStore.popularTracks.filter(t => t.trackTitle?.toLowerCase().includes(q) || t.trackArtist?.artistName?.toLowerCase().includes(q))
})

const filteredArtists = computed(() => {
  if (!searchQuery.value) return musicStore.artists
  const q = searchQuery.value.toLowerCase()
  return musicStore.artists.filter(a => a.artistName?.toLowerCase().includes(q))
})

const filteredAlbums = computed(() => {
  if (!searchQuery.value) return musicStore.albums
  const q = searchQuery.value.toLowerCase()
  return musicStore.albums.filter(a => a.albumTitle?.toLowerCase().includes(q) || a.albumArtist?.artistName?.toLowerCase().includes(q))
})

onMounted(async () => {
  await Promise.all([musicStore.fetchPopularTracks(), musicStore.fetchArtists(), musicStore.fetchAlbums()])
})

watch(activeTab, () => { searchQuery.value = '' })
</script>

<style scoped>
.filters { margin-bottom: 24px; }
.tabs { display: flex; gap: 8px; margin-top: 16px; }
.tabs button { padding: 8px 16px; background: #e5e5e5; border: none; border-radius: 4px; font-size: 14px; cursor: pointer; }
.tabs button.active { background: #2563eb; color: white; }
.tabs button:hover:not(.active) { background: #d4d4d4; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
@media (min-width: 640px) { .grid { grid-template-columns: repeat(4, 1fr); } }
</style>
