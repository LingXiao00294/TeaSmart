<template>
  <div>
    <div class="page-header">
      <h2 class="page-header__title font-heading">营业 · 概览</h2>
      <p class="page-header__sub">今日茶席，一览无余</p>
    </div>

    <el-row :gutter="16">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in cards" :key="card.label">
        <div class="stat-card rise">
          <div class="stat-card__icon" :style="{ '--c': card.color, '--bg': card.bg }">
            <el-icon :size="24"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value font-display">{{ card.value }}</div>
            <div class="stat-card__label font-heading">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, markRaw } from 'vue'
import { adminGetDashboard } from '@/api'
import { ShoppingCart, Money, User, Goods } from '@element-plus/icons-vue'

const cards = ref([
  { label: '今日接单', value: 0, icon: markRaw(ShoppingCart), color: '#2f5233', bg: '#e8efe6' },
  { label: '今日营收', value: '¥0', icon: markRaw(Money), color: '#8b5a2b', bg: '#efe4d5' },
  { label: '客户总量', value: 0, icon: markRaw(User), color: '#c0392b', bg: '#f6e3e0' },
  { label: '在售茶品', value: 0, icon: markRaw(Goods), color: '#6e8b5a', bg: '#e9eede' },
])

onMounted(async () => {
  try {
    const d = (await adminGetDashboard()).data
    cards.value[0].value = d.todayOrderCount || 0
    cards.value[1].value = `¥${d.todaySalesAmount || 0}`
    cards.value[2].value = d.totalUserCount || 0
    cards.value[3].value = d.totalProductCount || 0
  } catch {}
})
</script>

<style scoped>
.page-header {
  margin-bottom: 22px;
}
.page-header__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--tea-ink-text);
  letter-spacing: 3px;
}
.page-header__sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--tea-text-3);
  letter-spacing: 2px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: var(--tea-paper-2);
  border: 1px solid var(--tea-line);
  border-radius: var(--radius);
  padding: 20px;
  margin-bottom: 16px;
}
.stat-card__icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--c, var(--tea-ink));
  background: var(--bg, #e8efe6);
}
.stat-card__value {
  font-size: 26px;
  font-weight: 700;
  color: var(--tea-ink-text);
  line-height: 1.1;
}
.stat-card__label {
  font-size: 13px;
  color: var(--tea-text-3);
  letter-spacing: 2px;
  margin-top: 4px;
}
</style>
