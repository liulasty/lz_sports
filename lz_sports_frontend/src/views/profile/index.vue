<template>
  <div class="profile-container">
    <div class="profile-grid">

      <!-- ── Left: User Info Card ── -->
      <div class="info-card">
        <!-- Avatar section -->
        <div class="avatar-section">
          <div class="avatar-ring">
            <el-avatar
                :size="90"
                :src="userInfo.avatarSrc || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
            />
          </div>
          <h3 class="username">{{ userInfo.userName || '—' }}</h3>
          <div class="role-badge">
            <span class="role-dot"></span>
            {{ userInfo.userType || '用户' }}
          </div>
        </div>

        <!-- Divider -->
        <div class="card-divider"></div>

        <!-- Info rows -->
        <div class="info-list">
          <div class="info-row">
            <div class="info-label">
              <svg class="info-icon" viewBox="0 0 20 20" fill="currentColor"><path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/><path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/></svg>
              邮箱
            </div>
            <span class="info-value">{{ userInfo.email || '—' }}</span>
          </div>

          <div class="info-row">
            <div class="info-label">
              <svg class="info-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 2a1 1 0 00-1 1v1a1 1 0 002 0V3a1 1 0 00-1-1zM4 4h3a3 3 0 000 6H4a3 3 0 000-6zm0 8a3 3 0 000 6h3a3 3 0 000-6H4zm10-8a3 3 0 000 6h3a3 3 0 000-6h-3zm0 8a3 3 0 000 6h3a3 3 0 000-6h-3z" clip-rule="evenodd"/></svg>
              角色
            </div>
            <el-tag size="small" class="custom-tag">{{ userInfo.userType || '—' }}</el-tag>
          </div>

          <div class="info-row">
            <div class="info-label">
              <svg class="info-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/></svg>
              状态
            </div>
            <el-tag
                size="small"
                :type="userInfo.status === '已激活' ? 'success' : 'warning'"
                class="custom-tag"
            >{{ userInfo.status || '—' }}</el-tag>
          </div>

          <div class="info-row">
            <div class="info-label">
              <svg class="info-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/></svg>
              注册时间
            </div>
            <span class="info-value small">{{ formatDate(userInfo.registerTime) || '—' }}</span>
          </div>
        </div>
      </div>

      <!-- ── Right: Main Panel ── -->
      <div class="main-card">
        <el-tabs v-model="activeTab" class="profile-tabs" style="padding: 20px;">
          <el-tab-pane label="运动员认证" name="apply" v-if="!isAdmin">
            <div class="certification-section">
              <!-- 已申请的赛事列表 -->
              <div v-if="myApplications.length > 0" class="applications-list" style="margin-bottom: 30px;">
                <h4 style="margin-bottom: 16px; font-size: 15px; color: var(--text-primary, #fff); font-weight: 600;">已申请的赛事</h4>
                <el-table :data="myApplications" class="custom-table" :header-cell-style="headerCellStyle" :row-style="rowStyle">
                  <el-table-column prop="eventName" label="赛事名称" min-width="150" />
                  <el-table-column label="申请时间" min-width="160">
                    <template #default="scope">{{ formatDate(scope.row.applyTime) }}</template>
                  </el-table-column>
                  <el-table-column label="状态" width="100" align="center">
                    <template #default="scope">
                      <span class="status-pill" :class="getApplyPillClass(scope.row.athleteState)">
                        {{ normalizeAthleteStatus(scope.row.athleteState) === 'APPROVED' ? '审核通过' : (normalizeAthleteStatus(scope.row.athleteState) === 'REJECTED' ? '已拒绝' : '审核中') }}
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="140" align="center">
                    <template #default="scope">
                      <el-button type="primary" link size="small" @click="handleUpdateApplication(scope.row)">修改</el-button>
                      <el-button type="danger" link size="small" @click="handleCancelApplication(scope.row.id)">取消</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <!-- Apply form -->
              <div v-if="availableEvents.length > 0">
                <h4 style="margin-bottom: 16px; font-size: 15px; color: var(--text-primary, #fff); font-weight: 600;">申请新赛事资格</h4>
                <el-form
                    ref="applyFormRef"
                    :model="applyForm"
                    :rules="rules"
                    label-width="0"
                    class="apply-form"
                >
                  <div class="form-grid">
                    <div class="form-field full-width">
                      <div class="field-label">申请赛事 <span class="required">*</span></div>
                      <el-form-item prop="eventId">
                        <el-select v-model="applyForm.eventId" placeholder="请选择要报名的赛事" class="custom-input" style="width: 100%">
                          <el-option v-for="event in availableEvents" :key="event.id" :label="event.name" :value="event.id" />
                        </el-select>
                      </el-form-item>
                    </div>

                    <div class="form-field">
                      <div class="field-label">姓名 <span class="required">*</span></div>
                      <el-form-item prop="name">
                        <el-input v-model="applyForm.name" placeholder="请输入真实姓名" class="custom-input" />
                      </el-form-item>
                    </div>

                    <div class="form-field">
                      <div class="field-label">年龄 <span class="required">*</span></div>
                      <el-form-item prop="age">
                        <el-input-number v-model="applyForm.age" :min="1" :max="100" class="custom-number" controls-position="right" />
                      </el-form-item>
                    </div>

                    <div class="form-field">
                      <div class="field-label">性别 <span class="required">*</span></div>
                      <el-form-item prop="gender">
                        <div class="gender-toggle">
                          <button
                              class="gender-btn"
                              :class="{ active: applyForm.gender === '男' }"
                              type="button"
                              @click="applyForm.gender = '男'"
                          >♂ 男</button>
                          <button
                              class="gender-btn"
                              :class="{ active: applyForm.gender === '女' }"
                              type="button"
                              @click="applyForm.gender = '女'"
                          >♀ 女</button>
                        </div>
                      </el-form-item>
                    </div>

                    <div class="form-field">
                      <div class="field-label">联系方式 <span class="required">*</span></div>
                      <el-form-item prop="phone">
                        <el-input v-model="applyForm.phone" placeholder="请输入手机号码" class="custom-input" />
                      </el-form-item>
                    </div>

                    <div class="form-field full-width">
                      <div class="field-label">年级 / 班级 <span class="required">*</span></div>
                      <el-form-item prop="grade">
                        <el-input v-model="applyForm.grade" placeholder="例：高三 2 班" class="custom-input" />
                      </el-form-item>
                    </div>
                  </div>

                  <div class="form-actions">
                    <button class="submit-btn" type="button" @click="submitApply">
                      提交认证申请
                      <svg class="btn-arrow" viewBox="0 0 24 24" fill="none">
                        <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg>
                    </button>
                  </div>
                </el-form>
              </div>
              <div v-else-if="myApplications.length === 0" class="admin-empty">
                <div class="admin-empty-icon">📅</div>
                <p>当前暂无可以申请的赛事</p>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="我的报名记录" name="registrations" v-if="isAthlete || myApplications.length > 0">
            <div class="table-section">
              <el-table
                  :data="registrationList"
                  v-loading="loading"
                  class="custom-table"
                  :header-cell-style="headerCellStyle"
                  :row-style="rowStyle"
              >
                <el-table-column prop="eventName" label="赛事名称" min-width="140" />
                <el-table-column prop="itemName" label="参赛项目" min-width="100" />
                <el-table-column label="报名时间" min-width="140">
                  <template #default="scope">
                    {{ formatDate(scope.row.registrationTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="100" align="center">
                  <template #default="scope">
                    <span class="status-pill" :class="getPillClass(scope.row.registrationStatus)">
                      {{ scope.row.registrationStatus }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
              <div class="pagination-wrap">
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
            </div>
          </el-tab-pane>

          <el-tab-pane label="管理员说明" name="admin" v-if="isAdmin">
            <div class="admin-empty">
              <div class="admin-empty-icon">🛡️</div>
              <p>管理员账号无需运动员认证</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

    </div>
  </div>

  <!-- Update Athlete Info Dialog -->
  <el-dialog
    v-model="updateDialogVisible"
    title="修改运动员信息"
    width="500px"
    class="custom-dialog"
  >
    <el-alert
      title="修改信息后当前赛事的申请状态将重新变为「审核中」，需要重新审核。"
      type="warning"
      show-icon
      :closable="false"
      style="margin-bottom: 20px;"
    />
    <el-form ref="updateFormRef" :model="updateForm" :rules="rules" label-width="80px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="updateForm.name" placeholder="请输入真实姓名" />
      </el-form-item>
      <el-form-item label="年龄" prop="age">
        <el-input-number v-model="updateForm.age" :min="1" :max="100" />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="updateForm.gender">
          <el-radio label="男">男</el-radio>
          <el-radio label="女">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="联系方式" prop="phone">
        <el-input v-model="updateForm.phone" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="年级/班级" prop="grade">
        <el-input v-model="updateForm.grade" placeholder="例如：21级计科1班" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="updateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUpdate" :loading="updating">
          确认修改
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfo } from '@/api/user'
import { getRegistrationList } from '@/api/registration'
import { applyAthlete, getMyApplications, deleteAthleteRecord, updateAthlete } from '@/api/athlete'
import { getEventList } from '@/api/event'
import { ElMessage, ElMessageBox } from 'element-plus'
import { isSuccess } from '@/utils/result'
import { normalizeAthleteStatus } from '@/utils/athleteStatus'

const route = useRoute()
const userStore = useUserStore()
const userInfo = ref({})
const isAthlete = computed(() => userInfo.value.userType === 'ATHLETE' || userInfo.value.userType === '运动员')
const isAdmin = computed(() => ['SUPER_ADMIN', 'SCHOOL_ADMIN', 'EVENT_ADMIN', '管理员'].includes(userInfo.value.userType))

const activeTab = ref('apply')

const loading = ref(false)
const registrationList = ref([])
const total = ref(0)
const queryParams = reactive({ currentPage: 1, pageSize: 5 })

const eventList = ref([])
const myApplications = ref([])
const availableEvents = computed(() => {
  return eventList.value.filter(e => !myApplications.value.some(app => app.eventId === e.id))
})
const applyFormRef = ref(null)
const applyForm = reactive({ name: '', age: 18, gender: '', phone: '', grade: '', userId: '', eventId: null })

const updateDialogVisible = ref(false)
const updating = ref(false)
const updateFormRef = ref(null)
const updateForm = reactive({ id: null, name: '', age: 18, gender: '', phone: '', grade: '' })

const rules = {
  eventId: [{ required: true, message: '请选择要报名的赛事', trigger: 'change' }],
  name:   [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone:  [{ required: true, message: '请输入联系方式', trigger: 'blur' }],
  grade:  [{ required: true, message: '请输入年级/班级', trigger: 'blur' }]
}

// Table style helpers
const headerCellStyle = {
  background: 'rgba(255,255,255,0.03)',
  color: 'var(--text-secondary, #8892a4)',
  fontSize: '12px',
  fontWeight: '600',
  letterSpacing: '0.06em',
  borderBottom: '1px solid var(--border, rgba(255,255,255,0.07))',
  padding: '12px 0'
}
const rowStyle = { background: 'transparent', borderBottom: '1px solid var(--border, rgba(255,255,255,0.05))' }

const getPillClass = (status) => {
  if (status === '通过') return 'pill-success'
  if (status === '未通过' || status === '拒绝') return 'pill-danger'
  return 'pill-warning'
}

const getApplyPillClass = (status) => {
  const normalized = normalizeAthleteStatus(status)
  if (normalized === 'APPROVED') return 'pill-success'
  if (normalized === 'REJECTED') return 'pill-danger'
  return 'pill-warning'
}

const getInfo = async () => {
  try {
    const res = await getUserInfo()
    if (isSuccess(res)) {
      userInfo.value = res.data
      userStore.setUserInfo(res.data)
      applyForm.userId = res.data.userId
      
      if (isAdmin.value) {
        activeTab.value = 'admin'
      } else {
        await loadMyApplications()
        if (isAthlete.value || myApplications.value.length > 0) {
          getRegistrations()
          if (!route.query.eventId && activeTab.value !== 'apply') {
            activeTab.value = 'registrations'
          }
        }
        await loadEvents()
      }
    }
  } catch (error) { console.error(error) }
}

const loadMyApplications = async () => {
  try {
    const res = await getMyApplications()
    if (isSuccess(res)) {
      myApplications.value = res.data || []
    }
  } catch (error) { console.error(error) }
}

const loadEvents = async () => {
  try {
    const res = await getEventList({ currentPage: 1, pageSize: 100, status: 'OPEN' })
    if (isSuccess(res)) {
      eventList.value = res.data.records || []
      if (route.query.eventId && availableEvents.value.some(e => e.id === Number(route.query.eventId))) {
        applyForm.eventId = Number(route.query.eventId)
      } else if (availableEvents.value.length > 0) {
        applyForm.eventId = availableEvents.value[0].id
      } else {
        applyForm.eventId = null
      }
    }
  } catch (error) { console.error(error) }
}

const getRegistrations = async () => {
  loading.value = true
  try {
    const res = await getRegistrationList(queryParams)
    if (isSuccess(res)) { registrationList.value = res.data.records; total.value = res.data.total }
  } catch (error) { console.error(error) }
  finally { loading.value = false }
}

const handleCancelApplication = (id) => {
  ElMessageBox.confirm('确认取消该赛事的运动员申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteAthleteRecord(id)
      if (isSuccess(res)) {
        ElMessage.success('取消成功')
        await getInfo() // refresh everything
      }
    } catch (e) {
      console.error(e)
    }
  })
}

const handleUpdateApplication = (row) => {
  updateForm.id = row.id
  updateForm.name = row.name
  updateForm.age = Number(row.age) || 18
  updateForm.gender = row.gender
  updateForm.phone = row.contact
  updateForm.grade = row.grade
  updateDialogVisible.value = true
}

const submitUpdate = async () => {
  if (!updateFormRef.value) return
  await updateFormRef.value.validate(async (valid) => {
    if (valid) {
      ElMessageBox.confirm('修改信息后当前赛事的申请状态将重新变为「审核中」，确认修改吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        updating.value = true
        try {
          const res = await updateAthlete(updateForm.id, {
            name: updateForm.name,
            age: updateForm.age,
            gender: updateForm.gender,
            contact: updateForm.phone,
            grade: updateForm.grade
          })
          if (isSuccess(res)) {
            ElMessage.success('修改成功，请等待重新审核')
            updateDialogVisible.value = false
            await getInfo()
          }
        } catch (error) {
          console.error(error)
        } finally {
          updating.value = false
        }
      }).catch(() => {})
    }
  })
}

const submitApply = async () => {
  if (!applyFormRef.value) return
  await applyFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await applyAthlete(applyForm)
        if (isSuccess(res)) { 
          ElMessage.success('申请提交成功')
          applyFormRef.value.resetFields()
          await getInfo()
        }
      } catch (error) { console.error(error) }
    }
  })
}
const handleSizeChange = (val) => { queryParams.pageSize = val; getRegistrations() }
const handleCurrentChange = (val) => { queryParams.currentPage = val; getRegistrations() }
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => getInfo())
</script>

<style scoped>
/* ── Layout ── */
.profile-container {
  padding: 24px;
  font-family: 'Noto Sans SC', sans-serif;
}

.profile-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
  align-items: start;
}

/* ── Shared card base ── */
.info-card,
.main-card {
  background: var(--bg-card, #161921);
  border: 1px solid var(--border, rgba(255,255,255,0.07));
  border-radius: 16px;
  overflow: hidden;
}

/* ── Left: Info Card ── */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 24px 24px;
  gap: 10px;
}

.avatar-ring {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35, #e0541e);
  padding: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

:deep(.avatar-ring .el-avatar) {
  border: 2px solid var(--bg-card, #161921);
}

.username {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary, var(--el-bg-color-page));
  letter-spacing: 0.02em;
}

.role-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary, var(--el-text-color-secondary));
  background: rgba(255,255,255,0.05);
  padding: 4px 12px;
  border-radius: 20px;
}

.role-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #FF6B35;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.4; }
}

.card-divider {
  height: 1px;
  background: var(--border, rgba(255,255,255,0.07));
  margin: 0 20px;
}

.info-list {
  padding: 16px 0 24px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 24px;
  transition: background 0.15s;
}

.info-row:hover {
  background: rgba(255,255,255,0.03);
}

.info-label {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  color: var(--text-secondary, var(--el-text-color-secondary));
  font-weight: 500;
  flex-shrink: 0;
}

.info-icon {
  width: 14px;
  height: 14px;
  color: #FF6B35;
  flex-shrink: 0;
}

.info-value {
  font-size: 13px;
  color: var(--text-primary, var(--el-bg-color-page));
  text-align: right;
  word-break: break-all;
  max-width: 160px;
}

.info-value.small {
  font-size: 12px;
  color: var(--text-secondary, var(--el-text-color-secondary));
}

:deep(.custom-tag) {
  border-radius: 6px;
  font-size: 11px;
}

/* ── Right: Main Card ── */
.main-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border, rgba(255,255,255,0.07));
}

.header-accent {
  display: block;
  width: 3px;
  height: 18px;
  background: #FF6B35;
  border-radius: 2px;
  flex-shrink: 0;
}

.main-card-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary, var(--el-bg-color-page));
}

/* ── Table ── */
.table-section {
  padding: 16px 24px 24px;
}

:deep(.custom-table) {
  background: transparent !important;
}

:deep(.custom-table .el-table__inner-wrapper) {
  background: transparent !important;
}

:deep(.custom-table tr),
:deep(.custom-table td.el-table__cell),
:deep(.custom-table th.el-table__cell) {
  background: transparent !important;
}

:deep(.custom-table .el-table__cell) {
  border-bottom: 1px solid var(--border, rgba(255,255,255,0.06)) !important;
  color: var(--text-primary, var(--el-bg-color-page));
  font-size: 13px;
}

:deep(.custom-table .el-table__row:hover td) {
  background: rgba(255,107,53,0.04) !important;
}

/* Status pill */
.status-pill {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
}
.pill-success { background: rgba(16,185,129,0.15); color: #34d399; border: 1px solid rgba(16,185,129,0.25); }
.pill-danger  { background: rgba(239,68,68,0.15);  color: #f87171; border: 1px solid rgba(239,68,68,0.25); }
.pill-warning { background: rgba(245,158,11,0.15); color: #fbbf24; border: 1px solid rgba(245,158,11,0.25); }

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* ── Apply Form ── */
.apply-form {
  padding: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px 20px;
}

.form-field { display: flex; flex-direction: column; }
.form-field.full-width { grid-column: 1 / -1; }

.field-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary, var(--el-text-color-secondary));
  margin-bottom: 6px;
  letter-spacing: 0.04em;
}

.required { color: #FF6B35; }

:deep(.custom-input .el-input__wrapper) {
  background-color: rgba(255,255,255,0.05) !important;
  border: 1px solid var(--border, rgba(255,255,255,0.09)) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
  height: 40px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
:deep(.custom-input .el-input__wrapper:hover) {
  border-color: rgba(255,107,53,0.4) !important;
}
:deep(.custom-input .el-input__wrapper.is-focus) {
  border-color: #FF6B35 !important;
  box-shadow: 0 0 0 2px rgba(255,107,53,0.15) !important;
}
:deep(.custom-input .el-input__inner) {
  color: var(--text-primary, var(--el-bg-color-page)) !important;
  font-size: 13px;
}

:deep(.custom-number) {
  width: 100%;
}
:deep(.custom-number .el-input__wrapper) {
  background-color: rgba(255,255,255,0.05) !important;
  border: 1px solid var(--border, rgba(255,255,255,0.09)) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
}
:deep(.custom-number .el-input__inner) {
  color: var(--text-primary, var(--el-bg-color-page)) !important;
}

/* Gender toggle */
.gender-toggle {
  display: flex;
  gap: 10px;
  width: 100%;
}

.gender-btn {
  flex: 1;
  height: 40px;
  border: 1px solid var(--border, rgba(255,255,255,0.1));
  border-radius: 8px;
  background: rgba(255,255,255,0.04);
  color: var(--text-secondary, var(--el-text-color-secondary));
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s;
}

.gender-btn:hover {
  border-color: rgba(255,107,53,0.4);
  color: var(--text-primary, var(--el-bg-color-page));
}

.gender-btn.active {
  background: rgba(255,107,53,0.12);
  border-color: #FF6B35;
  color: #FF6B35;
  font-weight: 600;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-form-item__error) {
  color: #f87171;
  font-size: 11px;
}

/* Submit button */
.form-actions {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.submit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 28px;
  height: 44px;
  background: linear-gradient(135deg, #FF6B35, #e0541e);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  letter-spacing: 0.06em;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s;
  box-shadow: 0 4px 16px rgba(255,107,53,0.3);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255,107,53,0.4);
}

.btn-arrow {
  width: 16px;
  height: 16px;
  transition: transform 0.2s;
}

.submit-btn:hover .btn-arrow {
  transform: translateX(4px);
}

/* ── Applied Status ── */
.applied-section {
  padding: 24px;
}

.apply-status-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid;
}

.apply-success { background: rgba(16,185,129,0.08); border-color: rgba(16,185,129,0.25); }
.apply-danger  { background: rgba(239,68,68,0.08);  border-color: rgba(239,68,68,0.25); }
.apply-pending { background: rgba(245,158,11,0.08); border-color: rgba(245,158,11,0.25); }

.apply-status-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}

.apply-success .apply-status-icon { background: rgba(16,185,129,0.15); color: #34d399; }
.apply-danger  .apply-status-icon { background: rgba(239,68,68,0.15);  color: #f87171; }
.apply-pending .apply-status-icon { background: rgba(245,158,11,0.15); color: #fbbf24; }

.apply-status-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary, var(--el-bg-color-page));
  margin-bottom: 4px;
}

.apply-status-sub {
  font-size: 12px;
  color: var(--text-secondary, var(--el-text-color-secondary));
  line-height: 1.5;
}

.retry-section {
  margin-top: 16px;
}

.retry-btn {
  padding: 8px 20px;
  background: transparent;
  border: 1px solid rgba(255,107,53,0.4);
  border-radius: 8px;
  color: #FF6B35;
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s;
}

.retry-btn:hover {
  background: rgba(255,107,53,0.1);
  border-color: #FF6B35;
}

/* ── Admin empty ── */
.admin-empty {
  padding: 60px 24px;
  text-align: center;
  color: var(--text-secondary, var(--el-text-color-secondary));
  font-size: 14px;
}

.admin-empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
  .form-grid {
    grid-template-columns: 1fr;
  }
  .form-field.full-width {
    grid-column: auto;
  }
}

:global(html.dark) :deep(.custom-table.el-table) {
  background: var(--el-bg-color) !important;
  color: var(--el-text-color-primary);
}
:global(html.dark) :deep(.custom-table .el-table__inner-wrapper) {
  background: var(--el-bg-color) !important;
}
:global(html.dark) :deep(.custom-table .el-table__body-wrapper td) {
  background: var(--el-bg-color) !important;
}
</style>

<style>
/* Global: force dark inputs inside profile */
.profile-container .el-input__wrapper {
  background-color: rgba(255,255,255,0.05) !important;
  box-shadow: none !important;
}
.profile-container .el-input__inner {
  color: var(--text-primary, #f0f2f8) !important;
  background: transparent !important;
}
.profile-container .el-table {
  background: transparent !important;
  color: var(--text-primary, #f0f2f8) !important;
}
.profile-container .el-table__header-wrapper {
  background: transparent !important;
}
.profile-container .el-input-number .el-input-number__decrease,
.profile-container .el-input-number .el-input-number__increase {
  background: rgba(255,255,255,0.05) !important;
  border-color: rgba(255,255,255,0.09) !important;
  color: var(--text-secondary, #8892a4) !important;
}
</style>
