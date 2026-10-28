<template>
  <main class="auth">
    <section class="panel">
      <p class="eyebrow">{{ adminMode ? '管理后台' : '禾木商城' }}</p>
      <h1>{{ adminMode ? '管理员登录' : '欢迎回来' }}</h1>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="用户名或邮箱" prop="account">
          <el-input v-model="form.account" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-button type="primary" class="full" native-type="submit" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <p v-if="!adminMode" class="extra">还没有账号？<RouterLink to="/register">去注册</RouterLink></p>
      <p v-if="!adminMode" class="extra">管理员请使用<RouterLink to="/admin/login">后台登录</RouterLink></p>
      <p v-else class="extra">普通用户请<RouterLink to="/login">返回商城登录</RouterLink></p>
    </section>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)
const adminMode = computed(() => route.path.startsWith('/admin'))
const form = reactive({ account: '', password: '' })
const rules = {
  account: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const user = await auth.login(form)
    if (adminMode.value && user.role !== 'ADMIN') {
      await auth.logout()
      ElMessage.error('请使用管理员账号登录后台')
      return
    }
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    if (user.role === 'ADMIN' && (adminMode.value || redirect.startsWith('/admin'))) {
      router.push(redirect || '/admin/dashboard')
    } else {
      router.push(redirect && !redirect.startsWith('/admin') ? redirect : '/')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth { min-height: 100vh; display: grid; place-items: center; padding: 32px 16px; background: var(--bg); }
.panel { width: min(400px, 100%); background: var(--card); border: 1px solid var(--line); padding: 32px 28px; }
.eyebrow { margin: 0; color: var(--muted); font-size: 13px; letter-spacing: 0.08em; }
h1 { margin: 8px 0 22px; font-size: 30px; }
.full { width: 100%; }
.extra { margin: 14px 0 0; color: var(--muted); }
.extra a { color: var(--primary); margin-left: 6px; }
</style>
