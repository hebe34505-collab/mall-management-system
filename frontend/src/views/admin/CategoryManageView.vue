<template>
  <div>
    <el-button type="primary" @click="open()">新增分类</el-button>
    <el-table :data="records" v-loading="loading" style="margin-top: 12px">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="productCount" label="商品数" width="90" />
      <el-table-column label="状态" width="90"><template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template></el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link @click="open(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" title="分类" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="排序" prop="sortOrder"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="enabled" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api'

const records = ref([])
const loading = ref(false)
const visible = ref(false)
const editingId = ref(null)
const enabled = ref(true)
const formRef = ref()
const form = reactive({ name: '', sortOrder: 0 })
const rules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

async function load() {
  loading.value = true
  try { records.value = await adminApi.categories() } finally { loading.value = false }
}
function open(row) {
  editingId.value = row?.id || null
  form.name = row?.name || ''
  form.sortOrder = row?.sortOrder || 0
  enabled.value = row ? row.status === 1 : true
  visible.value = true
}
async function save() {
  await formRef.value.validate()
  const payload = { ...form, status: enabled.value ? 1 : 0 }
  if (editingId.value) await adminApi.updateCategory(editingId.value, payload)
  else await adminApi.saveCategory(payload)
  visible.value = false
  ElMessage.success('分类已保存')
  load()
}
async function remove(row) {
  await ElMessageBox.confirm(`分类下如果还有商品将无法删除。确定删除「${row.name}」吗？`, '删除分类')
  await adminApi.removeCategory(row.id)
  load()
}
onMounted(load)
</script>
