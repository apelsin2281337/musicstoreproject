import { defineStore } from 'pinia'
import { ref } from 'vue'
import { publicApi } from '@/services/api'

export const useMusicStore = defineStore('music', () => {
  const artists = ref([])
  const albums = ref([])
  const genres = ref([])
  const popularTracks = ref([])
  const recommendedTracks = ref([])
  const featuredPlaylists = ref([])
  const currentArtist = ref(null)
  const currentAlbum = ref(null)
  const artistTracks = ref([])
  const artistAlbums = ref([])
  const albumTracks = ref([])
  const genreTracks = ref([])

  const loading = ref(false)
  const error = ref(null)

  async function fetchGenres() {
    loading.value = true
    error.value = null
    try {
      genres.value = await publicApi.getGenres()
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function fetchGenreTracks(genreId) {
    loading.value = true
    error.value = null
    try {
      genreTracks.value = await publicApi.getGenreTracks(genreId)
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function fetchArtists(params = {}) {
    loading.value = true
    error.value = null
    try {
      artists.value = await publicApi.getArtists(params)
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function fetchArtist(id) {
    loading.value = true
    error.value = null
    try {
      currentArtist.value = await publicApi.getArtist(id)
      artistTracks.value = await publicApi.getArtistTracks(id)
      artistAlbums.value = await publicApi.getArtistAlbums(id)
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function fetchAlbums(params = {}) {
    loading.value = true
    error.value = null
    try {
      albums.value = await publicApi.getAlbums(params)
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function fetchAlbum(id) {
    loading.value = true
    error.value = null
    try {
      currentAlbum.value = await publicApi.getAlbum(id)
      albumTracks.value = await publicApi.getAlbumTracks(id)
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function fetchPopularTracks() {
    loading.value = true
    error.value = null
    try {
      popularTracks.value = await publicApi.getPopularTracks()
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function fetchRecommendedTracks() {
    loading.value = true
    error.value = null
    try {
      recommendedTracks.value = await publicApi.getRecommendedTracks()
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  function clearCurrent() {
    currentArtist.value = null
    currentAlbum.value = null
    artistTracks.value = []
    artistAlbums.value = []
    albumTracks.value = []
  }

  return {
    artists,
    albums,
    genres,
    popularTracks,
    recommendedTracks,
    featuredPlaylists,
    currentArtist,
    currentAlbum,
    artistTracks,
    artistAlbums,
    albumTracks,
    genreTracks,
    loading,
    error,
    fetchArtists,
    fetchArtist,
    fetchAlbums,
    fetchAlbum,
    fetchPopularTracks,
    fetchRecommendedTracks,
    fetchGenres,
    fetchGenreTracks,
    clearCurrent
  }
})
