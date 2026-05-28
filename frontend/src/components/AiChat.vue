<template>
  <div class="ai-chat">
    <!-- 悬浮按钮 -->
    <div class="chat-btn" @click="visible = !visible">
      <span>{{ visible ? '✕' : '💬' }}</span>
    </div>

    <!-- 聊天窗口 -->
    <div v-show="visible" class="chat-window">
      <div class="chat-header">茶小智 AI 助手</div>
      <div class="chat-body" ref="bodyRef">
        <div v-for="(msg, i) in messages" :key="i" :class="['msg', msg.role]">
          <div class="msg-bubble">{{ msg.content }}</div>
        </div>
        <div v-if="loading" class="msg ai">
          <div class="msg-bubble loading-dots">思考中...</div>
        </div>
      </div>
      <div class="chat-input">
        <el-input v-model="input" placeholder="输入消息..." @keyup.enter="sendMessage" :disabled="loading" />
        <el-button type="primary" @click="sendMessage" :loading="loading" :disabled="!input.trim()">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
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
    const response = await fetch('/api/ai/chat', {
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
.chat-btn {
  position: fixed; bottom: 24px; right: 24px; width: 50px; height: 50px;
  border-radius: 50%; background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 22px; cursor: pointer; box-shadow: 0 4px 12px rgba(0,0,0,.2); z-index: 1000;
}
.chat-window {
  position: fixed; bottom: 84px; right: 24px; width: 340px; height: 460px;
  background: #fff; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,.15);
  display: flex; flex-direction: column; z-index: 1000; overflow: hidden;
}
.chat-header {
  padding: 12px 16px; background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; font-weight: bold; font-size: 14px;
}
.chat-body { flex: 1; overflow-y: auto; padding: 12px; background: #f9f9f9; }
.msg { margin-bottom: 10px; display: flex; }
.msg.user { justify-content: flex-end; }
.msg.ai { justify-content: flex-start; }
.msg-bubble {
  max-width: 80%; padding: 8px 12px; border-radius: 12px; font-size: 13px; line-height: 1.5;
  word-break: break-all;
}
.msg.user .msg-bubble { background: #667eea; color: #fff; border-bottom-right-radius: 4px; }
.msg.ai .msg-bubble { background: #fff; color: #333; border-bottom-left-radius: 4px; box-shadow: 0 1px 2px rgba(0,0,0,.06); }
.loading-dots { color: #999; }
.chat-input { display: flex; gap: 8px; padding: 10px; border-top: 1px solid #eee; }
</style>
