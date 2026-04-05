package cn.iocoder.yudao.module.courtcase.dal.dataobject.cases;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@TableName("court_case_flow_log")
@KeySequence("court_case_flow_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CourtCaseFlowLogDO extends BaseDO {

    @TableId
    private Long id;

    private Long caseId;

    private String action;

    private String fromStage;

    private String toStage;

    private Long operatorId;

    private String remark;
}
