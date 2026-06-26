<template>
  <div class="ai-chat">
    <!-- 悬浮按钮：可拖动，释放后贴边吸附，位置记忆 -->
    <button
      class="chat-fab"
      :class="{ 'is-open': visible, 'is-dragging': dragging }"
      :style="fabStyle"
      @pointerdown="onPointerDown"
      @pointermove="onPointerMove"
      @pointerup="onPointerUp"
      @pointercancel="onPointerUp"
      @click="onClick"
      aria-label="茶小智"
    >
      <span class="chat-fab__inner">
        <el-icon v-if="visible" :size="22"><Close /></el-icon>
        <span v-else class="chat-fab__glyph">智</span>
      </span>
    </button>

    <!-- 聊天窗口 -->
    <transition name="chat-pop">
      <div v-if="visible" class="chat-window" :class="`chat-window--${fabSide}`">
        <div class="chat-head">
          <div class="chat-head__seal seal seal--sm">茶</div>
          <div class="chat-head__meta">
            <div class="chat-head__name">茶小智</div>
            <div class="chat-head__sub">为你寻一盏好茶</div>
          </div>
        </div>

        <div class="chat-body" ref="bodyRef">
          <div v-for="(msg, i) in messages" :key="i" :class="['msg', `msg--${msg.role}`]">
            <div class="msg__bubble">{{ msg.content }}</div>
          </div>
          <!-- 仅在等待 AI 首字时显示；首个 token 到达后由流式气泡接管 -->
          <div v-if="thinking" class="msg msg--ai">
            <div class="msg__bubble msg__bubble--muted">烹茶中…</div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="input"
            placeholder="说点什么，如「推荐一杯不苦的茶」"
            @keyup.enter="sendMessage"
            :disabled="loading"
            size="default"
          />
          <el-button type="primary" @click="sendMessage" :loading="loading" :disabled="!input.trim()">
            发送
          </el-button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, computed, onMounted, onBeforeUnmount } from 'vue'
import { Close } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

// FAB 尺寸与边界（px）
const FAB_SIZE = 54
const EDGE_MARGIN = 18        // 离左右边缘距离
const TOP_MARGIN = 12         // 顶部最小距离
const BOTTOM_MARGIN = 90      // 底部最小距离（避开底部导航栏 + 安全区近似）
const DRAG_THRESHOLD = 6      // 超过此位移才视为拖动，否则当作点击
const STORAGE_KEY = 'teasmart:ai-fab-pos'
const DESKTOP_COL = 600       // 桌面端居中列宽度（与 AppShell 一致）
const DESKTOP_BREAK = 720

const userStore = useUserStore()
const visible = ref(false)
const input = ref('')
const loading = ref(false)
const thinking = ref(false)   // 等待 AI 首字，独立于 loading
const messages = ref([])
const bodyRef = ref()

// FAB 定位（fixed 的 left/top，px）与贴边方向
const fabPos = ref({ x: 0, y: 0 })
const fabSide = ref('right')
const dragging = ref(false)
const dragState = ref(null)   // { startX, startY, originX, originY, pointerId, moved }

const fabStyle = computed(() => ({
  left: `${fabPos.value.x}px`,
  top: `${fabPos.value.y}px`,
}))

// 某一侧贴边时的 x 坐标：桌面端对齐居中列，移动端对齐视口边缘
function edgeX(side) {
  const vw = window.innerWidth
  if (vw >= DESKTOP_BREAK) {
    const colLeft = (vw - DESKTOP_COL) / 2
    return side === 'left'
      ? colLeft + EDGE_MARGIN
      : colLeft + DESKTOP_COL - FAB_SIZE - EDGE_MARGIN
  }
  return side === 'left' ? EDGE_MARGIN : vw - FAB_SIZE - EDGE_MARGIN
}

function defaultPosition() {
  const vh = window.innerHeight
  return { x: edgeX('right'), y: vh - FAB_SIZE - BOTTOM_MARGIN }
}

function clampPos(x, y) {
  const vw = window.innerWidth
  const vh = window.innerHeight
  return {
    x: Math.min(Math.max(x, EDGE_MARGIN), vw - FAB_SIZE - EDGE_MARGIN),
    y: Math.min(Math.max(y, TOP_MARGIN), vh - FAB_SIZE - BOTTOM_MARGIN),
  }
}

function snapToEdge() {
  const vw = window.innerWidth
  const side = fabPos.value.x + FAB_SIZE / 2 < vw / 2 ? 'left' : 'right'
  fabPos.value = { x: edgeX(side), y: fabPos.value.y }
  fabSide.value = side
}

function loadPos() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return false
    const { side, y } = JSON.parse(raw)
    const resolved = side === 'left' ? 'left' : 'right'
    fabPos.value = clampPos(edgeX(resolved), y)
    fabSide.value = resolved
    return true
  } catch {
    return false
  }
}

function savePos() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ side: fabSide.value, y: fabPos.value.y }))
  } catch {
    /* 忽略隐私模式等写入失败 */
  }
}

// —— 拖动：按下→移动→释放；未越过阈值视为点击，由 @click 处理开关 ——
function onPointerDown(e) {
  if (e.pointerType === 'mouse' && e.button !== 0) return
  dragState.value = {
    startX: e.clientX,
    startY: e.clientY,
    originX: fabPos.value.x,
    originY: fabPos.value.y,
    pointerId: e.pointerId,
    moved: false,
  }
  e.currentTarget.setPointerCapture?.(e.pointerId)
}

function onPointerMove(e) {
  const s = dragState.value
  if (!s || s.pointerId !== e.pointerId) return
  const dx = e.clientX - s.startX
  const dy = e.clientY - s.startY
  if (!s.moved && Math.hypot(dx, dy) > DRAG_THRESHOLD) {
    s.moved = true
    dragging.value = true
  }
  if (s.moved) {
    fabPos.value = clampPos(s.originX + dx, s.originY + dy)
  }
}

let suppressClick = false   // 拖动结束后吞掉紧接着的那次 click

function onPointerUp(e) {
  const s = dragState.value
  if (!s) return
  const wasMoved = s.moved
  try { e.currentTarget.releasePointerCapture?.(s.pointerId) } catch { /* noop */ }
  dragState.value = null
  if (wasMoved) {
    suppressClick = true
    snapToEdge()
    savePos()
    dragging.value = false
    setTimeout(() => { suppressClick = false }, 0)
  }
}

function onClick() {
  if (suppressClick) {
    suppressClick = false   // 拖动已消费，吞掉这次 click
    return
  }
  visible.value = !visible.value
}

function onResize() {
  // 视口变化后重新约束并以当前侧重新贴边，避免越界
  fabPos.value = clampPos(fabPos.value.x, fabPos.value.y)
  fabPos.value = { x: edgeX(fabSide.value), y: fabPos.value.y }
}

onMounted(() => {
  if (!loadPos()) {
    fabPos.value = defaultPosition()
    fabSide.value = 'right'
  }
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
})

async function sendMessage() {
  const text = input.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  thinking.value = true
  scrollToBottom()

  try {
    const response = await fetch(`${import.meta.env.BASE_URL}api/ai/chat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userStore.token}`,
      },
      body: JSON.stringify({ message: text }),
    })

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let aiContent = ''
    let aiMsg = null

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      const lines = chunk.split('\n')

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim()
          if (data === '[DONE]') continue
          if (!data) continue
          // 首个 token 到达：插入 AI 气泡，并停止「烹茶中…」
          if (!aiMsg) {
            aiMsg = { role: 'ai', content: '' }
            messages.value.push(aiMsg)
            thinking.value = false
          }
          aiContent += data
          aiMsg.content = aiContent
          scrollToBottom()
        }
      }
    }

    // 如果没有收到任何内容，显示提示
    if (!aiContent) {
      messages.value.push({ role: 'ai', content: '小智暂歇，稍后再来' })
    }
  } catch (e) {
    // 只有当前消息为空时才显示错误
    const lastMsg = messages.value[messages.value.length - 1]
    if (!lastMsg || lastMsg.role !== 'ai' || !lastMsg.content) {
      messages.value.push({ role: 'ai', content: '小智暂歇，稍后再来' })
    }
  } finally {
    loading.value = false
    thinking.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  })
}
</script>

<style scoped>
/* 悬浮按钮：墨绿圆 + 金线环 + 印章字；left/top 由 JS 内联控制 */
.chat-fab {
  position: fixed;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  border: none;
  padding: 0;
  cursor: pointer;
  background: var(--tea-ink);
  box-shadow: 0 8px 24px rgba(24, 40, 25, 0.35);
  z-index: 1000;
  transition: transform 0.25s, box-shadow 0.25s, left 0.2s ease, top 0.2s ease;
  touch-action: none;          /* 拖动时阻止页面滚动 */
  user-select: none;
  -webkit-user-select: none;
}
.chat-fab:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(24, 40, 25, 0.42);
}
.chat-fab.is-dragging {
  transition: none;            /* 跟手无延迟 */
  transform: scale(1.06);
  cursor: grabbing;
  box-shadow: 0 14px 32px rgba(24, 40, 25, 0.48);
}
.chat-fab__inner {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  box-shadow: inset 0 0 0 1.5px var(--tea-gold);
  color: #f7e9c6;
}
.chat-fab__glyph {
  font-family: var(--font-heading);
  font-weight: 700;
  font-size: 22px;
  line-height: 1;
}

/* 窗口：水平方向跟随 FAB 贴边侧 */
.chat-window {
  position: fixed;
  bottom: calc(150px + env(safe-area-inset-bottom));
  width: min(340px, calc(100vw - 36px));
  height: min(460px, 60vh);
  background: var(--tea-paper-2);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-float);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1000;
  border: 1px solid var(--tea-line);
}
.chat-window--right {
  right: 18px;
}
.chat-window--left {
  left: 18px;
}
.chat-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: var(--tea-ink);
  color: #f5efe0;
  position: relative;
}
.chat-head::after {
  content: '';
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--tea-gold), transparent);
  opacity: 0.8;
}
.chat-head .seal {
  transform: rotate(0);
  background: #f7e9c6;
  color: var(--tea-vermilion);
  box-shadow: inset 0 0 0 1.5px var(--tea-vermilion);
}
.chat-head__name {
  font-family: var(--font-heading);
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
}
.chat-head__sub {
  font-size: 11px;
  opacity: 0.7;
  margin-top: 1px;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  background:
    radial-gradient(circle at 20% 12%, rgba(184, 149, 106, 0.06) 0, transparent 50%),
    var(--tea-paper);
}
.msg {
  margin-bottom: 10px;
  display: flex;
}
.msg--user {
  justify-content: flex-end;
}
.msg--ai {
  justify-content: flex-start;
}
.msg__bubble {
  max-width: 82%;
  padding: 9px 13px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}
.msg--user .msg__bubble {
  background: var(--tea-ink);
  color: #f5efe0;
  border-bottom-right-radius: 4px;
}
.msg--ai .msg__bubble {
  background: var(--tea-paper-2);
  color: var(--tea-ink-text);
  border: 1px solid var(--tea-line);
  border-bottom-left-radius: 4px;
}
.msg__bubble--muted {
  color: var(--tea-text-3);
  font-style: italic;
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 10px;
  border-top: 1px solid var(--tea-line);
  background: var(--tea-paper-2);
}

/* 弹出动画 */
.chat-pop-enter-active,
.chat-pop-leave-active {
  transition: transform 0.25s cubic-bezier(0.22, 0.61, 0.36, 1), opacity 0.25s;
  transform-origin: bottom right;
}
.chat-pop-enter-from,
.chat-pop-leave-to {
  transform: scale(0.9) translateY(10px);
  opacity: 0;
}

/* 桌面端：FAB 与窗口对齐居中 600px 列两侧 */
@media (min-width: 720px) {
  .chat-window--right {
    right: calc(50% - 300px + 18px);
    left: auto;
  }
  .chat-window--left {
    left: calc(50% - 300px + 18px);
    right: auto;
  }
}
</style>
