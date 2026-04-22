<template>
  <div class="public-scores page-shell">
    <header class="ps-hero">
      <div class="hero-main">
        <p class="ps-eyebrow">PUBLIC RESULTS</p>
        <h1 class="ps-title">公开成绩榜</h1>
        <p class="ps-sub">展示各赛事已正式发布成绩，按项目分组；未发布成绩不会出现在此页面。</p>
        <div class="hero-tags">
          <span class="hero-tag">公开可见</span>
          <span class="hero-tag">自动更新</span>
          <span class="hero-tag">按项目分组</span>
        </div>
      </div>
      <div class="hero-metrics">
        <div class="metric-card">
          <p class="metric-label">项目分组</p>
          <p class="metric-value">{{ rankingBlocks.length }}</p>
        </div>
        <div class="metric-card">
          <p class="metric-label">已发布成绩</p>
          <p class="metric-value">{{ totalPublishedCount }}</p>
        </div>
      </div>
    </header>

    <section class="ps-toolbar lz-surface">
      <div class="toolbar-row">
        <div class="toolbar-controls">
          <SmartSelect
            v-model="eventId"
            :options="eventOptions"
            placeholder="选择赛事"
            class="event-select"
            :loading="eventsLoading"
            @change="onEventChange"
          />
          <el-button type="primary" :disabled="!eventId" :loading="rankingLoading" @click="loadRanking">
            刷新
          </el-button>
        </div>
        <div class="toolbar-status" :class="statusTone">
          <span class="status-dot" />
          {{ statusText }}
        </div>
      </div>
      <p v-if="eventSummary" class="ps-hint">{{ eventSummary }}</p>
    </section>

    <section class="ps-overview">
      <article class="overview-card">
        <p class="overview-label">赛事池</p>
        <p class="overview-value">{{ eventOptionCount }}</p>
        <p class="overview-sub">可检索公开赛事总数</p>
      </article>
      <article class="overview-card">
        <p class="overview-label">当前项目数</p>
        <p class="overview-value">{{ activeItemCount }}</p>
        <p class="overview-sub">所选赛事已发布项目</p>
      </article>
      <article class="overview-card wide">
        <p class="overview-label">当前上下文</p>
        <p class="overview-value text">{{ selectedEventName }}</p>
        <p class="overview-sub">{{ statusText }}</p>
      </article>
    </section>

    <section class="ps-body lz-surface" v-loading="rankingLoading">
      <div v-if="!eventId" class="empty-state">
        <div class="empty-icon">◇</div>
        <h3 class="empty-title">请选择赛事</h3>
        <p class="empty-sub">从上方下拉框选择一场赛事即可查看已发布的公开成绩。</p>
      </div>
      <div v-else-if="!rankingLoading && isRankingEmpty" class="empty-state">
        <div class="empty-icon">⌁</div>
        <h3 class="empty-title">暂无公开成绩</h3>
        <p class="empty-sub">该赛事可能尚未发布成绩，或管理员仍在录入中。</p>
      </div>
      <div v-else class="ranking-stack">
        <article v-for="block in rankingBlocks" :key="block.itemName" class="item-block">
          <header class="item-head">
            <div>
              <span class="item-name">{{ block.itemName }}</span>
              <p class="item-sub">按名次升序展示已发布成绩</p>
            </div>
            <span class="item-count">{{ block.rows.length }} 条</span>
          </header>
          <el-table :data="block.rows" class="ps-table" stripe>
            <el-table-column label="名次" width="88" align="center">
              <template #default="{ row }">
                <span class="rank-pill" :class="rankClass(row.scoreRank)">{{ formatRank(row.scoreRank) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="athleteName" label="运动员" min-width="120" />
            <el-table-column prop="deptName" label="院系/班级" min-width="140" show-overflow-tooltip />
            <el-table-column prop="scoreValue" label="成绩" min-width="120" />
          </el-table>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPublicEventList } from '@/api/public'
import { getPublicScores } from '@/api/score'
import SmartSelect from '@/components/SmartSelect.vue'

const route = useRoute()
const router = useRouter()

const eventsLoading = ref(false)
const rankingLoading = ref(false)
const eventOptions = ref([])
const eventId = ref(null)
const rankingMap = ref({})

const rankingBlocks = computed(() => {
  const m = rankingMap.value || {}
  return Object.keys(m).map((itemName) => ({
    itemName,
    rows: m[itemName] || []
  }))
})

const isRankingEmpty = computed(() => rankingBlocks.value.length === 0)
const totalPublishedCount = computed(() => rankingBlocks.value.reduce((sum, block) => sum + block.rows.length, 0))
const eventOptionCount = computed(() => eventOptions.value.length)
const activeItemCount = computed(() => rankingBlocks.value.length)

const eventSummary = computed(() => {
  if (!eventId.value) return ''
  const ev = eventOptions.value.find((e) => e.id === eventId.value)
  if (!ev) return ''
  const name = ev.name || ev.eventName
  return name ? `当前赛事：${name}` : ''
})

const selectedEventName = computed(() => {
  if (!eventId.value) return '未选择赛事'
  const ev = eventOptions.value.find((e) => e.id === eventId.value)
  return ev?.name || ev?.eventName || `赛事 #${eventId.value}`
})

const statusText = computed(() => {
  if (!eventId.value) return '待选择赛事'
  if (rankingLoading.value) return '加载中'
  if (isRankingEmpty.value) return '暂无公开成绩'
  return '已完成加载'
})

const statusTone = computed(() => {
  if (!eventId.value) return 'idle'
  if (rankingLoading.value) return 'loading'
  if (isRankingEmpty.value) return 'warning'
  return 'success'
})

const loadEvents = async () => {
  eventsLoading.value = true
  try {
    const res = await getPublicEventList({ currentPage: 1, pageSize: 200 })
    if (res.code === 200 && res.data) {
      eventOptions.value = res.data.records || res.data.rows || []
    }
  } finally {
    eventsLoading.value = false
  }
}

const loadRanking = async () => {
  if (!eventId.value) {
    rankingMap.value = {}
    return
  }
  rankingLoading.value = true
  try {
    const res = await getPublicScores(eventId.value)
    if (res.code === 200) {
      rankingMap.value = res.data || {}
    } else {
      rankingMap.value = {}
    }
  } catch {
    rankingMap.value = {}
  } finally {
    rankingLoading.value = false
  }
}

const syncQuery = () => {
  const q = { ...route.query }
  if (eventId.value) {
    q.eventId = String(eventId.value)
  } else {
    delete q.eventId
  }
  router.replace({ query: q })
}

const onEventChange = () => {
  syncQuery()
  loadRanking()
}

const applyRouteEventId = () => {
  const raw = route.query.eventId
  if (raw == null || raw === '') return
  const n = Number(raw)
  if (!Number.isFinite(n)) return
  eventId.value = n
}

const formatRank = (r) => {
  if (r == null || r === '') return '—'
  return `第 ${r} 名`
}

const rankClass = (rank) => {
  const r = Number(rank)
  if (r === 1) return 'top1'
  if (r === 2) return 'top2'
  if (r === 3) return 'top3'
  return ''
}

watch(
  () => route.query.eventId,
  () => {
    applyRouteEventId()
    if (eventId.value) {
      loadRanking()
    }
  }
)

onMounted(async () => {
  await loadEvents()
  applyRouteEventId()
  if (eventId.value) {
    await loadRanking()
  }
})
</script>

<style scoped>
.page-shell {
  --ps-bg: var(--bg-page);
  --ps-surface: var(--bg-card);
  --ps-surface-2: color-mix(in srgb, var(--bg-card) 94%, #8b97aa 6%);
  --ps-border: color-mix(in srgb, var(--border) 68%, transparent);
  --ps-border-strong: rgba(255, 107, 53, 0.28);
  --ps-text-primary: var(--text-primary);
  --ps-text-body: var(--text-primary);
  --ps-text-secondary: var(--text-secondary);
  --ps-text-placeholder: color-mix(in srgb, var(--text-secondary) 78%, #94a3b8 22%);
  --ps-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
  --el-bg-color: var(--ps-bg);
  --el-fill-color-blank: var(--ps-surface);
  --el-border-color: var(--ps-border);
  --el-border-color-hover: var(--ps-border-strong);
  --el-text-color-regular: var(--ps-text-body);
  --el-text-color-placeholder: var(--ps-text-placeholder);
  --el-color-primary: #ff6b35;
  padding: 24px;
  min-height: calc(100vh - 80px);
  color: var(--ps-text-body);
  background: var(--ps-bg);
}

.public-scores {
  max-width: 1100px;
  margin: 0 auto;
}

.ps-hero {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
  align-items: stretch;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 24px;
  background:
    radial-gradient(circle at 86% 20%, rgba(255, 107, 53, 0.16), transparent 48%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.44), rgba(255, 255, 255, 0)),
    var(--ps-surface-2);
  border-color: color-mix(in srgb, var(--ps-border) 72%, transparent);
  box-shadow: var(--ps-shadow);
}

.ps-hero::after {
  content: '';
  position: absolute;
  inset: auto -70px -70px auto;
  width: 210px;
  height: 210px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 107, 53, 0.22), rgba(255, 107, 53, 0));
  pointer-events: none;
}

.hero-main {
  position: relative;
  z-index: 1;
}

.hero-metrics {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 10px;
}

.metric-card {
  border-radius: 12px;
  border: 1px solid color-mix(in srgb, var(--ps-border) 65%, transparent);
  background: color-mix(in srgb, var(--ps-surface) 84%, transparent);
  backdrop-filter: blur(2px);
  padding: 12px 14px;
}

.metric-label {
  margin: 0 0 6px;
  font-size: 12px;
  color: var(--ps-text-secondary);
}

.metric-value {
  margin: 0;
  font-size: 26px;
  line-height: 1;
  color: var(--ps-text-primary);
  font-weight: 700;
}

.ps-eyebrow {
  margin: 0 0 10px;
  color: var(--el-color-primary);
  letter-spacing: 0.18em;
  font-size: 12px;
}

.ps-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  color: var(--ps-text-primary);
}

.ps-sub {
  margin: 0;
  color: var(--ps-text-secondary);
  font-size: 14px;
  line-height: 1.6;
  max-width: 640px;
}

.hero-tags {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-tag {
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: var(--ps-text-secondary);
  border: 1px solid color-mix(in srgb, var(--ps-border) 62%, transparent);
  background: color-mix(in srgb, var(--ps-surface) 88%, transparent);
}

.ps-toolbar {
  padding: 16px 18px 14px;
  border-radius: 14px;
  border: 1px solid color-mix(in srgb, var(--ps-border) 68%, transparent);
  margin-bottom: 16px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.016), rgba(255, 255, 255, 0));
  box-shadow: var(--ps-shadow);
}

.toolbar-row {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.toolbar-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

:deep(.event-select) {
  min-width: 280px;
  max-width: 100%;
}

.toolbar-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--ps-border) 64%, transparent);
  font-size: 12px;
  color: var(--ps-text-secondary);
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #94a3b8;
}

.toolbar-status.success {
  color: #10b981;
  border-color: color-mix(in srgb, #10b981 30%, var(--ps-border));
}

.toolbar-status.success .status-dot {
  background: #10b981;
}

.toolbar-status.warning {
  color: #f59e0b;
  border-color: color-mix(in srgb, #f59e0b 28%, var(--ps-border));
}

.toolbar-status.warning .status-dot {
  background: #f59e0b;
}

.toolbar-status.loading .status-dot {
  background: var(--el-color-primary);
  box-shadow: 0 0 0 0 rgba(255, 107, 53, 0.45);
  animation: pulse 1.4s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 107, 53, 0.4);
  }
  70% {
    box-shadow: 0 0 0 8px rgba(255, 107, 53, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 107, 53, 0);
  }
}

.ps-hint {
  margin: 12px 0 0;
  font-size: 13px;
  color: var(--ps-text-secondary);
}

.ps-overview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.overview-card {
  border-radius: 12px;
  border: 1px solid color-mix(in srgb, var(--ps-border) 62%, transparent);
  background: color-mix(in srgb, var(--ps-surface) 92%, #dbe3f1 8%);
  padding: 12px 14px;
  box-shadow: var(--ps-shadow);
}

.overview-card.wide {
  background: linear-gradient(
      90deg,
      color-mix(in srgb, var(--ps-surface) 96%, transparent),
      color-mix(in srgb, var(--ps-surface) 92%, rgba(255, 107, 53, 0.14) 8%)
    );
}

.overview-label {
  margin: 0;
  font-size: 12px;
  color: var(--ps-text-secondary);
}

.overview-value {
  margin: 7px 0 2px;
  font-size: 24px;
  font-weight: 700;
  color: var(--ps-text-primary);
}

.overview-value.text {
  font-size: 16px;
  line-height: 1.35;
}

.overview-sub {
  margin: 0;
  font-size: 12px;
  color: var(--ps-text-secondary);
}

.ps-body {
  padding: 14px;
  border-radius: 14px;
  border: 1px solid color-mix(in srgb, var(--ps-border) 70%, transparent);
  min-height: 200px;
  box-shadow: var(--ps-shadow);
}

.lz-surface {
  background: var(--ps-surface);
}

.ranking-stack {
  display: grid;
  gap: 20px;
}

.item-block {
  padding: 14px;
  border-radius: 12px;
  border: 1px solid color-mix(in srgb, var(--ps-border) 60%, transparent);
  background: color-mix(in srgb, var(--ps-surface) 90%, var(--el-color-primary) 2%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid color-mix(in srgb, var(--ps-border) 55%, transparent);
}

.item-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--ps-text-primary);
}

.item-sub {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--ps-text-secondary);
}

.item-count {
  font-size: 12px;
  color: #ffc4ac;
  border: 1px solid rgba(255, 107, 53, 0.22);
  border-radius: 999px;
  padding: 4px 10px;
  background: rgba(255, 107, 53, 0.1);
}

.empty-state {
  text-align: center;
  padding: 48px 16px;
}

.empty-icon {
  font-size: 32px;
  color: var(--ps-text-placeholder);
  margin-bottom: 12px;
}

.empty-title {
  margin: 0 0 8px;
  font-size: 18px;
  color: var(--ps-text-primary);
}

.empty-sub {
  margin: 0;
  font-size: 14px;
  color: var(--ps-text-secondary);
}

.rank-pill {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  color: var(--ps-text-body);
  background: color-mix(in srgb, var(--ps-surface) 80%, #94a3b8 20%);
}

.rank-pill.top1 {
  color: #fde68a;
  background: rgba(250, 204, 21, 0.12);
}

.rank-pill.top2 {
  color: #e5e7eb;
  background: rgba(156, 163, 175, 0.15);
}

.rank-pill.top3 {
  color: #fdba74;
  background: rgba(234, 88, 12, 0.12);
}

::deep(.ps-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: color-mix(in srgb, var(--ps-surface) 80%, #dbe3f1 20%);
  --el-table-border-color: color-mix(in srgb, var(--ps-border) 40%, transparent);
  --el-table-text-color: var(--ps-text-body);
  --el-table-row-hover-bg-color: color-mix(in srgb, var(--ps-surface) 78%, #eef3fb 22%);
}

::deep(.ps-table th.el-table__cell) {
  color: var(--ps-text-secondary);
  font-weight: 600;
  border-bottom-color: color-mix(in srgb, var(--ps-border) 45%, transparent) !important;
}

::deep(.ps-table td.el-table__cell) {
  border-bottom-color: color-mix(in srgb, var(--ps-border) 30%, transparent) !important;
}

:global(html.dark) .item-block {
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.02);
}

:global(html.dark) .ps-toolbar,
:global(html.dark) .ps-body,
:global([data-theme="dark"]) .ps-toolbar,
:global([data-theme="dark"]) .ps-body {
  box-shadow: none;
}

:global(html.dark) .overview-card,
:global([data-theme="dark"]) .overview-card {
  box-shadow: none;
  background: color-mix(in srgb, var(--ps-surface) 88%, #0b1018 12%);
}

:global(html.dark) .ps-table,
:global([data-theme="dark"]) .ps-table {
  --el-table-header-bg-color: rgba(255, 255, 255, 0.04);
  --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.03);
  --el-table-border-color: rgba(255, 255, 255, 0.04);
}

:global(html.dark) .page-shell,
:global([data-theme="dark"]) .page-shell {
  --ps-shadow: none;
  --ps-surface-2: color-mix(in srgb, var(--bg-card) 90%, #0b1018 10%);
}

:global(html.dark) .ps-hero,
:global([data-theme="dark"]) .ps-hero {
  background:
    radial-gradient(circle at 86% 20%, rgba(255, 107, 53, 0.16), transparent 48%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.02), rgba(255, 255, 255, 0)),
    var(--ps-surface-2);
}

::deep(.ps-table .el-table__inner-wrapper::before) {
  display: none;
}

@media (max-width: 900px) {
  .ps-hero {
    grid-template-columns: 1fr;
    padding: 18px;
  }

  .hero-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .ps-overview {
    grid-template-columns: 1fr 1fr;
  }

  .overview-card.wide {
    grid-column: 1 / -1;
  }
}

@media (max-width: 640px) {
  .page-shell {
    padding: 14px;
  }

  .ps-title {
    font-size: 24px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .ps-overview {
    grid-template-columns: 1fr;
  }

  .event-select {
    width: 100%;
    min-width: 0;
  }

  .toolbar-row {
    align-items: stretch;
  }

  .toolbar-controls {
    width: 100%;
  }

  .toolbar-status {
    align-self: flex-start;
  }
}
</style>
