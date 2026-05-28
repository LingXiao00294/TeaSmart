<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <div class="header">
        <h2>欢迎回来</h2>
        <p>登录以继续探索美味</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="links">
        <router-link to="/register">立即注册</router-link>
      </div>
    </el-card>
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
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--main-bg);
}
.login-card { width: 380px; padding: 30px; border-radius: 16px; border: none; }
.header { text-align: center; margin-bottom: 30px; }
.header h2 { margin: 0 0 10px; font-size: 24px; color: var(--text-primary); }
.header p { margin: 0; color: #909399; font-size: 14px; }
.login-btn { width: 100%; border-radius: 8px; font-size: 16px; height: 45px; }
.links { text-align: center; margin-top: 20px; font-size: 14px; }
.links a { color: var(--primary-color); text-decoration: none; }
</style>
