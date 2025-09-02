<template>
  <div class="register-container">
    <!-- Logo 和标题 -->
    <div class="logo">
      <img src="/static/images/logo.png" alt="Logo" @error="hideLogo">
      <h1>创建账号</h1>
      <p>加入 SSO 认证中心</p>
    </div>

    <!-- 错误/成功消息 -->
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>

    <!-- 注册表单 -->
    <el-form
      ref="registerFormRef"
      :model="registerForm"
      :rules="registerRules"
      class="register-form"
      label-position="top"
      @submit.prevent="handleRegister"
    >
      <!-- 用户名 -->
      <el-form-item label="用户名 *" prop="username">
        <el-input
          v-model="registerForm.username"
          placeholder="3-20位字母、数字或下划线"
          clearable
          @blur="validateUsername"
        />
        <div v-if="usernameStatus" class="field-status">
          <span v-if="usernameStatus === 'checking'" class="checking">检查中...</span>
          <span v-else-if="usernameStatus === 'available'" class="success">✓ 用户名可用</span>
          <span v-else-if="usernameStatus === 'unavailable'" class="error">✗ 用户名已存在</span>
        </div>
      </el-form-item>

      <!-- 真实姓名 -->
      <el-form-item label="真实姓名 *" prop="realName">
        <el-input
          v-model="registerForm.realName"
          placeholder="请输入真实姓名"
          clearable
        />
      </el-form-item>

      <!-- 手机号和邮箱 -->
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="手机号 *" prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="手机号"
              clearable
              @blur="validatePhone"
            />
            <div v-if="phoneStatus" class="field-status">
              <span v-if="phoneStatus === 'checking'" class="checking">检查中...</span>
              <span v-else-if="phoneStatus === 'available'" class="success">✓ 手机号可用</span>
              <span v-else-if="phoneStatus === 'unavailable'" class="error">✗ 手机号已存在</span>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱 *" prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="邮箱地址"
              clearable
              @blur="validateEmail"
            />
            <div v-if="emailStatus" class="field-status">
              <span v-if="emailStatus === 'checking'" class="checking">检查中...</span>
              <span v-else-if="emailStatus === 'available'" class="success">✓ 邮箱可用</span>
              <span v-else-if="emailStatus === 'unavailable'" class="error">✗ 邮箱已存在</span>
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 密码 -->
      <el-form-item label="密码 *" prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          placeholder="至少6位，包含字母和数字"
          show-password
          @input="updatePasswordStrength"
        />
        <div class="password-strength">
          <div class="strength-bar">
            <div 
              :class="['strength-fill', strengthClass]" 
              :style="{ width: strengthWidth }"
            ></div>
          </div>
          <div class="strength-text">密码强度：{{ strengthText }}</div>
        </div>
      </el-form-item>

      <!-- 确认密码 -->
      <el-form-item label="确认密码 *" prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          placeholder="再次输入密码"
          show-password
        />
      </el-form-item>

      <!-- 用户类型 -->
      <el-form-item label="用户类型" prop="userType">
        <el-select v-model="registerForm.userType" placeholder="请选择用户类型" style="width: 100%">
          <el-option
            v-for="option in userTypeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          >
            <span style="float: left">{{ option.label }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">{{ option.description }}</span>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 服务条款 -->
      <el-form-item prop="agreeTerms">
        <el-checkbox v-model="registerForm.agreeTerms">
          我已阅读并同意 
          <el-link type="primary" @click="showTerms">服务条款</el-link> 
          和 
          <el-link type="primary" @click="showPrivacy">隐私政策</el-link>
        </el-checkbox>
      </el-form-item>

      <!-- 注册按钮 -->
      <el-form-item>
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          @click="handleRegister"
          style="width: 100%"
        >
          {{ loading ? '创建中...' : '创建账号' }}
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 页脚链接 -->
    <div class="form-footer">
      <el-link type="primary" @click="goToLogin">已有账号？立即登录</el-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { register, checkUsername, checkPhone, checkEmail } from '@/api/auth'

const router = useRouter()

// 表单引用
const registerFormRef = ref()

// 加载状态
const loading = ref(false)

// 消息状态
const errorMessage = ref('')
const successMessage = ref('')

// 字段验证状态
const usernameStatus = ref('')
const phoneStatus = ref('')
const emailStatus = ref('')

// 密码强度
const passwordStrength = ref(0)
const strengthText = ref('无')
const strengthClass = ref('')
const strengthWidth = ref('0%')

// 注册表单数据
const registerForm = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: '',
  userType: 'normal',
  agreeTerms: false
})

// 用户类型选项
const userTypeOptions = [
  {
    value: 'normal',
    label: '个人用户',
    description: '个人用户，拥有基本功能权限'
  },
  {
    value: 'enterprise',
    label: '企业用户',
    description: '企业用户，拥有企业级功能权限'
  },
  {
    value: 'airline',
    label: '航司用户',
    description: '航空公司用户，拥有航司专用功能权限'
  }
]

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度必须在3-20位之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '真实姓名长度必须在2-20位之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度必须在6-50位之间', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  agreeTerms: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请阅读并同意服务条款和隐私政策'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 计算属性
const isFormValid = computed(() => {
  return registerForm.username && 
         registerForm.realName && 
         registerForm.phone && 
         registerForm.email && 
         registerForm.password && 
         registerForm.confirmPassword && 
         registerForm.userType && 
         registerForm.agreeTerms
})

// 方法
const hideLogo = (event) => {
  event.target.style.display = 'none'
}

const updatePasswordStrength = (password) => {
  if (!password) {
    passwordStrength.value = 0
    strengthText.value = '无'
    strengthClass.value = ''
    strengthWidth.value = '0%'
    return
  }

  let strength = 0
  let text = '无'
  let className = ''
  let width = '0%'

  if (password.length >= 6) strength++
  if (/[a-z]/.test(password)) strength++
  if (/[A-Z]/.test(password)) strength++
  if (/\d/.test(password)) strength++
  if (/[^a-zA-Z\d]/.test(password)) strength++

  switch (strength) {
    case 0:
    case 1:
      text = '弱'
      className = 'strength-weak'
      width = '25%'
      break
    case 2:
      text = '一般'
      className = 'strength-fair'
      width = '50%'
      break
    case 3:
      text = '良好'
      className = 'strength-good'
      width = '75%'
      break
    case 4:
    case 5:
      text = '强'
      className = 'strength-strong'
      width = '100%'
      break
  }

  passwordStrength.value = strength
  strengthText.value = text
  strengthClass.value = className
  strengthWidth.value = width
}

const validateUsername = async () => {
  if (!registerForm.username || registerForm.username.length < 3) {
    usernameStatus.value = ''
    return
  }

  usernameStatus.value = 'checking'
  try {
    const response = await checkUsername(registerForm.username)
    usernameStatus.value = response.data ? 'available' : 'unavailable'
  } catch (error) {
    usernameStatus.value = ''
    console.error('检查用户名失败:', error)
  }
}

const validatePhone = async () => {
  if (!registerForm.phone || !/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    phoneStatus.value = ''
    return
  }

  phoneStatus.value = 'checking'
  try {
    const response = await checkPhone(registerForm.phone)
    phoneStatus.value = response.data ? 'available' : 'unavailable'
  } catch (error) {
    phoneStatus.value = ''
    console.error('检查手机号失败:', error)
  }
}

const validateEmail = async () => {
  if (!registerForm.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) {
    emailStatus.value = ''
    return
  }

  emailStatus.value = 'checking'
  try {
    const response = await checkEmail(registerForm.email)
    emailStatus.value = response.data ? 'available' : 'unavailable'
  } catch (error) {
    emailStatus.value = ''
    console.error('检查邮箱失败:', error)
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    // 验证表单
    await registerFormRef.value.validate()
    
    // 检查字段可用性
    if (usernameStatus.value === 'unavailable') {
      ElMessage.error('用户名已存在，请重新输入')
      return
    }
    if (phoneStatus.value === 'unavailable') {
      ElMessage.error('手机号已存在，请重新输入')
      return
    }
    if (emailStatus.value === 'unavailable') {
      ElMessage.error('邮箱已存在，请重新输入')
      return
    }

    loading.value = true
    errorMessage.value = ''
    successMessage.value = ''

    // 调用注册API
    const response = await register(registerForm)
    
    if (response.code === 200) {
      successMessage.value = '注册成功！正在跳转到登录页面...'
      ElMessage.success('注册成功！')
      
      // 延迟跳转到登录页
      setTimeout(() => {
        router.push({
          path: '/login',
          query: { message: '注册成功，请登录' }
        })
      }, 2000)
    } else {
      errorMessage.value = response.message || '注册失败，请重试'
      ElMessage.error(response.message || '注册失败，请重试')
    }
  } catch (error) {
    console.error('注册失败:', error)
    errorMessage.value = '注册失败，请检查输入信息'
    ElMessage.error('注册失败，请检查输入信息')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}

const showTerms = () => {
  ElMessageBox.alert('服务条款内容', '服务条款', {
    confirmButtonText: '确定'
  })
}

const showPrivacy = () => {
  ElMessageBox.alert('隐私政策内容', '隐私政策', {
    confirmButtonText: '确定'
  })
}
</script>

<style scoped>
.register-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 450px;
  position: relative;
  overflow: hidden;
}

.register-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.logo {
  text-align: center;
  margin-bottom: 32px;
}

.logo img {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
}

.logo h1 {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.logo p {
  color: #7f8c8d;
  font-size: 14px;
}

.register-form {
  margin-bottom: 24px;
}

.error-message {
  background: #fee;
  color: #e74c3c;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #e74c3c;
  font-size: 14px;
}

.success-message {
  background: #efe;
  color: #27ae60;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #27ae60;
  font-size: 14px;
}

.field-status {
  margin-top: 4px;
  font-size: 12px;
}

.field-status .checking {
  color: #6c757d;
}

.field-status .success {
  color: #27ae60;
}

.field-status .error {
  color: #e74c3c;
}

.password-strength {
  margin-top: 8px;
  font-size: 12px;
}

.strength-bar {
  height: 4px;
  background: #e1e8ed;
  border-radius: 2px;
  margin: 4px 0;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 2px;
}

.strength-weak { background: #e74c3c; }
.strength-fair { background: #f39c12; }
.strength-good { background: #f1c40f; }
.strength-strong { background: #27ae60; }

.form-footer {
  text-align: center;
  margin-top: 24px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-container {
    padding: 24px;
    margin: 10px;
  }
  
  .logo h1 {
    font-size: 20px;
  }
}
</style>
