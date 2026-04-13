<template>
  <div class="notify-page">
    <div class="bg-layer">
      <div class="bg-grid"></div>
      <div class="blob b1"></div>
      <div class="blob b2"></div>
    </div>

    <section class="hero-panel">
      <div class="hero-content">
        <p class="hero-badge">NOTIFICATION HUB</p>
        <h1 class="hero-title">消息通知中心</h1>
        <p class="hero-sub">在这里快速处理系统提醒、审核通知与赛事动态，重要信息不再错过。</p>
      </div>
      <div class="hero-actions">
        <button class="btn-solid" @click="handleMarkAllRead">全部标记为已读</button>
      </div>
    </section>

    <section class="stats-grid">
      <article class="stat-card">
        <p class="stat-label">总消息</p>
        <p class="stat-value">{{ total }}</p>
      </article>
      <article class="stat-card unread">
        <p class="stat-label">未读消息</p>
        <p class="stat-value">{{ unreadCount }}</p>
      </article>
      <article class="stat-card read">
        <p class="stat-label">已读消息</p>
        <p class="stat-value">{{ readCount }}</p>
      </article>
    </section>

    <section class="list-shell lz-surface lz-ep-dark" v-loading="loading">
      <div class="toolbar">
        <div class="tabs">
          <button class="tab-btn" :class="{ active: activeFilter === 'all' }" @click="changeFilter('all')">全部</button>
          <button class="tab-btn" :class="{ active: activeFilter === 'unread' }" @click="changeFilter('unread')">未读</button>
          <button class="tab-btn" :class="{ active: activeFilter === 'read' }" @click="changeFilter('read')">已读</button>
        </div>
        <div class="toolbar-meta">当前第 {{ queryParams.currentPage }} 页</div>
      </div>

      <div v-if="notifications.length" class="notify-list">
        <article
          v-for="item in notifications"
          :key="item.id"
          class="notify-item"
          :class="{ unread: !item.isRead }"
        >
          <div class="dot"></div>
          <div class="item-main">
            <div class="item-head">
              <h3 class="item-title">{{ item.title || '系统通知' }}</h3>
              <span class="item-time">{{ formatTime(item.createTime) }}</span>
            </div>
            <p class="item-content">{{ item.content }}</p>
            <div class="item-footer">
              <span class="chip" :class="item.isRead ? 'chip-read' : 'chip-unread'">
                {{ item.isRead ? '已读' : '未读' }}
              </span>
              <button v-if="!item.isRead" class="link-btn" @click="handleMarkRead(item)">标记为已读</button>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">⌁</div>
        <p>当前筛选下暂无消息</p>
      </div>

      <div class="pagination-wrap lz-actions">
        <el-pagination
          v-model:current-page="queryParams.currentPage"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handleCurrentChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getNotificationPage, markNotificationRead, markAllNotificationRead } from '@/api/notification'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const notifications = ref([])
const total = ref(0)

const queryParams = reactive({
  currentPage: 1,
  pageSize: 10,
  isRead: null
})
const activeFilter = ref('all')
const unreadCount = computed(() => notifications.value.filter(item => !item.isRead).length)
const readCount = computed(() => notifications.value.filter(item => item.isRead).length)

const getList = async () => {
  loading.value = true
  try {
    const res = await getNotificationPage(queryParams)
    if (res.code === 200) {
      notifications.value = res.data.records || []
      total.value = res.data.total || 0
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

const changeFilter = (type) => {
  activeFilter.value = type
  queryParams.currentPage = 1
  if (type === 'unread') {
    queryParams.isRead = false
  } else if (type === 'read') {
    queryParams.isRead = true
  } else {
    queryParams.isRead = null
  }
  getList()
}

const handleMarkRead = async (row) => {
  try {
    const res = await markNotificationRead(row.id)
    if (res.code === 200) {
      row.isRead = true
      ElMessage.success('已标记为已读')
    }
  } catch (error) {
    console.error(error)
  }
}

const handleMarkAllRead = async () => {
  try {
    const res = await markAllNotificationRead()
    if (res.code === 200) {
      ElMessage.success('已全部标记为已读')
      getList()
    }
  } catch (error) {
    console.error(error)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.notify-page {
  position: relative;
  min-height: calc(100vh - 84px);
  padding: 24px;
  color: var(--text-primary);
  overflow: hidden;
}

.bg-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(color-mix(in srgb, var(--accent) 10%, transparent) 1px, transparent 1px),
    linear-gradient(90deg, color-mix(in srgb, var(--accent) 10%, transparent) 1px, transparent 1px);
  background-size: 50px 50px;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
}

.b1 {
  width: 320px;
  height: 320px;
  left: -80px;
  top: -40px;
  background: rgba(255, 107, 53, 0.18);
}

.b2 {
  width: 300px;
  height: 300px;
  right: -70px;
  bottom: -50px;
  background: rgba(85, 133, 255, 0.16);
}

.hero-panel,
.stats-grid,
.list-shell {
  position: relative;
  z-index: 1;
}

.hero-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.hero-badge {
  margin: 0 0 10px;
  color: var(--accent);
  letter-spacing: 0.18em;
  font-size: 11px;
  font-weight: 700;
}

.hero-title {
  margin: 0 0 8px;
  font-size: 34px;
  color: var(--text-primary);
}

.hero-sub {
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
  box-shadow: 0 10px 26px rgba(255, 107, 53, 0.24);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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

.stat-card.unread { border-color: color-mix(in srgb, var(--accent) 40%, var(--border)); }
.stat-card.read { border-color: color-mix(in srgb, #4fb77a 40%, var(--border)); }

.stat-label {
  margin: 0 0 8px;
  color: color-mix(in srgb, var(--text-secondary) 92%, transparent);
  font-size: 12px;
}

.stat-value {
  margin: 0;
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 700;
}

.list-shell {
  padding: 14px;
  backdrop-filter: blur(10px);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.tabs {
  display: inline-flex;
  gap: 6px;
  padding: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
}

.tab-btn {
  border: none;
  background: transparent;
  color: color-mix(in srgb, var(--text-secondary) 92%, transparent);
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 12px;
  cursor: pointer;
}

.tab-btn.active {
  color: #ffe0d5;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.95), rgba(224, 84, 30, 0.95));
}

.toolbar-meta {
  color: color-mix(in srgb, var(--text-secondary) 90%, transparent);
  font-size: 12px;
}

.notify-list {
  display: grid;
  gap: 10px;
}

.notify-item {
  display: grid;
  grid-template-columns: 14px 1fr;
  gap: 10px;
  border: 1px solid var(--border);
  background: color-mix(in srgb, var(--bg-card) 88%, transparent);
  border-radius: 12px;
  padding: 12px 14px;
  transition: transform 0.16s ease, border-color 0.16s ease, background 0.16s ease;
}

.notify-item:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--text-secondary) 18%, var(--border));
  background: color-mix(in srgb, var(--bg-card) 94%, var(--bg-soft));
}

.notify-item.unread {
  border-color: color-mix(in srgb, var(--accent) 40%, var(--border));
  background: linear-gradient(90deg, color-mix(in srgb, var(--accent) 10%, transparent), color-mix(in srgb, var(--bg-card) 90%, transparent));
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  margin-top: 6px;
  background: color-mix(in srgb, var(--text-secondary) 55%, transparent);
}

.notify-item.unread .dot {
  background: var(--accent);
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--accent) 18%, transparent);
}

.item-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 14px;
}

.item-title {
  margin: 0;
  font-size: 15px;
  color: var(--text-primary);
}

.item-time {
  color: color-mix(in srgb, var(--text-secondary) 92%, transparent);
  font-size: 12px;
  white-space: nowrap;
}

.item-content {
  margin: 8px 0 10px;
  color: color-mix(in srgb, var(--text-secondary) 85%, transparent);
  line-height: 1.6;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chip {
  font-size: 11px;
  border-radius: 999px;
  padding: 3px 9px;
  border: 1px solid transparent;
}

.chip-unread {
  color: color-mix(in srgb, var(--accent) 70%, var(--text-primary));
  border-color: color-mix(in srgb, var(--accent) 45%, var(--border));
  background: color-mix(in srgb, var(--accent) 10%, transparent);
}

.chip-read {
  color: color-mix(in srgb, #4fb77a 70%, var(--text-primary));
  border-color: color-mix(in srgb, #4fb77a 45%, var(--border));
  background: color-mix(in srgb, #4fb77a 10%, transparent);
}

.link-btn {
  border: none;
  background: transparent;
  color: var(--accent);
  cursor: pointer;
  font-size: 13px;
}

.empty-state {
  min-height: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 36px;
  margin-bottom: 10px;
  color: var(--accent);
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .hero-panel { flex-direction: column; align-items: flex-start; }
  .stats-grid { grid-template-columns: 1fr; }
  .item-head { flex-direction: column; gap: 6px; }
  .pagination-wrap { justify-content: center; }
}
</style>
