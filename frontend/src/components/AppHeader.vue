<template>
  <header class="header">
    <div class="container header-inner">
      <router-link to="/" class="logo">apelsinmusic</router-link>
      <nav class="nav">
        <router-link to="/">Главная</router-link>
        <router-link to="/browse">Каталог</router-link>
        <router-link v-if="authStore.isLoggedIn" to="/library">Медиатека</router-link>
        <router-link v-if="authStore.isAdmin" to="/admin">Админ</router-link>
      </nav>
      <div class="actions">
        <router-link to="/cart">Корзина <span v-if="cartStore.count">({{ cartStore.count }})</span></router-link>
        <template v-if="authStore.isLoggedIn">
          <span>{{ authStore.username }}</span>
          <button class="loginbtn" @click="logout">Выйти</button>
        </template>
        <template v-else>
          <router-link to="/login">Войти</router-link>
          <router-link to="/register">Регистрация</router-link>
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
