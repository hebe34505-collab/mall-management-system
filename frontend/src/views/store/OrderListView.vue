<template>
  <div class="page">
    <h1>我的订单</h1>
    <el-radio-group v-model="status" @change="load(1)">
      <el-radio-button value="">全部</el-radio-button>
      <el-radio-button value="PENDING_SHIPMENT">待发货</el-radio-button>
      <el-radio-button value="SHIPPED">已发货</el-radio-button>
      <el-radio-button value="COMPLETED">已完成</el-radio-button>
      <el-radio-button value="CANCELLED">已取消</el-radio-button>
    </el-radio-group>
    <div v-loading="loading" class="state-block">
      <el-alert v-if="error" :title="error" type="error" show-icon />
      <el-empty v-else-if="!records.length" description="还没有订单" />
      <el-table v-else :data="records">
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column label="金额" width="120"><template #default="{ row }">{{ formatPrice(row.totalAmount) }}</template></el-table-column>
        <el-table-column label="状态" width="120"><template #default="{ row }"><el-tag :type="orderStatusType[row.status]">{{ row.statusLabel }}</el-tag></template></el-table-column>
        <el-table-column label="时间" min-width="160"><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></el-table-column>
        <el-table-column label="操作" width="100"><template #default="{ row }"><RouterLink :to="`/orders/${row.id}`">详情</RouterLink></template></el-table-column>
      </el-table>
    </div>
    <el-pagination v-if="total" layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { orderApi } from '../../api'
import { formatDate, formatPrice, orderStatusType } from '../../utils/format'

const records = ref([])
const status = ref('')
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const error = ref('')

async function load(next = 1) {
  page.value = next
  loading.value = true
  error.value = ''
  try {
    const data = await orderApi.list({ status: status.value || undefined, page: page.value, size: 10 })
    records.value = data.records
    total.value = data.total
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
onMounted(() => load(1))
</script>
