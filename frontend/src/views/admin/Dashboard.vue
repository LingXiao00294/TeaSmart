<template>
  <div>
    <div class="page-header">
      <h2>Dashboard</h2>
    </div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon" :style="{ background: card.bg }">
            <el-icon :size="24"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, shallowRef, markRaw } from 'vue'
import { adminGetDashboard } from '@/api'
import { ShoppingCart, Money, User, Goods } from '@element-plus/icons-vue'

const cards = ref([
  { label: '今日订单', value: 0, icon: markRaw(ShoppingCart), bg: 'linear-gradient(135deg, #409eff, #66b1ff)' },
  { label: '今日销售额', value: '¥0', icon: markRaw(Money), bg: 'linear-gradient(135deg, #67c23a, #85ce61)' },
  { label: '总用户数', value: 0, icon: markRaw(User), bg: 'linear-gradient(135deg, #e6a23c, #ebb563)' },
  { label: '总商品数', value: 0, icon: markRaw(Goods), bg: 'linear-gradient(135deg, #f56c6c, #f89898)' },
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
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--text-primary); }
.stat-card { border: none; border-radius: 12px; }
.stat-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; padding: 20px; }
.stat-icon {
  width: 56px; height: 56px; border-radius: 12px; color: #fff;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-value { font-size: 24px; font-weight: 700; color: var(--text-primary); }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
</style>
