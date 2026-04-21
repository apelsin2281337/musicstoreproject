import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useAuthStore } from './auth'
import { userApi } from '@/services/api'

export const useLibraryStore = defineStore('library', () => {
  const tracks = ref([])
  const playlists = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchLibrary() {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn || !authStore.userId) return

    loading.value = true
    error.value = null
    try {
      const data = await userApi.getLibrary(authStore.userId)
      const seen = new Set()
      tracks.value = data.filter(t => {
        if (seen.has(t.trackId)) return false
        seen.add(t.trackId)
        return true
      })
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function fetchPlaylists() {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn || !authStore.userId) return

    error.value = null
    try {
      playlists.value = await userApi.getPlaylists(authStore.userId)
    } catch (e) {
      error.value = e.message
    }
  }

  async function createPlaylist(title) {
    const authStore = useAuthStore()
    if (!authStore.isLoggedIn || !authStore.userId) return

    try {
      const playlist = await userApi.createPlaylist(authStore.userId, title)
      playlists.value.push(playlist)
      return playlist
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  async function deletePlaylist(playlistId) {
    try {
      await userApi.deletePlaylist(playlistId)
      playlists.value = playlists.value.filter(p => p.playlistId !== playlistId)
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  async function addToPlaylist(playlistId, trackId) {
    try {
      await userApi.addToPlaylist(playlistId, trackId)
      const playlist = playlists.value.find(p => p.playlistId === playlistId)
      if (playlist && playlist.playlistTracks) {
        playlist.playlistTracks.push({ trackId })
      }
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  async function removeFromPlaylist(playlistId, trackId) {
    try {
      await userApi.removeFromPlaylist(playlistId, trackId)
      const playlist = playlists.value.find(p => p.playlistId === playlistId)
      if (playlist && playlist.playlistTracks) {
        playlist.playlistTracks = playlist.playlistTracks.filter(t => t.trackId !== trackId)
      }
    } catch (e) {
      error.value = e.message
      throw e
    }
  }

  function downloadTrack(trackId) {
    userApi.downloadTrack(trackId)
  }

  function isOwned(trackId) {
    return tracks.value.some(t => t.trackId === trackId)
  }

  function isInPlaylist(playlistId, trackId) {
    const playlist = playlists.value.find(p => p.playlistId === playlistId)
    if (!playlist || !playlist.playlistTracks) return false
    return playlist.playlistTracks.some(t => t.trackId === trackId)
  }

  return {
    tracks,
    playlists,
    loading,
    error,
    fetchLibrary,
    fetchPlaylists,
    createPlaylist,
    deletePlaylist,
    addToPlaylist,
    removeFromPlaylist,
    downloadTrack,
    isOwned,
    isInPlaylist
  }
})
