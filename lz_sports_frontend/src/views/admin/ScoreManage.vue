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
              <span class="header-subtitle">查询、导入、导出并发布赛事成绩</span>
            </div>
          </div>

          <div class="header-right">
            <!-- 筛选区 -->
            <div class="filter-group">
              <el-select v-model="query.eventId" placeholder="选择赛事" style="width: 185px" @change="handleEventChange">
                <el-option v-for="item in eventOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
              <el-select v-model="query.itemId" placeholder="选择项目" clearable style="width: 165px">
                <el-option v-for="item in itemOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
              <el-button type="primary" class="action-btn" @click="loadScores">
                <svg viewBox="0 0 24 24" fill="none" class="btn-icon"><circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2"/><path d="M21 21l-4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                查询
              </el-button>
            </div>

            <!-- 操作区 -->
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
              <el-button class="op-btn op-publish" :loading="submitting" :disabled="submitting" @click="publishWithConfirm">
                <svg v-if="!submitting" viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M22 2L11 13M22 2L15 22L11 13L2 9L22 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                发布成绩
              </el-button>
            </div>
          </div>
        </div>
      </template>

      <!-- 成绩统计小卡片 -->
      <div class="score-summary" v-if="scores.length > 0">
        <div class="summary-item">
          <div class="summary-value">{{ scores.length }}</div>
          <div class="summary-label">成绩总数</div>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <div class="summary-value published">{{ scores.filter(s => s.isPublished).length }}</div>
          <div class="summary-label">已发布</div>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <div class="summary-value draft">{{ scores.filter(s => !s.isPublished).length }}</div>
          <div class="summary-label">草稿</div>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <div class="summary-value items">{{ new Set(scores.map(s => s.itemName)).size }}</div>
          <div class="summary-label">参与项目</div>
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrapper">
        <el-table :data="scores" v-loading="loading" :row-class-name="getRowClass">
          <el-table-column prop="eventName" label="赛事" min-width="160">
            <template #default="scope">
              <div class="event-cell">
                <svg viewBox="0 0 24 24" fill="none" class="cell-icon"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                <span>{{ scope.row.eventName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="itemName" label="项目" min-width="140">
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
          <el-table-column prop="scoreValue" label="成绩" width="120">
            <template #default="scope">
              <span class="score-value">{{ scope.row.scoreValue }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="scoreRank" label="排名" width="100">
            <template #default="scope">
              <div class="rank-badge" :class="getRankClass(scope.row.scoreRank)">
                <span v-if="scope.row.scoreRank <= 3 && scope.row.scoreRank">
                  <svg viewBox="0 0 24 24" fill="none" class="rank-icon"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" fill="currentColor"/></svg>
                </span>
                {{ scope.row.scoreRank || '—' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="120">
            <template #default="scope">
              <span class="remark-text">{{ scope.row.remark || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="scope">
              <div class="status-badge" :class="scope.row.isPublished ? 'status-published' : 'status-draft'">
                <span class="status-dot"></span>
                {{ scope.row.isPublished ? '已发布' : '草稿' }}
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="!loading && scores.length === 0">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none"><path d="M18 20V10M12 20V4M6 20v-6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </div>
        <p>暂无成绩数据，请选择赛事和项目后查询，或通过导入功能添加</p>
      </div>
    </el-card>

    <!-- 导入弹窗 -->
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getEventList } from '@/api/event'
import { getProjectsByEventId } from '@/api/project'
import { downloadScoreTemplate, exportScore, getScorePage, importScores, publishScores } from '@/api/score'

const loading = ref(false)
const submitting = ref(false)
const showImportDialog = ref(false)
const importResult = ref(null)
const eventOptions = ref([])
const itemOptions = ref([])
const scores = ref([])
const query = reactive({
  eventId: null,
  itemId: null
})

const getRowClass = ({ row }) => {
  return row.isPublished ? 'row-published' : ''
}

const getRankClass = (rank) => {
  if (rank === 1) return 'rank-gold'
  if (rank === 2) return 'rank-silver'
  if (rank === 3) return 'rank-bronze'
  return 'rank-normal'
}

const loadEvents = async () => {
  const res = await getEventList({ currentPage: 1, pageSize: 200 })
  if (res.code === 200) {
    eventOptions.value = res.data.records || []
    if (!query.eventId && eventOptions.value.length) {
      query.eventId = eventOptions.value[0].id
      await handleEventChange(query.eventId)
    }
  }
}

const handleEventChange = async (eventId) => {
  query.itemId = null
  const res = await getProjectsByEventId(eventId)
  if (res.code === 200) itemOptions.value = res.data || []
  loadScores()
}

const loadScores = async () => {
  if (!query.eventId) return
  loading.value = true
  try {
    const res = await getScorePage({
      currentPage: 1, pageSize: 500,
      eventId: query.eventId, itemId: query.itemId
    })
    if (res.code === 200) scores.value = res.data.records || []
  } finally {
    loading.value = false
  }
}

const downloadBlob = (data, filename) => {
  const url = window.URL.createObjectURL(new Blob([data]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const downloadTemplate = async () => {
  if (!query.eventId) return
  const res = await downloadScoreTemplate(query.eventId)
  downloadBlob(res, '成绩导入模板.xlsx')
}

const exportScores = async () => {
  if (!query.eventId) return
  const res = await exportScore(query.eventId)
  downloadBlob(res, '成绩表.xlsx')
}

const handleUpload = async (options) => {
  if (!query.eventId) { ElMessage.error('请先选择赛事'); return }
  if (submitting.value) return
  submitting.value = true
  try {
    const res = await importScores(query.eventId, options.file)
    if (res.code === 200) {
      importResult.value = res.data
      ElMessage.success('导入完成')
      loadScores()
    }
  } finally {
    submitting.value = false
  }
}

const publishWithConfirm = async () => {
  if (!query.eventId || submitting.value) return
  await ElMessageBox.confirm('发布后成绩不可撤回，是否继续？', '二次确认', {
    type: 'warning',
    confirmButtonText: '确认发布',
    cancelButtonText: '取消'
  }).then(async () => {
    submitting.value = true
    try {
      const res = await publishScores(query.eventId)
      if (res.code === 200) { ElMessage.success('发布成功'); loadScores() }
    } finally {
      submitting.value = false
    }
  }).catch(() => {})
}

onMounted(() => { loadEvents() })
</script>

<style scoped>
/* ===================== Layout ===================== */
.score-manage-container {
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
.header-icon svg { width: 22px; height: 22px; }
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

/* Right side */
.header-right {
  display: flex;
  align-items: center;
  gap: 0;
  flex-wrap: wrap;
  gap: 10px;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.action-divider {
  width: 1px;
  height: 28px;
  background: var(--el-border-color);
  margin: 0 4px;
}
.op-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Buttons */
.action-btn,
.op-btn {
  display: inline-flex !important;
  align-items: center;
  gap: 5px;
  height: auto !important;
  padding: 8px 14px !important;
  border-radius: 8px !important;
  font-size: 13px !important;
  font-weight: 600 !important;
  transition: transform 0.15s, box-shadow 0.15s !important;
}
.action-btn {
  background: linear-gradient(135deg, var(--el-color-primary-light-3), var(--el-color-primary)) !important;
  border: none !important;
  color: #fff !important;
  box-shadow: 0 3px 10px rgba(64, 158, 255, 0.3) !important;
}
.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 5px 14px rgba(64, 158, 255, 0.4) !important;
}
.op-btn {
  background: var(--el-fill-color) !important;
  border: 1px solid var(--el-border-color) !important;
  color: var(--el-text-color-regular) !important;
}
.op-btn:hover {
  background: var(--el-fill-color-dark) !important;
  border-color: var(--el-border-color-dark) !important;
}
.op-import {
  background: linear-gradient(135deg, #34d399, #10b981) !important;
  border: none !important;
  color: #fff !important;
  box-shadow: 0 3px 10px rgba(16, 185, 129, 0.28) !important;
}
.op-import:hover {
  transform: translateY(-1px);
  box-shadow: 0 5px 14px rgba(16, 185, 129, 0.38) !important;
}
.op-publish {
  background: linear-gradient(135deg, #fbbf24, #f59e0b) !important;
  border: none !important;
  color: #fff !important;
  box-shadow: 0 3px 10px rgba(245, 158, 11, 0.28) !important;
}
.op-publish:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 5px 14px rgba(245, 158, 11, 0.38) !important;
}
.op-publish:disabled {
  opacity: 0.5 !important;
  transform: none !important;
}
.btn-icon { width: 14px; height: 14px; }

/* ===================== Summary Bar ===================== */
.score-summary {
  display: flex;
  align-items: center;
  gap: 0;
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
  letter-spacing: -0.5px;
  line-height: 1;
}
.summary-value.published { color: #10b981; }
.summary-value.draft { color: var(--el-text-color-placeholder); }
.summary-value.items { color: #7c3aed; }
.summary-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

/* ===================== Table ===================== */
.table-wrapper {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}
.event-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.cell-icon {
  width: 13px;
  height: 13px;
  color: var(--el-text-color-placeholder);
  flex-shrink: 0;
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
.score-value {
  font-size: 15px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  font-variant-numeric: tabular-nums;
}
.remark-text {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* Rank Badge */
.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  min-width: 36px;
  padding: 3px 8px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
}
.rank-icon { width: 12px; height: 12px; }
.rank-gold   { background: #fef3c7; color: #d97706; border: 1px solid #fde68a; }
.rank-silver { background: #f1f5f9; color: #64748b; border: 1px solid #e2e8f0; }
.rank-bronze { background: #fff7ed; color: #c2410c; border: 1px solid #fed7aa; }
.rank-normal { background: var(--el-fill-color); color: var(--el-text-color-secondary); border: 1px solid var(--el-border-color-lighter); }

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
.status-published {
  background: var(--el-color-success-light-9);
  color: var(--el-color-success-dark-2);
  border: 1px solid var(--el-color-success-light-5);
}
.status-published .status-dot { background: var(--el-color-success); }
.status-draft {
  background: var(--el-fill-color);
  color: var(--el-text-color-secondary);
  border: 1px solid var(--el-border-color-lighter);
}
.status-draft .status-dot { background: var(--el-text-color-placeholder); }

/* Row highlight */
:deep(.row-published td) {
  background: var(--el-color-success-light-9) !important;
}

/* ===================== Empty State ===================== */
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
.empty-icon svg { width: 28px; height: 28px; }
.empty-state p {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-placeholder);
  text-align: center;
  max-width: 360px;
  line-height: 1.6;
}

/* ===================== Import Dialog ===================== */
.dialog-body { display: flex; flex-direction: column; gap: 16px; }
.upload-area :deep(.el-upload-dragger) {
  border-radius: 12px !important;
  border: 2px dashed var(--el-border-color) !important;
  background: var(--el-fill-color-lighter) !important;
  padding: 32px 20px !important;
  transition: border-color 0.2s, background 0.2s !important;
}
.upload-area :deep(.el-upload-dragger:hover) {
  border-color: #7c3aed !important;
  background: rgba(124, 58, 237, 0.04) !important;
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
.upload-icon-wrap svg { width: 24px; height: 24px; }
.upload-main-text { font-size: 14px; font-weight: 600; color: var(--el-text-color-primary); }
.upload-sub-text { font-size: 12px; color: var(--el-text-color-placeholder); }

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
.result-icon { width: 18px; height: 18px; }
.success-item { color: var(--el-color-success); }
.fail-item { color: var(--el-color-danger); }
.result-item strong { font-weight: 800; font-size: 16px; }
.failure-table-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}
.failure-table { border-radius: 8px; overflow: hidden; }

.result-fade-enter-active, .result-fade-leave-active { transition: all 0.3s ease; }
.result-fade-enter-from, .result-fade-leave-to { opacity: 0; transform: translateY(-6px); }

/* ===================== Table overrides ===================== */
:deep(.el-table) {
  background: var(--el-bg-color) !important;
  color: var(--el-text-color-primary);
}
:deep(.el-table__inner-wrapper) { background: var(--el-bg-color) !important; }
:deep(.el-table__body-wrapper td) { background: var(--el-bg-color) !important; }
:deep(.el-table__header th) {
  background: var(--el-fill-color-light) !important;
  font-weight: 700;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* ===================== Responsive ===================== */
@media (max-width: 1100px) {
  .card-header { flex-direction: column; align-items: flex-start; }
  .header-right { flex-wrap: wrap; }
  .action-divider { display: none; }
}
</style>