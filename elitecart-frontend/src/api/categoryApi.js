import axiosClient from './axiosClient.js'

export const categoryApi = {
  getAll: () => axiosClient.get('/categories'),
  getTree: () => axiosClient.get('/categories/tree'),
  getBySlug: (slug) => axiosClient.get(`/categories/slug/${slug}`),
}
