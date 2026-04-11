<template>
  <div class="cart-page container">
    <h1 class="page-title">Корзина</h1>

    <div v-if="cartStore.isEmpty" class="empty-state">
      <p>Корзина пуста</p>
      <router-link to="/browse" class="btn">Каталог</router-link>
    </div>

    <div v-else class="cart-layout">
      <div class="items">
        <div v-for="item in cartStore.items" :key="item.trackId" class="item">
          <div class="item-info">
            <h4>{{ item.trackTitle }}</h4>
            <p>{{ item.trackArtist?.artistName }}</p>
          </div>
          <span class="price">{{ formatPrice(item.trackPrice) }}</span>
          <button class="btn btn-link" @click="removeItem(item.trackId)">Удалить</button>
        </div>
      </div>

      <div class="summary">
        <p>Итого: {{ formatPrice(cartStore.total) }}</p>
        <button class="btn btn-success" :disabled="!authStore.isLoggedIn || cartStore.loading" @click="checkout">
          {{ cartStore.loading ? '...' : 'Оформить' }}
        </button>
        <p v-if="!authStore.isLoggedIn"><router-link to="/login">Войдите</router-link> для заказа</p>
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
