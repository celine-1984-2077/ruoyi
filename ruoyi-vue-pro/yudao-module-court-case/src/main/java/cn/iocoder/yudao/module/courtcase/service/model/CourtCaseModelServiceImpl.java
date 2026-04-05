package cn.iocoder.yudao.module.courtcase.service.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.courtcase.controller.admin.model.vo.*;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.model.CourtCaseModelDO;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.model.CourtCaseModelFieldDO;
import cn.iocoder.yudao.module.courtcase.dal.mysql.model.CourtCaseModelFieldMapper;
import cn.iocoder.yudao.module.courtcase.dal.mysql.model.CourtCaseModelMapper;
import cn.iocoder.yudao.module.courtcase.enums.CourtCaseModelFieldTypeEnum;
import cn.iocoder.yudao.module.courtcase.enums.CourtCaseModelStatusEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.courtcase.enums.ErrorCodeConstants.*;

@Service
@Validated
public class CourtCaseModelServiceImpl implements CourtCaseModelService {

    @Resource
    private CourtCaseModelMapper courtCaseModelMapper;
    @Resource
    private CourtCaseModelFieldMapper courtCaseModelFieldMapper;

    @Override
    public CourtCaseModelConfigRespVO getModelConfig() {
        CourtCaseModelDO published = courtCaseModelMapper.selectLatestPublished();
        CourtCaseModelDO draft = courtCaseModelMapper.selectLatestDraft();
        List<CourtCaseModelDO> versions = courtCaseModelMapper.selectVersionList();
        Set<Long> modelIds = versions.stream().map(CourtCaseModelDO::getId).collect(Collectors.toSet());
        Map<Long, List<CourtCaseModelFieldDO>> fieldMap = courtCaseModelFieldMapper.selectListByModelIds(modelIds)
                .stream().collect(Collectors.groupingBy(CourtCaseModelFieldDO::getModelId));
        Set<String> publishedFieldCodes = getFieldCodeSet(fieldMap.get(published != null ? published.getId() : null));

        CourtCaseModelConfigRespVO respVO = new CourtCaseModelConfigRespVO();
        respVO.setPublished(toRespVO(published, fieldMap.get(published != null ? published.getId() : null), publishedFieldCodes));
        respVO.setDraft(toRespVO(draft, fieldMap.get(draft != null ? draft.getId() : null), publishedFieldCodes));
        respVO.setVersions(versions.stream()
                .map(model -> toRespVO(model, fieldMap.get(model.getId()), publishedFieldCodes))
                .collect(Collectors.toList()));
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourtCaseModelRespVO saveDraft(CourtCaseModelDraftSaveReqVO reqVO) {
        CourtCaseModelDO published = courtCaseModelMapper.selectLatestPublished();
        CourtCaseModelDO draft = getOrCreateDraft(reqVO.getId(), reqVO.getChangeSummary());
        validateFieldDefinitions(reqVO.getFields(), published);
        List<String> impactHints = buildImpactHints(reqVO.getFields(), published);
        draft.setChangeSummary(reqVO.getChangeSummary())
                .setImpactSummary(joinImpactHints(impactHints));
        courtCaseModelMapper.updateById(draft);
        saveFields(draft.getId(), reqVO.getFields(), published);
        return toRespVO(draft, courtCaseModelFieldMapper.selectListByModelId(draft.getId()), getFieldCodeSet(
                published != null ? courtCaseModelFieldMapper.selectListByModelId(published.getId()) : null), impactHints);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourtCaseModelRespVO publishDraft(CourtCaseModelDraftSaveReqVO reqVO) {
        CourtCaseModelRespVO draftResp = saveDraft(reqVO);
        CourtCaseModelDO draft = courtCaseModelMapper.selectById(draftResp.getId());
        CourtCaseModelDO published = courtCaseModelMapper.selectLatestPublished();
        if (published != null) {
            published.setStatus(CourtCaseModelStatusEnum.ARCHIVED.name());
            courtCaseModelMapper.updateById(published);
        }
        draft.setStatus(CourtCaseModelStatusEnum.PUBLISHED.name())
                .setPublishedTime(LocalDateTime.now());
        courtCaseModelMapper.updateById(draft);
        return toRespVO(draft, courtCaseModelFieldMapper.selectListByModelId(draft.getId()),
                getFieldCodeSet(courtCaseModelFieldMapper.selectListByModelId(draft.getId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDraft(Long id) {
        CourtCaseModelDO draft = courtCaseModelMapper.selectById(id);
        if (draft == null) {
            throw exception(CASE_MODEL_NOT_EXISTS);
        }
        if (!CourtCaseModelStatusEnum.DRAFT.name().equals(draft.getStatus())) {
            throw exception(CASE_MODEL_DRAFT_ONLY);
        }
        courtCaseModelFieldMapper.deleteByModelId(id);
        courtCaseModelMapper.deleteById(id);
    }

    @Override
    public String normalizeAndValidateExtJson(String extJson) {
        CourtCaseModelDO published = courtCaseModelMapper.selectLatestPublished();
        if (published == null) {
            if (StrUtil.isBlank(extJson) || "{}".equals(StrUtil.trim(extJson))) {
                return null;
            }
            throw exception(CASE_MODEL_NOT_PUBLISHED);
        }
        List<CourtCaseModelFieldDO> fields = courtCaseModelFieldMapper.selectListByModelId(published.getId()).stream()
                .filter(CourtCaseModelFieldDO::getEnabled)
                .sorted(Comparator.comparing(CourtCaseModelFieldDO::getSortNo))
                .collect(Collectors.toList());
        Map<String, Object> payload = StrUtil.isBlank(extJson)
                ? new LinkedHashMap<>()
                : JsonUtils.parseObjectQuietly(extJson, new TypeReference<LinkedHashMap<String, Object>>() {});
        if (payload == null) {
            throw exception(CASE_MODEL_EXT_JSON_INVALID);
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        Set<String> supportedCodes = fields.stream().map(CourtCaseModelFieldDO::getFieldCode).collect(Collectors.toSet());
        for (String code : payload.keySet()) {
            if (!supportedCodes.contains(code)) {
                throw exception(CASE_MODEL_FIELD_NOT_SUPPORTED, code);
            }
        }
        for (CourtCaseModelFieldDO field : fields) {
            Object value = payload.get(field.getFieldCode());
            if (isEmptyValue(value)) {
                if (Boolean.TRUE.equals(field.getRequired())) {
                    throw exception(CASE_MODEL_REQUIRED_FIELD_MISSING, field.getFieldLabel());
                }
                continue;
            }
            normalized.put(field.getFieldCode(), normalizeFieldValue(field, value));
        }
        return normalized.isEmpty() ? null : JsonUtils.toJsonString(normalized);
    }

    private CourtCaseModelDO getOrCreateDraft(Long id, String changeSummary) {
        if (id != null) {
            CourtCaseModelDO draft = courtCaseModelMapper.selectById(id);
            if (draft == null) {
                throw exception(CASE_MODEL_NOT_EXISTS);
            }
            if (!CourtCaseModelStatusEnum.DRAFT.name().equals(draft.getStatus())) {
                throw exception(CASE_MODEL_DRAFT_ONLY);
            }
            return draft;
        }
        CourtCaseModelDO latestDraft = courtCaseModelMapper.selectLatestDraft();
        if (latestDraft != null) {
            latestDraft.setChangeSummary(changeSummary);
            courtCaseModelMapper.updateById(latestDraft);
            return latestDraft;
        }
        Integer maxVersionNo = courtCaseModelMapper.selectMaxVersionNo();
        CourtCaseModelDO draft = new CourtCaseModelDO()
                .setVersionNo(maxVersionNo == null ? 1 : maxVersionNo + 1)
                .setStatus(CourtCaseModelStatusEnum.DRAFT.name())
                .setChangeSummary(changeSummary)
                .setImpactSummary("");
        courtCaseModelMapper.insert(draft);
        return draft;
    }

    private void validateFieldDefinitions(List<CourtCaseModelFieldSaveReqVO> fields, CourtCaseModelDO published) {
        Set<String> fieldCodes = new HashSet<>();
        Set<String> fieldLabels = new HashSet<>();
        for (CourtCaseModelFieldSaveReqVO field : fields) {
            CourtCaseModelFieldTypeEnum fieldType = CourtCaseModelFieldTypeEnum.valueOfType(field.getFieldType());
            if (fieldType == null) {
                throw exception(CASE_MODEL_FIELD_TYPE_INVALID, field.getFieldType());
            }
            if (!fieldCodes.add(field.getFieldCode())) {
                throw exception(CASE_MODEL_FIELD_CODE_DUPLICATE, field.getFieldCode());
            }
            if (!fieldLabels.add(field.getFieldLabel())) {
                throw exception(CASE_MODEL_FIELD_LABEL_DUPLICATE, field.getFieldLabel());
            }
            if (!field.getFieldCode().matches("^[a-z][a-zA-Z0-9_]{1,63}$")) {
                throw exception(CASE_MODEL_FIELD_CODE_INVALID, field.getFieldCode());
            }
            validateFieldOptions(field, fieldType);
        }
        if (published == null) {
            return;
        }
        Set<String> publishedCodes = courtCaseModelFieldMapper.selectListByModelId(published.getId()).stream()
                .map(CourtCaseModelFieldDO::getFieldCode)
                .collect(Collectors.toSet());
        Set<String> currentCodes = fields.stream().map(CourtCaseModelFieldSaveReqVO::getFieldCode).collect(Collectors.toSet());
        for (String publishedCode : publishedCodes) {
            if (!currentCodes.contains(publishedCode)) {
                throw exception(CASE_MODEL_FIELD_DELETE_FORBIDDEN, publishedCode);
            }
        }
    }

    private void validateFieldOptions(CourtCaseModelFieldSaveReqVO field, CourtCaseModelFieldTypeEnum fieldType) {
        if (fieldType == CourtCaseModelFieldTypeEnum.SINGLE_SELECT || fieldType == CourtCaseModelFieldTypeEnum.MULTI_SELECT) {
            if (CollUtil.isEmpty(field.getOptions())) {
                throw exception(CASE_MODEL_OPTIONS_REQUIRED, field.getFieldLabel());
            }
            Set<String> optionValues = new HashSet<>();
            for (CourtCaseModelOptionReqVO option : field.getOptions()) {
                if (!optionValues.add(option.getValue())) {
                    throw exception(CASE_MODEL_OPTION_VALUE_DUPLICATE, option.getValue());
                }
            }
        } else if (CollUtil.isNotEmpty(field.getOptions())) {
            throw exception(CASE_MODEL_OPTIONS_NOT_SUPPORTED, field.getFieldLabel());
        }
    }

    private List<String> buildImpactHints(List<CourtCaseModelFieldSaveReqVO> fields, CourtCaseModelDO published) {
        if (published == null) {
            return Collections.singletonList("首次发布后，案件建档页会按这套扩展字段渲染。");
        }
        Map<String, CourtCaseModelFieldDO> publishedFieldMap = courtCaseModelFieldMapper.selectListByModelId(published.getId()).stream()
                .collect(Collectors.toMap(CourtCaseModelFieldDO::getFieldCode, field -> field));
        List<String> hints = new ArrayList<>();
        for (CourtCaseModelFieldSaveReqVO field : fields) {
            CourtCaseModelFieldDO oldField = publishedFieldMap.get(field.getFieldCode());
            if (oldField == null) {
                hints.add(Boolean.TRUE.equals(field.getRequired())
                        ? StrUtil.format("新增必填字段「{}」，发布后新建案件必须填写。", field.getFieldLabel())
                        : StrUtil.format("新增扩展字段「{}」，发布后新建案件会展示该字段。", field.getFieldLabel()));
                continue;
            }
            if (!StrUtil.equals(oldField.getFieldType(), field.getFieldType())) {
                hints.add(StrUtil.format("字段「{}」类型已变更，请确认历史数据兼容性。", field.getFieldLabel()));
            }
            if (!Objects.equals(oldField.getRequired(), field.getRequired()) && Boolean.TRUE.equals(field.getRequired())) {
                hints.add(StrUtil.format("字段「{}」改为必填，发布后录入要求会更严格。", field.getFieldLabel()));
            }
            if (!Objects.equals(oldField.getEnabled(), field.getEnabled()) && Boolean.FALSE.equals(field.getEnabled())) {
                hints.add(StrUtil.format("字段「{}」已停用，历史值会保留，但新建案件不再展示。", field.getFieldLabel()));
            }
            if (!StrUtil.equals(StrUtil.blankToDefault(oldField.getOptionsJson(), ""), toOptionsJson(field.getOptions()))) {
                CourtCaseModelFieldTypeEnum fieldType = CourtCaseModelFieldTypeEnum.valueOfType(field.getFieldType());
                if (fieldType == CourtCaseModelFieldTypeEnum.SINGLE_SELECT || fieldType == CourtCaseModelFieldTypeEnum.MULTI_SELECT) {
                    hints.add(StrUtil.format("字段「{}」选项已调整，请确认历史值仍可识别。", field.getFieldLabel()));
                }
            }
        }
        return hints.isEmpty() ? Collections.singletonList("本次模型结构与已发布版本兼容，可直接发布。") : hints;
    }

    private void saveFields(Long modelId, List<CourtCaseModelFieldSaveReqVO> fields, CourtCaseModelDO published) {
        Set<String> publishedCodes = published == null ? Collections.emptySet()
                : courtCaseModelFieldMapper.selectListByModelId(published.getId()).stream()
                .map(CourtCaseModelFieldDO::getFieldCode).collect(Collectors.toSet());
        courtCaseModelFieldMapper.deleteByModelId(modelId);
        for (CourtCaseModelFieldSaveReqVO field : fields) {
            courtCaseModelFieldMapper.insert(new CourtCaseModelFieldDO()
                    .setModelId(modelId)
                    .setFieldCode(field.getFieldCode())
                    .setFieldLabel(field.getFieldLabel())
                    .setFieldType(CourtCaseModelFieldTypeEnum.valueOfType(field.getFieldType()).name())
                    .setRequired(field.getRequired())
                    .setEnabled(field.getEnabled())
                    .setDeployed(publishedCodes.contains(field.getFieldCode()))
                    .setDefaultValue(field.getDefaultValue())
                    .setOptionsJson(toOptionsJson(field.getOptions()))
                    .setSortNo(field.getSortNo()));
        }
    }

    private Object normalizeFieldValue(CourtCaseModelFieldDO field, Object value) {
        CourtCaseModelFieldTypeEnum fieldType = CourtCaseModelFieldTypeEnum.valueOfType(field.getFieldType());
        if (fieldType == null) {
            throw exception(CASE_MODEL_FIELD_TYPE_INVALID, field.getFieldType());
        }
        switch (fieldType) {
            case TEXT:
                return String.valueOf(value);
            case DATE:
                try {
                    String dateStr = String.valueOf(value);
                    LocalDate.parse(dateStr);
                    return dateStr;
                } catch (Exception e) {
                    throw exception(CASE_MODEL_FIELD_VALUE_INVALID, field.getFieldLabel());
                }
            case NUMBER:
                if (!NumberUtil.isNumber(String.valueOf(value))) {
                    throw exception(CASE_MODEL_FIELD_VALUE_INVALID, field.getFieldLabel());
                }
                return NumberUtil.toBigDecimal(String.valueOf(value));
            case SINGLE_SELECT:
                String optionValue = String.valueOf(value);
                validateOptionValue(field, Collections.singletonList(optionValue));
                return optionValue;
            case MULTI_SELECT:
                List<String> values = normalizeMultiValue(value);
                validateOptionValue(field, values);
                return values;
            default:
                throw exception(CASE_MODEL_FIELD_TYPE_INVALID, field.getFieldType());
        }
    }

    private void validateOptionValue(CourtCaseModelFieldDO field, List<String> values) {
        List<CourtCaseModelOptionRespVO> options = parseOptions(field.getOptionsJson());
        Set<String> optionValues = options.stream().map(CourtCaseModelOptionRespVO::getValue).collect(Collectors.toSet());
        for (String value : values) {
            if (!optionValues.contains(value)) {
                throw exception(CASE_MODEL_FIELD_VALUE_INVALID, field.getFieldLabel());
            }
        }
    }

    private List<String> normalizeMultiValue(Object value) {
        if (value instanceof List) {
            return ((List<?>) value).stream().map(String::valueOf).collect(Collectors.toList());
        }
        return Collections.singletonList(String.valueOf(value));
    }

    private boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return StrUtil.isBlank((String) value);
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        return false;
    }

    private CourtCaseModelRespVO toRespVO(CourtCaseModelDO model, List<CourtCaseModelFieldDO> fields, Set<String> publishedFieldCodes) {
        return toRespVO(model, fields, publishedFieldCodes, null);
    }

    private CourtCaseModelRespVO toRespVO(CourtCaseModelDO model, List<CourtCaseModelFieldDO> fields,
                                          Set<String> publishedFieldCodes, List<String> overrideImpactHints) {
        if (model == null) {
            return null;
        }
        CourtCaseModelRespVO respVO = new CourtCaseModelRespVO();
        respVO.setId(model.getId());
        respVO.setVersionNo(model.getVersionNo());
        respVO.setStatus(model.getStatus());
        respVO.setChangeSummary(model.getChangeSummary());
        respVO.setImpactSummary(model.getImpactSummary());
        respVO.setPublishedTime(model.getPublishedTime());
        respVO.setCreateTime(model.getCreateTime());
        respVO.setImpactHints(overrideImpactHints != null ? overrideImpactHints : splitImpactHints(model.getImpactSummary()));
        respVO.setFields((fields == null ? Collections.<CourtCaseModelFieldDO>emptyList() : fields).stream()
                .map(field -> {
                    CourtCaseModelFieldRespVO fieldRespVO = new CourtCaseModelFieldRespVO();
                    fieldRespVO.setFieldCode(field.getFieldCode());
                    fieldRespVO.setFieldLabel(field.getFieldLabel());
                    fieldRespVO.setFieldType(field.getFieldType());
                    fieldRespVO.setRequired(field.getRequired());
                    fieldRespVO.setEnabled(field.getEnabled());
                    fieldRespVO.setDeployed(Boolean.TRUE.equals(field.getDeployed()) || publishedFieldCodes.contains(field.getFieldCode()));
                    fieldRespVO.setDefaultValue(field.getDefaultValue());
                    fieldRespVO.setSortNo(field.getSortNo());
                    fieldRespVO.setOptions(parseOptions(field.getOptionsJson()));
                    return fieldRespVO;
                }).collect(Collectors.toList()));
        return respVO;
    }

    private Set<String> getFieldCodeSet(List<CourtCaseModelFieldDO> fields) {
        if (fields == null) {
            return Collections.emptySet();
        }
        return fields.stream().map(CourtCaseModelFieldDO::getFieldCode).collect(Collectors.toSet());
    }

    private String toOptionsJson(List<CourtCaseModelOptionReqVO> options) {
        if (CollUtil.isEmpty(options)) {
            return null;
        }
        return JsonUtils.toJsonString(options);
    }

    private List<CourtCaseModelOptionRespVO> parseOptions(String optionsJson) {
        if (StrUtil.isBlank(optionsJson)) {
            return Collections.emptyList();
        }
        List<CourtCaseModelOptionRespVO> options = JsonUtils.parseObjectQuietly(optionsJson,
                new TypeReference<List<CourtCaseModelOptionRespVO>>() {});
        return options == null ? Collections.emptyList() : options;
    }

    private String joinImpactHints(List<String> impactHints) {
        return impactHints == null ? "" : String.join("\n", impactHints);
    }

    private List<String> splitImpactHints(String impactSummary) {
        if (StrUtil.isBlank(impactSummary)) {
            return Collections.emptyList();
        }
        return Arrays.stream(impactSummary.split("\\n"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}
