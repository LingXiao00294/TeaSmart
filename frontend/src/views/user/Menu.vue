<template>
  <div class="menu-page">
    <div class="top-bar">
      <div class="header-nav">
        <el-button link @click="$router.push('/')"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="page-title">菜单</span>
      </div>
      <router-link to="/cart">
        <el-badge :value="cartCount" :hidden="cartCount===0"><el-icon size="20"><ShoppingCart /></el-icon></el-badge>
      </router-link>
    </div>

    <div class="menu-body">
      <div class="sidebar">
        <div v-for="cat in categories" :key="cat.id"
             :class="['cat-item', { active: activeCat === cat.id }]"
             @click="selectCategory(cat.id)">
          {{ cat.name }}
        </div>
      </div>

      <div class="product-list">
        <div v-for="p in products" :key="p.id" class="product-card" @click="openSpec(p)">
          <div class="prod-img">{{ p.name[0] }}</div>
          <div class="prod-info">
            <div class="prod-name">{{ p.name }}</div>
            <div class="prod-desc">{{ p.description }}</div>
            <div class="prod-price">¥{{ p.price }}</div>
          </div>
        </div>
        <div v-if="!products.length" class="empty">
          <el-empty description="暂无商品" />
        </div>
      </div>
    </div>

    <el-drawer v-model="drawerVisible" :title="selectedProduct?.name" direction="btt" size="60%">
      <div v-if="selectedProduct" class="spec-content">
        <div class="spec-price">¥{{ totalPrice }}</div>
        <div v-for="(specs, type) in selectedProduct.specs" :key="type" class="spec-group">
          <div class="spec-label">{{ specLabels[type] || type }}</div>
          <el-radio-group v-model="selectedSpecs[type]">
            <el-radio-button v-for="s in specs" :key="s.id" :value="s.id">
              {{ s.specName }}{{ s.priceDiff > 0 ? ` +¥${s.priceDiff}` : '' }}
            </el-radio-button>
          </el-radio-group>
        </div>
        <div class="quantity-row">
          <span>数量</span>
          <el-input-number v-model="quantity" :min="1" :max="99" />
        </div>
        <el-button type="primary" size="large" class="add-btn" @click="handleAddToCart">
          加入购物车
        </el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories, getProducts, getProductDetail, addToCart, getCart } from '@/api'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ShoppingCart } from '@element-plus/icons-vue'

const route = useRoute()
const categories = ref([])
const products = ref([])
const activeCat = ref(null)
const cartCount = ref(0)
const drawerVisible = ref(false)
const selectedProduct = ref(null)
const selectedSpecs = ref({})
const quantity = ref(1)
const specLabels = { cup_size: '杯型', sweetness: '糖度', ice: '冰度' }

const totalPrice = computed(() => {
  if (!selectedProduct.value) return 0
  let price = Number(selectedProduct.value.price)
  for (const [type, specId] of Object.entries(selectedSpecs.value)) {
    const specs = selectedProduct.value.specs[type] || []
    const spec = specs.find(s => s.id === specId)
    if (spec) price += Number(spec.priceDiff)
  }
  return price
})

onMounted(async () => {
  const [c, cart] = await Promise.allSettled([getCategories(), getCart()])
  if (c.status === 'fulfilled') categories.value = c.value.data
  if (cart.status === 'fulfilled') cartCount.value = cart.value.data.length
  const initCat = route.query.category
  if (initCat) selectCategory(Number(initCat))
  else if (categories.value.length) selectCategory(categories.value[0].id)
})

async function selectCategory(catId) {
  activeCat.value = catId
  const res = await getProducts(catId)
  products.value = res.data
}

async function openSpec(product) {
  const res = await getProductDetail(product.id)
  selectedProduct.value = res.data
  const defaults = {}
  for (const [type, specs] of Object.entries(res.data.specs || {})) {
    if (specs.length) defaults[type] = specs[0].id
  }
  selectedSpecs.value = defaults
  quantity.value = 1
  drawerVisible.value = true
}

async function handleAddToCart() {
  const specIds = Object.values(selectedSpecs.value)
  try {
    await addToCart({ productId: selectedProduct.value.id, specIds, quantity: quantity.value })
    ElMessage.success('已加入购物车')
    cartCount.value++
    drawerVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  }
}
</script>

<style scoped>
.menu-page { display: flex; flex-direction: column; height: 100vh; background: var(--main-bg); }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; background: #fff; border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.header-nav { display: flex; align-items: center; gap: 8px; }
.page-title { font-size: 17px; font-weight: 600; color: var(--text-primary); }
.top-bar a { color: var(--text-primary); }
.menu-body { display: flex; flex: 1; overflow: hidden; }
.sidebar {
  width: 88px; background: #fff; overflow-y: auto; flex-shrink: 0;
  border-right: 1px solid var(--border-color);
}
.cat-item {
  padding: 16px 8px; text-align: center; font-size: 13px; cursor: pointer;
  border-left: 3px solid transparent; transition: all 0.2s; color: #606266;
}
.cat-item.active {
  background: var(--main-bg); border-left-color: var(--primary-color);
  color: var(--primary-color); font-weight: 600;
}
.product-list { flex: 1; overflow-y: auto; padding: 12px; }
.product-card {
  display: flex; gap: 14px; background: #fff; border-radius: 12px;
  padding: 14px; margin-bottom: 10px; cursor: pointer; transition: all 0.2s;
}
.product-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.prod-img {
  width: 72px; height: 72px; border-radius: 12px;
  background: linear-gradient(135deg, #409eff, #764ba2);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 22px; flex-shrink: 0;
}
.prod-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.prod-name { font-weight: 600; font-size: 15px; color: var(--text-primary); }
.prod-desc { font-size: 12px; color: #909399; margin-top: 4px; }
.prod-price { color: #f56c6c; font-weight: 700; font-size: 16px; margin-top: 6px; }
.spec-content { padding: 0 10px; }
.spec-price { font-size: 28px; color: #f56c6c; font-weight: 700; margin-bottom: 20px; }
.spec-group { margin-bottom: 16px; }
.spec-label { font-size: 14px; font-weight: 600; margin-bottom: 10px; color: var(--text-primary); }
.quantity-row { display: flex; justify-content: space-between; align-items: center; margin-top: 16px; }
.add-btn { width: 100%; margin-top: 20px; border-radius: 8px; height: 44px; font-size: 15px; }
</style>
