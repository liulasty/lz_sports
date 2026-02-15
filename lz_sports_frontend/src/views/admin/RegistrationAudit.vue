<template>
  <div class="registration-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>报名审批</span>
          <div class="filter-box">
            <el-input v-model="queryParams.name" placeholder="运动员姓名" style="width: 150px; margin-right: 10px" @keyup.enter="handleQuery" />
            <el-select v-model="queryParams.status" placeholder="状态" style="width: 150px; margin-right: 10px" clearable>
              <el-option label="审核中" value="审核中" />
              <el-option label="通过" value="通过" />
              <el-option label="未通过" value="未通过" />
            </el-select>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="athleteName" label="运动员" width="120" />
        <el-table-column prop="eventName" label="赛事" width="180" />
        <el-table-column prop="itemName" label="项目" width="180" />
        <el-table-column prop="registrationTime" label="报名时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.registrationTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="registrationStatus" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.registrationStatus)">{{ scope.row.registrationStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button 
              v-if="scope.row.registrationStatus === '审核中'"
              size="small" 
              type="success" 
              @click="handleApprove(scope.row)"
            >通过</el-button>
            <el-button 
              v-if="scope.row.registrationStatus === '审核中'"
              size="small" 
              type="danger" 
              @click="handleRefuse(scope.row)"
            >拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.currentPage"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRegistrationList, approveRegistration, refuseRegistration } from '@/api/registration'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  currentPage: 1,
  pageSize: 10,
  name: '',
  status: '审核中' // Default to pending
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getRegistrationList(queryParams)
    if (res.code === 1) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.currentPage = 1
  getList()
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.currentPage = val
  getList()
}

const handleApprove = (row) => {
  ElMessageBox.confirm('确认通过该报名申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await approveRegistration(row.id)
      if (res.code === 1) {
        ElMessage.success('操作成功')
        getList()
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const handleRefuse = (row) => {
  ElMessageBox.confirm('确认拒绝该报名申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await refuseRegistration(row.id)
      if (res.code === 1) {
        ElMessage.success('操作成功')
        getList()
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

const getStatusType = (status) => {
  if (status === '通过') return 'success'
  if (status === '未通过') return 'danger'
  return 'warning'
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.registration-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
