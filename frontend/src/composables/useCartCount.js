import { ref } from 'vue'
import { getCart } from '@/api'

// 购物车数量：模块级单例 ref，跨组件/跨页面共享，保证角标全局一致且实时
const count = ref(0)

export function useCartCount() {
  return { count, refresh }
}

// 重新拉取购物车数量；任何加购/改数量/删除/下单后调用即可实时同步角标
export async function refresh() {
  try {
    const res = await getCart()
    count.value = res.data?.length || 0
  } catch {
    count.value = 0
  }
}
