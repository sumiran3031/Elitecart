import axiosClient from './axiosClient.js'

export const addressApi = {
  getAll: () => axiosClient.get('/addresses'),
  create: (payload) => axiosClient.post('/addresses', payload),
  update: (id, payload) => axiosClient.put(`/addresses/${id}`, payload),
  remove: (id) => axiosClient.delete(`/addresses/${id}`),
}
