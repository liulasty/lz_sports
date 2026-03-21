import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getSchoolConfig } from '@/api/init'

export const useConfigStore = defineStore('config', () => {
  const schoolName = ref('体育赛事管理系统')
  const logoUrl = ref('')
  const themeColor = ref('#409EFF')

  const fetchConfig = async () => {
    try {
      const res = await getSchoolConfig()
      if (res.code === 200 && res.data) {
        schoolName.value = res.data.schoolName || '体育赛事管理系统'
        logoUrl.value = res.data.logoUrl
        themeColor.value = res.data.themeColor || '#409EFF'
        
        document.documentElement.style.setProperty('--el-color-primary', themeColor.value)
        document.title = schoolName.value
      }
    } catch (error) {
      console.error('Failed to fetch school config', error)
    }
  }

  return { schoolName, logoUrl, themeColor, fetchConfig }
})
