<template>
  <router-view />
  <AiChat v-if="userStore.token" />
</template>

<script setup>
import AiChat from '@/components/AiChat.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>
