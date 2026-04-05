<template>
  <ContentWrap>
    <div class="hero">
      <div>
        <div class="hero__eyebrow">案件中心</div>
        <div class="hero__title">客服工作台</div>
        <div class="hero__desc">
          聚焦还款提醒、逾期跟进和法诉移交。客服在这个页面可以直接写跟进备注、设置下次提醒、标记已还款和发起移交法诉。
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
    <div class="summary-card">
      <div class="summary-card__label">7天内待提醒</div>
      <div class="summary-card__value">{{ summary.remindCount }}</div>
      <div class="summary-card__meta">自动筛选距离应还款日期不超过 7 天的客户</div>
    </div>
    <div class="summary-card is-danger">
      <div class="summary-card__label">3天内高优先</div>
      <div class="summary-card__value">{{ summary.highPriorityCount }}</div>
      <div class="summary-card__meta">应还款日 3 天内或已经逾期，优先标红提醒</div>
    </div>
    <div class="summary-card">
      <div class="summary-card__label">逾期客户</div>
      <div class="summary-card__value">{{ summary.overdueCount }}</div>
      <div class="summary-card__meta">逾期超过 15 天且跟进无果后，可移交法诉</div>
    </div>
    <div class="summary-card">
      <div class="summary-card__label">到期临时提醒</div>
      <div class="summary-card__value">{{ summary.manualReminderDueCount }}</div>
      <div class="summary-card__meta">客服手动设置且已到提醒时间的临时提醒</div>
    </div>
  </div>

  <ContentWrap>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="还款提醒" name="reminder">
        <el-form :inline="true" :model="reminderQuery" class="-mb-15px">
          <el-form-item label="案件编号">
            <el-input v-model="reminderQuery.caseNo" placeholder="请输入案件编号" clearable class="!w-220px" />
          </el-form-item>
          <el-form-item label="客户姓名">
            <el-input v-model="reminderQuery.customerName" placeholder="请输入客户姓名" clearable class="!w-220px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleReminderQuery">
              <Icon icon="ep:search" class="mr-5px" />
              搜索
            </el-button>
            <el-button @click="resetReminderQuery">
              <Icon icon="ep:refresh" class="mr-5px" />
              重置
            </el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="reminderLoading" :data="reminderList" class="mt-12px">
          <el-table-column label="优先级" width="110">
            <template #default="{ row }">
              <el-tag :type="row.priority === 'HIGH' ? 'danger' : 'warning'">
                {{ row.priority === 'HIGH' ? '高优先级' : '常规' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="客户姓名" prop="customerName" min-width="120" />
          <el-table-column label="脱敏手机号" min-width="140">
            <template #default="{ row }">
              {{ maskMobile(row.mobile) }}
            </template>
          </el-table-column>
          <el-table-column label="订单号" prop="orderNo" min-width="160" />
          <el-table-column label="应还款日期" prop="repaymentDueDate" min-width="140" />
          <el-table-column label="逾期状态" prop="overdueStatus" min-width="140" />
          <el-table-column label="提醒状态" prop="reminderStatus" min-width="150" />
          <el-table-column label="下次提醒" min-width="180">
            <template #default="{ row }">
              {{ row.nextRemindTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openFollowUpDrawer(row)">跟进记录</el-button>
              <el-button link type="primary" @click="openFollowUpDialog(row)">跟进备注</el-button>
              <el-button link type="warning" @click="openReminderDialog(row)">设置下次提醒</el-button>
              <el-button link type="success" @click="handleMarkRepaid(row)">标记已还款</el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          v-model:limit="reminderQuery.pageSize"
          v-model:page="reminderQuery.pageNo"
          :total="reminderTotal"
          @pagination="getReminderPage"
        />
      </el-tab-pane>

      <el-tab-pane label="逾期客户" name="overdue">
        <el-form :inline="true" :model="overdueQuery" class="-mb-15px">
          <el-form-item label="案件编号">
            <el-input v-model="overdueQuery.caseNo" placeholder="请输入案件编号" clearable class="!w-220px" />
          </el-form-item>
          <el-form-item label="客户姓名">
            <el-input v-model="overdueQuery.customerName" placeholder="请输入客户姓名" clearable class="!w-220px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleOverdueQuery">
              <Icon icon="ep:search" class="mr-5px" />
              搜索
            </el-button>
            <el-button @click="resetOverdueQuery">
              <Icon icon="ep:refresh" class="mr-5px" />
              重置
            </el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="overdueLoading" :data="overdueList" class="mt-12px">
          <el-table-column label="客户姓名" prop="customerName" min-width="120" />
          <el-table-column label="脱敏手机号" min-width="140">
            <template #default="{ row }">
              {{ maskMobile(row.mobile) }}
            </template>
          </el-table-column>
          <el-table-column label="订单号" prop="orderNo" min-width="160" />
          <el-table-column label="应还款日期" prop="repaymentDueDate" min-width="140" />
          <el-table-column label="逾期天数" min-width="120">
            <template #default="{ row }">
              <el-tag :type="row.overdueDays > 15 ? 'danger' : 'warning'">{{ row.overdueDays }} 天</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="客户状态" min-width="140">
            <template #default="{ row }">
              {{ customerStatusLabelMap[row.customerStatus] || row.customerStatus }}
            </template>
          </el-table-column>
          <el-table-column label="最近跟进" min-width="180">
            <template #default="{ row }">
              {{ row.lastFollowUpTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="320" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openFollowUpDrawer(row)">跟进记录</el-button>
              <el-button link type="primary" @click="openFollowUpDialog(row)">跟进备注</el-button>
              <el-button link type="warning" @click="openReminderDialog(row)">设置下次提醒</el-button>
              <el-button
                link
                type="danger"
                :disabled="!row.canTransferLegal"
                @click="openTransferDialog(row)"
              >
                移交法诉
              </el-button>
              <el-button link type="success" @click="handleMarkRepaid(row)">标记已还款</el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          v-model:limit="overdueQuery.pageSize"
          v-model:page="overdueQuery.pageNo"
          :total="overdueTotal"
          @pagination="getOverduePage"
        />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>

  <el-drawer v-model="followUpDrawerVisible" size="520px" :title="`${selectedCase?.customerName || ''} 的跟进记录`">
    <div class="drawer-summary">
      <div>案件编号：{{ selectedCase?.caseNo || '-' }}</div>
      <div>客户状态：{{ selectedCase ? customerStatusLabelMap[selectedCase.customerStatus] : '-' }}</div>
      <div>下次提醒：{{ selectedCase?.nextRemindTime || '-' }}</div>
    </div>
    <el-timeline v-loading="followUpLoading" class="mt-16px">
      <el-timeline-item
        v-for="item in followUpList"
        :key="item.id"
        :timestamp="item.createTime"
        placement="top"
      >
        <div class="font-600 mb-6px">{{ item.operatorName }}</div>
        <div class="follow-up-content">{{ item.content }}</div>
        <div v-if="parseAttachmentUrls(item.attachmentUrls).length" class="mt-8px">
          <el-link
            v-for="url in parseAttachmentUrls(item.attachmentUrls)"
            :key="url"
            :href="url"
            class="mr-10px"
            target="_blank"
            type="primary"
          >
            {{ getFileName(url) }}
          </el-link>
        </div>
      </el-timeline-item>
    </el-timeline>
  </el-drawer>

  <Dialog v-model="followUpDialogVisible" title="新增跟进备注" width="680px">
    <el-form ref="followUpFormRef" :model="followUpForm" :rules="followUpRules" label-width="100px">
      <el-form-item label="客户姓名">
        <el-input :model-value="selectedCase?.customerName || ''" disabled />
      </el-form-item>
      <el-form-item label="跟进备注" prop="content">
        <el-input v-model="followUpForm.content" type="textarea" :rows="5" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="跟进附件" prop="attachmentUrls">
        <UploadFile
          v-model="followUpForm.attachmentUrls"
          :file-type="['jpg', 'jpeg', 'png', 'pdf']"
          :file-size="50"
          :limit="5"
          class="min-w-120px"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="followUpDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="followUpSubmitting" @click="submitFollowUp">提交</el-button>
    </template>
  </Dialog>

  <Dialog v-model="reminderDialogVisible" title="设置下次提醒" width="620px">
    <el-form ref="reminderFormRef" :model="reminderForm" :rules="reminderRules" label-width="100px">
      <el-form-item label="客户姓名">
        <el-input :model-value="selectedCase?.customerName || ''" disabled />
      </el-form-item>
      <el-form-item label="提醒时间" prop="remindTime">
        <el-date-picker
          v-model="reminderForm.remindTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="提醒内容" prop="content">
        <el-input v-model="reminderForm.content" type="textarea" :rows="4" maxlength="500" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="reminderDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="reminderSubmitting" @click="submitReminder">提交</el-button>
    </template>
  </Dialog>

  <Dialog v-model="transferDialogVisible" title="移交法诉" width="620px">
    <el-form ref="transferFormRef" :model="transferForm" :rules="transferRules" label-width="110px">
      <el-form-item label="客户姓名">
        <el-input :model-value="selectedCase?.customerName || ''" disabled />
      </el-form-item>
      <el-form-item label="移交原因" prop="reason">
        <el-select v-model="transferForm.reason" class="!w-full">
          <el-option v-for="item in transferReasonOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="接收法务" prop="receiverUserId">
        <el-select v-model="transferForm.receiverUserId" class="!w-full" filterable>
          <el-option v-for="user in legalUsers" :key="user.id" :label="user.nickname" :value="user.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="补充说明" prop="extraNote">
        <el-input v-model="transferForm.extraNote" type="textarea" :rows="4" maxlength="500" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="transferDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="transferSubmitting" @click="submitTransfer">提交</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus'
import { ElNotification } from 'element-plus'
import { UploadFile } from '@/components/UploadFile'
import * as ServiceWorkbenchApi from '@/api/courtCase/serviceWorkbench'
import { getSimpleUserList, type UserVO } from '@/api/system/user'

defineOptions({ name: 'CourtCaseServiceWorkbench' })

const router = useRouter()
const message = useMessage()

const activeTab = ref('reminder')
const summary = reactive<ServiceWorkbenchApi.ServiceWorkbenchSummaryVO>({
  remindCount: 0,
  highPriorityCount: 0,
  overdueCount: 0,
  transferredCount: 0,
  manualReminderDueCount: 0
})

const customerStatusLabelMap: Record<string, string> = {
  PENDING_REPAY: '待还款',
  REMINDING: '提醒中',
  OVERDUE: '已逾期',
  FOLLOWING: '跟进中',
  TRANSFERRED_TO_LEGAL: '已移交法诉',
  REPAID: '已还款'
}

const reminderLoading = ref(false)
const reminderTotal = ref(0)
const reminderList = ref<ServiceWorkbenchApi.ServiceWorkbenchCaseVO[]>([])
const reminderQuery = reactive({
  pageNo: 1,
  pageSize: 10,
  caseNo: undefined as string | undefined,
  customerName: undefined as string | undefined
})

const overdueLoading = ref(false)
const overdueTotal = ref(0)
const overdueList = ref<ServiceWorkbenchApi.ServiceWorkbenchCaseVO[]>([])
const overdueQuery = reactive({
  pageNo: 1,
  pageSize: 10,
  caseNo: undefined as string | undefined,
  customerName: undefined as string | undefined
})

const selectedCase = ref<ServiceWorkbenchApi.ServiceWorkbenchCaseVO>()
const followUpDrawerVisible = ref(false)
const followUpLoading = ref(false)
const followUpList = ref<ServiceWorkbenchApi.FollowUpVO[]>([])

const followUpDialogVisible = ref(false)
const followUpSubmitting = ref(false)
const followUpFormRef = ref<FormInstance>()
const followUpForm = reactive({
  content: '',
  attachmentUrls: ''
})
const followUpRules = reactive<FormRules>({
  content: [{ required: true, message: '请输入跟进备注', trigger: 'blur' }]
})

const reminderDialogVisible = ref(false)
const reminderSubmitting = ref(false)
const reminderFormRef = ref<FormInstance>()
const reminderForm = reactive({
  remindTime: '',
  content: ''
})
const reminderRules = reactive<FormRules>({
  remindTime: [{ required: true, message: '请选择提醒时间', trigger: 'change' }],
  content: [{ required: true, message: '请输入提醒内容', trigger: 'blur' }]
})

const transferDialogVisible = ref(false)
const transferSubmitting = ref(false)
const transferFormRef = ref<FormInstance>()
const transferForm = reactive({
  receiverUserId: undefined as number | undefined,
  reason: '',
  extraNote: ''
})
const transferRules = reactive<FormRules>({
  receiverUserId: [{ required: true, message: '请选择接收法务人员', trigger: 'change' }],
  reason: [{ required: true, message: '请选择移交原因', trigger: 'change' }]
})
const transferReasonOptions = ['多次联系无果', '客户拒绝还款', '失联', '多次承诺未履约']
const legalUsers = ref<UserVO[]>([])

const maskMobile = (mobile?: string) => {
  if (!mobile) return '-'
  return mobile.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

const parseAttachmentUrls = (urls?: string) => {
  return urls ? urls.split(',').filter(Boolean) : []
}

const getFileName = (url: string) => {
  return url.substring(url.lastIndexOf('/') + 1)
}

const getSummary = async () => {
  Object.assign(summary, await ServiceWorkbenchApi.getServiceWorkbenchSummary())
}

const getReminderPage = async () => {
  reminderLoading.value = true
  try {
    const data = await ServiceWorkbenchApi.getReminderPage(reminderQuery)
    reminderList.value = data.list
    reminderTotal.value = data.total
  } finally {
    reminderLoading.value = false
  }
}

const getOverduePage = async () => {
  overdueLoading.value = true
  try {
    const data = await ServiceWorkbenchApi.getOverduePage(overdueQuery)
    overdueList.value = data.list
    overdueTotal.value = data.total
  } finally {
    overdueLoading.value = false
  }
}

const handleReminderQuery = () => {
  reminderQuery.pageNo = 1
  getReminderPage()
}

const resetReminderQuery = () => {
  reminderQuery.pageNo = 1
  reminderQuery.caseNo = undefined
  reminderQuery.customerName = undefined
  getReminderPage()
}

const handleOverdueQuery = () => {
  overdueQuery.pageNo = 1
  getOverduePage()
}

const resetOverdueQuery = () => {
  overdueQuery.pageNo = 1
  overdueQuery.caseNo = undefined
  overdueQuery.customerName = undefined
  getOverduePage()
}

const loadFollowUps = async (caseId: number) => {
  followUpLoading.value = true
  try {
    followUpList.value = await ServiceWorkbenchApi.getFollowUpList(caseId)
  } finally {
    followUpLoading.value = false
  }
}

const openFollowUpDrawer = async (row: ServiceWorkbenchApi.ServiceWorkbenchCaseVO) => {
  selectedCase.value = row
  followUpDrawerVisible.value = true
  await loadFollowUps(row.id)
}

const openFollowUpDialog = (row: ServiceWorkbenchApi.ServiceWorkbenchCaseVO) => {
  selectedCase.value = row
  followUpForm.content = ''
  followUpForm.attachmentUrls = ''
  followUpDialogVisible.value = true
}

const submitFollowUp = async () => {
  await followUpFormRef.value?.validate()
  if (!selectedCase.value) return
  followUpSubmitting.value = true
  try {
    await ServiceWorkbenchApi.createFollowUp({
      caseId: selectedCase.value.id,
      content: followUpForm.content,
      attachmentUrls: followUpForm.attachmentUrls || undefined
    })
    message.success('跟进备注已保存')
    followUpDialogVisible.value = false
    await Promise.all([loadFollowUps(selectedCase.value.id), getSummary(), getReminderPage(), getOverduePage()])
  } finally {
    followUpSubmitting.value = false
  }
}

const openReminderDialog = (row: ServiceWorkbenchApi.ServiceWorkbenchCaseVO) => {
  selectedCase.value = row
  reminderForm.remindTime = row.nextRemindTime || ''
  reminderForm.content = row.nextRemindContent || ''
  reminderDialogVisible.value = true
}

const submitReminder = async () => {
  await reminderFormRef.value?.validate()
  if (!selectedCase.value) return
  reminderSubmitting.value = true
  try {
    await ServiceWorkbenchApi.createReminder({
      caseId: selectedCase.value.id,
      remindTime: reminderForm.remindTime,
      content: reminderForm.content
    })
    message.success('下次提醒已设置')
    reminderDialogVisible.value = false
    await Promise.all([getSummary(), getReminderPage(), getOverduePage()])
  } finally {
    reminderSubmitting.value = false
  }
}

const handleMarkRepaid = async (row: ServiceWorkbenchApi.ServiceWorkbenchCaseVO) => {
  await message.confirm(`确定将「${row.customerName}」标记为已还款吗？`)
  await ServiceWorkbenchApi.markRepaid(row.id)
  message.success('已标记为已还款')
  await Promise.all([getSummary(), getReminderPage(), getOverduePage()])
}

const openTransferDialog = (row: ServiceWorkbenchApi.ServiceWorkbenchCaseVO) => {
  selectedCase.value = row
  transferForm.receiverUserId = undefined
  transferForm.reason = ''
  transferForm.extraNote = ''
  transferDialogVisible.value = true
}

const submitTransfer = async () => {
  await transferFormRef.value?.validate()
  if (!selectedCase.value) return
  transferSubmitting.value = true
  try {
    await ServiceWorkbenchApi.transferLegal({
      caseId: selectedCase.value.id,
      receiverUserId: transferForm.receiverUserId!,
      reason: transferForm.reason,
      extraNote: transferForm.extraNote || undefined
    })
    message.success('已移交法诉')
    transferDialogVisible.value = false
    await Promise.all([getSummary(), getReminderPage(), getOverduePage()])
  } finally {
    transferSubmitting.value = false
  }
}

const initLegalUsers = async () => {
  legalUsers.value = await getSimpleUserList()
}

const maybePopupDueReminder = () => {
  const dueRows = reminderList.value.filter((item) => item.manualReminderDue)
  if (!dueRows.length) return
  ElNotification({
    title: '临时提醒到期',
    type: 'warning',
    duration: 5000,
    message: `当前有 ${dueRows.length} 条临时提醒已到期，请优先处理。`
  })
}

onMounted(async () => {
  await Promise.all([getSummary(), getReminderPage(), getOverduePage(), initLegalUsers()])
  maybePopupDueReminder()
})
</script>

<style scoped>
.hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
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
}

.hero__desc {
  max-width: 820px;
  margin-top: 10px;
  color: var(--el-text-color-secondary);
  line-height: 1.8;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.summary-card {
  padding: 20px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 18px;
  background:
    radial-gradient(circle at top right, rgb(64 158 255 / 0.14), transparent 36%),
    linear-gradient(180deg, var(--el-bg-color) 0%, var(--el-fill-color-extra-light) 100%);
}

.summary-card.is-danger {
  background:
    radial-gradient(circle at top right, rgb(245 108 108 / 0.18), transparent 36%),
    linear-gradient(180deg, var(--el-bg-color) 0%, rgb(245 108 108 / 0.06) 100%);
}

.summary-card__label {
  color: var(--el-text-color-secondary);
}

.summary-card__value {
  margin-top: 12px;
  font-size: 32px;
  font-weight: 700;
}

.summary-card__meta {
  margin-top: 8px;
  line-height: 1.7;
  color: var(--el-text-color-secondary);
}

.drawer-summary {
  padding: 14px 16px;
  border-radius: 14px;
  background: var(--el-fill-color-light);
  line-height: 1.9;
}

.follow-up-content {
  line-height: 1.8;
  white-space: pre-wrap;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .hero {
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
