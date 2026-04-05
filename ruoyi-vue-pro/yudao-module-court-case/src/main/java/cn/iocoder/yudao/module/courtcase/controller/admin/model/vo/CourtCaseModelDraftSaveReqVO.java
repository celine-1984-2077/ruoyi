package cn.iocoder.yudao.module.courtcase.controller.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - 案件模型草稿保存 Request VO")
@Data
public class CourtCaseModelDraftSaveReqVO {

    @Schema(description = "草稿编号")
    private Long id;

    @Schema(description = "本次变更说明", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "变更说明不能为空")
    private String changeSummary;

    @Schema(description = "字段列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "至少保留一个扩展字段")
    private List<CourtCaseModelFieldSaveReqVO> fields;
}
