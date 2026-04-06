package cn.iocoder.yudao.module.courtcase.dal.dataobject.legal;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@TableName("court_case_evidence")
@KeySequence("court_case_evidence_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CourtCaseEvidenceDO extends BaseDO {

    @TableId
    private Long id;

    private Long caseId;

    private String category;

    private Long fileId;

    private String fileName;

    private String fileUrl;

    private String fileType;

    private Long fileSize;
}
