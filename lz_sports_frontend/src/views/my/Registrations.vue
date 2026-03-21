<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的报名</span>
        </div>
      </template>
      <el-table :data="registrationList" style="width: 100%" v-loading="loading">
        <el-table-column prop="eventName" label="赛事名称" />
        <el-table-column prop="itemName" label="项目名称" />
        <el-table-column prop="registrationTime" label="报名时间">
          <template #default="scope">
            {{ formatDate(scope.row.registrationTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="registrationStatus" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.registrationStatus)">
              {{ scope.row.registrationStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              type="danger" 
              size="small" 
              @click="handleCancel(scope.row)"
              :disabled="scope.row.registrationStatus === 'CANCELLED' || scope.row.registrationStatus === '已取消'"
            >
              取消报名
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-pagination
          v-model:current-page="queryParams.currentPage"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRegistrationList, cancelRegistration } from '@/api/registration'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const registrationList = ref([])
const total = ref(0)

const queryParams = reactive({
  currentPage: 1,
  pageSize: 10
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getRegistrationList(queryParams)
    if (res.code === 200) {
      registrationList.value = res.data.records || res.data.rows || []
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleCurrentChange = (val) => {
  queryParams.currentPage = val
  getList()
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确认取消该项目的报名吗?', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await cancelRegistration(row.id || row.registrationId)
      if (res.code === 200) {
        ElMessage.success('已取消报名')
        getList()
      }
    } catch (e) {
      console.error(e)
    }
  })
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

const getStatusType = (status) => {
  if (status === 'CONFIRMED' || status === '通过') return 'success'
  if (status === 'PENDING' || status === '待审核') return 'warning'
  if (status === 'CANCELLED' || status === '已取消') return 'info'
  return 'danger'
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
