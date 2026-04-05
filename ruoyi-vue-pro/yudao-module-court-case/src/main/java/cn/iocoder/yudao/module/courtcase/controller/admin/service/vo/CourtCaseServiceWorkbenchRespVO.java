package cn.iocoder.yudao.module.courtcase.controller.admin.service.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客服工作台案件 Response VO")
@Data
public class CourtCaseServiceWorkbenchRespVO {

    private Long id;
    private String caseNo;
    private String orderNo;
    private String customerName;
    private String mobile;
    private BigDecimal amount;
    private LocalDate repaymentDueDate;
    private Integer overdueDays;
    private String overdueStatus;
    private String reminderStatus;
    private String priority;
    private String customerStatus;
    private String currentStage;
    private Long currentAssigneeId;
    private LocalDateTime nextRemindTime;
    private String nextRemindContent;
    private LocalDateTime lastFollowUpTime;
    private Boolean repaid;
    private Boolean manualReminderDue;
    private Boolean canTransferLegal;
    private LocalDateTime createTime;
}
