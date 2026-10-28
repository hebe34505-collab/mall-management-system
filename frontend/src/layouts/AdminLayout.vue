<template>
  <el-container class="admin">
    <el-aside :width="collapsed ? '72px' : '220px'" class="aside">
      <div class="logo">{{ collapsed ? '禾' : '禾木后台' }}</div>
      <el-menu :default-active="route.path" router :collapse="collapsed" background-color="#24382e" text-color="#e7e1d6" active-text-color="#ffffff">
        <el-menu-item index="/admin/dashboard">数据总览</el-menu-item>
        <el-menu-item index="/admin/products">商品管理</el-menu-item>
        <el-menu-item index="/admin/categories">分类管理</el-menu-item>
        <el-menu-item index="/admin/stock">库存管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/stats">销售统计</el-menu-item>
        <el-menu-item index="/admin/logs">操作日志</el-menu-item>
        <el-menu-item index="/admin/profile">个人资料</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <button type="button" class="fold" @click="collapsed = !collapsed">{{ collapsed ? '展开' : '收起' }}</button>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">后台</el-breadcrumb-item>
          <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="right">
          <RouterLink to="/">返回商城</RouterLink>
          <span>{{ auth.user?.nickname }}</span>
          <el-button link type="danger" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const collapsed = ref(window.innerWidth < 768)

async function logout() {
  await auth.logout()
  router.push('/admin/login')
}
</script>

<style scoped>
.admin { min-height: 100vh; }
.aside { background: #24382e; color: #f6f3ee; }
.logo { height: 60px; display: grid; place-items: center; font-family: var(--display); font-weight: 700; }
.header { display: flex; align-items: center; gap: 16px; background: #fff; border-bottom: 1px solid var(--line); }
.right { margin-left: auto; display: flex; align-items: center; gap: 12px; }
.fold { border: 1px solid var(--line); background: #fff; border-radius: 8px; padding: 6px 10px; }
.main { background: var(--bg); }
:deep(.el-menu) { border-right: none; }
</style>
