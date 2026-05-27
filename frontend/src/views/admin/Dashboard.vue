<template>
  <div>
    <h2>Dashboard</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminGetDashboard } from '@/api'

const cards = ref([
  { label: '今日订单', value: 0 },
  { label: '今日销售额', value: '¥0' },
  { label: '总用户数', value: 0 },
  { label: '总商品数', value: 0 },
])

onMounted(async () => {
  try {
    const res = await adminGetDashboard()
    const d = res.data
    cards.value = [
      { label: '今日订单', value: d.todayOrderCount || 0 },
      { label: '今日销售额', value: `¥${d.todaySalesAmount || 0}` },
      { label: '总用户数', value: d.totalUserCount || 0 },
      { label: '总商品数', value: d.totalProductCount || 0 },
    ]
  } catch {}
})
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: bold; color: #409eff; }
.stat-label { color: #999; margin-top: 6px; }
</style>
