package cn.iocoder.yudao.module.courtcase.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {

    ErrorCode CASE_NOT_EXISTS = new ErrorCode(1_030_000_000, "案件不存在");
    ErrorCode CASE_STAGE_NOT_MATCH = new ErrorCode(1_030_000_001, "案件当前阶段不允许执行该动作");
    ErrorCode CASE_ACTION_NOT_SUPPORTED = new ErrorCode(1_030_000_002, "案件动作不受支持");
    ErrorCode CASE_OPERATOR_NOT_ASSIGNEE = new ErrorCode(1_030_000_003, "当前用户不是案件负责人，无法推进案件");
    ErrorCode CASE_OPERATOR_NOT_IN_DEPT = new ErrorCode(1_030_000_004, "当前用户不属于案件当前部门，无法推进案件");
    ErrorCode CASE_DELETE_ARCHIVED_FORBIDDEN = new ErrorCode(1_030_000_020, "已归档案件不允许删除");
    ErrorCode CASE_REPAY_DATE_REQUIRED = new ErrorCode(1_030_000_021, "案件缺少应还款日期，无法进入客服提醒流程");
    ErrorCode CASE_FOLLOW_UP_CONTENT_TOO_LONG = new ErrorCode(1_030_000_022, "跟进备注不能超过 500 字");
    ErrorCode CASE_TRANSFER_NOT_OVERDUE = new ErrorCode(1_030_000_023, "案件未逾期超过 15 天，暂不允许移交法诉");
    ErrorCode CASE_TRANSFER_RECEIVER_REQUIRED = new ErrorCode(1_030_000_024, "请选择接收法务人员");
    ErrorCode CASE_TRANSFER_NOT_EXISTS = new ErrorCode(1_030_000_025, "案件移交记录不存在");
    ErrorCode CASE_TRANSFER_RECALL_EXPIRED = new ErrorCode(1_030_000_026, "移交已超过 24 小时，不能撤回");
    ErrorCode CASE_TRANSFER_RECALL_STATUS_INVALID = new ErrorCode(1_030_000_027, "当前移交状态不允许撤回");
    ErrorCode CASE_LEGAL_STAGE_REQUIRED = new ErrorCode(1_030_000_028, "当前案件还未进入法务阶段");
    ErrorCode CASE_EVIDENCE_CATEGORY_INVALID = new ErrorCode(1_030_000_029, "证据材料分类不合法");
    ErrorCode CASE_EVIDENCE_FILE_REQUIRED = new ErrorCode(1_030_000_030, "请先上传证据材料文件");
    ErrorCode CASE_EVIDENCE_NOT_EXISTS = new ErrorCode(1_030_000_031, "证据材料不存在");
    ErrorCode CASE_EVIDENCE_DELETE_FORBIDDEN = new ErrorCode(1_030_000_032, "案件已提交立案，证据材料不可删除");
    ErrorCode CASE_EVIDENCE_BATCH_LIMIT = new ErrorCode(1_030_000_033, "单次最多上传 10 份证据材料");
    ErrorCode CASE_PETITION_TEMPLATE_NOT_EXISTS = new ErrorCode(1_030_000_034, "诉状模板不存在");
    ErrorCode CASE_PETITION_TEMPLATE_CONTENT_REQUIRED = new ErrorCode(1_030_000_035, "诉状模板内容不能为空");
    ErrorCode CASE_PETITION_RECORD_NOT_EXISTS = new ErrorCode(1_030_000_036, "诉状生成记录不存在");
    ErrorCode CASE_PETITION_OVERRIDE_FILE_REQUIRED = new ErrorCode(1_030_000_037, "请先上传新的诉状文件");
    ErrorCode CASE_FILING_REJECT_REASON_REQUIRED = new ErrorCode(1_030_000_038, "立案驳回时必须填写驳回原因");
    ErrorCode CASE_FILING_NOT_EXISTS = new ErrorCode(1_030_000_039, "立案信息不存在");
    ErrorCode CASE_PERMISSION_DENIED = new ErrorCode(1_030_000_040, "当前用户无权查看或操作该案件");
    ErrorCode CASE_MODEL_NOT_EXISTS = new ErrorCode(1_030_000_005, "案件模型不存在");
    ErrorCode CASE_MODEL_DRAFT_ONLY = new ErrorCode(1_030_000_006, "只有草稿版本支持编辑");
    ErrorCode CASE_MODEL_FIELD_TYPE_INVALID = new ErrorCode(1_030_000_007, "案件模型字段类型不合法: {}");
    ErrorCode CASE_MODEL_FIELD_CODE_DUPLICATE = new ErrorCode(1_030_000_008, "字段编码重复: {}");
    ErrorCode CASE_MODEL_FIELD_LABEL_DUPLICATE = new ErrorCode(1_030_000_009, "字段名称重复: {}");
    ErrorCode CASE_MODEL_FIELD_CODE_INVALID = new ErrorCode(1_030_000_010, "字段编码格式不合法: {}");
    ErrorCode CASE_MODEL_OPTIONS_REQUIRED = new ErrorCode(1_030_000_011, "字段 {} 需要配置选项");
    ErrorCode CASE_MODEL_OPTIONS_NOT_SUPPORTED = new ErrorCode(1_030_000_012, "字段 {} 不是选项类型，不能配置选项");
    ErrorCode CASE_MODEL_OPTION_VALUE_DUPLICATE = new ErrorCode(1_030_000_013, "字段选项值重复: {}");
    ErrorCode CASE_MODEL_FIELD_DELETE_FORBIDDEN = new ErrorCode(1_030_000_014, "已投产字段不能删除，请改为停用: {}");
    ErrorCode CASE_MODEL_NOT_PUBLISHED = new ErrorCode(1_030_000_015, "当前还没有发布的案件模型");
    ErrorCode CASE_MODEL_EXT_JSON_INVALID = new ErrorCode(1_030_000_016, "案件扩展字段 JSON 格式不正确");
    ErrorCode CASE_MODEL_REQUIRED_FIELD_MISSING = new ErrorCode(1_030_000_017, "案件扩展字段缺少必填项: {}");
    ErrorCode CASE_MODEL_FIELD_NOT_SUPPORTED = new ErrorCode(1_030_000_018, "案件扩展字段未在当前模型中定义: {}");
    ErrorCode CASE_MODEL_FIELD_VALUE_INVALID = new ErrorCode(1_030_000_019, "案件扩展字段值不合法: {}");
}
