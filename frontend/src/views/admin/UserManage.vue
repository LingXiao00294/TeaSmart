<template>
  <div>
    <h2>用户管理</h2>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column prop="createdAt" label="注册时间" width="180" />
    </el-table>
    <el-pagination v-if="total > query.size" :total="total" :page-size="query.size"
      v-model:current-page="query.page" @current-change="loadData" style="margin-top:12px" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

onMounted(loadData)

async function loadData() {
  const res = await request.get('/admin/users', { params: query })
  list.value = res.data.records || res.data
  total.value = res.data.total || list.value.length
}
</script>
