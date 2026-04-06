package cn.iocoder.yudao.module.courtcase.dal.dataobject.cases;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("court_case")
@KeySequence("court_case_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CourtCaseDO extends BaseDO {

    @TableId
    private Long id;

    private String caseNo;

    private String orderNo;

    private String contractNo;

    private String companyName;

    private String platformName;

    private String supplierName;

    private String serviceOwnerName;

    private String expressNo;

    private String productName;

    private String packageInfo;

    private String leaseMode;

    private String customerName;

    private String idCardNo;

    private String mobile;

    private String gender;

    private Integer age;

    private BigDecimal amount;

    private BigDecimal totalRentAmount;

    private BigDecimal paidDepositAmount;

    private BigDecimal remainingDepositAmount;

    private BigDecimal installmentAmount;

    private Integer installmentCount;

    private Integer overdueDays;

    private Integer remainingDays;

    private BigDecimal remainingUnpaidAmount;

    private String overdueType;

    private LocalDate repaymentDueDate;

    private String domicileAddress;

    private String shippingAddress;

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
}
