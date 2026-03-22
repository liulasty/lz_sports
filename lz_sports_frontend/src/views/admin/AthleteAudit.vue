<template>
  <div class="athlete-audit-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>运动员审核</span>
          <div class="filter-box">
            <el-select v-model="queryParams.eventId" placeholder="选择赛事" style="width: 200px; margin-right: 10px" @change="handleEventChange">
              <el-option v-for="event in eventList" :key="event.id" :label="event.name" :value="event.id" />
            </el-select>
            <el-select v-model="queryParams.status" placeholder="状态" style="width: 120px; margin-right: 10px" clearable>
              <el-option label="全部" value="" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
            <el-input v-model="queryParams.keyword" placeholder="姓名关键词" style="width: 150px; margin-right: 10px" @keyup.enter="handleQuery" clearable />
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </div>
        </div>
      </template>

      <!-- 统计数据 -->
      <div class="stats-container" v-if="queryParams.eventId">
        <el-tag type="warning" class="stat-tag">待审核: {{ stats.pending || 0 }}</el-tag>
        <el-tag type="success" class="stat-tag">已通过: {{ stats.approved || 0 }}</el-tag>
        <el-tag type="danger" class="stat-tag">已拒绝: {{ stats.rejected || 0 }}</el-tag>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar" v-if="queryParams.eventId">
        <el-button 
          type="success" 
          :disabled="!hasPendingSelection || submitting" 
          :loading="submitting"
          @click="handleBatchApprove"
        >
          批量通过
        </el-button>
        <span class="selection-tip" v-if="selectedRows.length > 0">已选 {{ selectedRows.length }} 条</span>
      </div>

      <el-table 
        :data="tableData" 
        style="width: 100%" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" :selectable="canSelect" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="contact" label="联系方式" width="150" />
        <el-table-column prop="applyTime" label="申请时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'PENDING'"
              size="small" 
              type="success" 
              :loading="submitting"
              :disabled="submitting"
              @click="handleApprove(scope.row)"
            >通过</el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING'"
              size="small" 
              type="danger" 
              :loading="submitting"
              :disabled="submitting"
              @click="handleReject(scope.row)"
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
import { getEventList } from '@/api/event'
import { 
  getAthleteApplications, 
  approveApplication, 
  rejectApplication, 
  batchApproveApplications,
  getRegistrationStats
} from '@/api/eventAdmin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const eventList = ref([])
const total = ref(0)
const selectedRows = ref([])
const stats = ref({ pending: 0, approved: 0, rejected: 0 })

const queryParams = reactive({
  eventId: null,
  status: '',
  keyword: '',
  currentPage: 1,
  pageSize: 10
})

const hasPendingSelection = computed(() => {
  return selectedRows.value.length > 0 && selectedRows.value.every(row => row.status === 'PENDING')
})

const canSelect = (row) => {
  return row.status === 'PENDING'
}

const fetchEvents = async () => {
  try {
    const res = await getEventList({ currentPage: 1, pageSize: 1000 })
    if (res.code === 200) {
      eventList.value = res.data.records || []
      if (eventList.value.length > 0 && !queryParams.eventId) {
        queryParams.eventId = eventList.value[0].id
        handleEventChange()
      }
    }
  } catch (error) {
    console.error('获取赛事列表失败', error)
  }
}

const fetchStats = async () => {
  if (!queryParams.eventId) return
  try {
    const res = await getRegistrationStats(queryParams.eventId)
    if (res.code === 200 && res.data) {
      // 假设 stats 包含 athlete 相关的统计，如果只有报名，则用列表数据自己计算
      if (res.data.athleteStats) {
        stats.value = res.data.athleteStats
      } else {
        calculateStatsFromList()
      }
    } else {
      calculateStatsFromList()
    }
  } catch (error) {
    calculateStatsFromList()
  }
}

// 备用计算逻辑，以防后端不返回这三个字段
const calculateStatsFromList = async () => {
  if (!queryParams.eventId) return
  try {
    const res = await getAthleteApplications(queryParams.eventId, { currentPage: 1, pageSize: 10000 })
    if (res.code === 200 && res.data && res.data.records) {
      const records = res.data.records
      stats.value.pending = records.filter(r => r.status === 'PENDING').length
      stats.value.approved = records.filter(r => r.status === 'APPROVED').length
      stats.value.rejected = records.filter(r => r.status === 'REJECTED').length
    }
  } catch (e) {
    console.error(e)
  }
}

const getList = async () => {
  if (!queryParams.eventId) return
  loading.value = true
  try {
    const res = await getAthleteApplications(queryParams.eventId, {
      status: queryParams.status,
      keyword: queryParams.keyword,
      currentPage: queryParams.currentPage,
      pageSize: queryParams.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleEventChange = () => {
  queryParams.currentPage = 1
  getList()
  fetchStats()
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

const handleSelectionChange = (val) => {
  selectedRows.value = val
}

const handleApprove = (row) => {
  if (submitting.value) return
  ElMessageBox.confirm('确认通过该运动员的申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    submitting.value = true
    try {
      const res = await approveApplication(queryParams.eventId, row.id)
      if (res.code === 200) {
        ElMessage.success('操作成功')
        getList()
        fetchStats()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    } catch (error) {
      console.error(error)
    } finally {
      submitting.value = false
    }
  }).catch(() => {})
}

const handleReject = (row) => {
  if (submitting.value) return
  ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S/,
    inputErrorMessage: '拒绝原因不能为空'
  }).then(async ({ value }) => {
    submitting.value = true
    try {
      const res = await rejectApplication(queryParams.eventId, row.id, value)
      if (res.code === 200) {
        ElMessage.success('操作成功')
        getList()
        fetchStats()
      } else {
        ElMessage.error(res.msg || '操作失败')
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
  if (!hasPendingSelection.value) {
    ElMessage.warning('请选择待审核的记录')
    return
  }
  const ids = selectedRows.value.map(row => row.id)
  ElMessageBox.confirm(`确认批量通过选中的 ${ids.length} 个申请吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    submitting.value = true
    try {
      const res = await batchApproveApplications(queryParams.eventId, ids)
      if (res.code === 200) {
        ElMessage.success('批量操作成功')
        getList()
        fetchStats()
      } else {
        ElMessage.error(res.msg || '操作失败')
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
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'PENDING') return 'warning'
  return 'info'
}

const getStatusLabel = (status) => {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已拒绝'
  if (status === 'PENDING') return '待审核'
  return status
}

onMounted(() => {
  fetchEvents()
})
</script>

<style scoped>
.athlete-audit-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter-box {
  display: flex;
  align-items: center;
}
.stats-container {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}
.stat-tag {
  font-size: 14px;
  padding: 8px 15px;
}
.toolbar {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}
.selection-tip {
  margin-left: 15px;
  font-size: 14px;
  color: #606266;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
