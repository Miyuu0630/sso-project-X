<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-illustration">
        <div class="error-code">404</div>
        <div class="error-icon">
          <el-icon size="120" color="#ddd"><DocumentDelete /></el-icon>
        </div>
      </div>
      
      <h1 class="error-title">页面未找到</h1>
      <p class="error-message">
        抱歉，您访问的页面不存在或已被移除。
      </p>
      
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome">
          返回首页
        </el-button>
        <el-button size="large" @click="goBack">
          返回上页
        </el-button>
      </div>
      
      <div class="helpful-links">
        <h3>您可能想要：</h3>
        <ul>
          <li><el-link @click="goHome">访问首页</el-link></li>
          <li><el-link @click="goProfile">个人中心</el-link></li>
          <li><el-link @click="goDashboard">仪表板</el-link></li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { DocumentDelete } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}

const goProfile = () => {
  router.push('/user/profile')
}

const goDashboard = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.not-found-content {
  max-width: 500px;
  text-align: center;
  background: white;
  padding: 60px 40px;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.error-illustration {
  position: relative;
  margin-bottom: 40px;
}

.error-code {
  font-size: 120px;
  font-weight: 900;
  color: #f0f0f0;
  line-height: 1;
  margin-bottom: -60px;
}

.error-icon {
  position: relative;
  z-index: 2;
}

.error-title {
  font-size: 32px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 16px 0;
}

.error-message {
  font-size: 16px;
  color: #7f8c8d;
  margin: 0 0 40px 0;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 40px;
}

.helpful-links h3 {
  font-size: 18px;
  color: #2c3e50;
  margin: 0 0 16px 0;
}

.helpful-links ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.helpful-links li {
  margin-bottom: 8px;
}

@media (max-width: 480px) {
  .not-found-content {
    padding: 40px 24px;
  }
  
  .error-code {
    font-size: 80px;
  }
  
  .error-title {
    font-size: 24px;
  }
  
  .error-actions {
    flex-direction: column;
  }
}
</style>
