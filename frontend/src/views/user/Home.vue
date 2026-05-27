<template>
  <div class="home">
    <!-- 顶部导航 -->
    <div class="nav-bar">
      <span class="title">茶小智</span>
      <div class="nav-right">
        <router-link to="/cart"><el-badge :value="cartCount" :hidden="cartCount===0">🛒</el-badge></router-link>
        <router-link to="/orders">订单</router-link>
        <router-link to="/profile">我的</router-link>
      </div>
    </div>

    <!-- 轮播图 -->
    <el-carousel v-if="banners.length" height="180px" class="banner">
      <el-carousel-item v-for="b in banners" :key="b.id">
        <div class="banner-item" :style="{backgroundImage: `url(${b.image})`}" />
      </el-carousel-item>
    </el-carousel>

    <!-- 分类入口 -->
    <div class="section">
      <h3>商品分类</h3>
      <div class="category-grid">
        <div v-for="cat in categories" :key="cat.id" class="category-item" @click="goMenu(cat.id)">
          <div class="cat-icon">{{ cat.name[0] }}</div>
          <span>{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <!-- AI 推荐 -->
    <div class="section">
      <h3>🌟 为你推荐</h3>
      <div v-if="recommends.length" class="recommend-list">
        <el-card v-for="r in recommends" :key="r.productId" class="recommend-card" shadow="hover">
          <div class="rec-name">{{ r.name }}</div>
          <div class="rec-reason">{{ r.reason }}</div>
        </el-card>
      </div>
      <div v-else class="loading">加载推荐中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBanners, getCategories, getRecommend, getCart } from '@/api'

const router = useRouter()
const banners = ref([])
const categories = ref([])
const recommends = ref([])
const cartCount = ref(0)

onMounted(async () => {
  const [b, c, r, cart] = await Promise.allSettled([
    getBanners(), getCategories(), getRecommend(), getCart()
  ])
  if (b.status === 'fulfilled') banners.value = b.value.data
  if (c.status === 'fulfilled') categories.value = c.value.data
  if (r.status === 'fulfilled') recommends.value = r.value.data
  if (cart.status === 'fulfilled') cartCount.value = cart.value.data.length
})

function goMenu(catId) {
  router.push({ path: '/menu', query: { category: catId } })
}
</script>

<style scoped>
.home { padding-bottom: 20px; }
.nav-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; background: #fff; position: sticky; top: 0; z-index: 10;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
}
.title { font-size: 20px; font-weight: bold; color: #764ba2; }
.nav-right { display: flex; gap: 16px; align-items: center; }
.nav-right a { text-decoration: none; color: #333; font-size: 14px; }
.banner { margin: 10px 16px; border-radius: 12px; overflow: hidden; }
.banner-item { height: 100%; background-size: cover; background-position: center; background-color: #eee; }
.section { padding: 0 16px; margin-top: 16px; }
.section h3 { margin-bottom: 12px; font-size: 16px; }
.category-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.category-item {
  display: flex; flex-direction: column; align-items: center; cursor: pointer;
  padding: 12px 0; background: #fff; border-radius: 8px;
}
.cat-icon {
  width: 44px; height: 44px; border-radius: 50%; background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; display: flex; align-items: center; justify-content: center; font-size: 18px; margin-bottom: 6px;
}
.recommend-list { display: flex; gap: 12px; overflow-x: auto; }
.recommend-card { min-width: 200px; cursor: pointer; }
.rec-name { font-weight: bold; margin-bottom: 6px; }
.rec-reason { font-size: 12px; color: #888; }
.loading { text-align: center; color: #999; padding: 20px; }
</style>
