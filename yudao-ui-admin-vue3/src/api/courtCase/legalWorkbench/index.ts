import request from '@/config/axios'

export interface LegalSummaryVO {
  legalCount: number
  litigationCount: number
  pendingFilingCount: number
  rejectedCount: number
  archivedCount: number
}

export interface LegalCaseVO {
  id: number
  caseNo: string
  orderNo: string
  customerName: string
  mobile: string
  amount: number
  currentStage: string
  filingStatus: string
  filingSubmitTime?: string
  evidenceCount: number
  petitionCount: number
  createTime: string
}

export interface EvidenceVO {
  id: number
  caseId: number
  category: string
  fileName: string
  fileUrl: string
  fileType?: string
  fileSize?: number
  canDelete: boolean
  createTime: string
}

export interface PetitionTemplateVO {
  id: number
  name: string
  templateFileName?: string
  templateFileUrl?: string
  templateContent: string
  placeholderDesc?: string
  enabled: boolean
  updateTime: string
}

export interface PetitionRecordVO {
  id: number
  caseId: number
  templateName: string
  outputType: string
  versionNo: number
  fileName: string
  fileUrl: string
  overwritten: boolean
  createTime: string
}

export interface FilingVO {
  id?: number
  caseId: number
  courtName?: string
  filingNo?: string
  submitTime?: string
  status: string
  rejectReason?: string
  evidenceLocked: boolean
}

export const getLegalSummary = async () => {
  return await request.get<LegalSummaryVO>({ url: '/court-case/legal-workbench/summary' })
}

export const getLegalCasePage = async (params: any) => {
  return await request.get({ url: '/court-case/legal-workbench/case-page', params })
}

export const getEvidenceList = async (caseId: number) => {
  return await request.get<EvidenceVO[]>({ url: '/court-case/legal-workbench/evidence/list', params: { caseId } })
}

export const createEvidence = async (data: any) => {
  return await request.post({ url: '/court-case/legal-workbench/evidence/create', data })
}

export const deleteEvidence = async (id: number) => {
  return await request.delete({ url: '/court-case/legal-workbench/evidence/delete', params: { id } })
}

export const downloadEvidenceZip = async (caseId: number) => {
  return await request.download({ url: '/court-case/legal-workbench/evidence/download-zip', params: { caseId } })
}

export const getPetitionTemplateList = async () => {
  return await request.get<PetitionTemplateVO[]>({ url: '/court-case/legal-workbench/petition-template/list' })
}

export const createPetitionTemplate = async (data: any) => {
  return await request.post({ url: '/court-case/legal-workbench/petition-template/create', data })
}

export const updatePetitionTemplate = async (data: any) => {
  return await request.put({ url: '/court-case/legal-workbench/petition-template/update', data })
}

export const deletePetitionTemplate = async (id: number) => {
  return await request.delete({ url: '/court-case/legal-workbench/petition-template/delete', params: { id } })
}

export const getPetitionRecordList = async (caseId: number) => {
  return await request.get<PetitionRecordVO[]>({ url: '/court-case/legal-workbench/petition/list', params: { caseId } })
}

export const generatePetition = async (data: any) => {
  return await request.post<number>({ url: '/court-case/legal-workbench/petition/generate', data })
}

export const overridePetition = async (data: any) => {
  return await request.post({ url: '/court-case/legal-workbench/petition/override', data })
}

export const getFiling = async (caseId: number) => {
  return await request.get<FilingVO>({ url: '/court-case/legal-workbench/filing/get', params: { caseId } })
}

export const saveFiling = async (data: any) => {
  return await request.post({ url: '/court-case/legal-workbench/filing/save', data })
}
