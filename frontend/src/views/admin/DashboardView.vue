<template>
  <div v-loading="loading">
    <el-alert v-if="error" :title="error" type="error" show-icon />
    <template v-else>
      <el-row :gutter="12">
        <el-col :xs="12" :md="6" v-for="item in cards" :key="item.label">
          <el-card><p class="muted">{{ item.label }}</p><strong class="num">{{ item.value }}</strong></el-card>
        </el-col>
      </el-row>
      <p class="muted">{{ overview.salesRule }}</p>
      <el-row :gutter="12">
        <el-col :md="14"><el-card header="近 7 日销售"><div ref="chartRef" class="chart"></div></el-card></el-col>
        <el-col :md="10"><el-card header="热销商品">
          <el-table :data="overview.hotProducts || []" size="small">
            <el-table-column prop="name" label="商品" />
            <el-table-column prop="sales" label="销量" width="80" />
          </el-table>
        </el-card></el-col>
      </el-row>
      <el-card header="近期订单" style="margin-top: 12px">
        <el-table :data="overview.recentOrders || []">
          <el-table-column prop="orderNo" label="订单号" />
          <el-table-column prop="username" label="用户" width="120" />
          <el-table-column label="金额" width="120"><template #default="{ row }">{{ formatPrice(row.totalAmount) }}</template></el-table-column>
          <el-table-column prop="statusLabel" label="状态" width="100" />
          <el-table-column label="时间"><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></el-table-column>
        </el-table>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { adminApi } from '../../api'
import { formatDate, formatPrice } from '../../utils/format'

const overview = ref({})
const trend = ref([])
const loading = ref(false)
const error = ref('')
const chartRef = ref()
let chart

const cards = computed(() => [
  { label: '用户总数', value: overview.value.userCount ?? '-' },
  { label: '商品总数', value: overview.value.productCount ?? '-' },
  { label: '有效订单', value: overview.value.effectiveOrderCount ?? '-' },
  { label: '销售额', value: formatPrice(overview.value.salesAmount || 0) }
])

function render() {
  if (!chartRef.value) return
  chart ||= echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '销售额'] },
    xAxis: { type: 'category', data: trend.value.map((item) => item.statDate) },
    yAxis: [{ type: 'value' }, { type: 'value' }],
    series: [
      { name: '订单数', type: 'bar', data: trend.value.map((item) => item.orderCount) },
      { name: '销售额', type: 'line', yAxisIndex: 1, data: trend.value.map((item) => Number(item.salesAmount)) }
    ]
  })
}

onMounted(async () => {
  loading.value = true
  try {
    overview.value = await adminApi.overview()
    trend.value = await adminApi.trend(7)
    await nextTick()
    render()
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
})
onBeforeUnmount(() => chart?.dispose())
</script>

<style scoped>
.num { font-size: 28px; font-family: var(--display); }
.chart { height: 320px; }
</style>
