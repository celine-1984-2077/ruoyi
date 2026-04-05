package cn.iocoder.yudao.module.courtcase.controller.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 案件模型字段 Response VO")
@Data
public class CourtCaseModelFieldRespVO {

    private String fieldCode;

    private String fieldLabel;

    private String fieldType;

    private Boolean required;

    private Boolean enabled;

    private Boolean deployed;

    private String defaultValue;

    private Integer sortNo;

    private List<CourtCaseModelOptionRespVO> options;
}
