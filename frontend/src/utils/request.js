import axios from 'axios'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: `${import.meta.env.BASE_URL}api`,
  timeout: 30000,
})

// 401 统一处理：登出并跳登录（axios 响应拦截器与 fetch 流式路径共用，避免逻辑分叉）
export function handleUnauthorized() {
  const userStore = useUserStore()
  userStore.logout()
  router.push('/login')
}

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) handleUnauthorized()
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) handleUnauthorized()
    const data = error.response?.data
    let message = '请求失败'
    if (typeof data?.message === 'string' && data.message) {
      message = data.message
    } else if (error.response?.status === 413) {
      message = '文件过大，请压缩后重试（最大 5MB）'
    } else if (error.message && error.message !== 'Network Error') {
      message = error.message
    }
    return Promise.reject(Object.assign(error, { message }))
  }
)

export default request
