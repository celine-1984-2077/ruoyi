package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CourtCasePetitionGenerateReqVO {

    @NotNull
    private Long caseId;

    @NotNull
    private Long templateId;
}
