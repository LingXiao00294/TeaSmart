<template>
  <AppShell>
  <div class="pay">
    <AppHeader title="茶 · 付" back-to="/orders" />

    <div v-if="order" class="pay__content">
      <section class="card rise">
        <div class="pay__order-info">
          <span class="pay__label font-heading">订单号</span>
          <span class="pay__order-no">{{ order.orderNo }}</span>
        </div>
      </section>

      <section class="card rise" style="animation-delay: 0.05s">
        <div class="card__title font-heading">茶 · 明细</div>
        <div v-for="item in order.items" :key="item.productId" class="pay-item">
          <div class="pay-item__main">
            <div class="pay-item__name font-heading">{{ item.productName }}</div>
            <div class="pay-item__spec">{{ item.specInfo }}</div>
          </div>
          <div class="pay-item__right">
            <span class="pay-item__unit">¥{{ item.price }} × {{ item.quantity }}</span>
            <span class="price pay-item__sub">¥{{ item.subtotal }}</span>
          </div>
        </div>
      </section>

      <section class="card pay__total rise" style="animation-delay: 0.1s">
        <span class="font-heading pay__total-label">待付总计</span>
        <span class="price pay__total-amount"><small>¥</small>{{ order.totalAmount }}</span>
      </section>

      <p v-if="order.remark" class="pay__remark">备注：{{ order.remark }}</p>

      <section class="card pay__actions rise" style="animation-delay: 0.15s">
        <el-button
          type="primary"
          size="large"
          class="pay__confirm"
          :loading="paying"
          :disabled="!canOperate"
          @click="handlePay"
        >{{ paying ? '处理中…' : '确认支付' }}</el-button>
        <el-button
          type="danger"
          plain
          size="large"
          class="pay__cancel"
          :loading="cancelling"
          :disabled="!canOperate"
          @click="handleCancel"
        >取消订单</el-button>
      </section>
    </div>
  </div>
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, payOrder, cancelOrder } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppShell from '@/components/AppShell.vue'
import AppHeader from '@/components/AppHeader.vue'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const paying = ref(false)
const cancelling = ref(false)
let payTimer = null

const canOperate = computed(() => order.value && order.value.status === 0)

onMounted(async () => {
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res.data
    if (order.value.status !== 0) {
      router.replace(`/orders/${order.value.id}`)
    }
  } catch {
    ElMessage.error('订单加载失败')
    router.replace('/orders')
  }
})

onUnmounted(() => {
  if (payTimer) clearTimeout(payTimer)
})

async function handlePay() {
  paying.value = true
  try {
    // 模拟支付处理延迟
    await new Promise(resolve => { payTimer = setTimeout(resolve, 1500) })
    await payOrder(order.value.id)
    ElMessage.success('支付成功')
    router.push(`/orders/${order.value.id}`)
  } catch (e) {
    ElMessage.error(e.message || '支付失败')
  } finally {
    paying.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定取消此单？茶品将不再保留。', '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
    })
  } catch {
    return // 用户点了「再想想」，静默返回
  }
  cancelling.value = true
  try {
    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    router.push('/orders')
  } catch (e) {
    ElMessage.error(e.message || '取消失败')
  } finally {
    cancelling.value = false
  }
}
</script>

<style scoped>
.pay {
  min-height: 100%;
}
.pay__content {
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

/* 订单号 */
.pay__order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pay__label {
  font-size: 13px;
  color: var(--tea-text-2);
  letter-spacing: 2px;
}
.pay__order-no {
  font-size: 13px;
  color: var(--tea-text-3);
  letter-spacing: 1px;
  font-family: var(--font-display);
}

/* 商品明细 */
.pay-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10px 0;
  border-bottom: 1px dashed var(--tea-line);
}
.pay-item:last-of-type {
  border-bottom: none;
}
.pay-item__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--tea-ink-text);
}
.pay-item__spec {
  font-size: 12px;
  color: var(--tea-text-3);
  margin-top: 3px;
}
.pay-item__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}
.pay-item__unit {
  font-size: 12px;
  color: var(--tea-text-3);
}
.pay-item__sub {
  font-size: 15px;
}

/* 合计 */
.pay__total {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.pay__total-label {
  font-size: 14px;
  color: var(--tea-text-2);
  letter-spacing: 2px;
}
.pay__total-amount {
  font-size: 28px;
}
.pay__total-amount small {
  font-size: 15px;
}

/* 备注 */
.pay__remark {
  margin: 4px 4px 0;
  font-size: 13px;
  color: var(--tea-text-3);
}

/* 操作按钮 */
.pay__actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.pay__confirm,
.pay__cancel {
  width: 100%;
  border-radius: var(--radius);
  height: 46px;
  font-family: var(--font-heading);
  letter-spacing: 3px;
  font-size: 15px;
}
</style>
