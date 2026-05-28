<template>
  <div class="detail-page">
    <div class="header-nav">
      <el-button link @click="$router.push('/orders')"><el-icon><ArrowLeft /></el-icon> 返回订单</el-button>
      <span class="page-title">订单详情</span>
    </div>

    <div v-if="order" class="detail-content">
      <el-card shadow="never" class="info-card">
        <div class="status-row">
          <el-tag :type="statusType(order.status)" size="large" effect="dark">{{ order.statusText }}</el-tag>
          <span class="order-no">{{ order.orderNo }}</span>
        </div>
      </el-card>

      <el-card shadow="never" class="info-card">
        <div class="card-title">商品明细</div>
        <div v-for="item in order.items" :key="item.productId" class="detail-item">
          <div class="di-info">
            <div class="di-name">{{ item.productName }}</div>
            <div class="di-spec">{{ item.specInfo }}</div>
          </div>
          <div class="di-right">
            <span class="di-price">¥{{ item.price }}</span>
            <span class="di-qty">x{{ item.quantity }}</span>
            <span class="di-sub">¥{{ item.subtotal }}</span>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="info-card">
        <div class="total-row">
          <span>总计</span>
          <span class="total-price">¥{{ order.totalAmount }}</span>
        </div>
        <div v-if="order.remark" class="remark">备注：{{ order.remark }}</div>
      </el-card>

      <div v-if="order.status === 0" class="actions">
        <el-button type="danger" size="large" class="cancel-btn" @click="handleCancel">取消订单</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetail, cancelOrder } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const order = ref(null)

onMounted(async () => { order.value = (await getOrderDetail(route.params.id)).data })

function statusType(s) {
  return { 0: 'warning', 1: '', 2: 'info', 3: 'success', 4: 'danger' }[s] || ''
}

async function handleCancel() {
  await ElMessageBox.confirm('确定取消订单？', '提示')
  await cancelOrder(order.value.id)
  ElMessage.success('已取消')
  order.value = (await getOrderDetail(route.params.id)).data
}
</script>

<style scoped>
.detail-page { min-height: 100vh; background: var(--main-bg); }
.header-nav { display: flex; align-items: center; gap: 10px; padding: 14px 16px; background: #fff; }
.page-title { font-weight: 600; color: var(--text-primary); }
.detail-content { padding: 12px 16px; }
.info-card { border-radius: 12px; border: none; margin-bottom: 10px; }
.card-title { font-weight: 600; font-size: 15px; color: var(--text-primary); margin-bottom: 14px; }
.status-row { display: flex; justify-content: space-between; align-items: center; }
.order-no { font-size: 12px; color: #909399; }
.detail-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid #f5f5f5; }
.detail-item:last-child { border-bottom: none; }
.di-name { font-weight: 600; font-size: 14px; color: var(--text-primary); }
.di-spec { font-size: 12px; color: #909399; margin-top: 2px; }
.di-right { display: flex; align-items: center; gap: 8px; }
.di-price { color: #606266; font-size: 13px; }
.di-qty { color: #909399; font-size: 13px; }
.di-sub { font-weight: 600; color: var(--text-primary); }
.total-row { display: flex; justify-content: space-between; font-size: 18px; font-weight: 700; }
.total-price { color: #f56c6c; }
.remark { margin-top: 12px; color: #909399; font-size: 13px; }
.actions { margin-top: 20px; }
.cancel-btn { width: 100%; border-radius: 8px; }
</style>
