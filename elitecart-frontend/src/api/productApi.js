import axiosClient from './axiosClient.js'

export const productApi = {
  search: (params) => axiosClient.get('/products', { params }),
  getById: (id) => axiosClient.get(`/products/${id}`),
  getBySlug: (slug) => axiosClient.get(`/products/slug/${slug}`),
  getFeatured: () => axiosClient.get('/products/featured'),
  getLatest: () => axiosClient.get('/products/latest'),
  getBestSellers: () => axiosClient.get('/products/best-sellers'),
  getReviews: (productId, params) => axiosClient.get(`/reviews/product/${productId}`, { params }),
  addReview: (productId, payload) => axiosClient.post(`/reviews/product/${productId}`, payload),
}
