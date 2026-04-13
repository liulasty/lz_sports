<template>
  <div class="school-page">
    <div class="school-hero">
      <div>
        <p class="school-eyebrow">SCHOOL BRANDING</p>
        <h1 class="school-title">学校个性化配置</h1>
        <p class="school-sub">配置学校名称、Logo 与主题色。主题色会实时预览并影响全站主色。</p>
      </div>
      <div class="hero-actions lz-actions lz-ep-dark">
        <el-button @click="resetForm">重置</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting" :disabled="submitting">保存配置</el-button>
      </div>
    </div>

    <section class="school-grid">
      <!-- 预览卡 -->
      <article class="panel lz-surface">
        <div class="panel-head">
          <div>
            <div class="panel-title">预览</div>
            <div class="panel-sub">当前配置在导航栏与登录页的展示效果</div>
          </div>
          <div class="badge" :style="{ borderColor: form.themeColor, color: form.themeColor }">
            PRIMARY
          </div>
        </div>

        <div class="preview">
          <div class="preview-logo">
            <img v-if="form.logoUrl" :src="form.logoUrl" alt="logo" />
            <div v-else class="preview-logo-placeholder" :style="{ borderColor: form.themeColor, color: form.themeColor }">LOGO</div>
          </div>
          <div class="preview-meta">
            <div class="preview-name">{{ form.schoolName || '未设置学校名称' }}</div>
            <div class="preview-color">
              <span class="dot" :style="{ background: form.themeColor }"></span>
              <span>主题色：{{ form.themeColor }}</span>
            </div>
          </div>
        </div>

        <div class="preview-actions lz-actions lz-ep-dark">
          <el-button @click="applyThemeColor(form.themeColor)">重新应用主题色</el-button>
          <el-button @click="loadConfig">重新加载配置</el-button>
        </div>
      </article>

      <!-- 配置表单 -->
      <article class="panel lz-surface lz-ep-dark">
        <div class="panel-head">
          <div>
            <div class="panel-title">基础配置</div>
            <div class="panel-sub">学校名称、Logo 与主题色</div>
          </div>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="lz-form"
        >
          <el-form-item label="学校名称" prop="schoolName">
            <el-input v-model="form.schoolName" placeholder="请输入学校名称" />
          </el-form-item>

          <el-form-item label="学校 Logo" prop="logoUrl">
            <div class="upload-row">
              <el-upload
                class="logo-uploader"
                action="#"
                :show-file-list="false"
                :http-request="handleLogoUpload"
                :before-upload="beforeLogoUpload"
              >
                <img v-if="form.logoUrl" :src="form.logoUrl" class="logo" alt="logo" />
                <div v-else class="logo-placeholder">
                  <el-icon><Plus /></el-icon>
                  <span>上传 Logo</span>
                </div>
              </el-upload>
              <div class="upload-tip">
                支持 jpg/png/gif 格式，大小不超过 5MB。上传后会触发全局刷新（导航栏/登录页）。
              </div>
            </div>
          </el-form-item>

          <el-form-item label="主题色" prop="themeColor">
            <div class="color-row">
              <el-color-picker v-model="form.themeColor" :predefine="predefineColors" @change="handleColorChange" />
              <div class="upload-tip">选择主题色后页面将实时预览</div>
            </div>
          </el-form-item>
        </el-form>
      </article>

      <!-- 危险操作 -->
      <article class="panel danger lz-surface">
        <div class="panel-head">
          <div>
            <div class="panel-title danger-title">危险操作</div>
            <div class="panel-sub">重置系统初始化状态与基础数据</div>
          </div>
        </div>

        <div class="danger-body">
          <div class="danger-text">
            <h3>重置系统基本信息</h3>
            <p>此操作将清空组织架构等基础数据并重置系统初始化状态，随后返回初始化引导页。</p>
          </div>
          <div class="lz-actions lz-ep-dark">
            <el-button type="danger" @click="handleResetSystem">重置系统</el-button>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSchoolConfig } from '@/api/init'
import { updateSchoolConfig, uploadSchoolLogo, resetSystem } from '@/api/schoolConfig'
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

const predefineColors = ref(['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#FF6B35'])

const rules = {
  schoolName: [
    { required: true, message: '请输入学校名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  themeColor: [{ required: true, message: '请选择主题色', trigger: 'change' }]
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
    document.title = form.schoolName || '体育赛事管理系统'
  }
}

const handleColorChange = (val) => {
  applyThemeColor(val)
}

const beforeLogoUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('上传 Logo 只能是 JPG/PNG/GIF 格式!')
  if (!isLt5M) ElMessage.error('上传 Logo 大小不能超过 5MB!')
  return isImage && isLt5M
}

const handleLogoUpload = async (options) => {
  if (submitting.value) return
  submitting.value = true
  try {
    const res = await uploadSchoolLogo(options.file)
    if (res.code === 200) {
      form.logoUrl = res.data
      ElMessage.success('Logo 上传成功')
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
    if (!valid) return
    submitting.value = true
    try {
      const res = await updateSchoolConfig({
        schoolName: form.schoolName,
        themeColor: form.themeColor
      })
      if (res.code === 200) {
        ElMessage.success('配置保存成功')
        Object.assign(originalConfig, form)
        window.dispatchEvent(new Event('schoolConfigUpdated'))
      }
    } catch (error) {
      console.error(error)
    } finally {
      submitting.value = false
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
  )
    .then(async () => {
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
    })
    .catch(() => {})
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.school-page {
  padding: 24px;
}

.school-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.school-eyebrow {
  margin: 0 0 10px;
  color: var(--accent);
  letter-spacing: 0.18em;
  font-size: 11px;
  font-weight: 700;
}

.school-title {
  margin: 0 0 8px;
  font-size: 32px;
  color: var(--text-primary);
}

.school-sub {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.school-grid {
  display: grid;
  grid-template-columns: 1.1fr 1.4fr;
  gap: 12px;
}

.panel {
  padding: 14px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-title {
  font-size: 15px;
  font-weight: 800;
  color: var(--text-primary);
}

.panel-sub {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.badge {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
  border: 1px solid var(--border);
  padding: 6px 10px;
  border-radius: 999px;
}

.preview {
  display: flex;
  gap: 12px;
  align-items: center;
  border: 1px solid var(--border);
  background: color-mix(in srgb, var(--bg-card) 92%, var(--bg-soft));
  border-radius: 14px;
  padding: 14px;
}

.preview-logo {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  background: color-mix(in srgb, var(--bg-soft) 70%, transparent);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.preview-logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.preview-logo-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  border: 1px dashed var(--border);
}

.preview-name {
  font-size: 16px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.preview-color {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--border) 70%, transparent);
}

.preview-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}

.upload-row {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 12px;
  align-items: center;
}

.logo-uploader {
  width: 120px;
  height: 120px;
}

.logo {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: contain;
  border-radius: 10px;
}

.logo-placeholder {
  width: 120px;
  height: 120px;
  border-radius: 10px;
  border: 1px dashed var(--border);
  background: color-mix(in srgb, var(--bg-soft) 70%, transparent);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--text-secondary);
}

.logo-placeholder span {
  font-size: 12px;
  font-weight: 700;
}

.upload-tip {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.color-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.danger {
  grid-column: 1 / -1;
  border: 1px solid color-mix(in srgb, #f43f5e 28%, var(--border));
  background: color-mix(in srgb, #f43f5e 6%, var(--bg-card));
}

.danger-title {
  color: #f43f5e;
}

.danger-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
}

.danger-text h3 {
  margin: 0 0 8px;
  font-size: 15px;
  color: var(--text-primary);
}

.danger-text p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 1024px) {
  .school-grid {
    grid-template-columns: 1fr;
  }
  .upload-row {
    grid-template-columns: 120px 1fr;
  }
}

@media (max-width: 700px) {
  .school-hero {
    flex-direction: column;
    align-items: flex-start;
  }
  .upload-row {
    grid-template-columns: 1fr;
  }
  .danger-body {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

