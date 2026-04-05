package cn.iocoder.yudao.module.courtcase.controller.admin.service.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 客服工作台分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CourtCaseServiceWorkbenchPageReqVO extends PageParam {

    @Schema(description = "案件编号", example = "CASE-20260403-001")
    private String caseNo;

    @Schema(description = "客户姓名", example = "张三")
    private String customerName;
}
