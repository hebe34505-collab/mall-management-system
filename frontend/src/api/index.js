import http from './http'

export const authApi = {
  register: (data) => http.post('/auth/register', data),
  login: (data) => http.post('/auth/login', data),
  logout: () => http.post('/auth/logout'),
  me: () => http.get('/auth/me'),
  updateProfile: (data) => http.put('/users/me', data),
  changePassword: (data) => http.put('/users/me/password', data),
  summary: () => http.get('/users/me/summary', { silent: true })
}

export const catalogApi = {
  categories: () => http.get('/categories', { silent: true }),
  products: (params) => http.get('/products', { params, silent: true }),
  product: (id) => http.get(`/products/${id}`, { silent: true })
}

export const cartApi = {
  list: () => http.get('/cart', { silent: true }),
  add: (data) => http.post('/cart', data),
  update: (id, data) => http.put(`/cart/${id}`, data),
  remove: (id) => http.delete(`/cart/${id}`),
  clear: () => http.delete('/cart')
}

export const orderApi = {
  create: (data, idempotencyKey) => http.post('/orders', data, { headers: { 'Idempotency-Key': idempotencyKey } }),
  list: (params) => http.get('/orders', { params, silent: true }),
  detail: (id) => http.get(`/orders/${id}`, { silent: true }),
  cancel: (id) => http.post(`/orders/${id}/cancel`)
}

export const addressApi = {
  list: () => http.get('/addresses', { silent: true }),
  create: (data) => http.post('/addresses', data),
  update: (id, data) => http.put(`/addresses/${id}`, data),
  remove: (id) => http.delete(`/addresses/${id}`)
}

export const adminApi = {
  products: (params) => http.get('/admin/products', { params, silent: true }),
  product: (id) => http.get(`/admin/products/${id}`, { silent: true }),
  saveProduct: (data) => http.post('/products', data),
  updateProduct: (id, data) => http.put(`/products/${id}`, data),
  removeProduct: (id) => http.delete(`/products/${id}`),
  productStatus: (id, status) => http.patch(`/products/${id}/status`, { status }),
  adjustStock: (id, stock) => http.patch(`/admin/products/${id}/stock`, { stock }),
  categories: () => http.get('/admin/categories', { silent: true }),
  saveCategory: (data) => http.post('/categories', data),
  updateCategory: (id, data) => http.put(`/categories/${id}`, data),
  removeCategory: (id) => http.delete(`/categories/${id}`),
  orders: (params) => http.get('/admin/orders', { params, silent: true }),
  order: (id) => http.get(`/admin/orders/${id}`, { silent: true }),
  orderStatus: (id, status) => http.patch(`/admin/orders/${id}/status`, { status }),
  users: (params) => http.get('/admin/users', { params, silent: true }),
  userStatus: (id, status) => http.patch(`/admin/users/${id}/status`, { status }),
  overview: () => http.get('/admin/stats/overview', { silent: true }),
  trend: (days = 7) => http.get('/admin/stats/trend', { params: { days }, silent: true }),
  logs: (params) => http.get('/admin/logs', { params, silent: true })
}
