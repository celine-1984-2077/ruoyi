package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourtCaseFilingRespVO {

    private Long id;
    private Long caseId;
    private String courtName;
    private String filingNo;
    private LocalDateTime submitTime;
    private String status;
    private String rejectReason;
    private Boolean evidenceLocked;
}
