<template>
  <div v-loading="loading">
    <p class="muted">图表数据来自订单统计接口。没有订单的日期会显示为 0，不会填入虚构销售额。</p>
    <el-card header="近 14 日趋势"><div ref="chartRef" class="chart"></div></el-card>
    <el-card header="热销商品" style="margin-top: 12px">
      <el-table :data="hot">
        <el-table-column prop="name" label="商品" />
        <el-table-column label="价格"><template #default="{ row }">{{ formatPrice(row.price) }}</template></el-table-column>
        <el-table-column prop="sales" label="销量" />
        <el-table-column prop="stock" label="库存" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { adminApi } from '../../api'
import { formatPrice } from '../../utils/format'

const chartRef = ref()
const hot = ref([])
const loading = ref(false)
let chart
onMounted(async () => {
  loading.value = true
  try {
    const [trend, overview] = await Promise.all([adminApi.trend(14), adminApi.overview()])
    hot.value = overview.hotProducts || []
    await nextTick()
    chart = echarts.init(chartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: trend.map((item) => item.statDate) },
      yAxis: { type: 'value' },
      series: [{ type: 'line', smooth: true, data: trend.map((item) => Number(item.salesAmount)), name: '销售额' }]
    })
  } finally { loading.value = false }
})
onBeforeUnmount(() => chart?.dispose())
</script>

<style scoped>
.chart { height: 360px; }
</style>
