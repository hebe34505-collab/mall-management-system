<template>
  <el-card style="max-width: 640px">
    <p>用户名：{{ auth.user?.username }}</p>
    <p>邮箱：{{ auth.user?.email }}</p>
    <p>角色：管理员</p>
    <el-divider />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="当前密码" prop="oldPassword"><el-input v-model="form.oldPassword" type="password" show-password /></el-form-item>
      <el-form-item label="新密码" prop="newPassword"><el-input v-model="form.newPassword" type="password" show-password /></el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" show-password /></el-form-item>
      <el-button type="primary" @click="save">修改密码</el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../../api'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const formRef = ref()
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const rules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,32}$/, message: '新密码需为 8-32 位并包含字母和数字', trigger: 'blur' }],
  confirmPassword: [{ validator: (_, value, callback) => value === form.newPassword ? callback() : callback(new Error('两次密码不一致')), trigger: 'blur' }]
}
async function save() {
  await formRef.value.validate()
  await authApi.changePassword(form)
  ElMessage.success('密码已修改，请重新登录')
  await auth.logout()
  router.push('/admin/login')
}
</script>
