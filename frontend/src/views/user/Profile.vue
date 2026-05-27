<template>
  <div class="profile-page">
    <div class="top-bar">
      <el-page-header @back="$router.push('/')" content="个人中心" />
    </div>

    <el-card class="profile-card">
      <div class="avatar">{{ user?.username?.[0] || '?' }}</div>
      <div class="username">{{ user?.username }}</div>
      <div class="phone">{{ user?.phone || '未绑定手机' }}</div>
      <div class="role">角色：{{ user?.role === 'ADMIN' ? '管理员' : '普通用户' }}</div>

      <div v-if="user?.role === 'ADMIN'" class="admin-link">
        <el-button type="primary" @click="$router.push('/admin')">进入管理后台</el-button>
      </div>

      <el-button type="danger" plain style="width:100%;margin-top:20px" @click="handleLogout">退出登录</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.userInfo)

async function handleLogout() {
  await ElMessageBox.confirm('确定退出登录？', '提示')
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.profile-page { min-height: 100vh; background: #f5f5f5; }
.top-bar { padding: 10px 16px; background: #fff; border-bottom: 1px solid #eee; }
.profile-card { margin: 20px 16px; text-align: center; padding: 30px 20px; }
.avatar {
  width: 80px; height: 80px; border-radius: 50%; margin: 0 auto 16px;
  background: linear-gradient(135deg, #667eea, #764ba2); color: #fff;
  display: flex; align-items: center; justify-content: center; font-size: 32px;
}
.username { font-size: 20px; font-weight: bold; margin-bottom: 6px; }
.phone { color: #999; margin-bottom: 4px; }
.role { color: #666; font-size: 13px; }
.admin-link { margin-top: 20px; }
</style>
