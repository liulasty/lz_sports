<template>
  <el-form
    ref="formRef"
    :model="model"
    :rules="rules"
    label-position="top"
    class="profile-edit-form embedded-profile-form"
  >
    <div class="profile-identity-cards">
      <section class="identity-card">
        <header class="identity-card-head">
          <span class="identity-card-icon">01</span>
          <div>
            <h5 class="identity-card-title">基本信息</h5>
            <p class="identity-card-sub">用于赛事档案与联系</p>
          </div>
        </header>
        <div class="identity-card-body profile-form-grid">
          <el-form-item label="真实姓名" prop="name">
            <el-input v-model="model.name" placeholder="与证件一致" maxlength="32" />
          </el-form-item>
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="model.gender" class="gender-radio-group">
              <el-radio-button value="男">男</el-radio-button>
              <el-radio-button value="女">女</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="联系方式" prop="contact" class="full-width">
            <el-input v-model="model.contact" placeholder="手机号或常用联系方式" maxlength="20" />
          </el-form-item>
        </div>
      </section>

      <section class="identity-card">
        <header class="identity-card-head">
          <span class="identity-card-icon">02</span>
          <div>
            <h5 class="identity-card-title">{{ deptMeta.title }}</h5>
            <p class="identity-card-sub">{{ deptMeta.levels.join(' → ') }}</p>
          </div>
        </header>
        <div class="identity-card-body">
          <p class="dept-cascader-hint">{{ deptMeta.hint }}</p>
          <el-form-item label="选择归属" prop="deptId" class="dept-form-item">
            <el-cascader
              v-model="model.deptId"
              :options="departmentTree"
              :props="deptCascaderProps"
              filterable
              clearable
              :placeholder="deptMeta.placeholder"
              class="dept-cascader"
              style="width: 100%"
            />
          </el-form-item>
          <div v-if="selectedDeptPath.length" class="dept-path-preview">
            <span class="dept-path-label">已选择</span>
            <span
              v-for="(segment, index) in selectedDeptPath"
              :key="`${segment}-${index}`"
              class="dept-path-segment"
            >{{ segment }}</span>
          </div>
        </div>
      </section>
    </div>

    <div class="profile-form-actions">
      <slot name="actions" :validate="validate" />
    </div>
  </el-form>
</template>

<script setup>
import { computed, ref } from 'vue'
import { getDeptCascaderLabels, findDeptPathLabels } from '@/utils/departmentTree'

const props = defineProps({
  model: { type: Object, required: true },
  rules: { type: Object, required: true },
  departmentTree: { type: Array, default: () => [] },
  orgMode: { type: String, default: 'UNIVERSITY' }
})

const formRef = ref(null)

const deptMeta = computed(() => getDeptCascaderLabels(props.orgMode))

const deptCascaderProps = {
  value: 'value',
  label: 'label',
  emitPath: false,
  checkStrictly: false
}

const selectedDeptPath = computed(() =>
  findDeptPathLabels(props.departmentTree, props.model.deptId)
)

const validate = () => {
  if (!formRef.value) return Promise.resolve(false)
  return new Promise((resolve) => {
    formRef.value.validate((valid) => resolve(valid))
  })
}

defineExpose({ validate, formRef })
</script>

<style scoped>
.profile-identity-cards {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.identity-card {
  border: 1px solid var(--pc-border);
  border-radius: 16px;
  background: var(--pc-card-alt);
  overflow: hidden;
}

.identity-card-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border-bottom: 1px solid var(--pc-border);
  background: rgba(255, 107, 53, 0.04);
}

.identity-card-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  color: var(--c-orange);
  background: var(--c-orange-dim);
  border: 1px solid rgba(255, 107, 53, 0.25);
}

.identity-card-title {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 700;
  color: var(--pc-text);
}

.identity-card-sub {
  margin: 0;
  font-size: 12px;
  color: var(--pc-text-muted);
}

.identity-card-body {
  padding: 20px;
}

.profile-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 20px;
}

.profile-form-grid .full-width {
  grid-column: 1 / -1;
}

.dept-cascader-hint {
  margin: 0 0 14px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--pc-text-sub);
}

.dept-path-preview {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  padding: 12px 14px;
  border-radius: 10px;
  background: rgba(255, 107, 53, 0.06);
  border: 1px dashed rgba(255, 107, 53, 0.28);
}

.dept-path-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--pc-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.dept-path-segment {
  font-size: 13px;
  font-weight: 600;
  color: var(--pc-text);
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--pc-card);
  border: 1px solid var(--pc-border);
}

.profile-form-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 20px;
}

:deep(.embedded-profile-form .el-form-item__label) {
  color: var(--pc-text-sub);
  font-weight: 600;
}

:deep(.gender-radio-group .el-radio-button__inner) {
  min-width: 72px;
}

:deep(.dept-cascader .el-input__wrapper) {
  min-height: 42px;
}

@media (max-width: 720px) {
  .profile-form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
