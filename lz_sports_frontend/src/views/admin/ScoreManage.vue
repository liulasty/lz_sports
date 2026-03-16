<template>
  <div class="score-manage-container">
    <el-card>
      <template #header>
        <div class="header">
          <span>成绩管理</span>
          <div class="actions">
            <el-select v-model="query.eventId" placeholder="选择赛事" style="width: 180px" @change="handleEventChange">
              <el-option v-for="item in eventOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <el-select v-model="query.itemId" placeholder="选择项目" clearable style="width: 180px">
              <el-option v-for="item in itemOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <el-button type="primary" @click="loadScores">查询</el-button>
            <el-button @click="downloadTemplate">下载模板</el-button>
            <el-button @click="exportScores">导出成绩</el-button>
            <el-button type="success" @click="showImportDialog = true">导入成绩</el-button>
            <el-button type="warning" @click="publishWithConfirm">发布成绩</el-button>
          </div>
        </div>
      </template>

      <el-table :data="scores" v-loading="loading">
        <el-table-column prop="eventName" label="赛事" />
        <el-table-column prop="itemName" label="项目" />
        <el-table-column prop="athleteName" label="姓名" />
        <el-table-column prop="scoreValue" label="成绩" />
        <el-table-column prop="scoreRank" label="排名" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.isPublished ? 'success' : 'info'">
              {{ scope.row.isPublished ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showImportDialog" title="Excel导入成绩" width="460px">
      <el-upload drag action="#" :http-request="handleUpload" :show-file-list="false" accept=".xlsx,.xls">
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div>点击或拖拽上传文件</div>
      </el-upload>
      <el-alert
        v-if="importResult"
        class="import-result"
        type="info"
        :title="`成功${importResult.successCount}条，失败${importResult.failCount}条`"
        show-icon
      />
      <el-table v-if="importResult && importResult.failures.length" :data="importResult.failures" max-height="240">
        <el-table-column prop="rowNumber" label="行号" width="80" />
        <el-table-column prop="reason" label="失败原因" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getEventList } from '@/api/event'
import { getProjectsByEventId } from '@/api/project'
import { downloadScoreTemplate, exportScore, getScorePage, importScores, publishScores } from '@/api/score'

const loading = ref(false)
const showImportDialog = ref(false)
const importResult = ref(null)
const eventOptions = ref([])
const itemOptions = ref([])
const scores = ref([])
const query = reactive({
  eventId: null,
  itemId: null
})

const loadEvents = async () => {
  const res = await getEventList({ currentPage: 1, pageSize: 200 })
  if (res.code === 200) {
    eventOptions.value = res.data.records || []
    if (!query.eventId && eventOptions.value.length) {
      query.eventId = eventOptions.value[0].id
      await handleEventChange(query.eventId)
    }
  }
}

const handleEventChange = async (eventId) => {
  query.itemId = null
  const res = await getProjectsByEventId(eventId)
  if (res.code === 200) {
    itemOptions.value = res.data || []
  }
  loadScores()
}

const loadScores = async () => {
  if (!query.eventId) {
    return
  }
  loading.value = true
  try {
    const res = await getScorePage({
      currentPage: 1,
      pageSize: 500,
      eventId: query.eventId,
      itemId: query.itemId
    })
    if (res.code === 200) {
      scores.value = res.data.records || []
    }
  } finally {
    loading.value = false
  }
}

const downloadBlob = (data, filename) => {
  const url = window.URL.createObjectURL(new Blob([data]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const downloadTemplate = async () => {
  if (!query.eventId) return
  const res = await downloadScoreTemplate(query.eventId)
  downloadBlob(res, '成绩导入模板.xlsx')
}

const exportScores = async () => {
  if (!query.eventId) return
  const res = await exportScore(query.eventId)
  downloadBlob(res, '成绩表.xlsx')
}

const handleUpload = async (options) => {
  if (!query.eventId) {
    ElMessage.error('请先选择赛事')
    return
  }
  const res = await importScores(query.eventId, options.file)
  if (res.code === 200) {
    importResult.value = res.data
    ElMessage.success('导入完成')
    loadScores()
  }
}

const publishWithConfirm = async () => {
  if (!query.eventId) return
  await ElMessageBox.confirm('发布后成绩不可撤回，是否继续？', '二次确认', {
    type: 'warning',
    confirmButtonText: '确认发布',
    cancelButtonText: '取消'
  })
  const res = await publishScores(query.eventId)
  if (res.code === 200) {
    ElMessage.success('发布成功')
    loadScores()
  }
}

onMounted(() => {
  loadEvents()
})
</script>

<style scoped>
.score-manage-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.import-result {
  margin: 12px 0;
}
</style>
