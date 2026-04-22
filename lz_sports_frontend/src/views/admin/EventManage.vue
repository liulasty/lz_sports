<template>
  <div class="event-manage-container">
    <el-card class="box-card main-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <div class="header-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
                <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              </svg>
            </div>
            <span class="header-title">赛事管理</span>
          </div>
          <div class="filter-box">
            <el-input 
              v-model="queryParams.name" 
              placeholder="请输入赛事名称搜索" 
              class="search-input"
              clearable
              @keyup.enter="handleQuery"
            >
              <template #prefix>
                <svg viewBox="0 0 24 24" fill="none" class="input-icon"><path d="M11 19C15.4183 19 19 15.4183 19 11C19 6.58172 15.4183 3 11 3C6.58172 3 3 6.58172 3 11C3 15.4183 6.58172 19 11 19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </template>
            </el-input>
            <el-button type="primary" class="action-btn" @click="handleQuery">查询</el-button>
            <el-button type="success" class="action-btn create-btn" @click="$router.push('/event-create')">
              <svg viewBox="0 0 24 24" fill="none" class="btn-icon"><path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              新建赛事
            </el-button>
          </div>
        </div>
      </template>

      <el-table 
        :data="eventList" 
        style="width: 100%" 
        v-loading="loading"
        class="styled-table"
        :header-cell-style="{ background: 'var(--el-fill-color-light)', color: 'var(--el-text-color-primary)' }"
      >
        <el-table-column prop="name" label="赛事名称" min-width="180">
          <template #default="scope">
            <div class="event-name-cell">
              <span class="event-name">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="参赛要求" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span class="text-secondary">{{ scope.row.type || '无特殊要求' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
             <el-tag 
               :type="getStatusType(scope.row.status)"
               class="status-tag"
               effect="light"
             >
               {{ getStatusLabel(scope.row.status) }}
             </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" width="280">
          <template #default="scope">
            <div class="time-cell">
              <div class="time-row">
                <span class="time-label">起：</span>
                <span class="time-value">{{ formatDate(scope.row.date) }}</span>
              </div>
              <div class="time-row">
                <span class="time-label">止：</span>
                <span class="time-value">{{ formatDate(scope.row.end) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="scope">
            <div class="action-cell">
              <el-button type="primary" link class="table-btn" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button v-if="isSuperAdmin" type="primary" link class="table-btn" @click="handleAssignAdmin(scope.row)">分配管理员</el-button>
              <el-button type="danger" link class="table-btn" @click="handleDelete(scope.row)">删除</el-button>
              
              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, scope.row)">
                <el-button type="info" link class="table-btn more-btn">
                  更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu class="action-dropdown">
                    <el-dropdown-item command="publish" v-if="scope.row.status === 'DRAFT'">
                      <span class="dropdown-item-text success-text">发布赛事</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="withdraw" v-if="scope.row.status === 'OPEN'">
                      <span class="dropdown-item-text warning-text">撤回赛事</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="finish" v-if="scope.row.status === 'ONGOING'">
                      <span class="dropdown-item-text info-text">结束赛事</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="export" divided>导出名单</el-dropdown-item>
                    <el-dropdown-item command="import">导入成绩</el-dropdown-item>
                    <el-dropdown-item command="publishScore">发布成绩</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
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
          class="custom-pagination"
        />
      </div>
    </el-card>

    <!-- Import Dialog -->
    <el-dialog v-model="importVisible" title="导入成绩" width="520px">
      <div class="import-dialog-body">
        <div class="import-mode-row">
          <span class="mode-label">导入模式</span>
          <el-radio-group v-model="importMode" size="small">
            <el-radio-button label="BEST_EFFORT">容错导入</el-radio-button>
            <el-radio-button label="STRICT">严格导入</el-radio-button>
          </el-radio-group>
        </div>
        <div class="import-mode-tip">
          <template v-if="importMode === 'STRICT'">
            <strong>严格导入：</strong>先全量校验，任意失败都不写入，请按失败明细修复后重试。
          </template>
          <template v-else>
            <strong>容错导入：</strong>成功行先写入，失败行返回明细，适合分批修复补导。
          </template>
        </div>
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
            点击或拖拽上传 Excel
          </div>
          <template #tip>
            <div class="el-upload__tip">
              请上传 .xlsx / .xls 文件
            </div>
          </template>
        </el-upload>
        <div v-if="importResult" class="import-result-card">
          <div class="result-mode-banner" :class="importResult.mode === 'STRICT' ? 'strict' : 'best-effort'">
            模式：{{ importResult.mode === 'STRICT' ? '严格导入' : '容错导入' }}
            <span v-if="importResult.allOrNothing">（全有或全无）</span>
          </div>
          <div class="result-row">
            <span class="result-success">成功 {{ importResult.successCount }} 条</span>
            <span v-if="importResult.failCount > 0" class="result-fail">失败 {{ importResult.failCount }} 条</span>
          </div>
          <div v-if="importResult.failCount > 0" class="result-guidance">
            <template v-if="importResult.mode === 'STRICT'">
              严格模式下本次未写入任何数据，请根据失败明细修复后再导入。
            </template>
            <template v-else>
              容错模式已写入成功数据，请根据失败明细修复后补导。
            </template>
          </div>
          <el-table
            v-if="importResult.failures && importResult.failures.length"
            :data="importResult.failures"
            border
            max-height="220"
            size="small"
          >
            <el-table-column prop="rowNumber" label="行号" width="80" />
            <el-table-column prop="reason" label="失败原因" />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" class="styled-dialog">
      <el-form :model="form" label-width="100px" class="dialog-form" :rules="rules" ref="formRef">
        <el-form-item label="赛事名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入赛事名称" />
        </el-form-item>
        <el-form-item label="参赛要求" prop="type">
          <el-input v-model="form.type" placeholder="例如：全校师生" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="报名费用" prop="fee" style="flex: 1">
            <el-input v-model="form.fee" type="number" placeholder="0" />
          </el-form-item>
          <el-form-item label="报名上限" prop="maxItemsPerAthlete" style="flex: 1">
            <el-input-number v-model="form.maxItemsPerAthlete" :min="1" :max="20" style="width: 100%" />
          </el-form-item>
        </div>
        <el-form-item label="报名时间" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="比赛时间" prop="eventTimeRange">
          <el-date-picker
            v-model="form.eventTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="图片链接">
           <el-input v-model="form.imageUrlInput" type="textarea" :rows="3" placeholder="请输入图片URL，每行一个（选填）" />
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
const importMode = ref('BEST_EFFORT')
const importResult = ref(null)
const dialogTitle = ref('新增赛事')
const isEdit = ref(false)

const queryParams = reactive({
  currentPage: 1,
  pageSize: 5,
  name: ''
})

const formRef = ref(null)

const rules = {
  name: [{ required: true, message: '请输入赛事名称', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择报名时间', trigger: 'change' }],
  eventTimeRange: [{ required: true, message: '请选择比赛时间', trigger: 'change' }]
}

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

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch (e) {
    return dateStr
  }
}

const handleCommand = (cmd, row) => {
  switch(cmd) {
    case 'publish': handleStatus(row, 'OPEN'); break;
    case 'withdraw': handleWithdraw(row); break;
    case 'finish': handleStatus(row, 'FINISHED'); break;
    case 'export': handleExport(row); break;
    case 'import': handleImportClick(row); break;
    case 'publishScore': handlePublishScore(row); break;
  }
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
  importResult.value = null
  importMode.value = 'BEST_EFFORT'
  importVisible.value = true
}

const uploadFile = async (param) => {
  const file = param.file
  try {
    const res = await importScores(currentEventId.value, file, importMode.value)
      if (res.code === 200) {
      importResult.value = res.data
      if (res.data?.failCount > 0 && res.data?.mode === 'STRICT') {
        ElMessage.warning('严格导入校验未通过，请先处理失败明细')
      } else {
        ElMessage.success('导入完成')
      }
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
  // Using explicit parsing rather than relying on v-model binding directly if strings are unformatted
  form.dateRange = [formatToBackendDate(row.date) || row.date, formatToBackendDate(row.end) || row.end]
  form.eventTimeRange = [formatToBackendDate(row.eventStartTime) || row.eventStartTime, formatToBackendDate(row.eventEndTime) || row.eventEndTime]
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

const formatToBackendDate = (dateStr) => {
  if (!dateStr) return null
  try {
    const d = new Date(dateStr)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
  } catch (e) {
    return dateStr
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (!form.dateRange || form.dateRange.length !== 2 || !form.eventTimeRange || form.eventTimeRange.length !== 2) {
      ElMessage.error('请完整填写报名时间和比赛时间')
      return
    }
    const data = {
        name: form.name,
        type: form.type,
        fee: form.fee,
        registrationStartTime: formatToBackendDate(form.dateRange[0]),
        registrationEndTime: formatToBackendDate(form.dateRange[1]),
        eventStartTime: formatToBackendDate(form.eventTimeRange[0]),
        eventEndTime: formatToBackendDate(form.eventTimeRange[1]),
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
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.event-manage-container {
  padding: 24px;
  min-height: 100vh;
}

/* ===================== Card & Header ===================== */
.main-card {
  border-radius: 16px !important;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06) !important;
  border: 1px solid var(--el-border-color-lighter) !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-light-3));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb, 64, 158, 255), 0.3);
}

.header-icon svg {
  width: 20px;
  height: 20px;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  letter-spacing: -0.3px;
}

/* ===================== Toolbar & Filter ===================== */
.filter-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 240px;
}
.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  padding-left: 16px;
  box-shadow: 0 0 0 1px var(--el-border-color-lighter) inset;
  background: var(--el-fill-color-lighter);
  transition: all 0.3s;
}
.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
  background: var(--el-bg-color);
}
.input-icon {
  width: 16px;
  height: 16px;
  color: var(--el-text-color-placeholder);
}

.action-btn {
  border-radius: 20px !important;
  padding: 8px 20px !important;
  height: 36px !important;
  font-weight: 600 !important;
  transition: all 0.3s !important;
}
.create-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, var(--el-color-success), #10b981) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3) !important;
}
.create-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4) !important;
}
.btn-icon {
  width: 16px;
  height: 16px;
}

/* ===================== Table Styles ===================== */
.styled-table {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}
.styled-table :deep(th.el-table__cell) {
  font-weight: 600;
  height: 54px;
}
.styled-table :deep(td.el-table__cell) {
  padding: 12px 0;
}

.event-name-cell {
  display: flex;
  align-items: center;
}
.event-name {
  font-weight: 600;
  color: var(--el-text-color-primary);
  font-size: 15px;
}

.text-secondary {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.status-tag {
  border-radius: 6px;
  padding: 4px 10px;
  height: auto;
  font-weight: 600;
  letter-spacing: 1px;
}

.time-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
}
.time-row {
  display: flex;
  align-items: center;
  background: var(--el-fill-color-lighter);
  padding: 4px 8px;
  border-radius: 6px;
  width: fit-content;
}
.time-label {
  color: var(--el-text-color-placeholder);
  margin-right: 4px;
}
.time-value {
  color: var(--el-text-color-regular);
  font-weight: 500;
}

/* ===================== Actions ===================== */
.action-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.table-btn {
  font-weight: 600 !important;
  padding: 6px 12px !important;
  height: 32px !important;
  border-radius: 6px !important;
  background: var(--el-fill-color-lighter) !important;
  transition: all 0.2s !important;
}
.table-btn:hover {
  background: var(--el-color-primary-light-9) !important;
  transform: translateY(-1px);
}
.table-btn.el-button--danger:hover {
  background: var(--el-color-danger-light-9) !important;
}
.more-btn {
  margin-left: 4px;
}

.action-dropdown .el-dropdown-menu__item {
  padding: 8px 20px;
  font-size: 13px;
}
.dropdown-item-text {
  font-weight: 600;
}
.success-text { color: var(--el-color-success); }
.warning-text { color: var(--el-color-warning); }
.info-text { color: var(--el-color-info); }

/* ===================== Pagination ===================== */
.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}
.custom-pagination :deep(.el-pagination__total),
.custom-pagination :deep(.el-pagination__sizes) {
  color: var(--el-text-color-secondary);
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
/* ===================== Dialog Styles ===================== */
.styled-dialog {
  border-radius: 12px;
  overflow: hidden;
}
.styled-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.styled-dialog :deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 16px;
}
.styled-dialog :deep(.el-dialog__body) {
  padding: 24px 32px;
}
.styled-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.dialog-form {
  margin-top: 8px;
}
.form-row {
  display: flex;
  gap: 16px;
}
.form-row .el-form-item {
  margin-bottom: 18px;
}

.import-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.import-mode-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.mode-label {
  font-size: 13px;
  font-weight: 600;
}

.import-mode-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}

.import-result-card {
  margin-top: 8px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  padding: 10px;
  background: var(--el-fill-color-lighter);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-mode-banner {
  display: inline-flex;
  width: fit-content;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 999px;
}

.result-mode-banner.strict {
  color: #c2410c;
  background: #fff7ed;
  border: 1px solid #fed7aa;
}

.result-mode-banner.best-effort {
  color: #0369a1;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
}

.result-row {
  display: flex;
  gap: 12px;
  font-size: 13px;
}

.result-success {
  color: var(--el-color-success);
  font-weight: 600;
}

.result-fail {
  color: var(--el-color-danger);
  font-weight: 600;
}

.result-guidance {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}
</style>
