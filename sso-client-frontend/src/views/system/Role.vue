<template>
  <div class="role-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><UserFilled /></el-icon>
          <span>角色管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="showAddDialog = true">
              <el-icon><Plus /></el-icon>
              新增角色
            </el-button>
          </div>
        </div>
      </template>

      <!-- 角色表格 -->
      <el-table :data="roles" v-loading="loading" style="width: 100%">
        <el-table-column prop="code" label="角色代码" width="150" />
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="description" label="角色描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '1' ? 'success' : 'danger'">
              {{ scope.row.status === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editRole(scope.row)">
              编辑
            </el-button>
            <el-button type="info" size="small" @click="setPermissions(scope.row)">
              权限
            </el-button>
            <el-button type="danger" size="small" @click="deleteRole(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑角色对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingRole ? '编辑角色' : '新增角色'"
      width="500px"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleRules"
        label-width="100px"
      >
        <el-form-item label="角色代码" prop="code">
          <el-input v-model="roleForm.code" :disabled="editingRole" />
        </el-form-item>
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio value="1">启用</el-radio>
            <el-radio value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRole" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, Plus } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const editingRole = ref(null)
const roleFormRef = ref()
const roles = ref([])

const roleForm = reactive({
  code: '',
  name: '',
  description: '',
  status: '1'
})

const roleRules = {
  code: [
    { required: true, message: '请输入角色代码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ]
}

// 方法
const loadRoles = async () => {
  try {
    loading.value = true
    // 模拟数据
    roles.value = [
      {
        id: 1,
        code: 'ADMIN',
        name: '系统管理员',
        description: '系统最高权限管理员',
        status: '1',
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: 2,
        code: 'AIRLINE_USER',
        name: '航司用户',
        description: '航空公司用户角色',
        status: '1',
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: 3,
        code: 'ENTERPRISE_USER',
        name: '企业用户',
        description: '企业用户角色',
        status: '1',
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: 4,
        code: 'PERSONAL_USER',
        name: '个人用户',
        description: '个人用户角色',
        status: '1',
        createTime: '2024-01-01 10:00:00'
      }
    ]
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

const editRole = (role) => {
  editingRole.value = role
  Object.assign(roleForm, {
    code: role.code,
    name: role.name,
    description: role.description,
    status: role.status
  })
  showAddDialog.value = true
}

const saveRole = async () => {
  try {
    await roleFormRef.value.validate()
    saving.value = true

    if (editingRole.value) {
      ElMessage.success('角色更新成功')
    } else {
      ElMessage.success('角色创建成功')
    }

    showAddDialog.value = false
    resetRoleForm()
    loadRoles()
  } catch (error) {
    console.error('保存角色失败:', error)
    ElMessage.error('保存角色失败')
  } finally {
    saving.value = false
  }
}

const setPermissions = (role) => {
  ElMessage.info(`设置角色 ${role.name} 的权限功能开发中...`)
}

const deleteRole = async (role) => {
  try {
    await ElMessageBox.confirm(`确定要删除角色 ${role.name} 吗？`, '确认删除', {
      type: 'warning'
    })
    
    ElMessage.success('角色删除成功')
    loadRoles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除角色失败:', error)
      ElMessage.error('删除角色失败')
    }
  }
}

const resetRoleForm = () => {
  editingRole.value = null
  Object.assign(roleForm, {
    code: '',
    name: '',
    description: '',
    status: '1'
  })
  roleFormRef.value?.resetFields()
}

// 生命周期
onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.role-management {
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
</style>
