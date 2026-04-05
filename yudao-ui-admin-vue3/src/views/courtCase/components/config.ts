export const stageLabelMap: Record<string, string> = {
  IMPORT: '导入建档',
  ASSIGN: '分单',
  REMIND: '预提醒',
  TODAY_OVERDUE: '当日未还',
  FOLLOW_UP: '追客',
  LEGAL: '法务',
  LITIGATION: '诉讼中',
  ARCHIVED: '已归档'
}

export const stageTagTypeMap: Record<string, string> = {
  IMPORT: 'info',
  ASSIGN: 'warning',
  REMIND: 'primary',
  TODAY_OVERDUE: 'danger',
  FOLLOW_UP: 'warning',
  LEGAL: 'primary',
  LITIGATION: 'danger',
  ARCHIVED: 'success'
}

export const statusLabelMap: Record<number, string> = {
  10: '处理中',
  20: '已归档'
}

export const statusTagTypeMap: Record<number, string> = {
  10: 'warning',
  20: 'success'
}

export const actionOptionsMap: Record<string, { label: string; value: string }[]> = {
  ASSIGN: [{ label: '提交分单', value: 'SUBMIT_ASSIGN' }],
  REMIND: [{ label: '完成预提醒', value: 'SUBMIT_REMIND' }],
  TODAY_OVERDUE: [{ label: '完成当日未还处理', value: 'SUBMIT_TODAY_OVERDUE' }],
  FOLLOW_UP: [
    { label: '继续追客并移交法务', value: 'SUBMIT_FOLLOW_UP' },
    { label: '直接转法务', value: 'TRANSFER_LEGAL' }
  ],
  LEGAL: [{ label: '确认进入诉讼', value: 'FILE_LAWSUIT' }],
  LITIGATION: [{ label: '归档案件', value: 'ARCHIVE' }]
}

export const getStageLabel = (stage?: string) => {
  if (!stage) {
    return '-'
  }
  return stageLabelMap[stage] || stage
}

export const getStatusLabel = (status?: number) => {
  if (status === undefined || status === null) {
    return '-'
  }
  return statusLabelMap[status] || String(status)
}
