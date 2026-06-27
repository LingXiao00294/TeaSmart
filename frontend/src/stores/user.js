import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  function setLogin(data) {
    token.value = data.token
    userInfo.value = data.user
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.user))
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const isAdmin = () => userInfo.value?.role === 'ADMIN'

  return { token, userInfo, setLogin, setUserInfo, logout, isAdmin }
})
