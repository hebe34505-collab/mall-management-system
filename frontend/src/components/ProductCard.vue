<template>
  <article class="product-card">
    <RouterLink :to="`/products/${product.id}`" :aria-label="product.name">
      <img :src="productImage(product.coverImage) || '/favicon.svg'" :alt="product.name" />
    </RouterLink>
    <div class="info">
      <h3><RouterLink :to="`/products/${product.id}`">{{ product.name }}</RouterLink></h3>
      <p class="sub">{{ product.subtitle || '禾木精选' }}</p>
      <div class="meta">
        <span class="price">{{ formatPrice(product.price) }}</span>
        <span class="muted">{{ product.stock > 0 ? `库存 ${product.stock}` : '暂时缺货' }}</span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { formatPrice, productImage } from '../utils/format'

defineProps({
  product: { type: Object, required: true }
})
</script>
