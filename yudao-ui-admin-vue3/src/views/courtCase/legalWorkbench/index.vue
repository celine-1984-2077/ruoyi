<template>
  <ContentWrap>
    <div class="hero">
      <div>
        <div class="hero__eyebrow">案件中心</div>
        <div class="hero__title">法务工作台</div>
        <div class="hero__desc">
          围绕法诉案件池处理证据材料、诉状生成和法院立案。当前页面只展示法务阶段的案件，客服侧待办不会混进来。
        </div>
      </div>
      <div class="hero__actions">
        <el-button plain @click="router.push('/court-case/case')">
          <Icon icon="ep:tickets" class="mr-5px" />
          案件台账
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <div class="summary-grid mb-16px">
    <div class="summary-card" :class="{ 'is-active': queryParams.currentStage === 'LEGAL' }" @click="toggleStage('LEGAL')">
      <div class="summary-card__label">法务待处理</div>
      <div class="summary-card__value">{{ summary.legalCount }}</div>
      <div class="summary-card__meta">已移交法务，等待整理材料和生成诉状</div>
    </div>
    <div class="summary-card" :class="{ 'is-active': queryParams.currentStage === 'LITIGATION' }" @click="toggleStage('LITIGATION')">
      <div class="summary-card__label">诉讼推进中</div>
      <div class="summary-card__value">{{ summary.litigationCount }}</div>
      <div class="summary-card__meta">已提交立案或正在法院处理中</div>
    </div>
    <div class="summary-card">
      <div class="summary-card__label">待立案</div>
      <div class="summary-card__value">{{ summary.pendingFilingCount }}</div>
      <div class="summary-card__meta">证据和诉状准备完成后可手工填写立案信息</div>
    </div>
    <div class="summary-card is-danger">
      <div class="summary-card__label">驳回待补充</div>
      <div class="summary-card__value">{{ summary.rejectedCount }}</div>
      <div class="summary-card__meta">被法院驳回的案件需要补材料并重新提交</div>
    </div>
  </div>

  <div class="legal-layout">
    <ContentWrap class="case-panel">
      <div class="panel-head">
        <div>
          <div class="panel-title">法务案件池</div>
          <div class="panel-subtitle">选择案件后，在右侧处理证据、诉状和立案信息</div>
        </div>
        <div class="panel-chip-list">
          <button class="panel-chip" :class="{ 'is-active': !queryParams.currentStage }" @click="toggleStage(undefined)">
            全部案件
          </button>
          <button class="panel-chip" :class="{ 'is-active': queryParams.currentStage === 'LEGAL' }" @click="toggleStage('LEGAL')">
            法务
          </button>
          <button class="panel-chip" :class="{ 'is-active': queryParams.currentStage === 'LITIGATION' }" @click="toggleStage('LITIGATION')">
            诉讼中
          </button>
          <button class="panel-chip" :class="{ 'is-active': queryParams.currentStage === 'ARCHIVED' }" @click="toggleStage('ARCHIVED')">
            已归档
          </button>
        </div>
      </div>

      <el-form :inline="true" :model="queryParams" class="-mb-15px">
        <el-form-item label="案件编号">
          <el-input v-model="queryParams.caseNo" clearable class="!w-200px" placeholder="请输入案件编号" />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="queryParams.customerName" clearable class="!w-200px" placeholder="请输入客户姓名" />
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

      <el-table
        v-loading="caseLoading"
        :data="caseList"
        highlight-current-row
        row-key="id"
        class="mt-12px"
        @current-change="handleCaseSelect"
      >
        <el-table-column label="案件编号" prop="caseNo" min-width="130" />
        <el-table-column label="客户姓名" prop="customerName" min-width="110" />
        <el-table-column label="订单号" prop="orderNo" min-width="130" />
        <el-table-column label="当前阶段" min-width="110">
          <template #default="{ row }">
            <el-tag :type="stageTagTypeMap[row.currentStage] || 'info'">{{ getStageLabel(row.currentStage) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="立案状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="filingTagTypeMap[row.filingStatus] || 'info'">{{ filingStatusLabelMap[row.filingStatus] || row.filingStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="证据" min-width="70" align="center">
          <template #default="{ row }">{{ row.evidenceCount }}</template>
        </el-table-column>
        <el-table-column label="诉状" min-width="70" align="center">
          <template #default="{ row }">{{ row.petitionCount }}</template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getCasePage"
      />
    </ContentWrap>

    <ContentWrap class="detail-panel">
      <template v-if="selectedCase">
        <div class="detail-head">
          <div>
            <div class="detail-title">{{ selectedCase.caseNo }}</div>
            <div class="detail-subtitle">
              {{ selectedCase.customerName }} / {{ selectedCase.orderNo }} / {{ getStageLabel(selectedCase.currentStage) }}
            </div>
          </div>
          <div class="detail-meta">
            <el-tag type="primary">涉案金额 {{ selectedCase.amount || 0 }}</el-tag>
            <el-tag :type="filingTagTypeMap[selectedCase.filingStatus] || 'info'">
              {{ filingStatusLabelMap[selectedCase.filingStatus] || selectedCase.filingStatus }}
            </el-tag>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="mt-12px">
          <el-tab-pane label="证据材料" name="evidence">
            <div class="section-actions">
              <el-button type="primary" @click="openEvidenceDialog">
                <Icon icon="ep:upload-filled" class="mr-5px" />
                上传证据
              </el-button>
              <el-button @click="handleDownloadEvidenceZip">
                <Icon icon="ep:download" class="mr-5px" />
                打包下载
              </el-button>
            </div>

            <el-table v-loading="evidenceLoading" :data="evidenceList" class="mt-12px">
              <el-table-column label="材料分类" min-width="130">
                <template #default="{ row }">
                  {{ evidenceCategoryLabelMap[row.category] || row.category }}
                </template>
              </el-table-column>
              <el-table-column label="文件名" min-width="220" show-overflow-tooltip>
                <template #default="{ row }">
                  <el-link :href="row.fileUrl" target="_blank" type="primary">{{ row.fileName }}</el-link>
                </template>
              </el-table-column>
              <el-table-column label="格式" prop="fileType" min-width="140" />
              <el-table-column label="大小" min-width="100">
                <template #default="{ row }">
                  {{ formatFileSize(row.fileSize) }}
                </template>
              </el-table-column>
              <el-table-column label="上传时间" prop="createTime" min-width="170" :formatter="dateFormatter" />
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="previewFile(row.fileUrl)">预览</el-button>
                  <el-button link type="primary" @click="downloadByUrl({ url: row.fileUrl, fileName: row.fileName })">下载</el-button>
                  <el-button link type="danger" :disabled="!row.canDelete" @click="handleDeleteEvidence(row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="诉状管理" name="petition">
            <div class="petition-bar">
              <el-select v-model="petitionGenerateForm.templateId" class="!w-260px" placeholder="请选择诉状模板">
                <el-option
                  v-for="item in enabledTemplates"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
              <el-button type="primary" :disabled="!petitionGenerateForm.templateId" :loading="petitionGenerating" @click="handleGeneratePetition">
                生成诉状
              </el-button>
              <el-button @click="openTemplateDialog()">新增模板</el-button>
            </div>

            <el-card class="template-card mt-12px" shadow="never">
              <template #header>
                <div class="template-card__header">
                  <span>模板管理</span>
                  <span class="template-tip">支持上传模板附件留存，并用正文占位符生成诉状</span>
                </div>
              </template>
              <el-table :data="templateList" class="template-table">
                <el-table-column label="模板名称" prop="name" min-width="160" />
                <el-table-column label="模板附件" min-width="180">
                  <template #default="{ row }">
                    <el-link v-if="row.templateFileUrl" :href="row.templateFileUrl" target="_blank" type="primary">
                      {{ row.templateFileName || '查看附件' }}
                    </el-link>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column label="占位符说明" prop="placeholderDesc" min-width="220" show-overflow-tooltip />
                <el-table-column label="状态" min-width="90">
                  <template #default="{ row }">
                    <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="140" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="openTemplateDialog(row)">编辑</el-button>
                    <el-button link type="danger" @click="handleDeleteTemplate(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>

            <el-table v-loading="petitionLoading" :data="petitionList" class="mt-12px">
              <el-table-column label="版本" min-width="80">
                <template #default="{ row }">V{{ row.versionNo }}</template>
              </el-table-column>
              <el-table-column label="模板" prop="templateName" min-width="140" />
              <el-table-column label="文件名" min-width="220" show-overflow-tooltip>
                <template #default="{ row }">
                  <el-link :href="row.fileUrl" target="_blank" type="primary">{{ row.fileName }}</el-link>
                </template>
              </el-table-column>
              <el-table-column label="类型" prop="outputType" min-width="90" />
              <el-table-column label="覆盖状态" min-width="100">
                <template #default="{ row }">
                  <el-tag :type="row.overwritten ? 'warning' : 'success'">{{ row.overwritten ? '已覆盖' : '系统生成' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="生成时间" prop="createTime" min-width="170" :formatter="dateFormatter" />
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="previewFile(row.fileUrl)">预览</el-button>
                  <el-button link type="primary" @click="downloadByUrl({ url: row.fileUrl, fileName: row.fileName })">下载</el-button>
                  <el-button link type="warning" @click="openOverrideDialog(row)">重新上传覆盖</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="法院立案" name="filing">
            <el-alert
              :title="filingForm.evidenceLocked ? '当前案件已经提交立案，证据材料只读。' : '当前案件尚未提交立案，证据材料仍可调整。'"
              :type="filingForm.evidenceLocked ? 'warning' : 'info'"
              :closable="false"
              class="mb-16px"
            />

            <el-form ref="filingFormRef" :model="filingForm" :rules="filingRules" label-width="120px" class="filing-form">
              <el-form-item label="立案法院" prop="courtName">
                <el-input v-model="filingForm.courtName" placeholder="请输入立案法院" />
              </el-form-item>
              <el-form-item label="立案编号" prop="filingNo">
                <el-input v-model="filingForm.filingNo" placeholder="请输入立案编号" />
              </el-form-item>
              <el-form-item label="提交时间" prop="submitTime">
                <el-date-picker
                  v-model="filingForm.submitTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="请选择提交时间"
                  class="!w-full"
                />
              </el-form-item>
              <el-form-item label="立案状态" prop="status">
                <el-select v-model="filingForm.status" class="!w-full">
                  <el-option v-for="item in filingStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item v-if="filingForm.status === 'REJECTED'" label="驳回原因" prop="rejectReason">
                <el-input v-model="filingForm.rejectReason" type="textarea" :rows="4" maxlength="500" show-word-limit />
              </el-form-item>
            </el-form>

            <div class="section-actions mt-12px">
              <el-button type="primary" :loading="filingSubmitting" @click="submitFiling">保存立案信息</el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </template>

      <el-empty v-else description="先从左侧选择一条法务案件" />
    </ContentWrap>
  </div>

  <Dialog v-model="evidenceDialogVisible" title="上传证据材料" width="620px">
    <el-form ref="evidenceFormRef" :model="evidenceForm" :rules="evidenceRules" label-width="100px">
      <el-form-item label="材料分类" prop="category">
        <el-select v-model="evidenceForm.category" class="!w-full" placeholder="请选择材料分类">
          <el-option v-for="item in evidenceCategoryOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="上传文件" prop="fileUrls">
        <UploadFile
          v-model="evidenceForm.fileUrls"
          :file-type="['jpg', 'jpeg', 'png', 'pdf', 'doc', 'docx', 'xls', 'xlsx']"
          :file-size="100"
          :limit="10"
          directory="court-case/evidence"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="evidenceDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="evidenceSubmitting" @click="submitEvidence">确定</el-button>
    </template>
  </Dialog>

  <Dialog v-model="templateDialogVisible" :title="templateForm.id ? '编辑诉状模板' : '新增诉状模板'" width="760px">
    <el-form ref="templateFormRef" :model="templateForm" :rules="templateRules" label-width="110px">
      <el-form-item label="模板名称" prop="name">
        <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
      </el-form-item>
      <el-form-item label="模板附件">
        <UploadFile
          v-model="templateForm.templateFileUrl"
          :file-type="['doc', 'docx']"
          :file-size="20"
          :limit="1"
          directory="court-case/petition-template"
        />
      </el-form-item>
      <el-form-item label="占位符说明">
        <el-input
          v-model="templateForm.placeholderDesc"
          type="textarea"
          :rows="2"
          placeholder="例如：{{customerName}}、{{orderNo}}、{{identityNo}}、{{overdueDays}}、{{amount}}"
        />
      </el-form-item>
      <el-form-item label="模板正文" prop="templateContent">
        <el-input
          v-model="templateForm.templateContent"
          type="textarea"
          :rows="10"
          placeholder="请输入诉状模板正文，支持 {{customerName}} / ${customerName} 占位符"
        />
      </el-form-item>
      <el-form-item label="是否启用">
        <el-switch v-model="templateForm.enabled" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="templateDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="templateSubmitting" @click="submitTemplate">保存</el-button>
    </template>
  </Dialog>

  <Dialog v-model="overrideDialogVisible" title="重新上传覆盖诉状" width="560px">
    <el-form ref="overrideFormRef" :model="overrideForm" :rules="overrideRules" label-width="100px">
      <el-form-item label="上传文件" prop="fileUrl">
        <UploadFile
          v-model="overrideForm.fileUrl"
          :file-type="['doc', 'docx', 'pdf']"
          :file-size="50"
          :limit="1"
          directory="court-case/petition-override"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="overrideDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="overrideSubmitting" @click="submitOverride">确定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus'
import { dateFormatter } from '@/utils/formatTime'
import { downloadByData, downloadByUrl } from '@/utils/filt'
import { UploadFile } from '@/components/UploadFile'
import { getStageLabel, stageTagTypeMap } from '../components/config'
import * as LegalApi from '@/api/courtCase/legalWorkbench'

defineOptions({ name: 'CourtCaseLegalWorkbench' })

const message = useMessage()
const router = useRouter()

const summary = ref<LegalApi.LegalSummaryVO>({
  legalCount: 0,
  litigationCount: 0,
  pendingFilingCount: 0,
  rejectedCount: 0,
  archivedCount: 0
})
const caseLoading = ref(false)
const evidenceLoading = ref(false)
const petitionLoading = ref(false)
const petitionGenerating = ref(false)
const filingSubmitting = ref(false)
const evidenceSubmitting = ref(false)
const templateSubmitting = ref(false)
const overrideSubmitting = ref(false)
const activeTab = ref('evidence')

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  caseNo: '',
  customerName: '',
  currentStage: undefined as string | undefined
})
const caseList = ref<LegalApi.LegalCaseVO[]>([])
const total = ref(0)
const selectedCase = ref<LegalApi.LegalCaseVO>()

const evidenceList = ref<LegalApi.EvidenceVO[]>([])
const petitionList = ref<LegalApi.PetitionRecordVO[]>([])
const templateList = ref<LegalApi.PetitionTemplateVO[]>([])
const petitionGenerateForm = reactive({
  templateId: undefined as number | undefined
})

const filingFormRef = ref<FormInstance>()
const filingForm = reactive<LegalApi.FilingVO>({
  caseId: 0,
  courtName: '',
  filingNo: '',
  submitTime: '',
  status: 'PENDING',
  rejectReason: '',
  evidenceLocked: false
})

const evidenceDialogVisible = ref(false)
const evidenceFormRef = ref<FormInstance>()
const evidenceForm = reactive({
  category: '',
  fileUrls: ''
})
const evidenceRules: FormRules = {
  category: [{ required: true, message: '请选择材料分类', trigger: 'change' }],
  fileUrls: [{ required: true, message: '请先上传证据材料', trigger: 'change' }]
}

const templateDialogVisible = ref(false)
const templateFormRef = ref<FormInstance>()
const templateForm = reactive({
  id: undefined as number | undefined,
  name: '',
  templateFileUrl: '',
  placeholderDesc: '',
  templateContent: '',
  enabled: true
})
const templateRules: FormRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  templateContent: [{ required: true, message: '请输入模板正文', trigger: 'blur' }]
}

const overrideDialogVisible = ref(false)
const overrideFormRef = ref<FormInstance>()
const overrideForm = reactive({
  recordId: undefined as number | undefined,
  fileUrl: ''
})
const overrideRules: FormRules = {
  fileUrl: [{ required: true, message: '请上传新的诉状文件', trigger: 'change' }]
}

const filingRules: FormRules = {
  status: [{ required: true, message: '请选择立案状态', trigger: 'change' }],
  rejectReason: [
    {
      validator: (_, value, callback) => {
        if (filingForm.status === 'REJECTED' && !value) {
          callback(new Error('驳回原因必填'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const evidenceCategoryOptions = [
  { label: '身份证正反面', value: 'ID_CARD' },
  { label: '租赁合同', value: 'LEASE_CONTRACT' },
  { label: '物流单据', value: 'LOGISTICS_DOC' },
  { label: '逾期证明', value: 'OVERDUE_PROOF' },
  { label: '沟通记录', value: 'COMMUNICATION_RECORD' },
  { label: '其他', value: 'OTHER' }
]
const evidenceCategoryLabelMap = evidenceCategoryOptions.reduce(
  (acc, item) => Object.assign(acc, { [item.value]: item.label }),
  {} as Record<string, string>
)

const filingStatusOptions = [
  { label: '待立案', value: 'PENDING' },
  { label: '已立案', value: 'FILED' },
  { label: '驳回', value: 'REJECTED' },
  { label: '结案', value: 'CLOSED' }
]
const filingStatusLabelMap = filingStatusOptions.reduce(
  (acc, item) => Object.assign(acc, { [item.value]: item.label }),
  {} as Record<string, string>
)
const filingTagTypeMap: Record<string, string> = {
  PENDING: 'warning',
  FILED: 'success',
  REJECTED: 'danger',
  CLOSED: 'info'
}

const enabledTemplates = computed(() => templateList.value.filter((item) => item.enabled))

const getSummary = async () => {
  summary.value = await LegalApi.getLegalSummary()
}

const getCasePage = async () => {
  caseLoading.value = true
  try {
    const data = await LegalApi.getLegalCasePage(queryParams)
    caseList.value = data.list || []
    total.value = data.total || 0
    const currentId = selectedCase.value?.id
    const nextSelected = caseList.value.find((item) => item.id === currentId) || caseList.value[0]
    selectedCase.value = nextSelected
    if (nextSelected) {
      await loadCaseDetail(nextSelected.id)
    } else {
      evidenceList.value = []
      petitionList.value = []
      resetFiling()
    }
  } finally {
    caseLoading.value = false
  }
}

const loadCaseDetail = async (caseId: number) => {
  await Promise.all([getEvidence(caseId), getPetitions(caseId), getFiling(caseId), getTemplates()])
}

const getEvidence = async (caseId: number) => {
  evidenceLoading.value = true
  try {
    evidenceList.value = await LegalApi.getEvidenceList(caseId)
  } finally {
    evidenceLoading.value = false
  }
}

const getPetitions = async (caseId: number) => {
  petitionLoading.value = true
  try {
    petitionList.value = await LegalApi.getPetitionRecordList(caseId)
  } finally {
    petitionLoading.value = false
  }
}

const getTemplates = async () => {
  templateList.value = await LegalApi.getPetitionTemplateList()
  if (!petitionGenerateForm.templateId && enabledTemplates.value.length) {
    petitionGenerateForm.templateId = enabledTemplates.value[0].id
  }
}

const getFiling = async (caseId: number) => {
  const data = await LegalApi.getFiling(caseId)
  filingForm.caseId = caseId
  filingForm.courtName = data.courtName || ''
  filingForm.filingNo = data.filingNo || ''
  filingForm.submitTime = data.submitTime || ''
  filingForm.status = data.status || 'PENDING'
  filingForm.rejectReason = data.rejectReason || ''
  filingForm.evidenceLocked = !!data.evidenceLocked
}

const resetFiling = () => {
  filingForm.caseId = 0
  filingForm.courtName = ''
  filingForm.filingNo = ''
  filingForm.submitTime = ''
  filingForm.status = 'PENDING'
  filingForm.rejectReason = ''
  filingForm.evidenceLocked = false
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getCasePage()
}

const resetQuery = async () => {
  queryParams.pageNo = 1
  queryParams.caseNo = ''
  queryParams.customerName = ''
  queryParams.currentStage = undefined
  await getCasePage()
}

const toggleStage = async (stage?: string) => {
  queryParams.currentStage = stage
  queryParams.pageNo = 1
  await getCasePage()
}

const handleCaseSelect = async (row?: LegalApi.LegalCaseVO) => {
  if (!row || row.id === selectedCase.value?.id) {
    return
  }
  selectedCase.value = row
  await loadCaseDetail(row.id)
}

const openEvidenceDialog = () => {
  evidenceForm.category = ''
  evidenceForm.fileUrls = ''
  evidenceDialogVisible.value = true
}

const submitEvidence = async () => {
  if (!selectedCase.value) return
  const valid = await evidenceFormRef.value?.validate()
  if (!valid) return
  evidenceSubmitting.value = true
  try {
    await LegalApi.createEvidence({
      caseId: selectedCase.value.id,
      category: evidenceForm.category,
      fileUrls: evidenceForm.fileUrls
    })
    message.success('证据材料已保存')
    evidenceDialogVisible.value = false
    await Promise.all([getEvidence(selectedCase.value.id), getCasePage()])
  } finally {
    evidenceSubmitting.value = false
  }
}

const handleDeleteEvidence = async (row: LegalApi.EvidenceVO) => {
  await message.delConfirm('确认删除这份证据材料吗？')
  await LegalApi.deleteEvidence(row.id)
  message.success('已删除')
  if (selectedCase.value) {
    await Promise.all([getEvidence(selectedCase.value.id), getCasePage()])
  }
}

const handleDownloadEvidenceZip = async () => {
  if (!selectedCase.value) return
  const res: any = await LegalApi.downloadEvidenceZip(selectedCase.value.id)
  const disposition = res.headers?.['content-disposition'] || ''
  const matched = disposition.match(/filename\*=UTF-8''(.+)/)
  const fileName = matched ? decodeURIComponent(matched[1]) : `${selectedCase.value.orderNo}-证据材料.zip`
  downloadByData(res.data, fileName)
}

const openTemplateDialog = (row?: LegalApi.PetitionTemplateVO) => {
  templateForm.id = row?.id
  templateForm.name = row?.name || ''
  templateForm.templateFileUrl = row?.templateFileUrl || ''
  templateForm.placeholderDesc = row?.placeholderDesc || ''
  templateForm.templateContent = row?.templateContent || ''
  templateForm.enabled = row?.enabled ?? true
  templateDialogVisible.value = true
}

const submitTemplate = async () => {
  const valid = await templateFormRef.value?.validate()
  if (!valid) return
  templateSubmitting.value = true
  try {
    if (templateForm.id) {
      await LegalApi.updatePetitionTemplate(templateForm)
    } else {
      await LegalApi.createPetitionTemplate(templateForm)
    }
    message.success('模板已保存')
    templateDialogVisible.value = false
    await getTemplates()
  } finally {
    templateSubmitting.value = false
  }
}

const handleDeleteTemplate = async (row: LegalApi.PetitionTemplateVO) => {
  await message.delConfirm(`确认删除模板「${row.name}」吗？`)
  await LegalApi.deletePetitionTemplate(row.id)
  message.success('模板已删除')
  await getTemplates()
}

const handleGeneratePetition = async () => {
  if (!selectedCase.value || !petitionGenerateForm.templateId) return
  petitionGenerating.value = true
  try {
    await LegalApi.generatePetition({
      caseId: selectedCase.value.id,
      templateId: petitionGenerateForm.templateId
    })
    message.success('诉状已生成')
    await Promise.all([getPetitions(selectedCase.value.id), getCasePage()])
  } finally {
    petitionGenerating.value = false
  }
}

const openOverrideDialog = (row: LegalApi.PetitionRecordVO) => {
  overrideForm.recordId = row.id
  overrideForm.fileUrl = ''
  overrideDialogVisible.value = true
}

const submitOverride = async () => {
  const valid = await overrideFormRef.value?.validate()
  if (!valid || !selectedCase.value || !overrideForm.recordId) return
  overrideSubmitting.value = true
  try {
    await LegalApi.overridePetition({
      recordId: overrideForm.recordId,
      fileUrl: overrideForm.fileUrl
    })
    message.success('诉状已覆盖')
    overrideDialogVisible.value = false
    await getPetitions(selectedCase.value.id)
  } finally {
    overrideSubmitting.value = false
  }
}

const submitFiling = async () => {
  if (!selectedCase.value) return
  const valid = await filingFormRef.value?.validate()
  if (!valid) return
  filingSubmitting.value = true
  try {
    await LegalApi.saveFiling({
      caseId: selectedCase.value.id,
      courtName: filingForm.courtName,
      filingNo: filingForm.filingNo,
      submitTime: filingForm.submitTime || null,
      status: filingForm.status,
      rejectReason: filingForm.rejectReason
    })
    message.success('立案信息已保存')
    await Promise.all([getFiling(selectedCase.value.id), getCasePage()])
  } finally {
    filingSubmitting.value = false
  }
}

const previewFile = (url: string) => {
  window.open(url, '_blank')
}

const formatFileSize = (size?: number) => {
  if (!size) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

onMounted(async () => {
  await Promise.all([getSummary(), getTemplates()])
  await getCasePage()
})
</script>

<style lang="scss" scoped>
.hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
}

.hero__eyebrow {
  color: var(--el-color-primary);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero__title {
  margin-top: 8px;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.2;
}

.hero__desc {
  margin-top: 8px;
  max-width: 760px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  line-height: 1.8;
}

.hero__actions {
  display: flex;
  gap: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.summary-card {
  cursor: pointer;
  border: 1px solid var(--el-border-color-light);
  border-radius: 18px;
  background: linear-gradient(135deg, #f8fbff 0%, #eef5ff 100%);
  padding: 22px 24px;
  transition: all 0.2s ease;
}

.summary-card:hover,
.summary-card.is-active {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 10px 24px rgb(64 158 255 / 10%);
  transform: translateY(-1px);
}

.summary-card.is-danger {
  background: linear-gradient(135deg, #fff5f5 0%, #fff1ef 100%);
}

.summary-card__label {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.summary-card__value {
  margin-top: 16px;
  font-size: 42px;
  font-weight: 700;
  line-height: 1;
}

.summary-card__meta {
  margin-top: 14px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.7;
}

.legal-layout {
  display: grid;
  grid-template-columns: 520px minmax(0, 1fr);
  gap: 16px;
}

.panel-head,
.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.panel-title,
.detail-title {
  font-size: 20px;
  font-weight: 700;
}

.panel-subtitle,
.detail-subtitle,
.template-tip {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.panel-chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.panel-chip {
  border: 1px solid var(--el-border-color);
  border-radius: 999px;
  background: #fff;
  padding: 8px 14px;
  color: var(--el-text-color-regular);
  cursor: pointer;
  transition: all 0.2s ease;
}

.panel-chip.is-active {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.section-actions,
.petition-bar,
.template-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.petition-bar {
  justify-content: flex-start;
  flex-wrap: wrap;
}

.filing-form {
  max-width: 640px;
}

@media (max-width: 1400px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .legal-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero,
  .panel-head,
  .detail-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
