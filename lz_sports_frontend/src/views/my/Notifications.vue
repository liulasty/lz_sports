<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span>消息通知</span>
          <el-button type="primary" size="small" @click="handleMarkAllRead">全部标记为已读</el-button>
        </div>
      </template>
      <el-table :data="notifications" style="width: 100%" v-loading="loading">
        <el-table-column width="50">
          <template #default="scope">
            <div class="unread-dot" v-if="!scope.row.isRead"></div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="content" label="内容" />
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              v-if="!scope.row.isRead"
              type="primary" 
              link 
              @click="handleMarkRead(scope.row)"
            >
              标为已读
            </el-button>
            <span v-else style="color: var(--el-text-color-secondary); font-size: 12px;">已读</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-pagination
          v-model:current-page="queryParams.currentPage"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
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

const handleMarkRead = async (row) => {
  try {
    const res = await markNotificationRead(row.id)
    if (res.code === 200) {
      row.isRead = true
      // 可触发全局事件更新 Navbar 角标
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
.app-container {
  padding: 20px;
}
.unread-dot {
  width: 8px;
  height: 8px;
  background-color: var(--el-color-danger);
  border-radius: 50%;
  display: inline-block;
}

:deep(.el-table) {
  background: var(--el-bg-color) !important;
  color: var(--el-text-color-primary);
}
:deep(.el-table__inner-wrapper) {
  background: var(--el-bg-color) !important;
}
:deep(.el-table__body-wrapper td) {
  background: var(--el-bg-color) !important;
}
</style>
