<template>
  <div>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="用户名、邮箱或昵称" @keyup.enter="load(1)" />
      <el-select v-model="status" clearable placeholder="状态" style="width: 120px"><el-option label="启用" :value="1" /><el-option label="禁用" :value="0" /></el-select>
      <el-button type="primary" @click="load(1)">搜索</el-button>
    </div>
    <el-table :data="records" v-loading="loading">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="90" />
      <el-table-column label="状态" width="90"><template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template></el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link :type="row.status === 1 ? 'danger' : 'primary'" @click="toggle(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
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

const records = ref([])
const keyword = ref('')
const status = ref()
const page = ref(1)
const total = ref(0)
const loading = ref(false)

async function load(next = 1) {
  page.value = next
  loading.value = true
  try {
    const data = await adminApi.users({ keyword: keyword.value || undefined, status: status.value, page: page.value, size: 10 })
    records.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
async function toggle(row) {
  const next = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确定${next === 0 ? '禁用' : '启用'}用户 ${row.username} 吗？`, '用户状态')
  await adminApi.userStatus(row.id, next)
  load(page.value)
}
onMounted(() => load(1))
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; }
</style>
