<template>
  <div class="orders-page">
    <div class="header-nav">
      <el-button link @click="$router.push('/')"><el-icon><ArrowLeft /></el-icon> 返回首页</el-button>
      <span class="page-title">我的订单</span>
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
          <el-tag :type="statusType(o.status)" size="small" effect="plain">{{ o.statusText }}</el-tag>
        </div>
        <div v-for="item in o.items" :key="item.productId" class="order-item">
          <span>{{ item.productName }}（{{ item.specInfo }}） x{{ item.quantity }}</span>
          <span class="item-price">¥{{ item.subtotal }}</span>
        </div>
        <div class="order-footer">
          <span class="order-total">合计：¥{{ o.totalAmount }}</span>
        </div>
      </div>
      <div v-if="!orders.length">
        <el-empty description="暂无订单" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrders } from '@/api'
import { ArrowLeft } from '@element-plus/icons-vue'

const activeTab = ref('')
const orders = ref([])

onMounted(loadOrders)

async function loadOrders() {
  const status = activeTab.value || undefined
  orders.value = (await getOrders(status)).data
}

function statusType(s) {
  return { 0: 'warning', 1: '', 2: 'info', 3: 'success', 4: 'danger' }[s] || ''
}
</script>

<style scoped>
.orders-page { min-height: 100vh; background: var(--main-bg); }
.header-nav { display: flex; align-items: center; gap: 10px; padding: 14px 16px; background: #fff; }
.page-title { font-weight: 600; color: var(--text-primary); }
.tabs { padding: 0 16px; background: #fff; }
.order-list { padding: 12px 16px; }
.order-card {
  background: #fff; border-radius: 12px; padding: 16px;
  margin-bottom: 10px; cursor: pointer; transition: all 0.2s;
}
.order-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.order-no { font-size: 12px; color: #909399; }
.order-item { display: flex; justify-content: space-between; font-size: 13px; padding: 4px 0; color: #606266; }
.item-price { color: var(--text-primary); font-weight: 500; }
.order-footer { text-align: right; margin-top: 10px; padding-top: 10px; border-top: 1px solid #f0f0f0; }
.order-total { font-weight: 700; color: #f56c6c; font-size: 15px; }
</style>
