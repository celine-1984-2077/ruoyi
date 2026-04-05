package cn.iocoder.yudao.module.courtcase.controller.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 案件模型字段选项 Response VO")
@Data
public class CourtCaseModelOptionRespVO {

    private String label;

    private String value;
}
