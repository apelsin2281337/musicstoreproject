<template>
  <div class="app-layout">
    <AppHeader />
    <main class="main-content">
      <router-view />
    </main>
    <AppFooter />
    <Toast />
  </div>
</template>

<script setup>
import { provide, ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useLibraryStore } from '@/stores/library'
import AppHeader from '@/components/AppHeader.vue'
import Toast from '@/components/Toast.vue'

const toasts = ref([])
provide('toasts', toasts)

const authStore = useAuthStore()
const libraryStore = useLibraryStore()

onMounted(() => {
  authStore.init()
  if (authStore.isLoggedIn) {
    libraryStore.fetchLibrary()
  }
})
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 24px 0;
}
</style>
