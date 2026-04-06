package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CourtCasePetitionOverrideReqVO {

    @NotNull
    private Long recordId;

    @NotBlank
    private String fileUrl;
}
