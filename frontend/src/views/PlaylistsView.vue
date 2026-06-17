<template>
  <div class="playlists-page container" data-testid="playlists-page">
    <div class="header">
      <h1 class="page-title" data-testid="playlists-title">Плейлисты</h1>
      <button class="btn" @click="showCreateModal = true" data-testid="playlists-create-btn">+ Создать</button>
    </div>

    <div v-if="libraryStore.loading" class="loading" data-testid="loading-spinner"><div class="spinner"></div></div>
    <div v-else-if="libraryStore.playlists.length === 0" class="empty-state" data-testid="playlists-empty"><p>Нет плейлистов</p></div>
    <div v-else class="list" data-testid="playlists-list">
      <div v-for="p in libraryStore.playlists" :key="p.playlistId" class="item" data-testid="playlists-list-item">
        <router-link :to="`/playlist/${p.playlistId}`" class="item-link" data-testid="playlists-item-link">
          <h4 data-testid="playlists-item-title">{{ p.playlistTitle }}</h4>
          <span data-testid="playlists-item-count">{{ p.playlistTracks?.length || 0 }} треков</span>
        </router-link>
          <button class="btn btn-link btn-danger" @click="confirmDelete(p)" data-testid="playlists-delete-btn">×</button>
      </div>
    </div>

    <div v-if="showCreateModal" class="modal" @click.self="closeModal" data-testid="playlists-create-modal">
      <div class="modal-content" data-testid="playlists-create-modal-content">
        <h3 data-testid="playlists-create-modal-title">Новый плейлист</h3>
        <form @submit.prevent="createPlaylist" data-testid="playlists-create-form">
          <input v-model="newPlaylistTitle" class="form-input" placeholder="Название" required autofocus data-testid="playlists-create-input" />
          <div class="actions">
            <button type="button" class="btn btn-secondary" @click="closeModal" data-testid="playlists-create-cancel">Отмена</button>
            <button type="submit" class="btn" data-testid="playlists-create-submit">Создать</button>
          </div>
        </form>
      </div>
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
  closeModal()
  await libraryStore.fetchPlaylists()
}

function closeModal() {
  showCreateModal.value = false
  newPlaylistTitle.value = ''
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
.item-link { flex: 1; display: flex; align-items: center; gap: 12px; color: inherit; text-decoration: none; }
.item-link h4 { flex: 1; font-size: 14px; margin: 0; }
.item span { color: #888; font-size: 13px; }
.btn-danger { color: #dc2626; }
.modal { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: white; padding: 24px; border-radius: 8px; width: 300px; }
.modal-content h3 { font-size: 16px; margin: 0 0 16px; }
.modal .actions { display: flex; gap: 8px; margin-top: 16px; justify-content: flex-end; }
</style>
