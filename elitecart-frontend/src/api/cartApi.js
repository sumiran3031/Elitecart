import axiosClient from './axiosClient.js'

export const cartApi = {
  getCart: () => axiosClient.get('/cart'),
  addItem: (payload) => axiosClient.post('/cart/items', payload),
  updateItem: (itemId, payload) => axiosClient.put(`/cart/items/${itemId}`, payload),
  removeItem: (itemId) => axiosClient.delete(`/cart/items/${itemId}`),
  clearCart: () => axiosClient.delete('/cart'),
}
