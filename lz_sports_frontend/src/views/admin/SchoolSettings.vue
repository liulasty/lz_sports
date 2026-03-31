<template>
  <div class="school-settings-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>学校个性化配置</span>
        </div>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" class="settings-form">
        <el-form-item label="学校名称" prop="schoolName">
          <el-input v-model="form.schoolName" placeholder="请输入学校名称" />
        </el-form-item>

        <el-form-item label="学校 Logo" prop="logoUrl">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :http-request="handleLogoUpload"
            :before-upload="beforeLogoUpload"
          >
            <img v-if="form.logoUrl" :src="form.logoUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持 jpg/png/gif 格式，大小不超过 5MB</div>
        </el-form-item>

        <el-form-item label="主题色" prop="themeColor">
          <el-color-picker v-model="form.themeColor" :predefine="predefineColors" @change="handleColorChange" />
          <div class="upload-tip" style="margin-left: 10px;">选择主题色后页面将实时预览</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting" :disabled="submitting">保存配置</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header" style="color: var(--el-color-danger)">
          <span>危险操作</span>
        </div>
      </template>
      <div style="display: flex; align-items: center; justify-content: space-between;">
        <div>
          <h4 style="margin: 0 0 10px 0;">重置系统基本信息</h4>
          <p style="margin: 0; color: var(--el-text-color-secondary); font-size: 14px;">此操作将清空组织架构等基础数据并重置系统初始化状态，返回初始化引导页。</p>
        </div>
        <el-button type="danger" @click="handleResetSystem">重置系统</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSchoolConfig } from '@/api/init'
import { updateSchoolConfig, uploadSchoolLogo, resetSystem } from '@/api/schoolConfig'
import { useConfigStore } from '@/stores/config' // We will create this or just use window.document
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  schoolName: '',
  logoUrl: '',
  themeColor: '#409EFF'
})

const originalConfig = reactive({
  schoolName: '',
  logoUrl: '',
  themeColor: ''
})

const predefineColors = ref([
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399',
  '#FF6B35'
])

const rules = {
  schoolName: [
    { required: true, message: '请输入学校名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  themeColor: [
    { required: true, message: '请选择主题色', trigger: 'change' }
  ]
}

const loadConfig = async () => {
  try {
    const res = await getSchoolConfig()
    if (res.code === 200 && res.data) {
      form.schoolName = res.data.schoolName
      form.logoUrl = res.data.logoUrl
      form.themeColor = res.data.themeColor || '#409EFF'
      
      Object.assign(originalConfig, form)
      applyThemeColor(form.themeColor)
    }
  } catch (error) {
    console.error('Failed to load config', error)
  }
}

const applyThemeColor = (color) => {
  if (color) {
    document.documentElement.style.setProperty('--el-color-primary', color)
    // 更新导航栏标题
    document.title = form.schoolName || '体育赛事管理系统'
  }
}

const handleColorChange = (val) => {
  applyThemeColor(val)
}

const beforeLogoUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('上传 Logo 只能是 JPG/PNG/GIF 格式!')
  }
  if (!isLt5M) {
    ElMessage.error('上传 Logo 大小不能超过 5MB!')
  }
  return isImage && isLt5M
}

const handleLogoUpload = async (options) => {
  if (submitting.value) return
  submitting.value = true
  try {
    const res = await uploadSchoolLogo(options.file)
    if (res.code === 200) {
      form.logoUrl = res.data
      ElMessage.success('Logo上传成功')
      // 触发全局更新
      window.dispatchEvent(new Event('schoolConfigUpdated'))
    }
  } catch (error) {
    console.error('Upload failed', error)
  } finally {
    submitting.value = false
  }
}

const submitForm = async () => {
  if (submitting.value) return
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const res = await updateSchoolConfig({
          schoolName: form.schoolName,
          themeColor: form.themeColor
        })
        if (res.code === 200) {
          ElMessage.success('配置保存成功')
          Object.assign(originalConfig, form)
          // 触发全局更新
          window.dispatchEvent(new Event('schoolConfigUpdated'))
        }
      } catch (error) {
        console.error(error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const resetForm = () => {
  Object.assign(form, originalConfig)
  applyThemeColor(form.themeColor)
}

const handleResetSystem = () => {
  ElMessageBox.confirm(
    '此操作将清空所有部门架构及非超管用户，并重置系统初始化状态。是否继续？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await resetSystem()
      if (res.code === 200) {
        ElMessage.success('系统已重置')
        userStore.logout()
        router.push('/init')
      }
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {
    // cancelled
  })
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.school-settings-container {
  padding: 20px;
}

.settings-form {
  max-width: 600px;
  margin-top: 20px;
}

.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: contain;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: var(--el-text-color-placeholder);
  width: 120px;
  height: 120px;
  text-align: center;
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 8px;
}
</style>
