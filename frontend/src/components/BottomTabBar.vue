<template>
  <nav v-if="visible" class="tabbar">
    <div class="gold-line tabbar__line" />
    <router-link
      v-for="t in tabs"
      :key="t.path"
      :to="t.path"
      class="tab"
      :class="{ 'tab--active': isActive(t) }"
    >
      <span class="tab__icon">
        <el-badge v-if="t.badge && count > 0" :value="count" :max="99" class="tab__badge">
          <el-icon :size="22"><component :is="t.icon" /></el-icon>
        </el-badge>
        <el-icon v-else :size="22"><component :is="t.icon" /></el-icon>
      </span>
      <span class="tab__label">{{ t.label }}</span>
      <span v-if="isActive(t)" class="tab__dot" />
    </router-link>
  </nav>
</template>

<script setup>
import { computed, watch, markRaw } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, Coffee, ShoppingCart, Tickets, User } from '@element-plus/icons-vue'
import { useCartCount } from '@/composables/useCartCount'

const route = useRoute()
const { count, refresh } = useCartCount()

const tabs = [
  { path: '/', label: '首页', icon: markRaw(HomeFilled) },
  { path: '/menu', label: '茶单', icon: markRaw(Coffee) },
  { path: '/cart', label: '购物车', icon: markRaw(ShoppingCart), badge: true },
  { path: '/orders', label: '订单', icon: markRaw(Tickets) },
  { path: '/profile', label: '我的', icon: markRaw(User) },
]

// 仅在 5 个主 tab 页显示；订单详情等子页隐藏
const visible = computed(() => tabs.some((t) => route.path === t.path))

function isActive(t) {
  if (t.path === '/') return route.path === '/'
  return route.path.startsWith(t.path)
}

// 仅在导航栏可见（即处于 tab 页）时刷新购物车数；加购等变更由各页直接调用 refresh 实时同步
watch(() => route.path, () => { if (visible.value) refresh() }, { immediate: true })
</script>

<style scoped>
.tabbar {
  flex-shrink: 0;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  background: var(--tea-paper-2);
  padding: 6px 0 calc(6px + env(safe-area-inset-bottom));
  position: relative;
  z-index: 20;
}
.tabbar__line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
}
.tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 4px 0;
  color: var(--tea-text-3);
  transition: color 0.2s;
  position: relative;
}
.tab--active {
  color: var(--tea-vermilion);
}
.tab__label {
  font-size: 11px;
  font-family: var(--font-heading);
  letter-spacing: 1px;
}
.tab__dot {
  position: absolute;
  bottom: 1px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--tea-vermilion);
}
.tab__badge :deep(.el-badge__content) {
  background: var(--tea-vermilion);
  border: none;
  font-family: var(--font-body);
}
</style>
