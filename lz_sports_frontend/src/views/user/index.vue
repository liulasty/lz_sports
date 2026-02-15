<template>
  <div class="user-manage-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="filter-box">
            <el-input v-model="queryParams.name" placeholder="用户名" style="width: 200px; margin-right: 10px" @keyup.enter="handleQuery" />
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="userList" style="width: 100%" v-loading="loading">
        <el-table-column label="头像" width="80">
          <template #default="scope">
             <el-avatar :size="40" :src="scope.row.avatarSrc || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="userType" label="角色">
           <template #default="scope">
              <el-tag :type="getRoleType(scope.row.userType)">{{ scope.row.userType }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
           <template #default="scope">
              <el-tag :type="scope.row.status === '已激活' ? 'success' : 'info'">{{ scope.row.status }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间">
          <template #default="scope">
            {{ formatDate(scope.row.registerTime) }}
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
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog v-model="dialogVisible" title="编辑用户" width="30%">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.userName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="角色">
           <el-select v-model="form.userType">
              <el-option label="普通用户" value="普通用户" />
              <el-option label="运动员" value="运动员" />
              <el-option label="管理员" value="管理员" />
              <el-option label="学生" value="学生" />
           </el-select>
        </el-form-item>
        <el-form-item label="状态">
           <el-select v-model="form.status">
              <el-option label="已激活" value="已激活" />
              <el-option label="未激活" value="未激活" />
           </el-select>
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
import { getUserList, updateUser, deleteUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const dialogVisible = ref(false)

const queryParams = reactive({
  currentPage: 1,
  pageSize: 10,
  name: ''
})

const form = reactive({
  userId: '',
  userName: '',
  email: '',
  userType: '',
  status: ''
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    if (res.code === 1) {
      userList.value = res.data.records
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

const handleEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const submitForm = async () => {
  try {
    const res = await updateUser(form)
    if (res.code === 1) {
      ElMessage.success('更新成功')
      dialogVisible.value = false
      getList()
    }
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该用户吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteUser(row.userId)
      if (res.code === 1) {
        ElMessage.success('删除成功')
        getList()
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

const getRoleType = (role) => {
  if (role === '管理员') return 'danger'
  if (role === '运动员') return 'warning'
  return ''
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.user-manage-container {
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
