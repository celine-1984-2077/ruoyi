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
    <el-table
      v-loading="loading"
      :data="list"
      border
      :row-class-name="getRowClassName"
      @header-dragend="handleHeaderDragend"
    >
      <el-table-column
        label="案件编号"
        prop="caseNo"
        column-key="caseNo"
        min-width="160"
        :width="getColumnWidth('caseNo', 160)"
      >
        <template #default="{ row }">
          <el-input v-if="isEditingRow(row)" v-model="editingForm.caseNo" />
          <div v-else class="editable-cell" @click="startInlineEdit(row)">{{ row.caseNo }}</div>
        </template>
      </el-table-column>
      <el-table-column
        label="订单号"
        prop="orderNo"
        column-key="orderNo"
        min-width="180"
        :width="getColumnWidth('orderNo', 180)"
      >
        <template #default="{ row }">
          <el-input v-if="isEditingRow(row)" v-model="editingForm.orderNo" />
          <div v-else class="editable-cell" @click="startInlineEdit(row)">{{ row.orderNo }}</div>
        </template>
      </el-table-column>
      <el-table-column
        label="客户姓名"
        prop="customerName"
        column-key="customerName"
        min-width="120"
        :width="getColumnWidth('customerName', 120)"
      >
        <template #default="{ row }">
          <el-input v-if="isEditingRow(row)" v-model="editingForm.customerName" />
          <div v-else class="editable-cell" @click="startInlineEdit(row)">{{ row.customerName }}</div>
        </template>
      </el-table-column>
      <el-table-column
        label="联系电话"
        prop="mobile"
        column-key="mobile"
        min-width="140"
        :width="getColumnWidth('mobile', 140)"
      >
        <template #default="{ row }">
          <el-input v-if="isEditingRow(row)" v-model="editingForm.mobile" />
          <div v-else class="editable-cell" @click="startInlineEdit(row)">{{ row.mobile }}</div>
        </template>
      </el-table-column>
      <el-table-column
        label="涉案金额"
        prop="amount"
        column-key="amount"
        min-width="120"
        :width="getColumnWidth('amount', 120)"
      >
        <template #default="{ row }">
          <el-input-number v-if="isEditingRow(row)" v-model="editingForm.amount" :min="0" :precision="2" class="!w-full" />
          <div v-else class="editable-cell" @click="startInlineEdit(row)">{{ row.amount }}</div>
        </template>
      </el-table-column>
      <el-table-column
        label="应还款日期"
        prop="repaymentDueDate"
        column-key="repaymentDueDate"
        min-width="140"
        :width="getColumnWidth('repaymentDueDate', 140)"
      >
        <template #default="{ row }">
          <el-date-picker
            v-if="isEditingRow(row)"
            v-model="editingForm.repaymentDueDate"
            type="date"
            value-format="YYYY-MM-DD"
            class="!w-full"
          />
          <div v-else class="editable-cell" @click="startInlineEdit(row)">{{ formatLocalDate(row.repaymentDueDate) || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="客户状态" column-key="customerStatus" min-width="140" :width="getColumnWidth('customerStatus', 140)">
        <template #default="{ row }">
          <el-select v-if="isEditingRow(row)" v-model="editingForm.customerStatus" class="!w-full">
            <el-option
              v-for="item in customerStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <div v-else class="editable-cell" @click="startInlineEdit(row)">
            {{ customerStatusLabelMap[row.customerStatus] || '-' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="下次提醒" column-key="nextRemindTime" min-width="180" :width="getColumnWidth('nextRemindTime', 180)">
        <template #default="{ row }">
          {{ row.nextRemindTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="最近跟进" column-key="lastFollowUpTime" min-width="180" :width="getColumnWidth('lastFollowUpTime', 180)">
        <template #default="{ row }">
          {{ row.lastFollowUpTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column
        v-for="field in modelFields"
        :key="field.fieldCode"
        :label="field.fieldLabel"
        :column-key="`dynamic:${field.fieldCode}`"
        min-width="150"
        :width="getColumnWidth(`dynamic:${field.fieldCode}`, 150)"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          <template v-if="isEditingRow(row)">
            <el-input v-if="field.fieldType === 'TEXT'" v-model="editingExtForm[field.fieldCode]" />
            <el-date-picker
              v-else-if="field.fieldType === 'DATE'"
              v-model="editingExtForm[field.fieldCode]"
              type="date"
              value-format="YYYY-MM-DD"
              class="!w-full"
            />
            <el-input-number
              v-else-if="field.fieldType === 'NUMBER'"
              v-model="editingExtForm[field.fieldCode]"
              :precision="2"
              class="!w-full"
            />
            <el-select v-else-if="field.fieldType === 'SINGLE_SELECT'" v-model="editingExtForm[field.fieldCode]" class="!w-full">
              <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
            <el-select
              v-else
              v-model="editingExtForm[field.fieldCode]"
              multiple
              collapse-tags
              collapse-tags-tooltip
              class="!w-full"
            >
              <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </template>
          <template v-else-if="field.fieldType === 'MULTI_SELECT'">
            <div class="editable-cell" @click="startInlineEdit(row)">
              {{ getDynamicFieldDisplay(row, field).join('、') || '-' }}
            </div>
          </template>
          <template v-else>
            <div class="editable-cell" @click="startInlineEdit(row)">
              {{ getDynamicFieldDisplay(row, field) || '-' }}
            </div>
          </template>
        </template>
      </el-table-column>
      <el-table-column label="当前阶段" column-key="currentStage" min-width="120" :width="getColumnWidth('currentStage', 120)">
        <template #default="{ row }">
          <el-tag :type="stageTagTypeMap[row.currentStage] || 'info'">
            {{ getStageLabel(row.currentStage) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" column-key="status" min-width="100" :width="getColumnWidth('status', 100)">
        <template #default="{ row }">
          <el-tag :type="statusTagTypeMap[row.status] || 'info'">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="建档时间"
        prop="createTime"
        column-key="createTime"
        min-width="180"
        :width="getColumnWidth('createTime', 180)"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" column-key="actions" :width="getColumnWidth('actions', 220)" min-width="220" fixed="right">
        <template #default="{ row }">
          <template v-if="isEditingRow(row)">
            <el-button v-hasPermi="['court-case:case:update']" link type="primary" :loading="editingSaving" @click="saveInlineEdit">
              保存
            </el-button>
            <el-button link @click="cancelInlineEdit">取消</el-button>
          </template>
          <template v-else>
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
            <el-button v-hasPermi="['court-case:case:delete']" link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
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
      <div v-loading="detailLoading" class="detail-sheet-compact">
        <div class="case-sheet">
          <div class="case-basic-panel">
            <div class="case-basic-header">基本信息</div>
            <div
              v-for="(group, groupIndex) in detailBasicInfoRows"
              :key="`detail-group-${groupIndex}`"
              class="case-basic-grid"
            >
              <template v-for="item in group" :key="item.label">
                <div class="case-basic-label">{{ item.label }}</div>
                <div class="case-basic-value" :class="{ 'span-3': item.span === 3 }">{{ item.value }}</div>
              </template>
            </div>
          </div>

          <div class="case-note-board">
            <div class="case-note-header">
              <div>所属客服备注</div>
              <div>法务备注</div>
            </div>
            <div class="case-note-grid">
              <div class="case-note-label">下次提醒</div>
              <div class="case-note-value">{{ formatDateTime(detailCase.nextRemindTime) }}</div>
              <div class="case-note-label">立案状态</div>
              <div class="case-note-value">{{ getFilingStatusLabel(detailFiling.status) }}</div>

              <div class="case-note-label">提醒内容</div>
              <div class="case-note-value">{{ formatDisplayValue(detailCase.nextRemindContent) }}</div>
              <div class="case-note-label">立案法院</div>
              <div class="case-note-value">{{ formatDisplayValue(detailFiling.courtName) }}</div>

              <div class="case-note-label">最新客服备注</div>
              <div class="case-note-value tall">{{ latestServiceNote }}</div>
              <div class="case-note-label">最新法务备注</div>
              <div class="case-note-value tall">{{ latestLegalNote }}</div>
            </div>

            <div class="case-note-record-title">留存记录</div>
            <div class="case-note-record-grid case-note-record-header">
              <div>客服记录</div>
              <div>法务记录</div>
            </div>
            <div
              v-for="(row, index) in noteHistoryRows"
              :key="`note-history-${index}`"
              class="case-note-record-grid"
            >
              <div>{{ row.service }}</div>
              <div>{{ row.legal }}</div>
            </div>
          </div>

          <div class="case-contact-board">
            <div class="case-contact-header">联系人信息</div>
            <div class="case-contact-grid">
              <div v-for="contact in detailContacts" :key="contact.label" class="case-contact-card">
                <div class="case-contact-name">{{ contact.label }}</div>
                <div class="case-contact-value">{{ contact.value }}</div>
              </div>
            </div>
          </div>

          <div class="case-basic-panel">
            <div class="case-basic-header">动态字段</div>
            <template v-if="detailDynamicFieldRows.length">
              <div
                v-for="(row, rowIndex) in detailDynamicFieldRows"
                :key="`dynamic-row-${rowIndex}`"
                class="case-basic-grid"
              >
                <template v-for="item in row" :key="item.label">
                  <div class="case-basic-label">{{ item.label }}</div>
                  <div class="case-basic-value">{{ item.value }}</div>
                </template>
              </div>
            </template>
            <div v-else class="case-empty-block">当前没有动态字段</div>
          </div>

          <div class="case-flow-board">
            <div class="case-flow-header">流转记录</div>
            <el-table :data="detailLogList" border>
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
          </div>
        </div>
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

const CASE_EDIT_ROW_CLASS = 'case-editing-row'

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
const CASE_TABLE_WIDTH_STORAGE_KEY = 'court-case:list-column-widths'
const columnWidthMap = ref<Record<string, number>>({})
const editingCaseId = ref<number>()
const editingSaving = ref(false)
const editingForm = reactive({
  id: undefined as number | undefined,
  caseNo: '',
  orderNo: '',
  customerName: '',
  mobile: '',
  amount: 0,
  repaymentDueDate: '',
  customerStatus: 'PENDING_REPAY'
})
const editingExtForm = reactive<Record<string, any>>({})

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

const customerStatusOptions = Object.entries(customerStatusLabelMap).map(([value, label]) => ({
  value,
  label
}))

const legalStageSet = new Set(['LEGAL', 'LITIGATION', 'ARCHIVED'])

const getFilingStatusLabel = (status?: string) => {
  const filingStatusMap: Record<string, string> = {
    PENDING: '待立案',
    FILED: '已立案',
    REJECTED: '驳回',
    CLOSED: '结案'
  }
  return filingStatusMap[status || ''] || '-'
}

const formatLocalDate = (value: unknown) => {
  if (!value) {
    return ''
  }
  if (typeof value === 'string') {
    return value
  }
  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day] = value
    const normalizedMonth = String(month).padStart(2, '0')
    const normalizedDay = String(day).padStart(2, '0')
    return `${year}-${normalizedMonth}-${normalizedDay}`
  }
  return String(value)
}

const formatDateTime = (value: unknown) => {
  if (!value) {
    return '-'
  }
  if (typeof value === 'string') {
    return value
  }
  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`
  }
  return String(value)
}

const formatDisplayValue = (value: unknown) => {
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  if (Array.isArray(value)) {
    return value.join('、') || '-'
  }
  return String(value)
}

const isLegalStageCase = (stage?: string) => legalStageSet.has(stage || '')

const detailBasicInfoRows = computed(() => {
  if (!detailCase.value) {
    return []
  }
  const c = detailCase.value
  return [
    [
      { label: '案件编号', value: formatDisplayValue(c.caseNo) },
      { label: '合同编号', value: formatDisplayValue(c.contractNo) },
      { label: '公司', value: formatDisplayValue(c.companyName) },
      { label: '平台', value: formatDisplayValue(c.platformName) }
    ],
    [
      { label: '订单号', value: formatDisplayValue(c.orderNo) },
      { label: '快递单号', value: formatDisplayValue(c.expressNo) },
      { label: '商品名称', value: formatDisplayValue(c.productName) },
      { label: '套餐信息', value: formatDisplayValue(c.packageInfo) }
    ],
    [
      { label: '客户姓名', value: formatDisplayValue(c.customerName) },
      { label: '联系电话', value: formatDisplayValue(c.mobile) },
      { label: '身份证号', value: formatDisplayValue(c.idCardNo) },
      { label: '性别 / 年龄', value: `${formatDisplayValue(c.gender)} / ${formatDisplayValue(c.age)}` }
    ],
    [
      { label: '供应商', value: formatDisplayValue(c.supplierName) },
      { label: '所属客服', value: formatDisplayValue(c.serviceOwnerName) },
      { label: '租赁方式', value: formatDisplayValue(c.leaseMode) },
      { label: '应还款日期', value: formatDisplayValue(formatLocalDate(c.repaymentDueDate)) }
    ],
    [
      { label: '涉案金额', value: formatDisplayValue(c.amount) },
      { label: '总租金', value: formatDisplayValue(c.totalRentAmount) },
      { label: '已付押金', value: formatDisplayValue(c.paidDepositAmount) },
      { label: '剩余押金', value: formatDisplayValue(c.remainingDepositAmount) }
    ],
    [
      { label: '期付金额', value: formatDisplayValue(c.installmentAmount) },
      { label: '期付次数', value: formatDisplayValue(c.installmentCount) },
      { label: '剩余未还金额', value: formatDisplayValue(c.remainingUnpaidAmount) },
      { label: '逾期类型', value: formatDisplayValue(c.overdueType) }
    ],
    [
      { label: '逾期天数', value: formatDisplayValue(c.overdueDays) },
      { label: '剩余天数', value: formatDisplayValue(c.remainingDays) },
      { label: '客户状态', value: customerStatusLabelMap[c.customerStatus || ''] || '-' },
      { label: '当前阶段', value: getStageLabel(c.currentStage) }
    ],
    [
      { label: '案件状态', value: getStatusLabel(c.status) },
      { label: '建档时间', value: formatDateTime(c.createTime) },
      { label: '下次提醒', value: formatDateTime(c.nextRemindTime) },
      { label: '最近跟进', value: formatDateTime(c.lastFollowUpTime) }
    ],
    [
      { label: '移交法务时间', value: formatDateTime(c.transferTime) },
      { label: '撤回截止', value: formatDateTime(c.transferRecallDeadline) },
      { label: '户籍地址', value: formatDisplayValue(c.domicileAddress), span: 3 },
      { label: '收货地址', value: formatDisplayValue(c.shippingAddress), span: 3 }
    ]
  ]
})

const latestServiceFollowUp = computed(() => detailFollowUpList.value[0])

const latestServiceNote = computed(() => {
  const item = latestServiceFollowUp.value
  if (!item) {
    return '暂无客服跟进记录'
  }
  return `${formatDateTime(item.createTime)} ${item.operatorName || `用户${item.operatorId}`}\n${item.content || '-'}`
})

const latestLegalNote = computed(() => {
  const parts = [
    detailFiling.rejectReason ? `驳回原因：${detailFiling.rejectReason}` : '',
    detailFiling.courtName ? `法院：${detailFiling.courtName}` : '',
    detailFiling.filingNo ? `案号：${detailFiling.filingNo}` : '',
    detailPetitionList.value.length ? `诉状：${detailPetitionList.value.length} 份` : '',
    detailEvidenceList.value.length ? `证据：${detailEvidenceList.value.length} 份` : ''
  ].filter(Boolean)
  return parts.join('；') || '暂无法务备注'
})

const noteHistoryRows = computed(() => {
  const serviceRows = detailFollowUpList.value.slice(0, 5).map((item) => {
    return `${formatDateTime(item.createTime)} ${item.operatorName || `用户${item.operatorId}`}\n${item.content || '-'}`
  })
  const legalRows = [
    ...detailPetitionList.value.slice(0, 3).map((item) => {
      return `${formatDateTime(item.createTime)} 诉状 ${item.templateName || item.fileName || '-'}`
    }),
    ...detailEvidenceList.value.slice(0, 2).map((item) => {
      return `${formatDateTime(item.createTime)} 证据 ${item.fileName || '-'}`
    })
  ]
  const max = Math.max(serviceRows.length, legalRows.length, 1)
  return Array.from({ length: max }, (_, index) => ({
    service: serviceRows[index] || '-',
    legal: legalRows[index] || '-'
  }))
})

const detailContacts = computed(() => {
  const caseData = detailCase.value
  if (!caseData) {
    return []
  }
  return [
    { label: '联系人 1', value: [caseData.customerName, caseData.mobile].filter(Boolean).join(' - ') || '-' },
    { label: '联系人 2', value: '-' },
    { label: '联系人 3', value: '-' },
    { label: '联系人 4', value: '-' }
  ]
})

const detailDynamicFieldRows = computed(() => {
  const items = modelFields.value.map((field) => {
    const rawValue = field.fieldType === 'MULTI_SELECT'
      ? getDetailDynamicFieldDisplay(field).join('、') || '-'
      : getDetailDynamicFieldDisplay(field) || '-'
    return {
      label: field.fieldLabel,
      value: String(rawValue)
    }
  })
  const rows: Array<Array<{ label: string; value: string }>> = []
  for (let index = 0; index < items.length; index += 4) {
    rows.push(items.slice(index, index + 4))
  }
  return rows
})

const isEditingRow = (row: CourtCaseApi.CourtCaseVO) => editingCaseId.value === row.id

const loadColumnWidthMap = () => {
  try {
    const cache = window.localStorage.getItem(CASE_TABLE_WIDTH_STORAGE_KEY)
    columnWidthMap.value = cache ? JSON.parse(cache) : {}
  } catch {
    columnWidthMap.value = {}
  }
}

const persistColumnWidthMap = () => {
  window.localStorage.setItem(CASE_TABLE_WIDTH_STORAGE_KEY, JSON.stringify(columnWidthMap.value))
}

const getColumnWidth = (key: string, fallback: number) => {
  return columnWidthMap.value[key] || fallback
}

const getRowClassName = ({ row }: { row: CourtCaseApi.CourtCaseVO }) => {
  return isEditingRow(row) ? CASE_EDIT_ROW_CLASS : ''
}

const handleHeaderDragend = (newWidth: number, _oldWidth: number, column: { columnKey?: string; property?: string }) => {
  const key = column?.columnKey || column?.property
  if (!key) {
    return
  }
  columnWidthMap.value = {
    ...columnWidthMap.value,
    [key]: Math.round(newWidth)
  }
  persistColumnWidthMap()
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

const resetEditingExtForm = () => {
  Object.keys(editingExtForm).forEach((key) => delete editingExtForm[key])
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

const startInlineEdit = (row: CourtCaseApi.CourtCaseVO) => {
  editingCaseId.value = row.id
  editingForm.id = row.id
  editingForm.caseNo = row.caseNo
  editingForm.orderNo = row.orderNo
  editingForm.customerName = row.customerName
  editingForm.mobile = row.mobile
  editingForm.amount = Number(row.amount || 0)
  editingForm.repaymentDueDate = formatLocalDate(row.repaymentDueDate)
  editingForm.customerStatus = row.customerStatus || 'PENDING_REPAY'
  resetEditingExtForm()
  const ext = caseExtCache[row.id] || {}
  modelFields.value.forEach((field) => {
    const value = ext[field.fieldCode]
    if (field.fieldType === 'MULTI_SELECT') {
      editingExtForm[field.fieldCode] = Array.isArray(value) ? [...value] : value ? [value] : []
      return
    }
    if (field.fieldType === 'NUMBER') {
      editingExtForm[field.fieldCode] = value === undefined || value === null || value === '' ? undefined : Number(value)
      return
    }
    editingExtForm[field.fieldCode] = value ?? ''
  })
}

const cancelInlineEdit = () => {
  editingCaseId.value = undefined
  editingForm.id = undefined
  resetEditingExtForm()
}

const validateEditingForm = () => {
  if (!editingForm.caseNo.trim() || !editingForm.orderNo.trim() || !editingForm.customerName.trim() || !editingForm.mobile.trim()) {
    message.warning('案件编号、订单号、客户姓名、联系电话不能为空')
    return false
  }
  if (!editingForm.repaymentDueDate) {
    message.warning('请选择应还款日期')
    return false
  }
  return true
}

const handleDocumentPointerDown = (event: PointerEvent) => {
  if (!editingCaseId.value) {
    return
  }
  const target = event.target as HTMLElement | null
  if (!target) {
    cancelInlineEdit()
    return
  }
  if (
    target.closest(`.${CASE_EDIT_ROW_CLASS}`) ||
    target.closest('.el-popper') ||
    target.closest('.el-picker-panel') ||
    target.closest('.el-select-dropdown')
  ) {
    return
  }
  void saveInlineEdit()
}

const saveInlineEdit = async () => {
  if (!editingCaseId.value || !editingForm.id) {
    return
  }
  if (!validateEditingForm()) {
    return
  }
  const extPayload = modelFields.value.reduce((acc: Record<string, any>, field) => {
    const value = editingExtForm[field.fieldCode]
    if (Array.isArray(value) ? value.length : value !== undefined && value !== null && value !== '') {
      acc[field.fieldCode] = value
    }
    return acc
  }, {})
  editingSaving.value = true
  try {
    await CourtCaseApi.updateCourtCase({
      id: editingForm.id,
      caseNo: editingForm.caseNo.trim(),
      orderNo: editingForm.orderNo.trim(),
      customerName: editingForm.customerName.trim(),
      mobile: editingForm.mobile.trim(),
      amount: editingForm.amount,
      repaymentDueDate: editingForm.repaymentDueDate,
      customerStatus: editingForm.customerStatus,
      extJson: Object.keys(extPayload).length ? JSON.stringify(extPayload) : undefined
    })
    message.success('案件已更新')
    cancelInlineEdit()
    await getList()
  } finally {
    editingSaving.value = false
  }
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
  detailLoading.value = true
  try {
    const caseData = await CourtCaseApi.getCourtCase(row.id)
    const [followUps, logs, evidences, petitions, filing] = await Promise.all([
      ServiceWorkbenchApi.getFollowUpList(row.id).catch(() => []),
      CourtCaseApi.getCourtCaseFlowLogList(row.id),
      isLegalStageCase(caseData.currentStage) ? LegalApi.getEvidenceList(row.id).catch(() => []) : Promise.resolve([]),
      isLegalStageCase(caseData.currentStage)
        ? LegalApi.getPetitionRecordList(row.id).catch(() => [])
        : Promise.resolve([]),
      isLegalStageCase(caseData.currentStage)
        ? LegalApi.getFiling(row.id).catch(() => ({ caseId: row.id, status: 'PENDING', evidenceLocked: false }))
        : Promise.resolve({ caseId: row.id, status: 'PENDING', evidenceLocked: false })
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
  loadColumnWidthMap()
  getModelFields()
  getList()
  document.addEventListener('pointerdown', handleDocumentPointerDown, true)
})

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', handleDocumentPointerDown, true)
})
</script>

<style scoped>
.case-basic-panel {
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

.case-basic-header {
  padding: 9px 12px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  background: linear-gradient(90deg, #edf6ff 0%, #f7fbff 100%);
  border-bottom: 1px solid var(--el-border-color-light);
}

.case-basic-grid {
  display: grid;
  grid-template-columns: 140px 1fr 140px 1fr 140px 1fr 140px 1fr;
}

.case-basic-grid + .case-basic-grid {
  border-top: 1px solid var(--el-border-color-lighter);
}

.case-basic-label,
.case-basic-value {
  min-height: 36px;
  padding: 7px 10px;
  display: flex;
  align-items: center;
  font-size: 12px;
  line-height: 1.35;
  word-break: break-word;
}

.case-basic-label {
  font-weight: 600;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-lighter);
}

.case-basic-value {
  color: var(--el-text-color-primary);
  border-left: 1px solid var(--el-border-color-lighter);
}

.case-basic-value.span-3 {
  grid-column: span 3;
}

.case-sheet {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.case-note-board,
.case-contact-board,
.case-flow-board {
  border: 1px solid var(--el-border-color-light);
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

.case-note-header,
.case-contact-header,
.case-flow-header {
  display: grid;
  align-items: center;
  min-height: 34px;
  padding: 0 12px;
  font-size: 13px;
  font-weight: 600;
}

.case-note-header {
  grid-template-columns: 1fr 1fr;
  background: linear-gradient(90deg, #ffe5c7 0%, #ffd4ab 100%);
}

.case-contact-header {
  background: linear-gradient(90deg, #ffef99 0%, #ffe46f 100%);
}

.case-flow-header {
  background: linear-gradient(90deg, #eef6ff 0%, #d9ebff 100%);
}

.case-note-grid {
  display: grid;
  grid-template-columns: 140px 1fr 140px 1fr;
}

.case-note-label,
.case-note-value {
  min-height: 36px;
  padding: 7px 10px;
  display: flex;
  align-items: center;
  font-size: 12px;
  line-height: 1.35;
  word-break: break-word;
  white-space: pre-wrap;
}

.case-note-label {
  font-weight: 600;
  color: #7c4b0c;
  background: #fff3d7;
  border-top: 1px solid var(--el-border-color-lighter);
}

.case-note-value {
  border-left: 1px solid var(--el-border-color-lighter);
  border-top: 1px solid var(--el-border-color-lighter);
}

.case-note-value.tall {
  min-height: 64px;
  align-items: flex-start;
}

.case-note-record-title {
  padding: 9px 12px;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.case-note-record-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.case-note-record-grid > div {
  min-height: 40px;
  padding: 7px 10px;
  font-size: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
  white-space: pre-wrap;
  word-break: break-word;
}

.case-note-record-grid > div + div {
  border-left: 1px solid var(--el-border-color-lighter);
}

.case-note-record-header > div {
  font-weight: 600;
  background: #f7f8fa;
}

.case-contact-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.case-contact-card {
  min-height: 60px;
  padding: 9px 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.case-contact-card + .case-contact-card {
  border-left: 1px solid var(--el-border-color-lighter);
}

.case-contact-name {
  margin-bottom: 5px;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
}

.case-contact-value {
  font-size: 12px;
  line-height: 1.35;
  word-break: break-word;
}

.case-empty-block {
  padding: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.detail-sheet-compact :deep(.el-table) {
  font-size: 12px;
}

.detail-sheet-compact :deep(.el-table th),
.detail-sheet-compact :deep(.el-table td) {
  padding: 6px 0;
}

.section-title {
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
}

.whitespace-pre-wrap {
  white-space: pre-wrap;
}

.editable-cell {
  min-height: 32px;
  display: flex;
  align-items: center;
  cursor: text;
  border-radius: 6px;
  padding: 0 4px;
  transition: background-color 0.2s ease;
}

.editable-cell:hover {
  background: var(--el-fill-color-light);
}
</style>
