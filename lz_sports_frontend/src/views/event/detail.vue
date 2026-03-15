<template>
  <div class="event-detail-container" v-if="event">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>{{ event.eventName }}</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <div class="event-info">
        <div class="image-box" v-if="event.imageUrls">
          <el-image :src="Array.isArray(event.imageUrls) ? event.imageUrls[0] : event.imageUrls" fit="cover" style="width: 100%; height: 300px; border-radius: 4px;" />
        </div>
        
        <el-descriptions title="赛事详情" :column="2" border style="margin-top: 20px;">
          <el-descriptions-item label="参赛要求">{{ event.eventDescription }}</el-descriptions-item>
          <el-descriptions-item label="报名费用">0 元</el-descriptions-item>
          <el-descriptions-item label="开始报名">{{ formatDate(event.registrationStartTime) }}</el-descriptions-item>
          <el-descriptions-item label="截止报名">{{ formatDate(event.registrationEndTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
             <el-tag :type="getStatusType(event.eventStatus)">{{ getStatusLabel(event.eventStatus) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="project-list" style="margin-top: 30px;">
        <el-alert
          v-if="athleteApplyStatus"
          :title="`运动员资格状态：${athleteApplyStatus}`"
          :type="athleteApplyStatus === '成功' ? 'success' : (athleteApplyStatus === '在审核' ? 'warning' : 'error')"
          :closable="false"
          style="margin-bottom: 16px;"
        />
        <el-alert
          v-if="registeredProjectNames.length > 0"
          :title="`已报名项目：${registeredProjectNames.join('、')}`"
          type="info"
          :closable="false"
          style="margin-bottom: 16px;"
        />
        <h3>包含项目</h3>
        <el-table :data="projects" style="width: 100%">
          <el-table-column prop="itemName" label="项目名称" />
          <el-table-column prop="limitation" label="性别限制" />
          <el-table-column prop="grade" label="年级限制" />
          <el-table-column prop="attendance" label="已报/限额">
            <template #default="scope">
              {{ scope.row.attendance }} / {{ scope.row.maxAttendance }}
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                :disabled="scope.row.registerDisabled"
                @click="handleRegister(scope.row)"
              >
                {{ scope.row.registerText }}
              </el-button>
              <el-button
                type="danger"
                size="small"
                style="margin-left: 8px;"
                :disabled="scope.row.cancelDisabled"
                @click="handleCancel(scope.row)"
              >
                {{ scope.row.cancelText }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEventById } from '@/api/event'
import { getProjectsByEventId } from '@/api/project'
import { applyProject, getRegistrationList, cancelRegistration } from '@/api/registration'
import { getAthleteApply } from '@/api/athlete'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const event = ref(null)
const projects = ref([])
const athleteApplyStatus = ref('')
const myRegistrations = ref([])
const registeredProjectNames = ref([])

const getStatusType = (status) => {
  if (status === 'OPEN' || status === 'ONGOING') return 'success'
  if (status === 'FINISHED') return 'info'
  return 'warning'
}

const getStatusLabel = (status) => {
  if (status === 'OPEN') return '报名中'
  if (status === 'CLOSED') return '报名结束'
  if (status === 'ONGOING') return '进行中'
  if (status === 'FINISHED') return '已结束'
  return '草稿'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

const loadEvent = async () => {
  const id = route.params.id
  try {
    const res = await getEventById(id)
    if (res.code === 200) {
      event.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const loadProjects = async () => {
  if (!event.value) return
  
  try {
    const res = await getProjectsByEventId(event.value.id)
    if (res.code === 200) {
      projects.value = (res.data || []).map(item => ({
        ...item,
        registerDisabled: true,
        registerText: '不可报名',
        cancelDisabled: true,
        cancelText: '不可取消'
      }))
      refreshButtonState()
    }
  } catch (error) {
    console.error(error)
  }
}

const loadAthleteStatus = async () => {
  try {
    const userId = userStore.userInfo.id
    if (!userId) return
    const res = await getAthleteApply(userId)
    if (res.code === 200 && res.data) {
      athleteApplyStatus.value = res.data.athleteState
    } else {
      athleteApplyStatus.value = ''
    }
  } catch (error) {
    athleteApplyStatus.value = ''
  }
}

const loadMyRegistrations = async () => {
  try {
    const res = await getRegistrationList({ currentPage: 1, pageSize: 200 })
    if (res.code === 200) {
      const records = res.data.records || []
      myRegistrations.value = records.filter(item => item.eventId === event.value.id && item.registrationStatus !== 'CANCELLED')
      registeredProjectNames.value = myRegistrations.value.map(item => item.itemName)
    }
  } catch (error) {
    myRegistrations.value = []
    registeredProjectNames.value = []
  }
}

const refreshButtonState = () => {
  if (!event.value) return
  const now = new Date()
  const regStart = event.value.registrationStartTime ? new Date(event.value.registrationStartTime) : null
  const regEnd = event.value.registrationEndTime ? new Date(event.value.registrationEndTime) : null
  projects.value = projects.value.map(project => {
    const registration = myRegistrations.value.find(item => item.itemId === project.id)
    let registerDisabled = false
    let registerText = '立即报名'
    let cancelDisabled = true
    let cancelText = '取消报名'

    if (event.value.eventStatus !== 'OPEN') {
      registerDisabled = true
      registerText = '赛事未开放报名'
    } else if (regStart && now < regStart) {
      registerDisabled = true
      registerText = '报名未开始'
    } else if (regEnd && now > regEnd) {
      registerDisabled = true
      registerText = '报名已截止'
    } else if (project.attendance >= project.maxAttendance) {
      registerDisabled = true
      registerText = '名额已满'
    } else if (athleteApplyStatus.value !== 'SUCCESS' && athleteApplyStatus.value !== '成功') {
      registerDisabled = true
      registerText = '请先通过运动员审核'
    } else if (registration) {
      registerDisabled = true
      registerText = '已报名'
    }

    if (registration) {
      if (regEnd && now > regEnd) {
        cancelDisabled = true
        cancelText = '报名截止后不可取消'
      } else {
        cancelDisabled = false
        cancelText = '取消报名'
      }
    } else {
      cancelDisabled = true
      cancelText = '未报名'
    }

    return {
      ...project,
      registrationId: registration ? registration.id : null,
      registerDisabled,
      registerText,
      cancelDisabled,
      cancelText
    }
  })
}

const handleRegister = (project) => {
  ElMessageBox.confirm(`确认报名项目 ${project.itemName} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'primary'
  }).then(async () => {
    try {
      const res = await applyProject(project.id)
      if (res.code === 200) {
        ElMessage.success('报名申请已提交')
        await loadMyRegistrations()
        await loadProjects()
      } else {
        ElMessage.error(res.msg || '报名失败')
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const handleCancel = (project) => {
  if (!project.registrationId) return
  ElMessageBox.confirm(`确认取消项目 ${project.itemName} 的报名吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await cancelRegistration(project.registrationId)
      if (res.code === 200) {
        ElMessage.success('取消成功')
        await loadMyRegistrations()
        await loadProjects()
      } else {
        ElMessage.error(res.msg || '取消失败')
      }
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(async () => {
  await loadEvent()
  await loadAthleteStatus()
  await loadMyRegistrations()
  await loadProjects()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
