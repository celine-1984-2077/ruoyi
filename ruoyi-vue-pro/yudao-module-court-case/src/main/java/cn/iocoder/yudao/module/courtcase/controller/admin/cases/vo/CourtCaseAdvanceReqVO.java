package cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 案件推进 Request VO")
@Data
public class CourtCaseAdvanceReqVO {

    @Schema(description = "案件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "案件编号不能为空")
    private Long id;

    @Schema(description = "执行动作", requiredMode = Schema.RequiredMode.REQUIRED, example = "SUBMIT_ASSIGN")
    @NotBlank(message = "执行动作不能为空")
    private String action;

    @Schema(description = "下一负责人编号", example = "149")
    private Long nextAssigneeId;

    @Schema(description = "备注", example = "分单完成，移交预提醒")
    private String remark;
}
