<template>
  <div class="audit-page">
    <div class="audit-hero">
      <div>
        <p class="audit-eyebrow">AUDIT DESK</p>
        <h1 class="audit-title">用户审核</h1>
        <p class="audit-sub">集中处理待审核用户。支持按用户名/邮箱搜索。</p>
      </div>
      <div class="hero-actions lz-actions lz-ep-dark">
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="primary" @click="handleQuery">查询</el-button>
      </div>
    </div>

    <section class="audit-stats">
      <article class="stat-card pending">
        <p class="stat-label">本页待审核</p>
        <p class="stat-value">{{ pagePendingCount }}</p>
      </article>
      <article class="stat-card ok">
        <p class="stat-label">本页已激活</p>
        <p class="stat-value">{{ pageActiveCount }}</p>
      </article>
      <article class="stat-card danger">
        <p class="stat-label">本页已拒绝</p>
        <p class="stat-value">{{ pageRejectedCount }}</p>
      </article>
      <article class="stat-card">
        <p class="stat-label">总记录</p>
        <p class="stat-value">{{ total }}</p>
      </article>
    </section>

    <section class="audit-shell lz-surface lz-ep-dark" v-loading="loading">
      <div class="toolbar">
        <div class="filters lz-form">
          <el-input
            v-model="queryParams.name"
            placeholder="用户名 / 邮箱"
            clearable
            class="w-260"
            @keyup.enter="handleQuery"
          />
        </div>
        <div class="toolbar-meta">第 {{ queryParams.currentPage }} 页</div>
      </div>

      <el-table :data="userList" class="audit-table" style="width: 100%">
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
        <el-table-column prop="registerTime" label="注册时间" min-width="180">
          <template #default="scope">
            {{ formatTime(scope.row.registerTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="state" label="状态" min-width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.state)">
              {{ scope.row.state === 'PENDING' ? '待审核' : (scope.row.state === 'ACTIVE' ? '已激活' : '已拒绝') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <div v-if="scope.row.state === 'PENDING'" class="lz-actions">
              <el-button type="success" @click="handleApprove(scope.row)">通过</el-button>
              <el-button type="danger" @click="handleReject(scope.row)">拒绝</el-button>
            </div>
            <span v-else style="color: var(--text-secondary);">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container lz-actions">
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
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getUserList, auditUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { isSuccess } from '@/utils/result'

interface UserData {
  id: number;
  userId?: number;
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

const pagePendingCount = computed(() => (userList.value || []).filter(u => u.state === 'PENDING').length)
const pageActiveCount = computed(() => (userList.value || []).filter(u => u.state === 'ACTIVE').length)
const pageRejectedCount = computed(() => (userList.value || []).filter(u => u.state === 'REJECTED').length)

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

const resetQuery = () => {
  queryParams.name = ''
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
      const res = await auditUser(row.userId, 1, '')
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
      const res = await auditUser(row.userId, 0, value)
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
.audit-page {
  padding: 24px;
}

.audit-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.audit-eyebrow {
  margin: 0 0 10px;
  color: var(--accent);
  letter-spacing: 0.18em;
  font-size: 11px;
  font-weight: 700;
}

.audit-title {
  margin: 0 0 8px;
  font-size: 32px;
  color: var(--text-primary);
}

.audit-sub {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.hero-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.audit-stats {
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

.stat-card.pending { border-color: color-mix(in srgb, var(--accent) 35%, var(--border)); }
.stat-card.ok { border-color: color-mix(in srgb, #4fb77a 35%, var(--border)); }
.stat-card.danger { border-color: color-mix(in srgb, #f43f5e 35%, var(--border)); }

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

.audit-shell {
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

.w-260 { width: 260px; }

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .audit-hero { flex-direction: column; align-items: flex-start; }
  .audit-stats { grid-template-columns: 1fr 1fr; }
  .toolbar { flex-direction: column; align-items: stretch; }
  .w-260 { width: 100%; }
  .pagination-container { justify-content: center; }
}
</style>

