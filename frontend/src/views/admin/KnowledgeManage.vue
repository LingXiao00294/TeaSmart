<template>
  <div>
    <div class="page-header">
      <h2>知识库管理</h2>
      <el-button type="primary" @click="openForm()">新增条目</el-button>
    </div>

    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" width="160" />
      <el-table-column prop="category" label="分类" width="100">
        <template #default="{ row }">
          <el-tag effect="plain" size="small">{{ row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="70" />
      <el-table-column label="状态" width="80">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑知识条目' : '新增知识条目'" width="560px" destroy-on-close>
      <el-form :model="form" label-width="60px">
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="如：品牌介绍、营业时间" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" allow-create filterable style="width:100%">
            <el-option label="店铺简介" value="店铺简介" />
            <el-option label="FAQ" value="FAQ" />
            <el-option label="促销活动" value="促销活动" />
            <el-option label="自定义" value="自定义" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" placeholder="AI 会引用此内容回答用户问题" /></el-form-item>
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
import { adminGetKnowledge, adminCreateKnowledge, adminUpdateKnowledge, adminDeleteKnowledge } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const dialogVisible = ref(false)
const form = ref({ title: '', content: '', category: '自定义', sortOrder: 0, status: 1 })

onMounted(loadData)
async function loadData() { list.value = (await adminGetKnowledge()).data }

function openForm(row) {
  form.value = row ? { ...row } : { title: '', content: '', category: '自定义', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

async function handleSave() {
  form.value.id
    ? await adminUpdateKnowledge(form.value.id, form.value)
    : await adminCreateKnowledge(form.value)
  ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该知识条目？', '提示', { type: 'warning' })
  await adminDeleteKnowledge(id); ElMessage.success('已删除'); loadData()
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--text-primary); }
</style>
