package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourtCaseLegalCaseRespVO {

    private Long id;
    private String caseNo;
    private String orderNo;
    private String customerName;
    private String mobile;
    private BigDecimal amount;
    private String currentStage;
    private String filingStatus;
    private LocalDateTime filingSubmitTime;
    private Integer evidenceCount;
    private Integer petitionCount;
    private LocalDateTime createTime;
}
