<template>
  <div class="page" v-loading="loading">
    <el-alert v-if="error" :title="error" type="error" show-icon />
    <article v-else-if="order" class="card">
      <div class="section-head">
        <h1>订单 {{ order.orderNo }}</h1>
        <el-tag :type="orderStatusType[order.status]">{{ order.statusLabel }}</el-tag>
      </div>
      <p>收货人：{{ order.receiver }} {{ order.phone }}</p>
      <p>地址：{{ order.fullAddress }}</p>
      <p>下单时间：{{ formatDate(order.createdAt) }}</p>
      <el-table :data="order.items">
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div class="goods"><img :src="productImage(row.productImage)" :alt="row.productName" /><span>{{ row.productName }}</span></div>
          </template>
        </el-table-column>
        <el-table-column label="单价"><template #default="{ row }">{{ formatPrice(row.unitPrice) }}</template></el-table-column>
        <el-table-column prop="quantity" label="数量" />
        <el-table-column label="小计"><template #default="{ row }">{{ formatPrice(row.subtotal) }}</template></el-table-column>
      </el-table>
      <p class="sum">合计 {{ formatPrice(order.totalAmount) }}</p>
      <el-button v-if="order.cancellable" type="danger" plain @click="cancel">取消订单</el-button>
    </article>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { orderApi } from '../../api'
import { formatDate, formatPrice, orderStatusType, productImage } from '../../utils/format'

const route = useRoute()
const order = ref(null)
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try { order.value = await orderApi.detail(route.params.id) } catch (err) { error.value = err.message } finally { loading.value = false }
}
async function cancel() {
  await ElMessageBox.confirm('取消后库存会退回，确定取消这笔待发货订单吗？', '取消订单')
  order.value = await orderApi.cancel(order.value.id)
}
onMounted(load)
</script>

<style scoped>
.card { background: var(--card); border: 1px solid var(--line); padding: 16px; }
.goods { display: flex; align-items: center; gap: 12px; }
.goods img { width: 64px; height: 64px; object-fit: cover; background: #f3efe8; }
.sum { text-align: right; font-size: 18px; }
</style>
