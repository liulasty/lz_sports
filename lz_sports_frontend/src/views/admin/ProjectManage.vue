<template>
  <div class="project-manage-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>项目管理</span>
          <div class="filter-box">
            <el-input v-model="queryParams.name" placeholder="项目名称" style="width: 200px; margin-right: 10px" @keyup.enter="handleQuery" />
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button type="success" @click="handleAdd">新增项目</el-button>
          </div>
        </div>
      </template>

      <el-table :data="projectList" style="width: 100%" v-loading="loading">
        <el-table-column prop="itemName" label="项目名称" />
        <el-table-column prop="eventName" label="所属赛事" />
        <el-table-column prop="startTime" label="开始时间" width="180">
           <template #default="scope">
             {{ formatDate(scope.row.startTime) }}
           </template>
        </el-table-column>
        <el-table-column prop="grade" label="地点" />
        <el-table-column prop="limitation" label="限制" />
        <el-table-column label="报名人数" width="120">
           <template #default="scope">
             {{ scope.row.attendance }} / {{ scope.row.maxAttendance }}
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
        <el-form-item label="项目名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="所属赛事">
           <el-select v-model="form.event" placeholder="请选择赛事" filterable>
              <el-option
                 v-for="item in eventTypes"
                 :key="item.EventID || item.eventId"
                 :label="item.EventName || item.eventName"
                 :value="item.EventID || item.eventId"
              />
           </el-select>
        </el-form-item>
        <el-form-item label="比赛时间">
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item label="比赛地点">
          <el-input v-model="form.grade" placeholder="输入地点" />
        </el-form-item>
        <el-form-item label="限制要求">
          <el-select v-model="form.limitation" placeholder="性别限制">
            <el-option label="不限" value="ALL" />
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目类别">
          <el-select v-model="form.category" placeholder="项目类别">
            <el-option label="自定义" value="CUSTOM" />
            <el-option label="标准项目" value="STANDARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大人数">
          <el-input-number v-model="form.maxAttendance" :min="1" />
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
import { getProjectList, addProject, updateProject, deleteProject } from '@/api/project'
import { getEventTypes } from '@/api/event'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const projectList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增项目')
const isEdit = ref(false)
const eventTypes = ref([])

const queryParams = reactive({
  currentPage: 1,
  pageSize: 5,
  name: ''
})

const form = reactive({
  id: '', // itemId
  name: '',
  event: '', // eventId
  grade: '',
  limitation: '',
  category: 'CUSTOM',
  maxAttendance: 50,
  dateRange: [],
  imageUrlInput: ''
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getProjectList(queryParams)
    if (res.code === 200) {
      projectList.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getEventOptions = async () => {
  try {
    const res = await getEventTypes()
    if (res.code === 200) {
      // Backend returns List<Map<Long, String>>. 
      // Need to handle potential key variations (EventID vs eventId).
      eventTypes.value = res.data
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

const handleAdd = () => {
  dialogTitle.value = '新增项目'
  isEdit.value = false
  form.id = ''
  form.name = ''
  form.event = ''
  form.grade = ''
  form.limitation = ''
  form.category = 'CUSTOM'
  form.maxAttendance = 50
  form.dateRange = []
  form.imageUrlInput = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑项目'
  isEdit.value = true
  form.id = row.id
  form.name = row.itemName
  form.event = row.eventId
  form.grade = row.grade
  form.limitation = row.limitation
  form.category = row.category || 'CUSTOM'
  form.maxAttendance = row.maxAttendance
  form.dateRange = [row.startTime, row.endTime]
  
  // Image handling omitted for simplicity or if row doesn't have image list readily available
  // Assuming row might have it or we fetch detail. 
  // ProjectVO usually doesn't have image list in `list` API.
  // For edit, we might need `getDetail` API call if image list is needed.
  // For now, leave image input empty on edit unless critical.
  form.imageUrlInput = ''
  
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该项目吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteProject(row.id)
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
  const data = {
      name: form.name,
      event: form.event,
      grade: form.grade,
      limitation: form.limitation,
      category: form.category,
      maxAttendance: form.maxAttendance,
      date: form.dateRange,
      addImage: form.imageUrlInput ? form.imageUrlInput.split('\n').filter(s => s.trim()) : []
  }

  try {
    let res
    if (isEdit.value) {
      res = await updateProject(form.id, data)
    } else {
      res = await addProject(data)
    }
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      getList()
    }
  } catch (error) {
    console.error(error)
  }
}

const formatDate = (dateStr) => {
    if (!dateStr) return ''
    return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  getList()
  getEventOptions()
})
</script>

<style scoped>
.project-manage-container {
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
