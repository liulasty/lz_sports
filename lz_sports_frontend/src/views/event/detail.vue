<template>
  <div class="event-detail-container" v-if="event">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>{{ event.name }}</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <div class="event-info">
        <div class="image-box" v-if="event.imageUrls && event.imageUrls.length > 0">
          <el-image :src="event.imageUrls[0]" fit="cover" style="width: 100%; height: 300px; border-radius: 4px;" />
        </div>
        
        <el-descriptions title="赛事详情" :column="2" border style="margin-top: 20px;">
          <el-descriptions-item label="参赛要求">{{ event.type }}</el-descriptions-item>
          <el-descriptions-item label="报名费用">{{ event.fee }} 元</el-descriptions-item>
          <el-descriptions-item label="开始报名">{{ formatDate(event.date) }}</el-descriptions-item>
          <el-descriptions-item label="截止报名">{{ formatDate(event.end) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
             <el-tag :type="getStatusType(event.status)">{{ getStatusLabel(event.status) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="project-list" style="margin-top: 30px;">
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
                :disabled="!canRegister(scope.row)"
                @click="handleRegister(scope.row)"
              >
                {{ getRegisterBtnText(scope.row) }}
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
// import { getProjectList } from '@/api/project' // Assume we have this or need to add
import request from '@/utils/request' // Direct call for project list for now
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const event = ref(null)
const projects = ref([])

const getStatusType = (status) => {
  if (status === 'PUBLISHED') return 'success'
  if (status === 'ENDED') return 'info'
  return 'warning'
}

const getStatusLabel = (status) => {
  if (status === 'PUBLISHED') return '进行中'
  if (status === 'ENDED') return '已结束'
  return '未开始'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

const loadEvent = async () => {
  const id = route.params.id
  try {
    const res = await getEventById(id)
    if (res.code === 1) {
      event.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const loadProjects = async () => {
  if (!event.value) return
  
  try {
    const res = await request({
      url: `/sports/project/getProjectByEventName?eventName=${encodeURIComponent(event.value.eventName)}`,
      method: 'get'
    })
    
    if (res.code === 1) {
      projects.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const canRegister = (project) => {
  if (!event.value) return false
  if (event.value.status !== 'PUBLISHED') return false
  
  const now = new Date()
  if (new Date(event.value.registrationDeadline) < now) return false
  if (project.attendance >= project.maxAttendance) return false
  return true
}

const getRegisterBtnText = (project) => {
  if (!event.value) return ''
  if (event.value.status === 'DRAFT') return '未发布'
  if (event.value.status === 'ENDED') return '已结束'
  
  const now = new Date()
  if (new Date(event.value.registrationStart) > now) return '报名未开始'
  if (new Date(event.value.registrationDeadline) < now) return '报名截止'
  
  if (project.attendance >= project.maxAttendance) return '名额已满'
  
  return '立即报名'
}

const handleRegister = (project) => {
  ElMessageBox.confirm(`确认报名项目 ${project.itemName} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'primary'
  }).then(async () => {
    try {
      const res = await request({
        url: `/sports/registration/apply/${project.itemId}`,
        method: 'post'
      })
      if (res.code === 1) {
        ElMessage.success('报名申请已提交')
        loadProjects() // Refresh to update attendance
      } else {
        ElMessage.error(res.msg || '报名失败')
      }
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(async () => {
  await loadEvent()
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
