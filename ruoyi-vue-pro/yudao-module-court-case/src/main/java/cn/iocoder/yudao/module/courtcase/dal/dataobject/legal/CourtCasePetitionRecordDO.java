package cn.iocoder.yudao.module.courtcase.dal.dataobject.legal;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@TableName("court_case_petition_record")
@KeySequence("court_case_petition_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CourtCasePetitionRecordDO extends BaseDO {

    @TableId
    private Long id;

    private Long caseId;

    private Long templateId;

    private String templateName;

    private String outputType;

    private Integer versionNo;

    private Long fileId;

    private String fileName;

    private String fileUrl;

    private String generatedContent;

    private Boolean overwritten;
}
