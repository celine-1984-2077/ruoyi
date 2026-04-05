package cn.iocoder.yudao.module.courtcase.controller.admin.service.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CourtCaseFollowUpCreateReqVO {

    @NotNull(message = "案件编号不能为空")
    private Long caseId;

    @NotBlank(message = "跟进备注不能为空")
    @Size(max = 500, message = "跟进备注不能超过 500 字")
    @Schema(description = "跟进内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "附件地址，逗号分隔")
    private String attachmentUrls;
}
