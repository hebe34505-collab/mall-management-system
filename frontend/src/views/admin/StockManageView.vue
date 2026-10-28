<template>
  <div>
    <el-input v-model="keyword" placeholder="搜索商品" style="width: 240px" @keyup.enter="load(1)" />
    <el-table :data="records" v-loading="loading" style="margin-top: 12px" :row-class-name="rowClass">
      <el-table-column prop="name" label="商品" />
      <el-table-column prop="stock" label="当前库存" width="120" />
      <el-table-column prop="sales" label="销量" width="90" />
      <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link @click="open(row)">调整</el-button></template></el-table-column>
    </el-table>
    <el-pagination layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" />
    <el-dialog v-model="visible" title="调整库存" width="420px">
      <p>{{ current?.name }}，当前库存 {{ current?.stock }}</p>
      <el-input-number v-model="stock" :min="0" />
      <template #footer><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../api'

const records = ref([])
const keyword = ref('')
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const visible = ref(false)
const current = ref(null)
const stock = ref(0)

async function load(next = 1) {
  page.value = next
  loading.value = true
  try {
    const data = await adminApi.products({ keyword: keyword.value || undefined, page: page.value, size: 10 })
    records.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
function open(row) { current.value = row; stock.value = row.stock; visible.value = true }
async function save() {
  await ElMessageBox.confirm(`把「${current.value.name}」库存调整为 ${stock.value}？`, '调整库存')
  await adminApi.adjustStock(current.value.id, stock.value)
  visible.value = false
  ElMessage.success('库存已更新')
  load(page.value)
}
function rowClass({ row }) { return row.stock <= 10 ? 'low' : '' }
onMounted(() => load(1))
</script>

<style>
.low { --el-table-tr-bg-color: #fff7ed; }
</style>
