<template>
  <div>
    <el-table :data="records" v-loading="loading">
      <el-table-column label="时间" width="170"><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></el-table-column>
      <el-table-column prop="username" label="操作人" width="120" />
      <el-table-column prop="module" label="模块" width="110" />
      <el-table-column prop="action" label="动作" width="110" />
      <el-table-column prop="detail" label="详情" />
      <el-table-column prop="ip" label="IP" width="130" />
    </el-table>
    <el-pagination layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminApi } from '../../api'
import { formatDate } from '../../utils/format'

const records = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)
async function load(next = 1) {
  page.value = next
  loading.value = true
  try {
    const data = await adminApi.logs({ page: page.value, size: 10 })
    records.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
onMounted(() => load(1))
</script>
