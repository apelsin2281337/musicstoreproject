<template>
  <div class="app-layout">
    <AppHeader />
    <main class="main-content">
      <router-view />
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useLibraryStore } from '@/stores/library'
import AppHeader from '@/components/AppHeader.vue'

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
