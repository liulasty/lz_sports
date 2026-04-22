<template>
  <div class="user-page">
    <div class="user-hero">
      <div>
        <p class="user-eyebrow">USER CONSOLE</p>
        <h1 class="user-title">用户管理</h1>
        <p class="user-sub">筛选用户、调整角色与启用状态。超级管理员账号不可被修改。</p>
      </div>
      <div class="hero-actions lz-actions lz-ep-dark">
        <el-button @click="resetQuery">重置筛选</el-button>
        <el-button type="primary" @click="handleQuery">查询</el-button>
      </div>
    </div>

    <section class="user-stats">
      <article class="stat-card">
        <p class="stat-label">总用户</p>
        <p class="stat-value">{{ total }}</p>
      </article>
      <article class="stat-card ok">
        <p class="stat-label">本页正常</p>
        <p class="stat-value">{{ pageActiveCount }}</p>
      </article>
      <article class="stat-card danger">
        <p class="stat-label">本页禁用</p>
        <p class="stat-value">{{ pageDisabledCount }}</p>
      </article>
      <article class="stat-card soft">
        <p class="stat-label">本页管理员</p>
        <p class="stat-value">{{ pageAdminCount }}</p>
      </article>
    </section>

    <section class="user-shell lz-surface lz-ep-dark" v-loading="loading">
      <div class="toolbar">
        <div class="filters lz-form">
          <SmartSelect v-model="queryParams.role" :options="roleOptions" placeholder="角色" clearable class="w-140" />
          <SmartSelect v-model="queryParams.status" :options="statusOptions" placeholder="状态" clearable class="w-120" />
          <el-input
            v-model="queryParams.keyword"
            placeholder="邮箱 / 姓名"
            clearable
            class="w-240"
            @keyup.enter="handleQuery"
          />
        </div>
        <div class="toolbar-meta">第 {{ queryParams.page }} 页</div>
      </div>

      <el-table :data="userList" class="user-table" style="width: 100%">
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="name" label="姓名" min-width="120">
          <template #default="scope">
            {{ scope.row.name ?? '未填写' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="type" label="角色" min-width="120">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.type)">{{ formatRole(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="state" label="状态" min-width="110">
          <template #default="scope">
            <el-tag :type="isActiveState(scope.row.state) ? 'success' : 'danger'">
              {{ isActiveState(scope.row.state) ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" min-width="180">
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
              :type="isActiveState(scope.row.state) ? 'danger' : 'success'"
              link
              @click="handleToggleStatus(scope.row)"
              :disabled="scope.row.type === 'SUPER_ADMIN'"
            >
              {{ isActiveState(scope.row.state) ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container lz-actions">
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
    </section>

    <el-dialog v-model="dialogVisible" title="修改角色" width="420px" class="lz-ep-dark">
      <el-form :model="roleForm" label-position="top" class="lz-form">
        <el-form-item label="角色分配">
          <SmartSelect v-model="roleForm.role" :options="assignableRoleOptions" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer lz-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRoleForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getAdminUserList, changeUserRole, disableUser, enableUser } from '@/api/adminUser'
import { ElMessage, ElMessageBox } from 'element-plus'
import SmartSelect from '@/components/SmartSelect.vue'

interface UserData {
  id: number;
  userId?: number;
  username: string;
  name: string;
  email: string;
  avatar: string;
  type: string;
  state: string;
  registerTime: string;
}

const loading = ref(false)
const submitLoading = ref(false)
const userList = ref<UserData[]>([])
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
  id: null as number | null,
  role: ''
})

const roleOptions = [
  { label: '普通用户', value: 'USER' },
  { label: '运动员', value: 'ATHLETE' },
  { label: '赛事管理员', value: 'EVENT_ADMIN' },
  { label: '系统管理员', value: 'SCHOOL_ADMIN' }
]

const statusOptions = [
  { label: '正常', value: 'ACTIVE' },
  { label: '禁用', value: 'DISABLED' }
]

const assignableRoleOptions = [
  { label: '普通用户', value: 'USER' },
  { label: '赛事管理员', value: 'EVENT_ADMIN' }
]

const isActiveState = (state: string) => state === 'ACTIVE' || state === '已激活'

const pageActiveCount = computed(() => (userList.value || []).filter(u => isActiveState(u.state)).length)
const pageDisabledCount = computed(() => (userList.value || []).filter(u => !isActiveState(u.state)).length)
const pageAdminCount = computed(() => (userList.value || []).filter(u => ['SUPER_ADMIN', 'SCHOOL_ADMIN', 'EVENT_ADMIN'].includes(u.type)).length)

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

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.role = ''
  queryParams.status = ''
  queryParams.page = 1
  getList()
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  getList()
}

const handleCurrentChange = (val: number) => {
  queryParams.page = val
  getList()
}

const handleEditRole = (row: UserData) => {
  roleForm.id = row.id || row.userId || null
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

const handleToggleStatus = (row: UserData) => {
  const isActive = isActiveState(row.state)
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

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString()
}

const formatRole = (role: string) => {
  const map: Record<string, string> = {
    'SUPER_ADMIN': '超级管理员',
    'SCHOOL_ADMIN': '系统管理员',
    'EVENT_ADMIN': '赛事管理员',
    'ATHLETE': '运动员',
    'USER': '普通用户'
  }
  return map[role] || role
}

const getRoleType = (role: string) => {
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
.user-page {
  padding: 24px;
}

.user-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.user-eyebrow {
  margin: 0 0 10px;
  color: var(--accent);
  letter-spacing: 0.18em;
  font-size: 11px;
  font-weight: 700;
}

.user-title {
  margin: 0 0 8px;
  font-size: 32px;
  color: var(--text-primary);
}

.user-sub {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.hero-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.user-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  background: color-mix(in srgb, var(--bg-card) 92%, var(--bg-soft));
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.12);
}

.stat-card.ok { border-color: color-mix(in srgb, #4fb77a 35%, var(--border)); }
.stat-card.danger { border-color: color-mix(in srgb, #f43f5e 35%, var(--border)); }
.stat-card.soft { border-color: color-mix(in srgb, var(--accent) 25%, var(--border)); }

.stat-label {
  margin: 0 0 8px;
  color: color-mix(in srgb, var(--text-secondary) 92%, transparent);
  font-size: 12px;
}

.stat-value {
  margin: 0;
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 800;
}

.user-shell {
  padding: 14px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-meta {
  color: color-mix(in srgb, var(--text-secondary) 90%, transparent);
  font-size: 12px;
}

.w-120 { width: 120px; }
.w-140 { width: 140px; }
.w-240 { width: 240px; }

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .user-hero { flex-direction: column; align-items: flex-start; }
  .user-stats { grid-template-columns: 1fr 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .w-120, .w-140, .w-240 { width: 100%; }
  .pagination-container { justify-content: center; }
}
</style>

