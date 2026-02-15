<template>
  <div class="event-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>赛事列表</span>
          <div class="filter-box">
            <el-input v-model="queryParams.name" placeholder="赛事名称" style="width: 200px; margin-right: 10px" @keyup.enter="handleQuery" />
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="6" v-for="item in eventList" :key="item.id" style="margin-bottom: 20px;">
          <el-card :body-style="{ padding: '0px' }" shadow="hover">
            <div class="image-container">
               <img v-if="item.imageUrls && item.imageUrls.length > 0" :src="item.imageUrls[0]" class="image"/>
               <div v-else class="image-placeholder">暂无图片</div>
            </div>
            <div style="padding: 14px">
              <span class="event-title">{{ item.name }}</span>
              <div class="bottom">
                <time class="time">{{ formatDate(item.date) }}</time>
                <el-tag size="small" :type="getStatusType(item)">{{ getStatus(item) }}</el-tag>
              </div>
              <div class="description">
                <div>要求：{{ item.type }}</div>
                <div>费用：{{ item.fee }}元</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="pagination-container">
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getEventList } from '@/api/event'

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
    if (res.code === 1) {
      eventList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  }
}

const handleQuery = () => {
  queryParams.currentPage = 1
  getList()
}

const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.currentPage = val
  getList()
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString()
}

const getStatus = (item) => {
  const now = new Date()
  const start = new Date(item.date)
  const end = new Date(item.end)
  if (now < start) return '未开始'
  if (now > end) return '已结束'
  return '进行中'
}

const getStatusType = (item) => {
  const status = getStatus(item)
  if (status === '进行中') return 'success'
  if (status === '已结束') return 'info'
  return 'warning'
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.event-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.image-container {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}
.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.image-placeholder {
  color: #909399;
}
.event-title {
  font-weight: bold;
  font-size: 16px;
  display: block;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.bottom {
  margin-top: 13px;
  line-height: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.time {
  font-size: 12px;
  color: #999;
}
.description {
  margin-top: 10px;
  font-size: 13px;
  color: #666;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
