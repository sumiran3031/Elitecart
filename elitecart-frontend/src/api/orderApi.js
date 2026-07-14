import axiosClient from './axiosClient.js'

export const orderApi = {
  checkout: (payload) => axiosClient.post('/orders/checkout', payload),
  getMyOrders: (params) => axiosClient.get('/orders', { params }),
  getById: (id) => axiosClient.get(`/orders/${id}`),
  cancel: (id, reason) => axiosClient.post(`/orders/${id}/cancel`, { reason }),
}
