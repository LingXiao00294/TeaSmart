<template>
  <div>
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openForm()">新增分类</el-button>
    </div>

    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="分类名" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="420px" destroy-on-close>
      <el-form :model="form" label-width="60px">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="请输入分类名称" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminGetCategories, adminCreateCategory, adminUpdateCategory, adminDeleteCategory } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const dialogVisible = ref(false)
const form = ref({ name: '', sortOrder: 0, status: 1 })

onMounted(loadData)
async function loadData() { list.value = (await adminGetCategories()).data }

function openForm(row) {
  form.value = row ? { ...row } : { name: '', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

async function handleSave() {
  form.value.id ? await adminUpdateCategory(form.value.id, form.value) : await adminCreateCategory(form.value)
  ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
  await adminDeleteCategory(id); ElMessage.success('已删除'); loadData()
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--text-primary); }
</style>
