<template>
  <div class="pm-container">

    <!-- ── Top Bar ── -->
    <div class="pm-topbar">
      <div class="pm-title">
        <span class="title-bar"></span>
        <h2>项目管理</h2>
        <span class="count-chip" v-if="total">{{ total }} 个项目</span>
      </div>
      <div class="pm-actions">
        <div class="search-wrap">
          <svg class="search-icon" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
          </svg>
          <input
              v-model="queryParams.name"
              class="search-input"
              placeholder="搜索项目名称…"
              @keyup.enter="handleQuery"
          />
        </div>
        <button class="btn-query" @click="handleQuery">查询</button>
        <button class="btn-add" @click="handleAdd">
          <svg viewBox="0 0 20 20" fill="currentColor" width="14" height="14">
            <path fill-rule="evenodd" d="M10 3a1 1 0 011 1v5h5a1 1 0 110 2h-5v5a1 1 0 11-2 0v-5H4a1 1 0 110-2h5V4a1 1 0 011-1z" clip-rule="evenodd"/>
          </svg>
          新增项目
        </button>
      </div>
    </div>

    <!-- ── Column Header ── -->
    <div class="table-header">
      <span class="col-name">项目名称</span>
      <span class="col-event">所属赛事</span>
      <span class="col-time">比赛时间</span>
      <span class="col-location">地点</span>
      <span class="col-limit">限制</span>
      <span class="col-quota">报名情况</span>
      <span class="col-ops">操作</span>
    </div>

    <!-- ── Loading skeleton ── -->
    <div v-if="loading" class="skeleton-list">
      <div class="skeleton-row" v-for="i in 5" :key="i">
        <div class="skel skel-long"></div>
        <div class="skel skel-mid"></div>
        <div class="skel skel-mid"></div>
        <div class="skel skel-short"></div>
        <div class="skel skel-short"></div>
        <div class="skel skel-mid"></div>
        <div class="skel skel-short"></div>
      </div>
    </div>

    <!-- ── Row Cards ── -->
    <transition-group name="row-fade" tag="div" class="row-list" v-else>
      <div
          class="row-card"
          v-for="(item, index) in projectList"
          :key="item.id"
          :style="{ animationDelay: index * 40 + 'ms' }"
      >
        <!-- 项目名称 -->
        <div class="col-name">
          <div class="item-name-wrap">
            <span class="item-category-dot" :class="item.category === 'STANDARD' ? 'dot-std' : 'dot-custom'"></span>
            <span class="item-name">{{ item.itemName }}</span>
          </div>
          <span class="item-category-label">{{ item.category === 'STANDARD' ? '标准' : '自定义' }}</span>
        </div>

        <!-- 所属赛事 -->
        <div class="col-event">
          <span class="event-tag">{{ item.eventName }}</span>
        </div>

        <!-- 时间 -->
        <div class="col-time">
          <div class="time-range">
            <span class="time-main">{{ formatDateShort(item.startTime) }}</span>
            <span class="time-sep">→</span>
            <span class="time-main">{{ formatDateShort(item.endTime) }}</span>
          </div>
        </div>

        <!-- 地点 -->
        <div class="col-location">
          <span class="location-text">{{ item.grade || '—' }}</span>
        </div>

        <!-- 限制 -->
        <div class="col-limit">
          <span v-if="item.limitation" class="limit-badge" :class="getLimitClass(item.limitation)">
            {{ getLimitLabel(item.limitation) }}
          </span>
          <span v-else class="limit-none">不限</span>
        </div>

        <!-- 报名进度 -->
        <div class="col-quota">
          <div class="quota-info">
            <span class="quota-num">{{ item.attendance }}</span>
            <span class="quota-sep">/</span>
            <span class="quota-max">{{ item.maxAttendance }}</span>
          </div>
          <div class="progress-bar">
            <div
                class="progress-fill"
                :class="getProgressClass(item)"
                :style="{ width: getProgressPct(item) + '%' }"
            ></div>
          </div>
          <span class="quota-pct">{{ getProgressPct(item) }}%</span>
        </div>

        <!-- 操作 -->
        <div class="col-ops">
          <button class="icon-btn btn-edit" @click="handleEdit(item)" title="编辑">
            <svg viewBox="0 0 20 20" fill="currentColor" width="15" height="15">
              <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"/>
            </svg>
          </button>
          <button class="icon-btn btn-delete" @click="handleDelete(item)" title="删除">
            <svg viewBox="0 0 20 20" fill="currentColor" width="15" height="15">
              <path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd"/>
            </svg>
          </button>
        </div>
      </div>
    </transition-group>

    <!-- Empty -->
    <div v-if="!loading && projectList.length === 0" class="empty-state">
      <div class="empty-icon">📋</div>
      <p>暂无项目数据</p>
    </div>

    <!-- ── Pagination ── -->
    <div class="pm-pagination">
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

    <!-- ── Dialog ── -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="560px"
        class="pm-dialog"
        destroy-on-close
    >
      <el-form :model="form" label-width="0" class="pm-form">
        <div class="form-grid">
          <div class="form-field full">
            <div class="field-label">项目名称 <span class="req">*</span></div>
            <el-input v-model="form.name" placeholder="请输入项目名称" class="fi" />
          </div>

          <div class="form-field full">
            <div class="field-label">所属赛事 <span class="req">*</span></div>
            <el-select v-model="form.event" placeholder="请选择赛事" filterable class="fi" style="width:100%">
              <el-option
                  v-for="item in eventTypes"
                  :key="item.eventId"
                  :label="item.eventName"
                  :value="item.eventId"
              />
            </el-select>
          </div>

          <div class="form-field full">
            <div class="field-label">比赛时间</div>
            <el-date-picker
                v-model="form.dateRange"
                type="datetimerange"
                range-separator="→"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                class="fi"
                style="width:100%"
            />
          </div>

          <div class="form-field">
            <div class="field-label">比赛地点</div>
            <el-input v-model="form.grade" placeholder="输入地点" class="fi" />
          </div>

          <div class="form-field">
            <div class="field-label">最大人数</div>
            <el-input-number v-model="form.maxAttendance" :min="1" controls-position="right" class="fi" style="width:100%" />
          </div>

          <div class="form-field">
            <div class="field-label">性别限制</div>
            <div class="toggle-group">
              <button
                  v-for="opt in limitOptions"
                  :key="opt.value"
                  class="toggle-btn"
                  :class="{ active: form.limitation === opt.value }"
                  type="button"
                  @click="form.limitation = opt.value"
              >{{ opt.label }}</button>
            </div>
          </div>

          <div class="form-field">
            <div class="field-label">项目类别</div>
            <div class="toggle-group">
              <button
                  v-for="opt in categoryOptions"
                  :key="opt.value"
                  class="toggle-btn"
                  :class="{ active: form.category === opt.value }"
                  type="button"
                  @click="form.category = opt.value"
              >{{ opt.label }}</button>
            </div>
          </div>

          <div class="form-field full">
            <div class="field-label">图片链接</div>
            <el-input
                v-model="form.imageUrlInput"
                type="textarea"
                :rows="3"
                placeholder="每行一个图片 URL"
                class="fi"
            />
          </div>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="dialogVisible = false">取消</button>
          <button class="btn-confirm" @click="submitForm">
            {{ isEdit ? '保存修改' : '创建项目' }}
          </button>
        </div>
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

const limitOptions   = [{ label: '不限', value: 'ALL' }, { label: '男', value: 'MALE' }, { label: '女', value: 'FEMALE' }]
const categoryOptions = [{ label: '自定义', value: 'CUSTOM' }, { label: '标准项目', value: 'STANDARD' }]

const queryParams = reactive({ currentPage: 1, pageSize: 5, name: '' })

const form = reactive({
  id: '', name: '', event: '', grade: '', limitation: 'ALL',
  category: 'CUSTOM', maxAttendance: 50, dateRange: [], imageUrlInput: ''
})

const resetForm = () => {
  Object.assign(form, { id: '', name: '', event: '', grade: '', limitation: 'ALL', category: 'CUSTOM', maxAttendance: 50, dateRange: [], imageUrlInput: '' })
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getProjectList(queryParams)
    if (res.code === 200) { projectList.value = res.data.records; total.value = res.data.total }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const getEventOptions = async () => {
  try {
    const res = await getEventTypes()
    if (res.code === 200) eventTypes.value = res.data
  } catch (e) { console.error(e) }
}

const handleQuery       = () => { queryParams.currentPage = 1; getList() }
const handleSizeChange  = (v) => { queryParams.pageSize = v; getList() }
const handleCurrentChange = (v) => { queryParams.currentPage = v; getList() }

const handleAdd = () => {
  dialogTitle.value = '新增项目'; isEdit.value = false; resetForm(); dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑项目'; isEdit.value = true
  Object.assign(form, {
    id: row.id, name: row.itemName, event: row.eventId, grade: row.grade || '',
    limitation: row.limitation || 'ALL', category: row.category || 'CUSTOM',
    maxAttendance: row.maxAttendance, dateRange: [row.startTime, row.endTime], imageUrlInput: ''
  })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除项目「${row.itemName}」？`, '删除确认', {
    confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    const res = await deleteProject(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); getList() }
  }).catch(() => {})
}

const submitForm = async () => {
  const data = {
    name: form.name, event: form.event, grade: form.grade, limitation: form.limitation,
    category: form.category, maxAttendance: form.maxAttendance, date: form.dateRange,
    addImage: form.imageUrlInput ? form.imageUrlInput.split('\n').filter(s => s.trim()) : []
  }
  try {
    const res = isEdit.value ? await updateProject(form.id, data) : await addProject(data)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false; getList()
    }
  } catch (e) { console.error(e) }
}

const formatDateShort = (d) => {
  if (!d) return '—'
  const dt = new Date(d)
  return `${dt.getMonth()+1}/${dt.getDate()} ${String(dt.getHours()).padStart(2,'0')}:${String(dt.getMinutes()).padStart(2,'0')}`
}

const getProgressPct = (item) => {
  if (!item.maxAttendance) return 0
  return Math.min(100, Math.round((item.attendance / item.maxAttendance) * 100))
}

const getProgressClass = (item) => {
  const pct = getProgressPct(item)
  if (pct >= 90) return 'prog-full'
  if (pct >= 60) return 'prog-mid'
  return 'prog-low'
}

const getLimitLabel = (v) => ({ ALL: '不限', MALE: '男', FEMALE: '女' }[v] || v)
const getLimitClass = (v) => ({ ALL: '', MALE: 'limit-male', FEMALE: 'limit-female' }[v] || '')

onMounted(() => { getList(); getEventOptions() })
</script>

<style scoped>
/* ── Variables & Base ── */
.pm-container {
  padding: 24px;
  font-family: 'Noto Sans SC', sans-serif;
  min-height: 100%;
}

/* ── Top Bar ── */
.pm-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 12px;
  flex-wrap: wrap;
}

.pm-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-bar {
  display: block;
  width: 4px;
  height: 20px;
  background: var(--accent, #FF6B35);
  border-radius: 2px;
}

.pm-title h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary, #1a1a2e);
}

.count-chip {
  font-size: 11px;
  color: var(--accent, #FF6B35);
  background: rgba(255, 107, 53, 0.1);
  border: 1px solid rgba(255, 107, 53, 0.22);
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 600;
}

.pm-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Search */
.search-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 10px;
  width: 15px;
  height: 15px;
  color: var(--text-secondary, var(--el-text-color-secondary));
  pointer-events: none;
}

.search-input {
  height: 36px;
  width: 220px;
  padding: 0 12px 0 34px;
  background: var(--bg-card, var(--el-bg-color));
  border: 1px solid var(--border, rgba(0,0,0,0.1));
  border-radius: 8px;
  font-size: 13px;
  font-family: inherit;
  color: var(--text-primary, #1a1a2e);
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search-input::placeholder { color: var(--text-secondary, var(--el-text-color-placeholder)); }
.search-input:focus {
  border-color: var(--accent, #FF6B35);
  box-shadow: 0 0 0 2px rgba(255,107,53,0.12);
}

.btn-query {
  height: 36px;
  padding: 0 16px;
  background: var(--bg-card, var(--el-bg-color));
  border: 1px solid var(--border, rgba(0,0,0,0.1));
  border-radius: 8px;
  color: var(--text-primary, #1a1a2e);
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}
.btn-query:hover { border-color: var(--accent, #FF6B35); color: var(--accent, #FF6B35); }

.btn-add {
  height: 36px;
  padding: 0 16px;
  background: var(--accent, #FF6B35);
  border: none;
  border-radius: 8px;
  color: var(--el-bg-color);
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: opacity 0.18s, transform 0.18s;
  box-shadow: 0 3px 12px rgba(255,107,53,0.3);
}
.btn-add:hover { opacity: 0.88; transform: translateY(-1px); }

/* ── Column Header ── */
.table-header,
.row-card {
  display: grid;
  grid-template-columns: 2fr 2fr 2fr 1.2fr 0.8fr 2fr 100px;
  gap: 0 12px;
  align-items: center;
  padding: 0 20px;
}

.table-header {
  padding-top: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border, rgba(0,0,0,0.07));
  margin-bottom: 4px;
}

.table-header > span {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: var(--text-secondary, var(--el-text-color-placeholder));
  text-transform: uppercase;
}

/* ── Row Cards ── */
.row-list { display: flex; flex-direction: column; gap: 6px; }

.row-card {
  background: var(--bg-card, var(--el-bg-color));
  border: 1px solid var(--border, rgba(0,0,0,0.06));
  border-radius: 12px;
  padding-top: 14px;
  padding-bottom: 14px;
  cursor: default;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
  animation: rowIn 0.35s cubic-bezier(0.22,1,0.36,1) both;
}

.row-card:hover {
  border-color: rgba(255,107,53,0.25);
  box-shadow: 0 4px 20px rgba(0,0,0,0.07);
  transform: translateY(-1px);
}

@keyframes rowIn {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ── Cell Styles ── */

/* Name */
.item-name-wrap {
  display: flex;
  align-items: center;
  gap: 7px;
}
.item-category-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot-custom { background: var(--accent, #FF6B35); }
.dot-std    { background: #6366f1; }

.item-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary, #1a1a2e);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-category-label {
  margin-top: 3px;
  font-size: 10px;
  color: var(--text-secondary, var(--el-text-color-placeholder));
  letter-spacing: 0.06em;
}

/* Event tag */
.event-tag {
  display: inline-block;
  font-size: 11px;
  background: rgba(99,102,241,0.08);
  color: #6366f1;
  border: 1px solid rgba(99,102,241,0.2);
  padding: 3px 8px;
  border-radius: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

/* Time */
.time-range {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}
.time-main {
  font-size: 12px;
  color: var(--text-primary, var(--el-text-color-regular));
  font-variant-numeric: tabular-nums;
}
.time-sep {
  font-size: 11px;
  color: var(--accent, #FF6B35);
  font-weight: 700;
}

/* Location */
.location-text {
  font-size: 12px;
  color: var(--text-secondary, var(--el-text-color-secondary));
}

/* Limit badge */
.limit-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 20px;
}
.limit-male   { background: rgba(59,130,246,0.1); color: #3b82f6; border: 1px solid rgba(59,130,246,0.22); }
.limit-female { background: rgba(236,72,153,0.1); color: #ec4899; border: 1px solid rgba(236,72,153,0.22); }
.limit-none   { font-size: 12px; color: var(--text-secondary, var(--el-text-color-placeholder)); }

/* Quota / Progress */
.col-quota { display: flex; flex-direction: column; gap: 4px; }

.quota-info {
  display: flex;
  align-items: baseline;
  gap: 2px;
  font-variant-numeric: tabular-nums;
}
.quota-num  { font-size: 14px; font-weight: 700; color: var(--text-primary, #1a1a2e); }
.quota-sep  { font-size: 11px; color: var(--text-secondary, var(--el-text-color-placeholder)); }
.quota-max  { font-size: 12px; color: var(--text-secondary, var(--el-text-color-placeholder)); }
.quota-pct  { font-size: 10px; color: var(--text-secondary, var(--el-text-color-placeholder)); }

.progress-bar {
  height: 4px;
  background: var(--border, rgba(0,0,0,0.07));
  border-radius: 4px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s cubic-bezier(0.34,1.56,0.64,1);
}
.prog-low  { background: linear-gradient(90deg, #10b981, #34d399); }
.prog-mid  { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.prog-full { background: linear-gradient(90deg, #ef4444, #f87171); }

/* Ops */
.col-ops {
  display: flex;
  gap: 6px;
  justify-content: flex-end;
}

.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid var(--border, rgba(0,0,0,0.09));
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.18s, border-color 0.18s, transform 0.15s;
  color: var(--text-secondary, var(--el-text-color-secondary));
}

.btn-edit:hover {
  background: rgba(99,102,241,0.1);
  border-color: rgba(99,102,241,0.35);
  color: #6366f1;
  transform: scale(1.1);
}

.btn-delete:hover {
  background: rgba(239,68,68,0.1);
  border-color: rgba(239,68,68,0.35);
  color: #ef4444;
  transform: scale(1.1);
}

/* ── Skeleton ── */
.skeleton-list { display: flex; flex-direction: column; gap: 6px; }
.skeleton-row {
  display: grid;
  grid-template-columns: 2fr 2fr 2fr 1.2fr 0.8fr 2fr 100px;
  gap: 12px;
  align-items: center;
  padding: 14px 20px;
  background: var(--bg-card, var(--el-bg-color));
  border: 1px solid var(--border, rgba(0,0,0,0.06));
  border-radius: 12px;
}
.skel {
  height: 14px;
  border-radius: 6px;
  background: linear-gradient(90deg, var(--border, var(--el-border-color-lighter)) 25%, rgba(255,255,255,0.5) 50%, var(--border, var(--el-border-color-lighter)) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
.skel-long  { width: 90%; }
.skel-mid   { width: 70%; }
.skel-short { width: 50%; }

@keyframes shimmer {
  from { background-position: 200% 0; }
  to   { background-position: -200% 0; }
}

/* ── Empty ── */
.empty-state {
  text-align: center;
  padding: 60px;
  color: var(--text-secondary, var(--el-text-color-placeholder));
}
.empty-icon { font-size: 40px; margin-bottom: 10px; }

/* ── Pagination ── */
.pm-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* ── Dialog ── */
:deep(.pm-dialog .el-dialog) {
  background: var(--bg-card, var(--el-bg-color));
  border-radius: 16px;
  border: 1px solid var(--border, rgba(0,0,0,0.08));
}
:deep(.pm-dialog .el-dialog__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--border, rgba(0,0,0,0.07));
}
:deep(.pm-dialog .el-dialog__title) {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary, #1a1a2e);
}
:deep(.pm-dialog .el-dialog__body) {
  padding: 20px 24px;
}
:deep(.pm-dialog .el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid var(--border, rgba(0,0,0,0.07));
}

.pm-form :deep(.el-form-item) { margin-bottom: 0; }

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 16px;
}
.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-field.full { grid-column: 1 / -1; }

.field-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary, var(--el-text-color-secondary));
  letter-spacing: 0.04em;
}
.req { color: var(--accent, #FF6B35); }

:deep(.fi .el-input__wrapper),
:deep(.fi.el-input .el-input__wrapper) {
  background: rgba(0,0,0,0.03) !important;
  border: 1px solid var(--border, rgba(0,0,0,0.1)) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
  height: 38px;
}
:deep(.fi .el-input__wrapper:hover),
:deep(.fi .el-input__wrapper.is-focus) {
  border-color: var(--accent, #FF6B35) !important;
  box-shadow: 0 0 0 2px rgba(255,107,53,0.1) !important;
}
:deep(.fi .el-input__inner) {
  color: var(--text-primary, #1a1a2e) !important;
  font-size: 13px;
}
:deep(.fi .el-textarea__inner) {
  background: rgba(0,0,0,0.03) !important;
  border: 1px solid var(--border, rgba(0,0,0,0.1)) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
  color: var(--text-primary, #1a1a2e) !important;
  font-size: 13px;
  resize: none;
}
:deep(.fi .el-textarea__inner:focus) {
  border-color: var(--accent, #FF6B35) !important;
}

/* Toggle groups */
.toggle-group {
  display: flex;
  gap: 8px;
}
.toggle-btn {
  flex: 1;
  height: 36px;
  border: 1px solid var(--border, rgba(0,0,0,0.1));
  border-radius: 8px;
  background: transparent;
  color: var(--text-secondary, var(--el-text-color-secondary));
  font-size: 12px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.18s;
}
.toggle-btn:hover { border-color: rgba(255,107,53,0.35); color: var(--text-primary, #1a1a2e); }
.toggle-btn.active {
  background: rgba(255,107,53,0.1);
  border-color: var(--accent, #FF6B35);
  color: var(--accent, #FF6B35);
  font-weight: 600;
}

/* Dialog footer buttons */
.dialog-footer { display: flex; gap: 10px; justify-content: flex-end; }

.btn-cancel {
  height: 38px; padding: 0 20px;
  background: transparent;
  border: 1px solid var(--border, rgba(0,0,0,0.1));
  border-radius: 8px;
  color: var(--text-secondary, var(--el-text-color-secondary));
  font-size: 13px; font-family: inherit;
  cursor: pointer; transition: all 0.18s;
}
.btn-cancel:hover { border-color: rgba(0,0,0,0.2); color: var(--text-primary, #1a1a2e); }

.btn-confirm {
  height: 38px; padding: 0 24px;
  background: linear-gradient(135deg, #FF6B35, #e0541e);
  border: none; border-radius: 8px;
  color: var(--el-bg-color); font-size: 13px; font-weight: 600; font-family: inherit;
  cursor: pointer;
  box-shadow: 0 3px 12px rgba(255,107,53,0.3);
  transition: transform 0.18s, box-shadow 0.18s;
}
.btn-confirm:hover { transform: translateY(-1px); box-shadow: 0 6px 18px rgba(255,107,53,0.4); }

/* ── Row animation ── */
.row-fade-enter-active { transition: all 0.3s ease; }
.row-fade-leave-active { transition: all 0.2s ease; }
.row-fade-enter-from  { opacity: 0; transform: translateY(6px); }
.row-fade-leave-to    { opacity: 0; transform: translateX(-6px); }

/* ── Dark mode ── */
html.dark .search-input {
  background: rgba(255,255,255,0.05);
  border-color: rgba(255,255,255,0.09);
  color: var(--el-bg-color-page);
}
html.dark .search-input:focus {
  border-color: #FF6B35;
}
html.dark .skel {
  background: linear-gradient(90deg, rgba(255,255,255,0.05) 25%, rgba(255,255,255,0.09) 50%, rgba(255,255,255,0.05) 75%);
  background-size: 200% 100%;
}
</style>

<style>
/* Global: dark mode dialog + inputs */
html.dark .pm-dialog .el-dialog {
  background: #161921 !important;
}
html.dark .pm-dialog .el-input__wrapper {
  background: rgba(255,255,255,0.05) !important;
  border-color: rgba(255,255,255,0.09) !important;
}
html.dark .pm-dialog .el-input__inner,
html.dark .pm-dialog .el-textarea__inner {
  color: #f0f2f8 !important;
  background: transparent !important;
}
html.dark .pm-dialog .el-select .el-input__wrapper {
  background: rgba(255,255,255,0.05) !important;
}
html.dark .pm-dialog .el-input-number .el-input__wrapper {
  background: rgba(255,255,255,0.05) !important;
}
</style>