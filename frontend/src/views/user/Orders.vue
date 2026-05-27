<template>
  <div class="orders-page">
    <div class="top-bar">
      <el-page-header @back="$router.push('/')" content="我的订单" />
    </div>

    <el-tabs v-model="activeTab" @tab-change="loadOrders" class="tabs">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待支付" name="0" />
      <el-tab-pane label="已支付" name="1" />
      <el-tab-pane label="已完成" name="3" />
      <el-tab-pane label="已取消" name="4" />
    </el-tabs>

    <div class="order-list">
      <div v-for="o in orders" :key="o.id" class="order-card" @click="$router.push(`/orders/${o.id}`)">
        <div class="order-header">
          <span class="order-no">{{ o.orderNo }}</span>
          <el-tag :type="statusType(o.status)" size="small">{{ o.statusText }}</el-tag>
        </div>
        <div v-for="item in o.items" :key="item.productId" class="order-item">
          {{ item.productName }}（{{ item.specInfo }}） x{{ item.quantity }}
          <span class="item-price">¥{{ item.subtotal }}</span>
        </div>
        <div class="order-footer">
          <span class="order-total">合计：¥{{ o.totalAmount }}</span>
        </div>
      </div>
      <div v-if="!orders.length" class="empty">暂无订单</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrders } from '@/api'

const activeTab = ref('')
const orders = ref([])

onMounted(loadOrders)

async function loadOrders() {
  const status = activeTab.value || undefined
  const res = await getOrders(status)
  orders.value = res.data
}

function statusType(s) {
  return { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger' }[s] || ''
}
</script>

<style scoped>
.orders-page { min-height: 100vh; background: #f5f5f5; }
.top-bar { padding: 10px 16px; background: #fff; border-bottom: 1px solid #eee; }
.tabs { padding: 0 16px; background: #fff; }
.order-list { padding: 10px 16px; }
.order-card { background: #fff; border-radius: 8px; padding: 14px; margin-bottom: 10px; cursor: pointer; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.order-no { font-size: 12px; color: #999; }
.order-item { display: flex; justify-content: space-between; font-size: 13px; padding: 4px 0; }
.item-price { color: #333; }
.order-footer { text-align: right; margin-top: 8px; }
.order-total { font-weight: bold; color: #e74c3c; }
.empty { text-align: center; color: #999; padding: 40px; }
</style>
