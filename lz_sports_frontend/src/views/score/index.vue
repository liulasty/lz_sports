<template>
  <div class="score-page">
    <div class="score-hero">
      <div>
        <p class="score-eyebrow">MY SCORE CENTER</p>
        <h1 class="score-title">我的成绩</h1>
        <p class="score-sub">按赛事查看个人成绩、名次与发布时间。</p>
      </div>

      <div class="score-filter lz-ep-dark">
        <div class="filter-row lz-form">
          <el-select v-model="eventId" clearable placeholder="选择赛事" class="event-select">
            <el-option v-for="item in eventOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
          <div class="lz-actions">
            <el-button type="primary" @click="getList">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </div>
        </div>
      </div>
    </div>

    <section class="score-stats">
      <article class="stat-card">
        <p class="stat-label">成绩条数</p>
        <p class="stat-value">{{ scoreList.length }}</p>
      </article>
      <article class="stat-card hot">
        <p class="stat-label">前三名次数</p>
        <p class="stat-value">{{ top3Count }}</p>
      </article>
      <article class="stat-card">
        <p class="stat-label">最近发布</p>
        <p class="stat-value small">{{ latestPublishText }}</p>
      </article>
    </section>

    <section class="score-shell lz-surface lz-ep-dark" v-loading="loading">
      <div v-if="!loading && scoreList.length === 0" class="empty-state">
        <div class="empty-icon">⌁</div>
        <h3 class="empty-title">暂无成绩</h3>
        <p class="empty-sub">可先选择赛事查询；成绩发布后会出现在这里。</p>
      </div>

      <el-table v-else :data="scoreList" class="score-table">
        <el-table-column prop="eventName" label="赛事名称" min-width="180" />
        <el-table-column prop="itemName" label="项目名称" min-width="140" />
        <el-table-column prop="athleteName" label="运动员" min-width="110" />
        <el-table-column prop="scoreValue" label="成绩" min-width="110" />
        <el-table-column prop="scoreRank" label="名次" min-width="120">
          <template #default="scope">
            <span class="rank-pill" :class="rankClass(scope.row.scoreRank)">
              第 {{ scope.row.scoreRank }} 名
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间" min-width="170">
          <template #default="scope">
            {{ formatDate(scope.row.publishedAt) }}
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getMyScores } from '@/api/score'
import { getEventList } from '@/api/event'

const loading = ref(false)
const scoreList = ref([])
const eventOptions = ref([])
const eventId = ref(null)

const top3Count = computed(() => {
  return (scoreList.value || []).filter(s => Number(s?.scoreRank) > 0 && Number(s?.scoreRank) <= 3).length
})

const latestPublishText = computed(() => {
  const list = scoreList.value || []
  let max = 0
  for (const it of list) {
    const t = new Date(it?.publishedAt || 0).getTime()
    if (t > max) max = t
  }
  return max ? new Date(max).toLocaleDateString() : '—'
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getMyScores({ eventId: eventId.value })
    if (res.code === 200) {
      scoreList.value = res.data || []
    }
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  eventId.value = null
  getList()
}

const loadEvents = async () => {
  const res = await getEventList({ currentPage: 1, pageSize: 200 })
  if (res.code === 200) {
    eventOptions.value = res.data.records || []
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

const rankClass = (rank) => {
  const r = Number(rank)
  if (r === 1) return 'top1'
  if (r === 2) return 'top2'
  if (r === 3) return 'top3'
  return ''
}

onMounted(() => {
  loadEvents()
  getList()
})
</script>

<style scoped>
.score-page {
  padding: 24px;
}

.score-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.score-eyebrow {
  margin: 0 0 10px;
  color: var(--accent);
  letter-spacing: 0.18em;
  font-size: 11px;
  font-weight: 700;
}

.score-title {
  margin: 0 0 8px;
  font-size: 32px;
  color: var(--text-primary);
}

.score-sub {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.event-select {
  width: 260px;
}

.score-stats {
  display: grid;
  grid-template-columns: 1.1fr 1fr 1.2fr;
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  background: color-mix(in srgb, var(--bg-card) 92%, var(--bg-soft));
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.12);
}

.stat-card.hot {
  border-color: color-mix(in srgb, var(--accent) 40%, var(--border));
}

.stat-label {
  margin: 0 0 8px;
  color: color-mix(in srgb, var(--text-secondary) 90%, transparent);
  font-size: 12px;
}

.stat-value {
  margin: 0;
  color: color-mix(in srgb, var(--text-primary) 92%, transparent);
  font-size: 28px;
  font-weight: 800;
}

.stat-value.small {
  font-size: 16px;
  font-weight: 700;
  margin-top: 2px;
}

.score-shell {
  padding: 14px;
}

.empty-state {
  min-height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 24px 16px;
}

.empty-icon {
  font-size: 44px;
  color: var(--accent);
  margin-bottom: 10px;
}

.empty-title {
  margin: 0 0 8px;
  color: color-mix(in srgb, var(--text-primary) 92%, transparent);
  font-size: 18px;
}

.empty-sub {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.7;
}

.rank-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid var(--border);
  background: color-mix(in srgb, var(--bg-card) 88%, transparent);
  color: color-mix(in srgb, var(--text-primary) 88%, transparent);
  font-weight: 700;
  font-size: 12px;
}

.rank-pill.top1 {
  border-color: rgba(245, 158, 11, 0.6);
  background: rgba(245, 158, 11, 0.12);
  color: #f59e0b;
}

.rank-pill.top2 {
  border-color: rgba(148, 163, 184, 0.6);
  background: rgba(148, 163, 184, 0.12);
  color: #94a3b8;
}

.rank-pill.top3 {
  border-color: rgba(180, 83, 9, 0.55);
  background: rgba(180, 83, 9, 0.12);
  color: #b45309;
}

@media (max-width: 900px) {
  .score-hero { flex-direction: column; align-items: flex-start; }
  .filter-row { flex-direction: column; align-items: stretch; }
  .event-select { width: 100%; }
  .score-stats { grid-template-columns: 1fr; }
}
</style>

