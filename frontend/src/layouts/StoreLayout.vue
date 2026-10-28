<template>
  <div class="store">
    <header class="top">
      <div class="page bar">
        <RouterLink to="/" class="brand">
          <img src="/favicon.svg" alt="" width="36" height="36" />
          <span>禾木商城</span>
        </RouterLink>
        <form class="search" @submit.prevent="search">
          <label class="sr" for="keyword">搜索商品</label>
          <el-input id="keyword" v-model="keyword" placeholder="搜索商品名称" clearable />
          <el-button type="primary" native-type="submit">搜索</el-button>
        </form>
        <nav class="actions" aria-label="账户导航">
          <RouterLink to="/cart">购物车</RouterLink>
          <RouterLink v-if="!auth.isLogin" to="/login">登录</RouterLink>
          <RouterLink v-else to="/profile">{{ auth.user?.nickname || '个人中心' }}</RouterLink>
          <RouterLink v-if="auth.isAdmin" to="/admin/dashboard">管理后台</RouterLink>
        </nav>
        <button class="menu" type="button" @click="open = !open" :aria-expanded="open">菜单</button>
      </div>
      <nav class="cats page" :class="{ open }" aria-label="商品分类">
        <RouterLink to="/products">全部商品</RouterLink>
        <RouterLink v-for="item in categories" :key="item.id" :to="`/products?categoryId=${item.id}`">{{ item.name }}</RouterLink>
      </nav>
    </header>
    <main>
      <router-view />
    </main>
    <footer>
      <div class="page foot">
        <div>
          <strong>禾木商城</strong>
          <p>给校园生活准备的日用与数码小店。开发学习项目，不用于真实交易。</p>
        </div>
        <p>客服邮箱 study@mall.local</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { catalogApi } from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const keyword = ref(route.query.keyword || '')
const categories = ref([])
const open = ref(false)

watch(() => route.query.keyword, (value) => { keyword.value = value || '' })

onMounted(async () => {
  try { categories.value = await catalogApi.categories() } catch { categories.value = [] }
})

function search() {
  open.value = false
  router.push({ path: '/products', query: keyword.value ? { keyword: keyword.value } : {} })
}
</script>

<style scoped>
.top { background: rgba(247, 244, 239, 0.94); border-bottom: 1px solid var(--line); position: sticky; top: 0; z-index: 20; backdrop-filter: blur(8px); }
.bar { display: flex; align-items: center; gap: 20px; min-height: 68px; }
.brand { display: flex; align-items: center; gap: 8px; font-family: var(--display); font-size: 22px; font-weight: 500; letter-spacing: 0.04em; }
.search { display: flex; gap: 8px; flex: 1; max-width: 460px; }
.actions { display: flex; gap: 18px; margin-left: auto; font-size: 14px; }
.actions a:hover { color: var(--primary); }
.menu { display: none; }
.cats { display: flex; gap: 20px; padding-bottom: 12px; overflow-x: auto; }
.cats a { white-space: nowrap; color: var(--muted); font-size: 14px; }
.cats a.router-link-active, .cats a:hover { color: var(--primary); }
main { min-height: calc(100vh - 220px); padding: 18px 0 56px; }
footer { background: var(--primary-deep); color: #f6f3ee; }
.foot { display: flex; justify-content: space-between; gap: 20px; padding: 32px 0; font-size: 14px; }
.foot p { margin: 6px 0 0; color: #d9d1c6; max-width: 420px; }
.sr { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0 0 0 0); }
@media (max-width: 768px) {
  .bar { flex-wrap: wrap; padding: 10px 0; }
  .search { order: 3; max-width: none; width: 100%; }
  .actions { display: none; }
  .menu { display: inline-flex; margin-left: auto; border: 1px solid var(--line); background: var(--card); border-radius: 2px; padding: 6px 10px; }
  .cats { display: none; }
  .cats.open { display: flex; flex-direction: column; padding-bottom: 12px; }
  .foot { flex-direction: column; }
}
</style>
