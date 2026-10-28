<template>
  <el-card>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" style="max-width: 720px">
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" style="width: 100%">
          <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="副标题" prop="subtitle"><el-input v-model="form.subtitle" /></el-form-item>
      <el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
      <el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0.01" :precision="2" /></el-form-item>
      <el-form-item label="库存" prop="stock"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
      <el-form-item label="图片" prop="coverImage"><el-input v-model="form.coverImage" placeholder="/images/products/p01.jpg" /></el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status"><el-radio value="ON_SHELF">上架</el-radio><el-radio value="OFF_SHELF">下架</el-radio></el-radio-group>
      </el-form-item>
      <el-form-item label="推荐"><el-switch v-model="form.recommend" /></el-form-item>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../api'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const categories = ref([])
const saving = ref(false)
const form = reactive({ categoryId: null, name: '', subtitle: '', description: '', price: 1, stock: 0, coverImage: '/images/products/p01.jpg', status: 'ON_SHELF', recommend: false })
const rules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (route.params.id) await adminApi.updateProduct(route.params.id, form)
    else await adminApi.saveProduct(form)
    ElMessage.success('商品已保存')
    router.push('/admin/products')
  } finally { saving.value = false }
}

onMounted(async () => {
  categories.value = await adminApi.categories()
  if (route.params.id) Object.assign(form, await adminApi.product(route.params.id))
})
</script>
