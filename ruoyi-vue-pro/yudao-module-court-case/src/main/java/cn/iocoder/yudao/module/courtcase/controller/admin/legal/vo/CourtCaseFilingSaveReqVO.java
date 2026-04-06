package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class CourtCaseFilingSaveReqVO {

    @NotNull
    private Long caseId;

    private String courtName;

    private String filingNo;

    private LocalDateTime submitTime;

    @NotBlank
    private String status;

    private String rejectReason;
}
