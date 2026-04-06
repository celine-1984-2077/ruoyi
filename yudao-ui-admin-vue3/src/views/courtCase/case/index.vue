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
          <el-button link type="primary" @click="openDetailDrawer(row)">查看详情</el-button>
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

  <el-drawer v-model="detailDrawerVisible" title="案件详情总览" size="78%">
    <template v-if="detailCase">
      <div v-loading="detailLoading">
        <el-row :gutter="12" class="mb-16px">
          <el-col :span="6">
            <div class="detail-stat-card">
              <div class="detail-stat-label">案件编号</div>
              <div class="detail-stat-value">{{ detailCase.caseNo || '-' }}</div>
              <div class="detail-stat-desc">{{ detailCase.orderNo || '-' }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="detail-stat-card">
              <div class="detail-stat-label">客户 / 状态</div>
              <div class="detail-stat-value">{{ detailCase.customerName || '-' }}</div>
              <div class="detail-stat-desc">
                {{ customerStatusLabelMap[detailCase.customerStatus || ''] || '-' }}
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="detail-stat-card">
              <div class="detail-stat-label">当前阶段</div>
              <div class="detail-stat-value">{{ getStageLabel(detailCase.currentStage) }}</div>
              <div class="detail-stat-desc">{{ getStatusLabel(detailCase.status) }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="detail-stat-card">
              <div class="detail-stat-label">法务概览</div>
              <div class="detail-stat-value">
                证据 {{ detailEvidenceList.length }} / 诉状 {{ detailPetitionList.length }}
              </div>
              <div class="detail-stat-desc">{{ getFilingStatusLabel(detailFiling.status) }}</div>
            </div>
          </el-col>
        </el-row>

        <el-tabs v-model="detailActiveTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="案件编号">{{ detailCase.caseNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="订单号">{{ detailCase.orderNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="客户姓名">{{ detailCase.customerName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ detailCase.mobile || '-' }}</el-descriptions-item>
              <el-descriptions-item label="涉案金额">{{ detailCase.amount ?? '-' }}</el-descriptions-item>
              <el-descriptions-item label="应还款日期">{{ detailCase.repaymentDueDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="客户状态">
                {{ customerStatusLabelMap[detailCase.customerStatus || ''] || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="当前阶段">
                {{ getStageLabel(detailCase.currentStage) }}
              </el-descriptions-item>
              <el-descriptions-item label="案件状态">{{ getStatusLabel(detailCase.status) }}</el-descriptions-item>
              <el-descriptions-item label="建档时间">{{ detailCase.createTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="下次提醒">{{ detailCase.nextRemindTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="最近跟进">{{ detailCase.lastFollowUpTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="移交法务时间">{{ detailCase.transferTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="撤回截止">{{ detailCase.transferRecallDeadline || '-' }}</el-descriptions-item>
            </el-descriptions>

            <el-divider content-position="left">动态字段</el-divider>
            <el-descriptions v-if="modelFields.length" :column="2" border>
              <el-descriptions-item
                v-for="field in modelFields"
                :key="field.fieldCode"
                :label="field.fieldLabel"
              >
                <template v-if="field.fieldType === 'MULTI_SELECT'">
                  {{ getDetailDynamicFieldDisplay(field).join('、') || '-' }}
                </template>
                <template v-else>
                  {{ getDetailDynamicFieldDisplay(field) || '-' }}
                </template>
              </el-descriptions-item>
            </el-descriptions>
            <el-empty v-else description="当前没有动态字段" />
          </el-tab-pane>

          <el-tab-pane label="客服信息" name="service">
            <el-descriptions :column="2" border class="mb-16px">
              <el-descriptions-item label="下次提醒时间">{{ detailCase.nextRemindTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="提醒内容">{{ detailCase.nextRemindContent || '-' }}</el-descriptions-item>
              <el-descriptions-item label="是否已还款">{{ detailCase.repaid ? '已还款' : '未还款' }}</el-descriptions-item>
              <el-descriptions-item label="还款时间">{{ detailCase.repaidTime || '-' }}</el-descriptions-item>
            </el-descriptions>

            <div class="section-title">跟进记录</div>
            <el-timeline v-if="detailFollowUpList.length">
              <el-timeline-item
                v-for="item in detailFollowUpList"
                :key="item.id"
                :timestamp="item.createTime"
                placement="top"
              >
                <div class="font-600 mb-4px">{{ item.operatorName || `用户${item.operatorId}` }}</div>
                <div class="mb-8px whitespace-pre-wrap">{{ item.content }}</div>
                <div v-if="splitAttachmentUrls(item.attachmentUrls).length" class="flex flex-wrap gap-8px">
                  <el-link
                    v-for="url in splitAttachmentUrls(item.attachmentUrls)"
                    :key="url"
                    :href="url"
                    target="_blank"
                    type="primary"
                  >
                    查看附件
                  </el-link>
                </div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无跟进记录" />
          </el-tab-pane>

          <el-tab-pane label="法务信息" name="legal">
            <el-descriptions :column="2" border class="mb-16px">
              <el-descriptions-item label="立案法院">{{ detailFiling.courtName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="立案编号">{{ detailFiling.filingNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="提交时间">{{ detailFiling.submitTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="立案状态">{{ getFilingStatusLabel(detailFiling.status) }}</el-descriptions-item>
              <el-descriptions-item label="驳回原因" :span="2">
                {{ detailFiling.rejectReason || '-' }}
              </el-descriptions-item>
            </el-descriptions>

            <div class="section-title">证据材料</div>
            <el-table :data="detailEvidenceList" class="mb-20px">
              <el-table-column label="分类" min-width="140">
                <template #default="{ row }">
                  {{ evidenceCategoryLabelMap[row.category] || row.category }}
                </template>
              </el-table-column>
              <el-table-column label="文件名" prop="fileName" min-width="220" show-overflow-tooltip />
              <el-table-column label="上传时间" prop="createTime" min-width="180" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-link :href="row.fileUrl" target="_blank" type="primary">预览</el-link>
                </template>
              </el-table-column>
            </el-table>

            <div class="section-title">诉状记录</div>
            <el-table :data="detailPetitionList">
              <el-table-column label="模板" prop="templateName" min-width="180" />
              <el-table-column label="版本" prop="versionNo" min-width="100" />
              <el-table-column label="文件名" prop="fileName" min-width="220" show-overflow-tooltip />
              <el-table-column label="生成时间" prop="createTime" min-width="180" />
              <el-table-column label="状态" min-width="120">
                <template #default="{ row }">
                  {{ row.overwritten ? '人工覆盖' : '系统生成' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-link :href="row.fileUrl" target="_blank" type="primary">查看诉状</el-link>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="流转记录" name="flow">
            <el-table :data="detailLogList">
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
              <el-table-column label="备注" prop="remark" min-width="220" show-overflow-tooltip />
              <el-table-column label="时间" prop="createTime" min-width="180" :formatter="dateFormatter" />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </el-drawer>

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
import * as ServiceWorkbenchApi from '@/api/courtCase/serviceWorkbench'
import * as LegalApi from '@/api/courtCase/legalWorkbench'
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
const detailDrawerVisible = ref(false)
const detailLoading = ref(false)
const detailActiveTab = ref('basic')
const detailCase = ref<CourtCaseApi.CourtCaseVO>()
const detailCaseExt = ref<Record<string, any>>({})
const detailFollowUpList = ref<ServiceWorkbenchApi.FollowUpVO[]>([])
const detailEvidenceList = ref<LegalApi.EvidenceVO[]>([])
const detailPetitionList = ref<LegalApi.PetitionRecordVO[]>([])
const detailLogList = ref<any[]>([])
const detailFiling = reactive<LegalApi.FilingVO>({
  caseId: 0,
  status: 'PENDING',
  evidenceLocked: false
})
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

const getFilingStatusLabel = (status?: string) => {
  const filingStatusMap: Record<string, string> = {
    PENDING: '待立案',
    FILED: '已立案',
    REJECTED: '驳回',
    CLOSED: '结案'
  }
  return filingStatusMap[status || ''] || '-'
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

const getDetailDynamicFieldDisplay = (field: CourtCaseModelApi.CourtCaseModelFieldVO) => {
  const value = detailCaseExt.value[field.fieldCode]
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

const splitAttachmentUrls = (value?: string) => {
  return (value || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

const openCreateDialog = () => {
  resetCreateForm()
  createDialogVisible.value = true
}

const openDetailDrawer = async (row: CourtCaseApi.CourtCaseVO) => {
  detailDrawerVisible.value = true
  detailActiveTab.value = 'basic'
  detailLoading.value = true
  try {
    const [caseData, followUps, evidences, petitions, filing, logs] = await Promise.all([
      CourtCaseApi.getCourtCase(row.id),
      ServiceWorkbenchApi.getFollowUpList(row.id).catch(() => []),
      LegalApi.getEvidenceList(row.id).catch(() => []),
      LegalApi.getPetitionRecordList(row.id).catch(() => []),
      LegalApi.getFiling(row.id).catch(() => ({ caseId: row.id, status: 'PENDING', evidenceLocked: false })),
      CourtCaseApi.getCourtCaseFlowLogList(row.id)
    ])
    detailCase.value = caseData
    detailFollowUpList.value = followUps
    detailEvidenceList.value = evidences
    detailPetitionList.value = petitions
    detailLogList.value = logs
    detailCaseExt.value = caseData.extJson ? JSON.parse(caseData.extJson) : {}
    detailFiling.caseId = filing.caseId || row.id
    detailFiling.courtName = filing.courtName || ''
    detailFiling.filingNo = filing.filingNo || ''
    detailFiling.submitTime = filing.submitTime || ''
    detailFiling.status = filing.status || 'PENDING'
    detailFiling.rejectReason = filing.rejectReason || ''
    detailFiling.evidenceLocked = !!filing.evidenceLocked
  } catch {
    message.error('加载案件详情失败')
  } finally {
    detailLoading.value = false
  }
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

<style scoped>
.detail-stat-card {
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  background: linear-gradient(135deg, #fff 0%, #f7fbff 100%);
}

.detail-stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.detail-stat-value {
  font-size: 22px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.2;
}

.detail-stat-desc {
  margin-top: 6px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.section-title {
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
}

.whitespace-pre-wrap {
  white-space: pre-wrap;
}
</style>
