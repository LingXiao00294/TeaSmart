import request from '@/utils/request'

export const getBanners = () => request.get('/banners')
export const getCategories = () => request.get('/categories')
export const getProducts = (categoryId) => request.get('/products', { params: { categoryId } })
export const getProductDetail = (id) => request.get(`/products/${id}`)
export const getRecommend = () => request.post('/ai/recommend')

export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getMe = () => request.get('/auth/me')
export const updateProfile = (data) => request.put('/auth/profile', data)
export const changePassword = (data) => request.put('/auth/password', data)

export const addToCart = (data) => request.post('/cart', data)
export const getCart = () => request.get('/cart')
export const updateCartItem = (id, data) => request.put(`/cart/${id}`, data)
export const deleteCartItem = (id) => request.delete(`/cart/${id}`)
export const clearCart = () => request.delete('/cart')

export const createOrder = (data) => request.post('/orders', data)
export const payOrder = (id) => request.post(`/orders/${id}/pay`)
export const getOrders = (status) => request.get('/orders', { params: { status } })
export const getOrderDetail = (id) => request.get(`/orders/${id}`)
export const cancelOrder = (id) => request.put(`/orders/${id}/cancel`)

// ========== Admin ==========
export const adminGetCategories = () => request.get('/admin/categories')
export const adminCreateCategory = (data) => request.post('/admin/categories', data)
export const adminUpdateCategory = (id, data) => request.put(`/admin/categories/${id}`, data)
export const adminDeleteCategory = (id) => request.delete(`/admin/categories/${id}`)

export const adminGetProducts = (params) => request.get('/admin/products', { params })
export const adminCreateProduct = (data) => request.post('/admin/products', data)
export const adminGetProduct = (id) => request.get(`/admin/products/${id}`)
export const adminUpdateProduct = (id, data) => request.put(`/admin/products/${id}`, data)
export const adminDeleteProduct = (id) => request.delete(`/admin/products/${id}`)

export const adminGetOrders = (params) => request.get('/admin/orders', { params })
export const adminUpdateOrderStatus = (id, status) => request.put(`/admin/orders/${id}/status`, { status })

export const adminGetUsers = (params) => request.get('/admin/users', { params })

export const adminGetBanners = () => request.get('/admin/banners')
export const adminCreateBanner = (data) => request.post('/admin/banners', data)
export const adminUpdateBanner = (id, data) => request.put(`/admin/banners/${id}`, data)
export const adminDeleteBanner = (id) => request.delete(`/admin/banners/${id}`)

export const adminGetKnowledge = () => request.get('/admin/knowledge')
export const adminCreateKnowledge = (data) => request.post('/admin/knowledge', data)
export const adminUpdateKnowledge = (id, data) => request.put(`/admin/knowledge/${id}`, data)
export const adminDeleteKnowledge = (id) => request.delete(`/admin/knowledge/${id}`)

export const adminUpload = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/admin/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export const adminGetDashboard = () => request.get('/admin/dashboard/stats')
