<template>
  <div>
    <div style="display:flex;justify-content:space-between;margin-bottom:16px">
      <h2>商品管理</h2>
      <el-button type="primary" @click="openForm()">新增商品</el-button>
    </div>

    <div style="display:flex;gap:12px;margin-bottom:12px">
      <el-select v-model="query.categoryId" clearable placeholder="按分类筛选" @change="loadData">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索商品名" clearable @keyup.enter="loadData" style="width:200px" />
    </div>

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="商品名" />
      <el-table-column prop="price" label="价格" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openForm(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="total > query.size" :total="total" :page-size="query.size"
      v-model:current-page="query.page" @current-change="loadData" style="margin-top:12px" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="form.categoryId">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>

        <el-divider>规格</el-divider>
        <div v-for="(group, type) in specGroups" :key="type" style="margin-bottom:12px">
          <div style="font-weight:bold;margin-bottom:6px">{{ specLabels[type] || type }}</div>
          <div v-for="(spec, i) in group" :key="i" style="display:flex;gap:8px;margin-bottom:6px">
            <el-input v-model="spec.specName" placeholder="规格名" style="width:150px" />
            <el-input-number v-model="spec.priceDiff" :min="0" :precision="2" placeholder="加价" size="small" />
            <el-button size="small" type="danger" text @click="removeSpec(type, i)">删除</el-button>
          </div>
          <el-button size="small" @click="addSpec(type)">+ 添加{{ specLabels[type] || type }}</el-button>
        </div>
        <div style="display:flex;gap:8px;margin-top:8px">
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

onMounted(async () => {
  const c = await adminGetCategories()
  categories.value = c.data
  loadData()
})

async function loadData() {
  const res = await adminGetProducts(query)
  list.value = res.data.records
  total.value = res.data.total
}

async function openForm(row) {
  if (row) {
    const res = await adminGetProduct(row.id)
    const p = res.data
    form.value = { id: p.id, categoryId: p.categoryId, name: p.name, price: p.price, description: p.description, status: p.status }
    specGroups.value = p.specs || {}
  } else {
    form.value = { categoryId: categories.value[0]?.id, name: '', price: 0, description: '', status: 1 }
    specGroups.value = {}
  }
  dialogVisible.value = true
}

function addSpecGroup(type) {
  if (!specGroups.value[type]) specGroups.value[type] = []
  specGroups.value[type].push({ specType: type, specName: '', priceDiff: 0 })
}

function addSpec(type) {
  specGroups.value[type].push({ specType: type, specName: '', priceDiff: 0 })
}

function removeSpec(type, i) {
  specGroups.value[type].splice(i, 1)
  if (!specGroups.value[type].length) delete specGroups.value[type]
}

async function handleSave() {
  const specs = []
  for (const group of Object.values(specGroups.value)) {
    specs.push(...group.filter(s => s.specName))
  }
  await (form.value.id
    ? adminUpdateProduct(form.value.id, { ...form.value, specs })
    : adminCreateProduct({ ...form.value, specs }))
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除？', '提示')
  await adminDeleteProduct(id)
  ElMessage.success('已删除')
  loadData()
}
</script>
