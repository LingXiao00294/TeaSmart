<template>
  <div>
    <div style="display:flex;justify-content:space-between;margin-bottom:16px">
      <h2>轮播图管理</h2>
      <el-button type="primary" @click="openForm()">新增轮播图</el-button>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="图片" width="150">
        <template #default="{ row }">
          <el-image :src="row.image" style="width:120px;height:60px" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="link" label="链接" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openForm(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑轮播图' : '新增轮播图'" width="500px">
      <el-form :model="form" label-width="60px">
        <el-form-item label="图片">
          <el-upload :http-request="handleUpload" :show-file-list="false" accept="image/*">
            <el-image v-if="form.image" :src="form.image" style="width:200px;height:100px" fit="cover" />
            <el-button v-else size="small">上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="链接"><el-input v-model="form.link" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
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
import { adminGetBanners, adminCreateBanner, adminUpdateBanner, adminDeleteBanner, adminUpload } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const dialogVisible = ref(false)
const form = ref({ image: '', link: '', sortOrder: 0, status: 1 })

onMounted(loadData)

async function loadData() {
  const res = await adminGetBanners()
  list.value = res.data
}

function openForm(row) {
  form.value = row ? { ...row } : { image: '', link: '', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

async function handleUpload({ file }) {
  const res = await adminUpload(file)
  form.value.image = res.data
  ElMessage.success('上传成功')
}

async function handleSave() {
  if (form.value.id) {
    await adminUpdateBanner(form.value.id, form.value)
  } else {
    await adminCreateBanner(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除？', '提示')
  await adminDeleteBanner(id)
  ElMessage.success('已删除')
  loadData()
}
</script>
