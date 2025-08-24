<template>
  <div class="passenger-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><UserFilled /></el-icon>
          <span>乘客管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="姓名">
            <el-input v-model="searchForm.name" placeholder="请输入乘客姓名" clearable />
          </el-form-item>
          <el-form-item label="证件号">
            <el-input v-model="searchForm.idNumber" placeholder="请输入证件号" clearable />
          </el-form-item>
          <el-form-item label="航班号">
            <el-input v-model="searchForm.flightNumber" placeholder="请输入航班号" clearable />
          </el-form-item>
          <el-form-item label="订票状态">
            <el-select v-model="searchForm.bookingStatus" placeholder="请选择状态" clearable>
              <el-option label="已预订" value="booked" />
              <el-option label="已支付" value="paid" />
              <el-option label="已值机" value="checked_in" />
              <el-option label="已登机" value="boarded" />
              <el-option label="已取消" value="cancelled" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadPassengers">
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

      <!-- 乘客表格 -->
      <el-table :data="passengers" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="scope">
            {{ scope.row.gender === 'M' ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="idType" label="证件类型" width="100">
          <template #default="scope">
            {{ getIdTypeText(scope.row.idType) }}
          </template>
        </el-table-column>
        <el-table-column prop="idNumber" label="证件号码" width="180" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="flightNumber" label="航班号" width="120" />
        <el-table-column prop="seatNumber" label="座位号" width="80" />
        <el-table-column prop="bookingTime" label="订票时间" width="160" />
        <el-table-column prop="bookingStatus" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.bookingStatus)">
              {{ getStatusText(scope.row.bookingStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ticketPrice" label="票价" width="100">
          <template #default="scope">
            ¥{{ scope.row.ticketPrice }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">
              详情
            </el-button>
            <el-button 
              v-if="scope.row.bookingStatus === 'paid'"
              type="success" 
              size="small" 
              @click="checkIn(scope.row)"
            >
              值机
            </el-button>
            <el-button 
              v-if="['booked', 'paid'].includes(scope.row.bookingStatus)"
              type="danger" 
              size="small" 
              @click="cancelBooking(scope.row)"
            >
              取消
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
          @size-change="loadPassengers"
          @current-change="loadPassengers"
        />
      </div>
    </el-card>

    <!-- 乘客详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="乘客详情" width="600px">
      <div v-if="selectedPassenger" class="passenger-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">
            {{ selectedPassenger.name }}
          </el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ selectedPassenger.gender === 'M' ? '男' : '女' }}
          </el-descriptions-item>
          <el-descriptions-item label="证件类型">
            {{ getIdTypeText(selectedPassenger.idType) }}
          </el-descriptions-item>
          <el-descriptions-item label="证件号码">
            {{ selectedPassenger.idNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ selectedPassenger.phone }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ selectedPassenger.email }}
          </el-descriptions-item>
          <el-descriptions-item label="航班号">
            {{ selectedPassenger.flightNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="座位号">
            {{ selectedPassenger.seatNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="舱位等级">
            {{ getCabinClassText(selectedPassenger.cabinClass) }}
          </el-descriptions-item>
          <el-descriptions-item label="票价">
            ¥{{ selectedPassenger.ticketPrice }}
          </el-descriptions-item>
          <el-descriptions-item label="订票时间">
            {{ selectedPassenger.bookingTime }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedPassenger.bookingStatus)">
              {{ getStatusText(selectedPassenger.bookingStatus) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 特殊需求 -->
        <div v-if="selectedPassenger.specialRequests" class="special-requests">
          <h4>特殊需求</h4>
          <el-tag
            v-for="request in selectedPassenger.specialRequests"
            :key="request"
            style="margin-right: 8px; margin-bottom: 8px;"
          >
            {{ request }}
          </el-tag>
        </div>

        <!-- 行李信息 -->
        <div v-if="selectedPassenger.luggage" class="luggage-info">
          <h4>行李信息</h4>
          <p>托运行李：{{ selectedPassenger.luggage.checked }}件</p>
          <p>手提行李：{{ selectedPassenger.luggage.carryon }}件</p>
        </div>
      </div>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, Search, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const showDetailDialog = ref(false)
const passengers = ref([])
const selectedPassenger = ref(null)

const searchForm = reactive({
  name: '',
  idNumber: '',
  flightNumber: '',
  bookingStatus: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 方法
const getIdTypeText = (type) => {
  const typeMap = {
    'id_card': '身份证',
    'passport': '护照',
    'military_id': '军官证',
    'other': '其他'
  }
  return typeMap[type] || '未知'
}

const getCabinClassText = (cabinClass) => {
  const classMap = {
    'economy': '经济舱',
    'business': '商务舱',
    'first': '头等舱'
  }
  return classMap[cabinClass] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    'booked': 'info',
    'paid': 'warning',
    'checked_in': 'primary',
    'boarded': 'success',
    'cancelled': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'booked': '已预订',
    'paid': '已支付',
    'checked_in': '已值机',
    'boarded': '已登机',
    'cancelled': '已取消'
  }
  return textMap[status] || '未知'
}

const loadPassengers = async () => {
  try {
    loading.value = true
    
    // 模拟乘客数据
    const mockPassengers = [
      {
        id: 1,
        name: '张三',
        gender: 'M',
        idType: 'id_card',
        idNumber: '110101199001011234',
        phone: '13800138000',
        email: 'zhangsan@example.com',
        flightNumber: 'CA1234',
        seatNumber: '12A',
        cabinClass: 'economy',
        bookingTime: '2024-01-18 10:30:00',
        bookingStatus: 'paid',
        ticketPrice: 1200,
        specialRequests: ['素食餐', '靠窗座位'],
        luggage: {
          checked: 1,
          carryon: 1
        }
      },
      {
        id: 2,
        name: '李四',
        gender: 'F',
        idType: 'id_card',
        idNumber: '110101199002021234',
        phone: '13800138001',
        email: 'lisi@example.com',
        flightNumber: 'CA5678',
        seatNumber: '8C',
        cabinClass: 'business',
        bookingTime: '2024-01-18 15:20:00',
        bookingStatus: 'checked_in',
        ticketPrice: 2800,
        specialRequests: ['轮椅服务'],
        luggage: {
          checked: 2,
          carryon: 1
        }
      }
    ]

    passengers.value = mockPassengers
    pagination.total = mockPassengers.length

  } catch (error) {
    console.error('加载乘客列表失败:', error)
    ElMessage.error('加载乘客列表失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    idNumber: '',
    flightNumber: '',
    bookingStatus: ''
  })
  pagination.page = 1
  loadPassengers()
}

const viewDetail = (passenger) => {
  selectedPassenger.value = passenger
  showDetailDialog.value = true
}

const checkIn = async (passenger) => {
  try {
    await ElMessageBox.confirm(`确定为乘客 ${passenger.name} 办理值机吗？`, '确认值机')
    
    passenger.bookingStatus = 'checked_in'
    ElMessage.success('值机成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('值机失败:', error)
      ElMessage.error('值机失败')
    }
  }
}

const cancelBooking = async (passenger) => {
  try {
    await ElMessageBox.confirm(`确定要取消乘客 ${passenger.name} 的订票吗？`, '确认取消', {
      type: 'warning'
    })
    
    passenger.bookingStatus = 'cancelled'
    ElMessage.success('订票已取消')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订票失败:', error)
      ElMessage.error('取消订票失败')
    }
  }
}

// 生命周期
onMounted(() => {
  loadPassengers()
})
</script>

<style scoped>
.passenger-management {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
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

.passenger-detail {
  padding: 20px 0;
}

.special-requests, .luggage-info {
  margin-top: 20px;
}

.special-requests h4, .luggage-info h4 {
  margin-bottom: 10px;
  color: #303133;
}

.luggage-info p {
  margin: 5px 0;
  color: #606266;
}
</style>
