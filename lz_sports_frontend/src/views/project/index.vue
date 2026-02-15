<template>
  <div class="project-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>项目列表</span>
          <div class="filter-box">
            <el-input v-model="queryParams.name" placeholder="项目名称" style="width: 200px; margin-right: 10px" @keyup.enter="handleQuery" />
            <el-input v-model="queryParams.event" placeholder="所属赛事" style="width: 200px; margin-right: 10px" @keyup.enter="handleQuery" />
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="projectList" style="width: 100%" v-loading="loading">
        <el-table-column prop="itemName" label="项目名称" width="150" />
        <el-table-column prop="eventName" label="所属赛事" width="150" />
        <el-table-column prop="projectStart" label="比赛时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.projectStart) }}
          </template>
        </el-table-column>
        <el-table-column prop="limitation" label="限制/要求" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.registrationStatus === '已报名' ? 'success' : 'info'">
              {{ scope.row.registrationStatus || '未报名' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button 
              v-if="!scope.row.registrationStatus || scope.row.registrationStatus === '未报名'"
              type="primary" 
              size="small" 
              @click="handleApply(scope.row)"
              :disabled="!isAthlete"
            >
              {{ isAthlete ? '报名' : '仅运动员可报名' }}
            </el-button>
            <el-button 
              v-else
              type="info" 
              size="small" 
              disabled
            >
              {{ scope.row.registrationStatus }}
            </el-button>
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

    <!-- 报名确认弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="报名确认"
      width="30%"
    >
      <span>确定要报名参加 {{ currentProject?.itemName }} 吗？</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmApply">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getProjectList } from '@/api/project'
import { applyProject } from '@/api/registration'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const isAthlete = computed(() => userStore.userInfo && userStore.userInfo.type === '运动员')

const loading = ref(false)
const projectList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentProject = ref(null)

const queryParams = reactive({
  currentPage: 1,
  pageSize: 10,
  name: '',
  event: ''
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getProjectList(queryParams)
    if (res.code === 1) {
      projectList.value = res.data.records
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

const handleApply = (row) => {
  if (!isAthlete.value) {
    ElMessage.warning('只有认证运动员才能报名参加项目')
    return
  }
  currentProject.value = row
  dialogVisible.value = true
}

const confirmApply = async () => {
  if (!currentProject.value) return
  try {
    // Call Registration API
    const res = await applyProject(currentProject.value.itemId)
    
    if (res.code === 1) {
      ElMessage.success('报名申请已提交')
      dialogVisible.value = false
      getList() // Refresh list to update status
    }
  } catch (error) {
    console.error(error)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.project-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
