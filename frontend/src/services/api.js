const API_BASE = '/api'

function getAuthHeaders() {
  const headers = { 'Content-Type': 'application/json' }
  const token = localStorage.getItem('token')
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}

async function handleResponse(response) {
  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || response.statusText)
  }
  const contentType = response.headers.get('content-type')
  if (contentType && contentType.includes('application/json')) {
    return response.json()
  }
  return response.text()
}

export const api = {
  async get(endpoint) {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      headers: getAuthHeaders()
    })
    return handleResponse(res)
  },

  async post(endpoint, data) {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(data)
    })
    return handleResponse(res)
  },

  async postForm(endpoint, formData) {
    const headers = {}
    const token = localStorage.getItem('token')
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    const res = await fetch(`${API_BASE}${endpoint}`, {
      method: 'POST',
      headers,
      body: formData
    })
    return handleResponse(res)
  },

  async put(endpoint, data) {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(data)
    })
    return handleResponse(res)
  },

  async delete(endpoint) {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    })
    return handleResponse(res)
  },

  download(endpoint, filename) {
    const token = localStorage.getItem('token')
    const url = `${API_BASE}${endpoint}`
    const a = document.createElement('a')
    a.href = token ? `${url}?token=${token}` : url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  }
}

export const authApi = {
  async login(username, password) {
    return api.post('/auth/login', { username, password })
  },
  async register(username, password, role = 'USER') {
    return api.post('/auth/register', { username, password, role })
  }
}

export const publicApi = {
  getArtists(params = {}) {
    const query = new URLSearchParams(params).toString()
    return api.get(`/public/artists${query ? '?' + query : ''}`)
  },
  getArtist(id) {
    return api.get(`/public/artists/${id}`)
  },
  getArtistTracks(artistId) {
    return api.get(`/public/artists/${artistId}/tracks`)
  },
  getArtistAlbums(artistId) {
    return api.get(`/public/artists/${artistId}/albums`)
  },
  getAlbums(params = {}) {
    const query = new URLSearchParams(params).toString()
    return api.get(`/public/albums${query ? '?' + query : ''}`)
  },
  getAlbum(id) {
    return api.get(`/public/albums/${id}`)
  },
  getAlbumTracks(albumId) {
    return api.get(`/public/albums/${albumId}/tracks`)
  },
  getTracks(params = {}) {
    const query = new URLSearchParams(params).toString()
    return api.get(`/public/tracks${query ? '?' + query : ''}`)
  },
  getPopularTracks() {
    return api.get('/public/tracks/popular')
  }
}

export const userApi = {
  getUser(id) {
    return api.get(`/user/${id}`)
  },
  getLibrary(userId) {
    return api.get(`/user/${userId}/library`)
  },
  buyTracks(userId, trackIds) {
    return api.post(`/user/${userId}/buy`, trackIds)
  },
  downloadTrack(trackId) {
    api.download(`/user/download/${trackId}`, `track_${trackId}.mp3`)
  },
  prepareDownload(trackId) {
    return api.get(`/user/download/${trackId}`)
  },
  getPlaylists(userId) {
    return api.get(`/user/${userId}/playlists`)
  },
  createPlaylist(userId, title) {
    return api.post(`/user/${userId}/playlists`, { title })
  },
  updatePlaylist(playlistId, title) {
    return api.put(`/user/playlists/${playlistId}`, { title })
  },
  addToPlaylist(playlistId, trackId) {
    return api.post(`/user/playlists/${playlistId}/tracks/${trackId}`)
  },
  removeFromPlaylist(playlistId, trackId) {
    return api.delete(`/user/playlists/${playlistId}/tracks/${trackId}`)
  },
  deletePlaylist(playlistId) {
    return api.delete(`/user/playlists/${playlistId}`)
  }
}

export const adminApi = {
  addArtist(name, description, rating) {
    const formData = new FormData()
    formData.append('name', name)
    formData.append('description', description)
    formData.append('rating', rating)
    return api.postForm('/admin/addartist', formData)
  },
  addAlbum(title, year, price, artistId) {
    const formData = new FormData()
    formData.append('title', title)
    formData.append('year', year)
    formData.append('price', price)
    formData.append('artistId', artistId)
    return api.postForm('/admin/addalbum', formData)
  },
  uploadTrack(file, title, price, artistId, albumId, adminId) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('title', title)
    formData.append('price', price)
    formData.append('artistId', artistId)
    if (albumId) formData.append('albumId', albumId)
    formData.append('adminId', adminId)
    return api.postForm('/admin/uploadtrack', formData)
  },
  promote(userId) {
    return api.post(`/admin/promote/${userId}`, {})
  },
  demote(userId) {
    return api.post(`/admin/demote/${userId}`, {})
  },
  getUsers() {
    return api.get('/admin/users')
  }
}
