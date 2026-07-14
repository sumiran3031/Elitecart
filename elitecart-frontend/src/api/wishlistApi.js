import axiosClient from './axiosClient.js'

export const wishlistApi = {
  getWishlist: () => axiosClient.get('/wishlist'),
  addItem: (payload) => axiosClient.post('/wishlist/items', payload),
  removeItem: (itemId) => axiosClient.delete(`/wishlist/items/${itemId}`),
  moveToCart: (itemId) => axiosClient.post(`/wishlist/items/${itemId}/move-to-cart`),
}
