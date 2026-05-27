<template>
  <div class="menu-page">
    <!-- 顶栏 -->
    <div class="top-bar">
      <el-page-header @back="$router.push('/')" content="菜单" />
      <router-link to="/cart"><el-badge :value="cartCount" :hidden="cartCount===0">🛒</el-badge></router-link>
    </div>

    <div class="menu-body">
      <!-- 左侧分类 -->
      <div class="sidebar">
        <div v-for="cat in categories" :key="cat.id"
             :class="['cat-item', { active: activeCat === cat.id }]"
             @click="selectCategory(cat.id)">
          {{ cat.name }}
        </div>
      </div>

      <!-- 右侧商品 -->
      <div class="product-list">
        <div v-for="p in products" :key="p.id" class="product-card" @click="openSpec(p)">
          <div class="prod-img">{{ p.name[0] }}</div>
          <div class="prod-info">
            <div class="prod-name">{{ p.name }}</div>
            <div class="prod-desc">{{ p.description }}</div>
            <div class="prod-price">¥{{ p.price }}</div>
          </div>
        </div>
        <div v-if="!products.length" class="empty">暂无商品</div>
      </div>
    </div>

    <!-- 规格选择弹窗 -->
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

        <el-button type="primary" size="large" style="width:100%;margin-top:16px" @click="handleAddToCart">
          加入购物车
        </el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories, getProducts, getProductDetail, addToCart, getCart } from '@/api'
import { ElMessage } from 'element-plus'

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
  if (initCat) {
    selectCategory(Number(initCat))
  } else if (categories.value.length) {
    selectCategory(categories.value[0].id)
  }
})

async function selectCategory(catId) {
  activeCat.value = catId
  const res = await getProducts(catId)
  products.value = res.data
}

async function openSpec(product) {
  const res = await getProductDetail(product.id)
  selectedProduct.value = res.data
  // 默认选中每个规格组的第一个
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
.menu-page { display: flex; flex-direction: column; height: 100vh; }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 16px; background: #fff; border-bottom: 1px solid #eee;
}
.menu-body { display: flex; flex: 1; overflow: hidden; }
.sidebar {
  width: 90px; background: #f5f5f5; overflow-y: auto; flex-shrink: 0;
}
.cat-item {
  padding: 14px 8px; text-align: center; font-size: 13px; cursor: pointer;
  border-left: 3px solid transparent;
}
.cat-item.active {
  background: #fff; border-left-color: #764ba2; color: #764ba2; font-weight: bold;
}
.product-list { flex: 1; overflow-y: auto; padding: 10px; }
.product-card {
  display: flex; gap: 12px; background: #fff; border-radius: 8px;
  padding: 12px; margin-bottom: 10px; cursor: pointer;
}
.prod-img {
  width: 70px; height: 70px; border-radius: 8px; background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0;
}
.prod-info { flex: 1; }
.prod-name { font-weight: bold; margin-bottom: 4px; }
.prod-desc { font-size: 12px; color: #999; margin-bottom: 6px; }
.prod-price { color: #e74c3c; font-weight: bold; }
.empty { text-align: center; color: #999; padding: 40px; }
.spec-content { padding: 0 10px; }
.spec-price { font-size: 24px; color: #e74c3c; font-weight: bold; margin-bottom: 16px; }
.spec-group { margin-bottom: 14px; }
.spec-label { font-size: 14px; font-weight: bold; margin-bottom: 8px; }
.quantity-row { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
</style>
