<template>
  <div class="enterprise-info">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><OfficeBuilding /></el-icon>
          <span>企业信息</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="enterprise-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入企业名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="creditCode">
              <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="法定代表人" prop="legalPerson">
              <el-input v-model="form.legalPerson" placeholder="请输入法定代表人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="注册资本" prop="registeredCapital">
              <el-input v-model="form.registeredCapital" placeholder="请输入注册资本">
                <template #append>万元</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成立日期" prop="establishDate">
              <el-date-picker
                v-model="form.establishDate"
                type="date"
                placeholder="请选择成立日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业规模" prop="scale">
              <el-select v-model="form.scale" placeholder="请选择企业规模" style="width: 100%">
                <el-option label="微型企业" value="micro" />
                <el-option label="小型企业" value="small" />
                <el-option label="中型企业" value="medium" />
                <el-option label="大型企业" value="large" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属行业" prop="industry">
              <el-select v-model="form.industry" placeholder="请选择所属行业" style="width: 100%">
                <el-option label="信息技术" value="it" />
                <el-option label="制造业" value="manufacturing" />
                <el-option label="金融业" value="finance" />
                <el-option label="服务业" value="service" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="企业地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入企业地址" />
        </el-form-item>

        <el-form-item label="企业简介" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入企业简介"
          />
        </el-form-item>

        <el-form-item label="企业LOGO" prop="logo">
          <el-upload
            class="logo-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeLogoUpload"
            :http-request="uploadLogo"
          >
            <img v-if="form.logo" :src="form.logo" class="logo" />
            <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="saveInfo" :loading="saving">
            <el-icon><Check /></el-icon>
            保存信息
          </el-button>
          <el-button @click="resetForm">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { OfficeBuilding, Plus, Check, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const formRef = ref()
const saving = ref(false)

const form = reactive({
  name: '',
  creditCode: '',
  legalPerson: '',
  registeredCapital: '',
  establishDate: '',
  scale: '',
  industry: '',
  phone: '',
  address: '',
  description: '',
  logo: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入企业名称', trigger: 'blur' }
  ],
  creditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { pattern: /^[0-9A-HJ-NPQRTUWXY]{2}\d{6}[0-9A-HJ-NPQRTUWXY]{10}$/, message: '请输入正确的统一社会信用代码', trigger: 'blur' }
  ],
  legalPerson: [
    { required: true, message: '请输入法定代表人', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 方法
const loadEnterpriseInfo = async () => {
  try {
    const response = await request.get('/api/enterprise/info')
    if (response.data.code === 200) {
      Object.assign(form, response.data.data)
    }
  } catch (error) {
    console.error('加载企业信息失败:', error)
    // 使用模拟数据
    Object.assign(form, {
      name: '示例科技有限公司',
      creditCode: '91110000123456789X',
      legalPerson: '张三',
      registeredCapital: '1000',
      establishDate: '2020-01-01',
      scale: 'medium',
      industry: 'it',
      phone: '13800138000',
      address: '北京市朝阳区示例大厦',
      description: '这是一家专注于软件开发的科技公司',
      logo: ''
    })
  }
}

const saveInfo = async () => {
  try {
    await formRef.value.validate()
    saving.value = true

    const response = await request.put('/api/enterprise/info', form)
    if (response.data.code === 200) {
      ElMessage.success('企业信息保存成功')
    } else {
      ElMessage.error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存企业信息失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  loadEnterpriseInfo()
}

const beforeLogoUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

const uploadLogo = (options) => {
  // 模拟上传
  const reader = new FileReader()
  reader.onload = (e) => {
    form.logo = e.target.result
    ElMessage.success('LOGO上传成功')
  }
  reader.readAsDataURL(options.file)
}

// 生命周期
onMounted(() => {
  loadEnterpriseInfo()
})
</script>

<style scoped>
.enterprise-info {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}

.enterprise-form {
  margin-top: 20px;
}

.logo-uploader .logo {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 6px;
}

.logo-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.logo-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}
</style>
