<template>
  <div class="airline-info">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><Ship /></el-icon>
          <span>航司信息</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="airline-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="航司名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入航司名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="IATA代码" prop="iataCode">
              <el-input v-model="form.iataCode" placeholder="请输入IATA代码" maxlength="2" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="ICAO代码" prop="icaoCode">
              <el-input v-model="form.icaoCode" placeholder="请输入ICAO代码" maxlength="3" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="呼号" prop="callsign">
              <el-input v-model="form.callsign" placeholder="请输入呼号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属国家" prop="country">
              <el-select v-model="form.country" placeholder="请选择国家" style="width: 100%">
                <el-option label="中国" value="CN" />
                <el-option label="美国" value="US" />
                <el-option label="英国" value="GB" />
                <el-option label="法国" value="FR" />
                <el-option label="德国" value="DE" />
                <el-option label="日本" value="JP" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成立时间" prop="foundedDate">
              <el-date-picker
                v-model="form.foundedDate"
                type="date"
                placeholder="请选择成立时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="机队规模" prop="fleetSize">
              <el-input-number 
                v-model="form.fleetSize" 
                :min="0" 
                placeholder="请输入机队规模"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="运营状态" prop="operatingStatus">
              <el-select v-model="form.operatingStatus" placeholder="请选择运营状态" style="width: 100%">
                <el-option label="正常运营" value="active" />
                <el-option label="暂停运营" value="suspended" />
                <el-option label="维护中" value="maintenance" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入联系邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="总部地址" prop="headquarters">
          <el-input v-model="form.headquarters" placeholder="请输入总部地址" />
        </el-form-item>

        <el-form-item label="主要航线" prop="mainRoutes">
          <el-input
            v-model="form.mainRoutes"
            type="textarea"
            :rows="3"
            placeholder="请输入主要航线信息"
          />
        </el-form-item>

        <el-form-item label="航司简介" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入航司简介"
          />
        </el-form-item>

        <el-form-item label="航司LOGO" prop="logo">
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
import { Ship, Plus, Check, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const formRef = ref()
const saving = ref(false)

const form = reactive({
  name: '',
  iataCode: '',
  icaoCode: '',
  callsign: '',
  country: '',
  foundedDate: '',
  fleetSize: 0,
  operatingStatus: 'active',
  phone: '',
  email: '',
  headquarters: '',
  mainRoutes: '',
  description: '',
  logo: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入航司名称', trigger: 'blur' }
  ],
  iataCode: [
    { required: true, message: '请输入IATA代码', trigger: 'blur' },
    { pattern: /^[A-Z]{2}$/, message: 'IATA代码必须是2位大写字母', trigger: 'blur' }
  ],
  icaoCode: [
    { required: true, message: '请输入ICAO代码', trigger: 'blur' },
    { pattern: /^[A-Z]{3}$/, message: 'ICAO代码必须是3位大写字母', trigger: 'blur' }
  ],
  country: [
    { required: true, message: '请选择所属国家', trigger: 'change' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 方法
const loadAirlineInfo = async () => {
  try {
    const response = await request.get('/api/airline/info')
    if (response.data.code === 200) {
      Object.assign(form, response.data.data)
    }
  } catch (error) {
    console.error('加载航司信息失败:', error)
    // 使用模拟数据
    Object.assign(form, {
      name: '中国国际航空股份有限公司',
      iataCode: 'CA',
      icaoCode: 'CCA',
      callsign: 'AIR CHINA',
      country: 'CN',
      foundedDate: '1988-07-01',
      fleetSize: 400,
      operatingStatus: 'active',
      phone: '400-100-0999',
      email: 'info@airchina.com',
      headquarters: '北京市顺义区天竺空港工业区A区天柱路28号',
      mainRoutes: '北京-上海、北京-广州、北京-深圳、北京-成都、国际航线等',
      description: '中国国际航空股份有限公司是中国唯一载国旗飞行的民用航空公司，承担着中国国家领导人出国访问的专机任务。',
      logo: ''
    })
  }
}

const saveInfo = async () => {
  try {
    await formRef.value.validate()
    saving.value = true

    const response = await request.put('/api/airline/info', form)
    if (response.data.code === 200) {
      ElMessage.success('航司信息保存成功')
    } else {
      ElMessage.error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存航司信息失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  loadAirlineInfo()
}

const beforeLogoUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传LOGO图片只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传LOGO图片大小不能超过 2MB!')
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
  loadAirlineInfo()
})
</script>

<style scoped>
.airline-info {
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

.airline-form {
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
