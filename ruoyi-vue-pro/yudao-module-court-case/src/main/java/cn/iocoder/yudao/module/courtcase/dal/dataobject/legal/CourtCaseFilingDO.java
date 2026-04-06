package cn.iocoder.yudao.module.courtcase.dal.dataobject.legal;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@TableName("court_case_filing")
@KeySequence("court_case_filing_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CourtCaseFilingDO extends BaseDO {

    @TableId
    private Long id;

    private Long caseId;

    private String courtName;

    private String filingNo;

    private LocalDateTime submitTime;

    private String status;

    private String rejectReason;

    private String integrationSnapshot;
}
