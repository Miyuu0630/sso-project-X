<template>
  <div class="error-container">
    <el-result
      :icon="errorIcon"
      :title="errorTitle"
      :sub-title="errorSubTitle"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">
          返回首页
        </el-button>
        <el-button @click="goBack">
          返回上页
        </el-button>
        <el-button v-if="showRetry" type="warning" @click="retry">
          重试
        </el-button>
      </template>
    </el-result>
    
    <!-- 错误详情（开发环境显示） -->
    <div v-if="isDevelopment && errorDetails" class="error-details">
      <el-card>
        <template #header>
          <span>错误详情</span>
        </template>
        <pre>{{ errorDetails }}</pre>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 响应式数据
const errorDetails = ref('')
const isDevelopment = ref(process.env.NODE_ENV === 'development')

// 错误类型映射
const errorTypeMap = {
  'redirect_loop': {
    icon: 'warning',
    title: '重定向循环',
    subTitle: '检测到重定向循环，请重新登录或联系管理员',
    showRetry: true
  },
  'root_path_failed': {
    icon: 'error',
    title: '页面加载失败',
    subTitle: '无法加载页面，请检查网络连接或重新登录',
    showRetry: true
  },
  'auth_failed': {
    icon: 'error',
    title: '认证失败',
    subTitle: '用户认证失败，请重新登录',
    showRetry: false
  },
  'permission_denied': {
    icon: 'warning',
    title: '权限不足',
    subTitle: '您没有访问此页面的权限',
    showRetry: false
  },
  'network_error': {
    icon: 'error',
    title: '网络错误',
    subTitle: '网络连接异常，请检查网络设置',
    showRetry: true
  },
  'server_error': {
    icon: 'error',
    title: '服务器错误',
    subTitle: '服务器暂时不可用，请稍后重试',
    showRetry: true
  }
}

// 计算属性
const errorType = computed(() => {
  return route.query.reason || 'unknown'
})

const errorConfig = computed(() => {
  return errorTypeMap[errorType.value] || {
    icon: 'error',
    title: '未知错误',
    subTitle: '发生了未知错误，请稍后重试',
    showRetry: true
  }
})

const errorIcon = computed(() => errorConfig.value.icon)
const errorTitle = computed(() => errorConfig.value.title)
const errorSubTitle = computed(() => errorConfig.value.subTitle)
const showRetry = computed(() => errorConfig.value.showRetry)

// 方法
const goHome = () => {
  router.push('/')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/')
  }
}

const retry = () => {
  // 根据错误类型执行不同的重试逻辑
  switch (errorType.value) {
    case 'redirect_loop':
      // 清除本地存储并重新登录
      localStorage.clear()
      sessionStorage.clear()
      router.push('/')
      break
    case 'root_path_failed':
    case 'network_error':
    case 'server_error':
      // 刷新页面
      window.location.reload()
      break
    default:
      // 默认返回首页
      router.push('/')
  }
}

// 收集错误详情
const collectErrorDetails = () => {
  if (!isDevelopment.value) return
  
  const details = {
    errorType: errorType.value,
    timestamp: new Date().toISOString(),
    userAgent: navigator.userAgent,
    url: window.location.href,
    route: route.fullPath,
    query: route.query,
    params: route.params
  }
  
  errorDetails.value = JSON.stringify(details, null, 2)
}

// 生命周期
onMounted(() => {
  collectErrorDetails()
  
  // 记录错误到控制台
  console.error('页面错误:', {
    type: errorType.value,
    config: errorConfig.value,
    route: route.fullPath
  })
})
</script>

<style scoped>
.error-container {
  min-height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background-color: #f5f7fa;
}

.error-details {
  margin-top: 30px;
  max-width: 800px;
  width: 100%;
}

.error-details pre {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #e9ecef;
  font-size: 12px;
  line-height: 1.4;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .error-container {
    padding: 10px;
  }
  
  .error-details {
    margin-top: 20px;
  }
}
</style>
