package cn.iocoder.yudao.module.courtcase.controller.admin.service.vo;

import lombok.Data;

@Data
public class CourtCaseServiceWorkbenchSummaryRespVO {

    private Long remindCount;
    private Long highPriorityCount;
    private Long overdueCount;
    private Long transferredCount;
    private Long manualReminderDueCount;
}
