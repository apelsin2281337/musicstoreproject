<template>
  <div class="browse container" data-testid="browse-page">
    <div class="page-header">
      <h1 class="page-title" data-testid="browse-title">Каталог</h1>
    </div>

    <div class="filters" data-testid="browse-filters">
      <input v-model="searchQuery" type="text" placeholder="Поиск..." class="form-input" style="max-width: 300px;" data-testid="browse-search-input" />
      <div class="tabs" data-testid="browse-tabs">
        <button :class="{ active: activeTab === 'tracks' }" @click="activeTab = 'tracks'" data-testid="browse-tab-tracks">Треки</button>
        <button :class="{ active: activeTab === 'artists' }" @click="activeTab = 'artists'" data-testid="browse-tab-artists">Артисты</button>
        <button :class="{ active: activeTab === 'albums' }" @click="activeTab = 'albums'" data-testid="browse-tab-albums">Альбомы</button>
        <button :class="{ active: activeTab === 'genres' }" @click="activeTab = 'genres'" data-testid="browse-tab-genres">Жанры</button>
        <button :class="{ active: activeTab === 'playlists'}" @click="activeTab = 'playlists'" data-testid="browse-tab-playlists">Плейлисты</button>
      </div>
    </div>

    <div v-if="musicStore.loading" class="loading" data-testid="loading-spinner"><div class="spinner"></div></div>
    <div v-else-if="activeTab === 'tracks'">
      <div v-if="filteredTracks.length === 0" class="empty-state" data-testid="empty-tracks"><p>Не найдено</p></div>
      <div v-else class="track-list" data-testid="browse-tracks-list"><TrackCard v-for="t in filteredTracks" :key="t.trackId" :track="t" /></div>
    </div>
    <div v-else-if="activeTab === 'artists'">
      <div v-if="filteredArtists.length === 0" class="empty-state" data-testid="empty-artists"><p>Не найдено</p></div>
      <div v-else class="grid" data-testid="browse-artists-grid"><ArtistCard v-for="a in filteredArtists" :key="a.artistId" :artist="a" /></div>
    </div>
    <div v-else-if="activeTab === 'albums'">
      <div v-if="filteredAlbums.length === 0" class="empty-state" data-testid="empty-albums"><p>Не найдено</p></div>
      <div v-else class="grid" data-testid="browse-albums-grid"><AlbumCard v-for="a in filteredAlbums" :key="a.albumId" :album="a" /></div>
    </div>
    <div v-else-if="activeTab === 'genres'">
      <div v-if="musicStore.genres.length === 0" class="empty-state" data-testid="empty-genres"><p>Жанры не найдены</p></div>
      <div v-else class="grid" data-testid="browse-genres-grid">
        <router-link v-for="g in musicStore.genres" :key="g.genreId" :to="`/genre/${g.genreId}`" class="genre-card" data-testid="genre-card">
          <h4>{{ g.genreName }}</h4>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useMusicStore } from '@/stores/music'
import { useLibraryStore } from '@/stores/library'
import { useAuthStore } from '@/stores/auth'
import TrackCard from '@/components/TrackCard.vue'
import ArtistCard from '@/components/ArtistCard.vue'
import AlbumCard from '@/components/AlbumCard.vue'

const musicStore = useMusicStore()
const libraryStore = useLibraryStore()
const authStore = useAuthStore()
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
  musicStore.loading = true
  await Promise.all([musicStore.fetchPopularTracks(), musicStore.fetchArtists(), musicStore.fetchAlbums(), musicStore.fetchGenres()])
  musicStore.loading = false
  if (authStore.isLoggedIn) {
    await libraryStore.fetchLibrary()
    await libraryStore.fetchPlaylists()
  }
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
@media (min-width: 640px) { .grid { grid-template-columns: repeat(2, 1fr); } }
.track-list { display: flex; flex-direction: column; gap: 8px; }
.genre-card { background: white; padding: 16px; border: 1px solid #e5e5e5; border-radius: 6px; text-decoration: none; color: inherit; }
.genre-card:hover { border-color: #999; }
.genre-card h4 { font-size: 14px; margin: 0; }
</style>
