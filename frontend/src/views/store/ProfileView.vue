<template>
  <div class="page">
    <div class="section-head"><h1>个人中心</h1><el-button @click="logout">退出登录</el-button></div>
    <section class="stats">
      <article class="stat main">
        <p>消费金额</p>
        <strong>{{ formatPrice(summary.totalSpent || 0) }}</strong>
      </article>
      <article class="stat"><p>全部订单</p><strong>{{ summary.orderCount || 0 }}</strong></article>
      <article class="stat"><p>待发货</p><strong>{{ summary.pendingCount || 0 }}</strong></article>
      <article class="stat"><p>已完成</p><strong>{{ summary.completedCount || 0 }}</strong></article>
    </section>
    <p class="muted rule">{{ summary.salesRule }}</p>
    <el-tabs>
      <el-tab-pane label="个人资料">
        <el-form ref="profileRef" :model="profile" :rules="profileRules" label-width="80px" style="max-width: 520px">
          <el-form-item label="用户名"><el-input :model-value="auth.user?.username" disabled /><span class="hint">注册后不可修改</span></el-form-item>
          <el-form-item label="邮箱"><el-input :model-value="auth.user?.email" disabled /><span class="hint">注册后不可修改</span></el-form-item>
          <el-form-item label="昵称" prop="nickname"><el-input v-model="profile.nickname" /></el-form-item>
          <el-form-item label="手机" prop="phone"><el-input v-model="profile.phone" /></el-form-item>
          <el-button type="primary" @click="saveProfile">保存资料</el-button>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="修改密码">
        <el-form ref="passwordRef" :model="password" :rules="passwordRules" label-width="90px" style="max-width: 520px">
          <el-form-item label="当前密码" prop="oldPassword"><el-input v-model="password.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码" prop="newPassword"><el-input v-model="password.newPassword" type="password" show-password /></el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="password.confirmPassword" type="password" show-password /></el-form-item>
          <el-button type="primary" @click="savePassword">修改密码</el-button>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="收货地址">
        <el-button type="primary" @click="openAddress()">新增地址</el-button>
        <el-table :data="addresses" style="margin-top: 12px">
          <el-table-column prop="receiver" label="收货人" width="100" />
          <el-table-column prop="phone" label="手机" width="140" />
          <el-table-column prop="fullAddress" label="地址" />
          <el-table-column label="默认" width="80"><template #default="{ row }">{{ row.isDefault ? '是' : '' }}</template></el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button link @click="openAddress(row)">编辑</el-button>
              <el-button link type="danger" @click="removeAddress(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="最近购买">
        <el-table :data="summary.recentOrders || []">
          <el-table-column prop="orderNo" label="订单号" />
          <el-table-column prop="statusLabel" label="状态" />
          <el-table-column label="金额"><template #default="{ row }">{{ formatPrice(row.totalAmount) }}</template></el-table-column>
        </el-table>
        <RouterLink to="/orders">查看全部订单</RouterLink>
      </el-tab-pane>
    </el-tabs>
    <el-dialog v-model="visible" title="收货地址" width="520px">
      <el-form ref="addressRef" :model="address" :rules="addressRules" label-width="80px">
        <el-form-item label="收货人" prop="receiver"><el-input v-model="address.receiver" /></el-form-item>
        <el-form-item label="手机" prop="phone"><el-input v-model="address.phone" /></el-form-item>
        <el-form-item label="省" prop="province"><el-input v-model="address.province" /></el-form-item>
        <el-form-item label="市" prop="city"><el-input v-model="address.city" /></el-form-item>
        <el-form-item label="区" prop="district"><el-input v-model="address.district" /></el-form-item>
        <el-form-item label="详细地址" prop="detail"><el-input v-model="address.detail" /></el-form-item>
        <el-form-item label="默认"><el-switch v-model="address.isDefault" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="saveAddress">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addressApi, authApi } from '../../api'
import { useAuthStore } from '../../stores/auth'
import { formatPrice } from '../../utils/format'

const auth = useAuthStore()
const router = useRouter()
const summary = ref({})
const addresses = ref([])
const visible = ref(false)
const editingId = ref(null)
const profileRef = ref()
const passwordRef = ref()
const addressRef = ref()
const profile = reactive({ nickname: auth.user?.nickname || '', phone: auth.user?.phone || '' })
const password = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const address = reactive({ receiver: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })
const profileRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ pattern: /^$|^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }]
}
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,32}$/, message: '新密码需为 8-32 位并包含字母和数字', trigger: 'blur' }],
  confirmPassword: [{ validator: (_, value, callback) => value === password.newPassword ? callback() : callback(new Error('两次密码不一致')), trigger: 'blur' }]
}
const addressRules = {
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}
async function load() {
  summary.value = await authApi.summary()
  addresses.value = await addressApi.list()
}
async function saveProfile() {
  await profileRef.value.validate()
  auth.updateUser(await authApi.updateProfile(profile))
  ElMessage.success('资料已保存')
}
async function savePassword() {
  await passwordRef.value.validate()
  await authApi.changePassword(password)
  ElMessage.success('密码已修改，请重新登录')
  await auth.logout()
  router.push('/login')
}
function openAddress(row) {
  editingId.value = row?.id || null
  Object.assign(address, row || { receiver: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })
  visible.value = true
}
async function saveAddress() {
  await addressRef.value.validate()
  if (editingId.value) await addressApi.update(editingId.value, address)
  else await addressApi.create(address)
  visible.value = false
  addresses.value = await addressApi.list()
  ElMessage.success('地址已保存')
}
async function removeAddress(row) {
  await ElMessageBox.confirm('确定删除这个地址吗？', '删除地址')
  await addressApi.remove(row.id)
  addresses.value = await addressApi.list()
}
async function logout() {
  await auth.logout()
  router.push('/login')
}
onMounted(load)
</script>

<style scoped>
.stats { display: grid; grid-template-columns: 1.4fr 1fr 1fr 1fr; gap: 12px; }
.stat { background: var(--card); border: 1px solid var(--line); padding: 16px 18px; }
.stat p { margin: 0; color: var(--muted); font-size: 13px; }
.stat strong { display: block; margin-top: 8px; font-family: var(--display); font-size: 26px; font-weight: 500; }
.stat.main { background: var(--sage); }
.stat.main strong { font-size: 32px; color: var(--primary); }
.rule { margin: 12px 0 20px; }
.hint { display: block; margin-top: 4px; color: var(--muted); font-size: 12px; }
@media (max-width: 768px) {
  .stats { grid-template-columns: 1fr 1fr; }
  .stat.main { grid-column: span 2; }
}
</style>
