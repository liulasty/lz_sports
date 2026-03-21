<template>
  <div class="dashboard-container">
    <div v-if="!isAdmin">
      <el-empty description="欢迎使用体育赛事管理系统！您可以在侧边栏浏览赛事大厅。" />
    </div>
    
    <div v-else>
      <!-- Stat Cards -->
      <el-row :gutter="20" class="stat-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-title">总用户数</div>
              <div class="stat-value">{{ overviewStats.totalUsers || 0 }}</div>
            </div>
            <el-icon class="stat-icon" :size="40" color="#409EFF"><User /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-title">赛事总数</div>
              <div class="stat-value">{{ overviewStats.totalEvents || 0 }}</div>
            </div>
            <el-icon class="stat-icon" :size="40" color="#67C23A"><Trophy /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-title">总报名数</div>
              <div class="stat-value">{{ overviewStats.totalRegistrations || 0 }}</div>
            </div>
            <el-icon class="stat-icon" :size="40" color="#E6A23C"><Tickets /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-title">本月新增用户</div>
              <div class="stat-value">{{ overviewStats.newUsersThisMonth || 0 }}</div>
            </div>
            <el-icon class="stat-icon" :size="40" color="#F56C6C"><TrendCharts /></el-icon>
          </el-card>
        </el-col>
      </el-row>

      <!-- Charts Row -->
      <el-row :gutter="20" class="chart-row">
        <!-- 赛事状态分布 -->
        <el-col :span="8">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="card-header">
                <span>赛事状态分布</span>
              </div>
            </template>
            <div class="status-list">
              <div class="status-item" v-for="(count, status) in overviewStats.eventsByStatus" :key="status">
                <span class="status-label">{{ formatStatus(status) }}</span>
                <span class="status-count">{{ count }}</span>
              </div>
              <el-empty v-if="!overviewStats.eventsByStatus || Object.keys(overviewStats.eventsByStatus).length === 0" description="暂无数据" :image-size="60" />
            </div>
          </el-card>
        </el-col>

        <!-- 赛事维度统计 -->
        <el-col :span="16">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="card-header">
                <span>各赛事统计</span>
              </div>
            </template>
            <el-table :data="eventStats" style="width: 100%" height="300">
              <el-table-column prop="eventName" label="赛事名称" min-width="150" show-overflow-tooltip />
              <el-table-column prop="totalRegistrations" label="报名总数" align="center" />
              <el-table-column prop="approvedRegistrations" label="审核通过" align="center" />
              <el-table-column prop="publishedProjects" label="发布成绩项目数" align="center" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Trophy, PieChart, User, Setting, Tickets, TrendCharts } from '@element-plus/icons-vue'
import { getOverviewStats, getEventStats } from '@/api/stats'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => {
  const role = userStore.userInfo?.role || userStore.userInfo?.type
  return role === 'SUPER_ADMIN' || role === 'SCHOOL_ADMIN' || role === '管理员'
})

const overviewStats = ref({})
const eventStats = ref([])

const formatStatus = (status) => {
  const map = {
    'DRAFT': '草稿',
    'OPEN': '报名中',
    'ONGOING': '进行中',
    'FINISHED': '已结束'
  }
  return map[status] || status
}

const loadData = async () => {
  if (!isAdmin.value) return
  try {
    const res1 = await getOverviewStats()
    if (res1.code === 200) {
      overviewStats.value = res1.data
    }
    const res2 = await getEventStats()
    if (res2.code === 200) {
      eventStats.value = res2.data
    }
  } catch (error) {
    console.error('Failed to load stats', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
}

.stat-content {
  position: relative;
  z-index: 2;
}

.stat-title {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 12px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}

.stat-icon {
  position: absolute;
  right: 20px;
  bottom: 20px;
  opacity: 0.2;
  transition: all 0.3s;
}

.stat-card:hover .stat-icon {
  transform: scale(1.2);
  opacity: 0.3;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
}

.status-list {
  padding: 10px;
  height: 300px;
  overflow-y: auto;
}

.status-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  margin-bottom: 10px;
  background-color: var(--el-fill-color-light);
  border-radius: 6px;
}

.status-label {
  color: var(--el-text-color-regular);
  font-weight: 500;
}

.status-count {
  color: var(--el-color-primary);
  font-weight: bold;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
</style>
