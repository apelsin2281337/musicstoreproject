<template>
  <div class="cart-page container" data-testid="cart-page">
    <h1 class="page-title" data-testid="cart-title">Корзина</h1>

    <div v-if="cartStore.isEmpty" class="empty-state" data-testid="cart-empty">
      <p>Корзина пуста</p>
      <router-link to="/browse" class="btn" data-testid="cart-catalog-link">Каталог</router-link>
    </div>

    <div v-else class="cart-layout">
      <div class="items" data-testid="cart-items">
        <div v-for="item in cartStore.albums" :key="item.albumId" class="item album-item" data-testid="cart-album-item">
          <div class="item-info">
            <h4 data-testid="cart-item-title">{{ item.albumTitle }}</h4>
            <p data-testid="cart-item-artist">{{ item.albumArtist?.artistName }}</p>
            <span class="item-type" data-testid="cart-item-type">Альбом</span>
          </div>
          <span class="price" data-testid="cart-item-price">{{ formatPrice(item.albumPrice) }}</span>
          <button class="btn btn-link" @click="removeAlbum(item.albumId)" data-testid="cart-remove-btn">Удалить</button>
        </div>
        <div v-for="item in cartStore.items" :key="item.trackId" class="item" data-testid="cart-track-item">
          <div class="item-info">
            <h4 data-testid="cart-item-title">{{ item.trackTitle }}</h4>
            <p data-testid="cart-item-artist">{{ item.trackArtist?.artistName }}</p>
            <span class="item-type" data-testid="cart-item-type">Трек</span>
          </div>
          <span class="price" data-testid="cart-item-price">{{ formatPrice(item.trackPrice) }}</span>
          <button class="btn btn-link" @click="removeItem(item.trackId)" data-testid="cart-remove-btn">Удалить</button>
        </div>
      </div>

      <div class="summary" data-testid="cart-summary">
        <p data-testid="cart-total">Итого: {{ formatPrice(cartStore.total) }}</p>
        <button class="btn btn-success" :disabled="!authStore.isLoggedIn || cartStore.loading" @click="checkout" data-testid="cart-checkout-btn">
          {{ cartStore.loading ? '...' : 'Оформить' }}
        </button>
        <p v-if="!authStore.isLoggedIn"><router-link to="/login" data-testid="cart-login-link">Войдите</router-link> для заказа</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { useLibraryStore } from '@/stores/library'
import { useToast } from '@/composables/useToast'

const cartStore = useCartStore()
const authStore = useAuthStore()
const libraryStore = useLibraryStore()
const toast = useToast()

function formatPrice(price) { return price ? `$${price.toFixed(2)}` : 'Бесплатно' }
function removeItem(id) { cartStore.removeItem(id) }
function removeAlbum(id) { cartStore.removeAlbum(id) }

async function checkout() {
  try {
    await cartStore.checkout()
    await libraryStore.fetchLibrary()
    toast.success('Заказ оформлен!')
  } catch (e) { toast.error(e.message) }
}
</script>

<style scoped>
.cart-layout { display: grid; grid-template-columns: 1fr 250px; gap: 24px; }
.items { display: flex; flex-direction: column; gap: 8px; }
.item { display: flex; align-items: center; gap: 12px; padding: 12px; background: white; border: 1px solid #e5e5e5; border-radius: 4px; }
.item h4 { font-size: 14px; margin-bottom: 2px; }
.item p { font-size: 13px; color: #888; }
.price { color: #2563eb; margin-left: auto; }
.summary { padding: 16px; background: white; border: 1px solid #e5e5e5; border-radius: 4px; height: fit-content; }
.summary p:first-of-type { font-size: 18px; font-weight: 600; margin-bottom: 12px; }
.summary p:last-of-type { font-size: 13px; margin-top: 8px; }
</style>
