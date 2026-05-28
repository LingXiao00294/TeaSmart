<template>
  <div>
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="phone" label="手机号">
        <template #default="{ row }">{{ row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : ''" effect="plain" size="small">
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="180" />
    </el-table>

    <div class="pagination-wrap">
      <el-pagination v-if="total > query.size" :total="total" :page-size="query.size"
        v-model:current-page="query.page" @current-change="loadData" layout="prev, pager, next" />
    </div>
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

<style scoped>
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--text-primary); }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
