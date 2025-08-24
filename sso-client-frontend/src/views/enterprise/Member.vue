<template>
  <div class="enterprise-member">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><UserFilled /></el-icon>
          <span>成员管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="showInviteDialog = true">
              <el-icon><Plus /></el-icon>
              邀请成员
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="姓名">
            <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
              <el-option label="管理员" value="admin" />
              <el-option label="项目经理" value="manager" />
              <el-option label="普通成员" value="member" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadMembers">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 成员表格 -->
      <el-table :data="members" v-loading="loading" style="width: 100%">
        <el-table-column prop="avatar" label="头像" width="80">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar">
              {{ scope.row.name.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="joinTime" label="加入时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'warning'">
              {{ scope.row.status === 'active' ? '正常' : '待激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editMember(scope.row)">
              编辑
            </el-button>
            <el-button 
              v-if="scope.row.status === 'pending'"
              type="success" 
              size="small" 
              @click="resendInvite(scope.row)"
            >
              重发邀请
            </el-button>
            <el-button type="danger" size="small" @click="removeMember(scope.row)">
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadMembers"
          @current-change="loadMembers"
        />
      </div>
    </el-card>

    <!-- 邀请成员对话框 -->
    <el-dialog v-model="showInviteDialog" title="邀请成员" width="500px">
      <el-form
        ref="inviteFormRef"
        :model="inviteForm"
        :rules="inviteRules"
        label-width="100px"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="inviteForm.email" placeholder="请输入成员邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="inviteForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通成员" value="member" />
            <el-option label="项目经理" value="manager" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="inviteForm.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="inviteForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showInviteDialog = false">取消</el-button>
        <el-button type="primary" @click="sendInvite" :loading="inviting">
          发送邀请
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑成员对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑成员" width="500px">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通成员" value="member" />
            <el-option label="项目经理" value="manager" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="editForm.department" placeholder="请输入部门" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="updateMember" :loading="updating">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, Plus, Search, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const inviting = ref(false)
const updating = ref(false)
const showInviteDialog = ref(false)
const showEditDialog = ref(false)
const inviteFormRef = ref()
const editFormRef = ref()
const members = ref([])
const editingMember = ref(null)

const searchForm = reactive({
  name: '',
  role: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const inviteForm = reactive({
  email: '',
  role: 'member',
  department: '',
  remark: ''
})

const editForm = reactive({
  name: '',
  role: '',
  department: ''
})

const inviteRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const editRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 方法
const getRoleTagType = (role) => {
  const typeMap = {
    'admin': 'danger',
    'manager': 'warning',
    'member': 'info'
  }
  return typeMap[role] || 'info'
}

const getRoleText = (role) => {
  const textMap = {
    'admin': '管理员',
    'manager': '项目经理',
    'member': '普通成员'
  }
  return textMap[role] || '未知'
}

const loadMembers = async () => {
  try {
    loading.value = true
    
    // 模拟成员数据
    const mockMembers = [
      {
        id: 1,
        name: '张三',
        email: 'zhangsan@example.com',
        phone: '13800138000',
        role: 'admin',
        department: '技术部',
        joinTime: '2024-01-01 10:00:00',
        status: 'active',
        avatar: ''
      },
      {
        id: 2,
        name: '李四',
        email: 'lisi@example.com',
        phone: '13800138001',
        role: 'manager',
        department: '产品部',
        joinTime: '2024-01-02 10:00:00',
        status: 'active',
        avatar: ''
      }
    ]

    members.value = mockMembers
    pagination.total = mockMembers.length

  } catch (error) {
    console.error('加载成员列表失败:', error)
    ElMessage.error('加载成员列表失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    role: ''
  })
  pagination.page = 1
  loadMembers()
}

const sendInvite = async () => {
  try {
    await inviteFormRef.value.validate()
    inviting.value = true

    // 模拟发送邀请
    ElMessage.success('邀请已发送')
    showInviteDialog.value = false
    resetInviteForm()
    loadMembers()
  } catch (error) {
    console.error('发送邀请失败:', error)
    ElMessage.error('发送邀请失败')
  } finally {
    inviting.value = false
  }
}

const editMember = (member) => {
  editingMember.value = member
  Object.assign(editForm, {
    name: member.name,
    role: member.role,
    department: member.department
  })
  showEditDialog.value = true
}

const updateMember = async () => {
  try {
    await editFormRef.value.validate()
    updating.value = true

    ElMessage.success('成员信息更新成功')
    showEditDialog.value = false
    loadMembers()
  } catch (error) {
    console.error('更新成员失败:', error)
    ElMessage.error('更新成员失败')
  } finally {
    updating.value = false
  }
}

const resendInvite = async (member) => {
  try {
    ElMessage.success(`已重新发送邀请给 ${member.email}`)
  } catch (error) {
    ElMessage.error('重发邀请失败')
  }
}

const removeMember = async (member) => {
  try {
    await ElMessageBox.confirm(`确定要移除成员 ${member.name} 吗？`, '确认操作', {
      type: 'warning'
    })
    
    ElMessage.success('成员移除成功')
    loadMembers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除成员失败:', error)
      ElMessage.error('移除成员失败')
    }
  }
}

const resetInviteForm = () => {
  Object.assign(inviteForm, {
    email: '',
    role: 'member',
    department: '',
    remark: ''
  })
  inviteFormRef.value?.resetFields()
}

// 生命周期
onMounted(() => {
  loadMembers()
})
</script>

<style scoped>
.enterprise-member {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.header-actions {
  margin-left: auto;
}

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
