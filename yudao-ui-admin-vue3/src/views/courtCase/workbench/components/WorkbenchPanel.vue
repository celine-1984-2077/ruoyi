<template>
  <ContentWrap>
    <div class="workbench-hero">
      <div>
        <div class="workbench-hero__eyebrow">案件中心</div>
        <div class="workbench-hero__title">{{ heroTitle }}</div>
        <div class="workbench-hero__desc">
          {{ heroDesc }}
        </div>
      </div>
      <div class="workbench-hero__actions">
        <el-button plain @click="router.push('/court-case/case')">
          <Icon icon="ep:tickets" class="mr-5px" />
          案件台账
        </el-button>
        <el-button v-hasPermi="['court-case:model:query']" type="primary" plain @click="router.push('/court-case/model')">
          <Icon icon="ep:setting" class="mr-5px" />
          案件模型
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <div class="workbench-grid workbench-grid--summary mb-16px">
    <div
      v-for="card in visibleSummaryCards"
      :key="card.key"
      class="summary-card"
      :class="{ 'is-active': activeStage === card.stage }"
      @click="activateStage(card.stage)"
    >
      <div class="summary-card__label">{{ card.label }}</div>
      <div class="summary-card__value">{{ card.count }}</div>
      <div class="summary-card__meta">{{ card.desc }}</div>
    </div>
  </div>

  <div class="workbench-grid mb-16px">
    <ContentWrap class="queue-panel">
      <div class="queue-panel__header">
        <div>
          <div class="queue-panel__title">{{ primaryQueueTitle }}</div>
          <div class="queue-panel__subtitle">{{ primaryQueueSubtitle }}</div>
        </div>
        <el-tag :type="mode === 'service' ? 'warning' : 'primary'">{{ primaryQueueTag }}</el-tag>
      </div>
      <div class="queue-list">
        <button
          v-for="queue in visibleQueues"
          :key="queue.stage"
          class="queue-item"
          :class="{ 'is-active': activeStage === queue.stage }"
          @click="activateStage(queue.stage)"
        >
          <span>{{ queue.label }}</span>
          <strong>{{ queue.count }}</strong>
        </button>
      </div>
      <div class="queue-panel__foot">{{ primaryQueueFoot }}</div>
    </ContentWrap>

    <ContentWrap class="queue-panel">
      <div class="queue-panel__header">
        <div>
          <div class="queue-panel__title">重点案件</div>
          <div class="queue-panel__subtitle">{{ focusSubtitle }}</div>
        </div>
        <el-tag type="info">按优先级排序</el-tag>
      </div>
      <el-table v-loading="urgentLoading" :data="urgentCases" empty-text="当前没有重点案件">
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityMeta(row).tagType">{{ getPriorityMeta(row).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="案件编号" prop="caseNo" min-width="160" />
        <el-table-column label="客户姓名" prop="customerName" min-width="120" />
        <el-table-column label="联系电话" min-width="140">
          <template #default="{ row }">
            {{ maskMobile(row.mobile) }}
          </template>
        </el-table-column>
        <el-table-column label="当前阶段" min-width="120">
          <template #default="{ row }">
            <el-tag :type="stageTagTypeMap[row.currentStage] || 'info'">{{ getStageLabel(row.currentStage) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="actionOptionsMap[row.currentStage]?.length"
              v-hasPermi="['court-case:case:advance']"
              link
              type="danger"
              @click="openAdvanceDialog(row)"
            >
              立即处理
            </el-button>
            <el-button link type="primary" @click="openLogDialog(row)">流转记录</el-button>
          </template>
        </el-table-column>
      </el-table>
    </ContentWrap>
  </div>

  <ContentWrap>
    <div class="panel-head">
      <div>
        <div class="panel-title">{{ listTitle }}</div>
        <div class="panel-subtitle">{{ listSubtitle }}</div>
      </div>
      <div class="panel-chip-list">
        <button class="panel-chip" :class="{ 'is-active': !activeStage }" @click="activateStage(undefined)">全部案件</button>
        <button
          v-for="item in visibleStageOptions"
          :key="item.value"
          class="panel-chip"
          :class="{ 'is-active': activeStage === item.value }"
          @click="activateStage(item.value)"
        >
          {{ item.label }}
        </button>
      </div>
    </div>

    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px">
      <el-form-item label="案件编号" prop="caseNo">
        <el-input v-model="queryParams.caseNo" placeholder="请输入案件编号" clearable class="!w-220px" />
      </el-form-item>
      <el-form-item label="客户姓名" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户姓名" clearable class="!w-220px" />
      </el-form-item>
      <el-form-item label="当前阶段" prop="currentStage">
        <el-select v-model="queryParams.currentStage" placeholder="请选择阶段" clearable class="!w-220px">
          <el-option v-for="item in visibleStageOptions" :key="item.value" :label="item.label" :value="item.value" />
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
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="优先级" width="100">
        <template #default="{ row }">
          <el-tag :type="getPriorityMeta(row).tagType">{{ getPriorityMeta(row).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="案件编号" prop="caseNo" min-width="160" />
      <el-table-column label="客户姓名" prop="customerName" min-width="120" />
      <el-table-column label="联系电话" min-width="140">
        <template #default="{ row }">
          {{ maskMobile(row.mobile) }}
        </template>
      </el-table-column>
      <el-table-column label="订单号" prop="orderNo" min-width="180" />
      <el-table-column label="涉案金额" prop="amount" min-width="120" />
      <el-table-column label="当前阶段" min-width="120">
        <template #default="{ row }">
          <el-tag :type="stageTagTypeMap[row.currentStage] || 'info'">{{ getStageLabel(row.currentStage) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagTypeMap[row.status] || 'info'">{{ getStatusLabel(row.status) }}</el-tag>
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
            处理案件
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
import {
  actionOptionsMap,
  getStageLabel,
  getStatusLabel,
  stageLabelMap,
  stageTagTypeMap,
  statusTagTypeMap
} from '../../components/config'

const props = defineProps<{
  mode: 'service' | 'legal'
}>()

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const urgentLoading = ref(false)
const total = ref(0)
const list = ref<CourtCaseApi.CourtCaseVO[]>([])
const summaryList = ref<CourtCaseApi.CourtCaseVO[]>([])
const queryFormRef = ref<FormInstance>()
const activeStage = ref<string | undefined>()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  caseNo: undefined as string | undefined,
  customerName: undefined as string | undefined,
  currentStage: undefined as string | undefined
})

const serviceStages = ['ASSIGN', 'REMIND', 'TODAY_OVERDUE', 'FOLLOW_UP']
const legalStages = ['LEGAL', 'LITIGATION', 'ARCHIVED']

const allStageOptions = Object.entries(stageLabelMap)
  .filter(([value]) => value !== 'IMPORT')
  .map(([value, label]) => ({ value, label }))

const visibleStageOptions = computed(() => {
  const allowedStages = props.mode === 'service' ? serviceStages : legalStages
  return allStageOptions.filter((item) => allowedStages.includes(item.value))
})
const allowedStages = computed(() => (props.mode === 'service' ? serviceStages : legalStages))

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

const priorityMetaMap: Record<string, { label: string; rank: number; tagType: '' | 'info' | 'success' | 'warning' | 'danger' | 'primary' }> = {
  TODAY_OVERDUE: { label: '高', rank: 4, tagType: 'danger' },
  LEGAL: { label: '高', rank: 4, tagType: 'danger' },
  REMIND: { label: '中', rank: 3, tagType: 'warning' },
  FOLLOW_UP: { label: '中', rank: 3, tagType: 'warning' },
  LITIGATION: { label: '中', rank: 3, tagType: 'primary' },
  ASSIGN: { label: '普通', rank: 2, tagType: 'info' },
  ARCHIVED: { label: '已完成', rank: 1, tagType: 'success' }
}

const heroTitle = computed(() => (props.mode === 'service' ? '客服工作台' : '法务工作台'))
const heroDesc = computed(() =>
  props.mode === 'service'
    ? '围绕客服每日待办来处理案件，优先关注预提醒、当日未还和追客案件。'
    : '围绕法务每日待办来推进案件，聚焦法诉承接、诉讼中案件和归档回收。'
)

const primaryQueueTitle = computed(() => (props.mode === 'service' ? '客服待办' : '法务待办'))
const primaryQueueSubtitle = computed(() =>
  props.mode === 'service' ? '适合今天优先处理的提醒、逾期和追客任务' : '承接移交后的案件，聚焦材料完善与诉讼推进'
)
const primaryQueueTag = computed(() => (props.mode === 'service' ? '客服队列' : '法务队列'))
const primaryQueueFoot = computed(() =>
  props.mode === 'service'
    ? '高优先级案件会优先排序到列表顶部，方便客服先处理临近节点。'
    : '后续接入证据材料、诉状生成和立案状态后，这里会继续扩展为完整法务面板。'
)
const focusSubtitle = computed(() =>
  props.mode === 'service' ? '优先展示还款提醒和当日未还案件' : '优先展示法诉承接和诉讼推进案件'
)
const listTitle = computed(() => (props.mode === 'service' ? '客服待办列表' : '法务待办列表'))
const listSubtitle = computed(() =>
  props.mode === 'service'
    ? '查看当前登录人所属部门下需要客服处理的案件池'
    : '查看当前登录人所属部门下需要法务推进的案件池'
)

const countByStage = (stage: string) => {
  return summaryList.value.filter((item) => item.currentStage === stage && (stage === 'ARCHIVED' || item.status === 10)).length
}

const serviceQueues = computed(() => [
  { stage: 'ASSIGN', label: '待分单', count: countByStage('ASSIGN') },
  { stage: 'REMIND', label: '还款提醒', count: countByStage('REMIND') },
  { stage: 'TODAY_OVERDUE', label: '当日未还', count: countByStage('TODAY_OVERDUE') },
  { stage: 'FOLLOW_UP', label: '追客跟进', count: countByStage('FOLLOW_UP') }
])

const legalQueues = computed(() => [
  { stage: 'LEGAL', label: '待法务承接', count: countByStage('LEGAL') },
  { stage: 'LITIGATION', label: '诉讼推进中', count: countByStage('LITIGATION') },
  { stage: 'ARCHIVED', label: '已归档案件', count: countByStage('ARCHIVED') }
])

const serviceSummaryCards = computed(() => [
  { key: 'assign', stage: 'ASSIGN', label: '待分单', count: countByStage('ASSIGN'), desc: '适合主管分配到客服' },
  { key: 'remind', stage: 'REMIND', label: '7天内提醒', count: countByStage('REMIND'), desc: '客服优先联系，提前触达客户' },
  { key: 'overdue', stage: 'TODAY_OVERDUE', label: '当日未还', count: countByStage('TODAY_OVERDUE'), desc: '高优先级，建议先处理' },
  { key: 'follow-up', stage: 'FOLLOW_UP', label: '追客处理中', count: countByStage('FOLLOW_UP'), desc: '跟进无果后可继续衔接法务' }
])

const legalSummaryCards = computed(() => [
  { key: 'legal', stage: 'LEGAL', label: '待法务承接', count: countByStage('LEGAL'), desc: '等待证据整理与法诉接手' },
  { key: 'litigation', stage: 'LITIGATION', label: '诉讼推进中', count: countByStage('LITIGATION'), desc: '持续跟踪立案、驳回和补充材料' },
  { key: 'archived', stage: 'ARCHIVED', label: '已归档', count: countByStage('ARCHIVED'), desc: '已完结案件，可回看流转轨迹' }
])

const visibleQueues = computed(() => (props.mode === 'service' ? serviceQueues.value : legalQueues.value))
const visibleSummaryCards = computed(() => (props.mode === 'service' ? serviceSummaryCards.value : legalSummaryCards.value))

const filteredSummaryCases = computed(() => {
  const allowedStages = props.mode === 'service' ? serviceStages : legalStages
  return summaryList.value.filter((item) => allowedStages.includes(item.currentStage))
})

const urgentCases = computed(() => {
  return [...filteredSummaryCases.value]
    .sort((left, right) => getPriorityMeta(right).rank - getPriorityMeta(left).rank)
    .slice(0, 6)
})

const currentActionOptions = computed(() => {
  return actionOptionsMap[currentCase.value?.currentStage || ''] || []
})

const getPriorityMeta = (row: CourtCaseApi.CourtCaseVO) => {
  return priorityMetaMap[row.currentStage] || { label: '普通', rank: 2, tagType: 'info' as const }
}

const maskMobile = (mobile?: string) => {
  if (!mobile) {
    return '-'
  }
  return mobile.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

const filterCasesByMode = (rows: CourtCaseApi.CourtCaseVO[]) => {
  return rows.filter((item) => allowedStages.value.includes(item.currentStage))
}

const getList = async () => {
  loading.value = true
  try {
    const fetchParams = {
      pageNo: 1,
      pageSize: 200,
      caseNo: queryParams.caseNo,
      customerName: queryParams.customerName,
      currentStage: queryParams.currentStage
    }
    const data = await CourtCaseApi.getCourtCaseWorkbenchPage(fetchParams)
    const filtered = filterCasesByMode(data.list)
    total.value = filtered.length
    const start = (queryParams.pageNo - 1) * queryParams.pageSize
    const end = start + queryParams.pageSize
    list.value = filtered.slice(start, end)
  } finally {
    loading.value = false
  }
}

const getSummaryList = async () => {
  urgentLoading.value = true
  try {
    const data = await CourtCaseApi.getCourtCaseWorkbenchPage({
      pageNo: 1,
      pageSize: 200
    })
    summaryList.value = filterCasesByMode(data.list)
  } finally {
    urgentLoading.value = false
  }
}

const activateStage = async (stage?: string) => {
  activeStage.value = stage
  queryParams.currentStage = stage
  queryParams.pageNo = 1
  await Promise.all([getSummaryList(), getList()])
}

const handleQuery = async () => {
  activeStage.value = queryParams.currentStage
  queryParams.pageNo = 1
  await Promise.all([getSummaryList(), getList()])
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  await activateStage(undefined)
}

const openAdvanceDialog = (row: CourtCaseApi.CourtCaseVO) => {
  currentCase.value = row
  advanceForm.id = row.id
  advanceForm.action = ''
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
      remark: advanceForm.remark || undefined
    })
    message.success('案件推进成功')
    advanceDialogVisible.value = false
    await Promise.all([getSummaryList(), getList()])
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

onMounted(async () => {
  await Promise.all([getSummaryList(), getList()])
})
</script>

<style scoped>
.workbench-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.workbench-grid--summary {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.workbench-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-start;
  padding: 6px 4px;
}

.workbench-hero__eyebrow {
  color: var(--el-color-primary);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.workbench-hero__title {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--el-text-color-primary);
}

.workbench-hero__desc {
  max-width: 760px;
  margin-top: 10px;
  line-height: 1.8;
  color: var(--el-text-color-secondary);
}

.workbench-hero__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.summary-card {
  padding: 20px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 18px;
  cursor: pointer;
  background:
    radial-gradient(circle at top right, rgb(64 158 255 / 0.16), transparent 35%),
    linear-gradient(180deg, var(--el-bg-color) 0%, var(--el-fill-color-extra-light) 100%);
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.summary-card:hover,
.summary-card.is-active {
  transform: translateY(-2px);
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 10px 24px rgb(31 35 41 / 0.08);
}

.summary-card__label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.summary-card__value {
  margin-top: 14px;
  font-size: 32px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.summary-card__meta {
  margin-top: 8px;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
}

.queue-panel {
  height: 100%;
}

.queue-panel__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.queue-panel__title,
.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.queue-panel__subtitle,
.panel-subtitle {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  line-height: 1.7;
}

.queue-list {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.queue-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 14px;
  color: var(--el-text-color-primary);
  background: var(--el-fill-color-extra-light);
  transition:
    border-color 0.18s ease,
    transform 0.18s ease,
    background 0.18s ease;
}

.queue-item:hover,
.queue-item.is-active {
  transform: translateY(-1px);
  border-color: var(--el-color-primary-light-5);
  background: rgb(64 158 255 / 0.08);
}

.queue-item strong {
  font-size: 20px;
}

.queue-panel__foot {
  margin-top: 14px;
  color: var(--el-text-color-secondary);
  line-height: 1.7;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-end;
  margin-bottom: 16px;
}

.panel-chip-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.panel-chip {
  padding: 8px 14px;
  border: 1px solid var(--el-border-color);
  border-radius: 999px;
  color: var(--el-text-color-regular);
  background: var(--el-bg-color);
  transition: all 0.18s ease;
}

.panel-chip.is-active,
.panel-chip:hover {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-5);
  background: rgb(64 158 255 / 0.08);
}

@media (max-width: 1200px) {
  .workbench-grid--summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .workbench-grid,
  .workbench-grid--summary {
    grid-template-columns: minmax(0, 1fr);
  }

  .workbench-hero,
  .panel-head {
    flex-direction: column;
  }
}
</style>
