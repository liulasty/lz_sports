import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(normalizeUserInfo(JSON.parse(localStorage.getItem('userInfo') || '{}')))

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function normalizeUserInfo(info = {}) {
    const normalized = { ...info }
    const role = normalized.role || normalized.type || normalized.userType
    normalized.role = role || ''
    delete normalized.type
    delete normalized.userType
    return normalized
  }

  function setUserInfo(info) {
    const normalized = normalizeUserInfo(info)
    userInfo.value = normalized
    localStorage.setItem('userInfo', JSON.stringify(normalized))
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, setToken, setUserInfo, logout }
})
