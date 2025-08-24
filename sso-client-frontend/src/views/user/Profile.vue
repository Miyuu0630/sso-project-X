<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>个人资料</span>
        </div>
      </template>

      <div class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <el-avatar :size="100" :src="form.avatar">
            {{ form.realName?.charAt(0) || form.username?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="avatar-actions">
            <el-button type="primary" size="small" @click="uploadAvatar">
              <el-icon><Camera /></el-icon>
              更换头像
            </el-button>
          </div>
        </div>

        <!-- 基本信息表单 -->
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="profile-form"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="form.username" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="form.realName" placeholder="请输入真实姓名" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="form.gender">
                  <el-radio :value="0">未知</el-radio>
                  <el-radio :value="1">男</el-radio>
                  <el-radio :value="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="用户类型">
                <el-tag :type="getRoleTagType()">{{ getRoleName() }}</el-tag>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="个人简介" prop="remark">
            <el-input
              v-model="form.remark"
              type="textarea"
              :rows="4"
              placeholder="请输入个人简介"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="saveProfile" :loading="saving">
              <el-icon><Check /></el-icon>
              保存修改
            </el-button>
            <el-button @click="resetForm">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 账号信息卡片 -->
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <el-icon><InfoFilled /></el-icon>
          <span>账号信息</span>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">
          {{ userInfo?.id }}
        </el-descriptions-item>
        <el-descriptions-item label="账号状态">
          <el-tag :type="userInfo?.status === '1' ? 'success' : 'danger'">
            {{ userInfo?.status === '1' ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ formatDate(userInfo?.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="最后登录">
          {{ formatDate(userInfo?.lastLoginTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="角色权限" :span="2">
          <div class="roles-permissions">
            <div class="roles">
              <span class="label">角色：</span>
              <el-tag
                v-for="role in userInfo?.roles"
                :key="role"
                type="info"
                size="small"
                style="margin-right: 8px;"
              >
                {{ getRoleDisplayName(role) }}
              </el-tag>
            </div>
            <div class="permissions">
              <span class="label">权限数量：</span>
              <el-tag type="success" size="small">
                {{ userInfo?.permissions?.length || 0 }} 个权限
              </el-tag>
            </div>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Camera, Check, Refresh, InfoFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'

const authStore = useAuthStore()
const formRef = ref()
const saving = ref(false)

// 表单数据
const form = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  gender: 0,
  avatar: '',
  remark: ''
})

// 表单验证规则
const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 计算属性
const userInfo = computed(() => authStore.userInfo)

// 角色名称映射
const roleNameMap = {
  'ADMIN': '系统管理员',
  'AIRLINE_USER': '航司用户',
  'ENTERPRISE_USER': '企业用户',
  'PERSONAL_USER': '个人用户'
}

// 方法
const getRoleName = () => {
  return roleNameMap[authStore.primaryRole] || '未知角色'
}

const getRoleTagType = () => {
  const typeMap = {
    'ADMIN': 'danger',
    'AIRLINE_USER': 'warning',
    'ENTERPRISE_USER': 'success',
    'PERSONAL_USER': 'info'
  }
  return typeMap[authStore.primaryRole] || 'info'
}

const getRoleDisplayName = (role) => {
  return roleNameMap[role] || role
}

const formatDate = (dateString) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleString('zh-CN')
}

const loadUserProfile = () => {
  if (userInfo.value) {
    Object.assign(form, {
      username: userInfo.value.username || '',
      realName: userInfo.value.realName || '',
      phone: userInfo.value.phone || '',
      email: userInfo.value.email || '',
      gender: userInfo.value.gender || 0,
      avatar: userInfo.value.avatar || '',
      remark: userInfo.value.remark || ''
    })
  }
}

const saveProfile = async () => {
  try {
    await formRef.value.validate()
    saving.value = true

    const response = await request.put('/api/user/profile', form)
    if (response.data.code === 200) {
      ElMessage.success('个人资料保存成功')
      // 刷新用户信息
      await authStore.fetchUserInfo()
      loadUserProfile()
    } else {
      ElMessage.error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存个人资料失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  loadUserProfile()
  ElMessage.info('已重置为原始数据')
}

const uploadAvatar = () => {
  ElMessage.info('头像上传功能开发中...')
}

// 生命周期
onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card, .info-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.avatar-actions {
  display: flex;
  gap: 12px;
}

.profile-form {
  flex: 1;
}

.roles-permissions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.roles, .permissions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  font-weight: 500;
  color: #666;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 10px;
  }
  
  .profile-content {
    gap: 20px;
  }
}
</style>
