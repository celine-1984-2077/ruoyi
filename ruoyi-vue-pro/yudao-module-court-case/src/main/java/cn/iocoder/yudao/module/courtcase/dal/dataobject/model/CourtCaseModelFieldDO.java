package cn.iocoder.yudao.module.courtcase.dal.dataobject.model;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@TableName("court_case_model_field")
@KeySequence("court_case_model_field_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CourtCaseModelFieldDO extends BaseDO {

    @TableId
    private Long id;

    private Long modelId;

    private String fieldCode;

    private String fieldLabel;

    private String fieldType;

    private Boolean required;

    private Boolean enabled;

    private Boolean deployed;

    private String defaultValue;

    private String optionsJson;

    private Integer sortNo;
}
