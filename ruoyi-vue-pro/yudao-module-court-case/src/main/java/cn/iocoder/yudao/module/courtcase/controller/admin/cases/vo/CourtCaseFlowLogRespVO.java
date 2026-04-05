package cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 案件流转日志 Response VO")
@Data
public class CourtCaseFlowLogRespVO {

    private Long id;

    private Long caseId;

    private String action;

    private String fromStage;

    private String toStage;

    private Long operatorId;

    private String remark;

    private LocalDateTime createTime;
}
