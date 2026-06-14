<template>
  <header class="app-header">
    <div class="app-header__left">
      <button v-if="showBack" class="app-header__back" @click="goBack" aria-label="返回">
        <el-icon :size="20"><ArrowLeftBold /></el-icon>
      </button>
      <h1 v-if="title" class="app-header__title">{{ title }}</h1>
      <slot name="title" />
    </div>
    <div class="app-header__right">
      <slot name="right" />
    </div>
  </header>
</template>

<script setup>
import { ArrowLeftBold } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  title: { type: String, default: '' },
  showBack: { type: Boolean, default: true },
  backTo: { type: [String, Object], default: null },
})

const router = useRouter()

function goBack() {
  if (props.backTo) {
    router.push(props.backTo)
  } else if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(251, 247, 238, 0.92);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--tea-line);
}
.app-header__left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.app-header__back {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--tea-ink);
  cursor: pointer;
  border-radius: 50%;
  transition: background 0.2s;
}
.app-header__back:hover {
  background: var(--tea-paper);
}
.app-header__title {
  font-family: var(--font-heading);
  font-size: 18px;
  font-weight: 600;
  color: var(--tea-ink-text);
  letter-spacing: 2px;
}
.app-header__right {
  display: flex;
  align-items: center;
  gap: 14px;
}
</style>
