<template>
  <div class="registrations-container">
    <!-- Header -->
    <div class="page-header">
      <div class="header-watermark">RECORDS</div>
      <div class="header-content">
        <div class="page-title">
          <div class="title-content">
            <h2>我的报名</h2>
            <p class="subtitle">追踪你的每一场赛事，记录每一次挑战</p>
          </div>
          <span class="total-badge" v-if="total > 0">共参与 {{ total }} 项</span>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="content-area" v-loading="loading">
      
      <!-- Empty State -->
      <div v-if="!loading && registrationList.length === 0" class="empty-state">
        <div class="empty-icon-wrapper">
          <div class="empty-icon">📝</div>
          <div class="empty-glow"></div>
        </div>
        <p class="empty-text">暂无报名记录</p>
        <p class="empty-sub">去赛事大厅看看有什么感兴趣的比赛吧</p>
        <el-button type="primary" class="go-events-btn" @click="router.push('/event')">
          发现赛事
        </el-button>
      </div>

      <!-- Registration List -->
      <div v-else class="registration-list">
        <div 
          class="registration-card" 
          v-for="(item, index) in registrationList" 
          :key="item.id || item.registrationId"
          :style="{ animationDelay: `${index * 0.08}s` }"
        >
          <!-- Card Header (Status indicator) -->
          <div class="card-status-bar" :class="getStatusStyle(item.registrationStatus).class"></div>
          
          <div class="card-inner">
            <!-- Left: Info -->
            <div class="reg-info">
              <div class="reg-header">
                <span class="item-tag">
                  <el-icon><Ticket /></el-icon>
                  {{ item.itemName }}
                </span>
                <div class="status-badge" :class="getStatusStyle(item.registrationStatus).class">
                  <el-icon class="status-icon">
                    <component :is="getStatusStyle(item.registrationStatus).icon" />
                  </el-icon>
                  {{ getStatusLabel(item.registrationStatus) }}
                </div>
              </div>
              
              <h3 class="event-name" :title="item.eventName">{{ item.eventName }}</h3>
              
              <div class="reg-meta">
                <div class="meta-item">
                  <el-icon><Clock /></el-icon>
                  <span>报名时间：{{ formatDate(item.registrationTime) }}</span>
                </div>
                <div v-if="item.registrationStatus === 'REJECTED'" class="meta-item reject-reason">
                  <el-icon><WarningFilled /></el-icon>
                  <span>拒绝原因：{{ item.rejectReason || '无' }}</span>
                </div>
              </div>
            </div>

            <!-- Right: Action -->
            <div class="reg-action">
              <el-button 
                v-if="item.registrationStatus !== 'CANCELLED' && item.registrationStatus !== 'REJECTED'"
                class="cancel-btn" 
                plain 
                @click="handleCancel(item)"
              >
                取消报名
              </el-button>
              <div v-else class="disabled-action-text">
                <el-icon><Lock /></el-icon>
                <span>无法操作</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.currentPage"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRegistrationList, cancelRegistration } from '@/api/registration'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { Ticket, Clock, WarningFilled, SuccessFilled, CircleCloseFilled, InfoFilled, Lock } from '@element-plus/icons-vue'

const router = useRouter()
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
  ElMessageBox.confirm(
    `<div style="font-weight: bold; margin-bottom: 8px;">确认取消报名？</div>
     <div style="color: var(--el-text-color-secondary); font-size: 13px;">取消后将无法恢复，如需参加需重新报名。</div>`,
    '提示', 
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确认取消',
      cancelButtonText: '暂不取消',
      confirmButtonClass: 'el-button--danger',
      type: 'warning',
      customClass: 'custom-confirm-box'
    }
  ).then(async () => {
    try {
      const res = await cancelRegistration(row.id || row.registrationId)
      if (res.code === 200) {
        ElMessage.success({ message: '已成功取消报名', duration: 2000 })
        getList()
      }
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

const formatDate = (dateStr) => {
  if (!dateStr) return '未知时间'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', month: '2-digit', day: '2-digit', 
    hour: '2-digit', minute: '2-digit' 
  })
}

const getStatusStyle = (status) => {
  const styles = {
    'APPROVED':  { class: 'status-success', icon: 'SuccessFilled' },
    'CONFIRMED': { class: 'status-success', icon: 'SuccessFilled' },
    'PENDING':   { class: 'status-warning', icon: 'WarningFilled' },
    'REJECTED':  { class: 'status-danger',  icon: 'CircleCloseFilled' },
    'CANCELLED': { class: 'status-info',    icon: 'InfoFilled' }
  }
  return styles[status] || { class: 'status-default', icon: 'InfoFilled' }
}

const getStatusLabel = (status) => {
  const labels = {
    'APPROVED': '审核通过',
    'REJECTED': '审核拒绝',
    'PENDING': '审核中',
    'CANCELLED': '已取消',
    'CONFIRMED': '已确认'
  }
  return labels[status] || status || '未知状态'
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
/* ── Container ── */
.registrations-container {
  padding: 32px 40px;
  min-height: 100%;
  background-color: var(--el-bg-color-page);
  transition: background-color 0.3s ease;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* ── Header ── */
.page-header {
  position: relative;
  margin-bottom: 40px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  z-index: 1;
}

.header-watermark {
  position: absolute;
  top: -40px;
  left: -20px;
  font-size: 140px;
  font-weight: 900;
  color: var(--el-text-color-primary);
  opacity: 0.02;
  z-index: -1;
  pointer-events: none;
  letter-spacing: -0.02em;
  user-select: none;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
  flex-wrap: wrap;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 24px;
}

.title-content h2 {
  margin: 0;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--el-text-color-primary);
}

.subtitle {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.total-badge {
  font-size: 13px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 6px 14px;
  border-radius: 24px;
  font-weight: 600;
  border: 1px solid var(--el-color-primary-light-8);
  white-space: nowrap;
}

/* ── Content Area ── */
.content-area {
  position: relative;
  min-height: 400px;
}

/* ── Empty State ── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
  background: var(--el-bg-color);
  border-radius: 24px;
  border: 1px dashed var(--el-border-color-lighter);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.02);
}

.empty-icon-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.empty-icon {
  font-size: 48px;
  z-index: 2;
  filter: grayscale(1);
  opacity: 0.6;
}

.empty-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  background: var(--el-color-info);
  filter: blur(30px);
  opacity: 0.1;
  border-radius: 50%;
  z-index: 1;
}

.empty-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
}

.empty-sub {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin: 0 0 24px 0;
}

.go-events-btn {
  border-radius: 20px;
  padding: 0 24px;
  font-weight: 600;
}

/* ── Registration List ── */
.registration-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.registration-card {
  position: relative;
  background: var(--el-bg-color);
  border-radius: 16px;
  border: 1px solid var(--el-border-color-lighter);
  overflow: hidden;
  display: flex;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  animation: slideInUp 0.5s backwards;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02);
}

.registration-card:hover {
  transform: translateX(4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
  border-color: var(--el-border-color-light);
}

:deep(.dark) .registration-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.3);
}

@keyframes slideInUp {
  from { opacity: 0; transform: translateY(15px) translateX(-10px); }
  to { opacity: 1; transform: translateY(0) translateX(0); }
}

.card-status-bar {
  width: 6px;
  flex-shrink: 0;
}

/* Status Colors */
.status-success { background-color: var(--el-color-success); color: var(--el-color-success); }
.status-warning { background-color: var(--el-color-warning); color: var(--el-color-warning); }
.status-danger  { background-color: var(--el-color-danger);  color: var(--el-color-danger); }
.status-info    { background-color: var(--el-color-info);    color: var(--el-color-info); }
.status-default { background-color: var(--el-text-color-placeholder); color: var(--el-text-color-placeholder); }

.card-inner {
  flex: 1;
  padding: 24px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

/* Info Section */
.reg-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.reg-header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.item-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 4px 12px;
  border-radius: 6px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  background-color: transparent;
}

.status-badge.status-success { background-color: var(--el-color-success-light-9); }
.status-badge.status-warning { background-color: var(--el-color-warning-light-9); }
.status-badge.status-danger  { background-color: var(--el-color-danger-light-9); }
.status-badge.status-info    { background-color: var(--el-color-info-light-9); color: var(--el-text-color-secondary); }

.status-icon {
  font-size: 14px;
}

.event-name {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

.reg-meta {
  display: flex;
  align-items: center;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.meta-item .el-icon {
  color: var(--el-text-color-secondary);
  font-size: 15px;
}

.meta-item.reject-reason {
  color: var(--el-color-danger);
}

/* Action Section */
.reg-action {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 120px;
}

.cancel-btn {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  border-color: var(--el-border-color);
  color: var(--el-text-color-regular);
}

.cancel-btn:hover {
  color: var(--el-color-danger);
  border-color: var(--el-color-danger-light-5);
  background-color: var(--el-color-danger-light-9);
  transform: scale(1.02);
}

.disabled-action-text {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--el-text-color-placeholder);
  font-weight: 500;
}

/* ── Pagination ── */
.pagination-container {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: var(--el-color-primary);
  font-weight: 700;
}

/* ── Global Message Box Customization ── */
:deep(.custom-confirm-box) {
  border-radius: 16px;
  padding: 24px;
}

/* Responsive */
@media (max-width: 768px) {
  .registrations-container {
    padding: 24px 16px;
  }
  
  .header-watermark {
    font-size: 80px;
    top: -20px;
  }

  .card-inner {
    flex-direction: column;
    align-items: flex-start;
    padding: 20px;
    gap: 16px;
  }

  .reg-action {
    width: 100%;
    justify-content: flex-start;
    padding-top: 16px;
    border-top: 1px dashed var(--el-border-color-lighter);
  }
}
</style>
