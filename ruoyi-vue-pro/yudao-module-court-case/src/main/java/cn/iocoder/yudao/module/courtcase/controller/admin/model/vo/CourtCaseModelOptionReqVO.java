package cn.iocoder.yudao.module.courtcase.controller.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 案件模型字段选项 Request VO")
@Data
public class CourtCaseModelOptionReqVO {

    @Schema(description = "选项标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "选项标签不能为空")
    private String label;

    @Schema(description = "选项值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "选项值不能为空")
    private String value;
}
