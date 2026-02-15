<template>
  <div class="dashboard-container">
    <!-- Stat Cards -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-title">赛事总数</div>
            <div class="stat-value">{{ totalEvents }}</div>
          </div>
          <el-icon class="stat-icon" :size="40" color="#409EFF"><Trophy /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-title">本月活动分布</div>
            <div class="stat-value">3 类</div>
          </div>
          <el-icon class="stat-icon" :size="40" color="#67C23A"><PieChart /></el-icon>
        </el-card>
      </el-col>
      <!-- Placeholders for future stats -->
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-title">活跃用户</div>
            <div class="stat-value">--</div>
          </div>
          <el-icon class="stat-icon" :size="40" color="#E6A23C"><User /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-title">今日访问</div>
            <div class="stat-value">--</div>
          </div>
          <el-icon class="stat-icon" :size="40" color="#F56C6C"><DataLine /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- Charts & Tables -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>本月赛事类型分布</span>
            </div>
          </template>
          <div ref="pieChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="table-card">
          <template #header>
            <div class="card-header">
              <span>近期发布赛事</span>
              <el-button link type="primary" @click="$router.push('/event')">查看更多</el-button>
            </div>
          </template>
          <el-table :data="newTenList" style="width: 100%" size="small" :show-header="true">
            <el-table-column prop="name" label="名称" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="date" label="时间" width="100">
              <template #default="scope">
                {{ formatDate(scope.row.date) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Trophy, PieChart, User, DataLine } from '@element-plus/icons-vue'
import { getNewTenEvents, getTotalEvents, getChartData } from '@/api/event'
import * as echarts from 'echarts'

const totalEvents = ref(0)
const newTenList = ref([])
const pieChartRef = ref(null)
let pieChart = null

const fetchData = async () => {
  // 1. Total Events
  try {
    const res = await getTotalEvents()
    if (res.code === 1) {
      totalEvents.value = res.data
    }
  } catch (e) {
    console.error(e)
  }

  // 2. New Ten
  try {
    const res = await getNewTenEvents()
    if (res.code === 1) {
      newTenList.value = res.data
    }
  } catch (e) {
    console.error(e)
  }

  // 3. Chart Data
  try {
    const dateStr = new Date().toISOString().slice(0, 7).replace('-', '') // YYYYMM
    const res = await getChartData(dateStr)
    if (res.code === 1 && res.data) {
      initPieChart(res.data)
    }
  } catch (e) {
    console.error(e)
  }
}

const initPieChart = (data) => {
  if (!pieChartRef.value) return
  
  pieChart = echarts.init(pieChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: '5%',
      left: 'center'
    },
    series: [
      {
        name: '赛事类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: data.online || 0, name: '线上报名' },
          { value: data.group || 0, name: '单位报名' },
          { value: data.offline || 0, name: '线下报名' },
          { value: data.other || 0, name: '其他' }
        ]
      }
    ]
  }
  
  pieChart.setOption(option)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', () => {
    pieChart && pieChart.resize()
  })
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
}
.stat-content {
  display: flex;
  flex-direction: column;
}
.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.stat-icon {
  float: right;
}
.chart-row {
  margin-top: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
