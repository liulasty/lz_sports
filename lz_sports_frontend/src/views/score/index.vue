<template>
  <div class="score-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>成绩查询</span>
          <div class="filter-box">
            <el-input v-model="queryParams.eventName" placeholder="赛事名称" style="width: 200px; margin-right: 10px" @keyup.enter="handleQuery" />
            <el-button type="primary" @click="handleQuery">查询</el-button>
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
        <el-table-column prop="createTime" label="发布时间">
           <template #default="scope">
             {{ formatDate(scope.row.createTime) }}
           </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.currentPage"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
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
import request from '@/utils/request'

const loading = ref(false)
const scoreList = ref([])
const total = ref(0)

const queryParams = reactive({
  currentPage: 1,
  pageSize: 10,
  eventName: ''
})

const getList = async () => {
  loading.value = true
  try {
    // Need backend API for score list
    // Let's assume a generic list endpoint for now, or create one if missing
    // We don't have a specific "list scores" endpoint in ScoreController yet.
    // We should probably add one in Backend Phase 5.
    // For now, I'll mock it or use a placeholder if backend isn't ready.
    // Wait, we didn't add list endpoint in ScoreController.
    // I should add it.
    
    // Temporary: return empty or try to call a new endpoint
    const res = await request({
      url: '/sports/score/page',
      method: 'get',
      params: queryParams
    })
    
    if (res.code === 1) {
      scoreList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
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
  return new Date(dateStr).toLocaleDateString()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
