<template>
  <div class="event-container">
    <!-- Header -->
    <div class="page-header">
      <div class="page-title">
        <span class="title-accent"></span>
        <h2>赛事列表</h2>
        <span class="total-badge" v-if="total > 0">{{ total }} 场</span>
      </div>
      <div class="filter-box">
        <el-input
            v-model="queryParams.name"
            placeholder="搜索赛事名称…"
            prefix-icon="Search"
            class="search-input"
            @keyup.enter="handleQuery"
            clearable
        />
        <el-button type="primary" class="query-btn" @click="handleQuery">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="eventList.length === 0" class="empty-state">
      <div class="empty-icon">🏆</div>
      <p class="empty-text">暂无赛事</p>
      <p class="empty-sub">敬请期待更多精彩赛事</p>
    </div>

    <!-- Grid -->
    <div v-else class="event-grid">
      <div
          class="event-card"
          v-for="item in eventList"
          :key="item.id"
          @click="goDetail(item.id)"
      >
        <!-- Image -->
        <div class="card-image">
          <img
              v-if="item.imageUrls && item.imageUrls.length > 0"
              :src="item.imageUrls[0]"
              :alt="item.name"
          />
          <div v-else class="image-placeholder">
            <span class="placeholder-icon">🏅</span>
          </div>
          <!-- Status badge on image -->
          <div class="status-badge" :class="getStatusClass(item)">
            {{ getStatus(item) }}
          </div>
        </div>

        <!-- Body -->
        <div class="card-body">
          <h3 class="event-title">{{ item.name }}</h3>
          <div class="event-meta">
            <div class="meta-item">
              <el-icon class="meta-icon"><Calendar /></el-icon>
              <span>{{ formatDate(item.date) }}</span>
            </div>
            <div class="meta-item">
              <el-icon class="meta-icon"><Trophy /></el-icon>
              <span>{{ item.type }}</span>
            </div>
          </div>
          <div class="card-footer">
            <div class="fee-tag">
              <span class="fee-label">报名费</span>
              <span class="fee-value">¥{{ item.fee }}</span>
            </div>
            <div class="go-arrow">→</div>
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
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getEventList } from '@/api/event'
import { useRouter } from 'vue-router'
import { Search, Calendar } from '@element-plus/icons-vue'

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
  padding: 24px;
  min-height: 100%;
  font-family: 'Noto Sans SC', sans-serif;
}

/* ── Page Header ── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-accent {
  display: block;
  width: 4px;
  height: 22px;
  background: var(--accent, #FF6B35);
  border-radius: 2px;
}

.page-title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary, #1a1a2e);
}

.total-badge {
  font-size: 12px;
  color: var(--accent, #FF6B35);
  background: rgba(255, 107, 53, 0.1);
  border: 1px solid rgba(255, 107, 53, 0.25);
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 600;
}

.filter-box {
  display: flex;
  align-items: center;
  gap: 10px;
}

:deep(.search-input .el-input__wrapper) {
  width: 220px;
  border-radius: 8px;
  background: var(--bg-card, var(--el-bg-color));
  border: 1px solid var(--border, rgba(0,0,0,0.08));
  box-shadow: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}
:deep(.search-input .el-input__wrapper:hover),
:deep(.search-input .el-input__wrapper.is-focus) {
  border-color: var(--accent, #FF6B35) !important;
  box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.12) !important;
}
:deep(.search-input .el-input__inner) {
  color: var(--text-primary, #1a1a2e);
  font-size: 13px;
}

.query-btn {
  background: var(--accent, #FF6B35) !important;
  border-color: var(--accent, #FF6B35) !important;
  border-radius: 8px !important;
  font-weight: 600;
  letter-spacing: 0.04em;
}
.query-btn:hover {
  opacity: 0.88;
}

/* ── Empty State ── */
.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--text-secondary, var(--el-text-color-secondary));
}
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-text { font-size: 16px; font-weight: 600; margin: 0 0 6px; color: var(--text-primary, #1a1a2e); }
.empty-sub  { font-size: 13px; margin: 0; }

/* ── Grid ── */
.event-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

/* ── Event Card ── */
.event-card {
  background: var(--bg-card, var(--el-bg-color));
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid var(--border, rgba(0,0,0,0.06));
  cursor: pointer;
  transition: transform 0.22s cubic-bezier(0.34,1.56,0.64,1), box-shadow 0.22s;
  display: flex;
  flex-direction: column;
}

.event-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12);
}

/* Image */
.card-image {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: var(--bg-placeholder, var(--el-bg-color-page));
  flex-shrink: 0;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.event-card:hover .card-image img {
  transform: scale(1.05);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--bg-placeholder, var(--el-bg-color-page)), var(--bg-card, var(--el-bg-color)));
}
.placeholder-icon { font-size: 40px; opacity: 0.4; }

/* Status badge */
.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  padding: 3px 10px;
  border-radius: 20px;
  backdrop-filter: blur(8px);
}
.status-open    { background: rgba(16, 185, 129, 0.85); color: #fff; }
.status-ongoing { background: rgba(255, 107, 53, 0.85); color: #fff; }
.status-closed  { background: rgba(107, 114, 128, 0.75); color: #fff; }
.status-finished{ background: rgba(55, 65, 81, 0.75);   color: var(--el-border-color-light); }
.status-draft   { background: rgba(245, 158, 11, 0.8);  color: #fff; }

/* Body */
.card-body {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.event-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary, #1a1a2e);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.event-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary, var(--el-text-color-secondary));
}
.meta-icon {
  font-size: 13px;
  color: var(--accent, #FF6B35);
  flex-shrink: 0;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid var(--border, rgba(0,0,0,0.06));
}
.fee-tag {
  display: flex;
  align-items: baseline;
  gap: 5px;
}
.fee-label {
  font-size: 11px;
  color: var(--text-secondary, var(--el-text-color-secondary));
}
.fee-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--accent, #FF6B35);
}

.go-arrow {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 107, 53, 0.08);
  color: var(--accent, #FF6B35);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: background 0.2s, transform 0.2s;
}
.event-card:hover .go-arrow {
  background: var(--accent, #FF6B35);
  color: #fff;
  transform: translateX(3px);
}

/* ── Pagination ── */
.pagination-container {
  margin-top: 28px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-pagination) {
  --el-pagination-button-color: var(--text-secondary, var(--el-text-color-secondary));
  --el-pagination-hover-color: var(--accent, #FF6B35);
}
</style>