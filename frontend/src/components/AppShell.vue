<template>
  <div class="app-shell ink-texture">
    <div class="app-shell__stage paper-texture">
      <main class="app-shell__main">
        <slot />
      </main>
      <slot name="footer" />
      <BottomTabBar />
    </div>
  </div>
</template>

<script setup>
import BottomTabBar from './BottomTabBar.vue'
</script>

<style scoped>
.app-shell {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
}
.app-shell__stage {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.app-shell__main {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
}

/* 桌面端：收束为居中 app 列，两侧露出墨绿氛围底 */
@media (min-width: 720px) {
  .app-shell__stage {
    width: 600px;
    box-shadow: 0 0 80px rgba(0, 0, 0, 0.35);
  }
}
</style>
