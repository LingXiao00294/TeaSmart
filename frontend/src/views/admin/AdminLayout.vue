<template>
  <el-container class="admin-layout">
    <el-aside width="200px" class="sidebar">
      <div class="logo">茶小智管理</div>
      <el-menu :default-active="$route.path" router class="side-menu">
        <el-menu-item index="/admin/dashboard">Dashboard</el-menu-item>
        <el-menu-item index="/admin/categories">分类管理</el-menu-item>
        <el-menu-item index="/admin/products">商品管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/banners">轮播图管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="top-bar">
        <span>管理后台</span>
        <div>
          <router-link to="/">返回前台</router-link>
          <el-button text @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
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
.sidebar { background: #304156; overflow-y: auto; }
.logo { color: #fff; font-size: 18px; font-weight: bold; padding: 16px; text-align: center; }
.side-menu { border-right: none; background: #304156; }
.side-menu .el-menu-item { color: #bfcbd9; }
.side-menu .el-menu-item.is-active { color: #409eff; background: #263445; }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; border-bottom: 1px solid #eee; box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.top-bar a { margin-right: 16px; color: #409eff; text-decoration: none; }
.main-content { background: #f0f2f5; }
</style>
