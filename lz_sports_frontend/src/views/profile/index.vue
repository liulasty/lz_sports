<template>
  <div class="profile-container">

    <!-- 环境背景光斑 -->
    <div class="bg-blob bg-blob-1"></div>
    <div class="bg-blob bg-blob-2"></div>
    <div class="bg-blob bg-blob-3"></div>

    <div class="profile-grid">

      <!-- ── 左侧：用户信息卡片 ── -->
      <div class="info-card">
        <!-- 顶部渐变背景带 -->
        <div class="card-hero">
          <div class="hero-pattern"></div>
          <div class="avatar-float">
            <div class="avatar-ring">
              <el-avatar
                  :size="100"
                  :src="userInfo.avatarSrc || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
              />
            </div>
            <div class="avatar-glow"></div>
          </div>
        </div>

        <!-- 身份信息区域 -->
        <div class="identity-section">
          <h3 class="username">{{ userInfo.userName || '—' }}</h3>
          <div class="role-badge">
            <span class="role-dot"></span>
            {{ userInfo.userType || '用户' }}
          </div>
        </div>

        <!-- 统计数据条 -->
        <div class="stats-strip">
          <div class="stat-item">
            <span class="stat-number">{{ myApplications.length }}</span>
            <span class="stat-label">赛事申请</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">{{ total }}</span>
            <span class="stat-label">报名记录</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number stat-status" :class="userInfo.status === '已激活' ? 'active' : 'inactive'">
              {{ userInfo.status === '已激活' ? '✓' : '○' }}
            </span>
            <span class="stat-label">账号状态</span>
          </div>
        </div>

        <!-- 用户详细信息列表 -->
        <div class="info-list">
          <div class="info-row">
            <div class="info-label">
              <span class="info-icon-wrap ic-email">
                <svg viewBox="0 0 20 20" fill="currentColor"><path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"/><path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"/></svg>
              </span>
              邮箱
            </div>
            <span class="info-value">{{ userInfo.email || '—' }}</span>
          </div>

          <div class="info-row">
            <div class="info-label">
              <span class="info-icon-wrap ic-role">
                <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 2a1 1 0 00-1 1v1a1 1 0 002 0V3a1 1 0 00-1-1zM4 4h3a3 3 0 000 6H4a3 3 0 000-6zm0 8a3 3 0 000 6h3a3 3 0 000-6H4zm10-8a3 3 0 000 6h3a3 3 0 000-6h-3zm0 8a3 3 0 000 6h3a3 3 0 000-6h-3z" clip-rule="evenodd"/></svg>
              </span>
              角色
            </div>
            <el-tag size="small" class="vivid-tag tag-role">{{ userInfo.userType || '—' }}</el-tag>
          </div>

          <div class="info-row">
            <div class="info-label">
              <span class="info-icon-wrap ic-status">
                <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/></svg>
              </span>
              状态
            </div>
            <el-tag
                size="small"
                :type="userInfo.status === '已激活' ? 'success' : 'warning'"
                class="vivid-tag"
            >{{ userInfo.status || '—' }}</el-tag>
          </div>

          <div class="info-row">
            <div class="info-label">
              <span class="info-icon-wrap ic-date">
                <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd"/></svg>
              </span>
              注册时间
            </div>
            <span class="info-value small">{{ formatDate(userInfo.registerTime) || '—' }}</span>
          </div>
        </div>
      </div>

      <!-- ── 右侧：主内容面板 ── -->
      <div class="main-card">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="运动员认证" name="apply" v-if="!isAdmin">
            <div class="certification-section">

              <!-- 已申请的赛事列表 -->
              <div v-if="myApplications.length > 0" class="applications-list">
                <div class="section-header">
                  <span class="section-badge badge-violet">已申请</span>
                  <h4 class="section-title">已申请的赛事</h4>
                </div>
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
                      <!-- 隐藏修改按钮，因为不能随意修改认证信息 -->
                      <el-button type="danger" link size="small" @click="handleCancelApplication(scope.row.id)">取消</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <!-- 赛事申请表单 -->
              <div v-if="availableEvents.length > 0" class="apply-form-wrap">
                <div class="section-header">
                  <span class="section-badge badge-orange">新申请</span>
                  <h4 class="section-title">申请新赛事资格</h4>
                </div>
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
                  </div>
                  <el-alert
                    title="注意：申请认证将直接使用您当前个人资料中的姓名、性别、联系方式及部门信息。请确保个人资料已完善，且认证通过后将无法随意修改。"
                    type="info"
                    show-icon
                    :closable="false"
                    style="margin-bottom: 20px;"
                  />

                  <div class="form-actions">
                    <button class="submit-btn" type="button" @click="submitApply">
                      <span class="btn-text">提交认证申请</span>
                      <svg class="btn-arrow" viewBox="0 0 24 24" fill="none">
                        <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg>
                    </button>
                  </div>
                </el-form>
              </div>

              <div v-else-if="myApplications.length === 0" class="empty-state">
                <div class="empty-icon">📅</div>
                <p class="empty-text">当前暂无可以申请的赛事</p>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="我的报名记录" name="registrations" v-if="isAthlete || myApplications.length > 0">
            <div class="table-section">
              <div class="section-header">
                <span class="section-badge badge-cyan">记录</span>
                <h4 class="section-title">报名历史</h4>
              </div>
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
                  <template #default="scope">{{ formatDate(scope.row.registrationTime) }}</template>
                </el-table-column>
                <el-table-column label="状态" width="100" align="center">
                  <template #default="scope">
                    <span class="status-pill" :class="getPillClass(scope.row.registrationStatus)">
                      {{ getStatusLabel(scope.row.registrationStatus) }}
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
            <div class="empty-state">
              <div class="empty-icon">🛡️</div>
              <p class="empty-text">管理员账号无需运动员认证</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

    </div>

    <!-- 修改运动员信息弹窗 -->
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
      <el-form ref="updateFormRef" :model="updateForm" label-width="80px">
        <el-alert
          title="系统已升级，运动员认证直接读取您的个人基础信息。"
          type="info"
          show-icon
          :closable="false"
          style="margin-bottom: 20px;"
        />
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="updateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUpdate" :loading="updating">确认修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * @file index.vue
 * @description 个人中心/运动员认证页面组件，包含个人信息展示、赛事认证申请、报名记录查询等功能
 */
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfo } from '@/api/user'
import { getRegistrationList } from '@/api/registration'
import { applyAthlete, getMyApplications, deleteAthleteRecord, updateAthlete } from '@/api/athlete'
import { getEventList } from '@/api/event'
import { getDepartmentTree } from '@/api/department'
import { getSchoolConfig } from '@/api/init'
import { ElMessage, ElMessageBox } from 'element-plus'
import { isSuccess } from '@/utils/result'
import { normalizeAthleteStatus } from '@/utils/athleteStatus'

const route = useRoute() // 路由对象
const userStore = useUserStore() // 用户状态管理
const userInfo = ref({}) // 用户详细信息
// 判断当前用户是否为运动员
const isAthlete = computed(() => userInfo.value.userType === 'ATHLETE' || userInfo.value.userType === '运动员')
// 判断当前用户是否为管理员
const isAdmin = computed(() => ['SUPER_ADMIN', 'SCHOOL_ADMIN', 'EVENT_ADMIN', '管理员'].includes(userInfo.value.userType))

const activeTab = ref('apply') // 当前激活的标签页
const loading = ref(false) // 表格加载状态
const registrationList = ref([]) // 报名记录列表
const total = ref(0) // 报名记录总数
const queryParams = reactive({ currentPage: 1, pageSize: 5 }) // 查询参数

const eventList = ref([]) // 赛事列表
const myApplications = ref([]) // 我的赛事申请列表
// 计算可申请的赛事（过滤掉已经申请过的）
const availableEvents = computed(() => {
  return eventList.value.filter(e => !myApplications.value.some(app => app.eventId === e.id))
})
const applyFormRef = ref(null) // 申请表单引用
const applyForm = reactive({ eventId: null }) // 申请表单数据，仅保留 eventId

const updateDialogVisible = ref(false) // 修改信息弹窗显示状态
const updating = ref(false) // 修改提交状态
const updateFormRef = ref(null) // 修改表单引用
// 由于后端去掉了所有身份字段，修改逻辑可能需要从此处移除或只允许修改某些东西
// 为适应后端最新接口，移除 updateForm 中的相关字段，或暂保留作其他用途
const updateForm = reactive({ id: null }) 

const departmentTree = ref([])
const schoolConfig = ref({ orgMode: 'UNIVERSITY' })

// 获取部门树和学校配置
const loadBaseData = async () => {
  try {
    const [deptRes, configRes] = await Promise.all([getDepartmentTree(), getSchoolConfig()])
    if (isSuccess(deptRes)) {
      departmentTree.value = deptRes.data || []
    }
    if (isSuccess(configRes)) {
      schoolConfig.value = configRes.data || { orgMode: 'UNIVERSITY' }
    }
  } catch (error) {
    console.error('获取基础数据失败:', error)
  }
}

// 表单校验规则
const rules = {
  eventId: [{ required: true, message: '请选择要报名的赛事', trigger: 'change' }]
}

const headerCellStyle = {
  background: 'transparent',
  color: 'var(--pc-text-muted)',
  fontSize: '11px',
  fontWeight: '700',
  letterSpacing: '0.08em',
  textTransform: 'uppercase',
  borderBottom: '2px solid var(--pc-border)',
  padding: '14px 0'
}
const rowStyle = { background: 'transparent' }

/**
 * 获取状态对应的样式类名（通用）
 * @param {string} status 状态字符串
 * @returns {string} 样式类名
 */
const getPillClass = (status) => {
  if (status === 'APPROVED' || status === 'CONFIRMED') return 'pill-success'
  if (status === 'REJECTED') return 'pill-danger'
  if (status === 'CANCELLED') return 'pill-info'
  return 'pill-warning'
}

const getStatusLabel = (status) => {
  if (status === 'APPROVED') return '审核通过'
  if (status === 'REJECTED') return '已拒绝'
  if (status === 'PENDING') return '审核中'
  if (status === 'CANCELLED') return '已取消'
  if (status === 'CONFIRMED') return '已确认'
  return status
}
/**
 * 获取赛事申请状态对应的样式类名
 * @param {string} status 申请状态
 * @returns {string} 样式类名
 */
const getApplyPillClass = (status) => {
  const normalized = normalizeAthleteStatus(status)
  if (normalized === 'APPROVED') return 'pill-success'
  if (normalized === 'REJECTED') return 'pill-danger'
  return 'pill-warning'
}

/**
 * 获取用户基础信息并初始化页面数据
 */
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
  } catch (error) { console.error('获取信息失败:', error) }
}

/**
 * 获取我的赛事申请列表
 */
const loadMyApplications = async () => {
  try {
    const res = await getMyApplications()
    if (isSuccess(res)) { myApplications.value = res.data || [] }
  } catch (error) { console.error('获取信息失败:', error) }
}

/**
 * 获取可用赛事列表
 */
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
  } catch (error) { console.error('获取信息失败:', error) }
}

/**
 * 获取报名记录列表
 */
const getRegistrations = async () => {
  loading.value = true
  try {
    const res = await getRegistrationList(queryParams)
    if (isSuccess(res)) { registrationList.value = res.data.records; total.value = res.data.total }
  } catch (error) { console.error('获取信息失败:', error) }
  finally { loading.value = false }
}

/**
 * 取消赛事申请
 * @param {number|string} id 申请记录ID
 */
const handleCancelApplication = (id) => {
  ElMessageBox.confirm('确认取消该赛事的运动员申请吗？', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteAthleteRecord(id)
      if (isSuccess(res)) { ElMessage.success('取消成功'); await getInfo() }
    } catch (e) { console.error('操作失败:', e) }
  })
}

/**
 * 打开修改申请信息弹窗
 * @param {Object} row 当前行数据
 */
const handleUpdateApplication = (row) => {
  updateForm.id = row.id
  updateDialogVisible.value = true
}

/**
 * 提交修改申请信息
 */
const submitUpdate = async () => {
  if (!updateFormRef.value) return
  await updateFormRef.value.validate(async (valid) => {
    if (valid) {
      ElMessageBox.confirm('修改信息后当前赛事的申请状态将重新变为「审核中」，确认修改吗？', '提示', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        updating.value = true
        try {
          const res = await updateAthlete(updateForm.id, {})
          if (isSuccess(res)) {
            ElMessage.success('修改成功，请等待重新审核')
            updateDialogVisible.value = false
            await getInfo()
          }
        } catch (error) { console.error('获取信息失败:', error) } finally { updating.value = false }
      }).catch(() => {})
    }
  })
}

/**
 * 提交新的赛事认证申请
 */
const submitApply = async () => {
  if (!applyFormRef.value) return
  await applyFormRef.value.validate(async (valid) => {
    if (valid) {
      ElMessageBox.confirm(
        '系统将读取您的个人资料进行申请，提交后若审核通过将无法随意修改信息，是否继续？',
        '提交认证确认',
        { confirmButtonText: '确定提交', cancelButtonText: '取消', type: 'warning' }
      ).then(async () => {
        try {
          const res = await applyAthlete(applyForm)
          if (isSuccess(res)) {
            ElMessage.success('申请提交成功')
            applyFormRef.value.resetFields()
            await getInfo()
          } else {
            ElMessage.error(res.message || '申请失败')
          }
        } catch (error) {
          console.error('提交申请失败:', error)
          // 提示由后端抛出的业务异常，如“请先在个人中心完善身份信息”
        }
      }).catch(() => {})
    }
  })
}

/**
 * 处理分页大小改变
 * @param {number} val 每页条数
 */
const handleSizeChange = (val) => { queryParams.pageSize = val; getRegistrations() }
/**
 * 处理页码改变
 * @param {number} val 当前页码
 */
const handleCurrentChange = (val) => { queryParams.currentPage = val; getRegistrations() }
/**
 * 格式化日期字符串
 * @param {string} dateStr 日期字符串
 * @returns {string} 格式化后的本地日期时间字符串
 */
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => {
  await loadBaseData()
  getInfo()
})
</script>

<style scoped>
/* ─────────────────────────────────────────
   CSS 变量 — 亮色与暗色模式自适应
───────────────────────────────────────── */
.profile-container {
  /* 亮色模式默认值 */
  --pc-bg:           #f4f5f9;
  --pc-card:         #ffffff;
  --pc-card-alt:     #fafbff;
  --pc-border:       rgba(0,0,0,0.07);
  --pc-text:         #0f1117;
  --pc-text-sub:     #4a5568;
  --pc-text-muted:   #8896a7;
  --pc-shadow:       0 4px 24px rgba(0,0,0,0.08), 0 1px 4px rgba(0,0,0,0.04);
  --pc-shadow-lg:    0 12px 48px rgba(0,0,0,0.12), 0 4px 12px rgba(0,0,0,0.06);

  /* 强调色调 — 两种模式下相同 */
  --c-orange:        #FF6B35;
  --c-orange-2:      #FF8F60;
  --c-orange-dim:    rgba(255,107,53,0.12);
  --c-orange-glow:   rgba(255,107,53,0.25);

  --c-violet:        #7C3AED;
  --c-violet-2:      #A78BFA;
  --c-violet-dim:    rgba(124,58,237,0.1);

  --c-cyan:          #0EA5E9;
  --c-cyan-2:        #38BDF8;
  --c-cyan-dim:      rgba(14,165,233,0.1);

  --c-emerald:       #10B981;
  --c-emerald-dim:   rgba(16,185,129,0.12);
  --c-red:           #EF4444;
  --c-red-dim:       rgba(239,68,68,0.12);
  --c-amber:         #F59E0B;
  --c-amber-dim:     rgba(245,158,11,0.12);
}

/* 暗色模式重写 */
:root.dark .profile-container,
html.dark .profile-container,
[data-theme="dark"] .profile-container {
  --pc-bg:           #0d0f14;
  --pc-card:         #161921;
  --pc-card-alt:     #1c1f28;
  --pc-border:       rgba(255,255,255,0.07);
  --pc-text:         #e8ecf5;
  --pc-text-sub:     #9aa5b8;
  --pc-text-muted:   #5a6478;
  --pc-shadow:       0 4px 24px rgba(0,0,0,0.4), 0 1px 4px rgba(0,0,0,0.2);
  --pc-shadow-lg:    0 12px 48px rgba(0,0,0,0.5), 0 4px 12px rgba(0,0,0,0.3);
}

/* ─────────────────────────────────────────
   外部容器与环境光斑
───────────────────────────────────────── */
.profile-container {
  position: relative;
  padding: 32px 28px;
  min-height: 100vh;
  background: var(--pc-bg);
  overflow: hidden;
  font-family: 'PingFang SC', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  transition: background 0.3s;
}

.bg-blob {
  position: fixed;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  z-index: 0;
  opacity: 0.18;
  transition: opacity 0.3s;
}
html.dark .bg-blob { opacity: 0.10; }

.bg-blob-1 {
  width: 500px; height: 500px;
  top: -120px; left: -100px;
  background: radial-gradient(circle, var(--c-orange), transparent 70%);
}
.bg-blob-2 {
  width: 400px; height: 400px;
  top: 30%; right: -80px;
  background: radial-gradient(circle, var(--c-violet), transparent 70%);
}
.bg-blob-3 {
  width: 350px; height: 350px;
  bottom: 60px; left: 30%;
  background: radial-gradient(circle, var(--c-cyan), transparent 70%);
}

/* ─────────────────────────────────────────
   网格布局
───────────────────────────────────────── */
.profile-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
  align-items: start;
  max-width: 1300px;
  margin: 0 auto;
}

/* ─────────────────────────────────────────
   左侧信息卡片
───────────────────────────────────────── */
.info-card {
  background: var(--pc-card);
  border: 1px solid var(--pc-border);
  border-radius: 24px;
  box-shadow: var(--pc-shadow-lg);
  overflow: hidden;
  transition: background 0.3s, border-color 0.3s;
}

/* 顶部英雄渐变带 */
.card-hero {
  position: relative;
  height: 120px;
  background: linear-gradient(135deg, #FF6B35 0%, #e040c0 50%, #7C3AED 100%);
  overflow: visible;
}

.hero-pattern {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle at 20% 50%, rgba(255,255,255,0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255,255,255,0.1) 0%, transparent 40%);
}

.avatar-float {
  position: absolute;
  bottom: -54px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
}

.avatar-ring {
  width: 108px;
  height: 108px;
  border-radius: 50%;
  padding: 4px;
  background: linear-gradient(135deg, #FF6B35, #e040c0, #7C3AED);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(255,107,53,0.4), 0 0 0 4px var(--pc-card);
  transition: box-shadow 0.3s;
}

:deep(.avatar-ring .el-avatar) {
  border: 3px solid var(--pc-card);
  transition: border-color 0.3s;
}

.avatar-glow {
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  background: conic-gradient(from 0deg, #FF6B35, #e040c0, #7C3AED, #FF6B35);
  opacity: 0.3;
  filter: blur(10px);
  z-index: -1;
  animation: spin-slow 8s linear infinite;
}

@keyframes spin-slow {
  to { transform: rotate(360deg); }
}

/* 身份信息区域 */
.identity-section {
  margin-top: 64px;
  padding: 0 24px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.username {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--pc-text);
  letter-spacing: -0.02em;
  transition: color 0.3s;
}

.role-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  font-weight: 600;
  color: var(--pc-text-sub);
  background: var(--pc-card-alt);
  border: 1px solid var(--pc-border);
  padding: 5px 14px;
  border-radius: 20px;
  letter-spacing: 0.04em;
  transition: all 0.3s;
}

.role-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35, #e040c0);
  box-shadow: 0 0 8px rgba(255,107,53,0.6);
  animation: pulse 2.4s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50%       { transform: scale(1.3); opacity: 0.7; }
}

/* 统计数据条 */
.stats-strip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin: 0 24px 4px;
  background: var(--pc-card-alt);
  border: 1px solid var(--pc-border);
  border-radius: 16px;
  padding: 16px 8px;
  transition: all 0.3s;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-number {
  font-size: 22px;
  font-weight: 800;
  color: var(--c-orange);
  line-height: 1;
  background: linear-gradient(135deg, var(--c-orange), var(--c-orange-2));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-number.active   { background: linear-gradient(135deg, #10B981, #34d399); -webkit-background-clip: text; background-clip: text; }
.stat-number.inactive { background: linear-gradient(135deg, #F59E0B, #fbbf24); -webkit-background-clip: text; background-clip: text; }

.stat-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--pc-text-muted);
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: var(--pc-border);
  flex-shrink: 0;
}

/* 信息列表 */
.info-list {
  padding: 12px 0 28px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 24px;
  transition: background 0.15s;
}

.info-row:hover {
  background: var(--pc-card-alt);
}

.info-label {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 12px;
  font-weight: 600;
  color: var(--pc-text-muted);
  letter-spacing: 0.04em;
  flex-shrink: 0;
}

/* 彩色图标容器 */
.info-icon-wrap {
  width: 28px; height: 28px;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.info-icon-wrap svg { width: 13px; height: 13px; }

.ic-email  { background: rgba(14,165,233,0.12);  color: var(--c-cyan); }
.ic-role   { background: rgba(124,58,237,0.12);  color: var(--c-violet); }
.ic-status { background: rgba(16,185,129,0.12);  color: var(--c-emerald); }
.ic-date   { background: rgba(255,107,53,0.12);  color: var(--c-orange); }

.info-value {
  font-size: 13px;
  font-weight: 500;
  color: var(--pc-text);
  text-align: right;
  word-break: break-all;
  max-width: 170px;
  transition: color 0.3s;
}

.info-value.small {
  font-size: 12px;
  color: var(--pc-text-sub);
}

:deep(.vivid-tag) {
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
}
:deep(.tag-role) {
  background: var(--c-violet-dim);
  color: var(--c-violet-2);
  border: 1px solid rgba(124,58,237,0.2);
}

/* ─────────────────────────────────────────
   右侧主卡片
───────────────────────────────────────── */
.main-card {
  background: var(--pc-card);
  border: 1px solid var(--pc-border);
  border-radius: 24px;
  box-shadow: var(--pc-shadow-lg);
  overflow: hidden;
  transition: background 0.3s, border-color 0.3s;
}

/* ── 标签页样式重写 ── */
:deep(.profile-tabs .el-tabs__header) {
  background: var(--pc-card-alt);
  border-bottom: 1px solid var(--pc-border);
  padding: 0 28px;
  margin: 0;
  transition: background 0.3s, border-color 0.3s;
}

:deep(.profile-tabs .el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.profile-tabs .el-tabs__item) {
  font-size: 13px;
  font-weight: 600;
  color: var(--pc-text-muted);
  height: 52px;
  line-height: 52px;
  padding: 0 20px;
  letter-spacing: 0.03em;
  transition: color 0.2s;
}

:deep(.profile-tabs .el-tabs__item.is-active) {
  color: var(--c-orange);
}

:deep(.profile-tabs .el-tabs__active-bar) {
  background: linear-gradient(90deg, var(--c-orange), var(--c-orange-2));
  height: 3px;
  border-radius: 2px;
}

:deep(.profile-tabs .el-tabs__content) {
  padding: 28px;
}

/* ── 区域标题 ── */
.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.section-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.badge-orange { background: var(--c-orange-dim);  color: var(--c-orange);  border: 1px solid var(--c-orange-glow); }
.badge-violet { background: var(--c-violet-dim);  color: var(--c-violet-2); border: 1px solid rgba(124,58,237,0.2); }
.badge-cyan   { background: var(--c-cyan-dim);    color: var(--c-cyan-2);  border: 1px solid rgba(14,165,233,0.2); }

.section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--pc-text);
  letter-spacing: -0.01em;
  transition: color 0.3s;
}

/* ── 表格样式 ── */
.table-section { /* 内边距由标签页内容处理 */ }

.applications-list {
  margin-bottom: 36px;
}

:deep(.custom-table) {
  background: transparent;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--pc-border);
}

:deep(.el-table__inner-wrapper::before) { display: none; }

:deep(.custom-table tr),
:deep(.custom-table td.el-table__cell),
:deep(.custom-table th.el-table__cell) {
  background: transparent;
}

:deep(.custom-table .el-table__cell) {
  border-bottom: 1px solid var(--pc-border);
  color: var(--pc-text);
  font-size: 13px;
  transition: color 0.3s, border-color 0.3s;
}

:deep(.custom-table .el-table__row:hover td) {
  background: var(--c-orange-dim) !important;
}

/* 状态胶囊标签 */
.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  white-space: nowrap;
}

.pill-success {
  background: var(--c-emerald-dim);
  color: var(--c-emerald);
  border: 1px solid rgba(16,185,129,0.25);
}
.pill-danger {
  background: var(--c-red-dim);
  color: var(--c-red);
  border: 1px solid rgba(239,68,68,0.25);
}
.pill-warning {
  background: var(--c-amber-dim);
  color: var(--c-amber);
  border: 1px solid rgba(245,158,11,0.25);
}
.pill-info {
  background: rgba(148,163,184,0.15);
  color: #94a3b8;
  border: 1px solid rgba(148,163,184,0.25);
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* ── 申请表单 ── */
.apply-form-wrap { }

.apply-form { }

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 20px;
}

.form-field { display: flex; flex-direction: column; }
.form-field.full-width { grid-column: 1 / -1; }

.field-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--pc-text-sub);
  margin-bottom: 7px;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  transition: color 0.3s;
}
.required { color: var(--c-orange); }

:deep(.el-form-item) { margin-bottom: 4px; }
:deep(.el-form-item__error) { color: var(--c-red); font-size: 11px; }

/* 输入框样式 — 仅在当前组件生效 */
:deep(.custom-input .el-input__wrapper) {
  background: var(--pc-card-alt);
  border: 1.5px solid var(--pc-border);
  border-radius: 10px;
  box-shadow: none;
  height: 42px;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.3s;
}
:deep(.custom-input .el-input__wrapper:hover) {
  border-color: var(--c-orange-2);
}
:deep(.custom-input .el-input__wrapper.is-focus) {
  border-color: var(--c-orange);
  box-shadow: 0 0 0 3px var(--c-orange-dim);
}
:deep(.custom-input .el-input__inner) {
  color: var(--pc-text);
  font-size: 13px;
  background: transparent;
}

:deep(.el-select .el-input__wrapper) {
  background: var(--pc-card-alt);
  border: 1.5px solid var(--pc-border);
  border-radius: 10px;
  box-shadow: none;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.3s;
}
:deep(.el-select .el-input__wrapper:hover) {
  border-color: var(--c-orange-2);
}
:deep(.el-select .el-input.is-focus .el-input__wrapper) {
  border-color: var(--c-orange);
  box-shadow: 0 0 0 3px var(--c-orange-dim);
}

:deep(.custom-number) { width: 100%; }
:deep(.custom-number .el-input__wrapper) {
  background: var(--pc-card-alt);
  border: 1.5px solid var(--pc-border);
  border-radius: 10px;
  box-shadow: none;
  transition: background 0.3s, border-color 0.3s;
}
:deep(.custom-number .el-input__inner) {
  color: var(--pc-text);
  background: transparent;
}
:deep(.el-input-number__decrease),
:deep(.el-input-number__increase) {
  background: var(--pc-card-alt);
  border-color: var(--pc-border);
  color: var(--pc-text-sub);
  transition: all 0.2s;
}

/* 性别切换按钮 */
.gender-toggle { display: flex; gap: 10px; width: 100%; }

.gender-btn {
  flex: 1;
  height: 42px;
  border: 1.5px solid var(--pc-border);
  border-radius: 10px;
  background: var(--pc-card-alt);
  color: var(--pc-text-sub);
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s;
  letter-spacing: 0.04em;
}
.gender-btn:hover {
  border-color: var(--c-cyan-2);
  color: var(--c-cyan-2);
  background: var(--c-cyan-dim);
}
.gender-btn.active {
  background: var(--c-cyan-dim);
  border-color: var(--c-cyan);
  color: var(--c-cyan);
  box-shadow: 0 0 0 2px rgba(14,165,233,0.15);
}
.gender-btn.female.active {
  background: rgba(232,68,156,0.1);
  border-color: #e8449c;
  color: #e8449c;
  box-shadow: 0 0 0 2px rgba(232,68,156,0.15);
}
.gender-btn.female:hover {
  border-color: #e8449c;
  color: #e8449c;
  background: rgba(232,68,156,0.08);
}

/* 提交按钮 */
.form-actions { margin-top: 28px; display: flex; justify-content: flex-end; }

.submit-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 0 32px;
  height: 48px;
  background: linear-gradient(135deg, #FF6B35 0%, #e040c0 100%);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  font-family: inherit;
  letter-spacing: 0.05em;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 6px 20px rgba(255,107,53,0.35), 0 2px 8px rgba(224,64,192,0.2);
}
.submit-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.18), transparent);
  opacity: 0;
  transition: opacity 0.2s;
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(255,107,53,0.45), 0 4px 12px rgba(224,64,192,0.3);
}
.submit-btn:hover::before { opacity: 1; }
.submit-btn:active { transform: translateY(0); }

.btn-arrow {
  width: 16px; height: 16px;
  transition: transform 0.2s;
}
.submit-btn:hover .btn-arrow {
  transform: translateX(4px);
}

/* 空状态提示 */
.empty-state {
  padding: 64px 24px;
  text-align: center;
}
.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.15));
}
.empty-text {
  font-size: 14px;
  color: var(--pc-text-muted);
  margin: 0;
  letter-spacing: 0.03em;
}

/* ─────────────────────────────────────────
   响应式设计
───────────────────────────────────────── */
@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 600px) {
  .profile-container { padding: 16px; }
  .form-grid { grid-template-columns: 1fr; }
  .form-field.full-width { grid-column: auto; }
}
</style>