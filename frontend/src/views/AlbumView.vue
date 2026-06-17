<template>
  <div class="album-page container" data-testid="album-page">
    <div v-if="musicStore.loading" class="loading" data-testid="loading-spinner"><div class="spinner"></div></div>
    <template v-else-if="album">
      <div class="album-header" data-testid="album-header">
        <h1 data-testid="album-title">{{ album.albumTitle }}</h1>
        <p><router-link :to="`/artist/${album.albumArtist?.artistId}`" data-testid="album-artist-link">{{ album.albumArtist?.artistName }}</router-link> - <span data-testid="album-year">{{ album.albumReleaseYear }}</span></p>
        <div class="album-buy" data-testid="album-buy">
          <span class="price" data-testid="album-price">{{ formatPrice(album.albumPrice) }}</span>
          <template v-if="authStore.isLoggedIn">
            <button v-if="!cartStore.isAlbumInCart(album.albumId)" class="btn btn-primary" @click="addAlbumToCart" data-testid="album-add-cart-btn">В корзину</button>
            <span v-else data-testid="album-in-cart">В корзине</span>
          </template>
          <template v-else>
            <router-link to="/login" class="btn btn-primary" data-testid="album-login-link">Войти</router-link>
          </template>
        </div>
      </div>
      <div class="tracklist" data-testid="album-tracklist">
        <div v-for="(t, i) in albumTracks" :key="t.trackId" class="track-row" data-testid="album-track-row">
          <span>{{ i + 1 }}</span>
          <div class="info"><router-link :to="`/track/${t.trackId}`" data-testid="album-track-link"><h4>{{ t.trackTitle }}</h4></router-link></div>
          <span class="price" data-testid="album-track-price">{{ formatPrice(t.trackPrice) }}</span>
          <template v-if="authStore.isLoggedIn && libraryStore.isOwned(t.trackId)">
            <button class="btn btn-sm btn-success" @click="download(t)" data-testid="album-track-download-btn">Скачать</button>
          </template>
          <template v-else-if="authStore.isLoggedIn">
            <button v-if="!cartStore.isInCart(t.trackId)" class="btn btn-sm" @click="addToCart(t)" data-testid="album-track-add-cart-btn">В корзину</button>
            <span v-else data-testid="album-track-in-cart">В корзине</span>
          </template>
          <template v-else>
            <router-link to="/login" class="btn btn-sm" data-testid="album-track-login-link">Войти</router-link>
          </template>
        </div>
      </div>
      <section class="reviews-section" data-testid="reviews-section">
        <h2 data-testid="reviews-title">Отзывы</h2>
        <div v-if="authStore.isLoggedIn" class="review-form" data-testid="review-form">
          <select v-model="newRating" data-testid="review-rating-select">
            <option :value="1">1</option>
            <option :value="2">2</option>
            <option :value="3">3</option>
            <option :value="4">4</option>
            <option :value="5">5</option>
          </select>
          <input v-model="newComment" placeholder="Комментарий" data-testid="review-comment-input" />
          <button @click="submitReview" class="btn" data-testid="review-submit-btn">Отправить</button>
        </div>
        <div v-for="r in reviews" :key="r.reviewId" class="review-card" data-testid="review-card">
          <div class="review-header">
            <span class="author" data-testid="review-author">{{ r.reviewUser?.userUsername || 'Аноним' }}</span>
            <span class="rating" data-testid="review-rating">{{ r.reviewRating }}/5</span>
            <span class="date" data-testid="review-date">{{ new Date(r.reviewDate).toLocaleDateString() }}</span>
          </div>
          <p data-testid="review-comment">{{ r.reviewComment }}</p>
        </div>
        <p v-if="reviews.length === 0" data-testid="reviews-empty">Пока нет отзывов</p>
      </section>
    </template>
    <div v-else class="empty-state" data-testid="album-not-found"><p>Не найден</p></div>
  </div>
</template>

<script setup>
import { onMounted, computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMusicStore } from '@/stores/music'
import { useCartStore } from '@/stores/cart'
import { useLibraryStore } from '@/stores/library'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { userApi, reviewApi } from '@/services/api'

const route = useRoute()
const router = useRouter()
const musicStore = useMusicStore()
const cartStore = useCartStore()
const libraryStore = useLibraryStore()
const authStore = useAuthStore()
const toast = useToast()
const reviews = ref([])
const newRating = ref(5)
const newComment = ref('')

const album = computed(() => musicStore.currentAlbum)
const albumTracks = computed(() => musicStore.albumTracks)

function formatPrice(price) { return price ? `$${price.toFixed(2)}` : 'Бесплатно' }
function addToCart(t) { cartStore.addItem(t); toast.success(`"${t.trackTitle}" добавлен`) }
function addAlbumToCart() { cartStore.addAlbum(album.value); toast.success(`"${album.value.albumTitle}" добавлен в корзину`) }
function download(t) { libraryStore.downloadTrack(t.trackId) }
async function buyWholeAlbum() {
  try {
    await userApi.buyAlbum(authStore.userId, album.value.albumId)
    toast.success('Альбом успешно куплен!')
    await libraryStore.fetchLibrary()
    await musicStore.fetchAlbum(route.params.id)
    loadReviews()
  } catch (e) {
    toast.error('Ошибка: ' + e.message)
  }
}
async function submitReview() {
  try {
    await reviewApi.addReview(authStore.userId, null, album.value.albumId, newRating.value, newComment.value)
    toast.success('Отзыв отправлен!')
    await loadReviews()
    newComment.value = ''
  } catch (e) {
    toast.error('Ошибка: ' + e.message)
  }
}
async function loadReviews() {
  reviews.value = await reviewApi.getAlbumReviews(route.params.id)
}

onMounted(async () => {
  await musicStore.fetchAlbum(route.params.id)
  loadReviews()
})
</script>

<style scoped>
.album-header { margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px solid #e5e5e5; }
.album-header h1 { font-size: 24px; margin-bottom: 8px; }
.album-header p { color: #666; }
.album-buy { margin-top: 12px; display: flex; align-items: center; gap: 12px; }
.album-buy .price { font-size: 18px; font-weight: 600; color: #2563eb; }
.tracklist { background: white; border: 1px solid #e5e5e5; border-radius: 4px; }
.track-row { display: flex; align-items: center; gap: 12px; padding: 12px 16px; border-bottom: 1px solid #e5e5e5; }
.track-row:last-child { border-bottom: none; }
.track-row span:first-child { width: 24px; color: #888; }
.track-row .info { flex: 1; }
.track-row h4 { font-size: 14px; }
.track-row .price { color: #2563eb; }
.reviews-section { margin-top: 32px; }
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
