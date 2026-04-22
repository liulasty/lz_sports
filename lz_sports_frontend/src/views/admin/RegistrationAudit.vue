<template>
  <div class="registration-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <div class="header-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <rect x="9" y="3" width="6" height="4" rx="1" stroke="currentColor" stroke-width="2"/>
                <path d="M9 12h6M9 16h4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="header-text">
              <span class="header-title">报名审批</span>
              <span class="header-subtitle">审核运动员的赛事项目报名申请</span>
            </div>
          </div>
          <div class="filter-box">
            <SmartSelect v-model="queryParams.eventId" :options="eventList" placeholder="选择赛事" style="width: 200px" clearable @change="handleQuery" />
            <el-input v-model="queryParams.name" placeholder="搜索运动员" style="width: 150px" @keyup.enter="handleQuery">
              <template #prefix>
                <svg viewBox="0 0 24 24" fill="none" class="search-icon"><circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2"/><path d="M21 21l-4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
              </template>
            </el-input>
            <SmartSelect v-model="queryParams.status" :options="statusOptions" placeholder="状态" style="width: 130px" clearable />
            <el-button type="primary" class="query-btn" @click="handleQuery">查询</el-button>
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
      <div class="toolbar">
        <div class="toolbar-actions">
          <el-button
            class="batch-approve-btn"
            :disabled="!hasPendingSelection || submitting"
            :loading="submitting"
            @click="handleBatchApprove"
          >
            <svg v-if="!submitting" viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M9 11l3 3L22 4" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
            批量通过
          </el-button>
          <el-button
            class="batch-reject-btn"
            :disabled="!hasPendingSelection || submitting"
            :loading="submitting"
            @click="handleBatchRefuse"
          >
            <svg v-if="!submitting" viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/></svg>
            批量拒绝
          </el-button>
        </div>
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
          <el-table-column prop="id" label="ID" width="70">
            <template #default="scope">
              <span class="id-text">#{{ scope.row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="athleteName" label="运动员" width="120">
            <template #default="scope">
              <div class="name-cell">
                <div class="name-avatar">{{ (scope.row.athleteName || '?').charAt(0) }}</div>
                <span>{{ scope.row.athleteName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="eventName" label="赛事" min-width="160">
            <template #default="scope">
              <div class="event-cell">
                <svg viewBox="0 0 24 24" fill="none" class="cell-icon"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                <span>{{ scope.row.eventName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="itemName" label="项目" min-width="150">
            <template #default="scope">
              <div class="item-tag">{{ scope.row.itemName }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="registrationTime" label="报名时间" width="170">
            <template #default="scope">
              <div class="time-cell">
                <svg viewBox="0 0 24 24" fill="none" class="time-icon"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5"/><path d="M12 7V12L15 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
                {{ formatDate(scope.row.registrationTime) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="registrationStatus" label="状态" width="105">
            <template #default="scope">
              <div class="status-badge" :class="'status-' + getStatusKey(scope.row.registrationStatus)">
                <span class="status-dot"></span>
                {{ getStatusLabel(scope.row.registrationStatus) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="155" fixed="right">
            <template #default="scope">
              <div class="action-btns" v-if="scope.row.registrationStatus === 'PENDING'">
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
                  @click="handleRefuse(scope.row)"
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
import { getRegistrationList, approveRegistration, refuseRegistration, batchAuditRegistration } from '@/api/registration'
import { getEventList } from '@/api/event'
import { ElMessage, ElMessageBox } from 'element-plus'
import { isSuccess } from '@/utils/result'
import RegistrationStats from '@/components/RegistrationStats.vue'
import SmartSelect from '@/components/SmartSelect.vue'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const eventList = ref([])
const total = ref(0)
const statsRef = ref(null)
const selectedRows = ref([])
const statusOptions = [
  { label: '审核中', value: 'PENDING' },
  { label: '通过', value: 'APPROVED' },
  { label: '未通过', value: 'REJECTED' }
]

const queryParams = reactive({
  currentPage: 1,
  pageSize: 10,
  eventId: null,
  name: '',
  status: 'PENDING'
})

const hasPendingSelection = computed(() => {
  return selectedRows.value.length > 0 && selectedRows.value.every(row => row.registrationStatus === 'PENDING')
})

const canSelect = (row) => row.registrationStatus === 'PENDING'

const getRowClass = ({ row }) => {
  if (row.registrationStatus === 'APPROVED') return 'row-approved'
  if (row.registrationStatus === 'REJECTED') return 'row-rejected'
  return ''
}

const getStatusKey = (status) => {
  if (status === 'APPROVED') return 'approved'
  if (status === 'REJECTED') return 'rejected'
  return 'pending'
}

const getStatusLabel = (status) => {
  if (status === 'APPROVED') return '通过'
  if (status === 'REJECTED') return '未通过'
  if (status === 'PENDING') return '审核中'
  if (status === 'CANCELLED') return '已取消'
  return status
}

const handleSelectionChange = (val) => { selectedRows.value = val }

const fetchEvents = async () => {
  try {
    const res = await getEventList({ currentPage: 1, pageSize: 1000 })
    if (isSuccess(res)) eventList.value = res.data.records
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

const handleSizeChange = (val) => { queryParams.pageSize = val; getList() }
const handleCurrentChange = (val) => { queryParams.currentPage = val; getList() }

const refreshStats = () => { if (statsRef.value) statsRef.value.refresh() }

const handleApprove = (row) => {
  if (submitting.value) return
  ElMessageBox.confirm('确认通过该报名申请吗?', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    submitting.value = true
    try {
      const res = await approveRegistration(row.id, row.eventId)
      if (isSuccess(res)) { ElMessage.success('操作成功'); getList(); refreshStats() }
    } catch (error) { console.error(error) } finally { submitting.value = false }
  }).catch(() => {})
}

const handleRefuse = (row) => {
  if (submitting.value) return
  ElMessageBox.confirm('确认拒绝该报名申请吗?', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    submitting.value = true
    try {
      const res = await refuseRegistration(row.id, row.eventId)
      if (isSuccess(res)) { ElMessage.success('操作成功'); getList(); refreshStats() }
    } catch (error) { console.error(error) } finally { submitting.value = false }
  }).catch(() => {})
}

const handleBatchApprove = () => {
  if (submitting.value || !hasPendingSelection.value) return
  const ids = selectedRows.value.map(row => row.id)
  const eventId = selectedRows.value[0]?.eventId || queryParams.eventId
  ElMessageBox.confirm(`确认批量通过选中的 ${ids.length} 个报名申请吗?`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    submitting.value = true
    try {
      const res = await batchAuditRegistration(ids, true, eventId)
      if (isSuccess(res)) { ElMessage.success('批量操作成功'); getList(); refreshStats() }
    } catch (error) { console.error(error) } finally { submitting.value = false }
  }).catch(() => {})
}

const handleBatchRefuse = () => {
  if (submitting.value || !hasPendingSelection.value) return
  const ids = selectedRows.value.map(row => row.id)
  const eventId = selectedRows.value[0]?.eventId || queryParams.eventId
  ElMessageBox.confirm('批量拒绝将不填写拒绝原因，确认继续？', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    submitting.value = true
    try {
      const res = await batchAuditRegistration(ids, false, eventId)
      if (isSuccess(res)) { ElMessage.success('批量操作成功'); getList(); refreshStats() }
    } catch (error) { console.error(error) } finally { submitting.value = false }
  }).catch(() => {})
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

const getStatusType = (status) => {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

onMounted(() => {
  fetchEvents()
  getList()
})
</script>

<style scoped>
/* ===================== Layout ===================== */
.registration-container {
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
  background: linear-gradient(135deg, #f472b6, #ec4899);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.28);
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

/* ===================== Toolbar ===================== */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 10px;
  border: 1px solid var(--el-border-color-lighter);
}
.toolbar-actions {
  display: flex;
  gap: 10px;
}
.batch-approve-btn,
.batch-reject-btn {
  display: inline-flex !important;
  align-items: center;
  gap: 6px;
  padding: 8px 18px !important;
  height: auto !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  border: none !important;
  transition: transform 0.15s, box-shadow 0.15s, opacity 0.2s !important;
}
.batch-approve-btn {
  background: linear-gradient(135deg, #34d399, #10b981) !important;
  color: #fff !important;
  box-shadow: 0 3px 10px rgba(16, 185, 129, 0.3) !important;
}
.batch-approve-btn:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 5px 14px rgba(16, 185, 129, 0.4) !important;
}
.batch-reject-btn {
  background: linear-gradient(135deg, #f87171, #ef4444) !important;
  color: #fff !important;
  box-shadow: 0 3px 10px rgba(239, 68, 68, 0.25) !important;
}
.batch-reject-btn:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 5px 14px rgba(239, 68, 68, 0.35) !important;
}
.batch-approve-btn:disabled,
.batch-reject-btn:disabled {
  opacity: 0.45 !important;
  transform: none !important;
}
.btn-icon {
  width: 14px;
  height: 14px;
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
.fade-slide-enter-active,
.fade-slide-leave-active { transition: all 0.25s ease; }
.fade-slide-enter-from,
.fade-slide-leave-to { opacity: 0; transform: translateX(8px); }

/* ===================== Table ===================== */
.table-wrapper {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}
.id-text {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-placeholder);
  font-variant-numeric: tabular-nums;
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
  background: linear-gradient(135deg, #f9a8d4, #ec4899);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.event-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.cell-icon {
  width: 14px;
  height: 14px;
  color: var(--el-text-color-placeholder);
  flex-shrink: 0;
}
.item-tag {
  display: inline-block;
  padding: 3px 10px;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  border: 1px solid var(--el-color-primary-light-7);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.time-cell {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.time-icon {
  width: 13px;
  height: 13px;
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
.status-pending .status-dot {
  background: var(--el-color-warning);
  animation: pulse 1.5s infinite;
}
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
  50% { opacity: 0.35; }
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
  width: 12px;
  height: 12px;
}
.action-approve {
  background: linear-gradient(135deg, #34d399, #10b981) !important;
  color: #fff !important;
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.3) !important;
}
.action-approve:not(:disabled):hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 10px rgba(16, 185, 129, 0.4) !important;
}
.action-reject {
  background: linear-gradient(135deg, #f87171, #ef4444) !important;
  color: #fff !important;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.25) !important;
}
.action-reject:not(:disabled):hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 4px 10px rgba(239, 68, 68, 0.35) !important;
}
.no-action-text {
  color: var(--el-text-color-placeholder);
  font-size: 16px;
}

/* Row highlight */
:deep(.row-approved td) {
  background: var(--el-color-success-light-9) !important;
}
:deep(.row-rejected td) {
  background: var(--el-color-danger-light-9) !important;
  opacity: 0.72;
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
  .card-header { flex-direction: column; align-items: flex-start; }
  .filter-box { flex-wrap: wrap; }
}
</style>