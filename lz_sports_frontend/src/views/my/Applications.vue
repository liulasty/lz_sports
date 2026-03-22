<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的运动员申请</span>
        </div>
      </template>
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else>
        <el-result v-if="!hasApplied" icon="info" title="未申请">
          <template #sub-title>
            您目前还未申请成为运动员。如需参赛，请先提交申请。
          </template>
          <template #extra>
            <el-button type="primary" @click="$router.push('/profile')">前往个人中心申请</el-button>
          </template>
        </el-result>
        <div v-else class="status-box">
          <el-alert
            :title="`当前申请状态：${applicationStatus}`"
            :type="getStatusType(applicationStatus)"
            show-icon
            :closable="false"
            class="status-alert"
          />
          <div class="tips" v-if="applicationStatus === 'PENDING'">
            您的申请正在由管理员审核中，请耐心等待。审核结果将通过站内信通知您。
          </div>
          <div class="tips" v-else-if="applicationStatus === 'APPROVED'">
            恭喜！您已成功通过运动员资格审核，现在可以前往赛事大厅报名参赛了。
            <div style="margin-top: 20px;">
              <el-button type="primary" @click="$router.push('/event')">去赛事大厅</el-button>
            </div>
          </div>
          <div class="tips" v-else-if="applicationStatus === 'REJECTED'">
            抱歉，您的申请未通过审核。您可以前往个人资料页修改信息后重新提交申请。
            <div style="margin-top: 20px;">
              <el-button type="primary" @click="$router.push('/profile')">修改申请资料</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAthleteApply } from '@/api/athlete'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(true)
const hasApplied = ref(false)
const applicationStatus = ref('')

const checkApplication = async () => {
  try {
    const userId = userStore.userInfo?.id || userStore.userInfo?.userId
    if (!userId) return
    const res = await getAthleteApply(userId)
    if (res.code === 200 && res.data) {
      hasApplied.value = true
      applicationStatus.value = res.data.athleteState || res.data.status
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  if (status === 'APPROVED') return 'success'
  if (status === 'PENDING') return 'warning'
  return 'error'
}

onMounted(() => {
  checkApplication()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.status-box {
  padding: 40px 20px;
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
}
.status-alert {
  margin-bottom: 20px;
}
.tips {
  color: var(--el-text-color-regular);
  line-height: 1.6;
}
</style>
