<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>Регистрация</h1>
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <div v-if="success" class="alert alert-success">{{ success }}</div>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label class="form-label">Имя пользователя</label>
          <input v-model="username" type="text" class="form-input" required minlength="3" />
        </div>
        <div class="form-group">
          <label class="form-label">Пароль</label>
          <input v-model="password" type="password" class="form-input" required minlength="4" />
        </div>
        <button type="submit" class="btn" :disabled="loading">{{ loading ? '...' : 'Зарегистрироваться' }}</button>
      </form>
      <p class="footer">Есть аккаунт? <router-link to="/login">Войти</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref(null)
const success = ref(null)

async function handleRegister() {
  loading.value = true
  error.value = null
  try {
    await authStore.register(username.value, password.value)
    success.value = 'Регистрация успешна!'
    setTimeout(() => router.push('/login'), 2000)
  } catch (e) {
    error.value = e.message || 'Ошибка'
  } finally {
    loading.value = false
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
