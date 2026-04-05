package cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 案件分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CourtCasePageReqVO extends PageParam {

    @Schema(description = "案件编号", example = "CASE-20260403-001")
    private String caseNo;

    @Schema(description = "订单号", example = "ORDER-20260403-001")
    private String orderNo;

    @Schema(description = "客户姓名", example = "张三")
    private String customerName;

    @Schema(description = "当前阶段", example = "ASSIGN")
    private String currentStage;

    @Schema(description = "当前部门编号", example = "119")
    private Long currentDeptId;

    @Schema(description = "当前负责人编号", example = "145")
    private Long currentAssigneeId;

    @Schema(description = "案件状态", example = "10")
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;
}
