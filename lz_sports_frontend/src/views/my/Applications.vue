<template>
  <div class="apps-page">
    <div class="apps-hero">
      <div>
        <p class="apps-eyebrow">MY APPLICATIONS</p>
        <h1 class="apps-title">我的运动员申请</h1>
        <p class="apps-sub">查看申请进度与审核状态；如需参赛，请先完成运动员申请。</p>
      </div>
      <div class="apps-hero-actions">
        <button class="btn-solid" @click="$router.push('/profile')">前往个人中心申请</button>
      </div>
    </div>

    <section class="apps-stats">
      <article class="stat-card">
        <p class="stat-label">申请记录</p>
        <p class="stat-value">{{ applicationList.length }}</p>
      </article>
      <article class="stat-card pending">
        <p class="stat-label">处理中</p>
        <p class="stat-value">{{ statusCount.pending }}</p>
      </article>
      <article class="stat-card success">
        <p class="stat-label">已通过</p>
        <p class="stat-value">{{ statusCount.approved }}</p>
      </article>
      <article class="stat-card danger">
        <p class="stat-label">未通过</p>
        <p class="stat-value">{{ statusCount.rejected }}</p>
      </article>
    </section>

    <section class="apps-shell lz-surface lz-ep-dark" v-loading="loading">
      <div v-if="!loading && applicationList.length === 0" class="empty-state">
        <div class="empty-icon">⌁</div>
        <h3 class="empty-title">还没有申请记录</h3>
        <p class="empty-sub">提交运动员申请后，审核进度会在这里实时更新。</p>
        <div class="lz-actions">
          <el-button type="primary" @click="$router.push('/profile')">立即去申请</el-button>
        </div>
      </div>

      <div v-else class="apps-list">
        <article v-for="item in sortedList" :key="item.id || item.applyTime" class="app-card" :class="{ unread: isPending(item) }">
          <div class="app-dot"></div>
          <div class="app-main">
            <div class="app-head">
              <div class="app-left">
                <h3 class="app-event">{{ item.eventName || '未命名赛事' }}</h3>
                <p class="app-time">申请时间：{{ formatTime(item.applyTime) }}</p>
              </div>
              <div class="app-right">
                <el-tag
                  :type="getAthleteStatusType(normalizeAthleteStatus(item.athleteState || item.status))"
                  effect="dark"
                >
                  {{ getAthleteStatusText(normalizeAthleteStatus(item.athleteState || item.status)) }}
                </el-tag>
              </div>
            </div>

            <div class="app-meta">
              <span class="meta-chip">状态码：{{ normalizeAthleteStatus(item.athleteState || item.status) }}</span>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getMyApplications } from '@/api/athlete'
import { getAthleteStatusText, getAthleteStatusType, normalizeAthleteStatus } from '@/utils/athleteStatus'

const loading = ref(true)
const applicationList = ref([])

const checkApplication = async () => {
  try {
    const res = await getMyApplications()
    if (res.code === 200) {
      applicationList.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  checkApplication()
})

const normalize = (item) => normalizeAthleteStatus(item?.athleteState || item?.status)

const isPending = (item) => {
  const s = normalize(item)
  return s === 'PENDING' || s === 'PROCESSING' || s === 'AUDITING'
}

const statusCount = computed(() => {
  const list = applicationList.value || []
  let pending = 0
  let approved = 0
  let rejected = 0
  for (const it of list) {
    const s = normalize(it)
    if (s === 'APPROVED' || s === 'PASS' || s === 'SUCCESS') approved++
    else if (s === 'REJECTED' || s === 'FAIL' || s === 'DENY') rejected++
    else pending++
  }
  return { pending, approved, rejected }
})

const sortedList = computed(() => {
  const list = [...(applicationList.value || [])]
  return list.sort((a, b) => {
    const ta = new Date(a?.applyTime || 0).getTime()
    const tb = new Date(b?.applyTime || 0).getTime()
    return tb - ta
  })
})

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}
</script>

<style scoped>
.apps-page {
  padding: 24px;
}

.apps-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.apps-eyebrow {
  margin: 0 0 10px;
  color: var(--accent);
  letter-spacing: 0.18em;
  font-size: 11px;
  font-weight: 700;
}

.apps-title {
  margin: 0 0 8px;
  font-size: 32px;
  color: var(--text-primary);
}

.apps-sub {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.btn-solid {
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--accent), #e0541e);
  color: #fff;
  padding: 10px 16px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 10px 26px rgba(255, 107, 53, 0.22);
}

.apps-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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

.stat-card.pending { border-color: color-mix(in srgb, var(--accent) 40%, var(--border)); }
.stat-card.success { border-color: color-mix(in srgb, #4fb77a 40%, var(--border)); }
.stat-card.danger { border-color: color-mix(in srgb, #f43f5e 40%, var(--border)); }

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

.apps-shell {
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
  margin: 0 0 18px;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.7;
}

.apps-list {
  display: grid;
  gap: 10px;
}

.app-card {
  display: grid;
  grid-template-columns: 14px 1fr;
  gap: 10px;
  border: 1px solid var(--border);
  background: color-mix(in srgb, var(--bg-card) 88%, transparent);
  border-radius: 12px;
  padding: 12px 14px;
  transition: transform 0.16s ease, border-color 0.16s ease, background 0.16s ease;
}

.app-card:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--text-secondary) 18%, var(--border));
  background: color-mix(in srgb, var(--bg-card) 94%, var(--bg-soft));
}

.app-dot {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  margin-top: 6px;
  background: color-mix(in srgb, var(--text-secondary) 55%, transparent);
}

.app-card.unread .app-dot {
  background: var(--accent);
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--accent) 18%, transparent);
}

.app-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
}

.app-event {
  margin: 0 0 6px;
  font-size: 15px;
  color: color-mix(in srgb, var(--text-primary) 92%, transparent);
}

.app-time {
  margin: 0;
  font-size: 12px;
  color: color-mix(in srgb, var(--text-secondary) 88%, transparent);
}

.app-meta {
  margin-top: 10px;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  border-radius: 999px;
  padding: 3px 10px;
  color: color-mix(in srgb, var(--text-secondary) 90%, transparent);
  border: 1px solid var(--border);
  background: color-mix(in srgb, var(--bg-card) 88%, transparent);
}

@media (max-width: 900px) {
  .apps-hero { flex-direction: column; align-items: flex-start; }
  .apps-stats { grid-template-columns: 1fr 1fr; }
  .app-head { flex-direction: column; gap: 8px; }
}
</style>
