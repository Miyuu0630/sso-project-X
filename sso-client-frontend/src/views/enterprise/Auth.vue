<template>
  <div class="enterprise-auth">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><Checked /></el-icon>
          <span>企业认证</span>
        </div>
      </template>

      <!-- 认证状态 -->
      <div class="auth-status">
        <el-alert
          :title="getStatusTitle()"
          :type="getStatusType()"
          :description="getStatusDescription()"
          show-icon
          :closable="false"
        />
      </div>

      <!-- 认证表单 -->
      <div v-if="authStatus !== 'verified'" class="auth-form">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
        >
          <el-form-item label="企业名称" prop="companyName">
            <el-input v-model="form.companyName" placeholder="请输入企业名称" />
          </el-form-item>

          <el-form-item label="统一社会信用代码" prop="creditCode">
            <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" />
          </el-form-item>

          <el-form-item label="法定代表人" prop="legalPerson">
            <el-input v-model="form.legalPerson" placeholder="请输入法定代表人姓名" />
          </el-form-item>

          <el-form-item label="营业执照" prop="businessLicense">
            <el-upload
              class="upload-demo"
              drag
              action="#"
              :before-upload="beforeUpload"
              :http-request="uploadFile"
              :file-list="businessLicenseFiles"
              accept="image/*,.pdf"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 jpg/png/pdf 文件，且不超过 10MB
                </div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item label="组织机构代码证" prop="organizationCode">
            <el-upload
              class="upload-demo"
              drag
              action="#"
              :before-upload="beforeUpload"
              :http-request="uploadFile"
              :file-list="organizationCodeFiles"
              accept="image/*,.pdf"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 jpg/png/pdf 文件，且不超过 10MB
                </div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item label="联系人姓名" prop="contactName">
            <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
          </el-form-item>

          <el-form-item label="联系人电话" prop="contactPhone">
            <el-input v-model="form.contactPhone" placeholder="请输入联系人电话" />
          </el-form-item>

          <el-form-item label="联系人邮箱" prop="contactEmail">
            <el-input v-model="form.contactEmail" placeholder="请输入联系人邮箱" />
          </el-form-item>

          <el-form-item label="备注说明" prop="remark">
            <el-input
              v-model="form.remark"
              type="textarea"
              :rows="4"
              placeholder="请输入备注说明"
            />
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              @click="submitAuth" 
              :loading="submitting"
              :disabled="authStatus === 'pending'"
            >
              <el-icon><Check /></el-icon>
              {{ authStatus === 'pending' ? '审核中' : '提交认证' }}
            </el-button>
            <el-button @click="resetForm">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 认证信息展示 -->
      <div v-if="authStatus === 'verified'" class="auth-info">
        <el-descriptions title="认证信息" :column="2" border>
          <el-descriptions-item label="企业名称">
            {{ authInfo.companyName }}
          </el-descriptions-item>
          <el-descriptions-item label="统一社会信用代码">
            {{ authInfo.creditCode }}
          </el-descriptions-item>
          <el-descriptions-item label="法定代表人">
            {{ authInfo.legalPerson }}
          </el-descriptions-item>
          <el-descriptions-item label="认证时间">
            {{ authInfo.authTime }}
          </el-descriptions-item>
          <el-descriptions-item label="有效期至">
            {{ authInfo.expireTime }}
          </el-descriptions-item>
          <el-descriptions-item label="认证状态">
            <el-tag type="success">已认证</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 审核记录 -->
      <div v-if="auditRecords.length > 0" class="audit-records">
        <h3>审核记录</h3>
        <el-timeline>
          <el-timeline-item
            v-for="record in auditRecords"
            :key="record.id"
            :timestamp="record.time"
            :type="record.type"
          >
            <h4>{{ record.title }}</h4>
            <p>{{ record.description }}</p>
            <p v-if="record.remark" class="audit-remark">备注：{{ record.remark }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Checked, Check, Refresh, UploadFilled } from '@element-plus/icons-vue'

// 响应式数据
const formRef = ref()
const submitting = ref(false)
const authStatus = ref('unverified') // unverified, pending, verified, rejected
const businessLicenseFiles = ref([])
const organizationCodeFiles = ref([])
const auditRecords = ref([])

const form = reactive({
  companyName: '',
  creditCode: '',
  legalPerson: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  remark: ''
})

const authInfo = reactive({
  companyName: '',
  creditCode: '',
  legalPerson: '',
  authTime: '',
  expireTime: ''
})

// 表单验证规则
const rules = {
  companyName: [
    { required: true, message: '请输入企业名称', trigger: 'blur' }
  ],
  creditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { pattern: /^[0-9A-HJ-NPQRTUWXY]{2}\d{6}[0-9A-HJ-NPQRTUWXY]{10}$/, message: '请输入正确的统一社会信用代码', trigger: 'blur' }
  ],
  legalPerson: [
    { required: true, message: '请输入法定代表人', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系人电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  contactEmail: [
    { required: true, message: '请输入联系人邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 方法
const getStatusTitle = () => {
  const titleMap = {
    'unverified': '企业认证',
    'pending': '认证审核中',
    'verified': '认证已通过',
    'rejected': '认证被拒绝'
  }
  return titleMap[authStatus.value]
}

const getStatusType = () => {
  const typeMap = {
    'unverified': 'info',
    'pending': 'warning',
    'verified': 'success',
    'rejected': 'error'
  }
  return typeMap[authStatus.value]
}

const getStatusDescription = () => {
  const descMap = {
    'unverified': '请完善企业信息并上传相关证件进行认证',
    'pending': '您的认证申请正在审核中，请耐心等待',
    'verified': '恭喜！您的企业认证已通过',
    'rejected': '很抱歉，您的认证申请被拒绝，请查看审核记录并重新提交'
  }
  return descMap[authStatus.value]
}

const beforeUpload = (file) => {
  const isValidType = file.type.startsWith('image/') || file.type === 'application/pdf'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isValidType) {
    ElMessage.error('只能上传图片或PDF文件!')
  }
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!')
  }
  return isValidType && isLt10M
}

const uploadFile = (options) => {
  // 模拟文件上传
  ElMessage.success('文件上传成功')
  return Promise.resolve()
}

const submitAuth = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    // 模拟提交认证
    authStatus.value = 'pending'
    ElMessage.success('认证申请已提交，请等待审核')
    
    // 添加审核记录
    auditRecords.value.unshift({
      id: Date.now(),
      title: '提交认证申请',
      description: '企业认证申请已提交，等待审核',
      time: new Date().toLocaleString('zh-CN'),
      type: 'primary'
    })

  } catch (error) {
    console.error('提交认证失败:', error)
    ElMessage.error('提交认证失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  businessLicenseFiles.value = []
  organizationCodeFiles.value = []
}

const loadAuthInfo = () => {
  // 模拟加载认证信息
  authStatus.value = 'unverified'
  
  // 如果已认证，显示认证信息
  if (authStatus.value === 'verified') {
    Object.assign(authInfo, {
      companyName: '示例科技有限公司',
      creditCode: '91110000123456789X',
      legalPerson: '张三',
      authTime: '2024-01-15 10:30:00',
      expireTime: '2025-01-15'
    })
  }
}

// 生命周期
onMounted(() => {
  loadAuthInfo()
})
</script>

<style scoped>
.enterprise-auth {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}

.auth-status {
  margin-bottom: 30px;
}

.auth-form {
  margin-bottom: 30px;
}

.upload-demo {
  width: 100%;
}

.auth-info {
  margin-bottom: 30px;
}

.audit-records {
  margin-top: 30px;
}

.audit-records h3 {
  margin-bottom: 20px;
  color: #303133;
}

.audit-remark {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}
</style>
