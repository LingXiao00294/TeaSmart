<template>
  <AppShell>
  <div class="cart">
    <AppHeader title="茶 · 盏" back-to="/" />

    <div v-if="items.length" class="cart__list">
      <div
        v-for="(item, i) in items"
        :key="item.id"
        class="cart-item rise"
        :style="{ animationDelay: `${i * 0.04}s` }"
      >
        <div class="cart-item__disc">{{ teaGlyph(item.productName) }}</div>
        <div class="cart-item__info">
          <div class="cart-item__name font-heading">{{ item.productName }}</div>
          <div class="cart-item__spec">{{ item.specInfo }}</div>
          <div class="cart-item__bottom">
            <span class="price cart-item__price"><small>¥</small>{{ item.unitPrice }}</span>
            <div class="cart-item__ops">
              <el-input-number
                :model-value="item.quantity"
                :min="1"
                size="small"
                @change="(val) => handleUpdate(item.id, val)"
              />
              <el-button type="danger" link size="small" @click="handleDelete(item.id)">移除</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="cart__empty">
      <div class="cart__empty-seal seal seal--lg">空</div>
      <p class="cart__empty-txt font-heading">茶盏尚空</p>
      <el-button type="primary" class="cart__empty-btn" @click="$router.push('/menu')">去 · 点茶</el-button>
    </div>

    <el-dialog
      v-model="showCheckout"
      title="确认茶单"
      :close-on-click-modal="false"
      class="tea-dialog"
      modal-class="tea-overlay"
    >
      <template #header>
        <div class="checkout-head">
          <span class="seal seal--sm">单</span>
          <span class="checkout-head__title font-heading">确认茶单</span>
        </div>
      </template>
      <div v-for="item in items" :key="item.id" class="checkout-row">
        <span class="checkout-row__name font-heading">{{ item.productName }}<small>（{{ item.specInfo }}）</small></span>
        <span class="checkout-row__amt">×{{ item.quantity }} ¥{{ item.subtotal }}</span>
      </div>
      <hr class="gold-line checkout-divider" />
      <div class="checkout-total">
        <span>总计</span>
        <span class="price"><small>¥</small>{{ totalPrice }}</span>
      </div>
      <el-input v-model="remark" placeholder="备注（如：少冰、半糖）" class="checkout-remark" />
      <template #footer>
        <el-button class="checkout-cancel" @click="showCheckout = false">再想想</el-button>
        <el-button type="primary" :loading="ordering" class="checkout-submit" @click="handleOrder">下 · 单</el-button>
      </template>
    </el-dialog>
  </div>

  <template #footer>
    <transition name="bar-slide">
      <div v-if="items.length" class="cart__bar">
        <div class="cart__bar-total">
          <span class="cart__bar-label">合计</span>
          <span class="price cart__bar-amount"><small>¥</small>{{ totalPrice }}</span>
        </div>
        <el-button type="primary" size="large" class="cart__checkout" @click="showCheckout = true">去 · 结账</el-button>
      </div>
    </transition>
  </template>
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, updateCartItem, deleteCartItem, createOrder } from '@/api'
import { refresh as refreshCartCount } from '@/composables/useCartCount'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppShell from '@/components/AppShell.vue'
import AppHeader from '@/components/AppHeader.vue'

const router = useRouter()
const items = ref([])
const showCheckout = ref(false)
const remark = ref('')
const ordering = ref(false)

function teaGlyph(name) {
  const map = { 绿: '🍵', 红: '🫖', 奶: '🧋', 果: '🍹', 茶: '🍃', 花: '🌸' }
  for (const k of Object.keys(map)) if (name && name.includes(k)) return map[k]
  return '🍵'
}

const totalPrice = computed(() =>
  items.value.reduce((sum, i) => sum + Number(i.subtotal || 0), 0).toFixed(2)
)

onMounted(loadCart)

async function loadCart() { items.value = (await getCart()).data; refreshCartCount() }
async function handleUpdate(id, quantity) { await updateCartItem(id, { quantity }); loadCart() }
async function handleDelete(id) {
  await ElMessageBox.confirm('确定移除此茶品？', '提示')
  await deleteCartItem(id); loadCart()
}

async function handleOrder() {
  ordering.value = true
  try {
    const res = await createOrder({ remark: remark.value })
    refreshCartCount()
    ElMessage.success('订单已创建，请完成支付')
    showCheckout.value = false
    router.push(`/pay/${res.data.id}`)
  } catch (e) { ElMessage.error(e.message || '下单失败') }
  finally { ordering.value = false }
}
</script>

<style scoped>
.cart {
  min-height: 100%;
  padding-bottom: 16px;
}
.cart__list {
  padding: 12px 16px;
}
.cart-item {
  display: flex;
  gap: 12px;
  background: var(--tea-paper-2);
  border: 1px solid var(--tea-line);
  border-radius: var(--radius);
  padding: 14px;
  margin-bottom: 10px;
}
.cart-item__disc {
  width: 58px;
  height: 58px;
  flex-shrink: 0;
  border-radius: 50%;
  background: var(--tea-paper);
  border: 1px solid var(--tea-line);
  box-shadow: inset 0 0 0 4px var(--tea-paper-2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}
.cart-item__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.cart-item__name {
  font-size: 15px;
  font-weight: 600;
  color: var(--tea-ink-text);
  letter-spacing: 1px;
}
.cart-item__spec {
  margin: 4px 0 8px;
  font-size: 12px;
  color: var(--tea-text-3);
}
.cart-item__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}
.cart-item__price {
  font-size: 17px;
}
.cart-item__price small {
  font-size: 11px;
}
.cart-item__ops {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 空状态 */
.cart__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 20px;
}
.cart__empty-seal {
  transform: rotate(-6deg);
  margin-bottom: 18px;
  opacity: 0.85;
}
.cart__empty-txt {
  font-size: 15px;
  color: var(--tea-text-3);
  letter-spacing: 4px;
  margin-bottom: 24px;
}
.cart__empty-btn {
  border-radius: var(--radius);
  height: 42px;
  padding: 0 28px;
  font-family: var(--font-heading);
  letter-spacing: 3px;
}

/* 结算栏（AppShell footer 插槽，常驻于滚动区与导航栏之间） */
.cart__bar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  background: var(--tea-ink);
  color: var(--tea-paper);
  box-shadow: 0 -6px 20px rgba(24, 40, 25, 0.2); /* 降级：不支持 color-mix 的浏览器 */
  box-shadow: 0 -6px 20px color-mix(in srgb, var(--tea-ink-900) 20%, transparent);
}
.cart__bar-total {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.cart__bar-label {
  font-size: 13px;
  opacity: 0.8;
  letter-spacing: 2px;
}
.cart__bar-amount {
  color: #f7e9c6;
  font-size: 24px;
}
.cart__bar-amount small {
  font-size: 14px;
}
.cart__checkout {
  border-radius: var(--radius);
  height: 44px;
  padding: 0 26px;
  font-family: var(--font-heading);
  letter-spacing: 3px;
}
.bar-slide-enter-active,
.bar-slide-leave-active {
  transition: transform 0.3s;
}
.bar-slide-enter-from,
.bar-slide-leave-to {
  transform: translateY(100%);
}

/* 结算弹窗 */
.checkout-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: 8px 0;
  font-size: 14px;
  color: var(--tea-text-2);
}
.checkout-row__name small {
  color: var(--tea-text-3);
  font-size: 12px;
}
.checkout-row__amt {
  color: var(--tea-ink-text);
  font-weight: 500;
}
.checkout-divider {
  margin: 10px 0;
}
.checkout-total {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 17px;
  font-weight: 600;
}
.checkout-total .price {
  font-size: 22px;
}
.checkout-remark {
  margin-top: 16px;
}

/* 茶韵弹窗：标题区（墨绿头内的印章 + 衬线标题）与按钮 */
.checkout-head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.checkout-head__title {
  font-size: 17px;
  letter-spacing: 3px;
  color: var(--tea-paper);
}
.checkout-cancel {
  font-family: var(--font-heading);
  letter-spacing: 2px;
  border-radius: var(--radius);
}
.checkout-submit {
  font-family: var(--font-heading);
  letter-spacing: 3px;
  border-radius: var(--radius);
}
</style>
