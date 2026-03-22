<template>
  <div class="user-audit-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>用户审核</span>
          <div class="filter-box">
            <el-input v-model="queryParams.name" placeholder="用户名/邮箱" style="width: 200px; margin-right: 10px" @keyup.enter="handleQuery" />
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="userList" style="width: 100%" v-loading="loading">
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="name" label="姓名">
          <template #default="scope">
            {{ scope.row.name ?? '未填写' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="registerTime" label="注册时间" width="180">
           <template #default="scope">
             {{ formatTime(scope.row.registerTime) }}
           </template>
        </el-table-column>
        <el-table-column prop="state" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.state)">{{ scope.row.state === 'PENDING' ? '待审核' : (scope.row.state === 'ACTIVE' ? '已激活' : '已拒绝') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <div v-if="scope.row.state === 'PENDING'">
              <el-button type="success" size="small" @click="handleApprove(scope.row)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(scope.row)">拒绝</el-button>
            </div>
            <span v-else>已处理</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUserList, auditUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { isSuccess } from '@/utils/result'

interface UserData {
  id: number;
  userId?: number; // Depending on actual API
  username: string;
  name: string;
  email: string;
  avatar: string;
  registerTime: string;
  state: string;
}

const loading = ref(false)
const userList = ref<UserData[]>([])
const total = ref(0)

const queryParams = reactive({
  currentPage: 1,
  pageSize: 10,
  name: ''
})

const getStatusType = (state: string) => {
  if (state === 'ACTIVE') return 'success'
  if (state === 'PENDING') return 'warning'
  if (state === 'REJECTED') return 'danger'
  return 'info'
}

const formatTime = (time: string | number | Date) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    if (isSuccess(res)) {
      userList.value = res.data.rows
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

const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  getList()
}

const handleCurrentChange = (val: number) => {
  queryParams.currentPage = val
  getList()
}

const handleApprove = (row: UserData) => {
  const displayName = row.name || row.username
  ElMessageBox.confirm(`确认通过用户 ${displayName} 的注册申请吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await auditUser(row.userId, 1, '') // 1 for active
      if (isSuccess(res)) {
        ElMessage.success('操作成功')
        getList()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const handleReject = (row: UserData) => {
  ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S/,
    inputErrorMessage: '原因不能为空'
  }).then(async ({ value }) => {
    try {
      const res = await auditUser(row.userId, 0, value) // 0 for rejected
      if (isSuccess(res)) {
        ElMessage.success('操作成功')
        getList()
      } else {
        ElMessage.error(res.msg || '操作失败')
      }
    } catch (error) {
      console.error(error)
    }
  })
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
