<template>
  <div class="album-page container">
    <div v-if="musicStore.loading" class="loading"><div class="spinner"></div></div>
    <template v-else-if="album">
      <div class="album-header">
        <h1>{{ album.albumTitle }}</h1>
        <p><router-link :to="`/artist/${album.albumArtist?.artistId}`">{{ album.albumArtist?.artistName }}</router-link> - {{ album.albumReleaseYear }}</p>
      </div>
      <div class="tracklist">
        <div v-for="(t, i) in albumTracks" :key="t.trackId" class="track-row">
          <span>{{ i + 1 }}</span>
          <div class="info"><h4>{{ t.trackTitle }}</h4></div>
          <span class="price">{{ formatPrice(t.trackPrice) }}</span>
          <template v-if="authStore.isLoggedIn && libraryStore.isOwned(t.trackId)">
            <button class="btn btn-sm btn-success" @click="download(t)">Скачать</button>
          </template>
          <template v-else-if="authStore.isLoggedIn">
            <button v-if="!cartStore.isInCart(t.trackId)" class="btn btn-sm" @click="addToCart(t)">В корзину</button>
            <span v-else>В корзине</span>
          </template>
          <template v-else>
            <router-link to="/login" class="btn btn-sm">Войти</router-link>
          </template>
        </div>
      </div>
    </template>
    <div v-else class="empty-state"><p>Не найден</p></div>
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useMusicStore } from '@/stores/music'
import { useCartStore } from '@/stores/cart'
import { useLibraryStore } from '@/stores/library'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const musicStore = useMusicStore()
const cartStore = useCartStore()
const libraryStore = useLibraryStore()
const authStore = useAuthStore()
const toast = useToast()

const album = computed(() => musicStore.currentAlbum)
const albumTracks = computed(() => musicStore.albumTracks)

function formatPrice(price) { return price ? `$${price.toFixed(2)}` : 'Бесплатно' }
function addToCart(t) { cartStore.addItem(t); toast.success(`"${t.trackTitle}" добавлен`) }
function download(t) { libraryStore.downloadTrack(t.trackId) }

onMounted(() => musicStore.fetchAlbum(route.params.id))
</script>

<style scoped>
.album-header { margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px solid #e5e5e5; }
.album-header h1 { font-size: 24px; margin-bottom: 8px; }
.album-header p { color: #666; }
.tracklist { background: white; border: 1px solid #e5e5e5; border-radius: 4px; }
.track-row { display: flex; align-items: center; gap: 12px; padding: 12px 16px; border-bottom: 1px solid #e5e5e5; }
.track-row:last-child { border-bottom: none; }
.track-row span:first-child { width: 24px; color: #888; }
.track-row .info { flex: 1; }
.track-row h4 { font-size: 14px; }
.track-row .price { color: #2563eb; }
</style>
