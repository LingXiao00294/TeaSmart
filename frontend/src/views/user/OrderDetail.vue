<template>
  <div class="detail-page">
    <div class="top-bar">
      <el-page-header @back="$router.push('/orders')" content="订单详情" />
    </div>

    <div v-if="order" class="detail-content">
      <el-card>
        <div class="status-row">
          <el-tag :type="statusType(order.status)" size="large">{{ order.statusText }}</el-tag>
          <span class="order-no">{{ order.orderNo }}</span>
        </div>

        <el-divider />

        <div v-for="item in order.items" :key="item.productId" class="detail-item">
          <div class="di-name">{{ item.productName }}</div>
          <div class="di-spec">{{ item.specInfo }}</div>
          <div class="di-row">
            <span>¥{{ item.price }} x{{ item.quantity }}</span>
            <span class="di-sub">¥{{ item.subtotal }}</span>
          </div>
        </div>

        <el-divider />

        <div class="total-row">
          <span>总计</span>
          <span class="total-price">¥{{ order.totalAmount }}</span>
        </div>

        <div v-if="order.remark" class="remark">备注：{{ order.remark }}</div>

        <div v-if="order.status === 0" class="actions">
          <el-button type="danger" @click="handleCancel">取消订单</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, cancelOrder } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const order = ref(null)

onMounted(async () => {
  const res = await getOrderDetail(route.params.id)
  order.value = res.data
})

function statusType(s) {
  return { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger' }[s] || ''
}

async function handleCancel() {
  await ElMessageBox.confirm('确定取消订单？', '提示')
  await cancelOrder(order.value.id)
  ElMessage.success('已取消')
  const res = await getOrderDetail(route.params.id)
  order.value = res.data
}
</script>

<style scoped>
.detail-page { min-height: 100vh; background: #f5f5f5; }
.top-bar { padding: 10px 16px; background: #fff; border-bottom: 1px solid #eee; }
.detail-content { padding: 16px; }
.status-row { display: flex; justify-content: space-between; align-items: center; }
.order-no { font-size: 12px; color: #999; }
.detail-item { padding: 8px 0; }
.di-name { font-weight: bold; }
.di-spec { font-size: 12px; color: #999; }
.di-row { display: flex; justify-content: space-between; margin-top: 4px; }
.di-sub { font-weight: bold; }
.total-row { display: flex; justify-content: space-between; font-size: 18px; font-weight: bold; }
.total-price { color: #e74c3c; }
.remark { margin-top: 12px; color: #666; font-size: 13px; }
.actions { margin-top: 20px; text-align: center; }
</style>
