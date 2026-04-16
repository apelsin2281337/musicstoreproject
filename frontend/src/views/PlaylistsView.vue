<template>
  <div class="playlists-page container">
    <div class="header">
      <h1 class="page-title">Плейлисты</h1>
      <button class="btn" @click="showCreateModal = true">+ Создать</button>
    </div>

    <div v-if="libraryStore.loading" class="loading"><div class="spinner"></div></div>
    <div v-else-if="libraryStore.playlists.length === 0" class="empty-state"><p>Нет плейлистов</p></div>
    <div v-else class="list">
      <div v-for="p in libraryStore.playlists" :key="p.playlistId" class="item">
        <h4>{{ p.playlistTitle }}</h4>
        <span>{{ p.playlistTracks?.length || 0 }} треков</span>
          <button class="btn btn-link" @click="confirmDelete(p)">Удалить</button>
      </div>
    </div>

    <div v-if="showCreateModal" class="modal">
      <h3>Новый плейлист</h3>
      <form @submit.prevent="createPlaylist">
        <input v-model="newPlaylistTitle" class="form-input" placeholder="Название" required />
        <div class="actions">
          <button type="button" class="btn btn-secondary" @click="showCreateModal = false">Отмена</button>
          <button type="submit" class="btn">Создать</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useLibraryStore } from '@/stores/library'
import { useToast } from '@/composables/useToast'

const libraryStore = useLibraryStore()
const toast = useToast()
const showCreateModal = ref(false)
const newPlaylistTitle = ref('')

async function createPlaylist() {
  if (!newPlaylistTitle.value) return
  await libraryStore.createPlaylist(newPlaylistTitle.value)
  toast.success('Создан')
  showCreateModal.value = false
  newPlaylistTitle.value = ''
  await libraryStore.fetchPlaylists()
}

async function confirmDelete(p) {
  if (confirm(`Удалить "${p.playlistTitle}"?`)) {
    await libraryStore.deletePlaylist(p.playlistId)
    toast.success('Удален')
    await libraryStore.fetchPlaylists()
  }
}

onMounted(() => libraryStore.fetchPlaylists())
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.list { background: white; border: 1px solid #e5e5e5; border-radius: 4px; }
.item { display: flex; align-items: center; gap: 12px; padding: 12px 16px; border-bottom: 1px solid #e5e5e5; }
.item:last-child { border-bottom: none; }
.item h4 { flex: 1; font-size: 14px; }
.item span { color: #888; font-size: 13px; }
.modal { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; }
.modal form { background: white; padding: 24px; border-radius: 4px; width: 300px; }
.modal h3 { font-size: 16px; margin-bottom: 16px; }
.modal .actions { display: flex; gap: 8px; margin-top: 16px; justify-content: flex-end; }
</style>
