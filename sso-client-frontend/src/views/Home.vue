<template>
  <div class="home">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎使用SSO业务系统</span>
        </div>
      </template>
      
      <div class="content">
        <div v-if="authStore.isLoggedIn" class="logged-in">
          <el-result
            icon="success"
            title="登录成功"
            :sub-title="`欢迎回来，${userInfo?.realName || userInfo?.username}！`"
          >
            <template #extra>
              <el-space>
                <el-button type="primary" @click="$router.push('/dashboard')">
                  进入控制台
                </el-button>
                <el-button @click="$router.push('/profile')">
                  个人信息
                </el-button>
              </el-space>
            </template>
          </el-result>
          
          <el-divider />
          
          <div class="user-stats">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-statistic title="用户类型" :value="userInfo?.userTypeDesc || '未知'" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="账号状态" :value="userInfo?.statusDesc || '未知'" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="权限数量" :value="permissions.length" />
              </el-col>
            </el-row>
          </div>
        </div>
        
        <div v-else class="not-logged-in">
          <el-result
            icon="info"
            title="欢迎访问SSO业务系统"
            sub-title="请先登录以访问系统功能"
          >
            <template #extra>
              <el-button type="primary" size="large" @click="login">
                立即登录
              </el-button>
            </template>
          </el-result>
          
          <el-divider />
          
          <div class="features">
            <h3>系统特性</h3>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card shadow="hover">
                  <div class="feature-item">
                    <el-icon size="32" color="#409EFF">
                      <Lock />
                    </el-icon>
                    <h4>单点登录</h4>
                    <p>一次登录，全系统通行</p>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover">
                  <div class="feature-item">
                    <el-icon size="32" color="#67C23A">
                      <User />
                    </el-icon>
                    <h4>权限管理</h4>
                    <p>精细化权限控制</p>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover">
                  <div class="feature-item">
                    <el-icon size="32" color="#E6A23C">
                      <Shield />
                    </el-icon>
                    <h4>安全可靠</h4>
                    <p>多重安全保障</p>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Lock, User, Shield } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const userInfo = computed(() => authStore.userInfo)
const permissions = computed(() => authStore.permissions)

const login = () => {
  authStore.redirectToLogin()
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.content {
  padding: 20px 0;
}

.user-stats {
  margin-top: 30px;
}

.features {
  margin-top: 30px;
}

.features h3 {
  text-align: center;
  margin-bottom: 20px;
  color: #303133;
}

.feature-item {
  text-align: center;
  padding: 20px;
}

.feature-item h4 {
  margin: 15px 0 10px;
  color: #303133;
}

.feature-item p {
  color: #606266;
  margin: 0;
}
</style>
