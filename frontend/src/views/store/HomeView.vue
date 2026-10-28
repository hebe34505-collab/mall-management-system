<template>
  <div class="page">
    <el-carousel class="hero" height="460px" indicator-position="outside" arrow="hover" :interval="7000">
      <el-carousel-item v-for="item in banners" :key="item.title">
        <div class="banner" :style="{ backgroundImage: `url(${item.image})` }">
          <div class="copy">
            <p>{{ item.kicker }}</p>
            <h1>{{ item.title }}</h1>
            <RouterLink :to="item.to">去看看</RouterLink>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <section>
      <div class="section-head"><h2>热销商品</h2><RouterLink to="/products?sort=sales_desc">查看全部</RouterLink></div>
      <div v-loading="loading">
        <el-empty v-if="!loading && error" :description="error" />
        <el-empty v-else-if="!loading && !hot.length" description="暂时没有热销商品" />
        <div v-else class="hot-grid">
          <ProductCard v-for="(item, index) in hot" :key="item.id" :product="item" :class="{ lead: index === 0 }" />
        </div>
      </div>
    </section>
    <section>
      <div class="section-head"><h2>新品推荐</h2><RouterLink to="/products?sort=newest">查看全部</RouterLink></div>
      <div class="product-grid"><ProductCard v-for="item in newest" :key="item.id" :product="item" /></div>
    </section>
    <section>
      <div class="section-head"><h2>编辑推荐</h2><RouterLink to="/products?recommend=true">查看全部</RouterLink></div>
      <div class="product-grid"><ProductCard v-for="item in recommend" :key="item.id" :product="item" /></div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import ProductCard from '../../components/ProductCard.vue'
import { catalogApi } from '../../api'

const banners = [
  { kicker: '书桌', title: '把书桌整理成舒服的学习角', to: '/products?categoryId=5', image: '/images/scenes/desk.jpg' },
  { kicker: '日常', title: '一杯咖啡，一段写作业的时间', to: '/products?categoryId=3', image: '/images/scenes/coffee.jpg' },
  { kicker: '运动', title: '宿舍里也能展开的瑜伽垫', to: '/products?categoryId=6', image: '/images/scenes/move.jpg' }
]
const hot = ref([])
const newest = ref([])
const recommend = ref([])
const loading = ref(false)
const error = ref('')

onMounted(async () => {
  loading.value = true
  error.value = ''
  try {
    const [hotPage, newPage, recPage] = await Promise.all([
      catalogApi.products({ sort: 'sales_desc', page: 1, size: 4 }),
      catalogApi.products({ sort: 'newest', page: 1, size: 4 }),
      catalogApi.products({ recommend: true, page: 1, size: 4 })
    ])
    hot.value = hotPage.records
    newest.value = newPage.records
    recommend.value = recPage.records
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hero { margin-bottom: 8px; }
.banner {
  height: 460px;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: flex-end;
}
.copy {
  margin: 28px;
  max-width: 460px;
  padding: 22px 24px;
  background: rgba(247, 244, 239, 0.94);
  color: var(--ink);
}
.copy p { margin: 0; font-size: 13px; letter-spacing: 0.12em; color: var(--muted); }
.copy h1 { margin: 8px 0 16px; font-size: 30px; text-wrap: balance; }
.copy a {
  display: inline-block;
  color: var(--card);
  background: var(--primary);
  padding: 8px 16px;
  font-size: 14px;
}
.hot-grid {
  display: grid;
  grid-template-columns: 1.35fr 1fr 1fr;
  gap: 28px 18px;
}
.hot-grid :deep(.lead) { grid-row: span 2; }
@media (max-width: 768px) {
  .hero, .banner { height: 280px; }
  :deep(.el-carousel__container) { height: 280px !important; }
  .copy { margin: 14px; padding: 16px; }
  .copy h1 { font-size: 24px; }
  .hot-grid { grid-template-columns: 1fr 1fr; }
  .hot-grid :deep(.lead) { grid-column: span 2; grid-row: auto; }
}
</style>
