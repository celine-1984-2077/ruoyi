package cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 案件 Response VO")
@Data
public class CourtCaseRespVO {

    private Long id;

    private String caseNo;

    private String orderNo;

    private String customerName;

    private String mobile;

    private BigDecimal amount;

    private LocalDate repaymentDueDate;

    private String currentStage;

    private Long currentDeptId;

    private Long currentAssigneeId;

    private String customerStatus;

    private LocalDateTime lastFollowUpTime;

    private LocalDateTime nextRemindTime;

    private String nextRemindContent;

    private Boolean repaid;

    private LocalDateTime repaidTime;

    private Long legalReceiverId;

    private LocalDateTime transferTime;

    private LocalDateTime transferRecallDeadline;

    private Integer status;

    private String extJson;

    private LocalDateTime createTime;
}
