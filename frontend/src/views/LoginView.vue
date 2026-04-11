<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>Вход</h1>
      <div v-if="authStore.error" class="alert alert-error">{{ authStore.error }}</div>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label class="form-label">Имя пользователя</label>
          <input v-model="username" type="text" class="form-input" required />
        </div>
        <div class="form-group">
          <label class="form-label">Пароль</label>
          <input v-model="password" type="password" class="form-input" required />
        </div>
        <button type="submit" class="btn" :disabled="authStore.loading">{{ authStore.loading ? '...' : 'Войти' }}</button>
      </form>
      <p class="footer">Нет аккаунта? <router-link to="/register">Регистрация</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const toast = useToast()

const username = ref('')
const password = ref('')

async function handleLogin() {
  const success = await authStore.login(username.value, password.value)
  if (success) {
    toast.success('Успешно!')
    router.push(route.query.redirect || '/')
  }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; padding: 48px 16px; }
.auth-card { width: 100%; max-width: 360px; }
.auth-card h1 { font-size: 20px; margin-bottom: 20px; }
.auth-card .btn { width: 100%; }
.footer { text-align: center; margin-top: 16px; font-size: 14px; color: #888; }
</style>
