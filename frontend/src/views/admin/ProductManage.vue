<template>
  <div>
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="openForm()">新增商品</el-button>
    </div>

    <div class="filter-bar">
      <el-select v-model="query.categoryId" clearable placeholder="按分类筛选" @change="loadData" style="width:150px">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索商品名" clearable @keyup.enter="loadData" style="width:220px" />
    </div>

    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="商品名" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
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

    <div class="pagination-wrap">
      <el-pagination v-if="total > query.size" :total="total" :page-size="query.size"
        v-model:current-page="query.page" @current-change="loadData" layout="prev, pager, next" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="620px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="商品名称" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="商品描述" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" /></el-form-item>

        <el-divider content-position="left">规格配置</el-divider>
        <div v-for="(group, type) in specGroups" :key="type" class="spec-section">
          <div class="spec-type-title">{{ specLabels[type] || type }}</div>
          <div v-for="(spec, i) in group" :key="i" class="spec-row">
            <el-input v-model="spec.specName" placeholder="规格名" style="width:150px" />
            <el-input-number v-model="spec.priceDiff" :min="0" :precision="2" placeholder="加价" size="small" />
            <el-button link type="danger" @click="removeSpec(type, i)">删除</el-button>
          </div>
          <el-button size="small" @click="addSpec(type)">+ 添加{{ specLabels[type] || type }}</el-button>
        </div>
        <div class="spec-actions">
          <el-button size="small" @click="addSpecGroup('cup_size')">+ 杯型</el-button>
          <el-button size="small" @click="addSpecGroup('sweetness')">+ 糖度</el-button>
          <el-button size="small" @click="addSpecGroup('ice')">+ 冰度</el-button>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminGetProducts, adminCreateProduct, adminUpdateProduct, adminDeleteProduct, adminGetCategories, adminGetProduct } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const total = ref(0)
const categories = ref([])
const dialogVisible = ref(false)
const query = reactive({ categoryId: null, keyword: '', page: 1, size: 10 })
const form = ref({ categoryId: null, name: '', price: 0, description: '', status: 1, specs: [] })
const specGroups = ref({})
const specLabels = { cup_size: '杯型', sweetness: '糖度', ice: '冰度' }

onMounted(async () => { categories.value = (await adminGetCategories()).data; loadData() })

async function loadData() {
  const res = await adminGetProducts(query)
  list.value = res.data.records; total.value = res.data.total
}

async function openForm(row) {
  if (row) {
    const p = (await adminGetProduct(row.id)).data
    form.value = { id: p.id, categoryId: p.categoryId, name: p.name, price: p.price, description: p.description, status: p.status }
    specGroups.value = p.specs || {}
  } else {
    form.value = { categoryId: categories.value[0]?.id, name: '', price: 0, description: '', status: 1 }
    specGroups.value = {}
  }
  dialogVisible.value = true
}

function addSpecGroup(type) { if (!specGroups.value[type]) specGroups.value[type] = []; specGroups.value[type].push({ specType: type, specName: '', priceDiff: 0 }) }
function addSpec(type) { specGroups.value[type].push({ specType: type, specName: '', priceDiff: 0 }) }
function removeSpec(type, i) { specGroups.value[type].splice(i, 1); if (!specGroups.value[type].length) delete specGroups.value[type] }

async function handleSave() {
  const specs = []
  for (const group of Object.values(specGroups.value)) specs.push(...group.filter(s => s.specName))
  form.value.id ? await adminUpdateProduct(form.value.id, { ...form.value, specs }) : await adminCreateProduct({ ...form.value, specs })
  ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
  await adminDeleteProduct(id); ElMessage.success('已删除'); loadData()
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--text-primary); }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.spec-section { margin-bottom: 16px; }
.spec-type-title { font-weight: 600; font-size: 14px; margin-bottom: 8px; color: var(--text-primary); }
.spec-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.spec-actions { display: flex; gap: 8px; margin-top: 12px; }
</style>
