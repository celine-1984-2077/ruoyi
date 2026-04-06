package cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - 案件更新 Request VO")
@Data
public class CourtCaseUpdateReqVO {

    @Schema(description = "案件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "案件编号不能为空")
    private Long id;

    @Schema(description = "案件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "CASE-20260403-001")
    @NotBlank(message = "案件编号不能为空")
    private String caseNo;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ORDER-20260403-001")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "平台")
    private String platformName;

    @Schema(description = "供应商")
    private String supplierName;

    @Schema(description = "所属客服")
    private String serviceOwnerName;

    @Schema(description = "快递单号")
    private String expressNo;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "套餐信息")
    private String packageInfo;

    @Schema(description = "租赁方式")
    private String leaseMode;

    @Schema(description = "客户姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "客户姓名不能为空")
    private String customerName;

    @Schema(description = "身份证号")
    private String idCardNo;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "联系电话不能为空")
    private String mobile;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "涉案金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "5999.00")
    @NotNull(message = "涉案金额不能为空")
    private BigDecimal amount;

    @Schema(description = "总租金")
    private BigDecimal totalRentAmount;

    @Schema(description = "已付押金")
    private BigDecimal paidDepositAmount;

    @Schema(description = "剩余押金")
    private BigDecimal remainingDepositAmount;

    @Schema(description = "期付金额")
    private BigDecimal installmentAmount;

    @Schema(description = "期付次数")
    private Integer installmentCount;

    @Schema(description = "逾期天数")
    private Integer overdueDays;

    @Schema(description = "剩余天数")
    private Integer remainingDays;

    @Schema(description = "剩余未还金额")
    private BigDecimal remainingUnpaidAmount;

    @Schema(description = "逾期类型")
    private String overdueType;

    @Schema(description = "应还款日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "应还款日期不能为空")
    private LocalDate repaymentDueDate;

    @Schema(description = "户籍地址")
    private String domicileAddress;

    @Schema(description = "收货地址")
    private String shippingAddress;

    @Schema(description = "客户状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING_REPAY")
    @NotBlank(message = "客户状态不能为空")
    private String customerStatus;

    @Schema(description = "扩展字段 JSON", example = "{\"deviceModel\":\"iPhone 15\"}")
    private String extJson;
}
