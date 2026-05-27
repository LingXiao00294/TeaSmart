<template>
  <div class="cart-page">
    <div class="top-bar">
      <el-page-header @back="$router.push('/')" content="购物车" />
    </div>

    <div v-if="items.length" class="cart-list">
      <div v-for="item in items" :key="item.id" class="cart-item">
        <div class="item-img">{{ item.productName?.[0] || '?' }}</div>
        <div class="item-info">
          <div class="item-name">{{ item.productName }}</div>
          <div class="item-spec">{{ item.specInfo }}</div>
          <div class="item-bottom">
            <span class="item-price">¥{{ item.unitPrice }}</span>
            <el-input-number :model-value="item.quantity" :min="1" size="small"
              @change="(val) => handleUpdate(item.id, val)" />
            <el-button type="danger" text size="small" @click="handleDelete(item.id)">删除</el-button>
          </div>
        </div>
      </div>

      <div class="cart-footer">
        <div class="total">合计：<span class="total-price">¥{{ totalPrice }}</span></div>
        <el-button type="primary" size="large" @click="showCheckout = true">去结算</el-button>
      </div>
    </div>

    <div v-else class="empty">
      <p>购物车空空如也</p>
      <el-button type="primary" @click="$router.push('/menu')">去选购</el-button>
    </div>

    <!-- 确认订单弹窗 -->
    <el-dialog v-model="showCheckout" title="确认订单" width="90%">
      <div v-for="item in items" :key="item.id" class="checkout-item">
        <span>{{ item.productName }}（{{ item.specInfo }}）</span>
        <span>x{{ item.quantity }} ¥{{ item.subtotal }}</span>
      </div>
      <div class="checkout-total">总计：¥{{ totalPrice }}</div>
      <el-input v-model="remark" placeholder="备注（选填）" style="margin-top:12px" />
      <template #footer>
        <el-button @click="showCheckout = false">取消</el-button>
        <el-button type="primary" :loading="ordering" @click="handleOrder">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, updateCartItem, deleteCartItem, createOrder, payOrder } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const items = ref([])
const showCheckout = ref(false)
const remark = ref('')
const ordering = ref(false)

const totalPrice = computed(() =>
  items.value.reduce((sum, i) => sum + Number(i.subtotal || 0), 0).toFixed(2)
)

onMounted(loadCart)

async function loadCart() {
  const res = await getCart()
  items.value = res.data
}

async function handleUpdate(id, quantity) {
  await updateCartItem(id, { quantity })
  loadCart()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除？', '提示')
  await deleteCartItem(id)
  loadCart()
}

async function handleOrder() {
  ordering.value = true
  try {
    const res = await createOrder({ remark: remark.value })
    const orderId = res.data.id
    // 模拟支付
    await payOrder(orderId)
    ElMessage.success('下单成功')
    showCheckout.value = false
    router.push(`/orders/${orderId}`)
  } catch (e) {
    ElMessage.error(e.message || '下单失败')
  } finally {
    ordering.value = false
  }
}
</script>

<style scoped>
.cart-page { display: flex; flex-direction: column; min-height: 100vh; }
.top-bar { padding: 10px 16px; background: #fff; border-bottom: 1px solid #eee; }
.cart-list { flex: 1; padding: 10px 16px; padding-bottom: 70px; }
.cart-item { display: flex; gap: 12px; background: #fff; border-radius: 8px; padding: 12px; margin-bottom: 10px; }
.item-img {
  width: 60px; height: 60px; border-radius: 8px; background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0;
}
.item-info { flex: 1; }
.item-name { font-weight: bold; }
.item-spec { font-size: 12px; color: #999; margin: 4px 0; }
.item-bottom { display: flex; align-items: center; gap: 10px; }
.item-price { color: #e74c3c; font-weight: bold; }
.cart-footer {
  position: fixed; bottom: 0; left: 0; right: 0; background: #fff;
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 16px; box-shadow: 0 -2px 8px rgba(0,0,0,.06);
}
.total-price { color: #e74c3c; font-size: 20px; font-weight: bold; }
.empty { text-align: center; padding: 60px; color: #999; }
.checkout-item { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid #f0f0f0; }
.checkout-total { text-align: right; font-size: 18px; font-weight: bold; color: #e74c3c; margin-top: 12px; }
</style>
