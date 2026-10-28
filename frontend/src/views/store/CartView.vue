<template>
  <div class="page" v-loading="loading">
    <div class="section-head"><h1>购物车</h1><el-button v-if="items.length" type="danger" plain @click="clear">清空购物车</el-button></div>
    <el-alert v-if="error" :title="error" type="error" show-icon />
    <el-empty v-else-if="!loading && !items.length" description="购物车还是空的">
      <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
    </el-empty>
    <template v-else>
      <el-table :data="items" @selection-change="onSelect">
        <el-table-column type="selection" width="48" :selectable="(row) => row.purchasable" />
        <el-table-column label="商品">
          <template #default="{ row }">
            <div class="name">
              <img :src="productImage(row.coverImage)" :alt="row.productName" />
              <span>
                {{ row.productName }}
                <small v-if="!row.purchasable">暂时不能购买</small>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120"><template #default="{ row }">{{ formatPrice(row.price) }}</template></el-table-column>
        <el-table-column label="数量" width="160">
          <template #default="{ row }">
            <el-input-number :model-value="row.quantity" :min="1" :max="Math.max(row.quantity, Math.min(row.stock || row.quantity, 99))" :disabled="!row.purchasable" @change="(value) => changeQty(row, value)" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120"><template #default="{ row }">{{ formatPrice(row.subtotal) }}</template></el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ row }"><el-button link type="danger" @click="remove(row)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <div class="settle">
        <span>已选 {{ selected.length }} 件，合计 <strong class="price">{{ formatPrice(total) }}</strong></span>
        <el-button type="primary" :disabled="!selected.length" @click="checkout">去结算</el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { cartApi } from '../../api'
import { formatPrice, productImage } from '../../utils/format'

const router = useRouter()
const items = ref([])
const selected = ref([])
const loading = ref(false)
const error = ref('')
const total = computed(() => selected.value.reduce((sum, item) => sum + Number(item.subtotal || 0), 0))

async function load() {
  loading.value = true
  error.value = ''
  try { items.value = await cartApi.list() } catch (err) { error.value = err.message } finally { loading.value = false }
}
function onSelect(rows) { selected.value = rows }
async function changeQty(row, value) {
  if (!value || value === row.quantity) return
  items.value = await cartApi.update(row.id, { quantity: value })
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除「${row.productName}」吗？`, '删除商品')
  items.value = await cartApi.remove(row.id)
}
async function clear() {
  await ElMessageBox.confirm('确定清空购物车吗？', '清空购物车')
  await cartApi.clear()
  items.value = []
  selected.value = []
}
function checkout() {
  router.push({ path: '/checkout', query: { cart: selected.value.map((item) => item.id).join(',') } })
}
onMounted(load)
</script>

<style scoped>
.name { display: flex; gap: 14px; align-items: center; }
.name img { width: 88px; height: 88px; object-fit: cover; background: #f3efe8; }
.name span { display: flex; flex-direction: column; gap: 4px; }
.name small { color: var(--danger); font-size: 12px; }
.settle { display: flex; justify-content: space-between; gap: 16px; align-items: center; margin-top: 18px; padding: 16px 0; border-top: 1px solid var(--line); }
h1 { margin: 0; }
@media (max-width: 768px) {
  .settle { flex-direction: column; align-items: stretch; }
  .name img { width: 72px; height: 72px; }
}
</style>
