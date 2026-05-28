<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">TeaSmart Admin</div>
      <el-menu
        :default-active="$route.path"
        router
        class="side-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/admin/dashboard">Dashboard</el-menu-item>
        <el-menu-item index="/admin/categories">分类管理</el-menu-item>
        <el-menu-item index="/admin/products">商品管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/banners">轮播图管理</el-menu-item>
        <el-menu-item index="/admin/knowledge">知识库管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="top-bar">
        <span>管理后台</span>
        <div class="user-info">
          <router-link to="/">返回前台</router-link>
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

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout { height: 100vh; }
.sidebar { background: #304156; transition: width 0.3s; }
.logo {
  color: #fff; font-size: 20px; font-weight: 600;
  padding: 20px 16px; text-align: center; letter-spacing: 1px;
}
.side-menu { border-right: none; }
.side-menu .el-menu-item.is-active { background-color: #409eff !important; }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}
.main-content { background: #f5f7fa; padding: 20px; }
.content-card {
  background: #fff; padding: 20px; border-radius: 8px;
  min-height: calc(100vh - 100px); box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.user-info { display: flex; align-items: center; gap: 15px; }
.top-bar a { color: #409eff; text-decoration: none; }
</style>
