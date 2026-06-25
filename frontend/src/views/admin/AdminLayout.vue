<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <div class="seal seal--md logo__seal">掌</div>
        <div class="logo__text">
          <span class="logo__cn font-heading">茶智</span>
          <span class="logo__en font-display">Admin</span>
        </div>
      </div>
      <hr class="gold-line logo__line" />
      <el-menu
        :default-active="$route.path"
        router
        class="side-menu"
        background-color="#1e3a28"
        text-color="#c7b896"
        active-text-color="#f7e9c6"
      >
        <el-menu-item index="/admin/dashboard"><el-icon><DataLine /></el-icon><span>营业概览</span></el-menu-item>
        <el-menu-item index="/admin/categories"><el-icon><Files /></el-icon><span>茶品分类</span></el-menu-item>
        <el-menu-item index="/admin/products"><el-icon><Goods /></el-icon><span>茶品管理</span></el-menu-item>
        <el-menu-item index="/admin/orders"><el-icon><Tickets /></el-icon><span>订单管理</span></el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User /></el-icon><span>茶客管理</span></el-menu-item>
        <el-menu-item index="/admin/banners"><el-icon><Picture /></el-icon><span>轮播图管理</span></el-menu-item>
        <el-menu-item index="/admin/knowledge"><el-icon><Reading /></el-icon><span>茶识管理</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="top-bar">
        <span class="top-bar__title font-heading">掌柜 · 后台</span>
        <div class="user-info">
          <router-link to="/" class="top-bar__link">返回 · 前厅</router-link>
          <el-button link @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <div class="content-card">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { DataLine, Files, Goods, Tickets, User, Picture, Reading } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
.sidebar {
  background: #1e3a28;
  transition: width 0.3s;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 18px 16px;
}
.logo__seal {
  transform: rotate(-6deg);
  background: #f7e9c6;
  color: var(--tea-vermilion);
  box-shadow: inset 0 0 0 1.5px var(--tea-vermilion);
}
.logo__text {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}
.logo__cn {
  color: #f5efe0;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 3px;
}
.logo__en {
  color: var(--tea-gold);
  font-size: 13px;
  letter-spacing: 1px;
}
.logo__line {
  margin: 0 18px;
}
.side-menu {
  border-right: none;
  padding-top: 6px;
}
.side-menu :deep(.el-menu-item) {
  margin: 4px 10px;
  border-radius: 8px;
  letter-spacing: 1px;
}
.side-menu :deep(.el-menu-item.is-active) {
  background-color: #2a4a32 !important;
  position: relative;
  font-weight: 600;
}
.side-menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background: var(--tea-vermilion);
  border-radius: 0 2px 2px 0;
}
.side-menu :deep(.el-menu-item:hover) {
  background-color: #244029 !important;
}
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--tea-paper-2);
  border-bottom: 1px solid var(--tea-line);
  padding: 0 20px;
}
.top-bar__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--tea-ink-text);
  letter-spacing: 3px;
}
.main-content {
  background: var(--tea-paper);
  padding: 20px;
}
.content-card {
  background: var(--tea-paper-2);
  padding: 22px;
  border: 1px solid var(--tea-line);
  border-radius: var(--radius-lg);
  min-height: calc(100vh - 100px);
  box-shadow: var(--shadow-card);
}
.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}
.top-bar__link {
  color: var(--tea-ink);
  font-size: 14px;
}
.top-bar :deep(.el-button) {
  color: var(--tea-vermilion);
}
</style>
