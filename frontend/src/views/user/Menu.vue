<template>
  <AppShell>
  <div class="menu">
    <AppHeader title="茶 · 单" back-to="/" />

    <div class="menu__body">
      <aside class="menu__rail">
        <div
          v-for="cat in categories"
          :key="cat.id"
          :class="['rail__item', { 'rail__item--active': activeCat === cat.id }]"
          @click="selectCategory(cat.id)"
        >
          <span class="rail__name font-heading">{{ cat.name }}</span>
        </div>
      </aside>

      <div class="menu__list">
        <article
          v-for="(p, i) in products"
          :key="p.id"
          class="prod rise"
          :style="{ animationDelay: `${i * 0.04}s` }"
          @click="openSpec(p)"
        >
          <div class="prod__bar"></div>
          <div class="prod__main">
            <div class="prod__name font-heading">{{ p.name }}</div>
            <div v-if="p.description" class="prod__desc">{{ p.description }}</div>
            <div class="prod__bottom">
              <span class="price prod__price"><small>¥</small>{{ p.price }}</span>
            </div>
          </div>
          <button class="prod__add" @click.stop="openSpec(p)" aria-label="收入茶盏">
            <el-icon :size="18"><Plus /></el-icon>
          </button>
        </article>

        <div v-if="!products.length" class="menu__empty">
          <el-empty description="此间暂无茶品" />
        </div>
      </div>
    </div>

    <el-drawer
      v-model="drawerVisible"
      :title="selectedProduct?.name"
      direction="btt"
      size="62%"
      class="tea-drawer"
      modal-class="tea-overlay"
    >
      <div v-if="selectedProduct" class="spec">
        <div class="spec__price">
          <span class="price"><small>¥</small>{{ totalPrice }}</span>
          <span class="spec__unit">/ 杯</span>
        </div>
        <div v-for="(specs, type) in selectedProduct.specs" :key="type" class="spec__group">
          <div class="spec__label font-heading">{{ specLabels[type] || type }}</div>
          <el-radio-group v-model="selectedSpecs[type]">
            <el-radio-button v-for="s in specs" :key="s.id" :value="s.id">
              {{ s.specName }}{{ s.priceDiff > 0 ? ` +¥${s.priceDiff}` : '' }}
            </el-radio-button>
          </el-radio-group>
        </div>
        <div class="spec__qty">
          <span class="spec__label font-heading">数量</span>
          <el-input-number v-model="quantity" :min="1" :max="99" />
        </div>
        <el-button type="primary" size="large" class="spec__btn" @click="handleAddToCart">
          收入茶盏
        </el-button>
      </div>
    </el-drawer>
  </div>
  </AppShell>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories, getProducts, getProductDetail, addToCart } from '@/api'
import { refresh as refreshCartCount } from '@/composables/useCartCount'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AppShell from '@/components/AppShell.vue'
import AppHeader from '@/components/AppHeader.vue'

const route = useRoute()
const categories = ref([])
const products = ref([])
const activeCat = ref(null)
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
    const spec = specs.find((s) => s.id === specId)
    if (spec) price += Number(spec.priceDiff)
  }
  return price
})

onMounted(async () => {
  const c = await Promise.allSettled([getCategories()])
  if (c[0].status === 'fulfilled') categories.value = c[0].value.data
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
    ElMessage.success('已收入茶盏')
    drawerVisible.value = false
    refreshCartCount()  // 实时同步底部购物车角标
  } catch (e) {
    ElMessage.error(e.message || '添加失败')
  }
}
</script>

<style scoped>
.menu {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.menu__body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* —— 分类导轨 —— */
.menu__rail {
  width: 84px;
  flex-shrink: 0;
  overflow-y: auto;
  background: var(--tea-paper);
  border-right: 1px solid var(--tea-line);
}
.rail__item {
  position: relative;
  padding: 18px 6px;
  text-align: center;
  font-size: 13px;
  cursor: pointer;
  color: var(--tea-text-2);
  transition: background 0.2s, color 0.2s;
}
.rail__item--active {
  background: var(--tea-paper-2);
  color: var(--tea-vermilion);
  font-weight: 600;
}
.rail__item--active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 22px;
  background: var(--tea-vermilion);
  border-radius: 0 2px 2px 0;
}
.rail__name {
  letter-spacing: 2px;
}

/* —— 茶品卡 —— */
.menu__list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}
.prod {
  position: relative;
  display: flex;
  align-items: stretch;
  gap: 4px;
  background: var(--tea-paper-2);
  border: 1px solid var(--tea-line);
  border-radius: var(--radius);
  padding: 14px 14px 14px 18px;
  margin-bottom: 10px;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}
.prod:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-card);
}
.prod__bar {
  position: absolute;
  left: 0;
  top: 14px;
  bottom: 14px;
  width: 3px;
  background: linear-gradient(var(--tea-amber), var(--tea-gold));
  border-radius: 0 2px 2px 0;
  opacity: 0.5;
}
.prod__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.prod__name {
  font-size: 16px;
  font-weight: 600;
  color: var(--tea-ink-text);
  letter-spacing: 1px;
}
.prod__desc {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--tea-text-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.prod__bottom {
  margin-top: auto;
  padding-top: 8px;
}
.prod__price {
  font-size: 19px;
}
.prod__price small {
  font-size: 12px;
  font-weight: 500;
}
.prod__add {
  align-self: center;
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border: none;
  border-radius: 50%;
  background: var(--tea-vermilion);
  color: #f7e9c6;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: inset 0 0 0 1.5px rgba(247, 233, 198, 0.5);
  transition: transform 0.2s;
}
.prod__add:hover {
  transform: scale(1.08);
}
.menu__empty {
  padding: 40px 0;
}

/* —— 规格 drawer —— */
.spec {
  padding: 16px 20px calc(20px + env(safe-area-inset-bottom));
}
.spec__price {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 20px;
}
.spec__price .price {
  font-size: 34px;
}
.spec__price .price small {
  font-size: 16px;
}
.spec__unit {
  font-size: 13px;
  color: var(--tea-text-3);
}
.spec__group {
  margin-bottom: 18px;
}
.spec__label {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
  color: var(--tea-ink-text);
  letter-spacing: 2px;
}
.spec__qty {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 18px;
}
.spec__btn {
  width: 100%;
  margin-top: 22px;
  height: 46px;
  border-radius: var(--radius);
  font-family: var(--font-heading);
  font-size: 15px;
  letter-spacing: 4px;
}

</style>
