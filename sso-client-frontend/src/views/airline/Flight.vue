<template>
  <div class="flight-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><Ship /></el-icon>
          <span>航班管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="showAddDialog = true">
              <el-icon><Plus /></el-icon>
              新增航班
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="航班号">
            <el-input v-model="searchForm.flightNumber" placeholder="请输入航班号" clearable />
          </el-form-item>
          <el-form-item label="出发地">
            <el-input v-model="searchForm.departure" placeholder="请输入出发地" clearable />
          </el-form-item>
          <el-form-item label="目的地">
            <el-input v-model="searchForm.arrival" placeholder="请输入目的地" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="计划" value="scheduled" />
              <el-option label="登机" value="boarding" />
              <el-option label="已起飞" value="departed" />
              <el-option label="已到达" value="arrived" />
              <el-option label="延误" value="delayed" />
              <el-option label="取消" value="cancelled" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadFlights">
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

      <!-- 航班表格 -->
      <el-table :data="flights" v-loading="loading" style="width: 100%">
        <el-table-column prop="flightNumber" label="航班号" width="120" />
        <el-table-column prop="route" label="航线" width="200">
          <template #default="scope">
            {{ scope.row.departure }} → {{ scope.row.arrival }}
          </template>
        </el-table-column>
        <el-table-column prop="aircraft" label="机型" width="120" />
        <el-table-column prop="departureTime" label="计划起飞" width="160" />
        <el-table-column prop="arrivalTime" label="计划到达" width="160" />
        <el-table-column prop="actualDepartureTime" label="实际起飞" width="160" />
        <el-table-column prop="actualArrivalTime" label="实际到达" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="passengers" label="乘客数" width="80" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editFlight(scope.row)">
              编辑
            </el-button>
            <el-button type="info" size="small" @click="viewDetail(scope.row)">
              详情
            </el-button>
            <el-button type="danger" size="small" @click="deleteFlight(scope.row)">
              删除
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
          @size-change="loadFlights"
          @current-change="loadFlights"
        />
      </div>
    </el-card>

    <!-- 新增/编辑航班对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingFlight ? '编辑航班' : '新增航班'"
      width="800px"
    >
      <el-form
        ref="flightFormRef"
        :model="flightForm"
        :rules="flightRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="航班号" prop="flightNumber">
              <el-input v-model="flightForm.flightNumber" placeholder="请输入航班号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="机型" prop="aircraft">
              <el-select v-model="flightForm.aircraft" placeholder="请选择机型" style="width: 100%">
                <el-option label="Boeing 737" value="B737" />
                <el-option label="Boeing 777" value="B777" />
                <el-option label="Airbus A320" value="A320" />
                <el-option label="Airbus A330" value="A330" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出发地" prop="departure">
              <el-input v-model="flightForm.departure" placeholder="请输入出发地" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目的地" prop="arrival">
              <el-input v-model="flightForm.arrival" placeholder="请输入目的地" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划起飞时间" prop="departureTime">
              <el-date-picker
                v-model="flightForm.departureTime"
                type="datetime"
                placeholder="请选择起飞时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划到达时间" prop="arrivalTime">
              <el-date-picker
                v-model="flightForm.arrivalTime"
                type="datetime"
                placeholder="请选择到达时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="座位数" prop="seatCount">
              <el-input-number 
                v-model="flightForm.seatCount" 
                :min="1" 
                placeholder="请输入座位数"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="票价" prop="price">
              <el-input-number 
                v-model="flightForm.price" 
                :min="0" 
                :precision="2"
                placeholder="请输入票价"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="flightForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveFlight" :loading="saving">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Ship, Plus, Search, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showAddDialog = ref(false)
const editingFlight = ref(null)
const flightFormRef = ref()
const flights = ref([])

const searchForm = reactive({
  flightNumber: '',
  departure: '',
  arrival: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const flightForm = reactive({
  flightNumber: '',
  aircraft: '',
  departure: '',
  arrival: '',
  departureTime: '',
  arrivalTime: '',
  seatCount: 0,
  price: 0,
  remark: ''
})

const flightRules = {
  flightNumber: [
    { required: true, message: '请输入航班号', trigger: 'blur' }
  ],
  aircraft: [
    { required: true, message: '请选择机型', trigger: 'change' }
  ],
  departure: [
    { required: true, message: '请输入出发地', trigger: 'blur' }
  ],
  arrival: [
    { required: true, message: '请输入目的地', trigger: 'blur' }
  ],
  departureTime: [
    { required: true, message: '请选择起飞时间', trigger: 'change' }
  ],
  arrivalTime: [
    { required: true, message: '请选择到达时间', trigger: 'change' }
  ]
}

// 方法
const getStatusType = (status) => {
  const typeMap = {
    'scheduled': 'info',
    'boarding': 'warning',
    'departed': 'primary',
    'arrived': 'success',
    'delayed': 'warning',
    'cancelled': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'scheduled': '计划',
    'boarding': '登机',
    'departed': '已起飞',
    'arrived': '已到达',
    'delayed': '延误',
    'cancelled': '取消'
  }
  return textMap[status] || '未知'
}

const loadFlights = async () => {
  try {
    loading.value = true
    
    // 模拟航班数据
    const mockFlights = [
      {
        id: 1,
        flightNumber: 'CA1234',
        departure: '北京首都机场',
        arrival: '上海浦东机场',
        aircraft: 'B737',
        departureTime: '2024-01-19 08:00',
        arrivalTime: '2024-01-19 10:30',
        actualDepartureTime: '2024-01-19 08:05',
        actualArrivalTime: '2024-01-19 10:35',
        status: 'arrived',
        passengers: 156,
        seatCount: 180,
        price: 1200
      },
      {
        id: 2,
        flightNumber: 'CA5678',
        departure: '上海浦东机场',
        arrival: '广州白云机场',
        aircraft: 'A320',
        departureTime: '2024-01-19 14:00',
        arrivalTime: '2024-01-19 16:30',
        actualDepartureTime: '',
        actualArrivalTime: '',
        status: 'scheduled',
        passengers: 0,
        seatCount: 160,
        price: 800
      }
    ]

    flights.value = mockFlights
    pagination.total = mockFlights.length

  } catch (error) {
    console.error('加载航班列表失败:', error)
    ElMessage.error('加载航班列表失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  Object.assign(searchForm, {
    flightNumber: '',
    departure: '',
    arrival: '',
    status: ''
  })
  pagination.page = 1
  loadFlights()
}

const editFlight = (flight) => {
  editingFlight.value = flight
  Object.assign(flightForm, {
    flightNumber: flight.flightNumber,
    aircraft: flight.aircraft,
    departure: flight.departure,
    arrival: flight.arrival,
    departureTime: flight.departureTime,
    arrivalTime: flight.arrivalTime,
    seatCount: flight.seatCount,
    price: flight.price,
    remark: flight.remark || ''
  })
  showAddDialog.value = true
}

const saveFlight = async () => {
  try {
    await flightFormRef.value.validate()
    saving.value = true

    if (editingFlight.value) {
      ElMessage.success('航班信息更新成功')
    } else {
      ElMessage.success('航班创建成功')
    }

    showAddDialog.value = false
    resetFlightForm()
    loadFlights()
  } catch (error) {
    console.error('保存航班失败:', error)
    ElMessage.error('保存航班失败')
  } finally {
    saving.value = false
  }
}

const viewDetail = (flight) => {
  ElMessage.info(`查看航班 ${flight.flightNumber} 详情功能开发中...`)
}

const deleteFlight = async (flight) => {
  try {
    await ElMessageBox.confirm(`确定要删除航班 ${flight.flightNumber} 吗？`, '确认删除', {
      type: 'warning'
    })
    
    ElMessage.success('航班删除成功')
    loadFlights()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除航班失败:', error)
      ElMessage.error('删除航班失败')
    }
  }
}

const resetFlightForm = () => {
  editingFlight.value = null
  Object.assign(flightForm, {
    flightNumber: '',
    aircraft: '',
    departure: '',
    arrival: '',
    departureTime: '',
    arrivalTime: '',
    seatCount: 0,
    price: 0,
    remark: ''
  })
  flightFormRef.value?.resetFields()
}

// 生命周期
onMounted(() => {
  loadFlights()
})
</script>

<style scoped>
.flight-management {
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
</style>
