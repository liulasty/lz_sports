<template>
  <div class="app-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="header-text">
            <span class="header-title">创建赛事</span>
            <span class="header-subtitle">填写完整信息以创建一场精彩的赛事</span>
          </div>
        </div>
      </template>

      <!-- Steps -->
      <div class="steps-wrapper">
        <div class="steps-track">
          <div
            v-for="(step, index) in steps"
            :key="index"
            class="step-item"
            :class="{
              'is-active': currentStep === index,
              'is-done': currentStep > index,
              'is-pending': currentStep < index
            }"
          >
            <div class="step-connector" v-if="index > 0" :class="{ 'done': currentStep > index }"></div>
            <div class="step-circle">
              <span v-if="currentStep <= index">{{ index + 1 }}</span>
              <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M5 12L10 17L19 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="step-label">
              <span class="step-title">{{ step.title }}</span>
              <span class="step-desc">{{ step.description }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Step 1: 赛事基本信息 -->
      <div v-show="currentStep === 0" class="step-content">
        <div class="section-title">
          <div class="section-badge">01</div>
          <h3>赛事基本信息</h3>
        </div>
        <el-form ref="step1FormRef" :model="form" :rules="rules" label-width="120px" class="styled-form">
          <div class="form-grid">
            <el-form-item label="赛事名称" prop="name" class="form-item-highlight">
              <el-input v-model="form.name" placeholder="请输入赛事名称" />
            </el-form-item>
            <el-form-item label="举办地点" prop="location">
              <el-input v-model="form.location" placeholder="请输入举办地点">
                <template #prefix>
                  <svg viewBox="0 0 24 24" fill="none" class="input-icon"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z" fill="currentColor"/></svg>
                </template>
              </el-input>
            </el-form-item>
          </div>

          <el-form-item label="赛事描述" prop="type">
            <el-input v-model="form.type" type="textarea" :rows="3" placeholder="请输入赛事描述、参赛要求等" class="textarea-styled" />
          </el-form-item>

          <div class="form-row-two">
            <div class="time-block">
              <div class="time-block-label">
                <svg viewBox="0 0 24 24" fill="none" class="label-icon"><rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/><path d="M3 9H21" stroke="currentColor" stroke-width="2"/><path d="M8 2V5M16 2V5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                报名时间 <span class="required-star">*</span>
              </div>
              <el-form-item prop="registrationTimeRange" class="no-label">
                <el-date-picker
                  v-model="form.registrationTimeRange"
                  type="datetimerange"
                  range-separator="→"
                  start-placeholder="报名开始"
                  end-placeholder="报名截止"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  @change="validateTimes"
                  style="width: 100%"
                />
              </el-form-item>
            </div>

            <div class="time-block">
              <div class="time-block-label">
                <svg viewBox="0 0 24 24" fill="none" class="label-icon"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/><path d="M12 7V12L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                比赛时间 <span class="required-star">*</span>
              </div>
              <el-form-item prop="eventTimeRange" class="no-label">
                <el-date-picker
                  v-model="form.eventTimeRange"
                  type="datetimerange"
                  range-separator="→"
                  start-placeholder="比赛开始"
                  end-placeholder="比赛结束"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  @change="validateTimes"
                  style="width: 100%"
                />
              </el-form-item>
            </div>
          </div>

          <div class="form-grid">
            <el-form-item label="报名项目上限" prop="maxItemsPerAthlete">
              <div class="number-input-wrapper">
                <el-input-number v-model="form.maxItemsPerAthlete" :min="1" :max="20" />
                <span class="tip-text">每人最多可报名的项目数量</span>
              </div>
            </el-form-item>
            <el-form-item label="封面图" prop="addImage">
              <el-input v-model="form.addImage" placeholder="请输入封面图 URL（可选）">
                <template #prefix>
                  <svg viewBox="0 0 24 24" fill="none" class="input-icon"><rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/><circle cx="8.5" cy="8.5" r="1.5" fill="currentColor"/><path d="M21 15L16 10L5 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </template>
              </el-input>
            </el-form-item>
          </div>
        </el-form>
      </div>

      <!-- Step 2: 比赛项目配置 -->
      <div v-show="currentStep === 1" class="step-content">
        <div class="section-title">
          <div class="section-badge">02</div>
          <h3>比赛项目配置</h3>
        </div>

        <div class="toolbar">
          <el-button type="primary" class="toolbar-btn" @click="openProjectLibrary">
            <svg viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/></svg>
            从项目库选择
          </el-button>
          <el-button type="success" class="toolbar-btn" @click="addCustomProject">
            <svg viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            添加自定义项目
          </el-button>
          <div class="project-count-badge" v-if="form.projects.length > 0">
            共 {{ form.projects.length }} 个项目
          </div>
        </div>

        <div v-if="form.projects.length === 0" class="empty-projects">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none"><path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2M9 5a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
          </div>
          <p>暂无项目，请从项目库选择或添加自定义项目</p>
        </div>

        <div v-else class="projects-list">
          <div
            v-for="(project, index) in form.projects"
            :key="index"
            class="project-card"
          >
            <div class="project-card-index">{{ String(index + 1).padStart(2, '0') }}</div>
            <div class="project-card-fields">
              <div class="project-field">
                <label>项目名称</label>
                <el-input v-model="project.name" placeholder="例如：男子100米" />
              </div>
              <div class="project-field project-field-sm">
                <label>人数上限</label>
                <el-input-number v-model="project.maxAttendance" :min="1" style="width: 100%" controls-position="right" />
              </div>
              <div class="project-field project-field-sm">
                <label>性别限制</label>
                <el-select v-model="project.limitation" style="width: 100%">
                  <el-option label="不限" value="ALL" />
                  <el-option label="限男" value="MALE" />
                  <el-option label="限女" value="FEMALE" />
                </el-select>
              </div>
              <div class="project-field project-field-time">
                <label>项目时间</label>
                <el-date-picker
                  v-model="project.timeRange"
                  type="datetimerange"
                  range-separator="→"
                  start-placeholder="开始"
                  end-placeholder="结束"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                />
              </div>
            </div>
            <el-button type="danger" link class="project-remove-btn" @click="removeProject(index)">
              <svg viewBox="0 0 24 24" fill="none"><path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
            </el-button>
          </div>
        </div>
      </div>

      <!-- Step 3: 指定赛事管理员 -->
      <div v-show="currentStep === 2" class="step-content">
        <div class="section-title">
          <div class="section-badge">03</div>
          <h3>指定赛事管理员</h3>
        </div>

        <div class="toolbar">
          <el-select
            v-model="selectedUser"
            filterable
            remote
            reserve-keyword
            placeholder="搜索用户（邮箱 / 姓名）"
            :remote-method="searchUsers"
            :loading="userLoading"
            style="width: 320px; margin-right: 12px;"
            value-key="id"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.id"
              :label="`${item.name} (${item.email || item.username})`"
              :value="item"
            />
          </el-select>
          <el-button type="primary" class="toolbar-btn" @click="addAdmin" :disabled="!selectedUser">
            <svg viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M20 8v6M23 11H17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/></svg>
            添加为管理员
          </el-button>
        </div>

        <div v-if="form.adminList.length === 0" class="empty-projects">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none"><circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="1.5"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
          </div>
          <p>暂未添加管理员，请通过搜索框查找并添加</p>
        </div>

        <div v-else class="admin-list">
          <div v-for="(admin, index) in form.adminList" :key="admin.id" class="admin-card">
            <div class="admin-avatar">{{ (admin.name || 'U').charAt(0).toUpperCase() }}</div>
            <div class="admin-info">
              <div class="admin-name">{{ admin.name }}</div>
              <div class="admin-email">{{ admin.email }}</div>
            </div>
            <el-tag class="admin-role-tag">
              {{ admin.type === 'EVENT_ADMIN' ? '赛事管理员' : (admin.type || '管理员') }}
            </el-tag>
            <el-button type="danger" link class="admin-remove-btn" @click="removeAdmin(index)">
              <svg viewBox="0 0 24 24" fill="none"><path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 底部操作按钮 -->
      <div class="footer-actions">
        <el-button v-if="currentStep > 0" class="btn-prev" @click="prevStep" :disabled="submitting">
          <svg viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M19 12H5M12 5L5 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          上一步
        </el-button>

        <el-button v-if="currentStep < 2" type="primary" class="btn-next" @click="nextStep">
          下一步
          <svg viewBox="0 0 24 24" fill="none" class="btn-icon-right"><path d="M5 12H19M12 5L19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </el-button>

        <template v-if="currentStep === 2">
          <el-button class="btn-draft" @click="submitEvent('DRAFT')" :loading="submitting" :disabled="submitting">
            <svg v-if="!submitting" viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><polyline points="17 21 17 13 7 13 7 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><polyline points="7 3 7 8 15 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            保存草稿
          </el-button>
          <el-button class="btn-publish" @click="submitEvent('OPEN')" :loading="submitting" :disabled="submitting">
            <svg v-if="!submitting" viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M22 2L11 13M22 2L15 22L11 13L2 9L22 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            直接发布
          </el-button>
        </template>
      </div>
    </el-card>

    <!-- 项目库选择弹窗 -->
    <el-dialog v-model="libraryVisible" title="从项目库选择" width="60%" class="styled-dialog">
      <el-table
        :data="libraryProjects"
        v-loading="libraryLoading"
        @selection-change="handleLibrarySelection"
        border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="itemName" label="项目名称" />
        <el-table-column prop="category" label="分类">
          <template #default="scope">
            <el-tag :type="scope.row.category === 'STANDARD' ? 'success' : 'info'">
              {{ scope.row.category === 'STANDARD' ? '标准项目' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="limitation" label="默认性别限制">
          <template #default="scope">
            {{ {ALL: '不限', MALE: '限男', FEMALE: '限女'}[scope.row.limitation] || scope.row.limitation }}
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container" style="margin-top: 15px; text-align: right;">
        <el-pagination
          v-model:current-page="libQuery.currentPage"
          v-model:page-size="libQuery.pageSize"
          layout="prev, pager, next"
          :total="libTotal"
          @current-change="fetchLibraryProjects"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="libraryVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmLibrarySelection">确定添加</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addEvent, changeEventStatus } from '@/api/event'
import { addProject, getProjectList } from '@/api/project'
import { getAdminUserList } from '@/api/adminUser'

const router = useRouter()
const currentStep = ref(0)
const step1FormRef = ref(null)
const submitting = ref(false)

const steps = [
  { title: '基本信息', description: '设置赛事名称与时间' },
  { title: '项目配置', description: '选择并配置比赛项目' },
  { title: '指定管理员', description: '分配赛事管理员' }
]

const form = reactive({
  name: '',
  type: '',
  location: '',
  registrationTimeRange: [],
  eventTimeRange: [],
  maxItemsPerAthlete: 3,
  addImage: '',
  projects: [],
  adminList: []
})

const validateTimes = () => {
  if (form.registrationTimeRange?.length === 2 && form.eventTimeRange?.length === 2) {
    const regStart = new Date(form.registrationTimeRange[0]).getTime()
    const regEnd = new Date(form.registrationTimeRange[1]).getTime()
    const eventStart = new Date(form.eventTimeRange[0]).getTime()
    const eventEnd = new Date(form.eventTimeRange[1]).getTime()
    if (regEnd <= regStart) {
      ElMessage.warning('报名截止时间必须晚于报名开始时间')
      form.registrationTimeRange = []
      return false
    }
    if (eventStart <= regEnd) {
      ElMessage.warning('比赛开始时间必须晚于报名截止时间')
      form.eventTimeRange = []
      return false
    }
    if (eventEnd <= eventStart) {
      ElMessage.warning('比赛结束时间必须晚于比赛开始时间')
      form.eventTimeRange = []
      return false
    }
  }
  return true
}

const rules = {
  name: [{ required: true, message: '请输入赛事名称', trigger: 'blur' }],
  registrationTimeRange: [{ required: true, message: '请选择报名时间范围', trigger: 'change' }],
  eventTimeRange: [{ required: true, message: '请选择比赛时间范围', trigger: 'change' }],
  maxItemsPerAthlete: [{ required: true, message: '请设置报名上限', trigger: 'blur' }]
}

const prevStep = () => { currentStep.value-- }

const nextStep = async () => {
  if (currentStep.value === 0) {
    if (!step1FormRef.value) return
    await step1FormRef.value.validate((valid) => {
      if (valid && validateTimes()) {
        currentStep.value++
      }
    })
  } else if (currentStep.value === 1) {
    if (form.projects.length === 0) {
      ElMessage.warning('至少需要配置1个比赛项目')
      return
    }
    for (let i = 0; i < form.projects.length; i++) {
      const p = form.projects[i]
      if (!p.name) { ElMessage.warning(`第 ${i + 1} 个项目未填写名称`); return }
      if (!p.timeRange || p.timeRange.length !== 2) { ElMessage.warning(`项目【${p.name}】未完整设置比赛时间`); return }
      const pStart = new Date(p.timeRange[0]).getTime()
      const pEnd = new Date(p.timeRange[1]).getTime()
      const eStart = new Date(form.eventTimeRange[0]).getTime()
      const eEnd = new Date(form.eventTimeRange[1]).getTime()
      if (pStart < eStart || pEnd > eEnd) { ElMessage.warning(`项目【${p.name}】的时间超出了赛事总时间范围`); return }
    }
    currentStep.value++
  }
}

const libraryVisible = ref(false)
const libraryLoading = ref(false)
const libraryProjects = ref([])
const libTotal = ref(0)
const libQuery = reactive({ currentPage: 1, pageSize: 10 })
let selectedLibraryItems = []

const openProjectLibrary = () => {
  libraryVisible.value = true
  fetchLibraryProjects()
}

const fetchLibraryProjects = async () => {
  libraryLoading.value = true
  try {
    const res = await getProjectList(libQuery)
    if (res.code === 200) {
      libraryProjects.value = res.data.records
      libTotal.value = res.data.total
    }
  } finally {
    libraryLoading.value = false
  }
}

const handleLibrarySelection = (val) => { selectedLibraryItems = val }

const confirmLibrarySelection = () => {
  selectedLibraryItems.forEach(item => {
    form.projects.push({
      name: item.itemName,
      maxAttendance: item.maxAttendance || 50,
      limitation: item.limitation || 'ALL',
      timeRange: [...form.eventTimeRange]
    })
  })
  libraryVisible.value = false
  selectedLibraryItems = []
}

const addCustomProject = () => {
  form.projects.push({ name: '', maxAttendance: 50, limitation: 'ALL', timeRange: [...form.eventTimeRange] })
}

const removeProject = (index) => { form.projects.splice(index, 1) }

const userOptions = ref([])
const userLoading = ref(false)
const selectedUser = ref(null)

const searchUsers = async (query) => {
  if (query) {
    userLoading.value = true
    try {
      const res = await getAdminUserList({ keyword: query, page: 1, size: 20 })
      if (res.code === 200) userOptions.value = res.data.records
    } finally {
      userLoading.value = false
    }
  } else {
    userOptions.value = []
  }
}

const addAdmin = () => {
  if (selectedUser.value) {
    if (form.adminList.find(u => u.id === selectedUser.value.id)) {
      ElMessage.warning('该用户已在列表中')
      return
    }
    form.adminList.push(selectedUser.value)
    selectedUser.value = null
    userOptions.value = []
  }
}

const removeAdmin = (index) => { form.adminList.splice(index, 1) }

const submitEvent = async (targetStatus) => {
  if (submitting.value) return
  if (form.adminList.length === 0) {
    ElMessage.warning('至少需要指定1名赛事管理员')
    return
  }
  submitting.value = true
  try {
    const eventPayload = {
      name: form.name,
      type: form.type + (form.location ? `\n地点：${form.location}` : ''),
      fee: '0',
      registrationStartTime: form.registrationTimeRange[0],
      registrationEndTime: form.registrationTimeRange[1],
      eventStartTime: form.eventTimeRange[0],
      eventEndTime: form.eventTimeRange[1],
      maxItemsPerAthlete: form.maxItemsPerAthlete,
      adminIds: form.adminList.map(u => u.id),
      addImage: form.addImage ? [form.addImage] : [],
      projects: form.projects.map(p => ({
        name: p.name,
        maxAttendance: p.maxAttendance,
        limitation: p.limitation,
        category: 'CUSTOM',
        startTime: p.timeRange[0],
        endTime: p.timeRange[1]
      }))
    }
    const eventRes = await addEvent(eventPayload)
    if (eventRes.code !== 200) throw new Error(eventRes.msg || '赛事创建失败')
    const eventId = parseInt(eventRes.data)
    if (!eventId || isNaN(eventId)) throw new Error('未获取到新建赛事的ID，请检查后端返回值是否为ID')
    if (targetStatus === 'OPEN') {
      await changeEventStatus(eventId, 'OPEN')
      ElMessage.success('赛事创建并发布成功')
    } else {
      ElMessage.success('草稿保存成功')
    }
    router.push('/event-manage')
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
/* ===================== Layout ===================== */
.app-container {
  padding: 24px;
  min-height: 100vh;
}

/* ===================== Card ===================== */
.main-card {
  border-radius: 16px !important;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06) !important;
  border: 1px solid var(--el-border-color-lighter) !important;
  overflow: hidden;
}

/* ===================== Card Header ===================== */
.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 4px 0;
}
.header-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-light-3));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb, 64, 158, 255), 0.3);
}
.header-icon svg {
  width: 22px;
  height: 22px;
}
.header-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.header-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  letter-spacing: -0.3px;
}
.header-subtitle {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* ===================== Custom Steps ===================== */
.steps-wrapper {
  margin: 8px 0 36px;
  padding: 24px 32px;
  background: var(--el-fill-color-lighter);
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);
}
.steps-track {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  position: relative;
}
.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  flex: 1;
  position: relative;
}
.step-connector {
  position: absolute;
  top: 18px;
  right: calc(50% + 26px);
  left: calc(-50% + 26px);
  height: 2px;
  background: var(--el-border-color);
  transition: background 0.4s ease;
}
.step-connector.done {
  background: var(--el-color-primary);
}
.step-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  border: 2px solid var(--el-border-color);
  background: var(--el-bg-color);
  color: var(--el-text-color-secondary);
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
}
.step-circle svg {
  width: 16px;
  height: 16px;
}
.step-item.is-active .step-circle {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary);
  color: #fff;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.15);
}
.step-item.is-done .step-circle {
  border-color: var(--el-color-success);
  background: var(--el-color-success);
  color: #fff;
}
.step-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  text-align: center;
}
.step-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-regular);
  transition: color 0.3s;
}
.step-desc {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}
.step-item.is-active .step-title {
  color: var(--el-color-primary);
}
.step-item.is-done .step-title {
  color: var(--el-color-success);
}

/* ===================== Section Title ===================== */
.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}
.section-badge {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 1px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-5);
  padding: 3px 8px;
  border-radius: 6px;
}
.section-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

/* ===================== Step Content ===================== */
.step-content {
  max-width: 940px;
  margin: 0 auto;
  padding: 8px 0 24px;
  min-height: 400px;
}

/* ===================== Form Styles ===================== */
.styled-form {
  background: transparent;
}
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 24px;
}
.form-item-highlight :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--el-color-primary-light-5) inset !important;
}
.textarea-styled :deep(.el-textarea__inner) {
  resize: vertical;
  min-height: 80px !important;
}
.input-icon {
  width: 15px;
  height: 15px;
  color: var(--el-text-color-placeholder);
}
.number-input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}
.tip-text {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  white-space: nowrap;
}
.required-star {
  color: var(--el-color-danger);
  margin-left: 2px;
}
.no-label :deep(.el-form-item__label) {
  display: none;
}
.no-label :deep(.el-form-item__content) {
  margin-left: 0 !important;
}

/* ===================== Time blocks ===================== */
.form-row-two {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 8px;
}
.time-block {
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  padding: 14px 16px 10px;
  transition: border-color 0.2s;
}
.time-block:hover {
  border-color: var(--el-color-primary-light-5);
}
.time-block-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-regular);
  margin-bottom: 10px;
}
.label-icon {
  width: 15px;
  height: 15px;
  color: var(--el-color-primary);
  flex-shrink: 0;
}
.time-block :deep(.el-form-item) {
  margin-bottom: 0;
}

/* ===================== Toolbar ===================== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}
.btn-icon {
  width: 15px;
  height: 15px;
}
.project-count-badge {
  margin-left: auto;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  padding: 4px 12px;
  border-radius: 20px;
  border: 1px solid var(--el-border-color-lighter);
}

/* ===================== Empty State ===================== */
.empty-projects {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 14px;
  border: 2px dashed var(--el-border-color);
  border-radius: 12px;
  background: var(--el-fill-color-lighter);
}
.empty-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: var(--el-fill-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-placeholder);
}
.empty-icon svg {
  width: 28px;
  height: 28px;
}
.empty-projects p {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-placeholder);
}

/* ===================== Project Cards ===================== */
.projects-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.project-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 18px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.project-card:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.project-card-index {
  font-size: 20px;
  font-weight: 800;
  color: var(--el-color-primary-light-5);
  min-width: 32px;
  line-height: 36px;
  letter-spacing: -1px;
}
.project-card-fields {
  flex: 1;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 2.5fr;
  gap: 12px;
  align-items: end;
}
.project-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.project-field label {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
}
.project-remove-btn {
  width: 32px !important;
  height: 32px !important;
  padding: 0 !important;
  margin-top: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px !important;
  color: var(--el-color-danger-light-3) !important;
  transition: background 0.2s, color 0.2s;
}
.project-remove-btn:hover {
  background: var(--el-color-danger-light-9) !important;
  color: var(--el-color-danger) !important;
}
.project-remove-btn svg {
  width: 16px;
  height: 16px;
}

/* ===================== Admin Cards ===================== */
.admin-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.admin-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.admin-card:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.admin-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--el-color-primary-light-3), var(--el-color-primary));
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.admin-info {
  flex: 1;
  min-width: 0;
}
.admin-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.admin-email {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.admin-role-tag {
  flex-shrink: 0;
}
.admin-remove-btn {
  width: 32px !important;
  height: 32px !important;
  padding: 0 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px !important;
  color: var(--el-color-danger-light-3) !important;
  transition: background 0.2s, color 0.2s;
}
.admin-remove-btn:hover {
  background: var(--el-color-danger-light-9) !important;
  color: var(--el-color-danger) !important;
}
.admin-remove-btn svg {
  width: 16px;
  height: 16px;
}

/* ===================== Footer Actions ===================== */
.footer-actions {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-lighter);
}
.btn-prev {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px !important;
  height: auto !important;
  border-radius: 10px !important;
}
.btn-next {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 28px !important;
  height: auto !important;
  border-radius: 10px !important;
  font-weight: 600 !important;
  background: linear-gradient(135deg, var(--el-color-primary-light-3), var(--el-color-primary)) !important;
  border: none !important;
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.35) !important;
  transition: transform 0.15s, box-shadow 0.15s !important;
}
.btn-next:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.45) !important;
}
.btn-icon-right {
  width: 15px;
  height: 15px;
}
.btn-draft {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px !important;
  height: auto !important;
  border-radius: 10px !important;
  font-weight: 600 !important;
  background: var(--el-fill-color) !important;
  border: 1px solid var(--el-border-color) !important;
  color: var(--el-text-color-regular) !important;
  transition: background 0.2s, border-color 0.2s !important;
}
.btn-draft:hover {
  background: var(--el-fill-color-dark) !important;
  border-color: var(--el-border-color-dark) !important;
}
.btn-publish {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 28px !important;
  height: auto !important;
  border-radius: 10px !important;
  font-weight: 600 !important;
  background: linear-gradient(135deg, #34d399, #10b981) !important;
  border: none !important;
  color: #fff !important;
  box-shadow: 0 4px 14px rgba(16, 185, 129, 0.35) !important;
  transition: transform 0.15s, box-shadow 0.15s !important;
}
.btn-publish:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(16, 185, 129, 0.45) !important;
}

/* ===================== Table overrides ===================== */
:deep(.el-table) {
  background: var(--el-bg-color) !important;
  color: var(--el-text-color-primary);
  border-radius: 10px;
  overflow: hidden;
}
:deep(.el-table__inner-wrapper) {
  background: var(--el-bg-color) !important;
}
:deep(.el-table__body-wrapper td) {
  background: var(--el-bg-color) !important;
}

/* ===================== Responsive ===================== */
@media (max-width: 768px) {
  .form-grid,
  .form-row-two {
    grid-template-columns: 1fr;
  }
  .project-card-fields {
    grid-template-columns: 1fr 1fr;
  }
  .steps-wrapper {
    padding: 16px;
  }
  .step-desc {
    display: none;
  }
}
</style>