<template>
  <div class="registration-stats">
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon total">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">总报名数</div>
            <div class="stat-value">{{ stats.total || 0 }}</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card pending">
          <div class="stat-icon warning">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">待审核</div>
            <div class="stat-value warning-text">{{ stats.pending || 0 }}</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon success">
            <el-icon><Select /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">已通过</div>
            <div class="stat-value success-text">{{ stats.approved || 0 }}</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon danger">
            <el-icon><CloseBold /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">已拒绝</div>
            <div class="stat-value danger-text">{{ stats.rejected || 0 }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, Timer, Select, CloseBold } from '@element-plus/icons-vue'
import { getRegistrationStats } from '@/api/eventAdmin'

const props = defineProps({
  eventId: {
    type: [Number, String],
    required: true
  }
})

const stats = ref({
  total: 0,
  pending: 0,
  approved: 0,
  rejected: 0
})

const fetchStats = async () => {
  if (!props.eventId) return
  try {
    const res = await getRegistrationStats(props.eventId)
    if (res.code === 200) {
      stats.value = res.data || { total: 0, pending: 0, approved: 0, rejected: 0 }
    }
  } catch (error) {
    console.error('获取报名统计失败', error)
  }
}

onMounted(() => {
  fetchStats()
})

// 暴露刷新方法供父组件调用
defineExpose({
  refresh: fetchStats
})
</script>

<style scoped>
.registration-stats {
  margin-bottom: 20px;
}

.stat-card {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  border: 1px solid var(--el-border-color-light);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
}

.stat-card.pending {
  border-color: var(--el-color-warning-light-5);
  background: var(--el-color-warning-light-9);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.stat-icon.total {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.stat-icon.warning {
  background: var(--el-color-warning-light-8);
  color: var(--el-color-warning);
}

.stat-icon.success {
  background: var(--el-color-success-light-9);
  color: var(--el-color-success);
}

.stat-icon.danger {
  background: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--el-text-color-primary);
  line-height: 1;
}

.warning-text {
  color: var(--el-color-warning);
}

.success-text {
  color: var(--el-color-success);
}

.danger-text {
  color: var(--el-color-danger);
}
</style>
