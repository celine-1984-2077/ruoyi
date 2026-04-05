package cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - 案件创建 Request VO")
@Data
public class CourtCaseCreateReqVO {

    @Schema(description = "案件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "CASE-20260403-001")
    @NotBlank(message = "案件编号不能为空")
    private String caseNo;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ORDER-20260403-001")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @Schema(description = "客户姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "客户姓名不能为空")
    private String customerName;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "联系电话不能为空")
    private String mobile;

    @Schema(description = "涉案金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "5999.00")
    @NotNull(message = "涉案金额不能为空")
    private BigDecimal amount;

    @Schema(description = "应还款日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "应还款日期不能为空")
    private LocalDate repaymentDueDate;

    @Schema(description = "当前负责人编号", example = "145")
    private Long currentAssigneeId;

    @Schema(description = "当前阶段扩展字段 JSON", example = "{\"deviceModel\":\"iPhone 15\"}")
    private String extJson;
}
