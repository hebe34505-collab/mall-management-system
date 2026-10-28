<template>
  <div>
    <div class="toolbar">
      <el-input v-model="orderNo" placeholder="订单号" @keyup.enter="load(1)" />
      <el-input v-model="username" placeholder="用户名" @keyup.enter="load(1)" />
      <el-select v-model="status" clearable placeholder="状态" style="width: 150px">
        <el-option label="待发货" value="PENDING_SHIPMENT" /><el-option label="已发货" value="SHIPPED" />
        <el-option label="已完成" value="COMPLETED" /><el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="load(1)">搜索</el-button>
    </div>
    <el-table :data="records" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" min-width="170" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column label="金额" width="110"><template #default="{ row }">{{ formatPrice(row.totalAmount) }}</template></el-table-column>
      <el-table-column prop="statusLabel" label="状态" width="100" />
      <el-table-column label="时间" width="160"><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link @click="open(row)">详情</el-button>
          <el-button v-if="row.status === 'PENDING_SHIPMENT'" link @click="update(row, 'SHIPPED')">发货</el-button>
          <el-button v-if="row.status === 'SHIPPED'" link @click="update(row, 'COMPLETED')">完成</el-button>
          <el-button v-if="row.status === 'PENDING_SHIPMENT'" link type="danger" @click="update(row, 'CANCELLED')">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination layout="prev, pager, next" :total="total" :page-size="10" :current-page="page" @current-change="load" />
    <el-drawer v-model="visible" title="订单详情" size="480px">
      <template v-if="detail">
        <p>{{ detail.receiver }} {{ detail.phone }}</p>
        <p>{{ detail.fullAddress }}</p>
        <el-table :data="detail.items"><el-table-column prop="productName" label="商品" /><el-table-column prop="quantity" label="数量" width="70" /><el-table-column label="小计" width="100"><template #default="{ row }">{{ formatPrice(row.subtotal) }}</template></el-table-column></el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { adminApi } from '../../api'
import { formatDate, formatPrice } from '../../utils/format'

const records = ref([])
const orderNo = ref('')
const username = ref('')
const status = ref('')
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const visible = ref(false)
const detail = ref(null)

async function load(next = 1) {
  page.value = next
  loading.value = true
  try {
    const data = await adminApi.orders({ orderNo: orderNo.value || undefined, username: username.value || undefined, status: status.value || undefined, page: page.value, size: 10 })
    records.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
async function open(row) { detail.value = await adminApi.order(row.id); visible.value = true }
async function update(row, next) {
  await ElMessageBox.confirm(`确定把订单 ${row.orderNo} 更新为新的状态吗？`, '更新订单')
  await adminApi.orderStatus(row.id, next)
  load(page.value)
}
onMounted(() => load(1))
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
</style>
