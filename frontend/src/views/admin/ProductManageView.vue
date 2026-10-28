<template>
  <div>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="商品名称" clearable @keyup.enter="load(1)" />
      <el-select v-model="status" clearable placeholder="状态" style="width: 140px" @change="load(1)">
        <el-option label="上架" value="ON_SHELF" /><el-option label="下架" value="OFF_SHELF" />
      </el-select>
      <el-button type="primary" @click="load(1)">搜索</el-button>
      <el-button @click="$router.push('/admin/products/new')">新增商品</el-button>
    </div>
    <el-table v-loading="loading" :data="records">
      <el-table-column prop="name" label="商品" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column label="价格" width="100"><template #default="{ row }">{{ formatPrice(row.price) }}</template></el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="statusLabel" label="状态" width="80" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link @click="$router.push(`/admin/products/${row.id}/edit`)">编辑</el-button>
          <el-button link @click="toggle(row)">{{ row.status === 'ON_SHELF' ? '下架' : '上架' }}</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { adminApi } from '../../api'
import { formatPrice } from '../../utils/format'

const records = ref([])
const keyword = ref('')
const status = ref('')
const page = ref(1)
const total = ref(0)
const loading = ref(false)

async function load(next = 1) {
  page.value = next
  loading.value = true
  try {
    const data = await adminApi.products({ keyword: keyword.value || undefined, status: status.value || undefined, page: page.value, size: 10 })
    records.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
async function toggle(row) {
  const next = row.status === 'ON_SHELF' ? 'OFF_SHELF' : 'ON_SHELF'
  await ElMessageBox.confirm(`确定${next === 'ON_SHELF' ? '上架' : '下架'}「${row.name}」吗？`, '修改状态')
  await adminApi.productStatus(row.id, next)
  load(page.value)
}
async function remove(row) {
  await ElMessageBox.confirm(`删除后不可恢复，确定删除「${row.name}」吗？`, '删除商品')
  await adminApi.removeProduct(row.id)
  load(1)
}
onMounted(() => load(1))
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
</style>
