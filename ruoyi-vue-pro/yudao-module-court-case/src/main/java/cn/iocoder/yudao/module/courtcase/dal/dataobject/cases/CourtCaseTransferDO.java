package cn.iocoder.yudao.module.courtcase.dal.dataobject.cases;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@TableName("court_case_transfer")
@KeySequence("court_case_transfer_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CourtCaseTransferDO extends BaseDO {

    @TableId
    private Long id;

    private Long caseId;

    private Long fromUserId;

    private Long toUserId;

    private String reason;

    private String extraNote;

    private String status;

    private LocalDateTime transferTime;

    private LocalDateTime recallDeadline;

    private LocalDateTime recallTime;

    private Long recalledBy;
}
