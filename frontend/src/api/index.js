import request from '@/utils/request'

export const getBanners = () => request.get('/banners')
export const getCategories = () => request.get('/categories')
export const getProducts = (categoryId) => request.get('/products', { params: { categoryId } })
export const getProductDetail = (id) => request.get(`/products/${id}`)
export const getRecommend = () => request.post('/ai/recommend')

export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getMe = () => request.get('/auth/me')

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
