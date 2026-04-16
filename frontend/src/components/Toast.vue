<template>
  <Teleport to="body">
    <div class="toast-container">
      <div v-for="t in toasts" :key="t.id" :class="['toast', `toast-${t.type}`]">
        {{ t.message }}
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { inject } from 'vue'

const toasts = inject('toasts')
</script>

<style>
.toast-container {
  position: fixed;
  bottom: 16px;
  right: 16px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.toast {
  padding: 12px 16px;
  background: #333;
  color: white;
  border-radius: 4px;
  font-size: 14px;
}

.toast-success { background: #16a34a; }
.toast-error { background: #dc2626; }
</style>
