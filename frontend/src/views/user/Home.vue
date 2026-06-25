<template>
  <AppShell>
  <div class="home">
    <!-- Hero 顶栏 -->
    <header class="hero">
      <div class="hero__bg"></div>
      <div class="hero__content">
        <div class="hero__top">
          <div class="hero__brand">
            <div class="seal seal--md hero__seal">茶</div>
            <div class="hero__titlebox">
              <div class="hero__wordmark font-display">TeaSmart</div>
              <div class="hero__cn font-heading">茶小智</div>
            </div>
          </div>
        </div>
        <p class="hero__poem">一盏清雅 · 余韵悠长</p>
      </div>
    </header>

    <!-- 轮播图 -->
    <section v-if="banners.length" class="block">
      <el-carousel height="150px" class="banner" indicator-position="none" arrow="never">
        <el-carousel-item v-for="b in banners" :key="b.id">
          <div class="banner__item" :style="{ backgroundImage: `url(${b.image})` }" @click="goBanner(b.link)">
            <span v-if="b.title" class="banner__cap">{{ b.title }}</span>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 茶类 -->
    <section class="block rise" style="animation-delay: 0.05s">
      <div class="sec-head">
        <span class="sec-head__tick"></span>
        <h3 class="sec-head__title font-heading">寻 · 茶类</h3>
      </div>
      <div class="cat-grid">
        <div
          v-for="(cat, i) in categories"
          :key="cat.id"
          class="cat rise"
          :style="{ animationDelay: `${0.05 + i * 0.05}s` }"
          @click="goMenu(cat.id)"
        >
          <div class="cat__emoji">{{ catEmoji(cat.name, i) }}</div>
          <span class="cat__name font-heading">{{ cat.name }}</span>
        </div>
      </div>
    </section>

    <!-- AI 推荐 -->
    <section class="block rise" style="animation-delay: 0.1s">
      <div class="sec-head">
        <div class="seal seal--sm sec-head__seal">荐</div>
        <h3 class="sec-head__title font-heading">茶小智 · 荐茶</h3>
      </div>
      <div v-if="recommends.length" class="rec-track">
        <article v-for="r in recommends" :key="r.productId" class="rec" @click="goMenu()">
          <div class="rec__name font-heading">{{ r.name }}</div>
          <hr class="gold-line rec__line" />
          <p class="rec__reason">{{ r.reason }}</p>
          <span class="rec__go">尝这一盏 →</span>
        </article>
      </div>
      <div v-else class="rec-empty">
        <span class="font-heading">茶小智正为您择茶…</span>
      </div>
    </section>

    <div class="home__foot">
      <hr class="gold-line" />
      <p class="home__foot-txt font-heading">— 茶小智 TeaSmart —</p>
    </div>
  </div>
  </AppShell>
</template>

<script>
// 模块级缓存：AI 推荐按 token 区分，整页会话内同一用户只请求一次
// （独立 <script> 块在模块加载时求值一次，跨 Home 重挂载保留；<script setup> 内顶层变量是每次 setup 重建的，留不住）
// 用 token 作 key，注销/换号登录后自动失效重取，避免展示上一用户的推荐
let recommendState = { token: null, promise: null }
</script>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBanners, getCategories, getRecommend } from '@/api'
import { useUserStore } from '@/stores/user'
import AppShell from '@/components/AppShell.vue'

const router = useRouter()
const userStore = useUserStore()
const banners = ref([])
const categories = ref([])
const recommends = ref([])

const EMOJI_POOL = ['🍵', '🧋', '🍃', '🥛', '🍹', '🌸', '🫖', '🍋']
function catEmoji(name, i) {
  const map = { 绿: '🍵', 红: '🫖', 奶: '🧋', 果: '🍹', 茶: '🍃', 花: '🌸', 咖: '☕' }
  for (const key of Object.keys(map)) {
    if (name && name.includes(key)) return map[key]
  }
  return EMOJI_POOL[i % EMOJI_POOL.length]
}

onMounted(async () => {
  const [b, c] = await Promise.allSettled([getBanners(), getCategories()])
  if (b.status === 'fulfilled') banners.value = b.value.data
  if (c.status === 'fulfilled') categories.value = c.value.data
  // AI 推荐：同一 token 复用缓存；token 变化（注销/换号）则重取；失败则下次可重试
  const tok = userStore.token
  if (tok !== recommendState.token || !recommendState.promise) {
    recommendState.token = tok
    recommendState.promise = getRecommend()
      .then((res) => res.data)
      .catch(() => { recommendState.token = null; recommendState.promise = null; return [] })
  }
  recommendState.promise.then((data) => { recommends.value = data })
})

function goMenu(catId) {
  router.push({ path: '/menu', query: catId ? { category: catId } : {} })
}
function goBanner(link) {
  if (link) router.push(link)
}
</script>

<style scoped>
.home {
  padding-bottom: 20px;
}

/* —— Hero —— */
.hero {
  position: relative;
  overflow: hidden;
  padding: 22px 20px 26px;
  color: #f5efe0;
}
.hero__bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 85% 10%, rgba(110, 139, 90, 0.5) 0, transparent 55%),
    radial-gradient(circle at 10% 90%, rgba(139, 90, 43, 0.4) 0, transparent 55%),
    linear-gradient(160deg, var(--tea-ink) 0%, var(--tea-ink-900) 100%);
}
.hero__content {
  position: relative;
}
.hero__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.hero__brand {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hero__seal {
  transform: rotate(-6deg);
  background: #f7e9c6;
  color: var(--tea-vermilion);
  box-shadow: inset 0 0 0 1.5px var(--tea-vermilion);
}
.hero__wordmark {
  font-size: 30px;
  font-weight: 600;
  line-height: 1;
  letter-spacing: 1px;
}
.hero__cn {
  font-size: 13px;
  letter-spacing: 10px;
  padding-left: 2px;
  opacity: 0.8;
  margin-top: 4px;
}
.hero__poem {
  margin: 22px 0 0;
  font-family: var(--font-heading);
  font-size: 14px;
  letter-spacing: 4px;
  opacity: 0.85;
}

/* —— 通用块 —— */
.block {
  padding: 0 16px;
  margin-top: 22px;
}
.sec-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}
.sec-head__tick {
  width: 3px;
  height: 16px;
  background: var(--tea-vermilion);
  border-radius: 2px;
}
.sec-head__seal {
  transform: rotate(-5deg) scale(0.82);
}
.sec-head__title {
  font-size: 17px;
  font-weight: 600;
  color: var(--tea-ink-text);
  letter-spacing: 2px;
}

/* —— 轮播 —— */
.banner {
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}
.banner__item {
  height: 100%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: flex-end;
  padding: 12px 14px;
}
.banner__cap {
  font-family: var(--font-heading);
  font-size: 13px;
  color: #f5efe0;
  background: rgba(24, 40, 25, 0.55);
  padding: 3px 10px;
  border-radius: 4px;
  letter-spacing: 1px;
}

/* —— 茶类 —— */
.cat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px 8px;
}
.cat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.cat__emoji {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  background: var(--tea-paper-2);
  border: 1px solid var(--tea-line);
  box-shadow: inset 0 0 0 4px var(--tea-paper);
  transition: transform 0.25s, box-shadow 0.25s;
}
.cat:hover .cat__emoji {
  transform: translateY(-3px);
  box-shadow: inset 0 0 0 4px var(--tea-paper), 0 6px 14px rgba(47, 82, 51, 0.12);
}
.cat__name {
  font-size: 12px;
  color: var(--tea-text-2);
  letter-spacing: 1px;
}

/* —— AI 推荐 —— */
.rec-track {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 2px 0 10px;
  scroll-snap-type: x mandatory;
}
.rec {
  min-width: 220px;
  scroll-snap-align: start;
  background: var(--tea-paper-2);
  border: 1px solid var(--tea-line);
  border-radius: var(--radius);
  padding: 14px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.rec:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card);
}
.rec__name {
  font-size: 16px;
  font-weight: 600;
  color: var(--tea-ink-text);
  letter-spacing: 1px;
}
.rec__line {
  margin: 8px 0;
}
.rec__reason {
  margin: 0;
  font-size: 12.5px;
  line-height: 1.6;
  color: var(--tea-text-2);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.rec__go {
  display: inline-block;
  margin-top: 10px;
  font-size: 12px;
  color: var(--tea-vermilion);
  letter-spacing: 1px;
}
.rec-empty {
  text-align: center;
  padding: 26px 0;
  color: var(--tea-text-3);
  font-size: 13px;
  letter-spacing: 2px;
}

/* —— 页脚 —— */
.home__foot {
  text-align: center;
  margin: 30px 24px 0;
}
.home__foot-txt {
  margin-top: 12px;
  font-size: 12px;
  color: var(--tea-text-3);
  letter-spacing: 4px;
}
</style>
