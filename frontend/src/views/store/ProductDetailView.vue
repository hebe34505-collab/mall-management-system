<template>
  <div class="page" v-loading="loading">
    <el-alert v-if="error" :title="error" type="error" show-icon />
    <article v-else-if="product" class="detail">
      <img :src="productImage(product.coverImage)" :alt="product.name" />
      <div>
        <p class="muted">{{ product.categoryName }}</p>
        <h1>{{ product.name }}</h1>
        <p>{{ product.subtitle }}</p>
        <p class="price big">{{ formatPrice(product.price) }}</p>
        <p>库存：{{ product.stock > 0 ? product.stock : '不足，暂不可购买' }}</p>
        <p class="desc">{{ product.description }}</p>
        <div class="buy">
          <el-input-number v-model="quantity" :min="1" :max="Math.max(product.stock, 1)" :disabled="!product.purchasable" />
          <el-button type="primary" :disabled="!product.purchasable" @click="addCart">加入购物车</el-button>
          <el-button :disabled="!product.purchasable" @click="buyNow">立即购买</el-button>
        </div>
      </div>
    </article>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { cartApi, catalogApi } from '../../api'
import { useAuthStore } from '../../stores/auth'
import { formatPrice, productImage } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const product = ref(null)
const quantity = ref(1)
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    product.value = await catalogApi.product(route.params.id)
    quantity.value = 1
  } catch (err) {
    product.value = null
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function addCart() {
  if (!auth.isLogin) return router.push({ path: '/login', query: { redirect: route.fullPath } })
  await cartApi.add({ productId: product.value.id, quantity: quantity.value })
  ElMessage.success('已加入购物车')
}

function buyNow() {
  if (!auth.isLogin) return router.push({ path: '/login', query: { redirect: route.fullPath } })
  router.push({ path: '/checkout', query: { productId: product.value.id, quantity: quantity.value } })
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.detail { display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 40px; align-items: start; }
.detail img { width: 100%; aspect-ratio: 1 / 1; object-fit: cover; background: #f3efe8; }
.big { font-size: 32px; }
.desc { line-height: 1.8; color: var(--ink); }
.buy { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }
@media (max-width: 768px) { .detail { grid-template-columns: 1fr; gap: 16px; } }
</style>
