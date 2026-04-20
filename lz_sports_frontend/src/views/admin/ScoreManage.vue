<template>
  <div class="score-manage-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <div class="header-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M18 20V10M12 20V4M6 20v-6" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="header-text">
              <span class="header-title">成绩管理</span>
              <span class="header-subtitle">支持手动录入、Excel 导入、导出与发布赛事成绩</span>
            </div>
          </div>

          <div class="header-right">
            <div class="filter-group">
              <SmartSelect v-model="query.eventId" :options="eventOptions" placeholder="选择赛事" style="width: 185px" @change="handleEventChange" />
              <SmartSelect v-model="query.itemId" :options="itemOptions" label-key="itemName" placeholder="选择项目" clearable style="width: 165px" @change="loadData" />
              <SmartSelect v-model="query.entryStatus" :options="entryStatusOptions" placeholder="录入状态" style="width: 150px" @change="applyManualRows" />
              <el-button type="primary" class="action-btn" @click="loadData">
                <svg viewBox="0 0 24 24" fill="none" class="btn-icon"><circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2"/><path d="M21 21l-4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                查询
              </el-button>
            </div>

            <div class="action-divider"></div>
            <div class="op-group">
              <el-tooltip content="下载导入模板" placement="bottom">
                <el-button class="op-btn" @click="downloadTemplate">
                  <svg viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><polyline points="7 10 12 15 17 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><line x1="12" y1="15" x2="12" y2="3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                  下载模板
                </el-button>
              </el-tooltip>
              <el-tooltip content="导出当前成绩为Excel" placement="bottom">
                <el-button class="op-btn" @click="exportScores">
                  <svg viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><polyline points="14 2 14 8 20 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                  导出成绩
                </el-button>
              </el-tooltip>
              <el-button class="op-btn op-import" @click="showImportDialog = true">
                <svg viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><polyline points="17 8 12 3 7 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><line x1="12" y1="3" x2="12" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                导入成绩
              </el-button>
              <el-button
                class="op-btn op-batch-save"
                :loading="batchSaving"
                :disabled="batchSaving || !dirtySavableRows.length"
                @click="saveDirtyRows"
              >
                <svg v-if="!batchSaving" viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><path d="M17 21v-8H7v8" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><path d="M7 3v5h8" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/></svg>
                批量保存 {{ dirtySavableRows.length ? `(${dirtySavableRows.length})` : '' }}
              </el-button>
              <el-button class="op-btn op-publish" :loading="publishing" :disabled="publishing || !query.eventId || !summary.draftCount" @click="publishWithConfirm">
                <svg v-if="!publishing" viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M22 2L11 13M22 2L15 22L11 13L2 9L22 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                发布成绩
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <div class="score-summary" v-if="summary.total > 0">
        <div class="summary-item">
          <div class="summary-value">{{ summary.total }}</div>
          <div class="summary-label">可录入名单</div>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <div class="summary-value published">{{ summary.publishedCount }}</div>
          <div class="summary-label">已发布</div>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <div class="summary-value draft">{{ summary.draftCount }}</div>
          <div class="summary-label">待发布</div>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <div class="summary-value items">{{ summary.itemCount }}</div>
          <div class="summary-label">涉及项目</div>
        </div>
      </div>

      <div class="tip-bar" v-if="query.eventId">
        <div class="tip-copy">
          <strong>手动录入规则：</strong>
          仅展示当前赛事已通过报名名单；已发布成绩会锁定输入框，不能再修改。
        </div>
        <div class="tip-copy role-copy">
          <strong>角色职责：</strong>
          {{ roleScopeText }}
        </div>
      </div>

      <div class="table-wrapper">
        <el-table :data="manualRows" v-loading="loading" :row-class-name="getRowClass">
          <el-table-column prop="itemName" label="项目" min-width="150">
            <template #default="scope">
              <div class="item-tag">{{ scope.row.itemName }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="athleteName" label="姓名" width="120">
            <template #default="scope">
              <div class="name-cell">
                <div class="name-avatar">{{ (scope.row.athleteName || '?').charAt(0) }}</div>
                <span>{{ scope.row.athleteName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="deptName" label="部门/班级" min-width="170">
            <template #default="scope">
              <span class="remark-text">{{ scope.row.deptName || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="registrationStatus" label="报名状态" width="110">
            <template #default="scope">
              <div class="status-badge status-approved">
                <span class="status-dot"></span>
                {{ scope.row.registrationStatus || 'APPROVED' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="成绩" width="150">
            <template #default="scope">
              <el-input
                v-model="scope.row.editScoreValue"
                placeholder="如 12.34"
                :disabled="scope.row.isPublished || isRowSaving(scope.row.registrationId)"
                @input="markRowDirty(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="名次" width="120">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.editScoreRank"
                :min="1"
                :step="1"
                controls-position="right"
                style="width: 100%"
                :disabled="scope.row.isPublished || isRowSaving(scope.row.registrationId)"
                @change="markRowDirty(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="180">
            <template #default="scope">
              <el-input
                v-model="scope.row.editRemark"
                placeholder="可选备注"
                :disabled="scope.row.isPublished || isRowSaving(scope.row.registrationId)"
                @input="markRowDirty(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <div class="status-badge" :class="scope.row.isPublished ? 'status-published' : 'status-draft'">
                <span class="status-dot"></span>
                {{ scope.row.isPublished ? '已发布' : (scope.row.hasScore ? '草稿' : '未录入') }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="scope">
              <el-button
                class="save-btn"
                size="small"
                type="primary"
                :disabled="scope.row.isPublished || !canSaveRow(scope.row)"
                :loading="isRowSaving(scope.row.registrationId)"
                @click="saveRow(scope.row)"
              >
                保存
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="empty-state" v-if="!loading && manualRows.length === 0">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none"><path d="M18 20V10M12 20V4M6 20v-6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <h3>暂无可录入名单</h3>
        <p>请先选择赛事，并确保已有审核通过的报名记录；也可以继续使用模板导入成绩。</p>
      </div>
    </el-card>

    <el-dialog v-model="showImportDialog" title="Excel 导入成绩" width="480px" class="import-dialog">
      <div class="dialog-body">
        <el-upload
          drag
          action="#"
          :http-request="handleUpload"
          :show-file-list="false"
          accept=".xlsx,.xls"
          class="upload-area"
        >
          <div class="upload-content">
            <div class="upload-icon-wrap">
              <svg viewBox="0 0 24 24" fill="none"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><polyline points="17 8 12 3 7 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><line x1="12" y1="3" x2="12" y2="15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
            </div>
            <div class="upload-main-text">点击或拖拽上传文件</div>
            <div class="upload-sub-text">支持 .xlsx / .xls 格式</div>
          </div>
        </el-upload>

        <transition name="result-fade">
          <div v-if="importResult" class="import-result-card">
            <div class="result-row">
              <div class="result-item success-item">
                <svg viewBox="0 0 24 24" fill="none" class="result-icon"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/><path d="M8 12l3 3 5-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                <span>成功 <strong>{{ importResult.successCount }}</strong> 条</span>
              </div>
              <div class="result-item fail-item" v-if="importResult.failCount > 0">
                <svg viewBox="0 0 24 24" fill="none" class="result-icon"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/><path d="M15 9l-6 6M9 9l6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                <span>失败 <strong>{{ importResult.failCount }}</strong> 条</span>
              </div>
            </div>

            <div class="failure-table-wrap" v-if="importResult.failures && importResult.failures.length">
              <div class="failure-table-title">失败明细</div>
              <el-table :data="importResult.failures" max-height="200" class="failure-table" border>
                <el-table-column prop="rowNumber" label="行号" width="80" />
                <el-table-column prop="reason" label="失败原因" />
              </el-table>
            </div>
          </div>
        </transition>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEventList } from '@/api/event'
import { getProjectsByEventId } from '@/api/project'
import { downloadScoreTemplate, exportScore, getScoreEntryCandidates, importScores, publishScores, upsertScore } from '@/api/score'
import { useUserStore } from '@/stores/user'
import { isSuccess } from '@/utils/result'
import SmartSelect from '@/components/SmartSelect.vue'

const userStore = useUserStore()
const loading = ref(false)
const publishing = ref(false)
const batchSaving = ref(false)
const showImportDialog = ref(false)
const importResult = ref(null)
const eventOptions = ref([])
const itemOptions = ref([])
const manualRows = ref([])
const candidateRows = ref([])
const rowSavingMap = ref({})
const entryStatusOptions = [
  { label: '全部记录', value: 'ALL' },
  { label: '未录入', value: 'UNENTERED' },
  { label: '待发布', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' }
]

const query = reactive({
  eventId: null,
  itemId: null,
  entryStatus: 'ALL'
})

const summary = computed(() => {
  const rows = manualRows.value || []
  return {
    total: rows.length,
    publishedCount: rows.filter(row => row.isPublished).length,
    draftCount: rows.filter(row => row.hasScore && !row.isPublished).length,
    itemCount: new Set(rows.map(row => row.itemName)).size
  }
})

const currentRole = computed(() => userStore.userInfo?.role || userStore.userInfo?.type || userStore.userInfo?.userType || '')

const roleScopeText = computed(() => {
  if (currentRole.value === 'EVENT_ADMIN') {
    return '赛事管理员只能看到自己被分配的赛事，并仅能录入这些赛事下 APPROVED/CONFIRMED 的报名成绩。'
  }
  if (currentRole.value === 'SCHOOL_ADMIN' || currentRole.value === 'SUPER_ADMIN') {
    return '学校管理员可查看全部赛事；赛事管理员进入本页时，只会看到自己已绑定的赛事。'
  }
  return '成绩录入以后台权限为准，赛事管理员只会看到自己有权管理的赛事。'
})

const dirtySavableRows = computed(() => manualRows.value.filter(row => row.dirty && !row.isPublished && canSaveRow(row)))

const setRowSaving = (registrationId, value) => {
  rowSavingMap.value = {
    ...rowSavingMap.value,
    [registrationId]: value
  }
}

const isRowSaving = (registrationId) => Boolean(rowSavingMap.value?.[registrationId])

const downloadBlob = (data, filename) => {
  const url = window.URL.createObjectURL(new Blob([data]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

const getRowClass = ({ row }) => (row.isPublished ? 'row-published' : '')

const applyManualRows = () => {
  const filteredRegistrations = (candidateRows.value || []).filter(row => {
    if (Number(row.eventId) !== Number(query.eventId)) return false
    if (query.itemId && Number(row.itemId) !== Number(query.itemId)) return false
    if (query.entryStatus === 'UNENTERED') return !row.hasScore
    if (query.entryStatus === 'DRAFT') return row.hasScore && !row.isPublished
    if (query.entryStatus === 'PUBLISHED') return row.isPublished
    return true
  })

  manualRows.value = filteredRegistrations.map(row => {
    return {
      ...row,
      registrationId: Number(row.registrationId || row.id),
      hasScore: Boolean(row.scoreId),
      scoreId: row.scoreId || null,
      scoreValue: row.scoreValue || '',
      scoreRank: row.scoreRank ?? null,
      remark: row.remark || '',
      isPublished: Boolean(row.isPublished),
      editScoreValue: row.scoreValue || '',
      editScoreRank: row.scoreRank ?? null,
      editRemark: row.remark || '',
      dirty: false
    }
  })
}

const loadEvents = async () => {
  const res = await getEventList({ currentPage: 1, pageSize: 200 })
  if (isSuccess(res)) {
    eventOptions.value = res.data.records || []
    if (!query.eventId && eventOptions.value.length) {
      query.eventId = eventOptions.value[0].id
      await handleEventChange(query.eventId)
    }
  }
}

const loadProjects = async (eventId) => {
  if (!eventId) {
    itemOptions.value = []
    return
  }
  const res = await getProjectsByEventId(eventId)
  if (isSuccess(res)) {
    itemOptions.value = res.data || []
  } else {
    itemOptions.value = []
  }
}

const loadCandidates = async () => {
  if (!query.eventId) {
    candidateRows.value = []
    return
  }
  const res = await getScoreEntryCandidates(query.eventId, query.itemId || undefined)
  if (isSuccess(res)) {
    candidateRows.value = (res.data || []).map(row => ({
      ...row,
      hasScore: Boolean(row.scoreId)
    }))
  } else {
    candidateRows.value = []
  }
}

const loadData = async () => {
  if (!query.eventId) return
  loading.value = true
  try {
    await loadCandidates()
    applyManualRows()
  } finally {
    loading.value = false
  }
}

const handleEventChange = async (eventId) => {
  query.itemId = null
  await loadProjects(eventId)
  await loadData()
}

const markRowDirty = (row) => {
  row.dirty = true
}

const canSaveRow = (row) => {
  const hasValue = String(row.editScoreValue || '').trim().length > 0
  const hasRank = Number(row.editScoreRank) > 0
  return hasValue && hasRank
}

const persistRow = async (row, { silent = false } = {}) => {
  if (row.isPublished || !canSaveRow(row) || isRowSaving(row.registrationId)) return
  setRowSaving(row.registrationId, true)
  try {
    const payload = {
      registrationId: row.registrationId,
      scoreValue: String(row.editScoreValue).trim(),
      scoreRank: Number(row.editScoreRank),
      remark: String(row.editRemark || '').trim()
    }
    const res = await upsertScore(payload)
    if (isSuccess(res)) {
      if (!silent) {
        ElMessage.success('保存成功')
      }
      row.dirty = false
    }
  } finally {
    setRowSaving(row.registrationId, false)
  }
}

const saveRow = async (row) => {
  await persistRow(row)
  await loadData()
}

const saveDirtyRows = async () => {
  if (batchSaving.value || !dirtySavableRows.value.length) return
  batchSaving.value = true
  let successCount = 0
  let failedCount = 0
  try {
    for (const row of dirtySavableRows.value) {
      try {
        await persistRow(row, { silent: true })
        if (!row.dirty) {
          successCount += 1
        }
      } catch (error) {
        failedCount += 1
      }
    }
    if (successCount) {
      ElMessage.success(`批量保存完成，成功 ${successCount} 条`)
    }
    if (failedCount) {
      ElMessage.warning(`有 ${failedCount} 条保存失败，请检查后重试`)
    }
    await loadData()
  } finally {
    batchSaving.value = false
  }
}

const downloadTemplate = async () => {
  if (!query.eventId) {
    ElMessage.warning('请先选择赛事')
    return
  }
  const res = await downloadScoreTemplate(query.eventId)
  downloadBlob(res, '成绩导入模板.xlsx')
}

const exportScores = async () => {
  if (!query.eventId) {
    ElMessage.warning('请先选择赛事')
    return
  }
  const res = await exportScore(query.eventId)
  downloadBlob(res, '成绩表.xlsx')
}

const handleUpload = async (options) => {
  if (!query.eventId) {
    ElMessage.error('请先选择赛事')
    return
  }
  if (publishing.value) return
  publishing.value = true
  try {
    const res = await importScores(query.eventId, options.file)
    if (isSuccess(res)) {
      importResult.value = res.data
      ElMessage.success('导入完成')
      await loadData()
    }
  } finally {
    publishing.value = false
  }
}

const publishWithConfirm = async () => {
  if (!query.eventId || publishing.value || !summary.value.draftCount) return
  await ElMessageBox.confirm('发布后成绩不可撤回，是否继续？', '二次确认', {
    type: 'warning',
    confirmButtonText: '确认发布',
    cancelButtonText: '取消'
  }).then(async () => {
    publishing.value = true
    try {
      const res = await publishScores(query.eventId)
      if (isSuccess(res)) {
        ElMessage.success('发布成功')
        await loadData()
      }
    } finally {
      publishing.value = false
    }
  }).catch(() => {})
}

onMounted(() => {
  loadEvents()
})
</script>

<style scoped>
.score-manage-container {
  padding: 24px;
  min-height: 100vh;
}

.main-card {
  border-radius: 16px !important;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06) !important;
  border: 1px solid var(--el-border-color-lighter) !important;
  overflow: hidden;
}

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
  flex-shrink: 0;
}

.header-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #a78bfa, #7c3aed);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.28);
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

.header-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-group,
.op-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.action-divider {
  width: 1px;
  height: 28px;
  background: var(--el-border-color);
  margin: 0 4px;
}

.action-btn,
.op-btn,
.save-btn {
  display: inline-flex !important;
  align-items: center;
  gap: 5px;
  height: auto !important;
  padding: 8px 14px !important;
  border-radius: 8px !important;
  font-size: 13px !important;
  font-weight: 600 !important;
}

.action-btn {
  background: linear-gradient(135deg, var(--el-color-primary-light-3), var(--el-color-primary)) !important;
  border: none !important;
  color: #fff !important;
}

.op-btn {
  background: var(--el-fill-color) !important;
  border: 1px solid var(--el-border-color) !important;
  color: var(--el-text-color-regular) !important;
}

.op-import {
  background: linear-gradient(135deg, #34d399, #10b981) !important;
  border: none !important;
  color: #fff !important;
}

.op-batch-save {
  background: linear-gradient(135deg, #60a5fa, #2563eb) !important;
  border: none !important;
  color: #fff !important;
}

.op-publish {
  background: linear-gradient(135deg, #fbbf24, #f59e0b) !important;
  border: none !important;
  color: #fff !important;
}

.btn-icon {
  width: 14px;
  height: 14px;
}

.score-summary {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 14px 20px;
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
}

.summary-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
}

.summary-divider {
  width: 1px;
  height: 36px;
  background: var(--el-border-color-lighter);
}

.summary-value {
  font-size: 26px;
  font-weight: 800;
  color: var(--el-text-color-primary);
  line-height: 1;
}

.summary-value.published {
  color: #10b981;
}

.summary-value.draft {
  color: #f59e0b;
}

.summary-value.items {
  color: #7c3aed;
}

.summary-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.tip-bar {
  margin-bottom: 14px;
  padding: 12px 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.08), rgba(59, 130, 246, 0.06));
  border: 1px solid rgba(124, 58, 237, 0.12);
}

.tip-copy {
  font-size: 13px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
}

.role-copy {
  margin-top: 4px;
}

.table-wrapper {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}

.item-tag {
  display: inline-block;
  padding: 3px 10px;
  background: rgba(124, 58, 237, 0.08);
  color: #7c3aed;
  border: 1px solid rgba(124, 58, 237, 0.2);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  background: linear-gradient(135deg, #c4b5fd, #7c3aed);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.remark-text {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

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

.status-approved {
  background: var(--el-color-success-light-9);
  color: var(--el-color-success-dark-2);
  border: 1px solid var(--el-color-success-light-5);
}

.status-approved .status-dot,
.status-published .status-dot {
  background: var(--el-color-success);
}

.status-published {
  background: var(--el-color-success-light-9);
  color: var(--el-color-success-dark-2);
  border: 1px solid var(--el-color-success-light-5);
}

.status-draft {
  background: #fff7ed;
  color: #c2410c;
  border: 1px solid #fed7aa;
}

.status-draft .status-dot {
  background: #f59e0b;
}

.save-btn {
  min-width: 74px;
  justify-content: center;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 20px;
  gap: 12px;
}

.empty-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: var(--el-fill-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-placeholder);
}

.empty-icon svg {
  width: 28px;
  height: 28px;
}

.empty-state h3,
.empty-state p {
  margin: 0;
  text-align: center;
}

.empty-state h3 {
  font-size: 18px;
  color: var(--el-text-color-primary);
}

.empty-state p {
  font-size: 14px;
  color: var(--el-text-color-placeholder);
  max-width: 360px;
  line-height: 1.6;
}

.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-area :deep(.el-upload-dragger) {
  border-radius: 12px !important;
  border: 2px dashed var(--el-border-color) !important;
  background: var(--el-fill-color-lighter) !important;
  padding: 32px 20px !important;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: rgba(124, 58, 237, 0.1);
  color: #7c3aed;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-icon-wrap svg {
  width: 24px;
  height: 24px;
}

.upload-main-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.upload-sub-text {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.import-result-card {
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-row {
  display: flex;
  gap: 20px;
  align-items: center;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 14px;
  font-weight: 500;
}

.result-icon {
  width: 18px;
  height: 18px;
}

.success-item {
  color: var(--el-color-success);
}

.fail-item {
  color: var(--el-color-danger);
}

.failure-table-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.result-fade-enter-active,
.result-fade-leave-active {
  transition: all 0.3s ease;
}

.result-fade-enter-from,
.result-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
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

:deep(.el-table__header th) {
  background: var(--el-fill-color-light) !important;
  font-weight: 700;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

:deep(.row-published td) {
  background: var(--el-color-success-light-9) !important;
}

@media (max-width: 1100px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .action-divider {
    display: none;
  }
}
</style>
