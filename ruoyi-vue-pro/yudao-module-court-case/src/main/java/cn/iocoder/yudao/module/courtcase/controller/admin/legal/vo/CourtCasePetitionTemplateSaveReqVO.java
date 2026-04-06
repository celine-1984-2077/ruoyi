package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CourtCasePetitionTemplateSaveReqVO {

    private Long id;

    @NotBlank
    private String name;

    private String templateFileUrl;

    @NotBlank
    private String templateContent;

    private String placeholderDesc;

    private Boolean enabled;
}
