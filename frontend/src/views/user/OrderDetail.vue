<template>
  <AppShell>
  <div class="detail">
    <AppHeader title="茶 · 详" back-to="/orders" />

    <div v-if="order" class="detail__content">
      <section class="card rise">
        <div class="detail__status">
          <el-tag :type="statusType(order.status)" size="large" effect="dark" round>{{ order.statusText }}</el-tag>
          <span class="detail__no">{{ order.orderNo }}</span>
        </div>
      </section>

      <section class="card rise" style="animation-delay: 0.05s">
        <div class="card__title font-heading">茶 · 明细</div>
        <div v-for="item in order.items" :key="item.productId" class="detail-item">
          <div class="detail-item__main">
            <div class="detail-item__name font-heading">{{ item.productName }}</div>
            <div class="detail-item__spec">{{ item.specInfo }}</div>
          </div>
          <div class="detail-item__right">
            <span class="detail-item__unit">¥{{ item.price }} × {{ item.quantity }}</span>
            <span class="price detail-item__sub">¥{{ item.subtotal }}</span>
          </div>
        </div>
      </section>

      <section class="card detail__total rise" style="animation-delay: 0.1s">
        <span class="font-heading detail__total-label">总计</span>
        <span class="price detail__total-amount"><small>¥</small>{{ order.totalAmount }}</span>
      </section>

      <p v-if="order.remark" class="detail__remark">备注：{{ order.remark }}</p>

      <div v-if="order.status === 0" class="detail__actions">
        <el-button type="primary" size="large" class="detail__pay" @click="$router.push(`/pay/${order.id}`)">去 · 支付</el-button>
        <el-button type="danger" plain size="large" class="detail__cancel" :loading="cancelling" @click="handleCancel">取消订单</el-button>
      </div>
    </div>
  </div>
  </AppShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetail, cancelOrder } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppShell from '@/components/AppShell.vue'
import AppHeader from '@/components/AppHeader.vue'

const route = useRoute()
const order = ref(null)
const cancelling = ref(false)

onMounted(async () => { order.value = (await getOrderDetail(route.params.id)).data })

function statusType(s) {
  return { 0: 'warning', 1: '', 2: 'info', 3: 'success', 4: 'danger' }[s] || ''
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定取消此单？', '提示')
  } catch {
    return
  }
  cancelling.value = true
  try {
    await cancelOrder(order.value.id)
    ElMessage.success('已取消')
    order.value = (await getOrderDetail(route.params.id)).data
  } catch (e) {
    ElMessage.error(e.message || '取消失败')
  } finally {
    cancelling.value = false
  }
}
</script>

<style scoped>
.detail {
  min-height: 100%;
}
.detail__content {
  padding: 12px 16px 24px;
}
.card {
  background: var(--tea-paper-2);
  border: 1px solid var(--tea-line);
  border-radius: var(--radius);
  padding: 16px;
  margin-bottom: 12px;
}
.card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--tea-ink-text);
  letter-spacing: 2px;
  margin-bottom: 14px;
  position: relative;
  padding-left: 10px;
}
.card__title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 14px;
  background: var(--tea-vermilion);
  border-radius: 2px;
}
.detail__status {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.detail__no {
  font-size: 12px;
  color: var(--tea-text-3);
  letter-spacing: 1px;
}
.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10px 0;
  border-bottom: 1px dashed var(--tea-line);
}
.detail-item:last-of-type {
  border-bottom: none;
}
.detail-item__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--tea-ink-text);
}
.detail-item__spec {
  font-size: 12px;
  color: var(--tea-text-3);
  margin-top: 3px;
}
.detail-item__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}
.detail-item__unit {
  font-size: 12px;
  color: var(--tea-text-3);
}
.detail-item__sub {
  font-size: 15px;
}
.detail__total {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.detail__total-label {
  font-size: 14px;
  color: var(--tea-text-2);
  letter-spacing: 2px;
}
.detail__total-amount {
  font-size: 26px;
}
.detail__total-amount small {
  font-size: 15px;
}
.detail__remark {
  margin: 4px 4px 0;
  font-size: 13px;
  color: var(--tea-text-3);
}
.detail__actions {
  margin-top: 22px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.detail__pay,
.detail__cancel {
  width: 100%;
  border-radius: var(--radius);
  height: 46px;
  font-family: var(--font-heading);
  letter-spacing: 3px;
}
</style>
