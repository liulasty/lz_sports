<template>
  <div class="event-container">
    <!-- Header -->
    <div class="page-header">
      <div class="header-watermark">EVENTS</div>
      <div class="header-content">
        <div class="page-title">
          <div class="title-content">
            <h2>赛事探索</h2>
            <p class="subtitle">发现身边的精彩体育赛事，超越自我</p>
          </div>
          <span class="total-badge" v-if="total > 0">共 {{ total }} 场赛事</span>
        </div>
        <div class="filter-box">
          <el-input
              v-model="queryParams.name"
              placeholder="搜索赛事名称…"
              class="search-input"
              @keyup.enter="handleQuery"
              clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" class="query-btn" @click="handleQuery">
            探索
          </el-button>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="eventList.length === 0" class="empty-state">
      <div class="empty-icon-wrapper">
        <div class="empty-icon">🏆</div>
        <div class="empty-glow"></div>
      </div>
      <p class="empty-text">暂无赛事</p>
      <p class="empty-sub">敬请期待更多精彩内容</p>
    </div>

    <!-- Grid -->
    <div v-else class="event-grid">
      <div
          class="event-card"
          v-for="(item, index) in eventList"
          :key="item.id"
          @click="goDetail(item.id)"
          :style="{ animationDelay: `${index * 0.05}s` }"
      >
        <!-- Image -->
        <div class="card-image">
          <img
              v-if="item.imageUrls && item.imageUrls.length > 0"
              :src="item.imageUrls[0]"
              :alt="item.name"
          />
          <div v-else class="image-placeholder">
            <el-icon class="placeholder-icon"><Trophy /></el-icon>
          </div>
          <div class="image-overlay"></div>
          <!-- Status badge -->
          <div class="status-badge" :class="getStatusClass(item)">
            <span class="status-dot"></span>
            {{ getStatus(item) }}
          </div>
        </div>

        <!-- Body -->
        <div class="card-body">
          <div class="event-type-tag">{{ item.type || '综合赛事' }}</div>
          <h3 class="event-title" :title="item.name">{{ item.name }}</h3>
          <div class="event-meta">
            <div class="meta-item">
              <el-icon class="meta-icon"><Calendar /></el-icon>
              <span>{{ formatDate(item.date) }}</span>
            </div>
          </div>
        </div>
        
        <div class="card-footer">
          <div class="fee-info">
            <span class="fee-currency">¥</span>
            <span class="fee-amount">{{ item.fee }}</span>
            <span class="fee-label">起</span>
          </div>
          <div class="action-btn">
            <span>查看详情</span>
            <el-icon class="arrow-icon"><Right /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination-container" v-if="total > 0">
      <el-pagination
          v-model:current-page="queryParams.currentPage"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[8, 16, 32]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getEventList } from '@/api/event'
import { useRouter } from 'vue-router'
import { Search, Calendar, Right } from '@element-plus/icons-vue'

// Trophy icon fallback (may not exist in all EP versions)
const Trophy = { template: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9H4a2 2 0 01-2-2V5h4M18 9h2a2 2 0 002-2V5h-4M12 17v4M8 21h8M3 5h18M12 13a4 4 0 004-4V5H8v4a4 4 0 004 4z"/></svg>' }

const router = useRouter()
const eventList = ref([])
const total = ref(0)
const queryParams = reactive({
  currentPage: 1,
  pageSize: 8,
  name: ''
})

const getList = async () => {
  try {
    const res = await getEventList(queryParams)
    if (res.code === 200) {
      const records = res.data.records || res.data.rows || []
      eventList.value = records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  }
}

const goDetail = (id) => router.push(`/event/${id}`)
const handleQuery = () => { queryParams.currentPage = 1; getList() }
const handleSizeChange = (val) => { queryParams.pageSize = val; getList() }
const handleCurrentChange = (val) => { queryParams.currentPage = val; getList() }

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const getStatus = (item) => {
  const map = { OPEN: '报名中', CLOSED: '报名截止', ONGOING: '进行中', FINISHED: '已结束' }
  return map[item.status] || '草稿'
}

const getStatusClass = (item) => {
  const map = { OPEN: 'status-open', ONGOING: 'status-ongoing', CLOSED: 'status-closed', FINISHED: 'status-finished' }
  return map[item.status] || 'status-draft'
}

onMounted(() => getList())
</script>

<style scoped>
/* ── Container ── */
.event-container {
  padding: 32px 40px;
  min-height: 100%;
  background-color: var(--el-bg-color-page);
  transition: background-color 0.3s ease;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* ── Header ── */
.page-header {
  position: relative;
  margin-bottom: 48px;
  padding-bottom: 32px;
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
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--el-text-color-primary);
  background: linear-gradient(135deg, var(--el-text-color-primary) 0%, var(--el-text-color-regular) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle {
  margin: 8px 0 0 0;
  font-size: 15px;
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

.filter-box {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--el-bg-color);
  padding: 8px;
  border-radius: 14px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--el-border-color-lighter);
  transition: box-shadow 0.3s ease, border-color 0.3s ease;
}

.filter-box:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border-color: var(--el-color-primary-light-7);
}

:deep(.dark) .filter-box {
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.2);
}

:deep(.search-input .el-input__wrapper) {
  width: 280px;
  border-radius: 8px;
  background: transparent;
  border: none;
  box-shadow: none !important;
}

:deep(.search-input .el-input__inner) {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.query-btn {
  border-radius: 10px !important;
  font-weight: 600;
  padding: 0 28px;
  height: 40px;
  border: none;
  background: var(--el-color-primary);
  color: white;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  letter-spacing: 0.05em;
}

.query-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px var(--el-color-primary-light-5);
  opacity: 0.95;
}

/* ── Grid ── */
.event-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 32px;
}

/* ── Card Styling ── */
.event-card {
  background: var(--el-bg-color);
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  animation: fadeInUp 0.6s backwards;
  position: relative;
}

.event-card:hover {
  transform: translateY(-8px) scale(1.01);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.08);
  border-color: var(--el-border-color-light);
}

:deep(.dark) .event-card:hover {
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.4);
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Image */
.card-image {
  position: relative;
  width: 100%;
  height: 240px;
  overflow: hidden;
  background: var(--el-fill-color-light);
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.event-card:hover .card-image img {
  transform: scale(1.08);
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.5) 0%, transparent 40%);
  opacity: 0.4;
  transition: opacity 0.4s ease;
}

.event-card:hover .image-overlay {
  opacity: 0.7;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--el-fill-color), var(--el-fill-color-darker));
}

.placeholder-icon {
  font-size: 56px;
  color: var(--el-text-color-placeholder);
}

/* Status Badge */
.status-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 30px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  gap: 6px;
  color: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  border: 1px solid rgba(255,255,255,0.15);
  z-index: 2;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 8px currentColor;
}

.status-open    { background: rgba(16, 185, 129, 0.75); }
.status-ongoing { background: rgba(255, 107, 53, 0.75); }
.status-closed  { background: rgba(107, 114, 128, 0.75); }
.status-finished{ background: rgba(15, 23, 42, 0.75); }
.status-draft   { background: rgba(245, 158, 11, 0.75); }

/* Card Body */
.card-body {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.event-type-tag {
  font-size: 12px;
  font-weight: 700;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 4px 10px;
  border-radius: 6px;
  align-self: flex-start;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.event-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
}

.event-card:hover .event-title {
  color: var(--el-color-primary);
}

.event-meta {
  margin-top: auto;
  display: flex;
  gap: 16px;
  padding-top: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-regular);
}

.meta-icon {
  font-size: 16px;
  color: var(--el-text-color-secondary);
}

/* Card Footer */
.card-footer {
  padding: 20px 24px;
  border-top: 1px solid var(--el-border-color-lighter);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--el-fill-color-blank);
}

.fee-info {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.fee-currency {
  font-size: 14px;
  font-weight: 700;
  color: var(--el-color-primary);
}

.fee-amount {
  font-size: 24px;
  font-weight: 800;
  color: var(--el-color-primary);
  letter-spacing: -0.02em;
}

.fee-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-left: 4px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-regular);
  transition: color 0.3s ease;
}

.arrow-icon {
  font-size: 16px;
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.event-card:hover .action-btn {
  color: var(--el-color-primary);
}

.event-card:hover .arrow-icon {
  transform: translateX(6px);
}

/* ── Empty State ── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120px 0;
}

.empty-icon-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.empty-icon {
  font-size: 64px;
  z-index: 2;
}

.empty-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  background: var(--el-color-primary);
  filter: blur(40px);
  opacity: 0.15;
  border-radius: 50%;
  z-index: 1;
}

.empty-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
}

.empty-sub {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin: 0;
}

/* ── Pagination ── */
.pagination-container {
  margin-top: 56px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: var(--el-color-primary);
  font-weight: 700;
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .event-container {
    padding: 24px 16px;
  }
  
  .header-watermark {
    font-size: 80px;
    top: -20px;
  }

  .title-content h2 {
    font-size: 28px;
  }

  .event-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-box {
    width: 100%;
  }
  
  :deep(.search-input .el-input__wrapper) {
    width: 100%;
  }
}
</style>
