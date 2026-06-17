<template>
  <div class="genre-page container" data-testid="genre-page">
    <div v-if="musicStore.loading" class="loading" data-testid="loading-spinner"><div class="spinner"></div></div>
    <template v-else-if="genre">
      <div class="page-header">
        <h1 class="page-title" data-testid="genre-title">{{ genre.genreName }}</h1>
      </div>

      <div v-if="genreTracks.length === 0" class="empty-state" data-testid="empty-genre-tracks"><p>Нет треков в этом жанре</p></div>
      <div v-else class="track-list" data-testid="genre-tracks-list">
        <TrackCard v-for="t in genreTracks" :key="t.trackId" :track="t" />
      </div>
    </template>
    <div v-else class="empty-state" data-testid="genre-not-found"><p>Жанр не найден</p></div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useMusicStore } from '@/stores/music'
import TrackCard from '@/components/TrackCard.vue'

const route = useRoute()
const musicStore = useMusicStore()

const genre = computed(() => musicStore.genres.find(g => g.genreId == route.params.id))
const genreTracks = computed(() => musicStore.genreTracks)

onMounted(async () => {
  await musicStore.fetchGenres()
  await musicStore.fetchGenreTracks(route.params.id)
})
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; }
.track-list { display: flex; flex-direction: column; gap: 8px; }
</style>
