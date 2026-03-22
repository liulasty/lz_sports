<template>
  <el-dialog
    :model-value="visible"
    title="分配赛事管理员"
    width="650px"
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
  >
    <div class="manager-container">
      <div class="add-section">
        <el-select
          v-model="selectedUserIds"
          multiple
          filterable
          remote
          reserve-keyword
          placeholder="搜索用户名或姓名添加为管理员"
          :remote-method="searchUsers"
          :loading="searching"
          style="flex: 1; margin-right: 10px;"
        >
          <el-option
            v-for="user in searchResults"
            :key="user.id"
            :label="`${user.name || user.username} (${user.email || '无邮箱'})`"
            :value="user.id"
            :disabled="isAlreadyAdmin(user.id)"
          />
        </el-select>
        <el-button type="primary" @click="handleAddAdmins" :disabled="!selectedUserIds.length">
          添加
        </el-button>
      </div>

      <el-table :data="adminList" v-loading="loading" style="width: 100%; margin-top: 20px;" border>
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="name" label="姓名">
          <template #default="scope">
            {{ scope.row.name ?? '未填写' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="scope">
            <el-button 
              type="danger" 
              link 
              @click="handleRemoveAdmin(scope.row)"
              :disabled="adminList.length <= 1"
              :title="adminList.length <= 1 ? '至少需保留一名管理员' : ''"
            >移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getEventAdmins, addEventAdmins, removeEventAdmin } from '@/api/admin'
import { getUserList } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

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

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  eventId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['update:visible', 'saved'])

const loading = ref(false)
const adminList = ref<UserData[]>([])
const searching = ref(false)
const searchResults = ref<UserData[]>([])
const selectedUserIds = ref<number[]>([])

const fetchAdmins = async () => {
  if (!props.eventId) return
  loading.value = true
  try {
    const res = await getEventAdmins(props.eventId)
    if (res.code === 200) {
      adminList.value = res.data || []
    }
  } catch (error) {
    console.error('获取管理员失败', error)
  } finally {
    loading.value = false
  }
}

const searchUsers = async (query: string) => {
  if (query) {
    searching.value = true
    try {
      const res = await getUserList({ username: query, currentPage: 1, pageSize: 20 })
      if (res.code === 200) {
        searchResults.value = res.data.records || res.data.rows || []
      }
    } catch (error) {
      console.error('搜索用户失败', error)
    } finally {
      searching.value = false
    }
  } else {
    searchResults.value = []
  }
}

const isAlreadyAdmin = (userId: number) => {
  return adminList.value.some(admin => admin.id === userId)
}

const handleAddAdmins = async () => {
  if (!selectedUserIds.value.length || !props.eventId) return
  try {
    const res = await addEventAdmins(props.eventId, selectedUserIds.value)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      selectedUserIds.value = []
      searchResults.value = []
      fetchAdmins()
      emit('saved')
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  } catch (error) {
    console.error(error)
  }
}

const handleRemoveAdmin = (row: UserData) => {
  if (adminList.value.length <= 1) {
    ElMessage.warning('至少需要保留一名赛事管理员')
    return
  }
  ElMessageBox.confirm(`确认移除管理员 ${row.username || row.name} 吗?`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await removeEventAdmin(props.eventId, row.id)
      if (res.code === 200) {
        ElMessage.success('移除成功')
        fetchAdmins()
        emit('saved')
      } else {
        ElMessage.error(res.msg || '移除失败')
      }
    } catch (error) {
      console.error(error)
    }
  })
}

const handleClose = () => {
  selectedUserIds.value = []
  searchResults.value = []
}

watch(() => props.visible, (newVal) => {
  if (newVal && props.eventId) {
    fetchAdmins()
  }
})
</script>

<style scoped>
.manager-container {
  padding: 10px 0;
}
.add-section {
  display: flex;
  align-items: center;
}
</style>
