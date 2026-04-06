package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CourtCaseEvidenceCreateReqVO {

    @NotNull
    private Long caseId;

    @NotBlank
    private String category;

    @Schema(description = "上传后的文件地址，多个用英文逗号分隔")
    @NotBlank
    private String fileUrls;
}
