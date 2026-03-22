<template>
  <div class="registration-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>报名审批</span>
          <div class="filter-box">
            <el-select v-model="queryParams.eventId" placeholder="选择赛事" style="width: 200px; margin-right: 10px" clearable @change="handleQuery">
              <el-option v-for="event in eventList" :key="event.id" :label="event.name" :value="event.id" />
            </el-select>
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

      <!-- 报名统计组件 -->
      <RegistrationStats 
        v-if="queryParams.eventId" 
        ref="statsRef" 
        :event-id="queryParams.eventId" 
      />

      <!-- 工具栏 -->
      <div class="toolbar" style="margin-bottom: 15px; display: flex; align-items: center;">
        <el-button 
          type="success" 
          :disabled="!hasPendingSelection || submitting" 
          :loading="submitting"
          @click="handleBatchApprove"
        >批量通过</el-button>
        <el-button 
          type="danger" 
          :disabled="!hasPendingSelection || submitting" 
          :loading="submitting"
          @click="handleBatchRefuse"
        >批量拒绝</el-button>
        <span v-if="selectedRows.length > 0" style="margin-left: 15px; font-size: 14px; color: #606266;">
          已选 {{ selectedRows.length }} 条
        </span>
      </div>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" :selectable="canSelect" />
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
              :loading="submitting"
              :disabled="submitting"
              @click="handleApprove(scope.row)"
            >通过</el-button>
            <el-button 
              v-if="scope.row.registrationStatus === '审核中'"
              size="small" 
              type="danger" 
              :loading="submitting"
              :disabled="submitting"
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
import { ref, reactive, onMounted, computed } from 'vue'
import { getRegistrationList, approveRegistration, refuseRegistration, batchAuditRegistration } from '@/api/registration'
import { getEventList } from '@/api/event'
import { ElMessage, ElMessageBox } from 'element-plus'
import { isSuccess } from '@/utils/result'
import RegistrationStats from '@/components/RegistrationStats.vue'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const eventList = ref([])
const total = ref(0)
const statsRef = ref(null)
const selectedRows = ref([])

const queryParams = reactive({
  currentPage: 1,
  pageSize: 10,
  eventId: null,
  name: '',
  status: '审核中' // Default to pending
})

const hasPendingSelection = computed(() => {
  return selectedRows.value.length > 0 && selectedRows.value.every(row => row.registrationStatus === '审核中')
})

const canSelect = (row) => {
  return row.registrationStatus === '审核中'
}

const handleSelectionChange = (val) => {
  selectedRows.value = val
}

const fetchEvents = async () => {
  try {
    const res = await getEventList({ currentPage: 1, pageSize: 1000 })
    if (isSuccess(res)) {
      eventList.value = res.data.records
    }
  } catch (error) {
    console.error('获取赛事列表失败', error)
  }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getRegistrationList(queryParams)
    if (isSuccess(res)) {
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
  if (submitting.value) return
  ElMessageBox.confirm('确认通过该报名申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    submitting.value = true
    try {
      const res = await approveRegistration(row.id, row.eventId)
      if (isSuccess(res)) {
        ElMessage.success('操作成功')
        getList()
        if (statsRef.value) {
          statsRef.value.refresh()
        }
      }
    } catch (error) {
      console.error(error)
    } finally {
      submitting.value = false
    }
  }).catch(() => {})
}

const handleRefuse = (row) => {
  if (submitting.value) return
  ElMessageBox.confirm('确认拒绝该报名申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    submitting.value = true
    try {
      const res = await refuseRegistration(row.id, row.eventId)
      if (isSuccess(res)) {
        ElMessage.success('操作成功')
        getList()
        if (statsRef.value) {
          statsRef.value.refresh()
        }
      }
    } catch (error) {
      console.error(error)
    } finally {
      submitting.value = false
    }
  }).catch(() => {})
}

const handleBatchApprove = () => {
  if (submitting.value) return
  if (!hasPendingSelection.value) return
  const ids = selectedRows.value.map(row => row.id)
  const eventId = selectedRows.value[0]?.eventId || queryParams.eventId
  ElMessageBox.confirm(`确认批量通过选中的 ${ids.length} 个报名申请吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    submitting.value = true
    try {
      const res = await batchAuditRegistration(ids, true, eventId)
      if (isSuccess(res)) {
        ElMessage.success('批量操作成功')
        getList()
        if (statsRef.value) {
          statsRef.value.refresh()
        }
      }
    } catch (error) {
      console.error(error)
    } finally {
      submitting.value = false
    }
  }).catch(() => {})
}

const handleBatchRefuse = () => {
  if (submitting.value) return
  if (!hasPendingSelection.value) return
  const ids = selectedRows.value.map(row => row.id)
  const eventId = selectedRows.value[0]?.eventId || queryParams.eventId
  ElMessageBox.confirm('批量拒绝将不填写拒绝原因，确认继续？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    submitting.value = true
    try {
      const res = await batchAuditRegistration(ids, false, eventId)
      if (isSuccess(res)) {
        ElMessage.success('批量操作成功')
        getList()
        if (statsRef.value) {
          statsRef.value.refresh()
        }
      }
    } catch (error) {
      console.error(error)
    } finally {
      submitting.value = false
    }
  }).catch(() => {})
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
  fetchEvents()
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
