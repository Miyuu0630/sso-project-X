<template>
  <div class="menu-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><Menu /></el-icon>
          <span>菜单管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="showAddDialog = true">
              <el-icon><Plus /></el-icon>
              新增菜单
            </el-button>
          </div>
        </div>
      </template>

      <!-- 菜单树形表格 -->
      <el-table
        :data="menus"
        v-loading="loading"
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="name" label="菜单名称" width="200" />
        <el-table-column prop="path" label="路径" width="200" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="scope">
            <el-icon v-if="scope.row.icon">
              <component :is="scope.row.icon" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'menu' ? 'primary' : 'info'">
              {{ scope.row.type === 'menu' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="hidden" label="隐藏" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.hidden ? 'warning' : 'success'">
              {{ scope.row.hidden ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="permission" label="权限标识" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editMenu(scope.row)">
              编辑
            </el-button>
            <el-button type="success" size="small" @click="addChild(scope.row)">
              新增子菜单
            </el-button>
            <el-button type="danger" size="small" @click="deleteMenu(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑菜单对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingMenu ? '编辑菜单' : '新增菜单'"
      width="600px"
    >
      <el-form
        ref="menuFormRef"
        :model="menuForm"
        :rules="menuRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="name">
              <el-input v-model="menuForm.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单路径" prop="path">
              <el-input v-model="menuForm.path" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜单图标" prop="icon">
              <el-input v-model="menuForm.icon" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单类型" prop="type">
              <el-radio-group v-model="menuForm.type">
                <el-radio value="menu">菜单</el-radio>
                <el-radio value="button">按钮</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序号" prop="sort">
              <el-input-number v-model="menuForm.sort" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否隐藏" prop="hidden">
              <el-switch v-model="menuForm.hidden" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="权限标识" prop="permission">
          <el-input v-model="menuForm.permission" />
        </el-form-item>
        <el-form-item label="父级菜单" prop="parentId">
          <el-tree-select
            v-model="menuForm.parentId"
            :data="menuTreeData"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            placeholder="请选择父级菜单"
            clearable
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMenu" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Menu, Plus } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const editingMenu = ref(null)
const menuFormRef = ref()
const menus = ref([])

const menuForm = reactive({
  name: '',
  path: '',
  icon: '',
  type: 'menu',
  sort: 0,
  hidden: false,
  permission: '',
  parentId: null
})

const menuRules = {
  name: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' }
  ],
  path: [
    { required: true, message: '请输入菜单路径', trigger: 'blur' }
  ]
}

// 计算属性
const menuTreeData = computed(() => {
  return buildTreeData(menus.value)
})

// 方法
const buildTreeData = (data) => {
  return data.map(item => ({
    id: item.id,
    name: item.name,
    children: item.children ? buildTreeData(item.children) : []
  }))
}

const loadMenus = async () => {
  try {
    loading.value = true
    // 模拟菜单数据
    menus.value = [
      {
        id: 1,
        name: '系统管理',
        path: '/system',
        icon: 'Setting',
        type: 'menu',
        sort: 1,
        hidden: false,
        permission: 'system:manage',
        children: [
          {
            id: 11,
            name: '用户管理',
            path: '/system/user',
            icon: 'User',
            type: 'menu',
            sort: 1,
            hidden: false,
            permission: 'system:user:list'
          },
          {
            id: 12,
            name: '角色管理',
            path: '/system/role',
            icon: 'UserFilled',
            type: 'menu',
            sort: 2,
            hidden: false,
            permission: 'system:role:list'
          }
        ]
      },
      {
        id: 2,
        name: '系统监控',
        path: '/monitor',
        icon: 'Monitor',
        type: 'menu',
        sort: 2,
        hidden: false,
        permission: 'monitor:manage',
        children: [
          {
            id: 21,
            name: '在线用户',
            path: '/monitor/online',
            icon: 'UserFilled',
            type: 'menu',
            sort: 1,
            hidden: false,
            permission: 'monitor:online'
          }
        ]
      }
    ]
  } catch (error) {
    console.error('加载菜单列表失败:', error)
    ElMessage.error('加载菜单列表失败')
  } finally {
    loading.value = false
  }
}

const editMenu = (menu) => {
  editingMenu.value = menu
  Object.assign(menuForm, {
    name: menu.name,
    path: menu.path,
    icon: menu.icon,
    type: menu.type,
    sort: menu.sort,
    hidden: menu.hidden,
    permission: menu.permission,
    parentId: menu.parentId
  })
  showAddDialog.value = true
}

const addChild = (menu) => {
  editingMenu.value = null
  Object.assign(menuForm, {
    name: '',
    path: '',
    icon: '',
    type: 'menu',
    sort: 0,
    hidden: false,
    permission: '',
    parentId: menu.id
  })
  showAddDialog.value = true
}

const saveMenu = async () => {
  try {
    await menuFormRef.value.validate()
    saving.value = true

    if (editingMenu.value) {
      ElMessage.success('菜单更新成功')
    } else {
      ElMessage.success('菜单创建成功')
    }

    showAddDialog.value = false
    resetMenuForm()
    loadMenus()
  } catch (error) {
    console.error('保存菜单失败:', error)
    ElMessage.error('保存菜单失败')
  } finally {
    saving.value = false
  }
}

const deleteMenu = async (menu) => {
  try {
    await ElMessageBox.confirm(`确定要删除菜单 ${menu.name} 吗？`, '确认删除', {
      type: 'warning'
    })
    
    ElMessage.success('菜单删除成功')
    loadMenus()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除菜单失败:', error)
      ElMessage.error('删除菜单失败')
    }
  }
}

const resetMenuForm = () => {
  editingMenu.value = null
  Object.assign(menuForm, {
    name: '',
    path: '',
    icon: '',
    type: 'menu',
    sort: 0,
    hidden: false,
    permission: '',
    parentId: null
  })
  menuFormRef.value?.resetFields()
}

// 生命周期
onMounted(() => {
  loadMenus()
})
</script>

<style scoped>
.menu-management {
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
