package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import lombok.Data;

@Data
public class CourtCaseLegalSummaryRespVO {

    private Long legalCount;

    private Long litigationCount;

    private Long pendingFilingCount;

    private Long rejectedCount;

    private Long archivedCount;
}
