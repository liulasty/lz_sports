<template>
  <div class="event-manage-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>赛事管理</span>
          <div class="filter-box">
            <el-input v-model="queryParams.name" placeholder="赛事名称" style="width: 200px; margin-right: 10px" @keyup.enter="handleQuery" />
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button type="success" @click="handleAdd">新增赛事</el-button>
          </div>
        </div>
      </template>

      <el-table :data="eventList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="赛事名称" />
        <el-table-column prop="type" label="参赛要求" />
        <el-table-column prop="fee" label="报名费(元)" />
        <el-table-column label="报名时间" width="300">
          <template #default="scope">
            {{ scope.row.date }} 至 {{ scope.row.end }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getEventList, addEvent, updateEvent, deleteEvent } from '@/api/event'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const eventList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
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
  imageUrlInput: ''
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getEventList(queryParams)
    if (res.code === 1) {
      eventList.value = res.data.records
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

const handleAdd = () => {
  dialogTitle.value = '新增赛事'
  isEdit.value = false
  form.id = ''
  form.name = ''
  form.type = ''
  form.fee = '0'
  form.dateRange = []
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
  // Backend returns date and end strings. 
  // Need to ensure format matches date picker or is compatible.
  // row.date is "YYYY-MM-DD HH:mm:ss" usually from VO.
  form.dateRange = [row.date, row.end]
  
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
      if (res.code === 1) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const submitForm = async () => {
  // Construct DTO
  const data = {
      name: form.name,
      type: form.type,
      fee: form.fee,
      date1: form.dateRange,
      addImage: form.imageUrlInput ? form.imageUrlInput.split('\n').filter(s => s.trim()) : []
  }

  try {
    let res
    if (isEdit.value) {
      res = await updateEvent(form.id, data)
    } else {
      res = await addEvent(data)
    }
    
    if (res.code === 1) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      getList()
    }
  } catch (error) {
    console.error(error)
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
</style>
