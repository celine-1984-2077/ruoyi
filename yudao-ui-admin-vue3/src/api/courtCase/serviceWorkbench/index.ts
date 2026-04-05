import request from '@/config/axios'

export interface ServiceWorkbenchSummaryVO {
  remindCount: number
  highPriorityCount: number
  overdueCount: number
  transferredCount: number
  manualReminderDueCount: number
}

export interface ServiceWorkbenchCaseVO {
  id: number
  caseNo: string
  orderNo: string
  customerName: string
  mobile: string
  amount: number
  repaymentDueDate?: string
  overdueDays: number
  overdueStatus: string
  reminderStatus: string
  priority: string
  customerStatus: string
  currentStage: string
  currentAssigneeId?: number
  nextRemindTime?: string
  nextRemindContent?: string
  lastFollowUpTime?: string
  repaid: boolean
  manualReminderDue: boolean
  canTransferLegal: boolean
  createTime: string
}

export interface FollowUpVO {
  id: number
  caseId: number
  operatorId: number
  operatorName: string
  content: string
  attachmentUrls?: string
  createTime: string
}

export interface ReminderCreateVO {
  caseId: number
  remindTime: string
  content: string
}

export interface FollowUpCreateVO {
  caseId: number
  content: string
  attachmentUrls?: string
}

export interface TransferLegalVO {
  caseId: number
  receiverUserId: number
  reason: string
  extraNote?: string
}

export const getServiceWorkbenchSummary = async () => {
  return await request.get<ServiceWorkbenchSummaryVO>({ url: '/court-case/service-workbench/summary' })
}

export const getReminderPage = async (params: any) => {
  return await request.get({ url: '/court-case/service-workbench/reminder-page', params })
}

export const getOverduePage = async (params: any) => {
  return await request.get({ url: '/court-case/service-workbench/overdue-page', params })
}

export const getFollowUpList = async (caseId: number) => {
  return await request.get<FollowUpVO[]>({ url: '/court-case/service-workbench/follow-up/list', params: { caseId } })
}

export const createFollowUp = async (data: FollowUpCreateVO) => {
  return await request.post({ url: '/court-case/service-workbench/follow-up/create', data })
}

export const createReminder = async (data: ReminderCreateVO) => {
  return await request.post({ url: '/court-case/service-workbench/reminder/create', data })
}

export const markRepaid = async (caseId: number) => {
  return await request.post({ url: '/court-case/service-workbench/mark-repaid', params: { caseId } })
}

export const transferLegal = async (data: TransferLegalVO) => {
  return await request.post({ url: '/court-case/service-workbench/transfer-legal', data })
}
