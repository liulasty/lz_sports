<template>
  <div class="dash-page">
    <div class="dash-hero">
      <div>
        <p class="dash-eyebrow">DASHBOARD</p>
        <h1 class="dash-title">工作台</h1>
        <p class="dash-sub">
          <template v-if="isAdmin">概览平台数据与赛事进度，快速定位异常与待处理事项。</template>
          <template v-else>欢迎使用体育赛事管理系统！你可以在侧边栏进入赛事大厅查看与报名。</template>
        </p>
      </div>
      <div class="dash-hero-actions lz-actions lz-ep-dark">
        <el-button v-if="isAdmin" type="primary" @click="loadData">刷新数据</el-button>
      </div>
    </div>

    <!-- 非管理员：给一个更友好的引导卡 -->
    <section v-if="!isAdmin" class="dash-shell lz-surface">
      <div class="welcome-card">
        <div class="welcome-icon">⌁</div>
        <h3 class="welcome-title">从赛事大厅开始</h3>
        <p class="welcome-sub">浏览可报名赛事，提交运动员申请后即可参赛。</p>
      </div>
    </section>

    <template v-else>
      <!-- KPI -->
      <section class="kpi-grid">
        <article class="kpi-card">
          <div>
            <p class="kpi-label">总用户数</p>
            <p class="kpi-value">{{ overviewStats.totalUsers || 0 }}</p>
          </div>
          <div class="kpi-icon kpi-blue">U</div>
        </article>
        <article class="kpi-card">
          <div>
            <p class="kpi-label">赛事总数</p>
            <p class="kpi-value">{{ overviewStats.totalEvents || 0 }}</p>
          </div>
          <div class="kpi-icon kpi-green">E</div>
        </article>
        <article class="kpi-card">
          <div>
            <p class="kpi-label">总报名数</p>
            <p class="kpi-value">{{ overviewStats.totalRegistrations || 0 }}</p>
          </div>
          <div class="kpi-icon kpi-amber">R</div>
        </article>
        <article class="kpi-card">
          <div>
            <p class="kpi-label">本月新增用户</p>
            <p class="kpi-value">{{ overviewStats.newUsersThisMonth || 0 }}</p>
          </div>
          <div class="kpi-icon kpi-red">M</div>
        </article>
      </section>

      <section class="dash-grid">
        <!-- 状态分布 -->
        <article class="panel lz-surface lz-ep-dark" v-loading="loading">
          <div class="panel-head">
            <div>
              <div class="panel-title">赛事状态分布</div>
              <div class="panel-sub">按状态统计赛事数量</div>
            </div>
            <div class="panel-meta">总计 {{ totalEventsByStatus }}</div>
          </div>

          <div v-if="statusItems.length" class="status-list">
            <div v-for="s in statusItems" :key="s.key" class="status-item">
              <div class="status-left">
                <span class="status-dot" :class="s.dotClass"></span>
                <span class="status-label">{{ s.label }}</span>
              </div>
              <div class="status-right">
                <span class="status-count">{{ s.count }}</span>
              </div>
              <div class="status-bar">
                <div class="status-bar-fill" :style="{ width: s.pct + '%' }"></div>
              </div>
            </div>
          </div>
          <div v-else class="empty-mini">
            <div class="empty-icon">⌁</div>
            <p>暂无数据</p>
          </div>
        </article>

        <!-- 各赛事统计表 -->
        <article class="panel lz-surface lz-ep-dark" v-loading="loading">
          <div class="panel-head">
            <div>
              <div class="panel-title">各赛事统计</div>
              <div class="panel-sub">报名、审核与成绩发布概览</div>
            </div>
            <div class="panel-meta">共 {{ eventStats.length }} 场</div>
          </div>

          <el-table :data="eventStats" class="event-table" height="320">
            <el-table-column prop="eventName" label="赛事名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="totalRegistrations" label="报名总数" align="center" min-width="110" />
            <el-table-column prop="approvedRegistrations" label="审核通过" align="center" min-width="110" />
            <el-table-column prop="publishedProjects" label="发布成绩项目数" align="center" min-width="140" />
          </el-table>
        </article>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getOverviewStats, getEventStats } from '@/api/stats'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => {
  const role = userStore.userInfo?.role
  return role === 'SUPER_ADMIN' || role === 'SCHOOL_ADMIN' || role === 'EVENT_ADMIN'
})

const loading = ref(false)
const overviewStats = ref({})
const eventStats = ref([])

const formatStatus = (status) => {
  const map = {
    DRAFT: '草稿',
    OPEN: '报名中',
    ONGOING: '进行中',
    FINISHED: '已结束'
  }
  return map[status] || status
}

const statusItems = computed(() => {
  const obj = overviewStats.value?.eventsByStatus || {}
  const entries = Object.entries(obj).map(([key, val]) => ({ key, count: Number(val) || 0 }))
  const total = entries.reduce((s, e) => s + e.count, 0) || 0
  const dotMap = { DRAFT: 'dot-gray', OPEN: 'dot-amber', ONGOING: 'dot-blue', FINISHED: 'dot-green' }
  return entries
    .sort((a, b) => b.count - a.count)
    .map((e) => ({
      ...e,
      label: formatStatus(e.key),
      pct: total ? Math.round((e.count / total) * 100) : 0,
      dotClass: dotMap[e.key] || 'dot-gray'
    }))
})

const totalEventsByStatus = computed(() => statusItems.value.reduce((s, e) => s + e.count, 0))

const loadData = async () => {
  if (!isAdmin.value) return
  loading.value = true
  try {
    const res1 = await getOverviewStats()
    if (res1.code === 200) overviewStats.value = res1.data
    const res2 = await getEventStats()
    if (res2.code === 200) eventStats.value = res2.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dash-page {
  padding: 24px;
}

.dash-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.dash-eyebrow {
  margin: 0 0 10px;
  color: var(--accent);
  letter-spacing: 0.18em;
  font-size: 11px;
  font-weight: 700;
}

.dash-title {
  margin: 0 0 8px;
  font-size: 32px;
  color: var(--text-primary);
}

.dash-sub {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.dash-shell {
  padding: 18px;
}

.welcome-card {
  min-height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.welcome-icon {
  font-size: 44px;
  color: var(--accent);
  margin-bottom: 10px;
}

.welcome-title {
  margin: 0 0 8px;
  color: var(--text-primary);
  font-size: 18px;
}

.welcome-sub {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.7;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.kpi-card {
  background: color-mix(in srgb, var(--bg-card) 92%, var(--bg-soft));
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.12);
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
}

.kpi-label {
  margin: 0 0 8px;
  color: color-mix(in srgb, var(--text-secondary) 92%, transparent);
  font-size: 12px;
}

.kpi-value {
  margin: 0;
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 800;
}

.kpi-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  color: #fff;
  opacity: 0.95;
}

.kpi-blue { background: color-mix(in srgb, #409EFF 75%, #111); }
.kpi-green { background: color-mix(in srgb, #4fb77a 75%, #111); }
.kpi-amber { background: color-mix(in srgb, #f59e0b 78%, #111); }
.kpi-red { background: color-mix(in srgb, #f43f5e 75%, #111); }

.dash-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 12px;
}

.panel {
  padding: 14px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-title {
  font-size: 15px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
}

.panel-sub {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 6px;
}

.panel-meta {
  font-size: 12px;
  color: color-mix(in srgb, var(--text-secondary) 90%, transparent);
}

.status-list {
  display: grid;
  gap: 10px;
}

.status-item {
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 12px 12px 10px;
  background: color-mix(in srgb, var(--bg-card) 88%, transparent);
}

.status-left {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.status-right {
  float: right;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  display: inline-block;
}

.dot-gray { background: color-mix(in srgb, var(--text-secondary) 55%, transparent); }
.dot-amber { background: #f59e0b; }
.dot-blue { background: #409EFF; }
.dot-green { background: #4fb77a; }

.status-label {
  color: var(--text-primary);
  font-weight: 700;
  font-size: 13px;
}

.status-count {
  color: var(--accent);
  font-weight: 900;
  font-variant-numeric: tabular-nums;
}

.status-bar {
  margin-top: 10px;
  height: 5px;
  background: color-mix(in srgb, var(--bg-soft) 70%, transparent);
  border-radius: 999px;
  overflow: hidden;
}

.status-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--accent), color-mix(in srgb, var(--accent) 35%, #ffffff));
}

.empty-mini {
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.empty-mini .empty-icon {
  font-size: 36px;
  color: var(--accent);
  margin-bottom: 10px;
}

@media (max-width: 1024px) {
  .dash-grid { grid-template-columns: 1fr; }
}

@media (max-width: 900px) {
  .dash-hero { flex-direction: column; align-items: flex-start; }
  .kpi-grid { grid-template-columns: 1fr 1fr; }
}

@media (max-width: 520px) {
  .kpi-grid { grid-template-columns: 1fr; }
}
</style>

