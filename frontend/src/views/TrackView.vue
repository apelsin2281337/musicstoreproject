<template>
  <div class="track-page container">
    <div v-if="musicStore.loading" class="loading"><div class="spinner"></div></div>
    <template v-else-if="track">
      <div class="track-header">
        <h1>{{ track.trackTitle }}</h1>
        <p>
          <router-link :to="`/artist/${track.trackArtist?.artistId}`">{{ track.trackArtist?.artistName }}</router-link>
          <span v-if="track.trackAlbum"> - <router-link :to="`/album/${track.trackAlbum.albumId}`">{{ track.trackAlbum.albumTitle }}</router-link></span>
        </p>
        <div class="track-meta">
          <span class="price">{{ formatPrice(track.trackPrice) }}</span>
          <span class="downloads">{{ track.trackDownloadCount || 0 }} скачиваний</span>
        </div>
        <div class="track-actions">
          <template v-if="authStore.isLoggedIn && libraryStore.isOwned(track.trackId)">
            <button class="btn btn-success" @click="download">Скачать</button>
          </template>
          <template v-else-if="authStore.isLoggedIn">
            <button class="btn btn-primary" @click="buy">Купить</button>
          </template>
          <template v-else>
            <router-link to="/login" class="btn btn-primary">Войти</router-link>
          </template>
        </div>
      </div>

      <section v-if="license" class="license-section">
        <h2>Лицензия</h2>
        <div class="license-card">
          <p><strong>Контракт:</strong> {{ license.licenseContractNumber }}</p>
          <p><strong>Владелец:</strong> {{ license.licenseOwnerName }}</p>
          <p><strong>Опубликован:</strong> {{ license.uploader}}</p>
          <p><strong>Действует:</strong> {{ formatDate(license.licenseStartDate) }} - {{ formatDate(license.licenseExpirationDate) }}</p>
        </div>
      </section>

      <section class="reviews-section">
        <h2>Отзывы</h2>
        <div v-if="authStore.isLoggedIn" class="review-form">
          <select v-model="newRating">
            <option :value="1">1</option>
            <option :value="2">2</option>
            <option :value="3">3</option>
            <option :value="4">4</option>
            <option :value="5">5</option>
          </select>
          <input v-model="newComment" placeholder="Комментарий" />
          <button @click="submitReview" class="btn">Отправить</button>
        </div>
        <div v-for="r in reviews" :key="r.reviewId" class="review-card">
          <div class="review-header">
            <span class="author">{{ r.reviewUser?.userUsername || 'Аноним' }}</span>
            <span class="rating">{{ r.reviewRating }}/5</span>
            <span class="date">{{ new Date(r.reviewDate).toLocaleDateString() }}</span>
          </div>
          <p>{{ r.reviewComment }}</p>
        </div>
        <p v-if="reviews.length === 0">Пока нет отзывов</p>
      </section>
    </template>
    <div v-else class="empty-state"><p>Трек не найден</p></div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useMusicStore } from '@/stores/music'
import { useLibraryStore } from '@/stores/library'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { userApi, reviewApi, licenseApi, publicApi } from '@/services/api'

const route = useRoute()
const musicStore = useMusicStore()
const libraryStore = useLibraryStore()
const authStore = useAuthStore()
const toast = useToast()

const track = ref(null)
const reviews = ref([])
const license = ref(null)
const newRating = ref(5)
const newComment = ref('')

function formatPrice(price) { return price ? `$${price.toFixed(2)}` : 'Бесплатно' }
function formatDate(date) { return date ? new Date(date).toLocaleDateString() : '' }

async function loadTrack() {
  try {
    track.value = await publicApi.getTrack(route.params.id)
  } catch (e) {
    toast.error('Ошибка загрузки трека')
  }
}

function download() { libraryStore.downloadTrack(track.value.trackId) }
async function buy() {
  try {
    await userApi.buyTracks(authStore.userId, [track.value.trackId])
    toast.success('Трек куплен!')
    await libraryStore.fetchLibrary()
    await loadTrack()
    loadReviews()
  } catch (e) {
    toast.error('Ошибка: ' + e.message)
  }
}
async function submitReview() {
  try {
    await reviewApi.addReview(authStore.userId, track.value.trackId, null, newRating.value, newComment.value)
    toast.success('Отзыв отправлен!')
    await loadReviews()
    newComment.value = ''
  } catch (e) {
    toast.error('Ошибка: ' + e.message)
  }
}
async function loadReviews() {
  reviews.value = await reviewApi.getTrackReviews(route.params.id)
}
async function loadLicense() {
  try {
    license.value = await licenseApi.getTrackLicense(route.params.id)
  } catch (e) {
    license.value = null
  }
}

onMounted(async () => {
  await loadTrack()
  loadReviews()
  loadLicense()
})
</script>

<style scoped>
.track-header { margin-bottom: 32px; padding-bottom: 24px; border-bottom: 1px solid #e5e5e5; }
.track-header h1 { font-size: 28px; margin-bottom: 8px; }
.track-header p { color: #666; margin-bottom: 12px; }
.track-meta { display: flex; gap: 24px; margin-bottom: 16px; }
.track-meta .price { font-size: 20px; font-weight: 600; color: #2563eb; }
.track-meta .downloads { color: #888; }
.track-actions { display: flex; gap: 12px; }

.license-section { margin-bottom: 32px; }
.license-section h2 { font-size: 18px; margin-bottom: 12px; }
.license-card { background: white; padding: 16px; border: 1px solid #e5e5e5; border-radius: 4px; }
.license-card p { margin-bottom: 8px; }
.license-card p:last-child { margin-bottom: 0; }

.reviews-section { margin-bottom: 32px; }
.reviews-section h2 { font-size: 18px; margin-bottom: 16px; }
.review-form { display: flex; gap: 8px; margin-bottom: 16px; }
.review-form select, .review-form input { padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
.review-form input { flex: 1; }
.review-card { background: white; padding: 12px; border: 1px solid #e5e5e5; border-radius: 4px; margin-bottom: 8px; }
.review-header { display: flex; justify-content: space-between; margin-bottom: 8px; gap: 12px; }
.review-header .author { font-weight: 500; }
.review-header .rating { font-weight: 600; }
.review-header .date { color: #888; font-size: 12px; }
</style>
