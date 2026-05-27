<template>
  <div>
    <h2>订单管理</h2>

    <el-tabs v-model="query.status" @tab-change="loadData">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待支付" name="0" />
      <el-tab-pane label="已支付" name="1" />
      <el-tab-pane label="制作中" name="2" />
      <el-tab-pane label="已完成" name="3" />
      <el-tab-pane label="已取消" name="4" />
    </el-tabs>

    <el-table :data="list" border>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="totalAmount" label="金额" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-select v-if="nextStatuses[row.status]?.length" v-model="row._newStatus" size="small"
            placeholder="变更状态" style="width:120px" @change="(val) => handleChangeStatus(row.id, val)">
            <el-option v-for="s in nextStatuses[row.status]" :key="s" :label="statusText(s)" :value="s" />
          </el-select>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="total > query.size" :total="total" :page-size="query.size"
      v-model:current-page="query.page" @current-change="loadData" style="margin-top:12px" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminGetOrders, adminUpdateOrderStatus } from '@/api'
import { ElMessage } from 'element-plus'

const list = ref([])
const total = ref(0)
const query = reactive({ status: '', page: 1, size: 10 })

const statusMap = { 0: '待支付', 1: '已支付', 2: '制作中', 3: '已完成', 4: '已取消' }
const nextStatuses = { 1: [2], 2: [3] }

function statusText(s) { return statusMap[s] || '未知' }
function statusType(s) {
  return { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger' }[s] || ''
}

onMounted(loadData)

async function loadData() {
  const params = { page: query.page, size: query.size }
  if (query.status) params.status = query.status
  const res = await adminGetOrders(params)
  list.value = res.data.records.map(r => ({ ...r, _newStatus: null }))
  total.value = res.data.total
}

async function handleChangeStatus(orderId, newStatus) {
  await adminUpdateOrderStatus(orderId, newStatus)
  ElMessage.success('状态已更新')
  loadData()
}
</script>
