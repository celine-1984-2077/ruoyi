import request from '@/config/axios'

export interface CourtCaseModelOptionVO {
  label: string
  value: string
}

export interface CourtCaseModelFieldVO {
  fieldCode: string
  fieldLabel: string
  fieldType: 'TEXT' | 'DATE' | 'NUMBER' | 'SINGLE_SELECT' | 'MULTI_SELECT'
  required: boolean
  enabled: boolean
  deployed: boolean
  defaultValue?: string
  sortNo: number
  options: CourtCaseModelOptionVO[]
}

export interface CourtCaseModelVO {
  id?: number
  versionNo: number
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  changeSummary?: string
  impactSummary?: string
  publishedTime?: string
  createTime?: string
  impactHints: string[]
  fields: CourtCaseModelFieldVO[]
}

export interface CourtCaseModelConfigVO {
  published?: CourtCaseModelVO
  draft?: CourtCaseModelVO
  versions: CourtCaseModelVO[]
}

export interface CourtCaseModelDraftSaveVO {
  id?: number
  changeSummary: string
  fields: CourtCaseModelFieldVO[]
}

export const getCourtCaseModelConfig = async () => {
  return await request.get<CourtCaseModelConfigVO>({ url: '/court-case/model/config' })
}

export const saveCourtCaseModelDraft = async (data: CourtCaseModelDraftSaveVO) => {
  return await request.post<CourtCaseModelVO>({ url: '/court-case/model/draft/save', data })
}

export const publishCourtCaseModelDraft = async (data: CourtCaseModelDraftSaveVO) => {
  return await request.post<CourtCaseModelVO>({ url: '/court-case/model/draft/publish', data })
}

export const deleteCourtCaseModelDraft = async (id: number) => {
  return await request.delete({ url: '/court-case/model/draft/delete?id=' + id })
}
