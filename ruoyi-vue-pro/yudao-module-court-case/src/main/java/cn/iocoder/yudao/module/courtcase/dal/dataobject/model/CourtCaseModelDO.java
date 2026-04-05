package cn.iocoder.yudao.module.courtcase.dal.dataobject.model;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@TableName("court_case_model")
@KeySequence("court_case_model_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CourtCaseModelDO extends BaseDO {

    @TableId
    private Long id;

    private Integer versionNo;

    private String status;

    private String changeSummary;

    private String impactSummary;

    private LocalDateTime publishedTime;
}
