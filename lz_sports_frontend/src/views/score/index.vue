<template>
  <div class="score-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的成绩</span>
          <div class="filter-box">
            <el-select v-model="eventId" clearable placeholder="选择赛事" style="width: 220px; margin-right: 10px">
              <el-option v-for="item in eventOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <el-button type="primary" @click="getList">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="scoreList" style="width: 100%" v-loading="loading">
        <el-table-column prop="eventName" label="赛事名称" />
        <el-table-column prop="itemName" label="项目名称" />
        <el-table-column prop="athleteName" label="运动员" />
        <el-table-column prop="scoreValue" label="成绩" />
        <el-table-column prop="scoreRank" label="名次">
          <template #default="scope">
             <span v-if="scope.row.scoreRank <= 3" style="color: #ff9900; font-weight: bold;">第 {{ scope.row.scoreRank }} 名</span>
             <span v-else>{{ scope.row.scoreRank }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间">
           <template #default="scope">
             {{ formatDate(scope.row.publishedAt) }}
           </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyScores } from '@/api/score'
import { getEventList } from '@/api/event'

const loading = ref(false)
const scoreList = ref([])
const eventOptions = ref([])
const eventId = ref(null)

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

onMounted(() => {
  loadEvents()
  getList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
