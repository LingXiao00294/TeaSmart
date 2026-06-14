<template>
  <AppShell>
  <div class="orders">
    <AppHeader title="茶 · 记" back-to="/" />

    <div class="orders__tabs">
      <el-tabs v-model="activeTab" @tab-change="loadOrders" class="tea-tabs">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="待支付" name="0" />
        <el-tab-pane label="已支付" name="1" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已取消" name="4" />
      </el-tabs>
    </div>

    <div v-if="orders.length" class="orders__list">
      <article
        v-for="(o, i) in orders"
        :key="o.id"
        class="order rise"
        :style="{ animationDelay: `${i * 0.05}s` }"
        @click="$router.push(`/orders/${o.id}`)"
      >
        <div class="order__head">
          <span class="order__no">{{ o.orderNo }}</span>
          <el-tag :type="statusType(o.status)" size="small" effect="plain" round>{{ o.statusText }}</el-tag>
        </div>
        <div v-for="item in o.items" :key="item.productId" class="order__item">
          <span class="order__item-name font-heading">{{ item.productName }}<small>（{{ item.specInfo }}）</small></span>
          <span class="order__item-amt">×{{ item.quantity }} <span class="price">{{ item.subtotal }}</span></span>
        </div>
        <hr class="gold-line-flat order__sep" />
        <div class="order__foot">
          <span class="order__foot-label">合计</span>
          <span class="price order__total"><small>¥</small>{{ o.totalAmount }}</span>
        </div>
      </article>
    </div>

    <div v-else class="orders__empty">
      <div class="seal seal--lg orders__empty-seal">记</div>
      <p class="orders__empty-txt font-heading">尚无茶记</p>
    </div>
  </div>
  </AppShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrders } from '@/api'
import AppShell from '@/components/AppShell.vue'
import AppHeader from '@/components/AppHeader.vue'

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
.orders {
  min-height: 100%;
}
.orders__tabs {
  padding: 0 8px;
  background: var(--tea-paper-2);
  border-bottom: 1px solid var(--tea-line);
}
.orders__list {
  padding: 12px 16px;
}
.order {
  background: var(--tea-paper-2);
  border: 1px solid var(--tea-line);
  border-radius: var(--radius);
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.order:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-card);
}
.order__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order__no {
  font-size: 12px;
  color: var(--tea-text-3);
  letter-spacing: 1px;
}
.order__item {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 13px;
  padding: 5px 0;
  color: var(--tea-text-2);
}
.order__item-name small {
  color: var(--tea-text-3);
  font-size: 11px;
}
.order__item-amt .price {
  color: var(--tea-ink-text);
  font-size: 13px;
}
.order__sep {
  margin: 10px 0;
}
.order__foot {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 6px;
}
.order__foot-label {
  font-size: 12px;
  color: var(--tea-text-3);
}
.order__total {
  font-size: 17px;
}
.order__total small {
  font-size: 12px;
}

.orders__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 90px 20px;
}
.orders__empty-seal {
  transform: rotate(-6deg);
  opacity: 0.8;
  margin-bottom: 18px;
}
.orders__empty-txt {
  font-size: 14px;
  color: var(--tea-text-3);
  letter-spacing: 4px;
}
</style>
