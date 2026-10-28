<template>
  <div class="page" v-loading="loading">
    <h1>确认订单</h1>
    <el-alert v-if="error" :title="error" type="error" show-icon />
    <section>
      <h2>收货地址</h2>
      <el-empty v-if="!addresses.length" description="还没有收货地址">
        <el-button type="primary" @click="$router.push('/profile')">去添加</el-button>
      </el-empty>
      <el-radio-group v-else v-model="addressId" class="addresses">
        <el-radio v-for="item in addresses" :key="item.id" :value="item.id" border>
          {{ item.receiver }} {{ item.phone }} {{ item.fullAddress }}
        </el-radio>
      </el-radio-group>
    </section>
    <section>
      <h2>商品</h2>
      <el-table :data="lines">
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div class="goods"><img v-if="row.image" :src="productImage(row.image)" :alt="row.name" /><span>{{ row.name }}</span></div>
          </template>
        </el-table-column>
        <el-table-column label="单价"><template #default="{ row }">{{ formatPrice(row.price) }}</template></el-table-column>
        <el-table-column prop="quantity" label="数量" />
        <el-table-column label="小计"><template #default="{ row }">{{ formatPrice(row.price * row.quantity) }}</template></el-table-column>
      </el-table>
      <p class="sum">预计合计 {{ formatPrice(previewTotal) }}。提交时以后端按数据库价格重新计算的金额为准。</p>
      <el-input v-model="remark" maxlength="200" placeholder="备注，选填" />
      <el-button type="primary" :loading="submitting" :disabled="!addressId || !lines.length" @click="submit">提交订单</el-button>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addressApi, cartApi, catalogApi, orderApi } from '../../api'
import { formatPrice, productImage } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const addresses = ref([])
const addressId = ref()
const lines = ref([])
const remark = ref('')
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const previewTotal = computed(() => lines.value.reduce((sum, item) => sum + Number(item.price) * item.quantity, 0))

async function load() {
  loading.value = true
  error.value = ''
  try {
    addresses.value = await addressApi.list()
    addressId.value = addresses.value.find((item) => item.isDefault)?.id || addresses.value[0]?.id
    if (route.query.productId) {
      const product = await catalogApi.product(route.query.productId)
      lines.value = [{ name: product.name, price: product.price, quantity: Number(route.query.quantity || 1), productId: product.id, image: product.coverImage }]
    } else if (route.query.cart) {
      const ids = String(route.query.cart).split(',').map(Number)
      const cart = await cartApi.list()
      lines.value = cart.filter((item) => ids.includes(item.id)).map((item) => ({
        id: item.id, name: item.productName, price: item.price, quantity: item.quantity, productId: item.productId, image: item.coverImage
      }))
    }
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function submit() {
  submitting.value = true
  try {
    const payload = { addressId: addressId.value, remark: remark.value }
    if (route.query.cart) payload.cartItemIds = lines.value.map((item) => item.id)
    else payload.items = lines.value.map((item) => ({ productId: item.productId, quantity: item.quantity }))
    const order = await orderApi.create(payload, crypto.randomUUID())
    ElMessage.success(`下单成功，实付 ${formatPrice(order.totalAmount)}`)
    router.push(`/orders/${order.id}`)
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
section { background: var(--card); border: 1px solid var(--line); padding: 16px; margin-bottom: 16px; }
.goods { display: flex; align-items: center; gap: 12px; }
.goods img { width: 64px; height: 64px; object-fit: cover; background: #f3efe8; }
.addresses { display: flex; flex-direction: column; align-items: stretch; gap: 8px; }
.sum { color: var(--muted); }
</style>
