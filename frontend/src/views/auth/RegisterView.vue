<template>
  <main class="auth">
    <section class="panel">
      <p class="eyebrow">创建账号</p>
      <h1>注册禾木商城</h1>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" show-password /></el-form-item>
        <el-button type="primary" class="full" :loading="loading" @click="submit">注册</el-button>
      </el-form>
      <p class="extra">已有账号？<RouterLink to="/login">去登录</RouterLink></p>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../../api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', email: '', password: '', confirmPassword: '' })
const rules = {
  username: [{ required: true, pattern: /^[A-Za-z0-9_]{4,20}$/, message: '用户名需为 4-20 位字母、数字或下划线', trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  password: [{ required: true, pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,32}$/, message: '密码需为 8-32 位并包含字母和数字', trigger: 'blur' }],
  confirmPassword: [{
    validator: (_, value, callback) => value === form.password ? callback() : callback(new Error('两次密码不一致')),
    trigger: 'blur'
  }]
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await authApi.register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth { min-height: 100vh; display: grid; place-items: center; padding: 32px 16px; background: var(--bg); }
.panel { width: min(440px, 100%); background: var(--card); border: 1px solid var(--line); padding: 32px 28px; }
.eyebrow { margin: 0; color: var(--muted); font-size: 13px; letter-spacing: 0.08em; }
h1 { margin: 8px 0 22px; font-size: 30px; }
.full { width: 100%; }
.extra a { color: var(--primary); margin-left: 6px; }
</style>
