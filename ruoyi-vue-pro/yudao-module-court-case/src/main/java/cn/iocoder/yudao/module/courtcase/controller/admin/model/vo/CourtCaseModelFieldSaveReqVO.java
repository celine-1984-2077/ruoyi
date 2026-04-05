package cn.iocoder.yudao.module.courtcase.controller.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 案件模型字段保存 Request VO")
@Data
public class CourtCaseModelFieldSaveReqVO {

    @Schema(description = "字段编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字段编码不能为空")
    private String fieldCode;

    @Schema(description = "字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字段名称不能为空")
    private String fieldLabel;

    @Schema(description = "字段类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字段类型不能为空")
    private String fieldType;

    @Schema(description = "是否必填", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否必填不能为空")
    private Boolean required;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer sortNo;

    @Schema(description = "选项列表")
    @Valid
    private List<CourtCaseModelOptionReqVO> options;
}
