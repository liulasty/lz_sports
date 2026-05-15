<template>
  <div class="event-detail-container" v-if="event">

    <!-- Hero Banner -->
    <div class="event-hero" :style="heroStyle">
      <div class="hero-overlay" />
      <div class="hero-content">
        <button class="back-btn" @click="$router.back()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          返回
        </button>
        <div class="hero-badge" :class="getStatusClass(event.eventStatus)">
          <span class="badge-dot" />
          {{ getStatusLabel(event.eventStatus) }}
        </div>
        <h1 class="hero-title">{{ event.eventName }}</h1>
        <div class="hero-meta">
          <span class="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            {{ formatDate(event.registrationStartTime) }} — {{ formatDate(event.registrationEndTime) }}
          </span>
        </div>
        <div class="hero-actions">
          <button
            type="button"
            class="hero-public-scores-btn"
            @click="$router.push({ path: '/public-scores', query: { eventId: String(event.id) } })"
          >
            公开成绩榜
          </button>
        </div>
      </div>
    </div>

    <div class="page-body">

      <!-- Athlete Status Bar -->
      <div class="status-bar" :class="getAthleteBarClass(athleteApplyStatus)">
        <div class="status-bar-left">
          <div class="status-icon-wrap">
            <svg v-if="athleteApplyStatus === 'APPROVED'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            <svg v-else-if="athleteApplyStatus === 'REJECTED'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            <svg v-else-if="athleteApplyStatus === 'PENDING'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          </div>
          <div>
            <div class="status-bar-title">运动员资格</div>
            <div class="status-bar-sub">{{ athleteApplyStatus ? athleteApplyStatusText : '您还未获取本赛事的运动员资格' }}</div>
          </div>
        </div>
        <button
            v-if="!athleteApplyStatus || athleteApplyStatus === 'REJECTED'"
            class="qualify-btn"
            @click="goToApply"
        >
          去认证
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
      </div>

      <!-- Registered Projects Notice -->
      <div v-if="registeredProjectNames.length > 0" class="registered-notice">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
        已报名：<strong>{{ registeredProjectNames.join('、') }}</strong>
      </div>

      <!-- Info Cards Row -->
      <div class="info-grid">
        <div class="info-card info-card--blue">
          <div class="info-card-icon">📋</div>
          <div class="info-card-label">参赛要求</div>
          <div class="info-card-value">{{ event.eventDescription }}</div>
        </div>
        <div class="info-card info-card--green">
          <div class="info-card-icon">💰</div>
          <div class="info-card-label">报名费用</div>
          <div class="info-card-value big">免费</div>
        </div>
        <div class="info-card info-card--orange">
          <div class="info-card-icon">🗓️</div>
          <div class="info-card-label">报名开始</div>
          <div class="info-card-value">{{ formatDate(event.registrationStartTime) }}</div>
        </div>
        <div class="info-card info-card--purple">
          <div class="info-card-icon">⏰</div>
          <div class="info-card-label">报名截止</div>
          <div class="info-card-value">{{ formatDate(event.registrationEndTime) }}</div>
        </div>
      </div>

      <!-- Projects Section -->
      <div class="projects-section">
        <div class="section-header">
          <h2 class="section-title">
            <span class="section-title-accent"></span>
            包含项目
          </h2>
          <span class="project-count">{{ projects.length }} 个项目</span>
        </div>

        <div class="project-cards">
          <div
              v-for="(project, index) in projects"
              :key="project.id"
              class="project-card"
              :class="`project-card--color${(index % 5) + 1}`"
          >
            <div class="project-card-top">
              <div class="project-index">{{ String(index + 1).padStart(2, '0') }}</div>
              <div class="project-info">
                <div class="project-name">{{ project.itemName }}</div>
                <div class="project-tags">
                  <span class="tag tag--gender">{{ project.limitation || '不限' }}</span>
                  <span class="tag tag--grade">{{ project.limitDeptIds && project.limitDeptIds.length > 0 ? '指定部门' : '不限部门' }}</span>
                </div>
              </div>
            </div>

            <div class="project-card-bottom">
              <div class="quota-bar-wrap">
                <div class="quota-labels">
                  <span>已报名</span>
                  <span class="quota-nums">{{ project.attendance }} / {{ project.maxAttendance }}</span>
                </div>
                <div class="quota-bar">
                  <div
                      class="quota-fill"
                      :style="{ width: Math.min(100, (project.attendance / project.maxAttendance) * 100) + '%' }"
                      :class="{ 'quota-fill--full': project.attendance >= project.maxAttendance }"
                  />
                </div>
              </div>

              <div class="project-actions">
                <button
                    class="action-btn action-btn--primary"
                    :disabled="project.registerDisabled"
                    @click="handleRegister(project)"
                >
                  {{ project.registerText }}
                </button>
                <button
                    class="action-btn action-btn--danger"
                    :disabled="project.cancelDisabled"
                    @click="handleCancel(project)"
                >
                  {{ project.cancelText }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEventById } from '@/api/event'
import { getProjectsByEventId } from '@/api/project'
import { applyProject, getRegistrationList, cancelRegistration } from '@/api/registration'
import { getAthleteApply } from '@/api/athlete'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAthleteStatusText, getAthleteStatusType, normalizeAthleteStatus } from '@/utils/athleteStatus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const event = ref(null)
const projects = ref([])
const athleteApplyStatus = ref('')
const athleteApplyStatusText = computed(() => getAthleteStatusText(athleteApplyStatus.value))
const myRegistrations = ref([])
const registeredProjectNames = ref([])

const heroStyle = computed(() => {
  if (event.value?.imageUrls) {
    const url = Array.isArray(event.value.imageUrls) ? event.value.imageUrls[0] : event.value.imageUrls
    return { backgroundImage: `url(${url})` }
  }
  return {}
})

const getStatusClass = (status) => {
  if (status === 'OPEN') return 'badge--green'
  if (status === 'ONGOING') return 'badge--blue'
  if (status === 'FINISHED') return 'badge--gray'
  return 'badge--orange'
}

const getStatusLabel = (status) => {
  if (status === 'OPEN') return '报名中'
  if (status === 'CLOSED') return '报名结束'
  if (status === 'ONGOING') return '进行中'
  if (status === 'FINISHED') return '已结束'
  return '草稿'
}

const getAthleteBarClass = (status) => {
  if (status === 'APPROVED') return 'status-bar--approved'
  if (status === 'REJECTED') return 'status-bar--rejected'
  if (status === 'PENDING') return 'status-bar--pending'
  return 'status-bar--none'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const loadEvent = async () => {
  const id = route.params.id
  try {
    const res = await getEventById(id)
    if (res.code === 200) event.value = res.data
  } catch (error) { console.error(error) }
}

const goToApply = () => {
  router.push({ path: '/profile', query: { tab: 'apply', eventId: event.value.id } })
}

const loadProjects = async () => {
  if (!event.value) return
  try {
    const res = await getProjectsByEventId(event.value.id)
    if (res.code === 200) {
      projects.value = (res.data || []).map(item => ({
        ...item,
        registerDisabled: true,
        registerText: '不可报名',
        cancelDisabled: true,
        cancelText: '不可取消'
      }))
      refreshButtonState()
    }
  } catch (error) { console.error(error) }
}

const loadAthleteStatus = async () => {
  try {
    const userId = userStore.userInfo.id || userStore.userInfo.userId
    if (!userId || !event.value) return
    const res = await getAthleteApply(userId, event.value.id)
    if (res.code === 200 && res.data) {
      athleteApplyStatus.value = normalizeAthleteStatus(res.data.athleteState || res.data.status)
    } else {
      athleteApplyStatus.value = ''
    }
  } catch (error) { athleteApplyStatus.value = '' }
}

const loadMyRegistrations = async () => {
  try {
    const res = await getRegistrationList({ currentPage: 1, pageSize: 200 })
    if (res.code === 200) {
      const records = res.data.records || []
      myRegistrations.value = records.filter(item => item.eventId === event.value.id && item.registrationStatus !== 'CANCELLED')
      registeredProjectNames.value = myRegistrations.value.map(item => item.itemName)
    }
  } catch (error) {
    myRegistrations.value = []
    registeredProjectNames.value = []
  }
}

const refreshButtonState = () => {
  if (!event.value) return
  const now = new Date()
  const regStart = event.value.registrationStartTime ? new Date(event.value.registrationStartTime) : null
  const regEnd = event.value.registrationEndTime ? new Date(event.value.registrationEndTime) : null
  projects.value = projects.value.map(project => {
    const registration = myRegistrations.value.find(item => item.itemId === project.id)
    let registerDisabled = false
    let registerText = '立即报名'
    let cancelDisabled = true
    let cancelText = '取消报名'

    if (event.value.eventStatus !== 'OPEN') {
      registerDisabled = true; registerText = '赛事未开放报名'
    } else if (regStart && now < regStart) {
      registerDisabled = true; registerText = '报名未开始'
    } else if (regEnd && now > regEnd) {
      registerDisabled = true; registerText = '报名已截止'
    } else if (project.attendance >= project.maxAttendance) {
      registerDisabled = true; registerText = '名额已满'
    } else if (normalizeAthleteStatus(athleteApplyStatus.value) !== 'APPROVED') {
      registerDisabled = true; registerText = '请先通过运动员审核'
    } else if (registration) {
      registerDisabled = true; registerText = '已报名'
    }

    if (registration) {
      if (regEnd && now > regEnd) {
        cancelDisabled = true; cancelText = '报名截止后不可取消'
      } else {
        cancelDisabled = false; cancelText = '取消报名'
      }
    } else {
      cancelDisabled = true; cancelText = '未报名'
    }

    return { ...project, registrationId: registration ? registration.id : null, registerDisabled, registerText, cancelDisabled, cancelText }
  })
}

const handleRegister = (project) => {
  ElMessageBox.confirm(`确认报名项目 ${project.itemName} 吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'primary'
  }).then(async () => {
    try {
      const res = await applyProject(project.id)
      if (res.code === 200) {
        ElMessage.success('报名申请已提交')
        await loadMyRegistrations()
        await loadProjects()
      } else {
        ElMessage.error(res.msg || '报名失败')
      }
    } catch (error) { console.error(error) }
  })
}

const handleCancel = (project) => {
  if (!project.registrationId) return
  ElMessageBox.confirm(`确认取消项目 ${project.itemName} 的报名吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      const res = await cancelRegistration(project.registrationId)
      if (res.code === 200) {
        ElMessage.success('取消成功')
        await loadMyRegistrations()
        await loadProjects()
      } else {
        ElMessage.error(res.msg || '取消失败')
      }
    } catch (error) { console.error(error) }
  })
}

onMounted(async () => {
  await loadEvent()
  await loadAthleteStatus()
  await loadMyRegistrations()
  await loadProjects()
})
</script>

<style scoped>
/* ==========================================
   CSS Variables — works with both light/dark
   ========================================== */
.event-detail-container {
  --c-blue:    #3b82f6;
  --c-blue-l:  #dbeafe;
  --c-green:   #22c55e;
  --c-green-l: #dcfce7;
  --c-orange:  #f97316;
  --c-orange-l:#ffedd5;
  --c-purple:  #a855f7;
  --c-purple-l:#f3e8ff;
  --c-red:     #ef4444;
  --c-red-l:   #fee2e2;
  --c-yellow:  #eab308;
  --c-yellow-l:#fef9c3;

  --card-bg:       var(--el-bg-color);
  --card-border:   var(--el-border-color-light);
  --text-primary:  var(--el-text-color-primary);
  --text-regular:  var(--el-text-color-regular);
  --text-secondary:var(--el-text-color-secondary);
  --page-bg:       var(--el-bg-color-page);

  min-height: 100vh;
  background: var(--page-bg);
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ==========================================
   Hero Banner
   ========================================== */
.event-hero {
  position: relative;
  min-height: 280px;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 40%, #06b6d4 100%);
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.event-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 70% 40%, rgba(99,102,241,0.35) 0%, transparent 60%),
  radial-gradient(ellipse at 20% 80%, rgba(6,182,212,0.25) 0%, transparent 50%);
  pointer-events: none;
}

/* Decorative sport lines */
.event-hero::after {
  content: '';
  position: absolute;
  right: -40px;
  top: -40px;
  width: 300px;
  height: 300px;
  border: 40px solid rgba(255,255,255,0.06);
  border-radius: 50%;
  pointer-events: none;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.65) 0%, rgba(0,0,0,0.1) 60%, transparent 100%);
}

.hero-content {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 24px 32px 32px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.25);
  color: #fff;
  padding: 7px 14px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  backdrop-filter: blur(8px);
  width: fit-content;
  transition: background 0.2s;
}
.back-btn:hover { background: rgba(255,255,255,0.25); }

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  width: fit-content;
}
.hero-badge.badge--green  { background: #16a34a; color: #fff; }
.hero-badge.badge--blue   { background: #2563eb; color: #fff; }
.hero-badge.badge--gray   { background: rgba(255,255,255,0.2); color: #fff; border: 1px solid rgba(255,255,255,0.3); }
.hero-badge.badge--orange { background: #ea580c; color: #fff; }

.badge-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: rgba(255,255,255,0.9);
  animation: pulse 1.8s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%       { opacity: 0.5; transform: scale(0.7); }
}

.hero-title {
  margin: 0;
  font-size: clamp(22px, 4vw, 34px);
  font-weight: 800;
  color: #fff;
  line-height: 1.2;
  text-shadow: 0 2px 12px rgba(0,0,0,0.3);
  letter-spacing: -0.3px;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: rgba(255,255,255,0.8);
  font-size: 13px;
}

.hero-actions {
  margin-top: 16px;
}

.hero-public-scores-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  backdrop-filter: blur(6px);
  transition: background 0.2s, border-color 0.2s;
}
.hero-public-scores-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.55);
}

/* ==========================================
   Page Body
   ========================================== */
.page-body {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 20px 48px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ==========================================
   Athlete Status Bar
   ========================================== */
.status-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-radius: 14px;
  border-left: 4px solid;
  gap: 12px;
}

.status-bar--approved {
  background: color-mix(in srgb, var(--c-green) 12%, var(--card-bg));
  border-color: var(--c-green);
}
.status-bar--pending {
  background: color-mix(in srgb, var(--c-yellow) 12%, var(--card-bg));
  border-color: var(--c-yellow);
}
.status-bar--rejected {
  background: color-mix(in srgb, var(--c-red) 12%, var(--card-bg));
  border-color: var(--c-red);
}
.status-bar--none {
  background: color-mix(in srgb, var(--c-orange) 12%, var(--card-bg));
  border-color: var(--c-orange);
}

.status-bar-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.status-icon-wrap {
  width: 40px; height: 40px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  background: var(--card-bg);
  flex-shrink: 0;
}
.status-bar--approved .status-icon-wrap { color: var(--c-green); box-shadow: 0 0 0 3px color-mix(in srgb, var(--c-green) 20%, transparent); }
.status-bar--pending  .status-icon-wrap { color: var(--c-yellow); box-shadow: 0 0 0 3px color-mix(in srgb, var(--c-yellow) 20%, transparent); }
.status-bar--rejected .status-icon-wrap { color: var(--c-red);    box-shadow: 0 0 0 3px color-mix(in srgb, var(--c-red) 20%, transparent); }
.status-bar--none     .status-icon-wrap { color: var(--c-orange); box-shadow: 0 0 0 3px color-mix(in srgb, var(--c-orange) 20%, transparent); }

.status-bar-title {
  font-weight: 700;
  font-size: 14px;
  color: var(--text-primary);
}
.status-bar-sub {
  font-size: 13px;
  color: var(--text-regular);
  margin-top: 2px;
}

.qualify-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 18px;
  border-radius: 20px;
  border: none;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  background: linear-gradient(135deg, #f97316, #fb923c);
  color: #fff;
  box-shadow: 0 4px 14px rgba(249,115,22,0.35);
  transition: transform 0.15s, box-shadow 0.15s;
}
.qualify-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(249,115,22,0.45);
}

/* ==========================================
   Registered Notice
   ========================================== */
.registered-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  background: color-mix(in srgb, var(--c-blue) 10%, var(--card-bg));
  border: 1px solid color-mix(in srgb, var(--c-blue) 30%, transparent);
  border-radius: 10px;
  color: var(--c-blue);
  font-size: 13px;
}
.registered-notice strong { font-weight: 700; }

/* ==========================================
   Info Cards Grid
   ========================================== */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
}

.info-card {
  padding: 18px 20px;
  border-radius: 14px;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  overflow: hidden;
}
.info-card::before {
  content: '';
  position: absolute;
  top: -20px; right: -20px;
  width: 80px; height: 80px;
  border-radius: 50%;
  opacity: 0.12;
}
.info-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }

.info-card--blue   { border-top: 3px solid var(--c-blue);   }
.info-card--green  { border-top: 3px solid var(--c-green);  }
.info-card--orange { border-top: 3px solid var(--c-orange); }
.info-card--purple { border-top: 3px solid var(--c-purple); }

.info-card--blue::before   { background: var(--c-blue);   }
.info-card--green::before  { background: var(--c-green);  }
.info-card--orange::before { background: var(--c-orange); }
.info-card--purple::before { background: var(--c-purple); }

.info-card-icon { font-size: 22px; margin-bottom: 8px; }
.info-card-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.info-card-value {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  line-height: 1.5;
}
.info-card-value.big {
  font-size: 20px;
  font-weight: 800;
  color: var(--c-green);
}

/* ==========================================
   Projects Section
   ========================================== */
.projects-section {}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-title-accent {
  display: inline-block;
  width: 4px; height: 20px;
  border-radius: 2px;
  background: linear-gradient(180deg, #3b82f6, #a855f7);
}

.project-count {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
  background: color-mix(in srgb, var(--c-blue) 12%, var(--card-bg));
  color: var(--c-blue);
  border: 1px solid color-mix(in srgb, var(--c-blue) 25%, transparent);
}

.project-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

/* Project card colors — 5 variants cycling */
.project-card {
  border-radius: 16px;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
}
.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.12);
}

/* Color accent strip at top */
.project-card--color1::before { content:''; display:block; height:4px; background: linear-gradient(90deg, #3b82f6, #06b6d4); }
.project-card--color2::before { content:''; display:block; height:4px; background: linear-gradient(90deg, #22c55e, #84cc16); }
.project-card--color3::before { content:''; display:block; height:4px; background: linear-gradient(90deg, #f97316, #fbbf24); }
.project-card--color4::before { content:''; display:block; height:4px; background: linear-gradient(90deg, #a855f7, #ec4899); }
.project-card--color5::before { content:''; display:block; height:4px; background: linear-gradient(90deg, #ef4444, #f97316); }

.project-card-top {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 16px 12px;
}

.project-index {
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
  color: var(--el-border-color);
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
  letter-spacing: -1px;
}

.project-info { flex: 1; min-width: 0; }
.project-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.3;
}

.project-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.tag {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 9px;
  border-radius: 10px;
}
.tag--gender {
  background: color-mix(in srgb, var(--c-blue) 14%, var(--card-bg));
  color: var(--c-blue);
  border: 1px solid color-mix(in srgb, var(--c-blue) 25%, transparent);
}
.tag--grade {
  background: color-mix(in srgb, var(--c-purple) 14%, var(--card-bg));
  color: var(--c-purple);
  border: 1px solid color-mix(in srgb, var(--c-purple) 25%, transparent);
}

.project-card-bottom {
  padding: 12px 16px 16px;
  border-top: 1px solid var(--card-border);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.quota-bar-wrap {}
.quota-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 6px;
  font-weight: 500;
}
.quota-nums { font-weight: 700; color: var(--text-regular); }

.quota-bar {
  height: 6px;
  border-radius: 3px;
  background: var(--el-border-color-lighter);
  overflow: hidden;
}
.quota-fill {
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, #3b82f6, #06b6d4);
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}
.quota-fill--full {
  background: linear-gradient(90deg, #ef4444, #f97316);
}

.project-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  padding: 8px 12px;
  border-radius: 8px;
  border: none;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.15s, opacity 0.15s, box-shadow 0.15s;
  white-space: nowrap;
}
.action-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}
.action-btn:not(:disabled):hover { transform: translateY(-1px); }

.action-btn--primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  box-shadow: 0 3px 12px rgba(59,130,246,0.35);
}
.action-btn--primary:not(:disabled):hover {
  box-shadow: 0 5px 18px rgba(59,130,246,0.5);
}

.action-btn--danger {
  background: color-mix(in srgb, var(--c-red) 12%, var(--card-bg));
  color: var(--c-red);
  border: 1px solid color-mix(in srgb, var(--c-red) 25%, transparent);
}
.action-btn--danger:not(:disabled):hover {
  background: color-mix(in srgb, var(--c-red) 20%, var(--card-bg));
}

/* ==========================================
   Responsive
   ========================================== */
@media (max-width: 600px) {
  .hero-content { padding: 16px 16px 24px; }
  .page-body    { padding: 16px 12px 40px; }
  .info-grid    { grid-template-columns: 1fr 1fr; }
  .project-cards { grid-template-columns: 1fr; }
}
</style>