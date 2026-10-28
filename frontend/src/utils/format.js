export function productImage(src) {
  if (!src) return ''
  if (src.startsWith('/images/products/') && src.endsWith('.svg')) return src.replace(/\.svg$/, '.jpg')
  return src
}

export function formatPrice(value) {
  const number = Number(value)
  if (Number.isNaN(number)) return '¥0.00'
  return `¥${number.toFixed(2)}`
}

export function formatDate(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 16)
  const pad = (item) => String(item).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export const orderStatusType = {
  PENDING_SHIPMENT: 'warning',
  SHIPPED: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'info'
}
