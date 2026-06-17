<template>
  <header class="header" data-testid="app-header">
    <div class="container header-inner">
      <router-link to="/" class="logo" data-testid="header-logo">apelsinmusic</router-link>
      <nav class="nav" data-testid="header-nav">
        <router-link to="/" data-testid="nav-home">Главная</router-link>
        <router-link to="/browse" data-testid="nav-browse">Каталог</router-link>
        <router-link v-if="authStore.isLoggedIn" to="/library" data-testid="nav-library">Медиатека</router-link>
        <router-link v-if="authStore.isLoggedIn" to="/playlists" data-testid="nav-playlists">Плейлисты</router-link>
        <router-link v-if="authStore.isAdmin" to="/admin" data-testid="nav-admin">Админ</router-link>
      </nav>
      <div class="actions" data-testid="header-actions">
        <router-link to="/cart" data-testid="header-cart">Корзина <span v-if="cartStore.count" data-testid="cart-count">({{ cartStore.count }})</span></router-link>
        <template v-if="authStore.isLoggedIn">
          <span data-testid="header-username">{{ authStore.username }}</span>
          <button class="loginbtn" @click="logout" data-testid="header-logout">Выйти</button>
        </template>
        <template v-else>
          <router-link to="/login" data-testid="header-login">Войти</router-link>
          <router-link to="/register" data-testid="header-register">Регистрация</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

function logout() {
  authStore.logout()
  router.push('/')
}
</script>

<style scoped>
.header {
  background: white;
  border-bottom: 1px solid #e5e5e5;
}

.header-inner {
  display: flex;
  align-items: center;
  gap: 24px;
  height: 56px;
}

.logo {
  font-weight: 600;
  font-size: 18px;
  color: #333;
}

.nav {
  display: flex;
  gap: 16px;
}

.nav a {
  color: #666;
  font-size: 14px;
}

.nav a:hover {
  color: #333;
}

.actions {
  margin-left: auto;
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.actions a, .actions button {
  color: #666;
}

.actions button {
  background: none;
}
</style>
