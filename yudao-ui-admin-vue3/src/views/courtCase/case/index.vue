<template>
  <ContentWrap>
    <div class="text-16px font-600 mb-12px">案件管理</div>
    <el-alert
      title="这一版已经接上案件主表、状态机推进和有限动态模型。扩展字段由案件模型页面统一配置，建档时按当前发布版本动态渲染。"
      type="info"
      :closable="false"
      class="mb-16px"
    />

    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px">
      <el-form-item label="案件编号" prop="caseNo">
        <el-input v-model="queryParams.caseNo" placeholder="请输入案件编号" clearable class="!w-220px" />
      </el-form-item>
      <el-form-item label="客户姓名" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户姓名" clearable class="!w-220px" />
      </el-form-item>
      <el-form-item label="当前阶段" prop="currentStage">
        <el-select v-model="queryParams.currentStage" placeholder="请选择阶段" clearable class="!w-220px">
          <el-option v-for="item in stageOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-220px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
        <el-button v-hasPermi="['court-case:case:create']" plain type="primary" @click="openCreateDialog">
          <Icon icon="ep:plus" class="mr-5px" />
          新建案件
        </el-button>
        <el-button v-hasPermi="['court-case:model:query']" plain @click="router.push('/court-case/model')">
          <Icon icon="ep:setting" class="mr-5px" />
          案件模型
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="案件编号" prop="caseNo" min-width="160" />
      <el-table-column label="订单号" prop="orderNo" min-width="180" />
      <el-table-column label="客户姓名" prop="customerName" min-width="120" />
      <el-table-column label="联系电话" prop="mobile" min-width="140" />
      <el-table-column label="涉案金额" prop="amount" min-width="120" />
      <el-table-column label="应还款日期" prop="repaymentDueDate" min-width="140" />
      <el-table-column label="客户状态" min-width="140">
        <template #default="{ row }">
          {{ customerStatusLabelMap[row.customerStatus] || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="下次提醒" min-width="180">
        <template #default="{ row }">
          {{ row.nextRemindTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="最近跟进" min-width="180">
        <template #default="{ row }">
          {{ row.lastFollowUpTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        v-for="field in modelFields"
        :key="field.fieldCode"
        :label="field.fieldLabel"
        min-width="150"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          <template v-if="field.fieldType === 'MULTI_SELECT'">
            {{ getDynamicFieldDisplay(row, field).join('、') || '-' }}
          </template>
          <template v-else>
            {{ getDynamicFieldDisplay(row, field) || '-' }}
          </template>
        </template>
      </el-table-column>
      <el-table-column label="当前阶段" min-width="120">
        <template #default="{ row }">
          <el-tag :type="stageTagTypeMap[row.currentStage] || 'info'">
            {{ getStageLabel(row.currentStage) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagTypeMap[row.status] || 'info'">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="建档时间" prop="createTime" min-width="180" :formatter="dateFormatter" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openLogDialog(row)">流转记录</el-button>
          <el-button
            v-if="actionOptionsMap[row.currentStage]?.length"
            v-hasPermi="['court-case:case:advance']"
            link
            type="primary"
            @click="openAdvanceDialog(row)"
          >
            推进
          </el-button>
          <el-button v-hasPermi="['court-case:case:create']" link type="danger" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <Dialog v-model="createDialogVisible" title="新建案件" width="620px">
    <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
      <el-form-item label="案件编号" prop="caseNo">
        <el-input v-model="createForm.caseNo" placeholder="请输入案件编号" />
      </el-form-item>
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="createForm.orderNo" placeholder="请输入订单号" />
      </el-form-item>
      <el-form-item label="客户姓名" prop="customerName">
        <el-input v-model="createForm.customerName" placeholder="请输入客户姓名" />
      </el-form-item>
      <el-form-item label="联系电话" prop="mobile">
        <el-input v-model="createForm.mobile" placeholder="请输入联系电话" />
      </el-form-item>
      <el-form-item label="涉案金额" prop="amount">
        <el-input-number v-model="createForm.amount" :min="0" :precision="2" class="!w-full" />
      </el-form-item>
      <el-form-item label="应还款日期" prop="repaymentDueDate">
        <el-date-picker
          v-model="createForm.repaymentDueDate"
          type="date"
          value-format="YYYY-MM-DD"
          class="!w-full"
          placeholder="请选择应还款日期"
        />
      </el-form-item>
      <el-form-item label="首位负责人" prop="currentAssigneeId">
        <el-input v-model="createForm.currentAssigneeId" placeholder="选填，默认当前登录人" />
      </el-form-item>
      <template v-if="modelFields.length">
        <el-divider content-position="left">扩展字段</el-divider>
        <el-form-item v-for="field in modelFields" :key="field.fieldCode" :label="field.fieldLabel">
          <el-input
            v-if="field.fieldType === 'TEXT'"
            v-model="createExtForm[field.fieldCode]"
            :placeholder="`请输入${field.fieldLabel}`"
          />
          <el-date-picker
            v-else-if="field.fieldType === 'DATE'"
            v-model="createExtForm[field.fieldCode]"
            type="date"
            value-format="YYYY-MM-DD"
            class="!w-full"
            :placeholder="`请选择${field.fieldLabel}`"
          />
          <el-input-number
            v-else-if="field.fieldType === 'NUMBER'"
            v-model="createExtForm[field.fieldCode]"
            class="!w-full"
          />
          <el-select
            v-else-if="field.fieldType === 'SINGLE_SELECT'"
            v-model="createExtForm[field.fieldCode]"
            class="!w-full"
            clearable
            :placeholder="`请选择${field.fieldLabel}`"
          >
            <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
          <el-select
            v-else
            v-model="createExtForm[field.fieldCode]"
            class="!w-full"
            clearable
            multiple
            collapse-tags
            collapse-tags-tooltip
            :placeholder="`请选择${field.fieldLabel}`"
          >
            <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
          <div class="mt-6px text-12px text-[var(--el-text-color-secondary)]">
            {{ field.required ? '必填字段' : '选填字段' }}
          </div>
        </el-form-item>
      </template>
      <el-form-item v-else label="扩展字段">
        <el-alert type="warning" :closable="false" title="当前还没有发布案件模型，暂时只显示基础字段。" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="createSubmitting" @click="submitCreate">确定</el-button>
    </template>
  </Dialog>

  <Dialog v-model="advanceDialogVisible" title="推进案件" width="520px">
    <el-form ref="advanceFormRef" :model="advanceForm" :rules="advanceRules" label-width="100px">
      <el-form-item label="案件编号">
        <el-input :model-value="currentCase?.caseNo || ''" disabled />
      </el-form-item>
      <el-form-item label="当前阶段">
        <el-input :model-value="getStageLabel(currentCase?.currentStage)" disabled />
      </el-form-item>
      <el-form-item label="推进动作" prop="action">
        <el-select v-model="advanceForm.action" placeholder="请选择动作" class="!w-full">
          <el-option
            v-for="item in currentActionOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="下一负责人" prop="nextAssigneeId">
        <el-input v-model="advanceForm.nextAssigneeId" placeholder="选填，默认当前登录人继续负责" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="advanceForm.remark" type="textarea" :rows="4" placeholder="请输入交接说明或处理备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="advanceDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="advanceSubmitting" @click="submitAdvance">确定</el-button>
    </template>
  </Dialog>

  <Dialog v-model="logDialogVisible" title="案件流转记录" width="760px">
    <div class="mb-12px text-14px">
      当前案件：
      <span class="font-600">{{ currentCase?.caseNo || '-' }}</span>
    </div>
    <el-table v-loading="logLoading" :data="logList">
      <el-table-column label="动作" prop="action" min-width="140" />
      <el-table-column label="原阶段" min-width="120">
        <template #default="{ row }">
          {{ getStageLabel(row.fromStage) }}
        </template>
      </el-table-column>
      <el-table-column label="目标阶段" min-width="120">
        <template #default="{ row }">
          {{ getStageLabel(row.toStage) }}
        </template>
      </el-table-column>
      <el-table-column label="操作人" prop="operatorId" min-width="100" />
      <el-table-column label="备注" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column label="时间" prop="createTime" min-width="180" :formatter="dateFormatter" />
    </el-table>
  </Dialog>
</template>

<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus'
import { dateFormatter } from '@/utils/formatTime'
import * as CourtCaseApi from '@/api/courtCase/case'
import * as CourtCaseModelApi from '@/api/courtCase/model'
import {
  actionOptionsMap,
  getStageLabel,
  getStatusLabel,
  stageLabelMap,
  stageTagTypeMap,
  statusLabelMap,
  statusTagTypeMap
} from '../components/config'

defineOptions({ name: 'CourtCaseList' })

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const total = ref(0)
const list = ref<CourtCaseApi.CourtCaseVO[]>([])
const queryFormRef = ref<FormInstance>()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  caseNo: undefined,
  customerName: undefined,
  currentStage: undefined,
  status: undefined
})

const createDialogVisible = ref(false)
const createSubmitting = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  caseNo: '',
  orderNo: '',
  customerName: '',
  mobile: '',
  amount: 0,
  repaymentDueDate: '',
  currentAssigneeId: ''
})
const createRules = reactive<FormRules>({
  caseNo: [{ required: true, message: '请输入案件编号', trigger: 'blur' }],
  orderNo: [{ required: true, message: '请输入订单号', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  mobile: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入涉案金额', trigger: 'blur' }],
  repaymentDueDate: [{ required: true, message: '请选择应还款日期', trigger: 'change' }]
})

const advanceDialogVisible = ref(false)
const advanceSubmitting = ref(false)
const advanceFormRef = ref<FormInstance>()
const currentCase = ref<CourtCaseApi.CourtCaseVO>()
const advanceForm = reactive({
  id: undefined as number | undefined,
  action: '',
  nextAssigneeId: '',
  remark: ''
})
const advanceRules = reactive<FormRules>({
  action: [{ required: true, message: '请选择推进动作', trigger: 'change' }]
})

const logDialogVisible = ref(false)
const logLoading = ref(false)
const logList = ref<any[]>([])
const modelFields = ref<CourtCaseModelApi.CourtCaseModelFieldVO[]>([])
const createExtForm = reactive<Record<string, any>>({})
const caseExtCache = reactive<Record<number, Record<string, any>>>({})

const stageOptions = Object.entries(stageLabelMap)
  .filter(([value]) => value !== 'IMPORT')
  .map(([value, label]) => ({ value, label }))
const statusOptions = Object.entries(statusLabelMap).map(([value, label]) => ({
  value: Number(value),
  label
}))

const currentActionOptions = computed(() => {
  return actionOptionsMap[currentCase.value?.currentStage || ''] || []
})

const customerStatusLabelMap: Record<string, string> = {
  PENDING_REPAY: '待还款',
  REMINDING: '提醒中',
  OVERDUE: '已逾期',
  FOLLOWING: '跟进中',
  TRANSFERRED_TO_LEGAL: '已移交法诉',
  REPAID: '已还款'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await CourtCaseApi.getCourtCasePage(queryParams)
    list.value = data.list
    total.value = data.total
    buildCaseExtCache(data.list)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const resetCreateForm = () => {
  createForm.caseNo = ''
  createForm.orderNo = ''
  createForm.customerName = ''
  createForm.mobile = ''
  createForm.amount = 0
  createForm.repaymentDueDate = ''
  createForm.currentAssigneeId = ''
  resetExtForm()
}

const resetExtForm = () => {
  Object.keys(createExtForm).forEach((key) => {
    delete createExtForm[key]
  })
  modelFields.value.forEach((field) => {
    if (field.fieldType === 'MULTI_SELECT') {
      createExtForm[field.fieldCode] = field.defaultValue ? [field.defaultValue] : []
    } else if (field.fieldType === 'NUMBER') {
      createExtForm[field.fieldCode] = field.defaultValue ? Number(field.defaultValue) : undefined
    } else {
      createExtForm[field.fieldCode] = field.defaultValue || ''
    }
  })
}

const buildCaseExtCache = (rows: CourtCaseApi.CourtCaseVO[]) => {
  Object.keys(caseExtCache).forEach((key) => delete caseExtCache[Number(key)])
  rows.forEach((row) => {
    try {
      caseExtCache[row.id] = row.extJson ? JSON.parse(row.extJson) : {}
    } catch {
      caseExtCache[row.id] = {}
    }
  })
}

const getDynamicFieldDisplay = (row: CourtCaseApi.CourtCaseVO, field: CourtCaseModelApi.CourtCaseModelFieldVO) => {
  const ext = caseExtCache[row.id] || {}
  const value = ext[field.fieldCode]
  if (value === undefined || value === null || value === '') {
    return field.fieldType === 'MULTI_SELECT' ? [] : ''
  }
  if (field.fieldType === 'SINGLE_SELECT') {
    return field.options.find((option) => option.value === value)?.label || value
  }
  if (field.fieldType === 'MULTI_SELECT') {
    const values = Array.isArray(value) ? value : [value]
    return values.map((item) => field.options.find((option) => option.value === item)?.label || item)
  }
  return value
}

const openCreateDialog = () => {
  resetCreateForm()
  createDialogVisible.value = true
}

const submitCreate = async () => {
  await createFormRef.value?.validate()
  createSubmitting.value = true
  try {
    const extPayload = modelFields.value.reduce((acc: Record<string, any>, field) => {
      const value = createExtForm[field.fieldCode]
      if (Array.isArray(value) ? value.length : value !== undefined && value !== null && value !== '') {
        acc[field.fieldCode] = value
      }
      return acc
    }, {})
    await CourtCaseApi.createCourtCase({
      ...createForm,
      currentAssigneeId: createForm.currentAssigneeId ? Number(createForm.currentAssigneeId) : undefined,
      extJson: Object.keys(extPayload).length ? JSON.stringify(extPayload) : undefined
    })
    message.success('案件建档成功')
    createDialogVisible.value = false
    getList()
  } finally {
    createSubmitting.value = false
  }
}

const getModelFields = async () => {
  try {
    const data = await CourtCaseModelApi.getCourtCaseModelConfig()
    modelFields.value = (data.published?.fields || []).filter((field) => field.enabled).sort((a, b) => a.sortNo - b.sortNo)
    resetExtForm()
    buildCaseExtCache(list.value)
  } catch {
    modelFields.value = []
  }
}

const openAdvanceDialog = (row: CourtCaseApi.CourtCaseVO) => {
  currentCase.value = row
  advanceForm.id = row.id
  advanceForm.action = actionOptionsMap[row.currentStage]?.[0]?.value || ''
  advanceForm.nextAssigneeId = ''
  advanceForm.remark = ''
  advanceDialogVisible.value = true
}

const submitAdvance = async () => {
  await advanceFormRef.value?.validate()
  advanceSubmitting.value = true
  try {
    await CourtCaseApi.advanceCourtCase({
      id: advanceForm.id!,
      action: advanceForm.action,
      nextAssigneeId: advanceForm.nextAssigneeId ? Number(advanceForm.nextAssigneeId) : undefined,
      remark: advanceForm.remark
    })
    message.success('案件已推进到下一阶段')
    advanceDialogVisible.value = false
    getList()
  } finally {
    advanceSubmitting.value = false
  }
}

const openLogDialog = async (row: CourtCaseApi.CourtCaseVO) => {
  currentCase.value = row
  logDialogVisible.value = true
  logLoading.value = true
  try {
    logList.value = await CourtCaseApi.getCourtCaseFlowLogList(row.id)
  } finally {
    logLoading.value = false
  }
}

const handleDelete = async (row: CourtCaseApi.CourtCaseVO) => {
  await message.confirm(`确定删除案件「${row.caseNo}」吗？删除后流转记录也会一并清除。`)
  await CourtCaseApi.deleteCourtCase(row.id)
  message.success('案件删除成功')
  await getList()
}

onMounted(() => {
  getModelFields()
  getList()
})
</script>
