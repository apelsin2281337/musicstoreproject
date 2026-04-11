<template>
  <div class="artist-page container">
    <div v-if="musicStore.loading" class="loading"><div class="spinner"></div></div>
    <template v-else-if="artist">
      <div class="artist-header">
        <h1>{{ artist.artistName }}</h1>
        <p>Рейтинг: {{ artist.artistRating || 0 }}</p>
        <p v-if="artist.artistDescription">{{ artist.artistDescription }}</p>
      </div>

      <section v-if="artistAlbums.length > 0">
        <h2>Альбомы</h2>
        <div class="grid"><AlbumCard v-for="a in artistAlbums" :key="a.albumId" :album="a" /></div>
      </section>

      <section v-if="artistTracks.length > 0">
        <h2>Треки</h2>
        <TrackCard v-for="t in artistTracks" :key="t.trackId" :track="t" />
      </section>
    </template>
    <div v-else class="empty-state"><p>Не найден</p></div>
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useMusicStore } from '@/stores/music'
import AlbumCard from '@/components/AlbumCard.vue'
import TrackCard from '@/components/TrackCard.vue'

const route = useRoute()
const musicStore = useMusicStore()

const artist = computed(() => musicStore.currentArtist)
const artistAlbums = computed(() => musicStore.artistAlbums)
const artistTracks = computed(() => musicStore.artistTracks)

onMounted(() => musicStore.fetchArtist(route.params.id))
</script>

<style scoped>
.artist-header { margin-bottom: 32px; padding-bottom: 24px; border-bottom: 1px solid #e5e5e5; }
.artist-header h1 { font-size: 28px; margin-bottom: 8px; }
.artist-header p { color: #666; }
section { margin-bottom: 32px; }
section h2 { font-size: 18px; font-weight: 600; margin-bottom: 16px; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
@media (min-width: 640px) { .grid { grid-template-columns: repeat(4, 1fr); } }
</style>
