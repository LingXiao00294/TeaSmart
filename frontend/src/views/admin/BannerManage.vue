<template>
  <div>
    <div class="page-header">
      <h2>轮播图管理</h2>
      <el-button type="primary" @click="openForm()">新增轮播图</el-button>
    </div>

    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="图片" width="160">
        <template #default="{ row }">
          <el-image :src="row.image" style="width:120px;height:60px;border-radius:6px" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="link" label="链接" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="80" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑轮播图' : '新增轮播图'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="60px">
        <el-form-item label="图片">
          <el-upload :http-request="handleUpload" :show-file-list="false" accept="image/*">
            <el-image v-if="form.image" :src="form.image" style="width:200px;height:100px;border-radius:8px" fit="cover" />
            <div v-else class="upload-placeholder">
              <el-icon :size="28"><UploadFilled /></el-icon>
              <span>点击上传图片</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="链接"><el-input v-model="form.link" placeholder="跳转链接" /></el-form-item>
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
import { adminGetBanners, adminCreateBanner, adminUpdateBanner, adminDeleteBanner, adminUpload } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const list = ref([])
const dialogVisible = ref(false)
const form = ref({ image: '', link: '', sortOrder: 0, status: 1 })

onMounted(loadData)
async function loadData() { list.value = (await adminGetBanners()).data }

function openForm(row) {
  form.value = row ? { ...row } : { image: '', link: '', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

async function handleUpload({ file }) {
  form.value.image = (await adminUpload(file)).data
  ElMessage.success('上传成功')
}

async function handleSave() {
  form.value.id ? await adminUpdateBanner(form.value.id, form.value) : await adminCreateBanner(form.value)
  ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该轮播图？', '提示', { type: 'warning' })
  await adminDeleteBanner(id); ElMessage.success('已删除'); loadData()
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--text-primary); }
.upload-placeholder {
  width: 200px; height: 100px; border: 2px dashed var(--border-color); border-radius: 8px;
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px;
  color: #909399; cursor: pointer; transition: all 0.2s;
}
.upload-placeholder:hover { border-color: var(--primary-color); color: var(--primary-color); }
</style>
