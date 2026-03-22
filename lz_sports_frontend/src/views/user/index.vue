<template>
  <div class="user-manage-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="filter-box">
            <el-select v-model="queryParams.role" placeholder="角色" clearable style="width: 120px; margin-right: 10px">
              <el-option label="普通用户" value="USER" />
              <el-option label="运动员" value="ATHLETE" />
              <el-option label="赛事管理员" value="EVENT_ADMIN" />
              <el-option label="系统管理员" value="SCHOOL_ADMIN" />
            </el-select>
            <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 100px; margin-right: 10px">
              <el-option label="正常" value="ACTIVE" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
            <el-input v-model="queryParams.keyword" placeholder="邮箱/姓名" clearable style="width: 200px; margin-right: 10px" @keyup.enter="handleQuery" />
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="userList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="用户名" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="type" label="角色">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.type)">{{ formatRole(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="state" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.state === 'ACTIVE' || scope.row.state === '已激活' ? 'success' : 'danger'">
              {{ scope.row.state === 'ACTIVE' || scope.row.state === '已激活' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.registerTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
                type="primary" 
                link 
                @click="handleEditRole(scope.row)"
                :disabled="scope.row.type === 'SUPER_ADMIN'"
            >
              修改角色
            </el-button>
            <el-button 
                :type="scope.row.state === 'ACTIVE' || scope.row.state === '已激活' ? 'danger' : 'success'" 
                link 
                @click="handleToggleStatus(scope.row)"
                :disabled="scope.row.type === 'SUPER_ADMIN'"
            >
              {{ scope.row.state === 'ACTIVE' || scope.row.state === '已激活' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="修改角色" width="400px">
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="角色分配">
          <el-select v-model="roleForm.role" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="赛事管理员" value="EVENT_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRoleForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAdminUserList, changeUserRole, disableUser, enableUser } from '@/api/adminUser'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const userList = ref([])
const total = ref(0)
const dialogVisible = ref(false)

const queryParams = reactive({
  page: 1,
  size: 20,
  keyword: '',
  role: '',
  status: ''
})

const roleForm = reactive({
  id: null,
  role: ''
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getAdminUserList(queryParams)
    if (res.code === 200) {
      userList.value = res.data.records || res.data.rows || []
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.page = 1
  getList()
}

const handleSizeChange = (val) => {
  queryParams.size = val
  getList()
}

const handleCurrentChange = (val) => {
  queryParams.page = val
  getList()
}

const handleEditRole = (row) => {
  roleForm.id = row.id || row.userId
  roleForm.role = row.type
  dialogVisible.value = true
}

const submitRoleForm = async () => {
  if (!roleForm.role) {
    ElMessage.warning('请选择角色')
    return
  }
  submitLoading.value = true
  try {
    const res = await changeUserRole(roleForm.id, roleForm.role)
    if (res.code === 200) {
      ElMessage.success('角色修改成功')
      dialogVisible.value = false
      getList()
    }
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = (row) => {
  const isActive = row.state === 'ACTIVE' || row.state === '已激活'
  const actionText = isActive ? '禁用' : '启用'
  const targetId = row.id || row.userId
  
  ElMessageBox.confirm(`确认${actionText}该用户吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: isActive ? 'warning' : 'success'
  }).then(async () => {
    try {
      const res = isActive ? await disableUser(targetId) : await enableUser(targetId)
      if (res.code === 200) {
        ElMessage.success(`${actionText}成功`)
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

const formatRole = (role) => {
  const map = {
    'SUPER_ADMIN': '超级管理员',
    'SCHOOL_ADMIN': '系统管理员',
    'EVENT_ADMIN': '赛事管理员',
    'ATHLETE': '运动员',
    'USER': '普通用户'
  }
  return map[role] || role
}

const getRoleType = (role) => {
  if (role === 'SUPER_ADMIN' || role === 'SCHOOL_ADMIN') return 'danger'
  if (role === 'EVENT_ADMIN') return 'warning'
  if (role === 'ATHLETE') return 'success'
  return 'info'
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
.filter-box {
  display: flex;
  align-items: center;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
