<template>
  <div class="athlete-audit-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <div class="header-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                <path d="M3 21v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M16 11l2 2 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="header-text">
              <span class="header-title">运动员审核</span>
              <span class="header-subtitle">管理运动员报名申请与审核状态</span>
            </div>
          </div>
          <div class="filter-box">
            <el-select v-model="queryParams.eventId" placeholder="选择赛事" style="width: 200px" @change="handleEventChange">
              <el-option v-for="event in eventList" :key="event.id" :label="event.name" :value="event.id" />
            </el-select>
            <el-select v-model="queryParams.status" placeholder="状态" style="width: 120px" clearable>
              <el-option label="全部" value="" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
            <el-input v-model="queryParams.keyword" placeholder="搜索姓名" style="width: 150px" @keyup.enter="handleQuery" clearable>
              <template #prefix>
                <svg viewBox="0 0 24 24" fill="none" class="search-icon"><circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2"/><path d="M21 21l-4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
              </template>
            </el-input>
            <el-button type="primary" class="query-btn" @click="handleQuery">
              查询
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-container" v-if="queryParams.eventId">
        <div class="stat-card stat-pending">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/><path d="M12 7V12L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.pending || 0 }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </div>
        <div class="stat-card stat-approved">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/><path d="M8 12l3 3 5-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.approved || 0 }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </div>
        <div class="stat-card stat-rejected">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/><path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.rejected || 0 }}</div>
            <div class="stat-label">已拒绝</div>
          </div>
        </div>
        <div class="stat-card stat-total">
          <div class="stat-icon">
            <svg viewBox="0 0 24 24" fill="none"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          </div>
          <div class="stat-body">
            <div class="stat-value">{{ (stats.pending || 0) + (stats.approved || 0) + (stats.rejected || 0) }}</div>
            <div class="stat-label">总申请</div>
          </div>
        </div>
      </div>

      <!-- 工具栏 -->
      <div class="toolbar" v-if="queryParams.eventId">
        <el-button
          class="batch-btn"
          :disabled="!hasPendingSelection || submitting"
          :loading="submitting"
          @click="handleBatchApprove"
        >
          <svg v-if="!submitting" viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M9 11l3 3L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
          批量通过
        </el-button>
        <transition name="fade-slide">
          <div class="selection-tip" v-if="selectedRows.length > 0">
            <svg viewBox="0 0 24 24" fill="none" class="tip-icon"><path d="M9 11l3 3 8-8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            已选 <strong>{{ selectedRows.length }}</strong> 条记录
          </div>
        </transition>
      </div>

      <!-- 表格 -->
      <div class="table-wrapper">
        <el-table
          :data="tableData"
          style="width: 100%"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          :row-class-name="getRowClass"
        >
          <el-table-column type="selection" width="50" :selectable="canSelect" />
          <el-table-column prop="name" label="姓名" width="110">
            <template #default="scope">
              <div class="name-cell">
                <div class="name-avatar">{{ (scope.row.name || '?').charAt(0) }}</div>
                <span>{{ scope.row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="gender" label="性别" width="80">
            <template #default="scope">
              <span class="gender-tag" :class="scope.row.gender === '男' ? 'male' : 'female'">
                {{ scope.row.gender }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="grade" label="年级" width="100" />
          <el-table-column prop="contact" label="联系方式" width="155" />
          <el-table-column prop="applyTime" label="申请时间" min-width="165">
            <template #default="scope">
              <div class="time-cell">
                <svg viewBox="0 0 24 24" fill="none" class="time-icon"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5"/><path d="M12 7V12L15 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
                {{ formatDate(scope.row.applyTime) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="athleteState" label="状态" width="100">
            <template #default="scope">
              <div class="status-badge" :class="'status-' + scope.row.athleteState?.toLowerCase()">
                <span class="status-dot"></span>
                {{ getStatusLabel(scope.row.athleteState) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="scope">
              <div class="action-btns" v-if="scope.row.athleteState === 'PENDING'">
                <el-button
                  size="small"
                  class="action-approve"
                  :loading="submitting"
                  :disabled="submitting"
                  @click="handleApprove(scope.row)"
                >
                  <svg v-if="!submitting" viewBox="0 0 24 24" fill="none" class="action-icon"><path d="M5 13l4 4L19 7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                  通过
                </el-button>
                <el-button
                  size="small"
                  class="action-reject"
                  :loading="submitting"
                  :disabled="submitting"
                  @click="handleReject(scope.row)"
                >
                  <svg v-if="!submitting" viewBox="0 0 24 24" fill="none" class="action-icon"><path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/></svg>
                  拒绝
                </el-button>
              </div>
              <span v-else class="no-action-text">—</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-container">
        <div class="pagination-info">
          共 <strong>{{ total }}</strong> 条记录
        </div>
        <el-pagination
          v-model:current-page="queryParams.currentPage"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next, jumper"
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
  return selectedRows.value.length > 0 && selectedRows.value.every(row => row.athleteState === 'PENDING')
})

const canSelect = (row) => row.athleteState === 'PENDING'

const getRowClass = ({ row }) => {
  if (row.athleteState === 'APPROVED') return 'row-approved'
  if (row.athleteState === 'REJECTED') return 'row-rejected'
  return ''
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

const calculateStatsFromList = async () => {
  if (!queryParams.eventId) return
  try {
    const res = await getAthleteApplications(queryParams.eventId, { currentPage: 1, pageSize: 10000 })
    if (res.code === 200 && res.data && res.data.records) {
      const records = res.data.records
      stats.value.pending = records.filter(r => r.athleteState === 'PENDING').length
      stats.value.approved = records.filter(r => r.athleteState === 'APPROVED').length
      stats.value.rejected = records.filter(r => r.athleteState === 'REJECTED').length
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
  return new Date(dateStr).toLocaleString()
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
/* ===================== Layout ===================== */
.athlete-audit-container {
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
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.header-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--el-color-primary-light-3), var(--el-color-primary));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.28);
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
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.filter-box {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.search-icon {
  width: 14px;
  height: 14px;
  color: var(--el-text-color-placeholder);
}
.query-btn {
  padding: 9px 20px !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
}

/* ===================== Stats ===================== */
.stats-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  border-radius: 12px;
  border: 1px solid transparent;
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}
.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-icon svg {
  width: 20px;
  height: 20px;
}
.stat-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.stat-value {
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.5px;
}
.stat-label {
  font-size: 12px;
  font-weight: 500;
}

/* Pending */
.stat-pending {
  background: var(--el-color-warning-light-9);
  border-color: var(--el-color-warning-light-5);
}
.stat-pending .stat-icon {
  background: var(--el-color-warning-light-7);
  color: var(--el-color-warning);
}
.stat-pending .stat-value { color: var(--el-color-warning-dark-2); }
.stat-pending .stat-label { color: var(--el-color-warning); }

/* Approved */
.stat-approved {
  background: var(--el-color-success-light-9);
  border-color: var(--el-color-success-light-5);
}
.stat-approved .stat-icon {
  background: var(--el-color-success-light-7);
  color: var(--el-color-success);
}
.stat-approved .stat-value { color: var(--el-color-success-dark-2); }
.stat-approved .stat-label { color: var(--el-color-success); }

/* Rejected */
.stat-rejected {
  background: var(--el-color-danger-light-9);
  border-color: var(--el-color-danger-light-5);
}
.stat-rejected .stat-icon {
  background: var(--el-color-danger-light-7);
  color: var(--el-color-danger);
}
.stat-rejected .stat-value { color: var(--el-color-danger-dark-2); }
.stat-rejected .stat-label { color: var(--el-color-danger); }

/* Total */
.stat-total {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-5);
}
.stat-total .stat-icon {
  background: var(--el-color-primary-light-7);
  color: var(--el-color-primary);
}
.stat-total .stat-value { color: var(--el-color-primary-dark-2); }
.stat-total .stat-label { color: var(--el-color-primary); }

/* ===================== Toolbar ===================== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 10px;
  border: 1px solid var(--el-border-color-lighter);
}
.batch-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px !important;
  height: auto !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  background: linear-gradient(135deg, #34d399, #10b981) !important;
  border: none !important;
  color: #fff !important;
  box-shadow: 0 3px 10px rgba(16, 185, 129, 0.3) !important;
  transition: transform 0.15s, box-shadow 0.15s, opacity 0.2s !important;
}
.batch-btn:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 5px 14px rgba(16, 185, 129, 0.4) !important;
}
.batch-btn:disabled {
  opacity: 0.5 !important;
  transform: none !important;
}
.btn-icon {
  width: 15px;
  height: 15px;
}
.selection-tip {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 5px 12px;
  border-radius: 20px;
  border: 1px solid var(--el-color-primary-light-5);
}
.tip-icon {
  width: 14px;
  height: 14px;
}

/* Transition */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}

/* ===================== Table ===================== */
.table-wrapper {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}
.name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.name-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--el-color-primary-light-5), var(--el-color-primary));
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.gender-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}
.gender-tag.male {
  background: #dbeafe;
  color: #2563eb;
}
.gender-tag.female {
  background: #fce7f3;
  color: #db2777;
}
.time-cell {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.time-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  color: var(--el-text-color-placeholder);
}

/* Status Badge */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}
.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}
.status-pending {
  background: var(--el-color-warning-light-9);
  color: var(--el-color-warning-dark-2);
  border: 1px solid var(--el-color-warning-light-5);
}
.status-pending .status-dot { background: var(--el-color-warning); animation: pulse 1.5s infinite; }
.status-approved {
  background: var(--el-color-success-light-9);
  color: var(--el-color-success-dark-2);
  border: 1px solid var(--el-color-success-light-5);
}
.status-approved .status-dot { background: var(--el-color-success); }
.status-rejected {
  background: var(--el-color-danger-light-9);
  color: var(--el-color-danger-dark-2);
  border: 1px solid var(--el-color-danger-light-5);
}
.status-rejected .status-dot { background: var(--el-color-danger); }

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

/* Action Buttons */
.action-btns {
  display: flex;
  gap: 6px;
}
.action-approve,
.action-reject {
  display: inline-flex !important;
  align-items: center;
  gap: 4px;
  padding: 5px 10px !important;
  height: auto !important;
  border-radius: 7px !important;
  font-size: 12px !important;
  font-weight: 600 !important;
  border: none !important;
  transition: transform 0.15s, box-shadow 0.15s !important;
}
.action-icon {
  width: 13px;
  height: 13px;
}
.action-approve {
  background: linear-gradient(135deg, #34d399, #10b981) !important;
  color: #fff !important;
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.3) !important;
}
.action-approve:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 10px rgba(16, 185, 129, 0.4) !important;
}
.action-reject {
  background: linear-gradient(135deg, #f87171, #ef4444) !important;
  color: #fff !important;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.25) !important;
}
.action-reject:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 10px rgba(239, 68, 68, 0.35) !important;
}
.no-action-text {
  color: var(--el-text-color-placeholder);
  font-size: 14px;
}

/* Row highlight */
:deep(.row-approved td) {
  background: var(--el-color-success-light-9) !important;
}
:deep(.row-rejected td) {
  background: var(--el-color-danger-light-9) !important;
  opacity: 0.75;
}

/* ===================== Pagination ===================== */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}
.pagination-info {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.pagination-info strong {
  color: var(--el-color-primary);
  font-weight: 700;
}

/* ===================== Table overrides ===================== */
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
:deep(.el-table__header th) {
  background: var(--el-fill-color-light) !important;
  font-weight: 700;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* ===================== Responsive ===================== */
@media (max-width: 900px) {
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
  }
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
@media (max-width: 600px) {
  .stats-container {
    grid-template-columns: 1fr 1fr;
  }
  .filter-box {
    flex-wrap: wrap;
  }
}
</style>