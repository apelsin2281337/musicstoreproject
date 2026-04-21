<template>
  <div class="home">
    <section class="hero">
      <h1>apelsinstore</h1>
      <p>Магазин музыки</p>
      <router-link to="/browse" class="btn">Каталог</router-link>
    </section>

    <section class="section container">
      <h2>Рекомендованные треки</h2>
      <div v-if="musicStore.loading" class="loading"><div class="spinner"></div></div>
      <div v-else-if="musicStore.recommendedTracks.length === 0" class="empty-state"><p>Треки не найдены</p></div>
      <div v-else class="grid">
        <TrackCard v-for="t in musicStore.recommendedTracks.slice(0, 6)" :key="t.trackId" :track="t" />
      </div>
    </section>

    <section class="section container">
      <h2>Артисты</h2>
      <div v-if="musicStore.loading" class="loading"><div class="spinner"></div></div>
      <div v-else-if="musicStore.artists.length === 0" class="empty-state"><p>Артисты не найдены</p></div>
      <div v-else class="grid">
        <ArtistCard v-for="a in musicStore.artists.slice(0, 6)" :key="a.artistId" :artist="a" />
      </div>
    </section>

    <section class="section container">
      <h2>Альбомы</h2>
      <div v-if="musicStore.loading" class="loading"><div class="spinner"></div></div>
      <div v-else-if="musicStore.albums.length === 0" class="empty-state"><p>Альбомы не найдены</p></div>
      <div v-else class="grid">
        <AlbumCard v-for="a in musicStore.albums.slice(0, 6)" :key="a.albumId" :album="a" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useMusicStore } from '@/stores/music'
import { useLibraryStore } from '@/stores/library'
import { useAuthStore } from '@/stores/auth'
import TrackCard from '@/components/TrackCard.vue'
import ArtistCard from '@/components/ArtistCard.vue'
import AlbumCard from '@/components/AlbumCard.vue'
import PlaylistCard from '@/components/PlaylistCard.vue'

const musicStore = useMusicStore()
const libraryStore = useLibraryStore()
const authStore = useAuthStore()

onMounted(async () => {
  await Promise.all([
    musicStore.fetchRecommendedTracks(),
    musicStore.fetchArtists(),
    musicStore.fetchAlbums()
  ])
  if (authStore.isLoggedIn) {
    await libraryStore.fetchLibrary()
    await libraryStore.fetchPlaylists()
  }
})
</script>

<style scoped>
.hero {
  background: white;
  padding: 48px 24px;
  text-align: center;
  margin-bottom: 32px;
  border-bottom: 1px solid #e5e5e5;
}

.hero h1 { font-size: 28px; margin-bottom: 8px; }
.hero p { color: #666; margin-bottom: 16px; }

.section { margin-bottom: 32px; }
.section h2 { font-size: 18px; font-weight: 600; margin-bottom: 16px; }

.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

@media (min-width: 640px) {
  .grid { grid-template-columns: repeat(2, 1fr); }
}

.grid-playlists {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

@media (min-width: 640px) {
  .grid-playlists { grid-template-columns: repeat(2, 1fr); }
}
</style>
