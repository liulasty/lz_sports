<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创建赛事</span>
        </div>
      </template>

      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 40px;">
        <el-step title="基本信息" description="设置赛事名称与时间" />
        <el-step title="项目配置" description="选择并配置比赛项目" />
        <el-step title="指定管理员" description="分配赛事管理员" />
      </el-steps>

      <!-- Step 1: 赛事基本信息 -->
      <div v-show="currentStep === 0" class="step-content">
        <el-form ref="step1FormRef" :model="form" :rules="rules" label-width="120px">
          <el-form-item label="赛事名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入赛事名称" />
          </el-form-item>
          <el-form-item label="赛事描述" prop="type">
            <el-input v-model="form.type" type="textarea" :rows="3" placeholder="请输入赛事描述、参赛要求等" />
          </el-form-item>
          <el-form-item label="举办地点" prop="location">
            <el-input v-model="form.location" placeholder="请输入举办地点" />
          </el-form-item>
          <el-form-item label="报名时间" prop="registrationTimeRange" required>
            <el-date-picker
              v-model="form.registrationTimeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="报名开始时间"
              end-placeholder="报名截止时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="validateTimes"
            />
          </el-form-item>
          <el-form-item label="比赛时间" prop="eventTimeRange" required>
            <el-date-picker
              v-model="form.eventTimeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="比赛开始时间"
              end-placeholder="比赛结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="validateTimes"
            />
          </el-form-item>
          <el-form-item label="报名项目上限" prop="maxItemsPerAthlete">
            <el-input-number v-model="form.maxItemsPerAthlete" :min="1" :max="20" />
            <span class="tip-text">每人最多可报名的项目数量</span>
          </el-form-item>
          <el-form-item label="封面图" prop="addImage">
            <el-input v-model="form.addImage" placeholder="请输入封面图 URL（可选）" />
          </el-form-item>
        </el-form>
      </div>

      <!-- Step 2: 比赛项目配置 -->
      <div v-show="currentStep === 1" class="step-content">
        <div class="toolbar">
          <el-button type="primary" @click="openProjectLibrary">从项目库选择</el-button>
          <el-button type="success" @click="addCustomProject">添加自定义项目</el-button>
        </div>
        
        <el-table :data="form.projects" style="width: 100%; margin-top: 15px" border>
          <el-table-column label="项目名称" min-width="150">
            <template #default="scope">
              <el-input v-model="scope.row.name" placeholder="例如：男子100米" />
            </template>
          </el-table-column>
          <el-table-column label="人数上限" width="120">
            <template #default="scope">
              <el-input-number v-model="scope.row.maxAttendance" :min="1" style="width: 100%" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="性别限制" width="120">
            <template #default="scope">
              <el-select v-model="scope.row.limitation">
                <el-option label="不限" value="ALL" />
                <el-option label="限男" value="MALE" />
                <el-option label="限女" value="FEMALE" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="项目时间" min-width="350">
            <template #default="scope">
              <el-date-picker
                v-model="scope.row.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始"
                end-placeholder="结束"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="scope">
              <el-button type="danger" link @click="removeProject(scope.$index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Step 3: 指定赛事管理员 -->
      <div v-show="currentStep === 2" class="step-content">
        <div class="toolbar">
          <el-select
            v-model="selectedUser"
            filterable
            remote
            reserve-keyword
            placeholder="请输入邮箱或姓名搜索用户"
            :remote-method="searchUsers"
            :loading="userLoading"
            style="width: 300px; margin-right: 10px;"
            value-key="id"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.id"
              :label="`${item.name} (${item.email || item.username})`"
              :value="item"
            />
          </el-select>
          <el-button type="primary" @click="addAdmin" :disabled="!selectedUser">添加为管理员</el-button>
        </div>

        <el-table :data="form.adminList" style="width: 100%; margin-top: 15px" border>
          <el-table-column prop="name" label="姓名" width="150" />
          <el-table-column prop="email" label="邮箱" min-width="200" />
          <el-table-column prop="type" label="角色" width="150">
            <template #default="scope">
              <el-tag>{{ scope.row.type === 'EVENT_ADMIN' ? '赛事管理员' : scope.row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button type="danger" link @click="removeAdmin(scope.$index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 底部操作按钮 -->
      <div class="footer-actions">
        <el-button v-if="currentStep > 0" @click="prevStep" :disabled="submitting">上一步</el-button>
        <el-button v-if="currentStep < 2" type="primary" @click="nextStep">下一步</el-button>
        
        <template v-if="currentStep === 2">
          <el-button type="warning" @click="submitEvent('DRAFT')" :loading="submitting" :disabled="submitting">保存草稿</el-button>
          <el-button type="success" @click="submitEvent('OPEN')" :loading="submitting" :disabled="submitting">直接发布</el-button>
        </template>
      </div>
    </el-card>

    <!-- 项目库选择弹窗 -->
    <el-dialog v-model="libraryVisible" title="从项目库选择" width="60%">
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

// 表单数据
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

// 时间校验逻辑
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

// Step 1 规则
const rules = {
  name: [{ required: true, message: '请输入赛事名称', trigger: 'blur' }],
  registrationTimeRange: [{ required: true, message: '请选择报名时间范围', trigger: 'change' }],
  eventTimeRange: [{ required: true, message: '请选择比赛时间范围', trigger: 'change' }],
  maxItemsPerAthlete: [{ required: true, message: '请设置报名上限', trigger: 'blur' }]
}

// 导航控制
const prevStep = () => {
  currentStep.value--
}

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
    // 校验项目数据
    for (let i = 0; i < form.projects.length; i++) {
      const p = form.projects[i]
      if (!p.name) {
        ElMessage.warning(`第 ${i + 1} 个项目未填写名称`)
        return
      }
      if (!p.timeRange || p.timeRange.length !== 2) {
        ElMessage.warning(`项目【${p.name}】未完整设置比赛时间`)
        return
      }
      // 校验项目时间是否在赛事范围内
      const pStart = new Date(p.timeRange[0]).getTime()
      const pEnd = new Date(p.timeRange[1]).getTime()
      const eStart = new Date(form.eventTimeRange[0]).getTime()
      const eEnd = new Date(form.eventTimeRange[1]).getTime()
      if (pStart < eStart || pEnd > eEnd) {
        ElMessage.warning(`项目【${p.name}】的时间超出了赛事总时间范围`)
        return
      }
    }
    currentStep.value++
  }
}

// --- 项目配置逻辑 ---
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

const handleLibrarySelection = (val) => {
  selectedLibraryItems = val
}

const confirmLibrarySelection = () => {
  selectedLibraryItems.forEach(item => {
    form.projects.push({
      name: item.itemName,
      maxAttendance: item.maxAttendance || 50,
      limitation: item.limitation || 'ALL',
      timeRange: [...form.eventTimeRange] // 默认等于赛事时间
    })
  })
  libraryVisible.value = false
  selectedLibraryItems = []
}

const addCustomProject = () => {
  form.projects.push({
    name: '',
    maxAttendance: 50,
    limitation: 'ALL',
    timeRange: [...form.eventTimeRange]
  })
}

const removeProject = (index) => {
  form.projects.splice(index, 1)
}

// --- 管理员配置逻辑 ---
const userOptions = ref([])
const userLoading = ref(false)
const selectedUser = ref(null)

const searchUsers = async (query) => {
  if (query) {
    userLoading.value = true
    try {
      const res = await getAdminUserList({ keyword: query, page: 1, size: 20 })
      if (res.code === 200) {
        userOptions.value = res.data.records
      }
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

const removeAdmin = (index) => {
  form.adminList.splice(index, 1)
}

// --- 最终提交逻辑 ---
const submitEvent = async (targetStatus) => {
  if (submitting.value) return
  if (form.adminList.length === 0) {
    ElMessage.warning('至少需要指定1名赛事管理员')
    return
  }

  submitting.value = true
  try {
    // 1. 创建赛事及项目
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
    if (eventRes.code !== 200) {
      throw new Error(eventRes.msg || '赛事创建失败')
    }
    
    // 解析返回的ID
    const eventId = parseInt(eventRes.data)
    if (!eventId || isNaN(eventId)) {
       throw new Error('未获取到新建赛事的ID，请检查后端返回值是否为ID')
    }

    // 2. 如果是直接发布，调用发布接口
    if (targetStatus === 'OPEN') {
      await changeEventStatus(eventId, 'OPEN')
      ElMessage.success('赛事创建并发布成功')
    } else {
      ElMessage.success('草稿保存成功')
    }

    // 3. 跳转回列表
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
.app-container {
  padding: 24px;
}
.step-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 0;
  min-height: 400px;
}
.toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
.tip-text {
  margin-left: 15px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.footer-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-lighter);
}

:deep(.el-table) {
  background: var(--el-bg-color) !important;
  color: var(--el-text-color-primary);
}
:deep(.el-table__inner-wrapper) {
  background: var(--el-bg-color) !important;
}
:deep(.el-table__body-wrapper td) {
  background: var(--el-bg-color) !important;
}
</style>
