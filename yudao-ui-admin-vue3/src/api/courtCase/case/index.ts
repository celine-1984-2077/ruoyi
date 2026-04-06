import request from '@/config/axios'

export interface CourtCaseVO {
  id: number
  caseNo: string
  orderNo: string
  contractNo?: string
  companyName?: string
  platformName?: string
  supplierName?: string
  serviceOwnerName?: string
  expressNo?: string
  productName?: string
  packageInfo?: string
  leaseMode?: string
  customerName: string
  idCardNo?: string
  mobile: string
  gender?: string
  age?: number
  amount: number
  totalRentAmount?: number
  paidDepositAmount?: number
  remainingDepositAmount?: number
  installmentAmount?: number
  installmentCount?: number
  overdueDays?: number
  remainingDays?: number
  remainingUnpaidAmount?: number
  overdueType?: string
  repaymentDueDate?: string
  domicileAddress?: string
  shippingAddress?: string
  currentStage: string
  currentDeptId: number
  currentAssigneeId: number
  customerStatus?: string
  lastFollowUpTime?: string
  nextRemindTime?: string
  nextRemindContent?: string
  repaid?: boolean
  repaidTime?: string
  legalReceiverId?: number
  transferTime?: string
  transferRecallDeadline?: string
  status: number
  extJson?: string
  createTime: string
}

export interface CourtCaseCreateVO {
  caseNo: string
  orderNo: string
  contractNo?: string
  companyName?: string
  platformName?: string
  supplierName?: string
  serviceOwnerName?: string
  expressNo?: string
  productName?: string
  packageInfo?: string
  leaseMode?: string
  customerName: string
  idCardNo?: string
  mobile: string
  gender?: string
  age?: number
  amount: number
  totalRentAmount?: number
  paidDepositAmount?: number
  remainingDepositAmount?: number
  installmentAmount?: number
  installmentCount?: number
  overdueDays?: number
  remainingDays?: number
  remainingUnpaidAmount?: number
  overdueType?: string
  repaymentDueDate: string
  domicileAddress?: string
  shippingAddress?: string
  currentAssigneeId?: number
  extJson?: string
}

export interface CourtCaseUpdateVO extends CourtCaseCreateVO {
  id: number
  customerStatus: string
}

export interface CourtCaseAdvanceVO {
  id: number
  action: string
  nextAssigneeId?: number
  remark?: string
}

export const createCourtCase = async (data: CourtCaseCreateVO) => {
  return await request.post({ url: '/court-case/case/create', data })
}

export const getCourtCase = async (id: number) => {
  return await request.get({ url: '/court-case/case/get?id=' + id })
}

export const updateCourtCase = async (data: CourtCaseUpdateVO) => {
  return await request.post({ url: '/court-case/case/update', data })
}

export const deleteCourtCase = async (id: number) => {
  return await request.delete({ url: '/court-case/case/delete?id=' + id })
}

export const getCourtCasePage = async (params: any) => {
  return await request.get({ url: '/court-case/case/page', params })
}

export const getCourtCaseWorkbenchPage = async (params: any) => {
  return await request.get({ url: '/court-case/case/workbench/page', params })
}

export const advanceCourtCase = async (data: CourtCaseAdvanceVO) => {
  return await request.post({ url: '/court-case/case/advance', data })
}

export const getCourtCaseFlowLogList = async (caseId: number) => {
  return await request.get({ url: '/court-case/case/flow-log/list?caseId=' + caseId })
}
