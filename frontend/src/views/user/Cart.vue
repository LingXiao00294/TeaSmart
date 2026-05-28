<template>
  <div class="cart-page">
    <div class="header-nav">
      <el-button link @click="$router.push('/')"><el-icon><ArrowLeft /></el-icon> 返回首页</el-button>
      <span class="page-title">购物车</span>
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
            <el-button type="danger" link size="small" @click="handleDelete(item.id)">删除</el-button>
          </div>
        </div>
      </div>

      <div class="cart-footer">
        <div class="total">合计：<span class="total-price">¥{{ totalPrice }}</span></div>
        <el-button type="primary" size="large" class="checkout-btn" @click="showCheckout = true">去结算</el-button>
      </div>
    </div>

    <div v-else class="empty">
      <el-empty description="购物车空空如也">
        <el-button type="primary" @click="$router.push('/menu')">去选购</el-button>
      </el-empty>
    </div>

    <el-dialog v-model="showCheckout" title="确认订单" width="90%" :close-on-click-modal="false">
      <div v-for="item in items" :key="item.id" class="checkout-item">
        <span>{{ item.productName }}（{{ item.specInfo }}）</span>
        <span>x{{ item.quantity }} ¥{{ item.subtotal }}</span>
      </div>
      <div class="checkout-total">总计：¥{{ totalPrice }}</div>
      <el-input v-model="remark" placeholder="备注（选填）" style="margin-top:14px" />
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
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const items = ref([])
const showCheckout = ref(false)
const remark = ref('')
const ordering = ref(false)

const totalPrice = computed(() =>
  items.value.reduce((sum, i) => sum + Number(i.subtotal || 0), 0).toFixed(2)
)

onMounted(loadCart)

async function loadCart() { items.value = (await getCart()).data }
async function handleUpdate(id, quantity) { await updateCartItem(id, { quantity }); loadCart() }
async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除？', '提示')
  await deleteCartItem(id); loadCart()
}

async function handleOrder() {
  ordering.value = true
  try {
    const res = await createOrder({ remark: remark.value })
    await payOrder(res.data.id)
    ElMessage.success('下单成功')
    showCheckout.value = false
    router.push(`/orders/${res.data.id}`)
  } catch (e) { ElMessage.error(e.message || '下单失败') }
  finally { ordering.value = false }
}
</script>

<style scoped>
.cart-page { min-height: 100vh; background: var(--main-bg); }
.header-nav { display: flex; align-items: center; gap: 10px; padding: 14px 16px; background: #fff; }
.page-title { font-weight: 600; color: var(--text-primary); }
.cart-list { padding: 12px 16px; padding-bottom: 80px; }
.cart-item {
  display: flex; gap: 14px; background: #fff; border-radius: 12px;
  padding: 14px; margin-bottom: 10px;
}
.item-img {
  width: 64px; height: 64px; border-radius: 12px;
  background: linear-gradient(135deg, #409eff, #764ba2);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 20px; flex-shrink: 0;
}
.item-info { flex: 1; }
.item-name { font-weight: 600; font-size: 15px; color: var(--text-primary); }
.item-spec { font-size: 12px; color: #909399; margin: 4px 0 8px; }
.item-bottom { display: flex; align-items: center; gap: 12px; }
.item-price { color: #f56c6c; font-weight: 700; font-size: 15px; }
.cart-footer {
  position: fixed; bottom: 0; left: 0; right: 0; background: #fff;
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 20px; box-shadow: 0 -2px 12px rgba(0,0,0,0.06);
  border-top: 1px solid var(--border-color);
}
.total-price { color: #f56c6c; font-size: 22px; font-weight: 700; }
.checkout-btn { border-radius: 8px; height: 42px; padding: 0 28px; }
.checkout-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f0f0f0; font-size: 14px; }
.checkout-total { text-align: right; font-size: 18px; font-weight: 700; color: #f56c6c; margin-top: 14px; }
</style>
