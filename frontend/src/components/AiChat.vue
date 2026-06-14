<template>
  <div class="ai-chat">
    <!-- 悬浮按钮 -->
    <button class="chat-fab" :class="{ 'is-open': visible }" @click="visible = !visible" aria-label="茶小智">
      <span class="chat-fab__inner">
        <el-icon v-if="visible" :size="22"><Close /></el-icon>
        <span v-else class="chat-fab__glyph">智</span>
      </span>
    </button>

    <!-- 聊天窗口 -->
    <transition name="chat-pop">
      <div v-if="visible" class="chat-window">
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
          <div v-if="loading" class="msg msg--ai">
            <div class="msg__bubble msg__bubble--muted">研墨中…</div>
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
import { ref, nextTick } from 'vue'
import { Close } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const visible = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref([])
const bodyRef = ref()

async function sendMessage() {
  const text = input.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
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
    messages.value.push({ role: 'ai', content: '' })

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
          aiContent += data
          messages.value[messages.value.length - 1].content = aiContent
          scrollToBottom()
        }
      }
    }

    // 如果没有收到任何内容，显示提示
    if (!aiContent) {
      messages.value[messages.value.length - 1].content = '抱歉，AI 服务暂时不可用。'
    }
  } catch (e) {
    // 只有当前消息为空时才显示错误
    const lastMsg = messages.value[messages.value.length - 1]
    if (!lastMsg || lastMsg.role !== 'ai' || !lastMsg.content) {
      messages.value.push({ role: 'ai', content: '抱歉，AI 服务暂时不可用。' })
    }
  } finally {
    loading.value = false
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
/* 悬浮按钮：墨绿圆 + 金线环 + 印章字 */
.chat-fab {
  position: fixed;
  bottom: calc(84px + env(safe-area-inset-bottom));
  right: 18px;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  border: none;
  padding: 0;
  cursor: pointer;
  background: var(--tea-ink);
  box-shadow: 0 8px 24px rgba(24, 40, 25, 0.35);
  z-index: 1000;
  transition: transform 0.25s, box-shadow 0.25s;
}
.chat-fab:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(24, 40, 25, 0.42);
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

/* 窗口 */
.chat-window {
  position: fixed;
  bottom: calc(150px + env(safe-area-inset-bottom));
  right: 18px;
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

/* 桌面端跟随居中列右移 */
@media (min-width: 720px) {
  .chat-fab {
    right: calc(50% - 300px + 18px);
  }
  .chat-window {
    right: calc(50% - 300px + 18px);
  }
}
</style>
