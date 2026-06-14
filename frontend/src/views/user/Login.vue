<template>
  <div class="auth ink-texture">
    <div class="auth__card rise">
      <div class="auth__brand">
        <div class="seal seal--lg auth__seal">茶</div>
        <div class="auth__wordmark font-display">TeaSmart</div>
        <div class="auth__cn font-heading">茶 · 智</div>
      </div>
      <p class="auth__poem">一盏清雅，余韵悠长</p>

      <hr class="gold-line auth__divider" />

      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin" size="large" class="auth__form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="auth__btn" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>

      <div class="auth__links">
        <router-link to="/register">尚未入席 · 立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await request.post('/auth/login', form)
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
}
.auth__card {
  width: 100%;
  max-width: 380px;
  background: var(--tea-paper-2);
  border: 1px solid var(--tea-line);
  border-radius: var(--radius-lg);
  padding: 36px 30px 28px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.4);
  text-align: center;
  position: relative;
}
.auth__card::before {
  content: '';
  position: absolute;
  inset: 6px;
  border: 1px solid var(--tea-line);
  border-radius: calc(var(--radius-lg) - 4px);
  pointer-events: none;
  opacity: 0.6;
}
.auth__brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.auth__seal {
  transform: rotate(-6deg);
  margin-bottom: 4px;
}
.auth__wordmark {
  font-size: 38px;
  font-weight: 600;
  color: var(--tea-ink);
  letter-spacing: 2px;
  line-height: 1;
}
.auth__cn {
  font-size: 15px;
  color: var(--tea-amber);
  letter-spacing: 8px;
  padding-left: 8px;
}
.auth__poem {
  margin: 14px 0 0;
  font-family: var(--font-heading);
  font-size: 13px;
  color: var(--tea-text-3);
  letter-spacing: 3px;
}
.auth__divider {
  margin: 22px 0 24px;
}
.auth__btn {
  width: 100%;
  height: 46px;
  border-radius: var(--radius);
  font-family: var(--font-heading);
  font-size: 16px;
  letter-spacing: 8px;
  padding-left: 16px;
}
.auth__links {
  margin-top: 18px;
  font-size: 13px;
}
.auth__links a {
  color: var(--tea-ink);
  border-bottom: 1px solid var(--tea-gold);
  padding-bottom: 1px;
}
</style>
