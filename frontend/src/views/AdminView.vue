<template>
  <div class="admin-page container">
    <h1 class="page-title">Админ</h1>

    <div class="tabs">
      <button :class="{ active: activeTab === 'users' }" @click="activeTab = 'users'">Пользователи</button>
      <button :class="{ active: activeTab === 'artists' }" @click="activeTab = 'artists'">Артисты</button>
      <button :class="{ active: activeTab === 'albums' }" @click="activeTab = 'albums'">Альбомы</button>
      <button :class="{ active: activeTab === 'tracks' }" @click="activeTab = 'tracks'">Треки</button>
      <button :class="{ active: activeTab === 'genres' }" @click="activeTab = 'genres'">Жанры</button>
      <button :class="{ active: activeTab === 'logs' }" @click="activeTab = 'logs'; loadLogs()">Логи</button>
    </div>

    <div v-if="activeTab === 'users'">
      <h3>Управление пользователями</h3>
      <div class="users-list">
        <div v-for="user in users" :key="user.userId" class="user-item">
          <div class="user-info">
            <span class="user-name">{{ user.userUsername }}</span>
            <span class="user-role" :class="user.userRole.toLowerCase()">{{ user.userRole }}</span>
          </div>
          <div class="user-actions">
            <button 
              v-if="user.userRole === 'USER'" 
              class="btn btn-sm btn-success" 
              @click="promoteUser(user.userId)"
              :disabled="loading"
            >
              Промоутить
            </button>
            <button 
              v-else-if="user.userRole === 'ADMIN'" 
              class="btn btn-sm btn-secondary" 
              @click="demoteUser(user.userId)"
              :disabled="loading"
            >
              Демоутить
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'artists'">
      <form @submit.prevent="addArtist">
        <h3>Добавить артиста</h3>
        <div class="form-group"><label class="form-label">Название</label><input v-model="artistForm.name" class="form-input" required /></div>
        <div class="form-group"><label class="form-label">Описание</label><textarea v-model="artistForm.description" class="form-input" rows="2"></textarea></div>
        <button type="submit" class="btn" :disabled="loading">Добавить</button>
      </form>
      <h3 style="margin-top: 32px;">Редактировать артиста</h3>
      <div class="edit-section">
        <select v-model="editArtistId" class="form-input">
          <option value="">Выберите артиста</option>
          <option v-for="a in artists" :key="a.artistId" :value="a.artistId">{{ a.artistName }}</option>
        </select>
        <template v-if="editArtistId">
          <div class="form-group"><label class="form-label">Название</label><input v-model="editArtistForm.name" class="form-input" /></div>
          <div class="form-group"><label class="form-label">Описание</label><textarea v-model="editArtistForm.description" class="form-input" rows="2"></textarea></div>
          <div class="form-group"><label class="form-label">Рейтинг</label><input v-model="editArtistForm.rating" type="number" class="form-input" /></div>
          <button class="btn" @click="updateArtist" :disabled="loading">Сохранить</button>
        </template>
      </div>
    </div>

    <div v-if="activeTab === 'albums'">
      <form @submit.prevent="addAlbum">
        <h3>Добавить альбом</h3>
        <div class="form-group"><label class="form-label">Альбом</label><input v-model="albumForm.title" class="form-input" required /></div>
        <div class="form-group"><label class="form-label">Артист</label>
          <select v-model="albumForm.artistId" class="form-input" required>
            <option value="" disabled>Выберите артиста</option>
            <option v-for="a in artists" :key="a.artistId" :value="a.artistId">{{ a.artistName }}</option>
          </select>
        </div>
        <div class="form-row">
          <div class="form-group"><label class="form-label">Год</label><input v-model="albumForm.year" type="number" class="form-input" /></div>
          <div class="form-group"><label class="form-label">Цена $</label><input v-model="albumForm.price" type="number" step="0.01" class="form-input" /></div>
        </div>
        <button type="submit" class="btn" :disabled="loading">Добавить</button>
      </form>
      <h3 style="margin-top: 32px;">Удалить альбом</h3>
      <div class="delete-section">
        <select v-model="deleteAlbumId" class="form-input">
          <option value="">Выберите альбом</option>
          <option v-for="a in albums" :key="a.albumId" :value="a.albumId">{{ a.albumTitle }} - {{ a.albumArtist?.artistName }}</option>
        </select>
        <button class="btn btn-danger" @click="deleteAlbum" :disabled="!deleteAlbumId || loading">Удалить</button>
      </div>
      <h3 style="margin-top: 32px;">Редактировать альбом</h3>
      <div class="edit-section">
        <select v-model="editAlbumId" class="form-input">
          <option value="">Выберите альбом</option>
          <option v-for="a in albums" :key="a.albumId" :value="a.albumId">{{ a.albumTitle }} - {{ a.albumArtist?.artistName }}</option>
        </select>
        <template v-if="editAlbumId">
          <div class="form-group"><label class="form-label">Название</label><input v-model="editAlbumForm.title" class="form-input" /></div>
          <div class="form-group"><label class="form-label">Артист</label>
            <select v-model="editAlbumForm.artistId" class="form-input">
              <option v-for="a in artists" :key="a.artistId" :value="a.artistId">{{ a.artistName }}</option>
            </select>
          </div>
          <div class="form-row">
            <div class="form-group"><label class="form-label">Год</label><input v-model="editAlbumForm.year" type="number" class="form-input" /></div>
            <div class="form-group"><label class="form-label">Цена $</label><input v-model="editAlbumForm.price" type="number" step="0.01" class="form-input" /></div>
          </div>
          <button class="btn" @click="updateAlbum" :disabled="loading">Сохранить</button>
        </template>
      </div>
    </div>

    <div v-if="activeTab === 'tracks'">
      <form @submit.prevent="uploadTrack">
        <h3>Загрузить трек</h3>
        <div class="form-group"><label class="form-label">Файл</label><input type="file" @change="handleFileChange" accept="audio/*" class="form-input" required /></div>
        <div class="form-group"><label class="form-label">Название</label><input v-model="trackForm.title" class="form-input" /></div>
        <div class="form-group"><label class="form-label">Артист</label>

          <select v-model="trackForm.artistId" class="form-input" required>
            <option value="" disabled>Выберите артиста</option>
            <option v-for="a in artists" :key="a.artistId" :value="a.artistId">{{ a.artistName }}</option>
          </select>
        </div>
        <div class="form-group"><label class="form-label">Альбом</label>
          <select v-model="trackForm.albumId" class="form-input">
            <option value="">Без альбома</option>
            <option v-for="al in filteredAlbums" :key="al.albumId" :value="al.albumId">{{ al.albumTitle }}</option>
          </select>
        </div>
        <div class="form-group"><label class="form-label">Цена $</label><input v-model="trackForm.price" type="number" step="0.01" class="form-input" /></div>
                <div class="form-group"><label class="form-label">Условия лицензирования</label><input v-model="trackForm.licenseTerms" class="form-input" /></div>
        <div class="form-group"><label class="form-label">Жанры</label>
          <div class="checkbox-list">
            <label v-for="g in genres" :key="g.genreId">
              <input type="checkbox" :value="g.genreId" v-model="trackForm.genreIds" /> {{ g.genreName }}
            </label>
          </div>
        </div>
        <button type="submit" class="btn" :disabled="loading || !trackForm.file">Загрузить</button>
      </form>
      <h3 style="margin-top: 32px;">Удалить трек</h3>
      <div class="delete-section">
        <select v-model="deleteTrackId" class="form-input">
          <option value="">Выберите трек</option>
          <option v-for="t in allTracks" :key="t.trackId" :value="t.trackId">{{ t.trackTitle }} - {{ t.trackArtist?.artistName }}</option>
        </select>
        <button class="btn btn-danger" @click="deleteTrack" :disabled="!deleteTrackId || loading">Удалить</button>
      </div>
      <h3 style="margin-top: 32px;">Редактировать трек</h3>
      <div class="edit-section">
        <select v-model="editTrackId" class="form-input">
          <option value="">Выберите трек</option>
          <option v-for="t in allTracks" :key="t.trackId" :value="t.trackId">{{ t.trackTitle }} - {{ t.trackArtist?.artistName }}</option>
        </select>
        <template v-if="editTrackId">
          <div class="form-group"><label class="form-label">Название</label><input v-model="editTrackForm.title" class="form-input" /></div>
          <div class="form-group"><label class="form-label">Артист</label>
            <select v-model="editTrackForm.artistId" class="form-input">
              <option v-for="a in artists" :key="a.artistId" :value="a.artistId">{{ a.artistName }}</option>
            </select>
          </div>
          <div class="form-group"><label class="form-label">Альбом</label>
            <select v-model="editTrackForm.albumId" class="form-input">
              <option value="">Без альбома</option>
              <option v-for="al in albums" :key="al.albumId" :value="al.albumId">{{ al.albumTitle }}</option>
            </select>
          </div>
          <div class="form-group"><label class="form-label">Цена $</label><input v-model="editTrackForm.price" type="number" step="0.01" class="form-input" /></div>
          <div class="form-group"><label class="form-label">Жанры</label>
            <div class="checkbox-list">
              <label v-for="g in genres" :key="g.genreId">
                <input type="checkbox" :value="g.genreId" v-model="editTrackForm.genreIds" /> {{ g.genreName }}
              </label>
            </div>
          </div>
          <button class="btn" @click="updateTrack" :disabled="loading">Сохранить</button>
        </template>
      </div>
    </div>

    <div v-if="activeTab === 'genres'">
      <form @submit.prevent="addGenre">
        <h3>Добавить жанр</h3>
        <div class="form-group"><label class="form-label">Название</label><input v-model="genreForm.name" class="form-input" required /></div>
        <button type="submit" class="btn" :disabled="loading">Добавить</button>
      </form>
      <h3 style="margin-top: 32px;">Удалить жанр</h3>
      <div class="delete-section">
        <select v-model="deleteGenreId" class="form-input">
          <option value="">Выберите жанр</option>
          <option v-for="g in genres" :key="g.genreId" :value="g.genreId">{{ g.genreName }}</option>
        </select>
        <button class="btn btn-danger" @click="deleteGenre" :disabled="!deleteGenreId || loading">Удалить</button>
      </div>
      <h3 style="margin-top: 32px;">Редактировать жанр</h3>
      <div class="edit-section">
        <select v-model="editGenreId" class="form-input">
          <option value="">Выберите жанр</option>
          <option v-for="g in genres" :key="g.genreId" :value="g.genreId">{{ g.genreName }}</option>
        </select>
        <template v-if="editGenreId">
          <div class="form-group"><label class="form-label">Название</label><input v-model="editGenreForm.name" class="form-input" /></div>
          <button class="btn" @click="updateGenre" :disabled="loading">Сохранить</button>
        </template>
      </div>
    </div>

    <div v-if="activeTab === 'logs'">
      <h3>История действий</h3>
      <div class="logs-list">
        <div v-for="log in logs" :key="log.logId" class="log-item">
          <div class="log-header">
            <span class="log-type">{{ log.actionType }}</span>
            <span class="log-date">{{ new Date(log.actionTimestamp).toLocaleString() }}</span>
          </div>
          <p class="log-details">{{ log.actionDetails }}</p>
          <span class="log-admin">Админ: {{ log.admin?.userUsername }}</span>
        </div>
        <p v-if="logs.length === 0">Нет записей</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { adminApi } from '@/services/api'
import { publicApi } from '@/services/api'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const authStore = useAuthStore()
const toast = useToast()
const activeTab = ref('users')

const loading = ref(false)
const artists = ref([])
const albums = ref([])
const users = ref([])
const allTracks = ref([])
const logs = ref([])
const deleteTrackId = ref('')
const deleteAlbumId = ref('')
const deleteGenreId = ref('')
const editArtistId = ref('')
const editAlbumId = ref('')
const editTrackId = ref('')
const editGenreId = ref('')
const editArtistForm = reactive({ name: '', description: '', rating: 0 })
const editAlbumForm = reactive({ title: '', year: 0, price: 0, artistId: '' })
const editTrackForm = reactive({ title: '', price: 0, artistId: '', albumId: '', genreIds: [] })
const editGenreForm = reactive({ name: '' })
const genres = ref([])
const genreForm = reactive({ name: '' })

const artistForm = reactive({ name: '', description: '' })
const albumForm = reactive({ title: '', year: new Date().getFullYear(), price: 9.99, artistId: '' })
const trackForm = reactive({ title: '', price: 0, artistId: '', albumId: '', file: null, licenseTerms: '', genreIds: [] })

watch(editArtistId, (id) => {
  if (!id) { editArtistForm.name = ''; editArtistForm.description = ''; editArtistForm.rating = 0; return }
  const a = artists.value.find(x => x.artistId == id)
  if (a) { editArtistForm.name = a.artistName; editArtistForm.description = a.artistDescription || ''; editArtistForm.rating = a.artistRating || 0 }
})

watch(editAlbumId, (id) => {
  if (!id) { editAlbumForm.title = ''; editAlbumForm.year = 0; editAlbumForm.price = 0; editAlbumForm.artistId = ''; return }
  const a = albums.value.find(x => x.albumId == id)
  if (a) { editAlbumForm.title = a.albumTitle; editAlbumForm.year = a.albumReleaseYear; editAlbumForm.price = a.albumPrice; editAlbumForm.artistId = a.albumArtist?.artistId }
})

watch(editTrackId, (id) => {
  if (!id) { editTrackForm.title = ''; editTrackForm.price = 0; editTrackForm.artistId = ''; editTrackForm.albumId = ''; editTrackForm.genreIds = []; return }
  const t = allTracks.value.find(x => x.trackId == id)
  if (t) { editTrackForm.title = t.trackTitle; editTrackForm.price = t.trackPrice; editTrackForm.artistId = t.trackArtist?.artistId; editTrackForm.albumId = t.trackAlbum?.albumId; editTrackForm.genreIds = t.trackGenres?.map(g => g.genreId) || [] }
})

watch(editGenreId, (id) => {
  if (!id) { editGenreForm.name = ''; return }
  const g = genres.value.find(x => x.genreId == id)
  if (g) editGenreForm.name = g.genreName
})

const filteredAlbums = computed(() => {
  if (!trackForm.artistId) return []
  return albums.value.filter(a => a.albumArtist?.artistId == trackForm.artistId)
})

function handleFileChange(e) {
  trackForm.file = e.target.files[0]
  if (trackForm.file && !trackForm.title) trackForm.title = trackForm.file.name.replace(/\.[^/.]+$/, '').replace(/\.[^.]+$/, '')
}

async function loadUsers() {
  try {
    users.value = await adminApi.getUsers()
  } catch (e) {
    toast.error('Ошибка загрузки пользователей')
  }
}

async function promoteUser(userId) {
  loading.value = true
  try {
    await adminApi.promote(userId, authStore.userId)
    toast.success('Пользователь промоучен')
    await loadUsers()
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function demoteUser(userId) {
  loading.value = true
  try {
    await adminApi.demote(userId, authStore.userId)
    toast.success('Пользователь демоучен')
    await loadUsers()
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function addArtist() {
  loading.value = true
  try { await adminApi.addArtist(artistForm.name, artistForm.description, 0, authStore.userId); toast.success('Добавлено'); artistForm.name = ''; artistForm.description = ''; await loadData() }
  catch (e) { toast.error(e.message) }
  finally { loading.value = false }
}

async function addAlbum() {
  loading.value = true
  try { await adminApi.addAlbum(albumForm.title, albumForm.year, albumForm.price, albumForm.artistId, authStore.userId); toast.success('Добавлено'); albumForm.title = ''; await loadData() }
  catch (e) { toast.error(e.message) }
  finally { loading.value = false }
}

async function uploadTrack() {
  if (!trackForm.file) { toast.error('Выберите файл'); return }
  loading.value = true
  try {
    await adminApi.uploadTrack(trackForm.file, trackForm.title, trackForm.price, trackForm.artistId, trackForm.albumId, authStore.userId, trackForm.licenseTerms, trackForm.genreIds)
    toast.success('Загружено')
    trackForm.title = ''; trackForm.file = null; trackForm.genreIds = []
    await loadData()
  } catch (e) { toast.error(e.message) }
  finally { loading.value = false }
}

async function loadData() {
  artists.value = await publicApi.getArtists()
  albums.value = await publicApi.getAlbums()
  allTracks.value = await publicApi.getTracks()
  genres.value = await publicApi.getGenres()
}

async function loadLogs() {
  try {
    logs.value = await adminApi.getLogs()
  } catch (e) {
    toast.error('Ошибка загрузки логов')
  }
}

async function deleteTrack() {
  if (!deleteTrackId.value) return
  loading.value = true
  try {
    await adminApi.deleteTrack(deleteTrackId.value, authStore.userId)
    toast.success('Трек удален')
    deleteTrackId.value = ''
    allTracks.value = await publicApi.getTracks()
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function deleteAlbum() {
  if (!deleteAlbumId.value) return
  loading.value = true
  try {
    await adminApi.deleteAlbum(deleteAlbumId.value, authStore.userId)
    toast.success('Альбом удален')
    deleteAlbumId.value = ''
    albums.value = await publicApi.getAlbums()
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function addGenre() {
  if (!genreForm.name) return
  loading.value = true
  try {
    await adminApi.addGenre(genreForm.name, authStore.userId)
    toast.success('Жанр добавлен')
    genreForm.name = ''
    genres.value = await publicApi.getGenres()
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function deleteGenre() {
  if (!deleteGenreId.value) return
  loading.value = true
  try {
    await adminApi.deleteGenre(deleteGenreId.value, authStore.userId)
    toast.success('Жанр удален')
    deleteGenreId.value = ''
    genres.value = await publicApi.getGenres()
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function updateArtist() {
  if (!editArtistId.value) return
  loading.value = true
  try {
    const data = {}
    if (editArtistForm.name) data.name = editArtistForm.name
    if (editArtistForm.description) data.description = editArtistForm.description
    if (editArtistForm.rating) data.rating = editArtistForm.rating
    await adminApi.updateArtist(editArtistId.value, data, authStore.userId)
    toast.success('Артист обновлен')
    editArtistId.value = ''
    await loadData()
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function updateAlbum() {
  if (!editAlbumId.value) return
  loading.value = true
  try {
    const data = {}
    if (editAlbumForm.title) data.title = editAlbumForm.title
    if (editAlbumForm.year) data.year = editAlbumForm.year
    if (editAlbumForm.price) data.price = editAlbumForm.price
    if (editAlbumForm.artistId) data.artistId = editAlbumForm.artistId
    await adminApi.updateAlbum(editAlbumId.value, data, authStore.userId)
    toast.success('Альбом обновлен')
    editAlbumId.value = ''
    await loadData()
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function updateTrack() {
  if (!editTrackId.value) return
  loading.value = true
  try {
    const data = {}
    if (editTrackForm.title) data.title = editTrackForm.title
    if (editTrackForm.price) data.price = editTrackForm.price
    if (editTrackForm.artistId) data.artistId = editTrackForm.artistId
    if (editTrackForm.albumId) data.albumId = editTrackForm.albumId
    if (editTrackForm.genreIds.length > 0) data.genreIds = editTrackForm.genreIds
    await adminApi.updateTrack(editTrackId.value, data, authStore.userId)
    toast.success('Трек обновлен')
    editTrackId.value = ''
    await loadData()
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function updateGenre() {
  if (!editGenreId.value || !editGenreForm.name) return
  loading.value = true
  try {
    await adminApi.updateGenre(editGenreId.value, editGenreForm.name, authStore.userId)
    toast.success('Жанр обновлен')
    editGenreId.value = ''
    editGenreForm.name = ''
    await loadData()
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadUsers(); loadData() })
</script>

<style scoped>
.tabs { display: flex; gap: 8px; margin: 16px 0; }
.tabs button { padding: 8px 16px; background: #e5e5e5; border: none; border-radius: 4px; }
.tabs button.active { background: #2563eb; color: white; }
form { background: white; padding: 24px; border: 1px solid #e5e5e5; border-radius: 4px; max-width: 500px; }
form h3 { font-size: 16px; margin-bottom: 16px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.checkbox-list { display: flex; flex-wrap: wrap; gap: 12px; }
.checkbox-list label { display: flex; align-items: center; gap: 4px; font-size: 14px; }

.users-list { display: flex; flex-direction: column; gap: 12px; }
.user-item { display: flex; justify-content: space-between; align-items: center; padding: 16px; background: white; border: 1px solid #e5e5e5; border-radius: 4px; }
.user-info { display: flex; align-items: center; gap: 12px; }
.user-name { font-weight: 500; }
.user-role { padding: 4px 8px; font-size: 12px; border-radius: 4px; text-transform: uppercase; }
.user-role.admin { background: #fef3c7; color: #92400e; }
.user-role.user { background: #e5e7eb; color: #374151; }
.delete-section { display: flex; gap: 12px; margin-top: 12px; }
.delete-section select { flex: 1; }
.edit-section { background: white; padding: 24px; border: 1px solid #e5e5e5; border-radius: 4px; max-width: 500px; margin-top: 12px; }
.edit-section select { margin-bottom: 16px; }
.btn-danger { background: #dc2626; color: white; }
.btn-danger:hover { background: #b91c1c; }
.logs-list { display: flex; flex-direction: column; gap: 12px; }
.log-item { background: white; padding: 16px; border: 1px solid #e5e5e5; border-radius: 4px; }
.log-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.log-type { font-weight: 600; color: #2563eb; }
.log-date { color: #888; font-size: 12px; }
.log-details { margin-bottom: 8px; }
.log-admin { font-size: 12px; color: #666; }
</style>
