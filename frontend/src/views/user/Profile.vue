<template>
  <div class="profile-page">
    <div class="header-nav">
      <el-button link @click="$router.push('/')"><el-icon><ArrowLeft /></el-icon> 返回首页</el-button>
      <span class="page-title">个人中心</span>
    </div>

    <el-card class="profile-card" shadow="hover">
      <div class="user-main">
        <div class="avatar">{{ user?.username?.[0]?.toUpperCase() || '?' }}</div>
        <div class="info-group">
          <div class="username">{{ user?.username }}</div>
          <div class="role-tag">{{ user?.role === 'ADMIN' ? '管理员' : '普通用户' }}</div>
        </div>
      </div>

      <div class="info-list">
        <div class="info-item">
          <span class="label">手机号码</span>
          <span class="value">{{ user?.phone || '未设置' }}</span>
        </div>
      </div>

      <div class="actions">
        <el-button v-if="user?.role === 'ADMIN'" type="primary" class="action-btn" @click="$router.push('/admin')">进入管理后台</el-button>
        <el-button type="danger" plain class="action-btn" @click="handleLogout">退出登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

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
.profile-page { min-height: 100vh; background: var(--main-bg); padding: 20px; }
.header-nav { display: flex; align-items: center; margin-bottom: 20px; gap: 10px; }
.page-title { font-weight: 600; color: var(--text-primary); }
.profile-card { border-radius: 16px; border: none; padding: 20px; }
.user-main { display: flex; align-items: center; gap: 20px; margin-bottom: 30px; }
.avatar {
  width: 70px; height: 70px; border-radius: 16px;
  background: linear-gradient(135deg, #409eff, #764ba2); color: #fff;
  display: flex; align-items: center; justify-content: center; font-size: 28px;
}
.username { font-size: 20px; font-weight: 700; color: var(--text-primary); }
.role-tag { font-size: 12px; color: var(--primary-color); background: #ecf5ff; padding: 2px 8px; border-radius: 4px; }
.info-list { border-top: 1px solid #eee; padding-top: 20px; margin-bottom: 30px; }
.info-item { display: flex; justify-content: space-between; padding: 10px 0; }
.label { color: #909399; }
.value { color: var(--text-primary); font-weight: 500; }
.actions { display: flex; flex-direction: column; gap: 12px; }
.action-btn { width: 100%; border-radius: 8px; height: 45px; }
</style>
