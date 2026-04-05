<template>
  <ContentWrap>
    <div class="flex flex-wrap items-start justify-between gap-12px">
      <div>
        <div class="text-16px font-600">案件模型配置</div>
        <div class="mt-8px text-14px text-[var(--el-text-color-secondary)]">
          这里管理案件扩展字段。每次发布都会生成一个新版本，已投产字段不能删除，只能停用。
        </div>
      </div>
      <div class="flex gap-8px">
        <el-button @click="resetEditor">恢复已发布版本</el-button>
        <el-button
          v-if="draftId"
          v-hasPermi="['court-case:model:update']"
          plain
          type="danger"
          @click="handleDeleteDraft"
        >
          删除草稿
        </el-button>
        <el-button
          v-hasPermi="['court-case:model:update']"
          type="primary"
          plain
          :loading="saving"
          @click="handleSaveDraft"
        >
          保存草稿
        </el-button>
        <el-button
          v-hasPermi="['court-case:model:publish']"
          type="primary"
          :loading="publishing"
          @click="handlePublish"
        >
          发布新版本
        </el-button>
      </div>
    </div>
  </ContentWrap>

  <ContentWrap>
    <div class="grid grid-cols-1 gap-16px lg:grid-cols-3">
      <el-card shadow="never">
        <template #header>当前发布版本</template>
        <div class="text-28px font-700 text-[var(--el-color-primary)]">
          v{{ config.published?.versionNo || 0 }}
        </div>
        <div class="mt-8px text-13px text-[var(--el-text-color-secondary)]">
          {{ config.published?.publishedTime || '暂未发布' }}
        </div>
      </el-card>
      <el-card shadow="never">
        <template #header>当前编辑草稿</template>
        <div class="text-28px font-700 text-[var(--el-color-warning)]">
          v{{ draftId ? nextVersionNo : config.published?.versionNo || 0 }}
        </div>
        <div class="mt-8px text-13px text-[var(--el-text-color-secondary)]">
          {{ draftId ? '存在未发布草稿' : '当前无独立草稿，保存后自动创建' }}
        </div>
      </el-card>
      <el-card shadow="never">
        <template #header>变更提醒</template>
        <div v-if="impactHints.length" class="space-y-6px">
          <div v-for="hint in impactHints" :key="hint" class="text-13px leading-20px">
            {{ hint }}
          </div>
        </div>
        <div v-else class="text-13px text-[var(--el-text-color-secondary)]">编辑字段后，这里会显示发布影响。</div>
      </el-card>
    </div>
  </ContentWrap>

  <ContentWrap>
    <el-form label-width="100px">
      <el-form-item label="变更说明" required>
        <el-input
          v-model="changeSummary"
          type="textarea"
          :rows="3"
          placeholder="例如：补充立案法院、案件标签；停用旧催收等级字段"
        />
      </el-form-item>
    </el-form>

    <div class="mb-12px flex items-center justify-between">
      <div class="text-15px font-600">扩展字段</div>
      <el-button v-hasPermi="['court-case:model:update']" plain type="primary" @click="handleAddField">
        <Icon icon="ep:plus" class="mr-5px" />
        新增字段
      </el-button>
    </div>

    <el-table :data="editorFields" border>
      <el-table-column label="字段编码" min-width="150">
        <template #default="{ row }">
          <el-input v-model="row.fieldCode" :disabled="row.deployed" placeholder="如 hearingCourt" />
        </template>
      </el-table-column>
      <el-table-column label="字段名称" min-width="140">
        <template #default="{ row }">
          <el-input v-model="row.fieldLabel" placeholder="如 立案法院" />
        </template>
      </el-table-column>
      <el-table-column label="类型" min-width="140">
        <template #default="{ row }">
          <el-select v-model="row.fieldType" class="!w-full">
            <el-option v-for="item in fieldTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="默认值" min-width="160">
        <template #default="{ row }">
          <el-input v-model="row.defaultValue" placeholder="可选" />
        </template>
      </el-table-column>
      <el-table-column label="排序" width="90">
        <template #default="{ row }">
          <el-input-number v-model="row.sortNo" :min="1" class="!w-full" />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="170">
        <template #default="{ row }">
          <div class="flex flex-col gap-8px">
            <el-switch v-model="row.enabled" active-text="启用" inactive-text="停用" />
            <el-checkbox v-model="row.required" :disabled="!row.enabled">必填</el-checkbox>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="选项" min-width="220">
        <template #default="{ row }">
          <el-input
            v-if="isOptionType(row.fieldType)"
            v-model="row.optionsText"
            type="textarea"
            :rows="3"
            placeholder="每行一个选项，支持 标签:值 或 直接填值"
          />
          <span v-else class="text-[var(--el-text-color-secondary)]">非选项字段</span>
        </template>
      </el-table-column>
      <el-table-column label="说明" min-width="120">
        <template #default="{ row }">
          <el-tag v-if="row.deployed" type="warning">已投产</el-tag>
          <div class="mt-8px text-12px text-[var(--el-text-color-secondary)]">
            {{ row.deployed ? '不可删除，可停用' : '新字段，发布后生效' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row, $index }">
          <el-button link type="danger" :disabled="row.deployed" @click="editorFields.splice($index, 1)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <ContentWrap>
    <div class="mb-12px text-15px font-600">版本记录</div>
    <el-timeline>
      <el-timeline-item
        v-for="item in config.versions"
        :key="item.id"
        :timestamp="item.publishedTime || item.createTime"
        :type="item.status === 'PUBLISHED' ? 'primary' : item.status === 'DRAFT' ? 'warning' : 'info'"
      >
        <div class="flex items-center gap-8px">
          <span class="font-600">v{{ item.versionNo }}</span>
          <el-tag :type="item.status === 'PUBLISHED' ? 'success' : item.status === 'DRAFT' ? 'warning' : 'info'">
            {{ statusLabelMap[item.status] }}
          </el-tag>
        </div>
        <div class="mt-6px text-14px">{{ item.changeSummary || '未填写变更说明' }}</div>
        <div v-if="item.impactHints?.length" class="mt-6px text-13px text-[var(--el-text-color-secondary)]">
          {{ item.impactHints.join('；') }}
        </div>
      </el-timeline-item>
    </el-timeline>
  </ContentWrap>
</template>

<script lang="ts" setup>
import * as CourtCaseModelApi from '@/api/courtCase/model'

defineOptions({ name: 'CourtCaseModelConfig' })

interface EditorField extends CourtCaseModelApi.CourtCaseModelFieldVO {
  optionsText: string
}

const message = useMessage()

const saving = ref(false)
const publishing = ref(false)
const config = reactive<CourtCaseModelApi.CourtCaseModelConfigVO>({
  published: undefined,
  draft: undefined,
  versions: []
})
const editorFields = ref<EditorField[]>([])
const changeSummary = ref('')
const draftId = ref<number | undefined>()

const statusLabelMap = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  ARCHIVED: '历史版本'
}

const fieldTypeOptions = [
  { label: '文本', value: 'TEXT' },
  { label: '日期', value: 'DATE' },
  { label: '数字', value: 'NUMBER' },
  { label: '单选', value: 'SINGLE_SELECT' },
  { label: '多选', value: 'MULTI_SELECT' }
]

const nextVersionNo = computed(() => {
  const maxVersion = config.versions.reduce((max, item) => Math.max(max, item.versionNo || 0), 0)
  return maxVersion + (draftId.value && config.draft ? 0 : 1)
})

const impactHints = computed(() => buildImpactHints(editorFields.value, config.published?.fields || []))

const isOptionType = (type: string) => ['SINGLE_SELECT', 'MULTI_SELECT'].includes(type)

const formatOptionsText = (options: CourtCaseModelApi.CourtCaseModelOptionVO[] = []) =>
  options.map((item) => (item.label === item.value ? item.value : `${item.label}:${item.value}`)).join('\n')

const parseOptionsText = (text: string) => {
  return text
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const separatorIndex = line.indexOf(':')
      if (separatorIndex === -1) {
        return { label: line, value: line }
      }
      return {
        label: line.slice(0, separatorIndex).trim(),
        value: line.slice(separatorIndex + 1).trim()
      }
    })
}

const toEditorFields = (fields: CourtCaseModelApi.CourtCaseModelFieldVO[] = []): EditorField[] => {
  return [...fields]
    .sort((a, b) => a.sortNo - b.sortNo)
    .map((field) => ({
      ...field,
      optionsText: formatOptionsText(field.options)
    }))
}

const buildImpactHints = (draftFields: EditorField[], publishedFields: CourtCaseModelApi.CourtCaseModelFieldVO[]) => {
  if (!publishedFields.length) {
    return ['首次发布后，案件建档页会按这套扩展字段渲染。']
  }
  const publishedMap = new Map(publishedFields.map((field) => [field.fieldCode, field]))
  const hints: string[] = []
  draftFields.forEach((field) => {
    const published = publishedMap.get(field.fieldCode)
    const optionsText = formatOptionsText(field.options)
    const publishedOptionsText = formatOptionsText(published?.options)
    if (!published) {
      hints.push(field.required ? `新增必填字段「${field.fieldLabel}」` : `新增字段「${field.fieldLabel}」`)
      return
    }
    if (published.fieldType !== field.fieldType) {
      hints.push(`字段「${field.fieldLabel}」类型已变更`)
    }
    if (!published.required && field.required) {
      hints.push(`字段「${field.fieldLabel}」改为必填`)
    }
    if (published.enabled && !field.enabled) {
      hints.push(`字段「${field.fieldLabel}」已停用`)
    }
    if (publishedOptionsText !== optionsText && isOptionType(field.fieldType)) {
      hints.push(`字段「${field.fieldLabel}」选项已调整`)
    }
  })
  return hints.length ? hints : ['本次模型结构与已发布版本兼容，可直接发布。']
}

const loadConfig = async () => {
  const data = await CourtCaseModelApi.getCourtCaseModelConfig()
  config.published = data.published
  config.draft = data.draft
  config.versions = data.versions || []
  resetEditor()
}

const resetEditor = () => {
  const source = config.draft || config.published
  editorFields.value = toEditorFields(source?.fields || [])
  changeSummary.value = source?.changeSummary || ''
  draftId.value = config.draft?.id
}

const handleAddField = () => {
  editorFields.value.push({
    fieldCode: '',
    fieldLabel: '',
    fieldType: 'TEXT',
    required: false,
    enabled: true,
    deployed: false,
    defaultValue: '',
    sortNo: editorFields.value.length + 1,
    options: [],
    optionsText: ''
  })
}

const buildPayload = () => {
  if (!changeSummary.value.trim()) {
    throw new Error('请先填写变更说明')
  }
  if (!editorFields.value.length) {
    throw new Error('请至少保留一个扩展字段')
  }
  const fields = editorFields.value.map((field, index) => ({
    fieldCode: field.fieldCode.trim(),
    fieldLabel: field.fieldLabel.trim(),
    fieldType: field.fieldType,
    required: field.enabled ? field.required : false,
    enabled: field.enabled,
    defaultValue: field.defaultValue?.trim() || undefined,
    sortNo: field.sortNo || index + 1,
    options: isOptionType(field.fieldType) ? parseOptionsText(field.optionsText) : []
  }))
  if (fields.some((field) => !field.fieldCode || !field.fieldLabel)) {
    throw new Error('字段编码和字段名称不能为空')
  }
  return {
    id: draftId.value,
    changeSummary: changeSummary.value.trim(),
    fields
  }
}

const handleSaveDraft = async () => {
  try {
    const payload = buildPayload()
    saving.value = true
    const data = await CourtCaseModelApi.saveCourtCaseModelDraft(payload)
    message.success('案件模型草稿已保存')
    draftId.value = data.id
    await loadConfig()
  } catch (error: any) {
    if (error?.message) {
      message.warning(error.message)
    }
  } finally {
    saving.value = false
  }
}

const handlePublish = async () => {
  try {
    const payload = buildPayload()
    await message.confirm('发布后会切换案件建档表单到新版本，确定继续吗？')
    publishing.value = true
    await CourtCaseModelApi.publishCourtCaseModelDraft(payload)
    message.success('案件模型已发布')
    await loadConfig()
  } catch (error: any) {
    if (error?.message && error.message !== 'cancel') {
      message.warning(error.message)
    }
  } finally {
    publishing.value = false
  }
}

const handleDeleteDraft = async () => {
  if (!draftId.value) {
    return
  }
  try {
    await message.confirm('删除后当前草稿字段会被清空，确定继续吗？')
    await CourtCaseModelApi.deleteCourtCaseModelDraft(draftId.value)
    message.success('案件模型草稿已删除')
    await loadConfig()
  } catch {}
}

onMounted(() => {
  loadConfig()
})
</script>
