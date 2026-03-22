<template>
  <div class="event-manage-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>赛事管理</span>
          <div class="filter-box">
            <el-input v-model="queryParams.name" placeholder="赛事名称" style="width: 200px; margin-right: 10px" @keyup.enter="handleQuery" />
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button type="success" @click="$router.push('/event-create')">新建赛事</el-button>
          </div>
        </div>
      </template>

      <el-table :data="eventList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="赛事名称" />
        <el-table-column prop="type" label="参赛要求" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
             <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" width="300">
          <template #default="scope">
            {{ scope.row.date }} 至 {{ scope.row.end }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="450">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            <el-button v-if="isSuperAdmin" type="info" size="small" @click="handleAssignAdmin(scope.row)">分配管理员</el-button>
            
            <el-dropdown style="margin-left: 10px">
              <el-button type="warning" size="small">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleStatus(scope.row, 'OPEN')" v-if="scope.row.status === 'DRAFT'">发布赛事</el-dropdown-item>
                  <el-dropdown-item @click="handleWithdraw(scope.row)" v-if="scope.row.status === 'OPEN'">撤回赛事</el-dropdown-item>
                  <el-dropdown-item @click="handleStatus(scope.row, 'FINISHED')" v-if="scope.row.status === 'ONGOING'">结束赛事</el-dropdown-item>
                  <el-dropdown-item @click="handleExport(scope.row)">导出名单</el-dropdown-item>
                  <el-dropdown-item @click="handleImportClick(scope.row)">导入成绩</el-dropdown-item>
                  <el-dropdown-item @click="handlePublishScore(scope.row)">发布成绩</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.currentPage"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Import Dialog -->
    <el-dialog v-model="importVisible" title="导入成绩" width="30%">
      <el-upload
        class="upload-demo"
        drag
        action="#"
        :http-request="uploadFile"
        :limit="1"
        accept=".xlsx, .xls"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          Drop file here or <em>click to upload</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            请上传 Excel 文件
          </div>
        </template>
      </el-upload>
    </el-dialog>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="50%">
      <el-form :model="form" label-width="100px">
        <el-form-item label="赛事名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="参赛要求">
          <el-input v-model="form.type" placeholder="例如：全校师生" />
        </el-form-item>
        <el-form-item label="报名费用">
          <el-input v-model="form.fee" type="number" placeholder="0" />
        </el-form-item>
        <el-form-item label="报名时间">
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="比赛时间">
          <el-date-picker
            v-model="form.eventTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="报名上限">
          <el-input-number v-model="form.maxItemsPerAthlete" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="图片链接">
           <el-input v-model="form.imageUrlInput" type="textarea" :rows="3" placeholder="请输入图片URL，每行一个" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <EventAdminManager 
      v-model:visible="adminManagerVisible"
      :event-id="currentEventId"
      @saved="getList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getEventList, addEvent, updateEvent, deleteEvent, changeEventStatus } from '@/api/event'
import { withdrawEvent } from '@/api/admin'
import { exportRegistration, importScores, publishScores } from '@/api/score'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, UploadFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import EventAdminManager from '@/components/EventAdminManager.vue'

const userStore = useUserStore()
const isSuperAdmin = computed(() => userStore.userInfo?.role === 'SUPER_ADMIN')

const loading = ref(false)
const eventList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const importVisible = ref(false)
const adminManagerVisible = ref(false)
const currentEventId = ref(null)
const dialogTitle = ref('新增赛事')
const isEdit = ref(false)

const queryParams = reactive({
  currentPage: 1,
  pageSize: 5,
  name: ''
})

const form = reactive({
  id: '',
  name: '',
  type: '',
  fee: '0',
  dateRange: [], // [start, end]
  eventTimeRange: [],
  maxItemsPerAthlete: 3,
  imageUrlInput: ''
})

const getStatusType = (status) => {
  if (status === 'OPEN' || status === 'ONGOING') return 'success'
  if (status === 'FINISHED') return 'info'
  return 'warning'
}

const getStatusLabel = (status) => {
  if (status === 'OPEN') return '报名中'
  if (status === 'CLOSED') return '报名结束'
  if (status === 'ONGOING') return '进行中'
  if (status === 'FINISHED') return '已结束'
  return '草稿'
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getEventList(queryParams)
    if (res.code === 200) {
      eventList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleStatus = (row, status) => {
  ElMessageBox.confirm(`确认将赛事状态更改为 ${getStatusLabel(status)} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await changeEventStatus(row.id, status)
      if (res.code === 200) {
        ElMessage.success('操作成功')
        getList()
      } else {
      ElMessage.error(error?.response?.data?.msg || error?.message || '操作失败')
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const handleWithdraw = (row) => {
  ElMessageBox.confirm('确认撤回该赛事吗？已报名的记录可能会受影响。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await withdrawEvent(row.id)
      if (res.code === 200) {
        ElMessage.success('赛事已撤回')
        getList()
      } else {
        ElMessage.error(res.msg || '撤回失败')
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const handleAssignAdmin = (row) => {
  currentEventId.value = row.id
  adminManagerVisible.value = true
}

const handleExport = async (row) => {
  try {
    const res = await exportRegistration(row.id)
    // Create Blob and download
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `${row.name}_名单.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    console.error(error)
    ElMessage.error('导出失败')
  }
}

const handleImportClick = (row) => {
  currentEventId.value = row.id
  importVisible.value = true
}

const uploadFile = async (param) => {
  const file = param.file
  try {
    const res = await importScores(currentEventId.value, file)
      if (res.code === 200) {
      ElMessage.success('导入成功')
      importVisible.value = false
    } else {
      ElMessage.error(res.msg || '导入失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('导入失败')
  }
}

const handlePublishScore = (row) => {
  ElMessageBox.confirm(`确认发布该赛事的所有成绩吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await publishScores(row.id)
      if (res.code === 200) {
        ElMessage.success('发布成功')
      } else {
        ElMessage.error(res.msg || '发布失败')
      }
    } catch (error) {
      console.error(error)
    }
  })
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

const handleAdd = () => {
  dialogTitle.value = '新增赛事'
  isEdit.value = false
  form.id = ''
  form.name = ''
  form.type = ''
  form.fee = '0'
  form.dateRange = []
  form.eventTimeRange = []
  form.maxItemsPerAthlete = 3
  form.imageUrlInput = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑赛事'
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.type = row.type
  form.fee = row.fee
  form.dateRange = [row.date, row.end]
  form.eventTimeRange = [row.eventStartTime, row.eventEndTime]
  form.maxItemsPerAthlete = row.maxItemsPerAthlete || 3
  
  // Image URLs handling
  if (row.imageUrls && row.imageUrls.length > 0) {
      form.imageUrlInput = row.imageUrls.join('\n')
  } else {
      form.imageUrlInput = ''
  }
  
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该赛事吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteEvent(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const submitForm = async () => {
  if (!form.dateRange || form.dateRange.length !== 2 || !form.eventTimeRange || form.eventTimeRange.length !== 2) {
    ElMessage.error('请完整填写报名时间和比赛时间')
    return
  }
  const data = {
      name: form.name,
      type: form.type,
      fee: form.fee,
      registrationStartTime: form.dateRange[0],
      registrationEndTime: form.dateRange[1],
      eventStartTime: form.eventTimeRange[0],
      eventEndTime: form.eventTimeRange[1],
      maxItemsPerAthlete: form.maxItemsPerAthlete,
      addImage: form.imageUrlInput ? form.imageUrlInput.split('\n').filter(s => s.trim()) : []
  }

  try {
    let res
    if (isEdit.value) {
      res = await updateEvent(form.id, data)
    } else {
      res = await addEvent(data)
    }
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      getList()
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.message || '操作失败')
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.event-manage-container {
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
