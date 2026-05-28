<template>
  <div class="register-container">
    <el-card class="register-card" shadow="hover">
      <div class="header">
        <h2>创建账户</h2>
        <p>加入 TeaSmart 开启你的茶艺之旅</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleRegister" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号（选填）" :prefix-icon="Phone" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="reg-btn" :loading="loading" @click="handleRegister">注册</el-button>
        </el-form-item>
      </el-form>
      <div class="links">
        <router-link to="/login">已有账号？返回登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '', phone: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' },
             { min: 2, max: 50, message: '长度2-50', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' },
             { min: 6, message: '密码至少6位', trigger: 'blur' }],
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await request.post('/auth/register', form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--main-bg);
}
.register-card { width: 380px; padding: 30px; border-radius: 16px; border: none; }
.header { text-align: center; margin-bottom: 30px; }
.header h2 { margin: 0 0 10px; font-size: 24px; color: var(--text-primary); }
.header p { margin: 0; color: #909399; font-size: 14px; }
.reg-btn { width: 100%; border-radius: 8px; font-size: 16px; height: 45px; }
.links { text-align: center; margin-top: 20px; font-size: 14px; }
.links a { color: var(--primary-color); text-decoration: none; }
</style>
