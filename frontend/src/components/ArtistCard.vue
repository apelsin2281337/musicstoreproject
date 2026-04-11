<template>
  <router-link :to="`/artist/${artist.artistId}`" class="artist-card">
    <div class="avatar">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
        <circle cx="12" cy="8" r="4"/>
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
      </svg>
    </div>
    <div class="info">
      <h4>{{ artist.artistName }}</h4>
      <p v-if="artist.artistRating">★ {{ artist.artistRating }}</p>
    </div>
  </router-link>
</template>

<script setup>
defineProps({ artist: { type: Object, required: true } })
</script>

<style scoped>
.artist-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: white;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
}

.artist-card:hover {
  border-color: #999;
}

.avatar {
  width: 48px;
  height: 48px;
  background: #f0f0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  flex-shrink: 0;
}

.info h4 {
  font-size: 14px;
  font-weight: 500;
}

.info p {
  font-size: 12px;
  color: #666;
}
</style>
