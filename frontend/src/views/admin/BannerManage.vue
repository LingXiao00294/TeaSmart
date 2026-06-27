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
          <el-upload
            class="banner-upload"
            :http-request="handleUpload"
            :before-upload="beforeUpload"
            :show-file-list="false"
            accept=".jpg,.jpeg,.png,.gif,.webp,image/jpeg,image/png,image/gif,image/webp"
          >
            <div class="upload-box" :class="{ 'is-uploading': uploading }">
              <el-image
                v-if="form.image"
                :key="form.image"
                :src="form.image"
                :preview-disabled="true"
                class="upload-box__img"
                fit="cover"
              />
              <div v-else class="upload-placeholder">
                <el-icon :size="28"><UploadFilled /></el-icon>
                <span>点击上传图片</span>
              </div>
              <div v-if="form.image && !uploading" class="upload-box__mask">点击替换</div>
              <div v-if="uploading" class="upload-box__loading">上传中…</div>
            </div>
          </el-upload>
          <p class="upload-tip">支持 JPG / PNG / GIF / WEBP，单张不超过 5MB</p>
        </el-form-item>
        <el-form-item label="链接"><el-input v-model="form.link" placeholder="跳转链接" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminGetBanners, adminCreateBanner, adminUpdateBanner, adminDeleteBanner, adminUpload } from '@/api'
import { validateImageFile } from '@/utils/upload'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const list = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const uploading = ref(false)
const form = ref({ image: '', link: '', sortOrder: 0, status: 1 })

onMounted(loadData)
async function loadData() { list.value = (await adminGetBanners()).data }

function openForm(row) {
  form.value = row
    ? { id: row.id, image: row.image, link: row.link ?? '', sortOrder: row.sortOrder ?? 0, status: row.status ?? 1 }
    : { image: '', link: '', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

function toPayload() {
  return {
    image: form.value.image,
    link: form.value.link,
    sortOrder: form.value.sortOrder,
    status: form.value.status,
  }
}

function beforeUpload(file) {
  const err = validateImageFile(file)
  if (err) {
    ElMessage.error(err)
    return false
  }
  return true
}

async function handleUpload({ file }) {
  if (uploading.value) return
  uploading.value = true
  try {
    form.value.image = (await adminUpload(file)).data
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error(e.message || '上传失败，请稍后重试')
  } finally {
    uploading.value = false
  }
}

async function handleSave() {
  if (!form.value.image) {
    ElMessage.warning('请先上传图片')
    return
  }
  saving.value = true
  try {
    const payload = toPayload()
    form.value.id
      ? await adminUpdateBanner(form.value.id, payload)
      : await adminCreateBanner(payload)
    ElMessage.success('轮播已保存')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.message || '保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该轮播图？', '提示', { type: 'warning' })
  await adminDeleteBanner(id)
  ElMessage.success('轮播已移除')
  loadData()
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--text-primary); }
.banner-upload :deep(.el-upload) { display: block; }
.upload-box {
  position: relative;
  width: 200px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}
.upload-box__img {
  width: 200px;
  height: 100px;
  display: block;
}
.upload-box__mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 13px;
  opacity: 0;
  transition: opacity 0.2s;
}
.upload-box:hover .upload-box__mask { opacity: 1; }
.upload-box.is-uploading { pointer-events: none; }
.upload-box__loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.85);
  color: var(--text-primary);
  font-size: 13px;
}
.upload-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--tea-text-3, #909399);
  line-height: 1.5;
}
.upload-placeholder {
  width: 200px; height: 100px; border: 2px dashed var(--border-color); border-radius: 8px;
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px;
  color: #909399; cursor: pointer; transition: all 0.2s;
}
.upload-placeholder:hover { border-color: var(--primary-color); color: var(--primary-color); }
</style>
