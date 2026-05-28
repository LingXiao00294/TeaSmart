<template>
  <div class="home">
    <!-- 顶部导航 -->
    <div class="nav-bar">
      <div class="brand">
        <el-icon class="brand-icon"><Coffee /></el-icon>
        <span class="title">TeaSmart</span>
      </div>
      <div class="nav-right">
        <router-link to="/cart"><el-badge :value="cartCount" :hidden="cartCount===0"><el-icon><ShoppingCart /></el-icon></el-badge></router-link>
        <router-link to="/orders"><el-icon><Tickets /></el-icon></router-link>
        <router-link to="/profile"><el-icon><User /></el-icon></router-link>
      </div>
    </div>

    <!-- 轮播图 -->
    <el-carousel v-if="banners.length" height="180px" class="banner" indicator-position="none">
      <el-carousel-item v-for="b in banners" :key="b.id">
        <div class="banner-item" :style="{backgroundImage: `url(${b.image})`}" @click="goBanner(b.link)" />
      </el-carousel-item>
    </el-carousel>

    <!-- 分类入口 -->
    <div class="section">
      <h3 class="section-title">商品分类</h3>
      <div class="category-grid">
        <div v-for="cat in categories" :key="cat.id" class="category-item" @click="goMenu(cat.id)">
          <div class="cat-icon">{{ cat.name[0] }}</div>
          <span class="cat-name">{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <!-- AI 推荐 -->
    <div class="section">
      <h3 class="section-title">🌟 为你推荐</h3>
      <div v-if="recommends.length" class="recommend-list">
        <el-card v-for="r in recommends" :key="r.productId" class="recommend-card" shadow="never">
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
import { Coffee, ShoppingCart, Tickets, User } from '@element-plus/icons-vue'


const router = useRouter()
const banners = ref([])
const categories = ref([])
const recommends = ref([])
const cartCount = ref(0)

onMounted(async () => {
  // 核心数据同步加载
  const [b, c, cart] = await Promise.allSettled([
    getBanners(), getCategories(), getCart()
  ])
  if (b.status === 'fulfilled') banners.value = b.value.data
  if (c.status === 'fulfilled') categories.value = c.value.data
  if (cart.status === 'fulfilled') cartCount.value = cart.value.data.length

  // AI 推荐异步加载，不阻塞页面
  getRecommend().then(res => { recommends.value = res.data }).catch(() => {})
})

function goMenu(catId) {
  router.push({ path: '/menu', query: { category: catId } })
}

function goBanner(link) {
  if (link) router.push(link)
}
</script>

<style scoped>
.home { padding-bottom: 30px; background-color: var(--main-bg); min-height: 100vh; }
.nav-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 15px 20px; background: #fff; position: sticky; top: 0; z-index: 10;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.brand { display: flex; align-items: center; gap: 8px; color: var(--primary-color); }
.brand-icon { font-size: 24px; }
.title { font-size: 20px; font-weight: 700; }
.nav-right { display: flex; gap: 20px; align-items: center; font-size: 20px; }
.nav-right a { color: var(--text-primary); }
.banner { margin: 20px 16px; border-radius: 16px; overflow: hidden; }
.banner-item { height: 100%; background-size: cover; background-position: center; cursor: pointer; }
.section { padding: 0 20px; margin-top: 24px; }
.section-title { font-size: 18px; font-weight: 600; margin-bottom: 16px; color: var(--text-primary); }
.category-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; }
.category-item {
  display: flex; flex-direction: column; align-items: center; cursor: pointer;
  padding: 15px 0; background: #fff; border-radius: 12px; transition: all 0.2s;
}
.category-item:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.cat-icon {
  width: 48px; height: 48px; border-radius: 12px; background: linear-gradient(135deg, #409eff, #764ba2);
  color: #fff; display: flex; align-items: center; justify-content: center; font-size: 20px; margin-bottom: 8px;
}
.cat-name { font-size: 13px; color: #606266; }
.recommend-list { display: flex; gap: 16px; overflow-x: auto; padding-bottom: 10px; }
.recommend-card { min-width: 220px; border-radius: 12px; border: none; }
.rec-name { font-weight: 600; font-size: 15px; margin-bottom: 8px; }
.rec-reason { font-size: 12px; color: #909399; line-height: 1.4; }
.loading { text-align: center; color: #999; padding: 20px; }
</style>
