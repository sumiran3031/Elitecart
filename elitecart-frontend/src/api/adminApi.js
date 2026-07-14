import axiosClient from './axiosClient.js'

export const adminProductApi = {
  create: (payload) => axiosClient.post('/admin/products', payload),
  update: (id, payload) => axiosClient.put(`/admin/products/${id}`, payload),
  remove: (id) => axiosClient.delete(`/admin/products/${id}`),
}

export const adminCategoryApi = {
  create: (payload) => axiosClient.post('/admin/categories', payload),
  update: (id, payload) => axiosClient.put(`/admin/categories/${id}`, payload),
  remove: (id) => axiosClient.delete(`/admin/categories/${id}`),
}

export const adminOrderApi = {
  getAll: (params) => axiosClient.get('/admin/orders', { params }),
  updateStatus: (id, status) => axiosClient.put(`/admin/orders/${id}/status`, { status }),
}

export const adminPaymentApi = {
  refund: (orderId) => axiosClient.post(`/admin/payments/orders/${orderId}/refund`),
}

export const adminCustomerApi = {
  getAll: (params) => axiosClient.get('/admin/customers', { params }),
}

export const adminAnalyticsApi = {
  getDashboard: () => axiosClient.get('/admin/analytics/dashboard'),
  exportOrdersCsv: () => axiosClient.get('/admin/analytics/orders/export', { responseType: 'blob' }),
}
