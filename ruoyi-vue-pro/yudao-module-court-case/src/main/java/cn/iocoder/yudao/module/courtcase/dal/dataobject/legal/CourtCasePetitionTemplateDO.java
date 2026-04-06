package cn.iocoder.yudao.module.courtcase.dal.dataobject.legal;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@TableName("court_case_petition_template")
@KeySequence("court_case_petition_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CourtCasePetitionTemplateDO extends BaseDO {

    @TableId
    private Long id;

    private String name;

    private Long templateFileId;

    private String templateFileName;

    private String templateFileUrl;

    private String templateContent;

    private String placeholderDesc;

    private Boolean enabled;
}
