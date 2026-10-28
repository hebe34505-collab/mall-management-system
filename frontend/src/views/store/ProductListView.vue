<template>
  <div class="page">
    <div class="layout">
      <aside>
        <h1>商品</h1>
        <el-radio-group v-model="categoryId" class="cats" @change="pushQuery">
          <el-radio-button :value="''">全部</el-radio-button>
          <el-radio-button v-for="item in categories" :key="item.id" :value="String(item.id)">{{ item.name }}</el-radio-button>
        </el-radio-group>
      </aside>
      <section>
        <div class="toolbar">
          <el-input v-model="keyword" placeholder="搜索商品" clearable @clear="pushQuery" @keyup.enter="pushQuery" />
          <el-select v-model="sort" style="width: 160px" @change="pushQuery">
            <el-option label="最新上架" value="newest" />
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
            <el-option label="销量优先" value="sales_desc" />
          </el-select>
          <el-button type="primary" @click="pushQuery">筛选</el-button>
        </div>
        <div v-loading="loading" class="state-block">
          <el-alert v-if="error" :title="error" type="error" show-icon />
          <el-empty v-else-if="!loading && !records.length" description="没有找到符合条件的商品" />
          <div v-else class="product-grid"><ProductCard v-for="item in records" :key="item.id" :product="item" /></div>
        </div>
        <el-pagination
          v-if="total"
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="load"
        />
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ProductCard from '../../components/ProductCard.vue'
import { catalogApi } from '../../api'

const route = useRoute()
const router = useRouter()
const categories = ref([])
const records = ref([])
const keyword = ref('')
const categoryId = ref('')
const sort = ref('newest')
const page = ref(1)
const size = 8
const total = ref(0)
const loading = ref(false)
const error = ref('')

function syncFromRoute() {
  keyword.value = route.query.keyword || ''
  categoryId.value = route.query.categoryId ? String(route.query.categoryId) : ''
  sort.value = route.query.sort || 'newest'
}

function pushQuery() {
  router.push({ path: '/products', query: {
    keyword: keyword.value || undefined,
    categoryId: categoryId.value || undefined,
    sort: sort.value,
    recommend: route.query.recommend || undefined
  } })
}

async function load(nextPage = 1) {
  page.value = nextPage
  loading.value = true
  error.value = ''
  try {
    const data = await catalogApi.products({
      keyword: keyword.value || undefined,
      categoryId: categoryId.value || undefined,
      sort: sort.value,
      recommend: route.query.recommend || undefined,
      page: page.value,
      size
    })
    records.value = data.records
    total.value = data.total
  } catch (err) {
    records.value = []
    error.value = err.message
  } finally {
    loading.value = false
  }
}

watch(() => route.fullPath, () => {
  if (route.path !== '/products') return
  syncFromRoute()
  load(1)
})
onMounted(async () => {
  syncFromRoute()
  try { categories.value = await catalogApi.categories() } catch { categories.value = [] }
  await load(1)
})
</script>

<style scoped>
.layout { display: grid; grid-template-columns: 180px 1fr; gap: 36px; }
h1 { margin: 0 0 14px; font-size: 28px; }
.cats { display: flex; flex-direction: column; align-items: stretch; gap: 2px; }
.cats :deep(.el-radio-button__inner) {
  width: 100%;
  text-align: left;
  border: 0 !important;
  border-radius: 0 !important;
  background: transparent;
  box-shadow: none !important;
  color: var(--muted);
  padding: 8px 0;
}
.cats :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: transparent;
  color: var(--primary);
  box-shadow: none !important;
}
.toolbar { display: flex; gap: 8px; margin-bottom: 18px; }
@media (max-width: 768px) {
  .layout { grid-template-columns: 1fr; gap: 12px; }
  .cats { flex-direction: row; overflow-x: auto; }
  .cats :deep(.el-radio-button__inner) { white-space: nowrap; padding: 8px 12px; }
  .toolbar { flex-direction: column; }
  .toolbar :deep(.el-select) { width: 100% !important; }
}
</style>
