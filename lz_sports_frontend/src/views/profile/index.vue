<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <div class="user-info">
            <div class="avatar-container">
              <el-avatar :size="100" :src="userInfo.avatarSrc || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            </div>
            <div class="info-item">
              <label>用户名：</label>
              <span>{{ userInfo.userName }}</span>
            </div>
            <div class="info-item">
              <label>邮箱：</label>
              <span>{{ userInfo.email }}</span>
            </div>
            <div class="info-item">
              <label>角色：</label>
              <el-tag>{{ userInfo.userType }}</el-tag>
            </div>
            <div class="info-item">
              <label>状态：</label>
              <el-tag :type="userInfo.status === '已激活' ? 'success' : 'warning'">{{ userInfo.status }}</el-tag>
            </div>
            <div class="info-item">
              <label>注册时间：</label>
              <span>{{ formatDate(userInfo.registerTime) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>{{ isAthlete ? '我的报名' : '运动员认证' }}</span>
            </div>
          </template>
          
          <!-- Athlete Registration List -->
          <div v-if="isAthlete">
            <el-table :data="registrationList" style="width: 100%" v-loading="loading">
              <el-table-column prop="eventName" label="赛事" />
              <el-table-column prop="itemName" label="项目" />
              <el-table-column prop="registrationTime" label="报名时间">
                <template #default="scope">
                  {{ formatDate(scope.row.registrationTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="registrationStatus" label="状态">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.registrationStatus)">{{ scope.row.registrationStatus }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="queryParams.currentPage"
                v-model:page-size="queryParams.pageSize"
                :page-sizes="[5, 10, 20]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>

          <!-- Athlete Application Form -->
          <div v-else-if="!isAdmin">
             <div v-if="hasApplied">
                <el-alert
                  :title="'您的申请状态：' + applicationStatus"
                  :type="applicationStatus === '成功' ? 'success' : (applicationStatus === '拒绝' ? 'error' : 'warning')"
                  show-icon
                  :closable="false"
                />
                <div v-if="applicationStatus === '拒绝'" style="margin-top: 20px;">
                   <p>您的申请已被拒绝，请重新提交。</p>
                   <el-button type="primary" @click="resetApplication">重新申请</el-button>
                </div>
             </div>
             
             <el-form 
               v-else 
               ref="applyFormRef"
               :model="applyForm" 
               :rules="rules" 
               label-width="100px"
               style="max-width: 500px; margin-top: 20px;"
             >
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="applyForm.name" />
                </el-form-item>
                <el-form-item label="年龄" prop="age">
                  <el-input-number v-model="applyForm.age" :min="1" :max="100" />
                </el-form-item>
                <el-form-item label="性别" prop="gender">
                  <el-select v-model="applyForm.gender" placeholder="请选择性别">
                    <el-option label="男" value="男" />
                    <el-option label="女" value="女" />
                  </el-select>
                </el-form-item>
                <el-form-item label="联系方式" prop="phone">
                  <el-input v-model="applyForm.phone" />
                </el-form-item>
                <el-form-item label="年级/班级" prop="grade">
                  <el-input v-model="applyForm.grade" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitApply">提交申请</el-button>
                </el-form-item>
             </el-form>
          </div>
          
          <div v-else>
            <el-empty description="管理员无需认证" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUserInfo } from '@/api/user'
import { getRegistrationList } from '@/api/registration'
import { applyAthlete, getAthleteApply } from '@/api/athlete'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const userInfo = ref({})
const isAthlete = computed(() => userInfo.value.userType === '运动员')
const isAdmin = computed(() => userInfo.value.userType === '管理员')

// Registration List Data
const loading = ref(false)
const registrationList = ref([])
const total = ref(0)
const queryParams = reactive({
  currentPage: 1,
  pageSize: 5
})

// Application Data
const hasApplied = ref(false)
const applicationStatus = ref('')
const applyFormRef = ref(null)
const applyForm = reactive({
  name: '',
  age: 18,
  gender: '',
  phone: '',
  grade: '',
  userId: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系方式', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级/班级', trigger: 'blur' }]
}

const getInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 1) {
      userInfo.value = res.data
      userStore.setUserInfo(res.data)
      applyForm.userId = res.data.userId
      
      if (isAthlete.value) {
        getRegistrations()
      } else if (!isAdmin.value) {
        checkApplication()
      }
    }
  } catch (error) {
    console.error(error)
  }
}

const getRegistrations = async () => {
  loading.value = true
  try {
    const res = await getRegistrationList(queryParams)
    if (res.code === 1) {
      registrationList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const checkApplication = async () => {
  try {
    const res = await getAthleteApply(userInfo.value.userId)
    if (res.code === 1 && res.data) {
      hasApplied.value = true
      applicationStatus.value = res.data.athleteState
    }
  } catch (error) {
    // Usually throws 500 if not found based on backend logic, or returns null
    console.log("No application found or error checking application")
  }
}

const submitApply = async () => {
  if (!applyFormRef.value) return
  await applyFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await applyAthlete(applyForm)
        if (res.code === 1) {
          ElMessage.success('申请提交成功')
          hasApplied.value = true
          applicationStatus.value = '申请中'
        }
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const resetApplication = () => {
  hasApplied.value = false
  applicationStatus.value = ''
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getRegistrations()
}

const handleCurrentChange = (val) => {
  queryParams.currentPage = val
  getRegistrations()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

const getStatusType = (status) => {
  if (status === '通过') return 'success'
  if (status === '未通过' || status === '拒绝') return 'danger'
  return 'warning'
}

onMounted(() => {
  getInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}
.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.avatar-container {
  margin-bottom: 20px;
}
.info-item {
  margin-bottom: 10px;
  width: 100%;
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
